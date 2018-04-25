// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

documentation { Configurations related to Ballerina message broker URL
    F{{username}} The caller's user name
    F{{password}} The caller's password
    F{{host}} Hostname of the broker node
    F{{port}} AMQP port of the broker node
    F{{clientID}} Identifier used to uniquely identify the client connection
    F{{virtualHost}} target virtualhost
}
public type BrokerURLConfig {
    string username = "admin",
    string password = "admin",
    string host = "localhost",
    int port = 5672,
    string clientID = "ballerina",
    string virtualHost = "default",
};

documentation { Generate the broker URL using the configurations provided
    P{{config}} URL configurations
}
function generateBrokerURL(BrokerURLConfig config) returns string {
    return "amqp://" + config.username + ":" + config.password + "@" + config.clientID + "/" + config.virtualHost
        + "?brokerlist='tcp://" + config.host + ":" + config.port + "'";
}
