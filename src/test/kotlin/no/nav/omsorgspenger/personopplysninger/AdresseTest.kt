package no.nav.omsorgspenger.personopplysninger

import org.junit.jupiter.api.Test

class AdresseTest {

    @Test
    fun `Tolker adresser som tenkt`() {
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

}