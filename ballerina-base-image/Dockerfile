FROM openjdk:jre-alpine
MAINTAINER dev@wso2.org

ARG BAL_DIST

# Insall Bash
RUN mkdir -p /ballerina && apk add --update bash && rm -rf /var/cache/apk/*

# Add Ballerina
COPY ${BAL_DIST}.zip /ballerina/
COPY scripts/cmd.sh /usr/bin/cmd.sh


RUN chmod +x /usr/bin/cmd.sh && \
    unzip /ballerina/${BAL_DIST}.zip -d /ballerina/ && \
    rm -rf /ballerina/${BAL_DIST}.zip > /dev/null 2>&1 && \
    mv /ballerina/${BAL_DIST} /ballerina/dist && \
    ln -s /ballerina/dist/bin/ballerina /usr/bin/ballerina && \
    ln -s /ballerina/dist/bin/ballerinaserver /usr/bin/ballerinaserver && \
    mkdir -p /ballerina/dist/logs

ENV BALLERINA_HOME=/ballerina/dist
WORKDIR /ballerina/dist
EXPOSE 9090

# Ballerina Runner
ENTRYPOINT ["/usr/bin/cmd.sh"]
# TODO: Reduce image size (~50MB from copying dist over network)
