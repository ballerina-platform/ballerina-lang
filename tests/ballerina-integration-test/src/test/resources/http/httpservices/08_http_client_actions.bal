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
        var byteChannel = req.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            _ = client->respond(untaint byteChannel);
        } else if (byteChannel is error) {
            _ = client->respond(untaint byteChannel.reason());
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/directPayload"
    }
    postReply(endpoint client, http:Request req) {
        if (req.hasHeader("content-type")) {
            var mediaType = mime:getMediaType(req.getContentType());
            if (mediaType is mime:MediaType) {
                string baseType = mediaType.getBaseType();
                if (mime:TEXT_PLAIN == baseType) {
                    var textValue = req.getTextPayload();
                    if (textValue is string) {
                        _ = client->respond(untaint textValue);
                    } else if (textValue is error) {
                        _ = client->respond(untaint textValue.reason());
                    }
                } else if (mime:APPLICATION_XML == baseType) {
                    var xmlValue = req.getXmlPayload();
                    if (xmlValue is xml) {
                        _ = client->respond(untaint xmlValue);
                    } else if (xmlValue is error) {
                        _ = client->respond(untaint xmlValue.reason());
                    }
                } else if (mime:APPLICATION_JSON == baseType) {
                    var jsonValue = req.getJsonPayload();
                    if (jsonValue is json) {
                        _ = client->respond(untaint jsonValue);
                    } else if (jsonValue is error) {
                        _ = client->respond(untaint jsonValue.reason());
                    }
                } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                    var blobValue = req.getBinaryPayload();
                    if (blobValue is byte[]) {
                        _ = client->respond(untaint blobValue);
                    } else if (blobValue is error) {
                        _ = client->respond(untaint blobValue.reason());
                    }
                } else if (mime:MULTIPART_FORM_DATA == baseType) {
                    var bodyParts = req.getBodyParts();
                    if (bodyParts is mime:Entity[]) {
                    _ = client->respond(untaint bodyParts);
                    } else if (bodyParts is error) {
                    _ = client->respond(untaint bodyParts.reason());
                    }
                }
            } else if (mediaType is error) {
                _ = client->respond("Error in parsing media type");
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
        string value = "";
        //No Payload
        var response1 = clientEP2->get("/test1/greeting");
        if (response1 is http:Response) {
            var result = response1.getTextPayload();
            if (result is string) {
                value = result;
            } else if (result is error) {
                value = result.reason();
            }
        }

        //No Payload
        var response2 = clientEP2->get("/test1/greeting", message = ());
        if (response2 is http:Response) {
            var result = response2.getTextPayload();
            if (result is string) {
                value = value + result;
            } else if (result is error) {
                value = value + result.reason();
            }
        }

        http:Request httpReq = new;
        //Request as message
        var response3 = clientEP2->get("/test1/greeting", message = httpReq);
        if (response3 is http:Response) {
            var result = response3.getTextPayload();
            if (result is string) {
                value = value + result;
            } else if (result is error) {
                value = value + result.reason();
            }
        }
        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithoutBody"
    }
    testPost(endpoint client, http:Request req) {
        string value = "";
        //No Payload
        var clientResponse = clientEP2->post("/test1/directPayload", ());
        if (clientResponse is http:Response) {
            var returnValue = clientResponse.getTextPayload();
            if (returnValue is string) {
                value = returnValue;
            } else if (returnValue is error) {
                value = <string> returnValue.detail().message;
            }
        } else if (clientResponse is error) {
            value = clientResponse.reason();
        }

        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithBody"
    }
    testPostWithBody(endpoint client, http:Request req) {
        string value = "";
        var textResponse = clientEP2->post("/test1/directPayload", "Sample Text");
        if (textResponse is http:Response) {
            var result = textResponse.getTextPayload();
            if (result is string) {
                value = result;
            } else if (result is error) {
                value = result.reason();
            }
        }

        var xmlResponse = clientEP2->post("/test1/directPayload", xml `<yy>Sample Xml</yy>`);
        if (xmlResponse is http:Response) {
            var result = xmlResponse.getXmlPayload();
            if (result is xml) {
                value = value + result.getTextValue();
            } else if (result is error) {
                value = value + result.reason();
            }
        }

        var jsonResponse = clientEP2->post("/test1/directPayload", { name: "apple", color: "red" });
        if (jsonResponse is http:Response) {
            var result = jsonResponse.getJsonPayload();
            if (result is json) {
                value = value + result.toString();
            } else if (result is error) {
                value = value + result.reason();
            }
        }
        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleBinary"
    }
    testPostWithBinaryData(endpoint client, http:Request req) {
        string value = "";
        string textVal = "Sample Text";
        byte[] binaryValue = textVal.toByteArray("UTF-8");
        var textResponse = clientEP2->post("/test1/directPayload", binaryValue);
        if (textResponse is http:Response) {
            var result = textResponse.getPayloadAsString();
            if (result is string) {
                value = result;
            } else if (result is error) {
                value = result.reason();
            }
        }
        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/handleByteChannel"
    }
    testPostWithByteChannel(endpoint client, http:Request req) {
        string value = "";
        var byteChannel = req.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            var res = clientEP2->post("/test1/byteChannel", untaint byteChannel);
            if (res is http:Response) {
                var result = res.getPayloadAsString();
                if (result is string) {
                    value = result;
                } else if (result is error) {
                    value = result.reason();
                }
            } else if (res is error) {
                value = res.reason();
            }
        } else if (byteChannel is error) {
            value = byteChannel.reason();
        }
        _ = client->respond(untaint value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleMultiparts"
    }
    testPostWithBodyParts(endpoint client, http:Request req) {
        string value = "";
        mime:Entity part1 = new;
        part1.setJson({ "name": "wso2" });
        mime:Entity part2 = new;
        part2.setText("Hello");
        mime:Entity[] bodyParts = [part1, part2];

        var res = clientEP2->post("/test1/directPayload", bodyParts);
        if (res is http:Response) {
            var returnParts = res.getBodyParts();
            if (returnParts is mime:Entity[]) {
                foreach bodyPart in returnParts {
                    var mediaType = mime:getMediaType(bodyPart.getContentType());
                    if (mediaType is mime:MediaType) {
                        string baseType = mediaType.getBaseType();
                        if (mime:APPLICATION_JSON == baseType) {
                            var payload = bodyPart.getJson();
                            if (payload is json) {
                                value = payload.toString();
                            } else if (payload is error) {
                                value = payload.reason();
                            }
                        }
                        if (mime:TEXT_PLAIN == baseType) {
                            var textVal = bodyPart.getText();
                            if (textVal is string) {
                                value = value + textVal;
                            } else if (textVal is error) {
                                value = value + textVal.reason();
                            }
                        }
                    } else if (mediaType is error) {
                        value = value + mediaType.reason();
                    }
                }
            } else if (returnParts is error) {
                value = returnParts.reason();
            }
        } else if (res is error) {
            value = res.reason();
        }
        _ = client->respond(untaint value);
    }
}
