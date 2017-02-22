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
MAINTAINER tryballerina@gmail.com

ARG BALLERINA_DIST

# Add Ballerina
COPY ${BALLERINA_DIST} /root/
COPY scripts/cmd.sh /usr/bin/cmd.sh

# Create folders, unzip distribution, create users, & set permissions.
RUN mkdir -p /ballerina && \
    apk add --update bash && \
    rm -rf /var/cache/apk/* && \
    chmod +x /usr/bin/cmd.sh && \
    unzip /root/${BALLERINA_DIST} -d /ballerina/ && \
    rm -rf /root/${BALLERINA_DIST} > /dev/null 2>&1 && \
    mv /ballerina/ballerina* /ballerina/dist && \
    ln -s /ballerina/dist/bin/ballerina /usr/bin/ballerina && \
    ln -s /ballerina/dist/bin/ballerinaserver /usr/bin/ballerinaserver && \
    mkdir -p /ballerina/dist/logs && \
    addgroup ballerina && \
    adduser -S -s /bin/bash -g 'buser' -G ballerina -D buser && \
    chown -R buser:ballerina /ballerina && \
    chown buser:ballerina /usr/bin/cmd.sh

ENV BALLERINA_HOME=/ballerina/dist
WORKDIR /ballerina/dist
USER buser

EXPOSE 9090

ENTRYPOINT ["/usr/bin/cmd.sh"]
