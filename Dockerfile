FROM gcr.io/distroless/java21-debian12:latest
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

WORKDIR /app
COPY build/libs/app.jar /app/app.jar
CMD [ "app.jar" ]
