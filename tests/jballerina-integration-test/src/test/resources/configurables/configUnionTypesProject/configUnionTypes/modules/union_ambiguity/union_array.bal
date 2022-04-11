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

import configUnionTypes.type_defs;
import ballerina/test;
import testOrg/configLib.mod1 as configLib;

configurable configLib:HttpVersion[] versions = ?;
configurable type_defs:CountryCodes[] countryCodes = ?;

type HttpResponse record {|
    configLib:HttpVersion httpVersion;
|};

type Person record {
    string name;
    int age?;
};

configurable HttpResponse[] responses = ?;
configurable type_defs:Country[] countries = ?;

configurable (int|Person)[] intPersonArr = [2];
configurable (map<anydata>|string)[] mapUnionArr = ?;

configurable (configLib:One|configLib:Two|configLib:Three)[] numbers = ?;
configurable configLib:GrantConfig[] configs = ?;

public function test_union_type_arrays() {
    test:assertEquals(versions.toString(), "[\"HTTP_1_1\",\"HTTP_1_1\",\"HTTP_2\"]");
    test:assertEquals(countryCodes.toString(), "[\"United States\",\"Sri Lanka\"]");
    test:assertEquals(responses.toString(), "[{\"httpVersion\":\"HTTP_1_1\"},{\"httpVersion\":\"HTTP_2\"}]");
    test:assertEquals(countries.toString(), "[{\"countryCode\":\"United States\"},{\"countryCode\":\"Sri Lanka\"}]");
    test:assertEquals(intPersonArr.toString(), "[{\"name\":\"Manu\",\"age\":12},{\"name\":\"Nadeeshan\"}]");
    test:assertEquals(mapUnionArr.toString(), "[{\"a\":\"Hello World!\",\"b\":34,\"c\":true},{\"a\":\"Hello Ballerina!\"" +
    ",\"b\":56,\"c\":false}]");
    test:assertEquals(numbers.toString(), "[1,2.5,\"three\"]");
    test:assertEquals(configs.toString(), "[{\"clientId\":123456,\"clientSecret\":\"hello\",\"clientConfig\":" +
    "{\"httpVersion\":\"HTTP_1_1\",\"customHeaders\":{\"header1\":\"header1\",\"header2\":\"header2\"}}}," +
    "{\"token\":\"123456\",\"timeLimit\":12.5,\"clientConfig\":{\"httpVersion\":\"HTTP_2\",\"customHeaders\":" +
    "{\"header3\":\"header3\",\"header4\":\"header4\"}}},{\"password\":[\"1\",2,3]}]");
}
