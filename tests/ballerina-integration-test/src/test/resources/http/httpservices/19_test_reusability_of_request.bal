import ballerina/http;
import ballerina/io;
import ballerina/file;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEP1 {
    url:"http://localhost:9115/test"
};

endpoint http:Listener testEP {
    port:9115
};

@http:ServiceConfig {basePath:"/reuseObj"}
service<http:Service> testService_1 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_without_entity"
    }
    getWithoutEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        string firstVal = "";
        string secondVal = "";

        var firstResponse = clientEP1 -> get("", message = clientReq);
        if (firstResponse is http:Response) {
            var result = untaint firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else if (result is error) {
                firstVal = result.reason();
            }
        } else if (firstResponse is error) {
            firstVal = firstResponse.reason();
        }

        var secondResponse = clientEP1 -> get("", message = clientReq);
        if (secondResponse is http:Response) {
            var result = untaint secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else if (result is error) {
                secondVal = result.reason();
            }
        } else if (secondResponse is error) {
            secondVal = secondResponse.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_empty_entity"
    }
    getWithEmptyEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        mime:Entity entity = new;
        clientReq.setEntity(entity);

        string firstVal = "";
        string secondVal = "";

        var firstResponse = clientEP1 -> get("", message = clientReq);
        if (firstResponse is http:Response) {
            var result = untaint firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else if (result is error) {
                firstVal = result.reason();
            }
        } else if (firstResponse is error) {
            firstVal = firstResponse.reason();
        }

        var secondResponse = clientEP1 -> get("", message = clientReq);
        if (secondResponse is http:Response) {
            var result = untaint secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else if (result is error) {
                secondVal = result.reason();
            }
        } else if (secondResponse is error) {
            secondVal = secondResponse.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/two_request_same_entity"
    }
    getWithEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setHeader("test1", "value1");
        http:Request newRequest = new;
        string firstVal = "";
        string secondVal = "";
        http:Response testResponse = new;

        var entity = clientReq.getEntity();
        if (entity is mime:Entity) {
            newRequest.setEntity(entity);
            var firstResponse = clientEP1 -> get("", message = clientReq);
            if (firstResponse is http:Response) {
                newRequest.setHeader("test2", "value2");
                var secondResponse = clientEP1 -> get("", message = newRequest);
                if (secondResponse is http:Response) {
                    var result1 = untaint firstResponse.getTextPayload();
                    if (result1 is string) {
                        firstVal = result1;
                    } else if (result1 is error) {
                        firstVal = result1.reason();
                    }
                    var result2 = untaint secondResponse.getTextPayload();
                    if (result2 is string) {
                        secondVal = result2;
                    } else if (result2 is error) {
                        secondVal = result2.reason();
                    }
                } else if (secondResponse is error) {
                    log:printError(secondResponse.reason(), err = secondResponse);
                }
            } else if (firstResponse is error) {
                log:printError(firstResponse.reason(), err = firstResponse);
            }
        } else if (entity is error) {
            log:printError(entity.reason(), err = entity);
        }
        testResponse.setTextPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/request_with_datasource"
    }
    postWithEntity(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        clientReq.setTextPayload("String datasource");

        string firstVal = "";
        string secondVal = "";

        var firstResponse = clientEP1 -> post("/datasource", clientReq);
        if (firstResponse is http:Response) {
            var result = untaint firstResponse.getTextPayload();
            if (result is string) {
                firstVal = result;
            } else if (result is error) {
                firstVal = result.reason();
            }
        } else if (firstResponse is error) {
            firstVal = firstResponse.reason();
        }

        var secondResponse = clientEP1 -> post("/datasource", clientReq);
        if (secondResponse is http:Response) {
            var result = untaint secondResponse.getTextPayload();
            if (result is string) {
                secondVal = result;
            } else if (result is error) {
                secondVal = result.reason();
            }
        } else if (secondResponse is error) {
            secondVal = secondResponse.reason();
        }
        http:Response testResponse = new;
        testResponse.setPayload(firstVal + secondVal);
        _ = outboundEP -> respond(testResponse);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/request_with_bytechannel"
    }
    postWithByteChannel(endpoint outboundEP, http:Request clientRequest) {
        http:Request clientReq = new;
        var byteChannel = clientRequest.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            clientReq.setByteChannel(byteChannel, contentType = "text/plain");
            var firstResponse = clientEP1 -> post("/consumeChannel", clientReq);
            if (firstResponse is http:Response) {
                var secondResponse = clientEP1 -> post("/consumeChannel", clientReq);
                http:Response testResponse = new;
                string firstVal = "";
                string secondVal = "";
                if (secondResponse is http:Response) {
                    var result1 = secondResponse.getTextPayload();
                    if  (result1 is string) {
                        secondVal = result1;
                    } else if (result1 is error) {
                        secondVal = "Error in parsing payload";
                    }
                } else if (secondResponse is error) {
                    secondVal = <string> secondResponse.detail().message;
                }
                var result2 = firstResponse.getTextPayload();
                if (result2 is string) {
                    firstVal = result2;
                } else if (result2 is error) {
                    firstVal = result2.reason();
                }
                testResponse.setTextPayload(untaint firstVal + untaint secondVal);
                _ = outboundEP -> respond(testResponse);
            } else if (firstResponse is error) {
                log:printError(firstResponse.reason(), err = firstResponse);
            }
        } else if (byteChannel is error) {
            log:printError(byteChannel.reason(), err = byteChannel);
        }
    }
}

@http:ServiceConfig {basePath:"/test"}
service<http:Service> testService_2 bind testEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    testForGet(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from GET!");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/datasource"
    }
    testForPost(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        response.setTextPayload("Hello from POST!");
        _ = outboundEP -> respond(response);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/consumeChannel"
    }
    testRequestBody(endpoint outboundEP, http:Request clientRequest) {
        http:Response response = new;
        var stringPayload = clientRequest.getTextPayload();
        if (stringPayload is string) {
            response.setPayload(untaint stringPayload);
        } else if (stringPayload is error) {
            string errMsg = <string> stringPayload.detail().message;
            response.setPayload(errMsg);
        }
        _ = outboundEP -> respond(response);
    }
}
