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
import ballerina/utils;

// A table is iterable as a sequence of mapping values, one for each row, where each mapping
// value has a field for each column, with the column as the field name and the value of that
// column in that row as the field value. The mapping values will belong to a closed record
// type.
@test:Config {}
function testTableIterable() {
    BazRecordFourteen bazRecord1 = { bazFieldOne: 1 };
    BazRecordFourteen bazRecord2 = { bazFieldOne: 2 };
    BazRecordFourteen bazRecord3 = { bazFieldOne: 3 };
    table<BazRecordFourteen> iterableTable = table{};
    _ = iterableTable.add(bazRecord1);
    _ = iterableTable.add(bazRecord2);
    _ = iterableTable.add(bazRecord3);

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
public type Foo record {
    int id;
    string name;
    !...;
};

public type Bar record {
    int id;
    string name;
    !...;
};

public type Baz record {
    int id;
    boolean name;
    !...;
};

public type Qux record {
    int id;
    string age;
    !...;
};

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
    float bazFieldOne;
};
