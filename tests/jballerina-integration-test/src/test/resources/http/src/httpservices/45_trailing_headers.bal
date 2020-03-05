// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

listener http:Listener epBackend = new(9257);
http:Client clientEp = new ("http://localhost:9257");

service initiator on new http:Listener(9256) {
    @http:ResourceConfig {
        path: "{svc}/{rsc}"
    }
    resource function echoResponse(http:Caller caller, http:Request request, string svc, string rsc) {
        var responseFromBackend = clientEp->forward("/" + <@untainted> svc + "/" + <@untainted> rsc, request);
        if (responseFromBackend is http:Response) {
            var textPayload = responseFromBackend.getTextPayload();

            string trailerHeaderValue = "No trailer header";
            if (responseFromBackend.hasHeader("trailer")) {
                trailerHeaderValue = responseFromBackend.getHeader("trailer");
            }
            string firstTrailer = "No trailer header foo";
            if (responseFromBackend.hasHeader("foo", position = "trailing")) {
                firstTrailer = responseFromBackend.getHeader("foo", position = "trailing");
            }
            string secondTrailer = "No trailer header baz";
            if (responseFromBackend.hasHeader("baz", position = "trailing")) {
                secondTrailer = responseFromBackend.getHeader("baz", position = "trailing");
            }

            http:Response newResponse = new;
            newResponse.setJsonPayload({ foo: <@untainted> firstTrailer, baz: <@untainted> secondTrailer });
            newResponse.setHeader("response-trailer", trailerHeaderValue);
            var resultSentToClient = caller->respond(<@untainted> newResponse);
        } else {
            var resultSentToClient = caller->respond("No response from backend");
        }
    }
}

@http:ServiceConfig {
    chunking: http:CHUNKING_ALWAYS
}
service chunkingBackend on epBackend {
    resource function echo(http:Caller caller, http:Request request) {
        http:Response response = new;
        var textPayload = request.getTextPayload();
        string inPayload = textPayload is string ? textPayload : "error in accessing payload";
        response.setTextPayload(<@untainted> inPayload);
        response.setHeader("foo", "Trailer for chunked payload", position = "trailing");
        response.setHeader("baz", "The second trailer", position = "trailing");
        var result = caller->respond(response);
    }

    resource function empty(http:Caller caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("");
        response.setHeader("foo", "Trailer for empty payload", position = "trailing");
        response.setHeader("baz", "The second trailer for empty payload", position = "trailing");
        var result = caller->respond(response);
    }
}
@http:ServiceConfig {
    chunking: http:CHUNKING_NEVER
}
service nonChunkingBackend on epBackend {
    resource function echo(http:Caller caller, http:Request request) {
        http:Response response = new;
        var textPayload = request.getTextPayload();
        string inPayload = textPayload is string ? textPayload : "error in accessing payload";
        response.setTextPayload(<@untainted> inPayload);
        response.setHeader("foo", "Trailer for non chunked payload", position = "trailing");
        response.setHeader("baz", "The second trailer", position = "trailing");
        var result = caller->respond(response);
    }
}
