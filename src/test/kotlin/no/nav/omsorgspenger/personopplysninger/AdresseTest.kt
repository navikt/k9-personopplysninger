package no.nav.omsorgspenger.personopplysninger

import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.adresser
import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.inneholderMinstEn
import no.nav.omsorgspenger.personopplysninger.Adresse.Companion.somAdresse
import no.nav.omsorgspenger.personopplysninger.pdl.HentRelasjonPdlResponse
import org.assertj.core.api.Assertions.assertThat
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
    fun `mapping fra PDL vegadresser`() {
        val vegadresse1 = HentRelasjonPdlResponse.VegAdresse(
            matrikkelId = null, adressenavn = null
        )
        assertNull(vegadresse1.somAdresse())
        val vegadresse2 = HentRelasjonPdlResponse.VegAdresse(
            matrikkelId = null, adressenavn = null
        )
        assertNull(vegadresse2.somAdresse())
        val vegadresse3 =  HentRelasjonPdlResponse.VegAdresse(
            matrikkelId = null, adressenavn = " adresse nummer en "
        )
        val vegadresse4 = HentRelasjonPdlResponse.VegAdresse(
            matrikkelId = null, adressenavn = "ADRESSE nuMMer EN"
        )
        assertNotNull(vegadresse3.somAdresse())
        assertNotNull(vegadresse4.somAdresse())
        assertEquals(vegadresse3.somAdresse(), vegadresse4.somAdresse())
    }

    @Test
    fun `mapping fra PDL person`() {
        val person = HentRelasjonPdlResponse.Person(
            bostedsadresse = listOf(HentRelasjonPdlResponse.Adresse(
                vegadresse = HentRelasjonPdlResponse.VegAdresse(matrikkelId = "1234", adressenavn = null)
            )),
            deltBosted = listOf(HentRelasjonPdlResponse.Adresse(
                vegadresse = HentRelasjonPdlResponse.VegAdresse(matrikkelId = "4567", adressenavn = " delt bosted ")
            )),
            oppholdsadresse = listOf(HentRelasjonPdlResponse.Adresse(
                vegadresse = HentRelasjonPdlResponse.VegAdresse(matrikkelId = null, adressenavn = " oppholdsadresse ")
            )),
            kontaktadresse = listOf(HentRelasjonPdlResponse.Adresse(
                vegadresse = HentRelasjonPdlResponse.VegAdresse(matrikkelId = null, adressenavn = " kontaktadresse "),
                postadresseIFrittFormat = HentRelasjonPdlResponse.PostAdresseIFrittFormat(
                    adresselinje1 = " Ola Nordmann C/O Kari",
                    adresselinje2 = "postadresse i FRITT format 127A",
                    adresselinje3 = "0475 Grünerløkka, Oslo"
                )
            ))
        )
        assertThat(person.adresser()).hasSameElementsAs(setOf(
            Adresse(matrikkelId = "1234", adressenavn = null),
            Adresse(matrikkelId = "4567", adressenavn = null),
            Adresse(matrikkelId = null, adressenavn = "DELT BOSTED"),
            Adresse(matrikkelId = null, adressenavn = "OPPHOLDSADRESSE"),
            Adresse(matrikkelId = null, adressenavn = "KONTAKTADRESSE"),
            Adresse(matrikkelId = null, adressenavn = "OLA NORDMANN C/O KARI"),
            Adresse(matrikkelId = null, adressenavn = "POSTADRESSE I FRITT FORMAT")
        ))
    }
}