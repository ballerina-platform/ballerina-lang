import ballerina/http;

function testSignatureHelp () {
    endpoint http:Listener listener {
        port: 9090
    };

    http:Response res = new;
    json connectionJson = { protocol: "json" };
    res.statusCode = 200;
    res.setJsonPayload(crypto:unsafeMarkUntainted(connectionJson));
    listener->respond(res);
}
