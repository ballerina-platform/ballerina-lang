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
import configStructuredTypes.mod1;

configurable mod1:Product product = ?;
configurable mod1:ProductArray productArr = ?;
configurable mod1:ProductTable productTable = ?;
configurable mod1:ProductMap productMap = ?;
configurable mod1:ProductMapTable productMapTable = ?;

configurable mod1:Owner owner = ?;
configurable mod1:OwnerArray ownerArr = ?;
configurable mod1:OwnerTable ownerTable = ?;
configurable mod1:OwnerMap ownerMap = ?;
configurable mod1:OwnerMapTable ownerMapTable = ?;

configurable mod1:Member member = ?;
configurable mod1:MemberArray memberArr = ?;
configurable mod1:MemberTable memberTable = ?;
configurable mod1:MemberMap memberMap = ?;
configurable mod1:MemberMapTable memberMapTable = ?;

public function testOpenRecords() {
    test:assertEquals(product.toString(), "{\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(productArr.toString(), "[{\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," + 
    "\"floatVal\":99.77,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]}," + 
    "{\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44,\"mapVal\":{\"e\":\"e\",\"f\":789}," + 
    "\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(productTable.toString(), "[{\"arrVal\":[\"4\",\"6\"],\"intVal\":65,\"stringVal\":\"val\"," + 
    "\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43},\"mapArr\":[{\"i\":\"iii\",\"j\":21}]}," + 
    "{\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\",\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68}," + 
    "\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");
    test:assertEquals(productMap.toString(), "{\"entry1\":{\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(productMapTable.toString(), "{\"entry1\":[{\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    test:assertEquals(owner.toString(), "{\"id\":101,\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(ownerArr.toString(), "[{\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," + 
    "\"floatVal\":99.77,\"id\":102,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]}," + 
    "{\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44,\"id\":103," + 
    "\"mapVal\":{\"e\":\"e\",\"f\":789},\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(ownerTable.toString(), "[{\"id\":104,\"arrVal\":[\"4\",\"6\"],\"intVal\":65," + 
    "\"stringVal\":\"val\",\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43}," + 
    "\"mapArr\":[{\"i\":\"iii\",\"j\":21}]},{\"id\":105,\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\"," + 
    "\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68},\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");
    test:assertEquals(ownerMap.toString(), "{\"entry1\":{\"id\":106,\"arrVal\":[1,2,3],\"intVal\":22," + 
    "\"stringVal\":\"abc\",\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"id\":107," + 
    "\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(ownerMapTable.toString(), "{\"entry1\":[{\"id\":108,\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    test:assertEquals(member.toString(), "{\"id\":101,\"arrVal\":[1,2,3],\"intVal\":22,\"stringVal\":\"abc\"," + 
    "\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123},\"mapArr\":[{\"c\":\"c\",\"d\":456}]}");
    test:assertEquals(memberArr.toString(), "[{\"arrVal\":[4.5,6.7],\"intVal\":11,\"stringVal\":\"def\"," + 
    "\"floatVal\":99.77,\"id\":102,\"mapVal\":{\"c\":\"c\",\"d\":456},\"mapArr\":[{\"a\":\"a\",\"b\":789}]}," + 
    "{\"arrVal\":[8.9,0.1],\"intVal\":33,\"stringVal\":\"ghi\",\"floatVal\":88.44,\"id\":103," + 
    "\"mapVal\":{\"e\":\"e\",\"f\":789},\"mapArr\":[{\"g\":\"g\",\"h\":876}]}]");
    test:assertEquals(memberTable.toString(), "[{\"id\":104,\"arrVal\":[\"4\",\"6\"],\"intVal\":65," + 
    "\"stringVal\":\"val\",\"floatVal\":9.12,\"mapVal\":{\"c\":\"ccc\",\"d\":43}," + 
    "\"mapArr\":[{\"i\":\"iii\",\"j\":21}]},{\"id\":105,\"arrVal\":[10,20],\"intVal\":40,\"stringVal\":\"str\"," + 
    "\"floatVal\":13.57,\"mapVal\":{\"m\":\"mmm\",\"n\":68},\"mapArr\":[{\"y\":\"yyy\",\"x\":24}]}]");
    test:assertEquals(memberMap.toString(), "{\"entry1\":{\"id\":106,\"arrVal\":[1,2,3],\"intVal\":22," + 
    "\"stringVal\":\"abc\",\"floatVal\":22.33,\"mapVal\":{\"a\":\"a\",\"b\":123}},\"entry2\":{\"id\":107," + 
    "\"mapArr\":[{\"c\":\"c\",\"d\":456}]}}");
    test:assertEquals(memberMapTable.toString(), "{\"entry1\":[{\"id\":108,\"arrVal\":[100,200],\"intVal\":100," + 
    "\"stringVal\":\"string\",\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}," + 
    "\"mapArr\":[{\"y\":\"test\",\"x\":204}]}]}");

    testTableIterator(ownerTable);
    testTableIterator(memberTable);

    testMapIterator(productMapTable, 1);
    testMapIterator(ownerMap, 2);
    testMapIterator(ownerMapTable, 1);
    testMapIterator(memberMap, 2);
    testMapIterator(memberMapTable, 1);

    // These lines should be enabled after fixing #30566
    // testTableIterator(productTable);
    // testMapIterator(productMap, 2);
}

public function testTableIterator(table<map<anydata>> tab) {
    int count = 0;
    foreach var entry in tab {
        count += 1;
    }
    test:assertEquals(count, 2);
}

public function testMapIterator(map<anydata> testMap, int length) {
    int count = 0;
    foreach var entry in testMap {
        count += 1;
    }
    test:assertEquals(count, length);
}
