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
MAINTAINER dev@wso2.org

ARG BAL_DIST

# Insall Bash
RUN mkdir -p /ballerina && apk add --update bash && rm -rf /var/cache/apk/*

# Add Ballerina
COPY ${BAL_DIST} /ballerina/
COPY scripts/cmd.sh /usr/bin/cmd.sh


RUN chmod +x /usr/bin/cmd.sh && \
    unzip /ballerina/${BAL_DIST} -d /ballerina/ && \
    rm -rf /ballerina/${BAL_DIST} > /dev/null 2>&1 && \
    mv /ballerina/ballerina* /ballerina/dist && \
    ln -s /ballerina/dist/bin/ballerina /usr/bin/ballerina && \
    ln -s /ballerina/dist/bin/ballerinaserver /usr/bin/ballerinaserver && \
    mkdir -p /ballerina/dist/logs

ENV BALLERINA_HOME=/ballerina/dist
WORKDIR /ballerina/dist
EXPOSE 9090

# Ballerina Runner
ENTRYPOINT ["/usr/bin/cmd.sh"]
# TODO: Reduce image size (~50MB from copying dist over network)
