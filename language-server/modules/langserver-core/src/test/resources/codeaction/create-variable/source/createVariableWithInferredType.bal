import ballerina/module1;

module1:Client 'client = new (url="http://ballerina.io");

function testImportedSimpleType() returns error? {
    'client->pull(path = "/path");
}

function testImportedStreamType() returns error? {
    'client->/responses.delete()
}

function testSimpleType() returns error? {
    'client->clear(path = "/path");
}

function testStreamType() returns error? {
    'client->/responses
}
