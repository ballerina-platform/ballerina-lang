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
import testOrg/configLib.util;

configurable type_defs:ProductMap productMap = ?;
configurable type_defs:ProductMapTable productMapTable = ?;

configurable type_defs:OwnerMap ownerMap = ?;
configurable type_defs:OwnerMapTable ownerMapTable = ?;

configurable type_defs:MemberMap memberMap = ?;
configurable type_defs:MemberMapTable memberMapTable = ?;

configurable readonly & map<record {int id;}> anonRecordMap = ?;
configurable map<table<record {readonly int id;}> key(id)> anonRecordMapTable = ?;

function testMaps() {
    test:assertEquals(productMap.toString(), "{\"entry1\":{\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(productMapTable.toString(), "{\"entry1\":[{\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    test:assertEquals(ownerMap.toString(), "{\"entry1\":{\"id\":106,\"arrVal\":[1,2,3],\"intVal\":22," + 
    "\"stringVal\":\"abc\",\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"id\":107," + 
    "\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(ownerMapTable.toString(), "{\"entry1\":[{\"id\":108,\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    test:assertEquals(memberMap.toString(), "{\"entry1\":{\"id\":106,\"arrVal\":[1,2,3],\"intVal\":22," + 
    "\"stringVal\":\"abc\",\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"id\":107," + 
    "\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(memberMapTable.toString(), "{\"entry1\":[{\"id\":108,\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    test:assertEquals(anonRecordMap.toString(), "{\"entry1\":{\"id\":124,\"arrVal\":[1,2,3],\"intVal\":22," +
    "\"stringVal\":\"abc\",\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"id\":125," +
    "\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(anonRecordMapTable.toString(), "{\"entry1\":[{\"id\":126,\"arrVal\":[100,200],\"intVal\":100," +
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," +
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");
}

function testMapIteration() {
    util:testMapIterator(productMapTable, 1);
    util:testMapIterator(ownerMap, 2);
    util:testMapIterator(ownerMapTable, 1);
    util:testMapIterator(memberMap, 2);
    util:testMapIterator(memberMapTable, 1);
    util:testMapIterator(anonRecordMap, 2);
    util:testMapIterator(anonRecordMapTable, 1);
    util:testMapIterator(productMap, 2);
}
