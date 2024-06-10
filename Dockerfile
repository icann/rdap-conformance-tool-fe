FROM alpine:latest
RUN echo 'http://dl-cdn.alpinelinux.org/alpine/edge/community' >> /etc/apk/repositories
RUN apk update && apk add openjdk21-jre maven git

LABEL maintainer="adam@cobenian.com"

RUN git clone https://github.com/icann/rdap-conformance-tool
RUN cd rdap-conformance-tool && mvn package -DskipTests && mvn install:install-file -Dfile=./tool/target/rdapct-1.0.4.jar -DgroupId=org.icann -DartifactId=rdap-conformance -Dversion=1.0.4 -Dpackaging=jar

RUN mkdir /app && \
    git clone https://github.com/Cobenian/rdap-conformance-tool-fe && \
    cd rdap-conformance-tool-fe && \
    mvn package && \
    cp target/rdapctfe-1.0.jar /app/app.jar && \
    cp rdapct-config.json /app/rdapct-config.json

WORKDIR /app
RUN chown nobody /app

ENV RDAPCT /app

USER nobody
ENTRYPOINT ["java","-jar","app.jar"]
