package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.inneholderMinstEn
import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.somAdresse
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals

internal class AdresseTest {

    @Test
    fun `inneholder minst en adresse`() {
        val person1Adresser = listOf(
            Adresse(matrikkelId = "1", adressenavn = null),
            Adresse(matrikkelId = null, adressenavn = "Adressà")
        )

        val person2Adresser = listOf(
            Adresse(matrikkelId = null, adressenavn = "Adressà")
        )

        // En av adressene matcher
        assertTrue(person1Adresser.inneholderMinstEn(person2Adresser))

        val person3Adresser = listOf(
            Adresse(matrikkelId = "1234", adressenavn = "Adresse")
        )
        val person4Adresser = listOf(
            Adresse(matrikkelId = "4567", adressenavn = "Adresse")
        )

        // Samme adressenavn men forskjellige matrikkelId
        assertFalse(person3Adresser.inneholderMinstEn(person4Adresser))

        val person5Adresser = listOf(
            Adresse(matrikkelId = null, adressenavn = "ØKLANDSVEGEN")
        )
        val person6Adresser = listOf(
            Adresse(matrikkelId = null, adressenavn = "FOSSEKROAVEGEN")
        )

        // Forskjellige adressenavn
        assertFalse(person5Adresser.inneholderMinstEn(person6Adresser))

        val person7Adresser = listOf(
            Adresse(matrikkelId = "1234", adressenavn = "en adresse")
        )
        val person8Adresser = listOf(
            Adresse(matrikkelId = "1234", adressenavn = "en annen adresse")
        )

        // Samme matrikkelId
        assertTrue(person7Adresser.inneholderMinstEn(person8Adresser))
    }

    @Test
    fun `sammenligne adresser`() {
        val adresse1 = Adresse(matrikkelId = null, adressenavn = "ØKLANDSVEGEN")
        val adresse2 = Adresse(matrikkelId = null, adressenavn = "FOSSEKROAVEGEN")
        assertNotEquals(adresse1, adresse2)
    }

    @Test
    fun `mapping fra PDL vegadresse`() {
        val vegadresse1 = HentRelasjonPdlResponse.VegAdresse(vegadresse = null)
        assertNull(vegadresse1.somAdresse())
        val vegadresse2 = HentRelasjonPdlResponse.VegAdresse(vegadresse = HentRelasjonPdlResponse.VegAdresseInfo(
            matrikkelId = null, adressenavn = null
        ))
        assertNull(vegadresse2.somAdresse())
        val vegadresse3 = HentRelasjonPdlResponse.VegAdresse(vegadresse = HentRelasjonPdlResponse.VegAdresseInfo(
            matrikkelId = null, adressenavn = " adresse nummer en "
        ))
        val vegadresse4 = HentRelasjonPdlResponse.VegAdresse(vegadresse = HentRelasjonPdlResponse.VegAdresseInfo(
            matrikkelId = null, adressenavn = "ADRESSE nuMMer EN"
        ))
        assertNotNull(vegadresse3.somAdresse())
        assertNotNull(vegadresse4.somAdresse())
        assertEquals(vegadresse3.somAdresse(), vegadresse4.somAdresse())
    }
}