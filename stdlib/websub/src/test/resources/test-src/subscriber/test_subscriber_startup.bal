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

import ballerina/websub;

function startSubscriberService() returns string {
    websub:Listener l1 = new(8387);
    websub:Listener l2 = new(8387);

    var l1Error = l1.__start();
    if (l1Error is error) {
        return l1Error.detail()?.message ?: "l1 error unavailable";
    }
    var l2Error = l2.__start();
    if (l2Error is error) {
        return <string>l2Error.detail()?.message;
    }
    return "no error";
}
