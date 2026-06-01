package com.vinithius.dex10.datasource.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TcgNameMappingTest {

    // ---------------------------------------------------------------------------
    // formatPokemonNameForSearch
    // ---------------------------------------------------------------------------

    @Test fun `common pokemon capitalises first letter`() {
        assertEquals("Pikachu", PokemonRepository.formatPokemonNameForSearch("pikachu"))
        assertEquals("Charizard", PokemonRepository.formatPokemonNameForSearch("charizard"))
        assertEquals("Bulbasaur", PokemonRepository.formatPokemonNameForSearch("bulbasaur"))
    }

    @Test fun `form variants strip form suffix`() {
        assertEquals("Pikachu", PokemonRepository.formatPokemonNameForSearch("pikachu-alola"))
        assertEquals("Mewtwo", PokemonRepository.formatPokemonNameForSearch("mewtwo-mega-x"))
        assertEquals("Charizard", PokemonRepository.formatPokemonNameForSearch("charizard-mega-x"))
        assertEquals("Meowth", PokemonRepository.formatPokemonNameForSearch("meowth-galar"))
    }

    @Test fun `gender variants map to special characters`() {
        assertEquals("Nidoran♀", PokemonRepository.formatPokemonNameForSearch("nidoran-f"))
        assertEquals("Nidoran♂", PokemonRepository.formatPokemonNameForSearch("nidoran-m"))
    }

    @Test fun `punctuation overrides`() {
        assertEquals("Ho-Oh", PokemonRepository.formatPokemonNameForSearch("ho-oh"))
        assertEquals("Mr. Mime", PokemonRepository.formatPokemonNameForSearch("mr-mime"))
        assertEquals("Mr. Rime", PokemonRepository.formatPokemonNameForSearch("mr-rime"))
        assertEquals("Mime Jr.", PokemonRepository.formatPokemonNameForSearch("mime-jr"))
        assertEquals("Porygon-Z", PokemonRepository.formatPokemonNameForSearch("porygon-z"))
        assertEquals("Type: Null", PokemonRepository.formatPokemonNameForSearch("type-null"))
        assertEquals("Jangmo-o", PokemonRepository.formatPokemonNameForSearch("jangmo-o"))
        assertEquals("Hakamo-o", PokemonRepository.formatPokemonNameForSearch("hakamo-o"))
        assertEquals("Kommo-o", PokemonRepository.formatPokemonNameForSearch("kommo-o"))
        assertEquals("Sirfetch'd", PokemonRepository.formatPokemonNameForSearch("sirfetchd"))
        assertEquals("Farfetch'd", PokemonRepository.formatPokemonNameForSearch("farfetchd"))
    }

    @Test fun `accented name`() {
        assertEquals("Flabébé", PokemonRepository.formatPokemonNameForSearch("flabebe"))
    }

    @Test fun `tapu family becomes two words`() {
        assertEquals("Tapu Koko", PokemonRepository.formatPokemonNameForSearch("tapu-koko"))
        assertEquals("Tapu Lele", PokemonRepository.formatPokemonNameForSearch("tapu-lele"))
        assertEquals("Tapu Bulu", PokemonRepository.formatPokemonNameForSearch("tapu-bulu"))
        assertEquals("Tapu Fini", PokemonRepository.formatPokemonNameForSearch("tapu-fini"))
    }

    @Test fun `paradox iron family becomes two words`() {
        assertEquals("Iron Bundle", PokemonRepository.formatPokemonNameForSearch("iron-bundle"))
        assertEquals("Iron Hands", PokemonRepository.formatPokemonNameForSearch("iron-hands"))
        assertEquals("Iron Valiant", PokemonRepository.formatPokemonNameForSearch("iron-valiant"))
        assertEquals("Iron Moth", PokemonRepository.formatPokemonNameForSearch("iron-moth"))
    }

    @Test fun `paradox ruinous quartet`() {
        assertEquals("Chien-Pao", PokemonRepository.formatPokemonNameForSearch("chien-pao"))
        assertEquals("Chi-Yu", PokemonRepository.formatPokemonNameForSearch("chi-yu"))
        assertEquals("Ting-Lu", PokemonRepository.formatPokemonNameForSearch("ting-lu"))
        assertEquals("Wo-Chien", PokemonRepository.formatPokemonNameForSearch("wo-chien"))
    }

    @Test fun `gender forms with shared TCG name`() {
        assertEquals("Indeedee", PokemonRepository.formatPokemonNameForSearch("indeedee-f"))
        assertEquals("Indeedee", PokemonRepository.formatPokemonNameForSearch("indeedee-m"))
        assertEquals("Meowstic", PokemonRepository.formatPokemonNameForSearch("meowstic-f"))
        assertEquals("Meowstic", PokemonRepository.formatPokemonNameForSearch("meowstic-m"))
    }

    @Test fun `urshifu forms map to base`() {
        assertEquals("Urshifu", PokemonRepository.formatPokemonNameForSearch("urshifu-single-strike"))
        assertEquals("Urshifu", PokemonRepository.formatPokemonNameForSearch("urshifu-rapid-strike"))
    }

    @Test fun `lycanroc forms map to base`() {
        assertEquals("Lycanroc", PokemonRepository.formatPokemonNameForSearch("lycanroc-midday"))
        assertEquals("Lycanroc", PokemonRepository.formatPokemonNameForSearch("lycanroc-midnight"))
        assertEquals("Lycanroc", PokemonRepository.formatPokemonNameForSearch("lycanroc-dusk"))
    }

    @Test fun `calyrex riders map to base`() {
        assertEquals("Calyrex", PokemonRepository.formatPokemonNameForSearch("calyrex-ice"))
        assertEquals("Calyrex", PokemonRepository.formatPokemonNameForSearch("calyrex-shadow"))
    }

    @Test fun `case insensitive input`() {
        assertEquals("Pikachu", PokemonRepository.formatPokemonNameForSearch("PIKACHU"))
        assertEquals("Mr. Mime", PokemonRepository.formatPokemonNameForSearch("MR-MIME"))
        assertEquals("Ho-Oh", PokemonRepository.formatPokemonNameForSearch("HO-OH"))
    }

    // ---------------------------------------------------------------------------
    // isTcgCardForPokemon — filter boundary tests
    // ---------------------------------------------------------------------------

    @Test fun `exact name matches`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Pikachu", "Pikachu"))
    }

    @Test fun `card suffix with space matches`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Pikachu V", "Pikachu"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Pikachu VMAX", "Pikachu"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Pikachu VSTAR", "Pikachu"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Charizard ex", "Charizard"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Mewtwo GX", "Mewtwo"))
    }

    @Test fun `card suffix with hyphen matches`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Charizard-EX", "Charizard"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Mewtwo-GX", "Mewtwo"))
    }

    @Test fun `mr mime does NOT match mr mime jr`() {
        assertFalse(PokemonRepository.isTcgCardForPokemon("Mr. Mime Jr.", "Mr. Mime"))
    }

    @Test fun `mime jr matches mime jr card`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Mime Jr.", "Mime Jr."))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Mime Jr. ex", "Mime Jr."))
    }

    @Test fun `nidoran female does NOT match nidoran male`() {
        assertFalse(PokemonRepository.isTcgCardForPokemon("Nidoran♂", "Nidoran♀"))
        assertFalse(PokemonRepository.isTcgCardForPokemon("Nidoran♀", "Nidoran♂"))
    }

    @Test fun `nidoran matches own card`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Nidoran♀", "Nidoran♀"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Nidoran♂", "Nidoran♂"))
    }

    @Test fun `case insensitive filter`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("pikachu v", "Pikachu"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("CHARIZARD ex", "Charizard"))
    }

    @Test fun `unrelated card does not match`() {
        assertFalse(PokemonRepository.isTcgCardForPokemon("Mr. Briney's Compassion", "Mr. Mime"))
        assertFalse(PokemonRepository.isTcgCardForPokemon("Raichu", "Pikachu"))
        assertFalse(PokemonRepository.isTcgCardForPokemon("Pikablu", "Pikachu"))
    }

    @Test fun `tapu card matches tapu two-word name`() {
        assertTrue(PokemonRepository.isTcgCardForPokemon("Tapu Koko", "Tapu Koko"))
        assertTrue(PokemonRepository.isTcgCardForPokemon("Tapu Koko GX", "Tapu Koko"))
        assertFalse(PokemonRepository.isTcgCardForPokemon("Tapu Lele GX", "Tapu Koko"))
    }
}
