package no.nav.omsorgspenger.personopplysninger

import io.prometheus.client.Counter
import org.slf4j.LoggerFactory

private object PersonopplysningerMetrics {

    val logger = LoggerFactory.getLogger(PersonopplysningerMetrics::class.java)

    val mottattBehov: Counter = Counter
            .build("omsorgspenger_behov_mottatt_total", "Antal behov mottatt")
            .labelNames("behov")
            .register()

    val feilBehovBehandling: Counter = Counter
            .build("omsorgspenger_behov_feil_total", "Antal feil vid behandling av behov")
            .labelNames("behov")
            .register()

    val behovBehandlet: Counter = Counter
            .build("omsorgspenger_behov_behandlet_total", "Antal lyckade behandlinger av behov")
            .labelNames("behov")
            .register()

}

private fun safeMetric(block: () -> Unit) = try {
    block()
} catch (cause: Throwable) {
    PersonopplysningerMetrics.logger.warn("Feil ved å rapportera metrics", cause)
}

internal fun incLostBehov(behov: String) {
    safeMetric { PersonopplysningerMetrics.behovBehandlet.labels(behov).inc() }
}

internal fun incMottattBehov(behov: String) {
    safeMetric { PersonopplysningerMetrics.mottattBehov.labels(behov).inc() }
}

internal fun incBehandlingFeil(behov: String) {
    safeMetric { PersonopplysningerMetrics.feilBehovBehandling.labels(behov).inc() }
}