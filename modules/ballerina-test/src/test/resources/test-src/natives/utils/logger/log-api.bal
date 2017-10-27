import ballerina.log;

function testError (string msg) {
    log:error(msg);
}

function testWarn (string msg) {
    log:warn(msg);
}

function testInfo (string msg) {
    log:info(msg);
}

function testDebug (string msg) {
    log:debug(msg);
}

function testTrace (string msg) {
    log:trace(msg);
}
