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

http:Client stockqEP = new("http://localhost:9107");

@http:ServiceConfig {
    basePath:"/product"
}
service headerService on new http:Listener(9106) {

    resource function value(http:Caller caller, http:Request req) {
        req.setHeader("core", "aaa");
        req.addHeader("core", "bbb");

        var result = stockqEP->get("/sample/stocks", <@untainted> req);
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function id(http:Caller caller, http:Request req) {
        http:Response clntResponse = new;
        var clientResponse = stockqEP->forward("/sample/customers", req);
        if (clientResponse is http:Response) {
            json payload = {};
            if (clientResponse.hasHeader("person")) {
                string[] headers = clientResponse.getHeaders("person");
                if (headers.length() == 2) {
                    payload = {header1:headers[0], header2:headers[1]};
                } else {
                    payload = {"response":"expected number of 'person' headers not found"};
                }
            } else {
                payload = {"response":"person header not available"};
            }
            checkpanic caller->respond(payload);
        } else {
            error err = clientResponse;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function nonEntityBodyGet(http:Caller caller, http:Request req) {
        var result = stockqEP->get("/sample/entitySizeChecker");
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function entityBodyGet(http:Caller caller, http:Request req) {
        var result = stockqEP->get("/sample/entitySizeChecker", "hello");
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function entityGet(http:Caller caller, http:Request req) {
        http:Request request = new;
        request.setHeader("X_test", "One header");
        var result = stockqEP->get("/sample/entitySizeChecker", request);
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function entityForward(http:Caller caller, http:Request req) {
        var result = stockqEP->forward("/sample/entitySizeChecker", req);
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function entityExecute(http:Caller caller, http:Request req) {
        var result = stockqEP->execute("GET", "/sample/entitySizeChecker", "hello ballerina");
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function noEntityExecute(http:Caller caller, http:Request req) {
        var result = stockqEP->execute("GET", "/sample/entitySizeChecker", ());
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }

    resource function passthruGet(http:Caller caller, http:Request req) {
        var result = stockqEP->get("/sample/entitySizeChecker", <@untainted> req);
        if (result is http:Response) {
            checkpanic caller->respond(result);
        } else {
            error err = result;
            checkpanic caller->respond(err.reason());
        }
    }
}

@http:ServiceConfig {
    basePath:"/sample"
}
service quoteService1 on new http:Listener(9107) {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource function company(http:Caller caller, http:Request req) {
        json payload = {};
        if (req.hasHeader("core")) {
            string[] headers = req.getHeaders("core");
            if (headers.length() == 2) {
                payload = {header1:headers[0], header2:headers[1]};
            } else {
                payload = {"response":"expected number of 'core' headers not found"};
            }
        } else {
            payload = {"response":"core header not available"};
        }
        http:Response res = new;
        res.setJsonPayload(<@untainted> payload);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/customers"
    }
    resource function product(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setHeader("person", "kkk");
        res.addHeader("person", "jjj");
        checkpanic caller->respond(res);
    }

    resource function entitySizeChecker(http:Caller caller, http:Request req) {
        if (req.hasHeader("content-length")) {
            checkpanic caller->respond("Content-length header available");
        } else {
            checkpanic caller->respond("No Content size related header present");
        }
    }
}
