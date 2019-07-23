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
import ballerina/websub;

listener http:Listener publisherServiceEPTwo = new http:Listener(23081);

service original on publisherServiceEPTwo {
    resource function one(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_MOVED_PERMANENTLY_301, ["http://localhost:23081/redirected/one"]);
    }

    resource function two(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_FOUND_302, ["http://localhost:23081/redirected/two"]);
    }
}

service redirected on publisherServiceEPTwo {
    resource function one(http:Caller caller, http:Request req) {
        http:Response res = new;
        websub:addWebSubLinkHeader(res, ["http://localhost:23081/hub/one"], WEBSUB_TOPIC_FIVE);
        var err = caller->respond(res);
        if (err is error) {
            log:printError("Error sending response", err);
        }
    }

    resource function two(http:Caller caller, http:Request req) {
        http:Response res = new;
        websub:addWebSubLinkHeader(res, ["http://localhost:23081/hub/two"], WEBSUB_TOPIC_SIX);
        var err = caller->respond(res);
        if (err is error) {
            log:printError("Error sending response", err);
        }
    }
}

service hub on publisherServiceEPTwo {
    resource function one(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307, ["http://localhost:23191/websub/hub"]);
    }

    resource function two(http:Caller caller, http:Request req) {
        http:Response res = new;
        checkpanic caller->redirect(res, http:REDIRECT_PERMANENT_REDIRECT_308, ["http://localhost:23191/websub/hub"]);
    }
}
