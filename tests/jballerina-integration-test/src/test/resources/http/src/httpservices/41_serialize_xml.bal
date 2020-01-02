// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/log;
import ballerina/mime;

listener http:Listener listenerEP = new(9250);
http:Client clientEP = new("http://localhost:9250");

@http:ServiceConfig {
    basePath:"/serialize"
}
service serializer on listenerEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/xml"
    }
    resource function serializeXml (http:Caller caller, http:Request req) {
        //Create an `xml` body part as a file upload.
        mime:Entity xmlFilePart = new;
        xmlFilePart.setContentDisposition(
                       getContentDispositionForFormData("xml file part"));
        xmlFilePart.setFileAsEntityBody("../../../src/test/resources/http/ComplexTestXmlSample.xml",
                                        contentType = mime:APPLICATION_XML);
        // Create an array to hold all the body parts.
        mime:Entity[] bodyParts = [xmlFilePart];
        http:Request request = new;
        request.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);
        var returnResponse = clientEP->post("/serialize/decode", request);
        if (returnResponse is http:Response) {
            var result = caller->respond(returnResponse);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        } else {
            http:Response response = new;
            response.setPayload("Error occurred while sending multipart request!");
            response.statusCode = 500;
            var result = caller->respond(response);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/decode"
    }
    resource function multipartReceiver(http:Caller caller, http:Request
                                        request) {
        http:Response response = new;
        // Extracts body parts from the request.
        var bodyParts = request.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            foreach var part in bodyParts {
                var payload = part.getXml();
                if (payload is xml) {
                    response.setPayload(<@untainted> payload);
                } else {
                    response.setPayload(<@untainted> <string> payload.detail().message);
                }
                break; //Accepts only one part
            }
        } else {
            log:printError(<string> bodyParts.reason());
            response.setPayload("Error in decoding multiparts!");
            response.statusCode = 500;
        }
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error sending response", err = result);
        }
    }
}

function getContentDispositionForFormData(string partName)
                                    returns (mime:ContentDisposition) {
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name = partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}
