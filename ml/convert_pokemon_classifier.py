"""
Converts the HuggingFace Pokemon classifier (ViT, Gen 1-9, 1025 classes) to a
quantized INT8 ONNX model for on-device inference in the Android app.

Model: imzynoxprince/pokemons-image-classifier-gen1-gen9 (Apache-2.0)
  - google/vit-base-patch16-224 fine-tuned, labels 0..1024 follow National Dex
    order (class index + 1 == dex id). Indices >= 1025 are training artifacts
    and fan-made entries, ignored by the app.

Outputs (in ml/out/, gitignored):
  - model_fp32.onnx          full precision export (intermediate)
  - pokemon_classifier_int8.onnx  dynamic-quantized model shipped to users
  - scanner_labels.json      label list copied into app assets

Usage:
  pip install "optimum[onnxruntime]" pillow requests
  python ml/convert_pokemon_classifier.py
"""
import json
import shutil
import sys
from pathlib import Path

MODEL_ID = "imzynoxprince/pokemons-image-classifier-gen1-gen9"
NUM_POKEMON = 1025  # classes beyond this are dataset artifacts
HERE = Path(__file__).parent
OUT = HERE / "out"
ASSETS = HERE.parent / "app" / "src" / "main" / "assets"


def export_onnx() -> Path:
    import torch
    from transformers import ViTForImageClassification

    export_dir = OUT / "onnx_fp32"
    export_dir.mkdir(parents=True, exist_ok=True)
    onnx_path = export_dir / "model.onnx"
    print(f"Loading {MODEL_ID}...")
    # eager attention: torch 2.2 cannot export scaled_dot_product_attention to ONNX
    model = ViTForImageClassification.from_pretrained(MODEL_ID, attn_implementation="eager")
    model.eval()
    (export_dir / "config.json").write_text(model.config.to_json_string(), encoding="utf-8")
    if not onnx_path.exists():
        print("Exporting to ONNX...")
        torch.onnx.export(
            model,
            torch.randn(1, 3, 224, 224),
            str(onnx_path),
            input_names=["pixel_values"],
            output_names=["logits"],
            dynamic_axes={"pixel_values": {0: "batch"}, "logits": {0: "batch"}},
            opset_version=17,
        )
    return onnx_path


def quantize(fp32_path: Path) -> Path:
    from onnxruntime.quantization import QuantType, quantize_dynamic

    int8_path = OUT / "pokemon_classifier_int8.onnx"
    print("Quantizing to INT8 (dynamic)...")
    quantize_dynamic(
        model_input=str(fp32_path),
        model_output=str(int8_path),
        weight_type=QuantType.QUInt8,
        extra_options={"EnableSubgraph": True},
    )
    mb = int8_path.stat().st_size / 1024 / 1024
    print(f"INT8 model: {int8_path} ({mb:.1f} MB)")
    return int8_path


def write_labels(export_dir: Path) -> None:
    config = json.loads((export_dir / "config.json").read_text(encoding="utf-8"))
    id2label = config["id2label"]
    labels = [id2label[str(i)] for i in range(NUM_POKEMON)]
    # Spot-check the National Dex ordering assumption (index + 1 == dex id).
    expected = {0: "Bulbasaur", 24: "Pikachu", 1024: "Pecharunt"}
    for idx, name in expected.items():
        assert labels[idx] == name, f"label[{idx}] == {labels[idx]!r}, expected {name!r}"
    out_file = ASSETS / "scanner_labels.json"
    out_file.write_text(
        json.dumps(labels, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"Labels asset: {out_file} ({len(labels)} entries)")


def preprocess(img):
    """Mirrors the on-device preprocessing: 224x224 bicubic, (p/255 - .5)/.5."""
    import numpy as np
    from PIL import Image

    img = img.convert("RGB").resize((224, 224), Image.BICUBIC)
    arr = (np.asarray(img, dtype=np.float32) / 255.0 - 0.5) / 0.5
    return arr.transpose(2, 0, 1)[None]  # NCHW


def sanity_check(int8_path: Path) -> None:
    import io

    import numpy as np
    import onnxruntime as ort
    import requests
    from PIL import Image

    art = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/{}.png"
    cases = [
        (1, art.format(1), "official artwork Bulbasaur"),
        (6, art.format(6), "official artwork Charizard"),
        (25, art.format(25), "official artwork Pikachu"),
        (130, art.format(130), "official artwork Gyarados"),
        (448, art.format(448), "official artwork Lucario"),
        (658, art.format(658), "official artwork Greninja"),
        (887, art.format(887), "official artwork Dragapult"),
        (1008, art.format(1008), "official artwork Miraidon"),
        (25, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png", "in-game pixel sprite Pikachu"),
        (6, "https://images.pokemontcg.io/base1/4_hires.png", "TCG card Charizard (full card)"),
    ]
    session = ort.InferenceSession(str(int8_path), providers=["CPUExecutionProvider"])
    input_name = session.get_inputs()[0].name
    labels = json.loads((ASSETS / "scanner_labels.json").read_text(encoding="utf-8"))

    passed = 0
    for dex_id, url, desc in cases:
        img = Image.open(io.BytesIO(requests.get(url, timeout=60).content))
        if "full card" in desc:
            # Crop the illustration window of a classic TCG card layout.
            w, h = img.size
            img = img.crop((int(w * 0.1), int(h * 0.1), int(w * 0.9), int(h * 0.55)))
        logits = session.run(None, {input_name: preprocess(img)})[0][0]
        top = np.argsort(logits)[::-1][:3]
        exp = np.exp(logits - logits.max())
        probs = exp / exp.sum()
        names = [(labels[i] if i < NUM_POKEMON else f"<artifact {i}>", probs[i]) for i in top]
        ok = top[0] == dex_id - 1
        passed += ok
        print(f"{'PASS' if ok else 'FAIL'} {desc}: " + ", ".join(f"{n} {p:.2f}" for n, p in names))
    print(f"\n{passed}/{len(cases)} top-1 correct")
    if passed < len(cases) - 2:
        sys.exit("Sanity check failed: model quality below expectations.")


def main() -> None:
    OUT.mkdir(exist_ok=True)
    fp32 = export_onnx()
    write_labels(fp32.parent)
    int8 = quantize(fp32)
    sanity_check(int8)
    print("\nDone. Upload ml/out/pokemon_classifier_int8.onnx as a GitHub release asset")
    print("and keep the URL in sync with ScannerModelManager.MODEL_URL.")


if __name__ == "__main__":
    main()
