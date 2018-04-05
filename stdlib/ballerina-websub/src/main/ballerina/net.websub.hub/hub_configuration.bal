package ballerina.net.websub.hub;

import ballerina/config;
import ballerina/http;

@final  string BASE_PATH = "/websub";
@final  string HUB_PATH = "/hub";

@final  string DEFAULT_HOST = "localhost";

@final  int DEFAULT_PORT = 9999;
@final  int DEFAULT_LEASE_SECONDS_VALUE = 86400000; //one day
@final  string DEFAULT_SIGNATURE_METHOD = "SHA256";

@final  int DEFAULT_DB_PORT = 3306;
@final  string DEFAULT_DB_NAME = "subscriptiondb";
@final  string DEFAULT_DB_USERNAME = "wso2";
@final  string DEFAULT_DB_PASSWORD = "wso2";

@final  string hubHost = getStringConfig("hub.host", DEFAULT_HOST);
@final  int hubPort = getIntConfig("hub.port", DEFAULT_PORT);
@final  int hubLeaseSeconds = getIntConfig("hub.lease_seconds", DEFAULT_LEASE_SECONDS_VALUE);
@final  string hubSignatureMethod = getStringConfig("hub.signature_method", DEFAULT_SIGNATURE_METHOD);

@final  boolean hubPersistenceEnabled = isHubPersistenceEnabled("hub.persist");
@final  string hubDatabaseHost = getStringConfig("hub.db.host", DEFAULT_HOST);
@final  int hubDatabasePort = getIntConfig("hub.db.port", DEFAULT_DB_PORT);
@final  string hubDatabaseName = getStringConfig("hub.db.name", DEFAULT_DB_NAME);
@final  string hubDatabaseUsername = getStringConfig("hub.db.username", DEFAULT_DB_USERNAME);
@final  string hubDatabasePassword = getStringConfig("hub.db.password", DEFAULT_DB_PASSWORD);

@final  boolean hubSslEnabled = isHubSslEnabled();
http:ServiceSecureSocket|null serviceSecureSocket = getServiceSecureSocketConfig();
http:SecureSocket|null secureSocket = getSecureSocketConfig();

@Description {value:"Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to
                    send subscription/unsubscription requests."}
@Return {value:"The WebSub Hub's URL"}
function getHubUrl () returns (string) {
    match (serviceSecureSocket) {
        http:ServiceSecureSocket => { return "https://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
        null => { return "http://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH; }
    }
}

@Description {value:"Function to retrieve if hub persistence is enabled, from a config file, or set to false by default
                    if not specified."}
@Return {value:"Whether persistence of hub subscriptions is expected"}
function isHubPersistenceEnabled (string property) returns (boolean) {
    boolean configuration;
    match (config:getAsString(property)) {
        string stringConfigFromFile => {
            configuration = <boolean>stringConfigFromFile;
        }
        any | null => { configuration = false; }
    }
    return configuration;
}

@Description {value:"Function to retrieve a configuration set as a string, from a config file, or set a default value
                    if not specified."}
@Return {value:"The string configuration"}
function getStringConfig (string property, string defaultIfNotSet) returns (string) {
    string configuration;
    match (config:getAsString(property)) {
        string stringConfigFromFile => { configuration = stringConfigFromFile == "" ? defaultIfNotSet
                                                         : stringConfigFromFile; }
        int | null => configuration = defaultIfNotSet;
    }
    return configuration;
}

@Description{value:"Function to retrieve a configuration set as an integer, from a config file, or set a default value
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
        int | null => configuration = defaultIfNotSet;
    }
    return configuration;
}

function isHubSslEnabled() returns (boolean) {
    match (config:getAsString("hub.ssl.enabled")) {
        string stringConfigFromFile => { return <boolean>stringConfigFromFile; }
        null => { return true; } //enabled by default
    }
}

function getServiceSecureSocketConfig() returns (http:ServiceSecureSocket|null) {
    if (!hubSslEnabled) {
        return null;
    }

    string keyStoreFilePath;
    string keyStorePassword;

    match (config:getAsString("hub.ssl.keyStore.filePath")) {
        string stringConfigFromFile => { keyStoreFilePath = stringConfigFromFile; }
        null => { keyStoreFilePath = "${ballerina.home}/bre/security/ballerinaKeystore.p12"; }
    }
    match (config:getAsString("hub.ssl.keyStore.password")) {
        string stringConfigFromFile => { keyStorePassword = stringConfigFromFile; }
        null => { keyStorePassword = "ballerina"; }
    }

    http:ServiceSecureSocket serviceSecureSocket =
                                    { keyStore: { filePath: keyStoreFilePath, password: keyStorePassword } };
    return serviceSecureSocket;
}

function getSecureSocketConfig() returns (http:SecureSocket|null) {
    if (!hubSslEnabled) {
       return null;
    }

    string trustStoreFilePath;
    string trustStorePassword;

    match (config:getAsString("hub.ssl.trustStore.filePath")) {
        string stringConfigFromFile => { trustStoreFilePath = stringConfigFromFile; }
        null => { trustStoreFilePath = "${ballerina.home}/bre/security/ballerinaTruststore.p12"; }
    }
    match (config:getAsString("hub.ssl.trustStore.password")) {
        string stringConfigFromFile => { trustStorePassword = stringConfigFromFile; }
        null => { trustStorePassword = "ballerina"; }
    }

    http:SecureSocket secureSocket = { trustStore: { filePath: trustStoreFilePath, password: trustStorePassword},
                                         hostNameVerification: false};
    return secureSocket;
}
