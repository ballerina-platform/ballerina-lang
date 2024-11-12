// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/test;

type OrderItemTargetType typedesc<anydata>;

public client isolated class Client {

    public isolated function init() {

    }

    isolated resource function get abc/[string ...path]() returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get abc/[int id]/[float v]/[string path]/[string ...paths]() returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get def/[string path]/[int path1]/[string path2]/[int ...path3]() returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray"]
    } external;

    isolated resource function get abc/[int id]/[string path](float f, string s) returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray", "double", "io.ballerina.runtime.api.values.BString"]
    } external;

    isolated resource function get abc/[int id]/[string path]/[string p2](string a, float f, string s) returns int = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BString", "double", "io.ballerina.runtime.api.values.BString"]
    } external;

    isolated resource function get def/[int id]/[string path]/[string p2](string s) returns string = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BString"]
    } external;

    isolated resource function get ghi/[int id]/[string p1]/[string p2]/[int ...ids](string s) returns string = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResource",
        paramTypes: ["io.ballerina.runtime.api.Environment", "io.ballerina.runtime.api.values.BObject", "io.ballerina.runtime.api.values.BArray", "io.ballerina.runtime.api.values.BString"]
    } external;

    isolated resource function get orderitem/[string orderId]/[string itemId](OrderItemTargetType targetType = <>) returns targetType = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResourceOne"
    } external;


    isolated resource function get orderitem(OrderItemTargetType targetType = <>) returns targetType = @java:Method {
        'class: "org/ballerinalang/nativeimpl/jvm/tests/StaticMethods",
        name: "getResourceTwo"
    } external;
}

public function testResourceFuncWithMultiplePathParams() {
    Client cl = new;
    var response = cl->/abc/[1]/[2.5]/abc/def/ghi/jkl/mno/pqr/stu/vwx/yz();
    test:assertEquals(response, 4);

    response = cl->/abc/abc/def/ghi/jkl/mno/pqr/stu/vwx/yz();
    test:assertEquals(response, 9);

    response = cl->/def/abc/[1001]/def/[101]/[10002]();
    test:assertEquals(response, 4);

    response = cl->/abc/[1]/["2"](1.2, "a");
    test:assertEquals(response, 2);

    response = cl->/abc/[1]/["2"]/["3"]("a", 1.2, "a");
    test:assertEquals(response, 3);

    string res = cl->/def/[1]/["2"]/["3"]("a");
    test:assertEquals(res, "a");

    res = cl->/ghi/[1]/["aaa"]/["bbb"]/[4]/[5]/[6]/[7]/[8]/[9]("xyz");
    test:assertEquals(res, "xyz");

    res = cl->/orderitem/["1234"]/["abcd"].get();
    test:assertEquals(res, "getResourceOne");

    res = cl->/orderitem.get();
    test:assertEquals(res, "getResourceTwo");
}
