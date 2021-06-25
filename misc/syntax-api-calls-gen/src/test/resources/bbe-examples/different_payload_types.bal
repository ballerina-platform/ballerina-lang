import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

//[Client](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client) endpoint.
http:Client clientEP = check new ("http://localhost:9091/backEndService");

//Service to test HTTP client remote functions with different payload types.
service /actionService on new http:Listener(9090) {

    resource function 'default messageUsage(http:Caller caller,
                                            http:Request req) {

        //[GET](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#get) remote function without any payload.
        var response = clientEP->get("/greeting");
        handleResponse(response);

        //[GET](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#get) remote function with
        //the request given as a message.
        http:Request request = new;
        response = clientEP->get("/greeting", message = request);
        handleResponse(response);

        //[POST](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#post) remote function without any payload.
        response = clientEP->post("/echo", ());
        handleResponse(response);

        //[POST](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#post) remote function with
        //text as the payload.
        response = clientEP->post("/echo", "Sample Text");
        handleResponse(response);

        //[POST](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#post) remote function with
        //`xml` as the payload.
        response = clientEP->post("/echo", xml `<yy>Sample Xml</yy>`);
        handleResponse(response);

        //POST remote function with `json` as the payload.
        response = clientEP->post("/echo", {name: "apple", color: "red"});
        handleResponse(response);

        //[POST](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#post) remote function with
        //`byte[]` as the payload.
        string textVal = "Sample Text";
        byte[] binaryValue = textVal.toBytes();
        response = clientEP->post("/echo", binaryValue);
        handleResponse(response);

         //[Create a JSON body part](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/mime/latest/mime/classes/Entity#setJson).
        mime:Entity part1 = new;
        part1.setJson({"name": "Jane"});

        //[Create a text body part](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/mime/latest/mime/classes/Entity#setText).
        mime:Entity part2 = new;
        part2.setText("Hello");

        //[POST](https://ballerina.io/swan-lake/learn/api-docs/ballerina/#/ballerina/http/latest/http/clients/Client#post) remote function
        //with a body parts as the payload.
        mime:Entity[] bodyParts = [part1, part2];
        response = clientEP->post("/echo", bodyParts);
        handleResponse(response);

        var result = caller->respond(
                                "Client actions successfully executed!");
        handleError(result);
    }
}

//Back end service that send out different payload types as response.
service /backEndService on new http:Listener(9091) {

    resource function get greeting(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello");
        if (result is error) {
            log:printError("Error in responding to caller", err = result);
        }
    }

    resource function post echo(http:Caller caller, http:Request req) {
        if (req.hasHeader("content-type")) {
            string baseType = getBaseType(req.getContentType());
            if (mime:TEXT_PLAIN == baseType) {
                var returnValue = req.getTextPayload();
                string textValue = "";
                if (returnValue is string) {
                    textValue = returnValue;
                } else {
                    textValue = returnValue.message();
                }
                var result = caller->respond(<@untainted>textValue);
                handleError(result);
            } else if (mime:APPLICATION_XML == baseType) {
                var xmlValue = req.getXmlPayload();
                if (xmlValue is xml) {
                    var result = caller->respond(<@untainted>xmlValue);
                    handleError(result);
                } else {
                    sendErrorMsg(caller, xmlValue);
                }
            } else if (mime:APPLICATION_JSON == baseType) {
                var jsonValue = req.getJsonPayload();
                if (jsonValue is json) {
                    var result = caller->respond(<@untainted>jsonValue);
                    handleError(result);
                } else {
                    sendErrorMsg(caller, jsonValue);
                }
            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                var blobValue = req.getBinaryPayload();
                if (blobValue is byte[]) {
                    var result = caller->respond(<@untainted>blobValue);
                    handleError(result);
                } else {
                    sendErrorMsg(caller, blobValue);
                }
            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                var bodyParts = req.getBodyParts();
                if (bodyParts is mime:Entity[]) {
                    var result = caller->respond(<@untainted>bodyParts);
                    handleError(result);
                } else {
                    sendErrorMsg(caller, bodyParts);
                }
            }
        } else {
            var result = caller->respond(());
            handleError(result);
        }
    }

    resource function post image(http:Caller caller, http:Request req) {
        var bytes = req.getBinaryPayload();
        if (bytes is byte[]) {
            http:Response response = new;
            response.setBinaryPayload(<@untainted>bytes,
                                        contentType = mime:IMAGE_PNG);
            var result = caller->respond(response);
            handleError(result);
        } else {
            sendErrorMsg(caller, bytes);
        }
    }
}

//Handle response data received from HTTP client remote functions.
function handleResponse(http:Response|http:PayloadType|error response) {
    if (response is http:Response) {
        //Print the content type of the received data.
        if (response.hasHeader("content-type")) {
            string baseType = getBaseType(response.getContentType());
            if (mime:TEXT_PLAIN == baseType) {
                var payload = response.getTextPayload();
                if (payload is string) {
                    log:print("Text data: " + payload);
                } else {
                    log:printError("Error in parsing text data", err = payload);
                }
            } else if (mime:APPLICATION_XML == baseType) {
                var payload = response.getXmlPayload();
                if (payload is xml) {
                    string strValue = io:sprintf("%s", payload);
                    log:print("Xml data: " + strValue);
                } else {
                    log:printError("Error in parsing xml data", err = payload);
                }
            } else if (mime:APPLICATION_JSON == baseType) {
                var payload = response.getJsonPayload();
                if (payload is json) {
                    log:print("Json data: " + payload.toJsonString());
                } else {
                    log:printError("Error in parsing json data", err = payload);
                }
            } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                var payload = response.getTextPayload();
                if (payload is string) {
                    log:print("Response contains binary data: " + payload);
                } else {
                    log:printError("Error in parsing binary data",
                                    err = payload);
                }
            } else if (mime:MULTIPART_FORM_DATA == baseType) {
                log:print("Response contains body parts: ");
                var payload = response.getBodyParts();
                if (payload is mime:Entity[]) {
                    handleBodyParts(payload);
                } else {
                    log:printError("Error in parsing multipart data",
                                    err = payload);
                }
            } else if (mime:IMAGE_PNG == baseType) {
                log:print("Response contains an image");
            }
        } else {
            log:print("Entity body is not available");
        }
    } else {
        error err = <error>response;
        log:printError(err.message(), err = err);
    }
}

function sendErrorMsg(http:Caller caller, error err) {
    http:Response res = new;
    res.statusCode = 500;
    res.setPayload(<@untainted>err.message());
    var result = caller->respond(res);
    handleError(result);
}

function handleError(error? result) {
    if (result is error) {
        log:printError(result.message(), err = result);
    }
}

//Get the base type from a given content type.
function getBaseType(string contentType) returns string {
    var result = mime:getMediaType(contentType);
    if (result is mime:MediaType) {
        return result.getBaseType();
    } else {
        panic result;
    }
}

//Loop through body parts and print its content.
function handleBodyParts(mime:Entity[] bodyParts) {
    foreach var bodyPart in bodyParts {
        string baseType = getBaseType(bodyPart.getContentType());
        if (mime:APPLICATION_JSON == baseType) {
            var payload = bodyPart.getJson();
            if (payload is json) {
                log:print("Json Part: " + payload.toJsonString());
            } else {
                log:printError(payload.message(), err = payload);
            }
        }
        if (mime:TEXT_PLAIN == baseType) {
                var payload = bodyPart.getText();
            if (payload is string) {
                log:print("Text Part: " + payload);
            } else {
                log:printError(payload.message(), err = payload);
            }
        }
    }
}
