FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.06.24.1245Z
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "-jar", "app.jar" ]
