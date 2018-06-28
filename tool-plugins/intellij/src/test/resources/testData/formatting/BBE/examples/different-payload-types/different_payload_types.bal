import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

//Client endpoint.
endpoint http:Client clientEP {
    url: "http://localhost:9091/backEndService"
};

//Service to test HTTP client actions with different payload types.
service<http:Service> actionService bind { port: 9090 } {

    messageUsage(endpoint caller, http:Request req) {

        //GET action without any payload.
        var response = clientEP->get("/greeting");
        handleResponse(response);

        //GET action with request as message.
        http:Request request = new;
        response = clientEP->get("/greeting", message = request);
        handleResponse(response);

        //POST action without any payload.
        response = clientEP->post("/echo", ());
        handleResponse(response);

        //POST action with text as payload.
        response = clientEP->post("/echo", "Sample Text");
        handleResponse(response);

        //POST action with xml as payload.
        response = clientEP->post("/echo", xml `<yy>Sample Xml</yy>`);
        handleResponse(response);

        //POST action with json as payload.
        response = clientEP->post("/echo", { name: "apple", color: "red" });
        handleResponse(response);

        //POST action with blob as payload.
        string textVal = "Sample Text";
        blob binaryValue = textVal.toBlob("UTF-8");
        response = clientEP->post("/echo", binaryValue);
        handleResponse(response);

        //Get a byte channel to a given file.
        io:Mode permission = "r";
        io:ByteChannel byteChannel = io:openFile("./files/logo.png",
            permission);

        //POST action with byte channel as payload.
        response = check clientEP->post("/image", byteChannel);
        handleResponse(response);

        //Create a json body part.
        mime:Entity part1 = new;
        part1.setJson({ "name": "Jane" });

        //Create a text body part.
        mime:Entity part2 = new;
        part2.setText("Hello");

        //POST action with body parts as payload.
        mime:Entity[] bodyParts = [part1, part2];
        response = check clientEP->post("/echo", bodyParts);
        handleResponse(response);

        caller->respond("Client actions successfully executed!") but {
            error err => log:printError(err.message, err = err)
        };
    }
}

//Back end service that send out different payload types as response.
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
        path: "/echo"
    }
    directResponse(endpoint client, http:Request req) {
        if (req.hasHeader("content-type")) {
            string baseType = getBaseType(req.getContentType());

            if (mime:TEXT_PLAIN == baseType) {
                string textValue = check req.getTextPayload();
                client->respond(textValue) but {
                    error err => log:printError(err.message, err = err)
                };

            } else if (mime:APPLICATION_XML == baseType) {
                xml xmlValue = check req.getXmlPayload();
                client->respond(xmlValue) but {
                    error err => log:printError(err.message, err = err)
                };

            } else if (mime:APPLICATION_JSON == baseType) {
                json jsonValue = check req.getJsonPayload();
                client->respond(jsonValue) but {
                    error err => log:printError(err.message, err = err)
                };

            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                blob blobValue = check req.getBinaryPayload();
                client->respond(blobValue) but {
                    error err => log:printError(err.message, err = err)
                };

            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                mime:Entity[] bodyParts = check req.getBodyParts();
                client->respond(bodyParts) but {
                    error err => log:printError(err.message, err = err)
                };
            }
        } else {
            client->respond(()) but {
                error err => log:printError(err.message, err = err)
            };
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/image"
    }
    sendByteChannel(endpoint client, http:Request req) {
        blob bytes = check req.getBinaryPayload();
        http:Response response = new;
        response.setBinaryPayload(bytes, contentType = mime:IMAGE_PNG);
        client->respond(response) but {
            error err => log:printError(err.message, err = err)
        };
    }
}

//Handle response data received from HTTP client actions.
function handleResponse(http:Response|error returnValue) {
    match returnValue {
        http:Response response => {
            //Print the content type of the received data.
            if (response.hasHeader("content-type")) {
                string baseType = getBaseType(response.getContentType());

                if (mime:TEXT_PLAIN == baseType) {
                    log:printInfo("Text data: " + check response.getTextPayload());

                } else if (mime:APPLICATION_XML == baseType) {
                    xml xmlData = check response.getXmlPayload();
                    log:printInfo("Xml data: " + xmlData.getTextValue());

                } else if (mime:APPLICATION_JSON == baseType) {
                    json jsonData = check response.getJsonPayload();
                    log:printInfo("Json data: " +  jsonData.toString());

                } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                    log:printInfo("Response contains binary data: " +
                            check response.getPayloadAsString());

                } else if (mime:MULTIPART_FORM_DATA == baseType) {
                    log:printInfo("Response contains body parts: ");
                    mime:Entity[] returnParts = check response.getBodyParts();
                    handleBodyParts(returnParts);

                } else if (mime:IMAGE_PNG == baseType) {
                    log:printInfo("Response contains an image");
                }
            } else {
                log:printInfo("There's no body in response");
            }
        }
        error err => {
            log:printError(err.message, err = err);
        }
    }
}

//Get the base type from a given content type.
function getBaseType(string contentType) returns string {
    mime:MediaType mediaType = check mime:getMediaType(contentType);
    return mediaType.getBaseType();
}

//Loop through body parts and print its content.
function handleBodyParts(mime:Entity[] bodyParts) {
    foreach bodyPart in bodyParts {
        string baseType = getBaseType(bodyPart.getContentType());
        if (mime:APPLICATION_JSON == baseType) {
            json payload = check bodyPart.getJson();
            log:printInfo("Json Part: " + payload.toString());
        }
        if (mime:TEXT_PLAIN == baseType) {
            log:printInfo("Text Part: " + check bodyPart.getText());
        }
    }
}
