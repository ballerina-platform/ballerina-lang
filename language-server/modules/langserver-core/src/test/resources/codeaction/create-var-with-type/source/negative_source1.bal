import ballerina/module1;

module1:Client 'client = new (url="http://ballerina.io");

function testRemoteMethodCallWithAnyInferType() {
    'client->delete();
}
