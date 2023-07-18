// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/mime;

int targetServicePort = 9091;
int passthroughServicePort = 9092;

final http:Client clientEP = new("http://localhost:" + targetServicePort.toString());

service passthrough on new http:MockListener(passthroughServicePort) {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/consume"
    }
    resource function consume(http:Caller caller, http:Request request) returns @tainted error? {
        var bodyParts = request.getBodyParts();
        if (bodyParts is error) {
            log:printError(<string>bodyParts.reason());
            http:Response response = new;
            response.setPayload("Error in decoding multiparts in the passthrough service!");
            response.statusCode = 500;
        }
        var result = clientEP->forward("/target/consume", request);
        if (result is error) {
            log:printError("Error sending response", result);
        }
        return caller->respond(<http:Response>result);
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/process"
    }
    resource function process(http:Caller caller, http:Request request) returns @tainted error? {
        var result = clientEP->forward("/target/consume", request);
        if (result is error) {
            log:printError("Error sending response", result);
        }
        return caller->respond(<http:Response>result);
    }
}

service target on new http:Listener(targetServicePort) {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/consume"
    }
    resource function process(http:Caller caller, http:Request request) returns @tainted error? {
        http:Response response = new;
        var bodyParts = request.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            string[] parts = [];
            foreach var part in bodyParts {
                parts.push(check handleContent(part));
            }
            string payload = strings:'join(", ", ...parts);
            response.setPayload(payload);
        } else {
            log:printError(<string>bodyParts.reason());
            response.setPayload("Error in decoding multiparts!");
            response.statusCode = 500;
        }
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

function handleContent(mime:Entity bodyPart) returns @tainted string|error {
    var mediaType = mime:getMediaType(bodyPart.getContentType());
    if (mediaType is mime:MediaType) {
        string baseType = mediaType.getBaseType();
        if (mime:APPLICATION_XML == baseType || "application/xop+xml" == baseType) {
            var payload = bodyPart.getXml();
            if (payload is xml) {
                return payload.toString();
            } else {
                return error(<string>payload.detail().message);
            }
        } else if (mime:APPLICATION_JSON == baseType) {
            var payload = bodyPart.getJson();
            if (payload is json) {
                return payload.toString();
            } else {
                return error(<string>payload.detail().message);
            }
        } else if (mime:TEXT_PLAIN == baseType) {
            var payload = bodyPart.getText();
            if (payload is string) {
                return payload;
            } else {
                return error(<string>payload.detail().message);
            }
        } else if (mime:MULTIPART_FORM_DATA == baseType || mime:MULTIPART_MIXED == baseType) {
            var bodyParts = bodyPart.getBodyParts();
            if (bodyParts is mime:Entity[]) {
                string[] parts = [];
                foreach var part in bodyParts {
                    parts.push(check handleContent(part));
                }
                return strings:'join(", ", ...parts);
            } else {
                return error(<string>bodyParts.detail().message);
            }
        } else {
            return error("Unsupported media type: " + baseType);
        }
    }
    return error("Invalid media type: " + bodyPart.getContentType());
}
