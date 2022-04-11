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

import ballerina/test;
import configOpenRecord.type_defs;
import configOpenRecord.rest_fields;
import testOrg/configLib.util;

configurable type_defs:Product product = ?;
configurable type_defs:Owner owner = ?;
configurable type_defs:Member member = ?;
configurable readonly & record {int id;} anonRecord = ?;

function testOpenRecords() {
    test:assertEquals(product.toString(), "{\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(owner.toString(), "{\"id\":101,\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(member.toString(), "{\"id\":101,\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(anonRecord.toString(), "{\"id\":123,\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
}

public function main() {
    testOpenRecords();
    testRecordIteration();

    testMaps();
    testMapIteration();

    testRecordCollection();
    testArrayIteration();
    testTableIteration();

    rest_fields:testRestFields();
    util:print("Tests passed");
}

function testRecordIteration() {
    util:testRecordIterator(product, 6);
    util:testRecordIterator(owner, 7);
    util:testRecordIterator(member, 7);
    util:testRecordIterator(anonRecord, 7);  
}
