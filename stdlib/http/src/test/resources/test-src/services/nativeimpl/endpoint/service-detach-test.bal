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

listener http:MockListener backendEP = new(9090);

@http:ServiceConfig {
    basePath: "/mock1"
}
service mock1 on backendEP {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock2Resource(http:Caller caller, http:Request req) {
        checkpanic backendEP.__attach(mock2);
        checkpanic backendEP.__attach(mock3);
        var responseToCaller = caller->respond("Mock1 invoked. Mock2 attached. Mock3 attached");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
}

service mock2 =
@http:ServiceConfig {
    basePath: "/mock2"
}
service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock2Resource(http:Caller caller, http:Request req) {
        checkpanic backendEP.__detach(mock3);
        checkpanic backendEP.__attach(mock3);
        var responseToCaller = caller->respond("Mock2 resource was invoked");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};

service mock3 =
@http:ServiceConfig {
    basePath: "/mock3"
}
service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock3Resource(http:Caller caller, http:Request req) {
        checkpanic backendEP.__detach(mock2);
        checkpanic backendEP.__attach(mock4);
        var responseToCaller = caller->respond("Mock3 invoked. Mock2 detached. Mock4 attached");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};

service mock4 =
@http:ServiceConfig {
    basePath: "/mock4"
}
service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock4Resource(http:Caller caller, http:Request req) {
        checkpanic backendEP.__attach(mock2);
        checkpanic backendEP.__detach(mock5);
        var responseToCaller = caller->respond("Mock4 invoked. Mock2 attached");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};

service mock5 =
@http:ServiceConfig {
    basePath: "/mock5"
}
service {
    @http:ResourceConfig {
        path: "/"
    }
    resource function mock5Resource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock5 invoked");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", err = responseToCaller);
        }
    }
};
