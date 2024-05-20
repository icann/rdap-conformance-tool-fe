FROM alpine:latest
RUN echo 'http://dl-cdn.alpinelinux.org/alpine/edge/community' >> /etc/apk/repositories
RUN apk update && apk add openjdk21-jre

LABEL maintainer="adam@cobenian.com"

EXPOSE 8080

WORKDIR /app
RUN chown nobody /app

ARG JAR_FILE=target/tool-1.0-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ADD rdapct-config.json rdapct-config.json

ENV RDPT /app

USER nobody
ENTRYPOINT ["java","-jar","app.jar"]