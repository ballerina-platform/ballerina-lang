import ballerina/log;

function testError (string msg) {
    log:printError(msg);
}

function testWarn (string msg) {
    log:printWarn(msg);
}

function testInfo (string msg) {
    log:printInfo(msg);
}

function testDebug (string msg) {
    log:printDebug(msg);
}

function testTrace (string msg) {
    log:printTrace(msg);
}
