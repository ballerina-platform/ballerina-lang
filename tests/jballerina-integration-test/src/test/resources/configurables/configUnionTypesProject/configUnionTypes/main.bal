// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import configUnionTypes.mod1;
import ballerina/jballerina.java;
import configUnionTypes.mod2;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

configurable configLib:HttpVersion & readonly httpVersion = ?;
configurable mod1:CountryCodes & readonly countryCode = ?;
configurable mod1:CountryCodes[] countryCodes = ?;

type HttpResponse record {|
    configLib:HttpVersion httpVersion;
|};

configurable HttpResponse httpResponse = ?;
configurable mod1:Country country = ?;

public function main() {
    testEnumValues();
    mod2:testEnumValues();
    print("Tests passed");
}

function testEnumValues() {
    test:assertEquals(httpVersion, configLib:HTTP_1_1);
    test:assertEquals(countryCode, mod1:SL);
    test:assertEquals(httpResponse.httpVersion, configLib:HTTP_2);
    test:assertEquals(country.countryCode, mod1:US);
    test:assertEquals(countryCodes[0], mod1:US);
    test:assertEquals(countryCodes[1], mod1:SL);
}

function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
