import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Client clientEP2 {
    url: "http://localhost:9097",
    cache: { enabled: false }
};

@http:ServiceConfig {
    basePath: "/test1"
}
service<http:Service> backEndService bind { port: 9097 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/greeting"
    }
    replyText(endpoint client, http:Request req) {
        _ = client->respond("Hello");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/byteChannel"
    }
    sendByteChannel(endpoint client, http:Request req) {
        io:ReadableByteChannel byteChannel = check req.getByteChannel();
        _ = client->respond(untaint byteChannel);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/directPayload"
    }
    postReply(endpoint client, http:Request req) {
        if (req.hasHeader("content-type")) {
            mime:MediaType mediaType = check mime:getMediaType(req.getContentType());
            string baseType = mediaType.getBaseType();
            if (mime:TEXT_PLAIN == baseType) {
                string textValue = check req.getTextPayload();
                _ = client->respond(untaint textValue);
            } else if (mime:APPLICATION_XML == baseType) {
                xml xmlValue = check req.getXmlPayload();
                _ = client->respond(untaint xmlValue);
            } else if (mime:APPLICATION_JSON == baseType) {
                json jsonValue = check req.getJsonPayload();
                _ = client->respond(untaint jsonValue);
            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                byte[] blobValue = check req.getBinaryPayload();
                _ = client->respond(untaint blobValue);
            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                mime:Entity[] bodyParts = check req.getBodyParts();
                _ = client->respond(untaint bodyParts);
            }
        } else {
            _ = client->respond(());
        }
    }
}

@http:ServiceConfig {
    basePath: "/test2"
}
service<http:Service> testService bind { port: 9098 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientGet"
    }
    testGet(endpoint client, http:Request req) {
        string value;
        //No Payload
        http:Response response = check clientEP2->get("/test1/greeting");
        value = check response.getTextPayload();
        //No Payload
        response = check clientEP2->get("/test1/greeting", message = ());
        value = value + check response.getTextPayload();
        http:Request httpReq = new;
        //Request as message
        response = check clientEP2->get("/test1/greeting", message = httpReq);
        value = value + check response.getTextPayload();
        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithoutBody"
    }
    testPost(endpoint client, http:Request req) {
        string value;
        //No Payload
        var clientResponse = clientEP2->post("/test1/directPayload", ());
        match clientResponse {
            error err => {
                value = err.message;
            }
            http:Response res => {
                match res.getTextPayload() {
                    string returnValue => {
                        value = returnValue;
                    }
                    error payloadErr => {
                        value = payloadErr.message;
                    }
                }
            }
        }

        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithBody"
    }
    testPostWithBody(endpoint client, http:Request req) {
        string value;
        http:Response textResponse = check clientEP2->post("/test1/directPayload", "Sample Text");
        value = check textResponse.getTextPayload();

        http:Response xmlResponse = check clientEP2->post("/test1/directPayload", xml `<yy>Sample Xml</yy>`);
        xml xmlValue = check xmlResponse.getXmlPayload();
        value = value + xmlValue.getTextValue();

        http:Response jsonResponse = check clientEP2->post("/test1/directPayload", { name: "apple", color: "red" });
        json jsonValue = check jsonResponse.getJsonPayload();
        value = value + jsonValue.toString();

        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleBinary"
    }
    testPostWithBinaryData(endpoint client, http:Request req) {
        string value;
        string textVal = "Sample Text";
        byte[] binaryValue = textVal.toByteArray("UTF-8");
        http:Response textResponse = check clientEP2->post("/test1/directPayload", binaryValue);
        value = check textResponse.getPayloadAsString();

        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/handleByteChannel"
    }
    testPostWithByteChannel(endpoint client, http:Request req) {
        string value;
        io:ReadableByteChannel byteChannel = check req.getByteChannel();
        http:Response res = check clientEP2->post("/test1/byteChannel", untaint byteChannel);
        value = check res.getPayloadAsString();

        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleMultiparts"
    }
    testPostWithBodyParts(endpoint client, http:Request req) {
        string value;
        mime:Entity part1 = new;
        part1.setJson({ "name": "wso2" });
        mime:Entity part2 = new;
        part2.setText("Hello");
        mime:Entity[] bodyParts = [part1, part2];

        http:Response res = check clientEP2->post("/test1/directPayload", bodyParts);
        mime:Entity[] returnParts = check res.getBodyParts();

        foreach bodyPart in returnParts {
            mime:MediaType mediaType = check mime:getMediaType(bodyPart.getContentType());
            string baseType = mediaType.getBaseType();
            if (mime:APPLICATION_JSON == baseType) {
                json payload = check bodyPart.getJson();
                value = payload.toString();
            }
            if (mime:TEXT_PLAIN == baseType) {
                string textVal = check bodyPart.getText();
                value = value + textVal;
            }
        }
        _ = client->respond(untaint value);
    }
}
