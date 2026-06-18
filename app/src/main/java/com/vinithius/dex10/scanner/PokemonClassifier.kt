package com.vinithius.dex10.scanner

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.exp

/**
 * On-device Pokémon image classifier (ViT INT8 ONNX, Gen 1-9).
 *
 * Class indexes 0..1024 follow the National Pokédex order, so `index + 1` is the
 * Pokédex id used by the detail screen. Indexes beyond 1024 are training
 * artifacts of the source model and are ignored.
 */
class PokemonClassifier(context: Context, modelFile: File) {

    data class Prediction(val dexId: Int, val name: String, val confidence: Float)

    private val environment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession =
        environment.createSession(modelFile.absolutePath, OrtSession.SessionOptions())

    private val labels: List<String> = context.assets.open(LABELS_ASSET).use { stream ->
        Gson().fromJson(InputStreamReader(stream, Charsets.UTF_8), Array<String>::class.java).toList()
    }

    fun classify(bitmap: Bitmap, topK: Int = 3): List<Prediction> {
        val input = preprocess(bitmap)
        OnnxTensor.createTensor(
            environment,
            input,
            longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
        ).use { tensor ->
            session.run(mapOf(session.inputNames.first() to tensor)).use { output ->
                @Suppress("UNCHECKED_CAST")
                val logits = (output[0].value as Array<FloatArray>)[0]
                return topPredictions(logits, topK)
            }
        }
    }

    /** Center-crop to a square, scale to 224x224 and normalize to [-1, 1] (NCHW). */
    private fun preprocess(source: Bitmap): FloatBuffer {
        val squareSize = minOf(source.width, source.height)
        val cropped = Bitmap.createBitmap(
            source,
            (source.width - squareSize) / 2,
            (source.height - squareSize) / 2,
            squareSize,
            squareSize
        )
        val scaled = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true)
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaled.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        val area = INPUT_SIZE * INPUT_SIZE
        // Direct off-heap buffer required: ONNX Runtime native code on arm64 dereferences
        // the raw pointer. A heap FloatBuffer (allocate) causes SIGABRT in convertToTensorInfo.
        val buffer = ByteBuffer.allocateDirect(3 * area * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        for (i in 0 until area) {
            val pixel = pixels[i]
            buffer.put(i, ((pixel shr 16 and 0xFF) / 127.5f) - 1f)
            buffer.put(area + i, ((pixel shr 8 and 0xFF) / 127.5f) - 1f)
            buffer.put(2 * area + i, ((pixel and 0xFF) / 127.5f) - 1f)
        }
        return buffer
    }

    private fun topPredictions(logits: FloatArray, topK: Int): List<Prediction> {
        var maxLogit = Float.NEGATIVE_INFINITY
        for (logit in logits) if (logit > maxLogit) maxLogit = logit
        val exponentials = FloatArray(logits.size)
        var sum = 0f
        for (i in logits.indices) {
            exponentials[i] = exp(logits[i] - maxLogit)
            sum += exponentials[i]
        }
        return (0 until minOf(logits.size, labels.size, NUM_POKEMON))
            .sortedByDescending { exponentials[it] }
            .take(topK)
            .map { Prediction(dexId = it + 1, name = labels[it], confidence = exponentials[it] / sum) }
    }

    fun close() {
        session.close()
    }

    companion object {
        const val INPUT_SIZE = 224
        private const val NUM_POKEMON = 1025
        private const val LABELS_ASSET = "scanner_labels.json"
    }
}
