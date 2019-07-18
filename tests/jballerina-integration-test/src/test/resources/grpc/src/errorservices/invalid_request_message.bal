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

listener grpc:Listener server1 = new (9090, {
    host:"localhost"
});

service HelloWorld on server1 {
    resource function invalidReqType(grpc:Caller caller, string? name) {
        string input = name ?: "";
        string message = "Hello " + input;
        checkpanic caller->send(message);
        checkpanic caller->complete();
    }
}
