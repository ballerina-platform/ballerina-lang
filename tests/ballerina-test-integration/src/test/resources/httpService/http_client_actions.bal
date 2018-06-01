import ballerina/io;
import ballerina/http;
import ballerina/mime;

endpoint http:Listener testEP1 {
    port: 9090
};

endpoint http:Listener testEP2 {
    port: 9091
};

endpoint http:Client clientEP {
    url: "http://localhost:9091",
    cache: { enabled: false }
};

@http:ServiceConfig {
    basePath: "/test1"
}
service<http:Service> backEndService bind testEP2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/greeting"
    }
    replyText(endpoint client, http:Request req) {
        _ = client->respond("Hello");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/123"
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
service<http:Service> testService bind testEP1 {

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
        path: "/clientPost"
    }
    testPost(endpoint client, http:Request req) {
        string value;
        //No Payload
        var clientResponse = clientEP->post("/test1/123", ());
        match clientResponse {
            error err => {value = err.message;}
            http:Response res => {
                match res.getTextPayload() {
                    string returnValue => {
                        value = returnValue;
                    }
                    error payloadErr => { value = payloadErr.message;}
                }
            }
        }

        _ = client->respond(value);
    }
}
