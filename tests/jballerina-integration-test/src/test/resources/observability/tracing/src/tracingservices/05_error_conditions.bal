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
import ballerina/observe;

@http:ServiceConfig {
    basePath:"/test-service"
}
service testServiceFive on new http:Listener(9095) {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-1"
    }
    resource function resourceOne(http:Caller caller, http:Request clientRequest) {
        var ret = returnError("error 1");

        http:Response outResponse = new;
        outResponse.setTextPayload("Hello, World! from resource one");
        checkpanic caller->respond(outResponse);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-2"
    }
    resource function resourceTwo(http:Caller caller, http:Request clientRequest) {
        var ret = returnErrorWithOptionalErrorReturn("error 2", true);
        if (ret is error) {
            http:Response outResponse = new;
            outResponse.setTextPayload("Hello, World! from resource two");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error("expected test error not returned");
            panic err;
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-3"
    }
    resource function resourceThree(http:Caller caller, http:Request clientRequest) {
        var ret = returnErrorWithAlternateErrorReturn("error 3 - a", true);
        if (ret is error) {
            callErrorReturnFunctionFromNonObservableFunction("error 3 - b");

            http:Response outResponse = new;
            outResponse.setTextPayload("Hello, World! from resource three");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error("expected test error not returned");
            panic err;
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-4"
    }
    resource function resourceFour(http:Caller caller, http:Request clientRequest) {
        panicError("error 4");

        http:Response outResponse = new;
        outResponse.setTextPayload("Hello, World! from resource four");
        checkpanic caller->respond(outResponse);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-5"
    }
    resource function resourceFive(http:Caller caller, http:Request clientRequest) {
        error err = error("test panic error: error 5");
        panic err;
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-6"
    }
    resource function resourceSix(http:Caller caller, http:Request clientRequest) {
        var ret = trap panicError("error 6");
        if (ret is error) {
            http:Response outResponse = new;
            outResponse.setTextPayload("Hello, World! from resource six");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error("expected test error not trapped");
            panic err;
        }
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-7"
    }
    resource function resourceSeven(http:Caller caller, http:Request clientRequest) {
        var ret = checkpanic returnErrorWithAlternateErrorReturn("error 7", true);

        http:Response outResponse = new;
        outResponse.setTextPayload("Hello, World! from resource seven");
        checkpanic caller->respond(outResponse);
    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/resource-8"
    }
    resource function resourceEight(http:Caller caller, http:Request clientRequest) {
        var ret = testCheckError("error 8", true);
        if (ret is error) {
            http:Response outResponse = new;
            outResponse.setTextPayload("Hello, World! from resource eight");
            checkpanic caller->respond(outResponse);
        } else {
            error err = error("expected test error not trapped");
            panic err;
        }
    }
}

@observe:Observable
function returnError(string text) returns error {
    error err = error("test return error: " + text);
    return err;
}

@observe:Observable
function returnErrorWithOptionalErrorReturn(string text, boolean isError) returns error? {
    if (isError) {
        error err = error("test return error: " + text);
        return err;
    }
}

@observe:Observable
function returnErrorWithAlternateErrorReturn(string text, boolean isError) returns error|string {
    if (isError) {
        error err = error("test return error: " + text);
        return err;
    } else {
        return "test return value: " + text;
    }
}

function callErrorReturnFunctionFromNonObservableFunction(string text) {
    var ret = returnErrorWithAlternateErrorReturn(text, true);
    if (!(ret is error)) {
        error err = error("expected test error not returned");
        panic err;
    }
}

@observe:Observable
function panicError(string text) {
    error err = error("test panic error: " + text);
    panic err;
}

@observe:Observable
function testCheckError(string text, boolean isError) returns error|string {
    return check returnErrorWithAlternateErrorReturn(text, isError);
}
