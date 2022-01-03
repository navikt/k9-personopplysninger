FROM navikt/java:17
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger
COPY build/libs/app.jar .
