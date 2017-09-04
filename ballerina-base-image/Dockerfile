# --------------------------------------------------------------------
# Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# -----------------------------------------------------------------------

FROM openjdk:jre-alpine
LABEL maintainer "tryballerina@gmail.com"

# Ballerina runtime distribution filename.
ARG BALLERINA_DIST
ARG BUILD_DATE
ARG VCS_REF
ARG BUILD_VERSION

# Labels.
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="ballerinalang/ballerina"
LABEL org.label-schema.description="Ballerina language runtime"
LABEL org.label-schema.url="http://ballerinalang.org/"
LABEL org.label-schema.vcs-url="https://github.com/ballerinalang/container-support"
LABEL org.label-schema.vcs-ref=$VCS_REF
LABEL org.label-schema.vendor="WSO2"
LABEL org.label-schema.version=$BUILD_VERSION
LABEL org.label-schema.docker.cmd="docker run -v ~/ballerina/packages:/ballerina/files -p 9090:9090 -d ballerinalang/ballerina"


# Add Ballerina runtime.
COPY ${BALLERINA_DIST} /root/
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

# Create folders, unzip distribution, create users, & set permissions.
RUN mkdir -p /ballerina/files \
    && addgroup troupe \
    && adduser -S -s /bin/bash -g 'ballerina' -G troupe -D ballerina \
    && apk add --update bash \
    && chmod +x /usr/local/bin/docker-entrypoint.sh \
    && chown ballerina:troupe /usr/local/bin/docker-entrypoint.sh \
    && unzip /root/${BALLERINA_DIST} -d /ballerina/ > /dev/null 2>&1 \
    && mv /ballerina/ballerina* /ballerina/runtime \
    && mkdir -p /ballerina/runtime/logs \
    && chown -R ballerina:troupe /ballerina \
    && rm -rf /root/${BALLERINA_DIST} > /dev/null 2>&1 \
    && rm -rf /var/cache/apk/*

ENV BALLERINA_HOME /ballerina/runtime
ENV PATH $BALLERINA_HOME/bin:$PATH

# Where Ballerina files and archives should be copied to.
VOLUME /ballerina/files

WORKDIR /ballerina/runtime

USER ballerina

# Ballerina Service port.
EXPOSE 9090

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

# Force child images to override the crucial labels
ONBUILD ARG BUILD_DATE
ONBUILD RUN if [ -z "$BUILD_DATE" ]; then >&2 echo "ERROR: Haven't set mandatory build argument \"BUILD_DATE\". Please set an RFC 3339 Date value."; exit 1; fi
LABEL org.label-schema.build-date=$BUILD_DATE

