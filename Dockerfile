FROM navikt/java:15
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger
COPY build/libs/*.jar app.jar
