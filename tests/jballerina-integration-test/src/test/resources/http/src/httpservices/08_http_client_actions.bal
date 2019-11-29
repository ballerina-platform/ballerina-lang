// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/http;
import ballerina/mime;

http:Client clientEP2 = new ("http://localhost:9097", { cache: { enabled: false }});

@http:ServiceConfig {
    basePath: "/test1"
}
service backEndService on new http:Listener(9097) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/greeting"
    }
    resource function replyText(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/byteChannel"
    }
    resource function sendByteChannel(http:Caller caller, http:Request req) {
        var byteChannel = req.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            checkpanic caller->respond(<@untainted> byteChannel);
        } else {
            error err = byteChannel;
            checkpanic caller->respond(<@untainted> err.reason());
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/directPayload"
    }
    resource function postReply(http:Caller caller, http:Request req) {
        if (req.hasHeader("content-type")) {
            var mediaType = mime:getMediaType(req.getContentType());
            if (mediaType is mime:MediaType) {
                string baseType = mediaType.getBaseType();
                if (mime:TEXT_PLAIN == baseType) {
                    var textValue = req.getTextPayload();
                    if (textValue is string) {
                        checkpanic caller->respond(<@untainted> textValue);
                    } else {
                        error err = textValue;
                        checkpanic caller->respond(<@untainted string> err.reason());
                    }
                } else if (mime:APPLICATION_XML == baseType) {
                    var xmlValue = req.getXmlPayload();
                    if (xmlValue is xml) {
                        checkpanic caller->respond(<@untainted> xmlValue);
                    } else {
                        error err = xmlValue;
                        checkpanic caller->respond(<@untainted string> err.reason());
                    }
                } else if (mime:APPLICATION_JSON == baseType) {
                    var jsonValue = req.getJsonPayload();
                    if (jsonValue is json) {
                        checkpanic caller->respond(<@untainted> jsonValue);
                    } else {
                        error err = jsonValue;
                        checkpanic caller->respond(<@untainted string> err.reason());
                    }
                } else if (mime:APPLICATION_OCTET_STREAM == baseType) {
                    var blobValue = req.getBinaryPayload();
                    if (blobValue is byte[]) {
                        checkpanic caller->respond(<@untainted> blobValue);
                    } else {
                        error err = blobValue;
                        checkpanic caller->respond(<@untainted string> err.reason());
                    }
                } else if (mime:MULTIPART_FORM_DATA == baseType) {
                    var bodyParts = req.getBodyParts();
                    if (bodyParts is mime:Entity[]) {
                        checkpanic caller->respond(<@untainted> bodyParts);
                    } else {
                        error err = bodyParts;
                        checkpanic caller->respond(<@untainted> <string> err.detail()["message"]);
                    }
                }
            } else {
                checkpanic caller->respond("Error in parsing media type");
            }
        } else {
            checkpanic caller->respond();
        }
    }

    @http:ResourceConfig {
            path: "_bulk"
    }
    resource function respond(http:Caller caller, http:Request request) {
        checkpanic caller->respond(checkpanic <@untainted> request.getTextPayload());
    }

    @http:ResourceConfig {
            path: "/{id}"
    }
    resource function withWhitespacedExpression(http:Caller caller, http:Request request, string id) {
        var res = caller->respond(<@untainted> id);
    }

    @http:ResourceConfig {
            path: "/a/b%20c/d"
    }
    resource function withWhitespacedLiteral(http:Caller caller, http:Request request) {
        var res = caller->respond("dispatched to white_spaced literal");
    }
}

@http:ServiceConfig {
    basePath: "/test2"
}
service testService on new http:Listener(9098) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientGet"
    }
    resource function testGet(http:Caller caller, http:Request req) {
        string value = "";
        //No Payload
        var response1 = clientEP2->get("/test1/greeting");
        if (response1 is http:Response) {
            var result = response1.getTextPayload();
            if (result is string) {
                value = result;
            } else {
                error err = result;
                value = err.reason();
            }
        }

        //No Payload
        var response2 = clientEP2->get("/test1/greeting", ());
        if (response2 is http:Response) {
            var result = response2.getTextPayload();
            if (result is string) {
                value = value + result;
            } else {
                error err = result;
                value = value + err.reason();
            }
        }

        future<error|http:Response> asyncInvocation = start clientEP2->get("/test1/greeting", ());

        http:Request httpReq = new;
        //Request as message
        var response3 = clientEP2->get("/test1/greeting", httpReq);
        if (response3 is http:Response) {
            var result = response3.getTextPayload();
            if (result is string) {
                value = value + result;
            } else {
                error err = result;
                value = value + err.reason();
            }
        }
        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithoutBody"
    }
    resource function testPost(http:Caller caller, http:Request req) {
        string value = "";
        //No Payload
        var clientResponse = clientEP2->post("/test1/directPayload", ());
        if (clientResponse is http:Response) {
            var returnValue = clientResponse.getTextPayload();
            if (returnValue is string) {
                value = returnValue;
            } else {
                error err = returnValue;
                string? errMsg = <string>err.detail()?.message;
                value = errMsg is string ? errMsg : "Error in parsing text payload";
            }
        } else  {
            error err = clientResponse;
            value = <string>err.reason();
        }

        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/clientPostWithBody"
    }
    resource function testPostWithBody(http:Caller caller, http:Request req) {
        string value = "";
        var textResponse = clientEP2->post("/test1/directPayload", "Sample Text");
        if (textResponse is http:Response) {
            var result = textResponse.getTextPayload();
            if (result is string) {
                value = result;
            } else  {
                error err = result;
                value = err.reason();
            }
        }

        var xmlResponse = clientEP2->post("/test1/directPayload", xml `<yy>Sample Xml</yy>`);
        if (xmlResponse is http:Response) {
            var result = xmlResponse.getXmlPayload();
            if (result is xml) {
                value = value + result.getTextValue();
            } else {
                error err = result;
                value = value + err.reason();
            }
        }

        var jsonResponse = clientEP2->post("/test1/directPayload", { name: "apple", color: "red" });
        if (jsonResponse is http:Response) {
            var result = jsonResponse.getJsonPayload();
            if (result is json) {
                value = value + result.toJsonString();
            } else {
                error err = result;
                value = value + err.reason();
            }
        }
        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleBinary"
    }
    resource function testPostWithBinaryData(http:Caller caller, http:Request req) {
        string value = "";
        string textVal = "Sample Text";
        byte[] binaryValue = textVal.toBytes();
        var textResponse = clientEP2->post("/test1/directPayload", binaryValue);
        if (textResponse is http:Response) {
            var result = textResponse.getTextPayload();
            if (result is string) {
                value = result;
            } else {
                error err = result;
                value = err.reason();
            }
        }
        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleStringJson"
    }
    resource function testPostWithStringJson(http:Caller caller, http:Request request) {
      http:Request req = new;
      string payload = "a" + "\n" + "b" + "\n";
      req.setJsonPayload(payload);
      http:Response response = checkpanic clientEP2->post("/test1/_bulk", req);
      checkpanic caller->respond(checkpanic <@untainted> response.getTextPayload());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleTextAndJsonContent"
    }
    resource function testPostWithTextAndJsonContent(http:Caller caller, http:Request request) {
        http:Request req = new;
        string payload = "a" + "\n" + "b" + "\n";
        req.setTextPayload(payload, contentType = "application/json");
        http:Response response = checkpanic clientEP2->post("/test1/_bulk", req);
        checkpanic caller->respond(checkpanic <@untainted> response.getTextPayload());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleTextAndXmlContent"
    }
    resource function testPostWithTextAndXmlContent(http:Caller caller, http:Request request) {
        http:Request req = new;
        string payload = "a" + "\n" + "b" + "\n";
        req.setTextPayload(payload, contentType = "text/xml");
        http:Response response = checkpanic clientEP2->post("/test1/_bulk", req);
        checkpanic caller->respond(checkpanic <@untainted> response.getTextPayload());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleTextAndJsonAlternateContent"
    }
    resource function testPostWithTextAndJsonAlternateContent(http:Caller caller, http:Request request) {
        http:Request req = new;
        string payload = "a" + "\n" + "b" + "\n";
        req.setTextPayload(payload, contentType = "application/json");
        req.setJsonPayload(payload);
        http:Response response = checkpanic clientEP2->post("/test1/_bulk", req);
        checkpanic caller->respond(checkpanic <@untainted> response.getTextPayload());
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleStringJsonAlternate"
    }
    resource function testPostWithStringJsonAlternate(http:Caller caller, http:Request request) {
      http:Request req = new;
      string payload = "a" + "\n" + "b" + "\n";
      req.setJsonPayload(payload);
      req.setTextPayload(payload, contentType = "application/json");
      http:Response response = checkpanic clientEP2->post("/test1/_bulk", req);
      checkpanic caller->respond(checkpanic <@untainted> response.getTextPayload());
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/handleByteChannel"
    }
    resource function testPostWithByteChannel(http:Caller caller, http:Request req) {
        string value = "";
        var byteChannel = req.getByteChannel();
        if (byteChannel is io:ReadableByteChannel) {
            var res = clientEP2->post("/test1/byteChannel", <@untainted> byteChannel);
            if (res is http:Response) {
                var result = res.getTextPayload();
                if (result is string) {
                    value = result;
                } else {
                    error err = result;
                    value = err.reason();
                }
            } else {
                error err = res;
                value = err.reason();
            }
        } else {
            error err = byteChannel;
            value = err.reason();
        }
        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/handleMultiparts"
    }
    resource function testPostWithBodyParts(http:Caller caller, http:Request req) {
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
                foreach var bodyPart in returnParts {
                    var mediaType = mime:getMediaType(bodyPart.getContentType());
                    if (mediaType is mime:MediaType) {
                        string baseType = mediaType.getBaseType();
                        if (mime:APPLICATION_JSON == baseType) {
                            var payload = bodyPart.getJson();
                            if (payload is json) {
                                value = payload.toJsonString();
                            } else {
                                error err = payload;
                                value = err.reason();
                            }
                        }
                        if (mime:TEXT_PLAIN == baseType) {
                            var textVal = bodyPart.getText();
                            if (textVal is string) {
                                value = value + textVal;
                            } else {
                                error err = textVal;
                                value = value + <string>err.reason();
                            }
                        }
                    } else {
                        error err = mediaType;
                        value = value + <string>err.reason();
                    }
                }
            } else {
                error err = returnParts;
                value = err.reason();
            }
        } else {
            error err = res;
            value = err.reason();
        }
        checkpanic caller->respond(<@untainted> value);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/literal"
    }
    resource function testPathWithWhitespacesForLiteral(http:Caller caller, http:Request request) returns error? {
        http:Response response = check clientEP2->get("/test1/a/b c/d ");
        var res = caller->respond(<@untainted> response);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/expression"
    }
    resource function testClientPathWithWhitespacesForExpression(http:Caller caller, http:Request request) returns error? {
        http:Response response = check clientEP2->get("/test1/dispatched to white_spaced expression ");
        var res = caller->respond(<@untainted> response);
    }
}
