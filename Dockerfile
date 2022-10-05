FROM amazoncorretto:17-alpine
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

COPY build/libs/app.jar .
CMD ["java", "-jar", "app.jar"]
