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

public type Foo record {
    string code?;
    string message?;
};

public type Bar record {
    string processId?;
    Qux[] reasons?;
    boolean success?;
};

public type Baz record {
    *Bar;
    string id?;
    Foo[] reasons?;
};

public type Qux record {
    string code?;
    string message?;
};

public type Bdd Node|boolean;

public type Node readonly & record {|
    *R;
    int atom;
    Bdd left;
    Bdd middle;
    Bdd right;
|};

type R record {
};

function testRecordTypeResolving() {
    Baz & readonly baz = {reasons: [{code: "22", message: "message-22"}]};
    var reasons = baz?.reasons;
    if (reasons is Foo[]) {
        Foo foo = reasons[0];
        assertEquality("22", foo?.code);
        assertEquality("message-22", foo?.message);
    } else {
        panic error("Assertion error: expected Foo[], found: " + (typeof reasons).toString());
    }
}

function testRecordTypeResolvingWithTypeInclusion() {
    Node node = {
        atom: 0,
        left: false,
        middle: false,
        right: {atom: 1, left: false, middle: false, right: false}
    };

    var right = node.right;
    if (right is Node) {
        assertEquality(1, right.atom);
    } else {
        panic error("Expected Node, found: " + (typeof right).toString());
    }
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("Assertion error: expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
