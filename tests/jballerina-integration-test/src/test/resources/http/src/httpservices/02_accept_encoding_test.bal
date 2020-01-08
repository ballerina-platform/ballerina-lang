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

import ballerina/http;
import ballerina/log;

final string ACCEPT_ENCODING = "accept-encoding";

listener http:Listener passthroughEP2 = new(9091, {server: "Mysql"});

http:Client acceptEncodingAutoEP = new("http://localhost:9091/hello", {
    compression:http:COMPRESSION_AUTO
});

http:Client acceptEncodingEnableEP = new("http://localhost:9091/hello", {
    compression:http:COMPRESSION_ALWAYS
});

http:Client acceptEncodingDisableEP = new("http://localhost:9091/hello", {
    compression:http:COMPRESSION_NEVER
});

service passthrough on passthroughEP2 {
    @http:ResourceConfig {
        path:"/"
    }
    resource function passthrough(http:Caller caller, http:Request req) {
        if (req.getHeader("AcceptValue") == "auto") {
            var clientResponse = acceptEncodingAutoEP -> post("/",<@untainted> req);
            if (clientResponse is http:Response) {
                var responseError = caller->respond(clientResponse);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            } else {
                error err = clientResponse;
                http:Response res = new;
                res.statusCode = 500;
                res.setPayload(err.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            }
        } else if (req.getHeader("AcceptValue") == "enable") {
            var clientResponse = acceptEncodingEnableEP -> post("/",<@untainted> req);
            if (clientResponse is http:Response) {
                checkpanic caller->respond(clientResponse);
            } else  {
                error err = clientResponse;
                http:Response res = new;
                res.statusCode = 500;
                res.setPayload(err.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            }
        } else if (req.getHeader("AcceptValue") == "disable") {
            var clientResponse = acceptEncodingDisableEP -> post("/",<@untainted> req);
            if (clientResponse is http:Response) {
                checkpanic caller->respond(clientResponse);
            } else {
                error err = clientResponse;
                http:Response res = new;
                res.statusCode =500;
                res.setPayload(err.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", responseError);
                }
            }
        }
    }
}

#
# Sample hello world service.
#
service hello on passthroughEP2 {

    #
    # The helloResource only accepts requests made using the specified HTTP methods
    #
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource function helloResource(http:Caller caller, http:Request req) {
        http:Response res = new;
        json payload = {};
        boolean hasHeader = req.hasHeader(ACCEPT_ENCODING);
        if (hasHeader) {
            payload = {acceptEncoding:req.getHeader(ACCEPT_ENCODING)};
        } else {
            payload = {acceptEncoding:"Accept-Encoding hdeaer not present."};
        }
        res.setJsonPayload(<@untainted> payload);
        checkpanic caller->respond(res);
    }
}
