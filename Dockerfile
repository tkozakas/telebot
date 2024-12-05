FROM gradle:8-jdk21-alpine AS builder
WORKDIR /app
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY src/ ./src/
RUN gradle build --no-daemon --debug
RUN java -Djarmode=layertools -jar build/libs/telebot*.jar extract

FROM gradle:8-jdk21-alpine
WORKDIR /app
RUN apk update && apk add --no-cache ffmpeg
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
RUN addgroup telebot && adduser --ingroup telebot --disabled-password telebot && chown -R telebot:telebot /app
USER telebot

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
