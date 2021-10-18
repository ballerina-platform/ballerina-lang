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

import configOpenRecord.type_defs;
import testOrg/configLib.util;
import ballerina/test;

configurable type_defs:ProductArray productArr = ?;
configurable type_defs:ProductTable productTable = ?;

configurable type_defs:OwnerArray ownerArr = ?;
configurable type_defs:OwnerTable ownerTable = ?;

configurable type_defs:MemberArray memberArr = ?;
configurable type_defs:MemberTable memberTable = ?;

configurable readonly & record {int id;}[] anonRecordArr = ?;
configurable readonly & table<record {readonly int id;}> key(id) anonRecordTable = ?;

function testRecordCollection() {
    test:assertEquals(productArr.toString(), "[{\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," + 
    "\"floatVal\":99.77,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]}," + 
    "{\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44,\"mapVal\":{\"e\":\"e\",\"f\":789}," + 
    "\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(productTable.toString(), "[{\"arrVal\":[\"4\",\"6\"],\"intVal\":65,\"stringVal\":\"val\"," + 
    "\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43},\"mapArr\":[{\"i\":\"iii\",\"j\":21}]}," + 
    "{\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\",\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68}," + 
    "\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");

    test:assertEquals(ownerArr.toString(), "[{\"id\":102,\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," +
    "\"floatVal\":99.77,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]},{\"id\":103," +
    "\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44,\"mapVal\":{\"e\":\"e\"," +
    "\"f\":789},\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(ownerTable.toString(), "[{\"id\":104,\"arrVal\":[\"4\",\"6\"],\"intVal\":65," + 
    "\"stringVal\":\"val\",\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43}," + 
    "\"mapArr\":[{\"i\":\"iii\",\"j\":21}]},{\"id\":105,\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\"," + 
    "\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68},\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");

    test:assertEquals(memberArr.toString(), "[{\"id\":102,\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," +
    "\"floatVal\":99.77,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]}," +
    "{\"id\":103,\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44," +
    "\"mapVal\":{\"e\":\"e\",\"f\":789},\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(memberTable.toString(), "[{\"id\":104,\"arrVal\":[\"4\",\"6\"],\"intVal\":65," + 
    "\"stringVal\":\"val\",\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43}," + 
    "\"mapArr\":[{\"i\":\"iii\",\"j\":21}]},{\"id\":105,\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\"," + 
    "\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68},\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");

    test:assertEquals(anonRecordArr.toString(), "[{\"id\":127,\"arrVal\":[4.5,6.7],\"intVal\":11," +
    "\"stringVal\":\"def\",\"floatVal\":99.77,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\"," +
    "\"b\":789}]},{\"id\":128,\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44," +
    "\"mapVal\":{\"e\":\"e\",\"f\":789},\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(anonRecordTable.toString(), "[{\"id\":129,\"arrVal\":[\"4\",\"6\"],\"intVal\":65," + 
    "\"stringVal\":\"val\",\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43}," + 
    "\"mapArr\":[{\"i\":\"iii\",\"j\":21}]},{\"id\":130,\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\"," + 
    "\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68},\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");
}

function testArrayIteration() {
    util:testArrayIterator(ownerArr, 2);
    util:testArrayIterator(memberArr, 2);
    util:testArrayIterator(productArr, 2);
    util:testArrayIterator(anonRecordArr, 2);
}

function testTableIteration() {
    util:testTableIterator(ownerTable);
    util:testTableIterator(memberTable);
    util:testTableIterator(anonRecordTable);
    util:testTableIterator(productTable);
}
