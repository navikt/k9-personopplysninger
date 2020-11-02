package no.nav.omsorgspenger.personopplysninger

import io.prometheus.client.Counter
import org.slf4j.LoggerFactory

private object PersonopplysningerMetrics {

    val logger = LoggerFactory.getLogger(PersonopplysningerMetrics::class.java)

    val pdlFeil: Counter = Counter
            .build("pdl_feil", "Feil vid hämtning av uppgifter fra PDL")
            .register()

    val lostBehov: Counter = Counter
            .build("lost_behov", "Løst behov")
            .register()

    val mottattBehov: Counter = Counter
            .build("mottatt_behov", "Mottatt behov")
            .register()
}

private fun safeMetric(block: () -> Unit) = try {
    block()
} catch (cause: Throwable) {
    PersonopplysningerMetrics.logger.warn("Feil ved å rapportera metrics", cause)
}

internal fun incLostBehov() {
    safeMetric { PersonopplysningerMetrics.lostBehov.inc() }
}

internal fun incMottattBehov() {
    safeMetric { PersonopplysningerMetrics.mottattBehov.inc() }
}

internal fun incPdlFeil() {
    safeMetric { PersonopplysningerMetrics.pdlFeil.inc() }
}