import ballerina.runtime;

function testSleepCurrentThread () {
    runtime:sleepCurrentWorker(1000);
}

function testSetProperty (string name, string value) {
    runtime:setProperty(name, value);
}

function testGetProperty (string name) (string) {
    return runtime:getProperty(name);
}

function testGetProperties () (map) {
    return runtime:getProperties();
}

function testGetCurrentDirectory () (string) {
    return runtime:getCurrentDirectory();
}

function testGetFileEncoding () (string) {
    return runtime:getFileEncoding();
}
