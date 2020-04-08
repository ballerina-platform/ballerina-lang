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

import ballerina/nats;
import ballerina/io;

nats:Connection conn = new;

listener nats:StreamingListener lis = new(conn);

@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service testService on lis {
     resource function onMessage(nats:StreamingMessage message, int[] data) {
        io:println("Received Message");
    }

    resource function onError(nats:StreamingMessage message, error errorVal) {
        io:println("Error occurred!");
    }
}

