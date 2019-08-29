// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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



type ClosedFoo record {|
    string s;
    int i;
|};

type OpenedFoo record {
    string s;
    int i;
};

function testRecordInvalidPattern() returns string {

    ClosedFoo foo = {s: "S", i: 23};
    match foo {
        var {s, i: integer, f: [a, b, c]} => {return "A";} // invalid record binding pattern
        var {s, i: integer, f} => {return "A";} // invalid record binding pattern
        var {s, i: integer} => {return "A";}
        var {s} => {return "A";}
        var {a} => {return "A";} // invalid record binding pattern;
        "12" => {return "A";} // pattern will not be matched
        var [s, i] => {return "A";} // invalid tuple variable;
    }

    OpenedFoo foo1 = {s: "S", i: 23};
    match foo1 {
        var {s, i: integer, f: [a, b, c]} => {return "A";}
        var {s, i: integer, f} => {return "A";}
        var {s, i: integer} => {return "A";}
        var {s} => {return "A";}
        var {a} => {return "A";}
        "12" => {return "A";} // pattern will not be matched
        var [s, i] => {return "A";} // invalid tuple variable;
    }

    return "Default";
}

function testTupleInvalidPattern() returns string {
    ClosedFoo foo = {s: "S", i: 23};
    [string, int, ClosedFoo] t = ["A", 12, foo];
    match t {
        var [a, b] => {return "A";} // invalid tuple binding pattern;
        var {s, i} => {return "A";} // invalid record binding pattern
        "12" => {return "A";} // pattern will not be matched
        var [a, b, [d, e]] => {return "A";} // invalid tuple variable;
        var [a, b, {s}] => {return "A";}
        var [a, b, {s, i, f}] => {return "A";} // invalid record binding pattern;
        var [a, b, {s, i}] => {return "A";} // unreachable
        var [a, b, c] => {return "A";}
    }
    return "Default";
}

function singleMatchStmt() returns string {
    any k = 1;
    match k {
        var x => {return "A";} // pattern will always be matched
    }
}
