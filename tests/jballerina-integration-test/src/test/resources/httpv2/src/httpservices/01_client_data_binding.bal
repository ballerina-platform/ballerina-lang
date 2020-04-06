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
import ballerina/lang.'string as strings;
import ballerina/runtime;

http:Client basicClient = new ("http://localhost:9301");

type Person record {|
    string name;
    int age;
|};

int counter = 0;

@http:ServiceConfig {
    basePath: "/call"
}
service passthrough on new http:Listener(9300) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/allTypes"
    }
    resource function checkAllDataBindingTypes(http:Caller caller, http:Request request) returns error? {
        string payload = "";

        var res = basicClient->post("/backend/getJson", "want json", targetType = json);
        json p = <json>res;
        payload = payload + p.toJsonString();

        res = basicClient->post("/backend/getXml", "want xml", targetType = xml);
        xml q = <xml>res;
        payload = payload + " | " + q.toString();

        res = basicClient->post("/backend/getString", "want string", targetType = string);
        string r = <string>res;
        payload = payload + " | " + r;

        res = basicClient->post("/backend/getByteArray", "want byte[]", targetType = byte[]);
        byte[] val = <byte[]>res;
        string s = check <@untainted>strings:fromBytes(val);
        payload = payload + " | " + s;

        res = basicClient->post("/backend/getRecord", "want record", targetType = Person);
        Person t = <Person>res;
        payload = payload + " | " + t.name;

        res = basicClient->post("/backend/getRecordArr", "want record[]", targetType = Person[]);
        Person[] u = <Person[]>res;
        payload = payload + " | " + u[0].name + " | " + u[1].age.toString();

        res = basicClient->post("/backend/getResponse", "want record[]", targetType = http:Response);
        http:Response v = <http:Response>res;
        payload = payload + " | " + v.getHeader("x-fact");

        var result = caller->respond(<@untainted>payload);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/allMethods"
    }
    resource function checkAllRequestMethods(http:Caller caller, http:Request request) returns error? {
        string payload = "";

        var res = basicClient->get("/backend/getJson", targetType = json);
        json p = <json>res;
        payload = payload + p.toJsonString();

        res = basicClient->head("/backend/getXml", "want xml");
        http:Response v = <http:Response>res;
        payload = payload + " | " + v.getHeader("Content-type");

        res = basicClient->delete("/backend/getString", "want string", targetType = string);
        string r = <string>res;
        payload = payload + " | " + r;

        res = basicClient->put("/backend/getByteArray", "want byte[]", targetType = byte[]);
        byte[] val = <byte[]>res;
        string s = check <@untainted>strings:fromBytes(val);
        payload = payload + " | " + s;

        res = basicClient->execute("POST", "/backend/getRecord", "want record", targetType = Person);
        Person t = <Person>res;
        payload = payload + " | " + t.name;

        res = basicClient->forward("/backend/getRecordArr", request, targetType = Person[]);
        Person[] u = <Person[]>res;
        payload = payload + " | " + u[0].name + " | " + u[1].age.toString();

        var result = caller->respond(<@untainted>payload);
    }

     @http:ResourceConfig {
        methods: ["GET"],
        path: "/redirect"
    }
    resource function checkJsonDatabinding(http:Caller caller, http:Request req) {
        http:Client redirectClient = new ("http://localhost:9302", {
                followRedirects: {enabled: true, maxCount: 5}
            }
        );
        var res = redirectClient->post("/redirect1/", "want json", targetType = json);
        json p = <json>res;
        var result = caller->respond(<@untainted>p);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/retry"
    }
    resource function invokeEndpoint(http:Caller caller, http:Request request) {
        http:Client retryClient = new ("http://localhost:9301", {
                retryConfig: { intervalInMillis: 3000, count: 3, backOffFactor: 2.0,
                maxWaitIntervalInMillis: 20000 },  timeoutInMillis: 2000
            }
        );
        var backendResponse = retryClient->forward("/backend/getRetryResponse", request, targetType = string);
        string r = <string>backendResponse;
        var responseToCaller = caller->respond(<@untainted>r);
    }

    @http:ResourceConfig {
        path: "/cast"
    }
    resource function checkCastError(http:Caller caller, http:Request request) {
        var res = basicClient->post("/backend/getJson", "want json", targetType = json);
        xml p = <xml>res;
        var responseToCaller = caller->respond(<@untainted>p);
    }

    @http:ResourceConfig {
        path: "/500"
    }
    resource function check500Error(http:Caller caller, http:Request request) {
        var res = basicClient->post("/backend/get5XX", "want 500", targetType = json);
        json p = <json>res;
        var responseToCaller = caller->respond(<@untainted>p);
    }

    @http:ResourceConfig {
        path: "/500handle"
    }
    resource function handle500Error(http:Caller caller, http:Request request) {
        var res = basicClient->post("/backend/get5XX", "want 500", targetType = json);
        if res is http:RemoteServerError {
            http:Response resp = new;
            resp.statusCode = res.detail()?.statusCode ?: 500;
            resp.setPayload(<@untainted><string> res.detail()?.message);
            var responseToCaller = caller->respond(<@untainted>resp);
        } else {
            json p = <json>res;
            var responseToCaller = caller->respond(<@untainted>p);
        }
    }

    @http:ResourceConfig {
        path: "/404"
    }
    resource function check404Error(http:Caller caller, http:Request request) {
        var res = basicClient->post("/backend/getIncorrectPath404", "want 500", targetType = json);
        json p = <json>res;
        var responseToCaller = caller->respond(<@untainted>p);
    }

    @http:ResourceConfig {
        path: "/404/{path}"
    }
    resource function handle404Error(http:Caller caller, http:Request request, string path) {
        var res = basicClient->post("/backend/" + <@untainted>path, "want 500", targetType = json);
        if res is http:ClientRequestError {
            http:Response resp = new;
            resp.statusCode = res.detail()?.statusCode ?: 400;
            resp.setPayload(<@untainted><string> res.detail()?.message);
            var responseToCaller = caller->respond(<@untainted>resp);
        } else {
            json p = <json>res;
            var responseToCaller = caller->respond(<@untainted>p);
        }
    }
}

@http:ServiceConfig {
    basePath: "/backend"
}
service mockHelloService on new http:Listener(9301) {
    resource function getJson(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({id: "chamil", values: {a: 2, b: 45, c: {x: "mnb", y: "uio"}}});
        var result = caller->respond(response);
    }

    resource function getXml(http:Caller caller, http:Request req) {
        http:Response response = new;
        xml xmlStr = xml `<name>Ballerina</name>`;
        response.setXmlPayload(xmlStr);
        var result = caller->respond(response);
    }

    resource function getString(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setTextPayload("This is my @4491*&&#$^($@");
        var result = caller->respond(response);
    }

    resource function getByteArray(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setBinaryPayload("BinaryPayload is textVal".toBytes());
        var result = caller->respond(response);
    }

    resource function getRecord(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({name: "chamil", age: 15});
        var result = caller->respond(response);
    }

    resource function getRecordArr(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload([{name: "wso2", age: 12}, {name: "ballerina", age: 3}]);
        var result = caller->respond(response);
    }

    resource function getResponse(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.setJsonPayload({id: "hello"});
        response.setHeader("x-fact", "data-binding");
        var result = caller->respond(response);
    }

    resource function getRetryResponse(http:Caller caller, http:Request req) {
        counter = counter + 1;
        if (counter == 1) {
            runtime:sleep(5000);
            var responseToCaller = caller->respond("Not received");
        } else {
            var responseToCaller = caller->respond("Hello World!!!");
        }
    }

    resource function get5XX(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.statusCode = 501;
        response.setTextPayload("data-binding-failed-with-501");
        var result = caller->respond(response);
    }

    @http:ResourceConfig {
        methods: ["GET"]
    }
    resource function get4XX(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.statusCode = 400;
        response.setTextPayload("data-binding-failed-due-to-bad-request");
        var result = caller->respond(response);
    }
}

service redirect1 on new http:Listener(9302) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function redirect1(http:Caller caller, http:Request req) {
        http:Response res = new;
        var result = caller->redirect(res, http:REDIRECT_SEE_OTHER_303, ["http://localhost:9301/backend/getJson"]);
    }
}
