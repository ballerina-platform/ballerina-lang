import ballerina/log;

function testError (string msg) {
    log:printError(msg);
}

function testErrorLambda (string msg) {
    log:printError(function() returns (string){ return msg; });
}

function testWarn (string msg) {
    log:printWarn(msg);
}

function testWarnLambda (string msg) {
    log:printWarn(function() returns (string){ return msg; });
}

function testInfo (string msg) {
    log:printInfo(msg);
}

function testInfoLambda (string msg) {
    log:printInfo(function() returns (string){ return msg; });
}

function testDebug (string msg) {
    log:printDebug(msg);
}

function testDebugLambda (string msg) {
    log:printDebug(function() returns (string){ return msg; });
}

function testTrace (string msg) {
    log:printTrace(msg);
}

function testTraceLambda (string msg) {
    log:printTrace(function() returns (string){ return msg; });
}
