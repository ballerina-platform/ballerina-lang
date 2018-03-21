import ballerina/utils.logger;

function testError (string msg) {
    logger:error(msg);
}

function testWarn (string msg) {
    logger:warn(msg);
}

function testInfo (string msg) {
    logger:info(msg);
}

function testDebug (string msg) {
    logger:debug(msg);
}

function testTrace (string msg) {
    logger:trace(msg);
}
