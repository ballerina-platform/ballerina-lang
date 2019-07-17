// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/internal;

# Represents ActiveMQ Artemis Connection.
public type Connection client object {

    # Creates an ActiveMQ Artemis Connection object.
    # 
    # + url - The connection url to the broker
    # + config - The connection configuration
    public function __init(string url, ConnectionConfiguration? config = ()) {
        ConnectionConfiguration configuration = {};
        if (config is ConnectionConfiguration) {
            configuration = config;
        }
        self.createConnection(self.getPopulatedUrl(url, configuration["secureSocket"]), configuration);
    }

    function createConnection(string url, ConnectionConfiguration config) = external;

    # Returns true if close was already called.
    # 
    # + return - `true` if closed, `false` otherwise
    public function isClosed() returns boolean = external;

    # Closes the connection and release all its resources.
    public remote function close() = external;

    private function getPopulatedUrl(string url, SecureSocket? secureSocket) returns string {
        if (secureSocket is SecureSocket) {
            return self.buildUrlFromQueryParams(url, self.populateQueryParamsFromConfigs(secureSocket));
        }
        return url;
    }

    private function populateQueryParamsFromConfigs(SecureSocket secureSocket) returns map<string> {
        map<string> queryParams = {};
        self.setQueryParamsForKeyAndTrustStores(secureSocket, queryParams);
        self.setQueryCipherParams(secureSocket, queryParams);
        queryParams["verifyHost"] = secureSocket.verifyHost.toString();
        return queryParams;
    }

    private function setQueryParamsForKeyAndTrustStores(SecureSocket secureSocket, map<string> queryParams) {
        var trustStore = secureSocket["trustStore"];
        if (trustStore is crypto:TrustStore) {
            var trustStorePath = trustStore.path.trim();
            self.checkAndSetPath(trustStorePath, queryParams, "trustStorePath");
            var trustStorePassword = trustStore.password;
            if (self.isNotBlankString(trustStorePassword)) {
                queryParams["trustStorePassword"] = trustStorePassword;
            }
        }

        var keyStore = secureSocket["keyStore"];
        if (keyStore is crypto:KeyStore) {
            var keyStorePath = keyStore.path.trim();
            self.checkAndSetPath(keyStorePath, queryParams, "keyStorePath");
            var keyStorePassword = keyStore.password;
            if (self.isNotBlankString(keyStorePassword)) {
                queryParams["keyStorePassword"] = keyStorePassword;
            }
        }
    }

    private function checkAndSetPath(string path, map<string> queryParams, string configName) {
        if (self.isNotBlankString(path)) {
            string absPath = checkpanic filepath:absolute(path);
            queryParams[configName] = internal:replaceAll(absPath, " ", "%20");
        }
    }

    private function setQueryCipherParams(SecureSocket secureSocket, map<string> queryParams) {
        string ciphers = "";
        boolean first = true;
        foreach (var cipher in secureSocket.ciphers) {
            if (first) {
                ciphers = cipher;
                first = false;
            } else {
                queryParams["enabledCipherSuites"] = "," + cipher;
            }
        }
    }

    private function isNotBlankString(string value) returns boolean {
        return value.trim() != "";
    }

    private function buildUrlFromQueryParams(string initialUrl, map<string> queryParams) returns string {
        boolean first = true;
        var populatedUrl = initialUrl;
        foreach (var [key, value] in queryParams.entries()) {
            if (first) {
                populatedUrl += "?sslEnabled=true";
                populatedUrl += self.getQueryParamPart(key, value);
                first = false;
            } else {
                populatedUrl += self.getQueryParamPart(key, value);
            }
        }
        return populatedUrl;
    }

    private function getQueryParamPart(string key, string value) returns string {
        return "&" + key + "=" + value;
    }
};

# Configurations related to a Artemis `Connection`.
#
# + secureSocket - Configurations related to SSL/TLS
# + timeToLive - Connection's time-to-live. negative to disable or greater or equals to 0
# + callTimeout - The blocking calls timeout in milliseconds
# + consumerWindowSize - Window size in bytes for flow control of the consumers created through this `Connection`
# + consumerMaxRate - Maximum rate of message consumption for consumers created through this `Connection`
# + producerWindowSize - Window size for flow control of the producers created through this `Connection`
# + producerMaxRate - The maximum rate of message production for producers created through this `Connection`
# + retryInterval - The time in milliseconds to retry connection
# + retryIntervalMultiplier - Multiplier to apply to successive retry intervals
# + maxRetryInterval - The maximum retry interval (in the case a retry interval multiplier has been specified)
# + reconnectAttempts - The maximum number of attempts to retry connection in case of failure
# + initialConnectAttempts - The maximum number of attempts to establish an initial connection
public type ConnectionConfiguration record {|
    SecureSocket secureSocket?;
    int timeToLive = 60000;
    int callTimeout = 30000;
    int consumerWindowSize = 1024 * 1024;
    int consumerMaxRate = -1;
    int producerWindowSize = 64 * 1024;
    int producerMaxRate = -1;
    int retryInterval = 2000;
    float retryIntervalMultiplier = 1;
    int maxRetryInterval = 2000;
    int reconnectAttempts = 0;
    int initialConnectAttempts = 1;
|};
