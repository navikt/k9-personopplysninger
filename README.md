k9-personopplysninger
================

Tjänsten bygger på <a href="https://github.com/navikt/k9-rapid">k9-rapid</a> och har två rivers i bruk som löser behov från omsorgspenger-rammemeldinger.


<a href ="https://github.com/navikt/k9-personopplysninger/blob/master/src/main/kotlin/no/nav/omsorgspenger/personopplysninger/HentPersonopplysninger.kt">HentPersonopplysninger</a>
behandlar behovet 
<a href="https://github.com/navikt/omsorgspenger-rammemeldinger/blob/master/src/main/kotlin/no/nav/omsorgspenger/personopplysninger/HentPersonopplysningerMelding.kt">HentPersonopplysninger</a>.<br>
Personopplysningar hämtas från 
<a href="https://navikt.github.io/pdl/">Persondatalösningen</a> 
via pdl-api och formatteras om innan de sänds vidare som lösning.

<a href="https://github.com/navikt/k9-personopplysninger/blob/master/src/main/kotlin/no/nav/omsorgspenger/personopplysninger/HentRelasjoner.kt">HentRelasjoner</a> 
behandlar behovet
<a href="https://github.com/navikt/omsorgspenger-rammemeldinger/blob/master/src/main/kotlin/no/nav/omsorgspenger/overf%C3%B8ringer/rivers/InitierOverf%C3%B8ringAvOmsorgsdager.kt">VurderRelasjoner</a>.<br>



---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #sif_omsorgspenger.

