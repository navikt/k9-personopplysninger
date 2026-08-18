FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.08.18.0742Z
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "-jar", "app.jar" ]
