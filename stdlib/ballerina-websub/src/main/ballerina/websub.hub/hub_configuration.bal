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
package ballerina.websub.hub;

import ballerina/config;
import ballerina/http;
import ballerina/log;
import ballerina/websub;

@final string BASE_PATH = "/websub";
@final string HUB_PATH = "/hub";

@final string DEFAULT_HOST = "localhost";

@final int DEFAULT_PORT = 9292;
@final int DEFAULT_LEASE_SECONDS_VALUE = 86400000; //one day
@final string DEFAULT_SIGNATURE_METHOD = "SHA256";

@final int DEFAULT_DB_PORT = 3306;
@final string DEFAULT_DB_NAME = "subscriptiondb";
@final string DEFAULT_DB_USERNAME = "wso2";
@final string DEFAULT_DB_PASSWORD = "wso2";

@final string hubHost = getStringConfig("hub.host", DEFAULT_HOST);
@final int hubPort = getIntConfig("hub.port", DEFAULT_PORT);
@final int hubLeaseSeconds = getIntConfig("hub.lease_seconds", DEFAULT_LEASE_SECONDS_VALUE);
@final string hubSignatureMethod = getStringConfig("hub.signature_method", DEFAULT_SIGNATURE_METHOD);
@final boolean hubRemotePublishingEnabled = getBooleanConfig("hub.remote_publishing.enabled", false);
@final string hubRemotePublishingMode = getStringConfig("hub.remote_publishing.mode",
                                                                                websub:REMOTE_PUBLISHING_MODE_DIRECT);
@final boolean hubTopicRegistrationRequired = getBooleanConfig("hub.topic_registration.required", true);
@final string hubPublicUrl = getStringConfig("hub.url", getHubUrl());

@final boolean hubPersistenceEnabled = getBooleanConfig("hub.persistence.enabled", false);
//TODO: include db type --> "hub.db.type"
@final string hubDatabaseHost = getStringConfig("hub.db.host", DEFAULT_HOST);
@final int hubDatabasePort = getIntConfig("hub.db.port", DEFAULT_DB_PORT);
@final string hubDatabaseName = getStringConfig("hub.db.name", DEFAULT_DB_NAME);
@final string hubDatabaseUsername = getStringConfig("hub.db.username", DEFAULT_DB_USERNAME);
@final string hubDatabasePassword = getStringConfig("hub.db.password", DEFAULT_DB_PASSWORD);

@final boolean hubSslEnabled = isHubSslEnabled();
http:ServiceSecureSocket? serviceSecureSocket = getServiceSecureSocketConfig();
http:SecureSocket? secureSocket = getSecureSocketConfig();

@Description {value:"Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to
                    send subscription/unsubscription requests."}
@Return {value:"The WebSub Hub's URL"}
function getHubUrl () returns (string) {
    match (serviceSecureSocket) {
        http:ServiceSecureSocket => { return "https://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
        () => { return "http://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
    }
}

@Description {value:"Function to retrieve if persistence is enabled for the Hub."}
@Return {value:"True if persistence is enabled, false if not"}
function isHubPersistenceEnabled () returns (boolean) {
    return hubPersistenceEnabled;
}

@Description {value:"Function to retrieve if topics need to be registered at the Hub prior to publishing/subscribing."}
@Return {value:"True if persistence is enabled, false if not"}
function isHubTopicRegistrationRequired () returns (boolean) {
    return hubTopicRegistrationRequired;
}

@Description {value:"Function to retrieve a configuration set as a boolean, via the config API, or set a default value
                    if not specified."}
@Return {value:"The boolean configuration"}
function getBooleanConfig (string property, boolean defaultIfNotSet) returns (boolean) {
    boolean configuration;
    match (config:getAsString(property)) {
        string stringConfigFromFile => { configuration = <boolean>stringConfigFromFile; }
        () => { configuration = defaultIfNotSet; }
    }
    return configuration;
}

@Description {value:"Function to retrieve a configuration set as a string, via the config API, or set a default value
                    if not specified."}
@Return {value:"The string configuration"}
function getStringConfig (string property, string defaultIfNotSet) returns (string) {
    string configuration;
    match (config:getAsString(property)) {
        string stringConfigFromFile => { configuration = stringConfigFromFile == "" ? defaultIfNotSet
                                                         : stringConfigFromFile; }
        () => configuration = defaultIfNotSet;
    }
    return configuration;
}

@Description{value:"Function to retrieve a configuration set as an integer, via the config API, or set a default value
                    if not specified."}
@Return{value:"The integer configuration"}
function getIntConfig (string property, int defaultIfNotSet) returns (int) {
    int configuration;
    match (config:getAsString(property)) {
        string stringConfigFromFile => {
            match (<int>stringConfigFromFile) {
                int portConfigFromFile => { configuration = portConfigFromFile; }
                error => { configuration = defaultIfNotSet; }
            }
        }
        () => configuration = defaultIfNotSet;
    }
    return configuration;
}

function isHubSslEnabled() returns (boolean) {
    match (config:getAsString("hub.ssl.enabled")) {
        string stringConfigFromFile => { return <boolean>stringConfigFromFile; }
        () => { return true; } //enabled by default
    }
}

function getServiceSecureSocketConfig() returns (http:ServiceSecureSocket | ()) {
    if (!hubSslEnabled) {
        return;
    }

    string keyStoreFilePath;
    string keyStorePassword;

    match (config:getAsString("hub.ssl.key_store.file_path")) {
        string stringConfigFromFile => { keyStoreFilePath = stringConfigFromFile; }
        () => { keyStoreFilePath = "${ballerina.home}/bre/security/ballerinaKeystore.p12"; }
    }
    match (config:getAsString("hub.ssl.key_store.password")) {
        string stringConfigFromFile => { keyStorePassword = stringConfigFromFile; }
        () => { keyStorePassword = "ballerina"; }
    }

    http:ServiceSecureSocket serviceSecureSocket =
                                    { keyStore: { filePath: keyStoreFilePath, password: keyStorePassword } };
    return serviceSecureSocket;
}

function getSecureSocketConfig() returns (http:SecureSocket | ()) {
    string trustStoreFilePath;
    string trustStorePassword;

    if (!hubSslEnabled) {
        match (config:getAsString("hub.ssl.trust_store.file_path")) {
            string stringConfigFromFile => { trustStoreFilePath = stringConfigFromFile; }
            () => { return; }
        }
        match (config:getAsString("hub.ssl.trust_store.password")) {
            string stringConfigFromFile => {
                trustStorePassword = stringConfigFromFile;
                http:SecureSocket secureSocket = {
                    trustStore: { filePath: trustStoreFilePath, password: trustStorePassword},
                    verifyHostname: false
                };
                return secureSocket;
            }
            () => {
                log:printWarn("Ignoring trust store file since password is not specified.");
                return;
            }
        }
    }

    match (config:getAsString("hub.ssl.trust_store.file_path")) {
        string stringConfigFromFile => { trustStoreFilePath = stringConfigFromFile; }
        () => { trustStoreFilePath = "${ballerina.home}/bre/security/ballerinaTruststore.p12"; }
    }
    match (config:getAsString("hub.ssl.trust_store.password")) {
        string stringConfigFromFile => { trustStorePassword = stringConfigFromFile; }
        () => { trustStorePassword = "ballerina"; }
    }

    http:SecureSocket secureSocket = { trustStore: { filePath: trustStoreFilePath, password: trustStorePassword},
                                        verifyHostname: false};
    return secureSocket;
}
