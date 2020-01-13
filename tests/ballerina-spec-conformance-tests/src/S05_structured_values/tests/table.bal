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

// A table is iterable as a sequence of mapping values, one for each row, where each mapping
// value has a field for each column, with the column as the field name and the value of that
// column in that row as the field value. The mapping values will belong to a closed record
// type.
@test:Config {}
function testTableIterable() {
    BazRecordFourteen bazRecord1 = { bazFieldOne: 1, bazFieldTwo: 4 };
    BazRecordFourteen bazRecord2 = { bazFieldOne: 2, bazFieldTwo: 5 };
    BazRecordFourteen bazRecord3 = { bazFieldOne: 3, bazFieldTwo: 6 };
    table<BazRecordFourteen> iterableTable = table{};
    error? err1 = iterableTable.add(bazRecord1);
    error? err2 = iterableTable.add(bazRecord2);
    error? err3 = iterableTable.add(bazRecord3);
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
    foreach var bazRecord in iterableTable {
        // TODO: Clarify: should bazRecord be a closed record?
        count += 1;
    }
    test:assertEquals(count, 3, msg = "expected table type to iterate over its entries");
}


// Note that a table type T’ will be a subtype of a table type T if and only if:
// ● T and T’ have the same set of column names;
// ● T and T’ have the same set of primary keys; and
// ● for each column, the type for that column in T’ is a subtype of the type of that column
// in T.
public type Foo record {|
    int id;
    string name;
|};

public type Bar record {|
    int id;
    string name;
|};

public type Baz record {|
    int id;
    boolean name;
|};

public type Qux record {|
    int id;
    string age;
|};

@test:Config {}
function testTableSubtype() {
    table<Foo> t1 = table {
        {key id, name}
    };

    any a = t1;

    table<Bar> t2 = table {
        {key id, name}
    };

    any b = t2;

    table<Baz> t3 = table {
        {key id, name}
    };

    any c = t3;

    table<Qux> t4 = table {
        {key id, age}
    };

    any d = t4;

    test:assertTrue(a is table<Bar>);
    test:assertTrue(b is table<Foo>);

    test:assertFalse(c is table<Foo>);
    test:assertFalse(d is table<Foo>);
}

public type BazRecordFourteen record {
    int bazFieldOne;
    int bazFieldTwo;
};

// Otherwise the value for all primary keys together must uniquely identify each row in the table;
// in other words, a table cannot have two rows where for every column marked as a primary key,
// that value of that column in both rows is the same.
@test:Config {}
function testDuplicateKeyValue() {
    BazRecordFourteen bazRecord1 = { bazFieldOne: 1, bazFieldTwo: 2 };
    BazRecordFourteen bazRecord2 = { bazFieldOne: 1, bazFieldTwo: 3 };

    table<BazRecordFourteen> iterableTable = table{
        {key bazFieldOne, bazFieldTwo}
    };
    var resultOne = iterableTable.add(bazRecord1);
    var resultTwo = iterableTable.add(bazRecord2);

    if (resultOne is error) {
        test:assertFail(msg = "expected table row to be added with no error");
    }

    if (resultTwo is error) {
        test:assertEquals(resultTwo.reason(), "{ballerina}TableOperationError",
            msg = "invalid reason on adding duplicate key");
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}

// A table value also contains a boolean flag for each column name saying
// whether that column is a primary key for the table;
@test:Config {}
function testMultipleKeyTableDescriptor() {
    BazRecordFourteen bazRecord1 = { bazFieldOne: 1, bazFieldTwo: 2 };
    BazRecordFourteen bazRecord2 = { bazFieldOne: 1, bazFieldTwo: 2 };

    table<BazRecordFourteen> iterableTable = table{
        {key bazFieldOne, key bazFieldTwo}
    };
    var resultOne = iterableTable.add(bazRecord1);
    var resultTwo = iterableTable.add(bazRecord2);

    if (resultOne is error) {
        test:assertFail(msg = "expected table row to be added with no error");
    }

    if (resultTwo is error) {
        test:assertEquals(resultTwo.reason(), "{ballerina}TableOperationError",
            msg = "invalid reason on adding duplicate key");
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}
