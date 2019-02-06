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

listener http:Listener serviceEndpoint1 = new(9092, config = { httpVersion: "2.0" });

listener http:Listener serviceEndpoint2 = new(9093, config = { httpVersion: "2.0" });

listener http:Listener serviceEndpoint3 = new(9094, config = { httpVersion: "2.0" });

http:Client endPoint1 = new("http://localhost:9093", config = {
    httpVersion: "2.0",
    followRedirects: { enabled: true, maxCount: 3 }
});

http:Client endPoint2 = new("http://localhost:9093", config = {
    httpVersion: "2.0",
    followRedirects: { enabled: true, maxCount: 5 }
});

http:Client endPoint3 = new("http://localhost:9094", config = {
    httpVersion: "2.0",
    followRedirects: { enabled: true }
});

@http:ServiceConfig {
    basePath: "/service1"
}
service testRedirect on serviceEndpoint1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function redirectClient(http:Caller caller, http:Request req) {
        var response = endPoint1->get("/redirect1");
        if (response is http:Response) {
            _ = caller->respond(response.resolvedRequestedURI);
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/maxRedirect"
    }
    resource function maxRedirectClient(http:Caller caller, http:Request req) {
        var response = endPoint1->get("/redirect1/round1");
        if (response is http:Response) {
            string value = "";
            if (response.hasHeader(http:LOCATION)) {
                value = response.getHeader(http:LOCATION);
            }
            value = value + ":" + response.resolvedRequestedURI;
            _ = caller->respond(untaint value);
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/crossDomain"
    }
    resource function crossDomain(http:Caller caller, http:Request req) {
        var response = endPoint2->get("/redirect1/round1");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/noRedirect"
    }
    resource function noRedirect(http:Caller caller, http:Request req) {
        var response = endPoint3->get("/redirect2");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithRelativePath"
    }
    resource function qpWithRelativePath(http:Caller caller, http:Request req) {
        var response = endPoint2->get("/redirect1/qpWithRelativePath");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithAbsolutePath"
    }
    resource function qpWithAbsolutePath(http:Caller caller, http:Request req) {
        var response = endPoint2->get("/redirect1/qpWithAbsolutePath");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/originalRequestWithQP"
    }
    resource function originalRequestWithQP(http:Caller caller, http:Request req) {
        var response = endPoint2->get("/redirect1/round4?key=value&lang=ballerina");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/test303"
    }
    resource function test303(http:Caller caller, http:Request req) {
        var response = endPoint3->post("/redirect2/test303", "Test value!");
        if (response is http:Response) {
            var value = response.getTextPayload();
            if (value is string) {
                value = value + ":" + response.resolvedRequestedURI;
                _ = caller->respond(untaint value);
            } else {
                panic value;
            }
        } else {
            io:println("Connector error!");
        }
    }
}

@http:ServiceConfig {
    basePath: "/redirect1"
}
service redirect1 on serviceEndpoint2 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function redirect1(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:9094/redirect2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round1"
    }
    resource function round1(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["/redirect1/round2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round2"
    }
    resource function round2(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_USE_PROXY_305, ["/redirect1/round3"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round3"
    }
    resource function round3(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_MULTIPLE_CHOICES_300, ["/redirect1/round4"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round4"
    }
    resource function round4(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["/redirect1/round5"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/round5"
    }
    resource function round5(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_FOUND_302, ["http://localhost:9094/redirect2"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithRelativePath"
    }
    resource function qpWithRelativePath(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307,
                ["/redirect1/processQP?key=value&lang=ballerina"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/qpWithAbsolutePath"
    }
    resource function qpWithAbsolutePath(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307,
                ["http://localhost:9093/redirect1/processQP?key=value&lang=ballerina"]);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/processQP"
    }
    resource function processQP(http:Caller caller, http:Request req) {
        map<string> paramsMap = req.getQueryParams();
        string returnVal = paramsMap.key + ":" + paramsMap.lang;
        _ = caller->respond(untaint returnVal);
    }
}

@http:ServiceConfig {
    basePath: "/redirect2"
}
service redirect2 on serviceEndpoint3 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function redirect2(http:Caller caller, http:Request req) {
        _ = caller->respond("hello world");
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/test303"
    }
    resource function test303(http:Caller caller, http:Request req) {
        http:Response res = new;
        _ = caller->redirect(res, http:REDIRECT_SEE_OTHER_303, ["/redirect2"]);
    }
}
