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

function simpleTypes() returns string {
    int x1 = 10;
    match x1 {
        20 => {return "20";}
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";}
        [20, 21] => {return "(20, 21)";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    boolean x2 = true;
    match x2 {
        20 => {return "20";} // pattern will not be matched
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";}
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    float x3 = 10.1;
    match x3 {
        20 => {return "20";} // pattern will not be matched
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";}
    }

    string x4 = "Ballerina";
    match x4 {
        20 => {return "20"; } // pattern will not be matched
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";}
        10.4 => {return "10.4";} // pattern will not be matched
    }

    byte x5 = 200;
    match x5 {
        20 => {return "20";}
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";}
        [20, 21] => {return "[20, 21]";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    anydata x6 = 15;
    match x6 {
        20 => {return "20";}
        {a: 20} => {return "{a: 20}";}
        false => {return "false";}
        21 => {return "21";}
        "Ballerina" => {return "Ballerina";}
        10.4 => {return "10.4";}
    }

    return "Fail";
}

type Rec1 record {|
    int | float a;
|};

type Rec2 record {
    int | float a;
};

type Rec3 record {
    int a;
};

type Rec4 record {
    int | float | boolean a;
};

type Rec5 record {|
    int | float | boolean a;
|};


function recordTypes() returns string {
    Rec1 r1 = {a: 200};
    match r1 {
        20 => {return "20"; }// pattern will not be matched
        {a: 20.2} => {return "{a: 20.2}";}
        {a: 20, b: 10} => {return "{a: 20.2}";} // pattern will not be matched
        {a: 20} => {return "{a: 20}";}
        false => {return "false";} // pattern will not be matched
        [20, 21] => {return "[20, 21]";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    Rec2 r2 = {a: 200};
    match r2 {
        {a: 20.2} => {return "{a: 20.2}";}
        {a: 20, b: 10} => {return "{a: 20, b: 10}";}
        {a: 20} => {return "{a: 20}";}
        {a: false, b: 10} => {return "{a: false, b: 10}";} // pattern will not be matched
    }

    Rec3 r3 = {a: 200};
    match r3 {
        {a: 20} => {return "{a: 20}";}
        {a: 20.2} => {return "{a: 20.2}";} // pattern will not be matched
        {a: 21, b: 10} => {return "{a: 21, b: 10}";}
        {a: false, b: 10} => {return "{a: false, b: 10}";} // pattern will not be matched
    }

    Rec4 r4 = {a: true};
    match r4 {
        {a: 20} => {return "{a: 20}";}
        {a: 20.2} => {return "{a: 20.2}";}
        {a: false, b: 10} => {return "{a: false, b: 10}";}
        {a: true} => {return "{a: 20, b: 10}";}
        {a: "r1", b: 10} => {return "{a: r1, b: 10}";} // pattern will not be matched
    }

    Rec1|Rec4 r5 = r1;
    match r5 {
        {a: 20} => {return "{a: 20}";}
        {a: 20.2, b: true} => {return "{a: 20.2}";}
        {a: false, b: 10} => {return "{a: false, b: 10}";}
        {a: true} => {return "{a: 20, b: 10}";}
        {a: "r1", b: 10} => {return "{a: r1, b: 10}";} // pattern will not be matched
    }

    Rec1|Rec5 r6 = r1;
    match r6 {
        {a: 20} => {return "{a: 20}";}
        {a: 20.2} => {return "{a: 20.2}";}
        {a: false, b: 10} => {return "{a: false, b: 10}";} // pattern will not be matched
        {a: true} => {return "{a: 20, b: 10}";}
        {a: "r1", b: 10} => {return "{a: r1, b: 10}";} // pattern will not be matched
    }
    return "Fail";
}

function tupleTypes() returns string {

    [int, string] t1 = [10, "A"];
    match t1 {
        20 => {return "20";} // pattern will not be matched
        {a: 20} => {return "{a: 20}";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        [20, 21] => {return "[20, 21]";} // pattern will not be matched
        [20, "A", true] => {return "[20, 21]";} // pattern will not be matched
        [20, "A"] => {return "[20, 21]";}
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    [int, string]|[string, int] t2 = ["A", 2];
    match t2 {
        [12, "A"] => {return "[12, \"A\"]";}
        ["A", 12] => {return "[12, \"A\"]";}
    }

    [int|string, string|boolean] t3 = ["A", false];
    match t3 {
        [12, "A"] => {return "t3";}
        [12, false] => {return "t3";}
        ["A", "false"] => {return "t3";}
        ["A", false] => {return "t3";}
        [false, "A"] => {return "t3";} // pattern will not be matched
    }

    [int, Rec1|string] t4 = [1, "Ballerina"];
    match t4 {
        [12, "B"] => {return "t4";}
        [12, {a: "B"}] => {return "t4";} // pattern will not be matched
        [12, {a: 12.1}] => {return "t4";}
    }

    [int, Rec4|string]|Rec1 t5 = [1, "Ballerina"];
    match t5 {
        [12, "B"] => {return "t5";}
        [12, {a: "B"}] => {return "t5";} // pattern will not be matched
        [12, {a: 12.1, b: false}] => {return "t5";}
        [12, {a: 12.1}] => {return "t5";}
        {a: "A"} => {return "t5";} // pattern will not be matched
        {a: 12} => {return "t5";}
    }
    return "Fail";
}

type Foo record {|
    int x;
    boolean y?;
    string...;
|};

function recordRestParamAndOptionalFields() returns string {
    Foo f = {x: 1, "a": "a"};
    match f {
        {a: "b"} => {return "f";}
        {x: 1, b: "b"} => {return "f";}
        {x: 1, b: 2} => {return "f";} // pattern will not be matched
        {x: 1, y: 2} => {return "f";} // pattern will not be matched
        {x: 1, y: true} => {return "f";}
    }

    return "Fail";
}

type Finite "A" | "B" | true | 15;

function finiteTypes() returns string {
    Finite f = "B";
    match f {
        16 => {return "a";} // pattern will not be matched
        15 => {return "a";}
        "A" => {return "a";}
        {a: "b"} => {return "a";} // pattern will not be matched
        "B" => {return "a";}
        true => {return "a";}
        false => {return "a";} // pattern will not be matched
        "C" => {return "a";} // pattern will not be matched
        [12, "B"] => {return "a";} // pattern will not be matched
    }

    return "Fail";
}

class Obj {
    int var1 = 12;
}

function nonAnydataTypes() returns string {
    Obj y = new;

    match y {
        {var1: 12} => {return "a";} // pattern will not be matched
        {"var1": 12} => {return "a";} // pattern will not be matched
        //{foo(): 12} => {return "a";} // pattern will not be matched and invalid key
    }
    return "Fail";
}

function foo() returns string {
    return "var1";
}

function singleMatchStmt() returns string {
    any k = 1;
    match k {
        _ => {return "A";} // pattern will always be matched
    }
}

function invalidSimpleVariable() returns string {
    any k = 1;
    string a = "A";
    match k {
        10 => {return "A";} // pattern will always be matched
        // a => {return "A";} invalid literal for match pattern; moved to static_match_patterns_semantics_negative.bal
    }
}

const CONST_1 = "A";
const CONST_2 = "B";
const CONST_3 = "C";

function invalidConstTypes(CONST_1|CONST_2 a) returns string {
    match a {
        CONST_1 => {return "A";}
        CONST_2 => {return "B";}
        CONST_3 => {return "C";} // pattern will not be matched
    }
    return "Default";
}
