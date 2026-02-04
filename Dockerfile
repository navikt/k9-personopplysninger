FROM ghcr.io/navikt/sif-baseimages/java-chainguard-21:2026.01.29.1157Z
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "app.jar" ]
