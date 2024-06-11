FROM alpine:latest
RUN echo 'http://dl-cdn.alpinelinux.org/alpine/edge/community' >> /etc/apk/repositories
RUN apk update && apk add openjdk21-jre maven git

LABEL maintainer="adam@cobenian.com"

RUN git clone https://github.com/icann/rdap-conformance-tool && \
    cd rdap-conformance-tool && \
    mvn package -DskipTests && \
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout > version.txt && \
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) && \
    mvn install:install-file -Dfile=./tool/target/rdapct-$VERSION.jar -DgroupId=org.icann -DartifactId=rdap-conformance -Dversion=$VERSION -Dpackaging=jar

RUN mkdir /app && \
    git clone -b dev https://github.com/Cobenian/rdap-conformance-tool-fe && \
    cd rdap-conformance-tool-fe && \
    ls -la && \ 
    pwd && \
    chmod +x scripts/fix_versions.sh && \
    ./scripts/fix_versions.sh ../rdap-conformance-tool/version.txt && \
    mvn package && \
    VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) && \
    cp target/rdapctfe-$VERSION.jar /app/app.jar && \
    cp rdapct-config.json /app/rdapct-config.json

WORKDIR /app
RUN chown nobody /app

ENV RDAPCT /app

USER nobody
ENTRYPOINT ["java","-jar","app.jar"]
