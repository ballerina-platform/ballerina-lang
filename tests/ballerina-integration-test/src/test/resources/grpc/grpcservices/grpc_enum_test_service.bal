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
import ballerina/grpc;

endpoint grpc:Listener listener {
    host:"localhost",
    port:8555
};

map<orderInfo> ordersMap;

type orderInfo record {
    string id;
    Mode mode;
};

public type Mode "r";
@final public Mode READ = "r";

@grpc:ServiceConfig
service testEnumService bind listener {
    testEnum(endpoint caller, orderInfo orderReq) {
        string permission;
        match orderReq.mode {
            READ => permission = "r";
        }
        _ = caller->send(permission);
        _ = caller->complete();
    }
}
