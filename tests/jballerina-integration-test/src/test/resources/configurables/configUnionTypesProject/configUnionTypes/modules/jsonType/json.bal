// Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import ballerina/test;

configurable json jsonVar1 = ?;
configurable json jsonVar2 = ?;
configurable json jsonVar3 = ?;
configurable json jsonVar4 = ?;
configurable json jsonVar5 = ?;
configurable json jsonVar6 = ?;

type Response record {|
    json payload;
    int code;
|};

configurable json[] jsonArr = ?;
configurable string|json jsonUnion = ?;
configurable map<json> jsonMap = ?;
configurable Response jsonRecord = ?;
configurable table<map<json>> jsonTable1 = ?;
configurable table<Response> jsonTable2 = ?;

public function testJsonValues() {
    test:assertEquals(jsonVar1.toString(), "json String");
    test:assertEquals(jsonVar2.toString(), "[1,2]");
    test:assertEquals(jsonVar3.toString(), "[\"hello\",1,true]");
    test:assertEquals(jsonVar4.toString(), "{\"a\":\"aaa\",\"b\":2}");
    test:assertEquals(jsonVar5.toString(), "[{\"a\":\"aaa\",\"b\":2},{\"c\":\"ccc\",\"d\":4}]");
    test:assertEquals(jsonVar6.toString(), "[[1,2],[4,5]]");

    test:assertEquals(jsonArr.toString(), "[1,\"hello\",[1,2],{\"name\":\"Hindu\",\"age\":26}," +
    "[{\"one\":1},{\"two\":2}]]");
    test:assertEquals(jsonUnion.toString(), "22");
    test:assertEquals(jsonMap.toString(), "{\"string\":\"string\",\"num\":12,\"arr\":[1,2,\"hello\"]," +
    "\"map\":{\"a\":\"a\"}}");
    test:assertEquals(jsonRecord.toString(), "{\"payload\":{\"msg\":\"This is message\"},\"code\":200}");
    test:assertEquals(jsonTable1.toString(), "[{\"val1\":[\"hello\",1,true]},{\"val2\":\"json String\"}]");
    test:assertEquals(jsonTable2.toString(), "[{\"payload\":{\"msg\":\"This is message\"},\"code\":200}," +
    "{\"payload\":{\"error\":\"Not found\"},\"code\":400}]");
}
