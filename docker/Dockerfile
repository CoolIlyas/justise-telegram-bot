FROM alpine:latest as base

RUN apk add git openjdk17

RUN git clone https://docker:<ACCESS_TOKEN>@gitlab.croc.ru/frp/justice/telegrambot.git

WORKDIR /telegrambot
RUN chmod +x ./mvnw && ./mvnw clean install

FROM alpine:latest as result
RUN apk add openjdk17
WORKDIR /app
COPY --from=base telegrambot/target/*.jar ./bot.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "bot.jar"]
CMD ["-Dspring.config.location=/app/config/"]


