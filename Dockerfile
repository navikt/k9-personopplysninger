FROM ghcr.io/navikt/sif-baseimages/java-25:2026.01.29.1157z
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "-jar", "app.jar" ]