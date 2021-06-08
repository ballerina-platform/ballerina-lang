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

import configStructuredTypes.mod1;
import testOrg/configLib.mod1 as configLib;
import ballerina/test;

final string word1 = "word 1";

configurable string word2 = ?;

type Words record {|
    string word1 = word1;
    string word2 = word2;
    string word3 = getWord();
    string word4 = mod1:getWord();
    string word5 = configLib:getWord();
    string word6;
|};

// Defaultable records
configurable Words words = ?;
configurable mod1:Numbers numbers = ?;
configurable configLib:Symbols symbols = ?;

// Array of defaultable records
configurable Words[] wordArr = ?;
configurable mod1:Numbers[] numberArr = ?;
configurable configLib:Symbols[] symbolArr = ?;

// Defaultable record tables
configurable table<Words> wordTable = ?;
configurable table<mod1:Numbers> numberTable = ?;
configurable table<configLib:Symbols> symbolTable = ?;

// Map of defaultable records
configurable map<Words> wordMap = ?;
configurable map<mod1:Numbers> numberMap = ?;
configurable map<configLib:Symbols> symbolMap = ?;

// Map of table with defaultable records
configurable map<table<Words>> wordTableMap = ?;
configurable map<table<mod1:Numbers>> numberTableMap = ?;
configurable map<table<configLib:Symbols>> symbolTableMap = ?;

public function testRecords() {
    test:assertEquals(words.toString(), "");
    test:assertEquals(wordArr.toString(), "");
    test:assertEquals(wordTable.toString(), "");
    test:assertEquals(wordMap.toString(), "");
    test:assertEquals(wordTableMap.toString(), "");

    test:assertEquals(numbers.toString(), "");
    test:assertEquals(numberArr.toString(), "");
    test:assertEquals(numberTable.toString(), "");
    test:assertEquals(numberMap.toString(), "");
    test:assertEquals(numberTableMap.toString(), "");

    test:assertEquals(symbols.toString(), "");
    test:assertEquals(symbols.toString(), "");
    test:assertEquals(symbolTable.toString(), "");
    test:assertEquals(symbolMap.toString(), "");
    test:assertEquals(symbolTableMap.toString(), "");

    testTableIterator(wordTable);
    testTableIterator(numberTable);
    testTableIterator(symbolTable);

    testMapIterator(wordMap, 2);
    testMapIterator(numberMap, 2);
    testMapIterator(symbolMap, 2);
    testMapIterator(wordTableMap, 2);
    testMapIterator(numberTableMap, 2);
    testMapIterator(symbolTableMap, 2);
}

isolated function getWord() returns string {
    return "word 3";
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
