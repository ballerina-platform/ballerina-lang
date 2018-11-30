import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

//Client endpoint.
http:Client clientEP = new("http://localhost:9091/backEndService");

//Service to test HTTP client actions with different payload types.
service actionService on new http:Listener(9090) {

    resource function messageUsage(http:Caller caller, http:Request req) {

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

        //POST action with byte array as payload.
        string textVal = "Sample Text";
        byte[] binaryValue = textVal.toByteArray("UTF-8");
        response = clientEP->post("/echo", binaryValue);
        handleResponse(response);

        //Get a byte channel to a given file.
        io:ReadableByteChannel byteChannel = io:openReadableFile("./files/logo.png");

        //POST action with byte channel as payload. Xince the file path is static
        //`untaint` is used to denote that the byte channel is trusted .
        response = clientEP->post("/image", untaint byteChannel);
        handleResponse(response);

        //Create a json body part.
        mime:Entity part1 = new;
        part1.setJson({ "name": "Jane" });

        //Create a text body part.
        mime:Entity part2 = new;
        part2.setText("Hello");

        //POST action with body parts as payload.
        mime:Entity[] bodyParts = [part1, part2];
        response = clientEP->post("/echo", bodyParts);
        handleResponse(response);

        var result = caller->respond("Client actions successfully executed!");
        handleError(result);
    }
}

//Back end service that send out different payload types as response.
service backEndService on new http:Listener(9091) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/greeting"
    }
    resource function replyText(http:Caller caller, http:Request req) {
        _ = caller->respond("Hello");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/echo"
    }
    resource function directResponse(http:Caller caller, http:Request req) {
        if (req.hasHeader("content-type")) {
            string baseType = getBaseType(req.getContentType());

            if (mime:TEXT_PLAIN == baseType) {
                var returnValue = req.getTextPayload();
                string textValue = "";
                if (returnValue is string) {
                    textValue = returnValue;
                } else if (returnValue is error) {
                    textValue = string.create(returnValue.detail().message);
                }
                var result = caller->respond(untaint textValue);
                handleError(result);

            } else if (mime:APPLICATION_XML == baseType) {
                var xmlValue = req.getXmlPayload();
                if (xmlValue is xml) {
                    var result = caller->respond(untaint xmlValue);
                    handleError(result);
                } else if (xmlValue is error) {
                    sendErrorMsg(caller, xmlValue);
                }

            } else if (mime:APPLICATION_JSON == baseType) {
                var jsonValue = req.getJsonPayload();
                if (jsonValue is json) {
                    var result = caller->respond(untaint jsonValue);
                    handleError(result);
                } else if (jsonValue is error) {
                    sendErrorMsg(caller, jsonValue);
                }

            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                var blobValue = req.getBinaryPayload();
                if (blobValue is byte[]) {
                    var result = caller->respond(untaint blobValue);
                    handleError(result);
                } else if (blobValue is error) {
                    sendErrorMsg(caller, blobValue);
                }

            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                var bodyParts = req.getBodyParts();
                if (bodyParts is mime:Entity[]) {
                    var result = caller->respond(untaint bodyParts);
                    handleError(result);
                } else if (bodyParts is error) {
                    sendErrorMsg(caller, bodyParts);
                }
            }
        } else {
            var result = caller->respond(());
            handleError(result);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/image"
    }
    resource function sendByteChannel(http:Caller caller, http:Request req) {
        var bytes = req.getBinaryPayload();
        if (bytes is byte[]) {
            http:Response response = new;
            response.setBinaryPayload(untaint bytes, contentType = mime:IMAGE_PNG);
            var result = caller->respond(response);
            handleError(result);
        } else if (bytes is error) {
            sendErrorMsg(caller, bytes);
        }
    }
}

//Handle response data received from HTTP client actions.
function handleResponse(http:Response|error response) {
    if (response is http:Response) {
        //Print the content type of the received data.
        if (response.hasHeader("content-type")) {
            string baseType = getBaseType(response.getContentType());

            if (mime:TEXT_PLAIN == baseType) {
                var payload = response.getTextPayload();
                if (payload is string) {
                    log:printInfo("Text data: " + payload);
                } else if (payload is error) {
                    log:printError("Error in parsing text data", err = payload);
                }
            } else if (mime:APPLICATION_XML == baseType) {
                var payload = response.getXmlPayload();
                if (payload is xml) {
                    string strValue = io:sprintf("%s", payload);
                    log:printInfo("Xml data: " + strValue);
                } else if (payload is error) {
                    log:printError("Error in parsing xml data", err = payload);
                }
            } else if (mime:APPLICATION_JSON == baseType) {
                var payload = response.getJsonPayload();
                if (payload is json) {
                    log:printInfo("Json data: " + payload.toString());
                } else if (payload is error) {
                    log:printError("Error in parsing json data", err = payload);
                }
            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                var payload = response.getPayloadAsString();
                if (payload is string) {
                    log:printInfo("Response contains binary data: " + payload);
                } else if (payload is error) {
                    log:printError("Error in parsing binary data", err = payload);
                }
            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                log:printInfo("Response contains body parts: ");
                var payload = response.getBodyParts();
                if (payload is mime:Entity[]) {
                    handleBodyParts(payload);
                } else if (payload is error) {
                    log:printError("Error in parsing multipart data", err = payload);
                }
            } else if (mime:IMAGE_PNG == baseType) {
                log:printInfo("Response contains an image");
            }
        } else {
            log:printInfo("Entity body is not available");
        }
    } else if (response is error) {
        log:printError(response.reason(), err = response);
    }
}

function sendErrorMsg(http:Caller caller, error err) {
    http:Response res = new;
    res.statusCode = 500;
    res.setPayload(untaint string.create(err.detail().message));
    var result = caller->respond(res);
    handleError(result);
}

function handleError(error? result) {
    if (result is error) {
        log:printError(result.reason(), err = result);
    }
}

//Get the base type from a given content type.
function getBaseType(string contentType) returns string {
    var result = mime:getMediaType(contentType);
    if (result is mime:MediaType) {
        return result.getBaseType();
    } else if (result is error) {
        panic result;
    }
    return "";
}

//Loop through body parts and print its content.
function handleBodyParts(mime:Entity[] bodyParts) {
    foreach bodyPart in bodyParts {
        string baseType = getBaseType(bodyPart.getContentType());
        if (mime:APPLICATION_JSON == baseType) {
            var payload = bodyPart.getJson();
            if (payload is json) {
                log:printInfo("Json Part: " + payload.toString());
            } else if (payload is error) {
                log:printError(payload.reason(), err = payload);
            }
        }
        if (mime:TEXT_PLAIN == baseType) {
                var payload = bodyPart.getText();
            if (payload is string) {
                log:printInfo("Text Part: " + payload);
            } else if (payload is error) {
                log:printError(payload.reason(), err = payload);
            }
        }
    }
}
