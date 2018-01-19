package transactions.coordinator;

import ballerina.config;

const string basePath = "/txnmgr";
const string registrationPath = "/register";

const string coordinatorHost = getCoordinatorHost();
const int coordinatorPort = getCoordinatorPort();

function getCoordinatorHost () returns (string host) {
    host = config:getGlobalValue("coordinator.host");
    if (host == null || host == "") {
        host = "localhost";
    }
    return;
}

function getCoordinatorPort () returns (int port) {
    var p, e = <int>config:getGlobalValue("coordinator.port");
    if (e != null) {
        port = 8080;
    } else {
        port = p;
    }
    return;
}
