// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Most basic types of structured values (along with one basic type of simple value) are
// iterable, meaning that a value of the type can be accessed as a sequence of simpler values.
@test:Config {}
function testIterableTypeArray() {
    int[] iterableArray = [1, 2, 3];
    int count = 0;
    foreach int value in iterableArray {
        count += value;
    }
    test:assertEquals(count, 6, msg = "expected int array to iterate over its members");
}

@test:Config {}
function testIterableTypeTuple() {
    (int, string, boolean) iterableTuple = (100, "test string", true);
    int count = 0;
    foreach int|string|boolean variable in iterableTuple {
        count += 1;
    }
    test:assertEquals(count, 3, msg = "expected tuple to iterate 3 loops");
}

@test:Config {}
function testIterableTypeMap() {
    map<string> iterableMap = { fieldOne: "valueOne", fieldTwo: "valueTwo", fieldThree: "valueThree" };
    string result = "";
    foreach (string, string) (key, value) in iterableMap {
        result += value;
    }
    test:assertEquals(result, "valueOnevalueTwovalueThree", msg = "expected map to iterate over its members");
}

public type IterableRecord record {
    float bazFieldOne;
};

@test:Config {}
function testIterableTypeRecord() {
    IterableRecord iterableRecord = { bazFieldOne: 2.2, bazFieldTwo: true, bazFieldThree: "valueThree" };
    string result = "";
    foreach (string, any) (key, value) in iterableRecord {
        result += string.convert(value);
    }
    test:assertEquals(result, "2.2truevalueThree", msg = "expected record type to iterate over its fields");
}

public type TableConstraint record {|
    int constraintField;
|};

@test:Config {}
function testIterableTypeTable() {
    TableConstraint tableEntry1 = { constraintField: 1 };
    TableConstraint tableEntry2 = { constraintField: 2 };
    TableConstraint tableEntry3 = { constraintField: 3 };
    table<TableConstraint> iterableTable = table{};
    error? err1 = iterableTable.add(tableEntry1);
    error? err2 = iterableTable.add(tableEntry2);
    error? err3 = iterableTable.add(tableEntry3);

    if err1 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    if err2 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    if err3 is error {
        test:assertFail(msg = "failed in adding record to table");
    }
    int count = 0;
    foreach TableConstraint entry in iterableTable {
        count += entry.constraintField;
    }
    test:assertEquals(count, 6, msg = "expected table type to iterate over its entries");
}

@test:Config {}
function testIterableTypeXML() {
    xml bookstore = xml `<bookstore>
                            <book category="cooking">
                                <title lang="en">Title1</title>
                                <author>Giada De Laurentiis</author>
                            </book>
                            <book category="children">
                                <title lang="en">Title2</title>
                                <author>J. K. Rowling</author>
                            </book>
                            <book category="web" cover="paperback">
                                <title lang="en">Title3</title>
                                <author>Erik T. Ray</author>
                            </book>
                        </bookstore>`;
    string result = "";
    foreach var x in bookstore["book"] {
        if x is xml {
            result += x["title"].getTextValue();
        }
    }
    test:assertEquals(result, "Title1Title2Title3", msg = "expected xml to iterate over its elements");
}
