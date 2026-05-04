FROM ghcr.io/navikt/sif-baseimages/java-chainguard-25:2026.04.30.1354Z
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "-jar", "app.jar" ]
