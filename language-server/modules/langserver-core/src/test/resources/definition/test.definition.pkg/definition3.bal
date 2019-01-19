import ballerina/http;
import ballerina/crypto;

function testSignatureHelp () {
    endpoint http:Listener listener {
        port: 9090
    };

    http:Response res = new;
    json connectionJson = { protocol: "json" };
    res.statusCode = 200;
    res.setJsonPayload(<json>crypto:unsafeMarkUntainted(connectionJson));
    listener->respond(res);
}
