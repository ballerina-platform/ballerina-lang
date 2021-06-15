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

function testRecords() {
    test:assertEquals(words.toString(), "{\"word1\":\"word 1\",\"word2\":\"word 2\",\"word3\":\"word 3\"," + 
    "\"word4\":\"word 4\",\"word5\":\"word 5\",\"word6\":\"word 6\"}");
    test:assertEquals(wordArr.toString(), "[{\"word1\":\"word 1\",\"word2\":\"word 2\",\"word3\":\"word 3\"," + 
    "\"word4\":\"word 4\",\"word5\":\"word 5\",\"word6\":\"word 7\"},{\"word1\":\"word 10\",\"word2\":\"word 20\"," + 
    "\"word3\":\"word 30\",\"word4\":\"word 40\",\"word5\":\"word 50\",\"word6\":\"word 60\"}]");
    test:assertEquals(wordTable.toString(), "[{\"word1\":\"word 1\",\"word2\":\"word 2\",\"word3\":\"word 3\"," + 
    "\"word4\":\"word 4\",\"word5\":\"word 5\",\"word6\":\"word 7\"},{\"word1\":\"word 10\",\"word2\":\"word 20\"," + 
    "\"word3\":\"word 30\",\"word4\":\"word 40\",\"word5\":\"word 50\",\"word6\":\"word 60\"}]");
    test:assertEquals(wordMap.toString(), "{\"entry1\":{\"word1\":\"word 1\",\"word2\":\"word 2\"," + 
    "\"word3\":\"word 3\",\"word4\":\"word 4\",\"word5\":\"word 5\",\"word6\":\"word 6\"}," + 
    "\"entry2\":{\"word1\":\"word 11\",\"word2\":\"word 22\",\"word3\":\"word 33\",\"word4\":\"word 44\"," + 
    "\"word5\":\"word 55\",\"word6\":\"word 66\"}}");
    test:assertEquals(wordTableMap.toString(), "{\"map1\":[{\"word1\":\"word 1\",\"word2\":\"word 2\"," + 
    "\"word3\":\"word 3\",\"word4\":\"word 4\",\"word5\":\"word 5\",\"word6\":\"word 7\"}]," + 
    "\"map2\":[{\"word1\":\"word 10\",\"word2\":\"word 20\",\"word3\":\"word 30\",\"word4\":\"word 40\"," + 
    "\"word5\":\"word 50\",\"word6\":\"word 60\"}]}");

    test:assertEquals(numbers.toString(), "{\"num1\":1,\"num2\":2,\"num3\":3,\"num4\":4,\"num5\":5}");
    test:assertEquals(numberArr.toString(), "[{\"num1\":1,\"num2\":2,\"num3\":3,\"num4\":4,\"num5\":6}," + 
    "{\"num1\":10,\"num2\":20,\"num3\":30,\"num4\":40,\"num5\":50}]");
    test:assertEquals(numberTable.toString(), "[{\"num1\":1,\"num2\":2,\"num3\":3,\"num4\":4,\"num5\":6}," + 
    "{\"num1\":10,\"num2\":20,\"num3\":30,\"num4\":40,\"num5\":50}]");
    test:assertEquals(numberMap.toString(), "{\"entry1\":{\"num1\":1,\"num2\":2,\"num3\":3,\"num4\":4," + 
    "\"num5\":55},\"entry2\":{\"num1\":11,\"num2\":22,\"num3\":33,\"num4\":44,\"num5\":55}}");
    test:assertEquals(numberTableMap.toString(), "{\"map1\":[{\"num1\":1,\"num2\":2,\"num3\":3,\"num4\":4," + 
    "\"num5\":6}],\"map2\":[{\"num1\":10,\"num2\":20,\"num3\":30,\"num4\":40,\"num5\":50}]}");

    test:assertEquals(symbols.toString(), "{\"symbol1\":\"!\",\"symbol2\":\"@\",\"symbol3\":\"#\",\"symbol4\":\"$\"}");
    test:assertEquals(symbolArr.toString(), "[{\"symbol1\":\"!\",\"symbol2\":\"@\",\"symbol3\":\"#\"," + 
    "\"symbol4\":\"%\"},{\"symbol1\":\"^\",\"symbol2\":\"&\",\"symbol3\":\"*\",\"symbol4\":\"-\"}]");
    test:assertEquals(symbolTable.toString(), "[{\"symbol1\":\"!\",\"symbol2\":\"@\",\"symbol3\":\"#\"," + 
    "\"symbol4\":\"%\"},{\"symbol1\":\"^\",\"symbol2\":\"&\",\"symbol3\":\"*\",\"symbol4\":\"-\"}]");
    test:assertEquals(symbolMap.toString(), "{\"entry1\":{\"symbol1\":\"!\",\"symbol2\":\"@\",\"symbol3\":\"#\"," + 
    "\"symbol4\":\"=\"},\"entry2\":{\"symbol1\":\"+\",\"symbol2\":\"|\",\"symbol3\":\"}\",\"symbol4\":\"?\"}}");
    test:assertEquals(symbolTableMap.toString(), "{\"map1\":[{\"symbol1\":\"!\",\"symbol2\":\"@\",\"symbol3\":\"#\"," + 
    "\"symbol4\":\"%\"}],\"map2\":[{\"symbol1\":\"^\",\"symbol2\":\"&\",\"symbol3\":\"*\",\"symbol4\":\"-\"}]}");

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

public function testRecordsReferringConfigVariables() {
    testRecords();
    test:assertTrue(configLib:getEnabled());
    test:assertEquals(configLib:getMetrics().toString(), "{\"enabled\":false,\"reporter\":\"choreo\"}");
    test:assertEquals(configLib:getTracing().toString(), "{\"enabled\":true,\"provider\":\"wso2\"}");
}
