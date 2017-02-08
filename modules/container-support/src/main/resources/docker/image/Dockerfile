FROM ballerina:latest
MAINTAINER dev@wso2.org

ARG SVC_MODE=false
ARG FILE_MODE=false

COPY files/* /ballerina/files/
ENV SVC_MODE=${SVC_MODE}
ENV FILE_MODE=${FILE_MODE}