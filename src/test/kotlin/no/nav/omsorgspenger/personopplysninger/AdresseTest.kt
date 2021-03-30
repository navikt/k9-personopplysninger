package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.inneholderMinstEn
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

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

        assertTrue(person1Adresser.inneholderMinstEn(person2Adresser))

        val person3Adresser = listOf(
            Adresse(matrikkelId = "1234", adressenavn = "Adresse")
        )
        val person4Adresser = listOf(
            Adresse(matrikkelId = "4567", adressenavn = "Adresse")
        )

        assertFalse(person3Adresser.inneholderMinstEn(person4Adresser))

        val person5Adresser = listOf(
            Adresse(matrikkelId = null, adressenavn = "ØKLANDSVEGEN")
        )
        val person6Adresser = listOf(
            Adresse(matrikkelId = null, adressenavn = "FOSSEKROAVEGEN")
        )

        assertFalse(person5Adresser.inneholderMinstEn(person6Adresser))
    }

    @Test
    fun `sammenligne adresser`() {
        val adresse1 = Adresse(matrikkelId = null, adressenavn = "ØKLANDSVEGEN")
        val adresse2 = Adresse(matrikkelId = null, adressenavn = "FOSSEKROAVEGEN")
        assertNotEquals(adresse1, adresse2)
    }
}