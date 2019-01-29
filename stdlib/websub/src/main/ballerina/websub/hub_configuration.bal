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

import ballerina/config;
import ballerina/http;
import ballerina/log;

const string BASE_PATH = "/websub";
const string HUB_PATH = "/hub";

const string DEFAULT_HOST = "0.0.0.0";
const int DEFAULT_LEASE_SECONDS_VALUE = 86400; //one day
const string DEFAULT_SIGNATURE_METHOD = "SHA256";

//TODO: Fix persistence configs, H2?
const string DEFAULT_DB_DIRECTORY = "/tmp/websubdb";
const string DEFAULT_DB_NAME = "HUB_DB";
const string DEFAULT_DB_USERNAME = "sa";
const string DEFAULT_DB_PASSWORD = "";

int hubLeaseSeconds = DEFAULT_LEASE_SECONDS_VALUE;
string hubSignatureMethod = DEFAULT_SIGNATURE_METHOD;
RemotePublishConfig remotePublishConfig = {};
boolean hubTopicRegistrationRequired = false;
string hubPublicUrl = "";
http:ClientEndpointConfig? hubClientConfig = ();

HubPersistenceStore? hubPersistenceStoreImpl = ();
boolean hubPersistenceEnabled = false;

# Function to attach and start the Ballerina WebSub Hub service.
#
# + hubServiceListener - The `http:Listener` to which the service is attached
function startHubService(http:Listener hubServiceListener) {
    // TODO : handle errors
    _ = hubServiceListener.__attach(hubService, {});
    _ = hubServiceListener.__start();
}

# Function to retrieve if persistence is enabled for the Hub.
#
# + return - True if persistence is enabled, false if not
function isHubPersistenceEnabled() returns boolean {
    return hubPersistenceEnabled;
}

# Function to retrieve if topics need to be registered at the Hub prior to publishing/subscribing.
#
# + return - True if persistence is enabled, false if not
function isHubTopicRegistrationRequired() returns boolean {
    return hubTopicRegistrationRequired;
}


function getSignatureMethod(SignatureMethod? signatureMethod) returns string {
    string signaturemethodAsConfig = config:getAsString("b7a.websub.hub.signaturemethod");
    if (signaturemethodAsConfig == "") {
        match signatureMethod {
            "SHA256" => return "SHA256";
            "SHA1" => return "SHA1";
        }
    } else {
        if (signaturemethodAsConfig.equalsIgnoreCase(SHA1)) {
            return signaturemethodAsConfig;
        }
        if (!signaturemethodAsConfig.equalsIgnoreCase(SHA256)) {
            log:printWarn("unknown signature method : [" + signaturemethodAsConfig + "], defaulting to SHA256");
        }
    }
    return DEFAULT_SIGNATURE_METHOD;
}

function getRemotePublishConfig(RemotePublishConfig? remotePublish) returns RemotePublishConfig {
    RemotePublishMode hubRemotePublishMode = PUBLISH_MODE_DIRECT;
    boolean remotePublishingEnabled = config:getAsBoolean("b7a.websub.hub.remotepublish",
                                     default = remotePublish.enabled ?: false);

    string remotePublishModeAsConfig =  config:getAsString("b7a.websub.hub.remotepublish.mode");
    if (remotePublishModeAsConfig == "") {
        hubRemotePublishMode = remotePublish.mode ?: PUBLISH_MODE_DIRECT;
    } else {
        if (remotePublishModeAsConfig.equalsIgnoreCase(REMOTE_PUBLISHING_MODE_FETCH)) {
            hubRemotePublishMode = PUBLISH_MODE_FETCH;
        } else if (!remotePublishModeAsConfig.equalsIgnoreCase(REMOTE_PUBLISHING_MODE_DIRECT)) {
            log:printWarn("unknown publish mode: [" + remotePublishModeAsConfig + "], defaulting to direct mode");
        }
    }
    return { enabled : remotePublishingEnabled, mode : hubRemotePublishMode };
}
