import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Client clientEP {
    url: "http://localhost:9091",
    cache: { enabled: false }
};

@http:ServiceConfig {
    basePath: "/test1"
}
service<http:Service> backEndService bind { port: 9091 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/greeting"
    }
    replyText(endpoint client, http:Request req) {
        _ = client->respond("Hello");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/directPayload"
    }
    postReply(endpoint client, http:Request req) {
        if (req.hasHeader("content-type")) {
            string contentType = req.getContentType();
            if (mime:TEXT_PLAIN == contentType) {
                string textValue = check req.getTextPayload();
                _ = client->respond(textValue);
            } else if (mime:APPLICATION_XML == contentType) {
                xml xmlValue = check req.getXmlPayload();
                _ = client->respond(xmlValue);
            } else if (mime:APPLICATION_JSON == contentType) {
                json jsonValue = check req.getJsonPayload();
                _ = client->respond(jsonValue);
            } else if ("text/customSubType1" == contentType) {
                blob blobValue = check req.getBinaryPayload();
                _ = client->respond(blobValue);
            } else if (mime:APPLICATION_OCTET_STREAM == contentType) {
                io:ByteChannel byteChannel = check req.getByteChannel();
                _ = client->respond(byteChannel);
            } else if (mime:MULTIPART_FORM_DATA == contentType) {
                mime:Entity[] bodyParts = check req.getBodyParts();
                _ = client->respond(bodyParts);
            }
        } else {
            _ = client->respond(());
        }
    }
}

@http:ServiceConfig {
    basePath: "/test2"
}
service<http:Service> testService bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientGet"
    }
    testGet(endpoint client, http:Request req) {
        string value;
        //No Payload
        http:Response response = check clientEP->get("/test1/greeting");
        value = check response.getTextPayload();
        //No Payload
        response = check clientEP->get("/test1/greeting", message = ());
        value = value + check response.getTextPayload();
        http:Request req = new;
        //Request as message
        response = check clientEP->get("/test1/greeting", message = req);
        value = value + check response.getTextPayload();
        _ = client->respond(value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithoutBody"
    }
    testPost(endpoint client, http:Request req) {
        string value;
        //No Payload
        var clientResponse = clientEP->post("/test1/directPayload", ());
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

        _ = client->respond(value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithBody"
    }
    testPostWithBody(endpoint client, http:Request req) {
        string value;
        //string payload
        http:Response textResponse = check clientEP->post("/test1/directPayload", "Sample Text");
        value = check textResponse.getTextPayload();

        http:Response xmlResponse = check clientEP->post("/test1/directPayload", xml `<yy>Sample Xml</yy>`);
        xml xmlValue = check xmlResponse.getXmlPayload();
        value = value + xmlValue.getTextValue();

        http:Response jsonResponse = check clientEP->post("/test1/directPayload", { name: "apple", color: "red" });
        json jsonValue = check jsonResponse.getJsonPayload();
        value = value + jsonValue.toString();

        _ = client->respond(value);
    }
}
