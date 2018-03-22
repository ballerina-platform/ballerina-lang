package ballerina.net.websub.hub;

import ballerina/config;

const string BASE_PATH = "/websub";
const string HUB_PATH = "/hub";

const string DEFAULT_HOST = "localhost";

const int DEFAULT_PORT = 9999;
const int DEFAULT_LEASE_SECONDS_VALUE = 86400000; //one day
const string DEFAULT_SIGNATURE_METHOD = "SHA256";

const int DEFAULT_DB_PORT = 3306;
const string DEFAULT_DB_NAME = "subscriptiondb";
const string DEFAULT_DB_USERNAME = "wso2";
const string DEFAULT_DB_PASSWORD = "wso2";

const string hubHost = getStringConfig("hub.host", DEFAULT_HOST);
const int hubPort = getIntConfig("hub.port", DEFAULT_PORT);
const int hubLeaseSeconds = getIntConfig("hub.lease_seconds", DEFAULT_LEASE_SECONDS_VALUE);
const string hubSignatureMethod = getStringConfig("hub.signature_method", DEFAULT_SIGNATURE_METHOD);

const boolean hubPersistenceEnabled = isHubPersistenceEnabled("hub.persist");
const string hubDatabaseHost = getStringConfig("hub.db.host", DEFAULT_HOST);
const int hubDatabasePort = getIntConfig("hub.db.port", DEFAULT_DB_PORT);
const string hubDatabaseName = getStringConfig("hub.db.name", DEFAULT_DB_NAME);
const string hubDatabaseUsername = getStringConfig("hub.db.username", DEFAULT_DB_USERNAME);
const string hubDatabasePassword = getStringConfig("hub.db.password", DEFAULT_DB_PASSWORD);

@Description {value:"Function to retrieve the URL for the Ballerina WebSub Hub, to which potential subscribers need to
                    send subscription/unsubscription requests."}
@Return {value:"The WebSub Hub's URL"}
function getHubUrl () returns (string) {
    //TODO: HTTPS
    return "http://" + hubHost + ":" + hubPort + BASE_PATH + HUB_PATH;
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
