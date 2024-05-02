// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import ballerina/jballerina.java;

public class ABC {
    public int port;

    public isolated function init(int port) {
        self.port = port;
    }
}

public class XYZ {
    function getMethodWithReceiverType(string[] arr) returns any = @java:Method {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "acceptingBObjectAndReturnField",
        paramTypes: ["io.ballerina.runtime.api.values.BArray"]
    } external;

    function getMethodWithNonReceiverBObjectTypeParameter(ABC abc, string[] arr) returns any = @java:Method {
        'class:"org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "acceptingBObjectAndReturnField",
        paramTypes: ["io.ballerina.runtime.api.values.BArray"]
    } external;
}
