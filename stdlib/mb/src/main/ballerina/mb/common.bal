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

# Configurations related to Ballerina message broker URL
#
# + username - The caller's user name
# + password - The caller's password
# + host - Hostname of the broker node
# + port - AMQP port of the broker node
# + clientID - Identifier used to uniquely identify the client connection
# + virtualHost - target virtualhost
# + secureSocket - TLS configurations
public type BrokerURLConfig record {
    string username = "admin";
    string password = "admin";
    string host = "localhost";
    int port = 5672;
    string clientID = "ballerina";
    string virtualHost = "default";
    ServiceSecureSocket? secureSocket;
    !...
};

# Configurations related to TLS
#
# + trustStore - Trustore configurations
# + keyStore - Keystore configuration
# + sslCertAlias - name of the ssl cert alias
public type ServiceSecureSocket record {
    Store? trustStore;
    Store? keyStore;
    string sslCertAlias;
    !...
};

#
# + path - file path to key store
# + password - password used to protect the key store
public type Store record {
    string path;
    string password;
    !...
};

function generateBrokerURL(BrokerURLConfig config) returns string {
    return "amqp://" + config.username + ":" + config.password + "@" + config.clientID + "/" + config.virtualHost
        + "?brokerlist='tcp://" + config.host + ":" + config.port + "'";
}

function generateSecureBrokerURL(BrokerURLConfig config, ServiceSecureSocket secureSocket) returns string {
    var (trustStorePath, trustStorePassword) = getStoreDetails(secureSocket.trustStore);
    var (keyStorePath, keyStorePassword) = getStoreDetails(secureSocket.keyStore);

    return "amqp://" + config.username + ":" + config.password + "@" + config.clientID + "/" + config.virtualHost
        + "?brokerlist='tcp://" + config.host + ":" + config.port
        + "?ssl='true'"
        + "&ssl_cert_alias='" + secureSocket.sslCertAlias + "'"
        + "&trust_store='" + trustStorePath + "'"
        + "&trust_store_password='" + trustStorePassword + "'"
        + "&key_store='" + keyStorePath + "'"
        + "&key_store_password='" + keyStorePassword + "'"
        + "'";
}

function getConnectionUrl(BrokerURLConfig config) returns string {
    string connectionUrl;
    match (config.secureSocket) {
        ServiceSecureSocket secSocket => {
            connectionUrl = generateSecureBrokerURL(config, secSocket);
        }
        () => {
            connectionUrl = generateBrokerURL(config);
        }
    }
    return connectionUrl;
}

function getStoreDetails(Store? store) returns (string, string) {
    match(store) {
        Store t => {
            return (t.path, t.password);
        }
        () => {
            error e = {message:"Store details not provided."};
            throw e;
        }
    }

}
