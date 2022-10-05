FROM amazoncorretto:17-alpine as corretto-jdk
LABEL org.opencontainers.image.source=https://github.com/navikt/k9-personopplysninger

RUN apk add --no-cache binutils

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# main app image
FROM alpine:latest
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=corretto-jdk /customjre $JAVA_HOME

RUN adduser --no-create-home -u 1000 -D someone
RUN mkdir /app && chown -R someone /app
USER 1000

COPY --chown=1000:1000 build/libs/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT [ "/jre/bin/java", "-jar", "/app/app.jar" ]
