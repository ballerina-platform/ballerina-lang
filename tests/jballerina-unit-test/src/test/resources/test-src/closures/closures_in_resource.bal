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

import ballerina/jballerina.java;

public type TLift record {|
    readonly string id;
    string name;
    string status;
    int capacity;
    boolean night;
    int elevationgain;
    TTrail trailAccess;
|};

public type TTrail record {|
    string id;
    string name;
    string status;
    string difficulty;
    boolean groomed;
    boolean trees;
    boolean night;
|};

service class TestService {

    resource function get closureTest1(string status) returns string {
        var addFunc = function (int funcInt) returns string {
            return status;
        };
        return addFunc(1);
    }

    resource function get closureTest2(string status) returns TLift[] {
        table<TLift> key(id) liftTable = table [
            {
                id: "l1",
                name: "Lift1",
                status: "OPEN",
                capacity: 10,
                night: false,
                elevationgain: 20,
                trailAccess: {
                    id: "t1",
                    name: "tail1",
                    status: "OPEN",
                    difficulty: "HARD",
                    groomed: true,
                    trees: false,
                    night: false
                }
            }
        ];
        TLift[] tLifts = from var l in liftTable where l.status == status select l;
        return tLifts;
    }

}

public function testClosureWithinResource() {
    TestService t = new;
    string str = <string> (checkpanic (wait callMethodWithParams(t, "$get$closureTest1", ["foobar"])));
    TLift[] tLifts = <TLift[]> (checkpanic (wait callMethodWithParams(t, "$get$closureTest2", ["OPEN"])));
    assertEquality(str, "foobar");
    assertEquality(tLifts.length(), 1);
    assertEquality(tLifts[0]["name"], "Lift1");
}

public function callMethodWithParams(service object {} s, string name, (any|error)[] ar)
    returns future<any|error>  = @java:Method {
        'class:"org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
        name:"callMethodWithParams"
    } external;

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("AssertionError",
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
