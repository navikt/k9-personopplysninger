package no.nav.omsorgspenger.personopplysninger

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AdresseTest {

    @Test
    fun `Sammenligner adresser`() {
        val tomAdresse = RelasjonMediator.Adresse(
                bostedMatrikkelId = null,
                bostedVegadresse = null,
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = null
        )
        val bostedMatrikkelId = RelasjonMediator.Adresse(
                bostedMatrikkelId = "12345",
                bostedVegadresse = null,
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = null
        )
        val annenBostedMatrikkelId = RelasjonMediator.Adresse(
                bostedMatrikkelId = "6789",
                bostedVegadresse = null,
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = null
        )
        val deltBostedMatrikkelId = RelasjonMediator.Adresse(
                bostedMatrikkelId = null,
                bostedVegadresse = null,
                deltBostedMatrikkelId = "12345",
                deltBostedVegadresse = null
        )
        val bostedVegAdresse = RelasjonMediator.Adresse(
                bostedMatrikkelId = null,
                bostedVegadresse = "sannergate 2",
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = null
        )
        val annenBostedVegAdresse = RelasjonMediator.Adresse(
                bostedMatrikkelId = null,
                bostedVegadresse = "sannergate 5",
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = null
        )
        val deltBostedVegAdresse = RelasjonMediator.Adresse(
                bostedMatrikkelId = null,
                bostedVegadresse = null,
                deltBostedMatrikkelId = null,
                deltBostedVegadresse = "sannergate 2"
        )

        assert(tomAdresse != bostedVegAdresse && tomAdresse != tomAdresse) { "Sjekker null-verdi" }
        assert(bostedMatrikkelId == deltBostedMatrikkelId) { "Like matrikkelId "}
        assert(bostedMatrikkelId != annenBostedMatrikkelId) { "Uike matrikkelId "}
        assert(deltBostedMatrikkelId == bostedMatrikkelId) { "Like matrikkelId andre veien"}
        assert(bostedVegAdresse == deltBostedVegAdresse) { "Like vegadresse "}
        assert(bostedVegAdresse != annenBostedVegAdresse) { "Ulike vegadresse "}
        assert(deltBostedVegAdresse == bostedVegAdresse) { "Like vegadresse andre veien"}
        assert(bostedMatrikkelId != bostedVegAdresse) { "matrikkelId ikke like vegadresse "}
    }

    @Test
    fun `Sjekker matrikkelId først ifall både har det`() {
        val sammeVegAdresse1 = RelasjonMediator.Adresse(
            bostedMatrikkelId = "11111",
            bostedVegadresse = "sannergate 2",
            deltBostedMatrikkelId = null,
            deltBostedVegadresse = null
        )

        val sammeVegAdresse2 = RelasjonMediator.Adresse(
            bostedMatrikkelId = "22222",
            bostedVegadresse = "sannergate 2",
            deltBostedMatrikkelId = null,
            deltBostedVegadresse = null
        )

        val sammeMatrikkelId1 = RelasjonMediator.Adresse(
            bostedMatrikkelId = "11111",
            bostedVegadresse = "sannergate 1",
            deltBostedMatrikkelId = null,
            deltBostedVegadresse = null
        )

        val sammeMatrikkelId2 = RelasjonMediator.Adresse(
            bostedMatrikkelId = "11111",
            bostedVegadresse = "sannergate 5",
            deltBostedMatrikkelId = null,
            deltBostedVegadresse = null
        )

        assertNotEquals(sammeVegAdresse1, sammeVegAdresse2)
        assertEquals(sammeMatrikkelId1, sammeMatrikkelId2)
    }

}