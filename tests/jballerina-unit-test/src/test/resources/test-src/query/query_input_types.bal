// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org)
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

public type MyRecord record {
    int id;
    string name;
};

public function testArrayQuery() {
    int[] arr = [1, 2, 3];
    int[] result = from var item in arr
                   select item;

    assertEquality(result.length(), 3);
    assertEquality(result[0], 1);
    assertEquality(result[1], 2);
    assertEquality(result[2], 3);
}

public function testMapQuery() {
    map<int> m = {"key1": 1, "key2": 2, "key3": 3};
    int[] result = from int i in m
                   select i;

    assertEquality(result.length(), 3);

    boolean containsOne = result[0] == 1;
    boolean containsTwo = result[1] == 2;
    boolean containsThree = result[2] == 3;

    assertTrue(containsOne);
    assertTrue(containsTwo);
    assertTrue(containsThree);
}

public function testRecordQuery() {
    MyRecord[] records = [
        {id: 1, name: "record1"},
        {id: 2, name: "record2"}
    ];
    MyRecord[] result = from var rec in records
                        select rec;

    assertEquality(result.length(), 2);
    assertEquality(result[0].id, 1);
    assertEquality(result[0].name, "record1");
    assertEquality(result[1].id, 2);
    assertEquality(result[1].name, "record2");
}

public function testStringQuery() {
    string str = "Ballerina";
    string[] result = from var ch in str
                      select ch.toString();

    assertEquality(result.length(), 9);
    assertEquality(result[0], "B");
    assertEquality(result[1], "a");
    assertEquality(result[8], "a");
}


function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

function assertTrue(any|error actual) {
    assertEquality(actual, true);
}

function assertFalse(any|error actual) {
    assertEquality(actual, false);
}
