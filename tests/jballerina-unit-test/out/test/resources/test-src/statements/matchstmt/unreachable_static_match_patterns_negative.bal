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

type Rec1 record {
    int | float a;
};

function recordTypes() returns string {
    Rec1 r1 = {a: 200};
    match r1 {
        {a: 200, b: "A"} => {return "A";}
        {a: 200, b: "A"} => {return "A";} // unreachable pattern
        {b: "A", a: 200} => {return "A";} // unreachable pattern
        {a: 100, b: "A"} => {return "A";}
        {a: 200, b: "B"} => {return "A";}
        {a: 150, b: "A", c: true} => {return "A";}
        {a: 150, b: "A"} => {return "A";}
        {a: 150, b: "A", c: false} => {return "A";} // unreachable pattern
    }

    return "Fail";
}

function tupleTypes() returns string {
    [string, int] t1 = ["A", 12];
    match t1 {
        ["A", 15] => {return "T";}
        ["A", 15] => {return "T";}// unreachable pattern
        ["B", 15] => {return "T";}
        ["B", 15] => {return "T";} // unreachable pattern
    }

    return "Fail";
}

function simpleTypes() returns string {
    anydata k = 10;
    match k {
        15 => {return "T";}
        [12, 15] => {return "T";}
        20 => {return "T";}
        20 => {return "T";} // unreachable pattern
        15 => {return "T";} // unreachable pattern
        [12, 15] => {return "T";} // unreachable pattern
    }

    return "Fail";
}

function unreachableCode() returns string {
    anydata k = 10;
    match k {
        15 => {return "T";}
        [12, 15] => {return "T";}
        _ => {return "T";}
    }

    return "Fail"; // unreachable
}

const CONST_1 = "B";
const CONST_2 = "B";

function invalidConstPatterns(any a) returns string {
    match a {
        CONST_1 => {return "B";}
        CONST_2 => {return "B";} // unreachable pattern: preceding patterns are too general or the pattern ordering is not correct
    }

    match a {
        "B" => {return "B";}
        CONST_2 => {return "B";} // unreachable pattern: preceding patterns are too general or the pattern ordering is not correct
    }

    match a {
        CONST_2 => {return "B";}
        "B" => {return "B";}// unreachable pattern: preceding patterns are too general or the pattern ordering is not correct
    }

    match a {
        _ => {return "Default";}
        CONST_1 => {return "B";} // unreachable pattern: preceding patterns are too general or the pattern ordering is not correct
    }
    return "Default";
}

function testUnreachableUnionStaticPatterns() returns string {
    any a = 10;
    match a {
        15 => {return "1";}
        10 => {return "2";}
        10|11 => {return "3";} // unreachable
    }

    match a {
        10|11 => {return "1";}
        15 => {return "2";}
        11 => {return "3";} // unreachable
    }

    match a {
        10|11 => {return "1";}
        15 => {return "2";}
        12|11 => {return "3";} // unreachable
    }

    match a {
        10|11|"Ballerina" => {return "1";}
        15 => {return "2";}
        12 => {return "3";}
        "Ballerina" => {return "4";} // unreachable
    }

    match a {
        10|11|"Ballerina" => {return "1";}
        15 => {return "2";}
        "Ballerina"|"Lang" => {return "4";} // unreachable
        12|11 => {return "3";} // unreachable
    }

    match a {
        "Ballerina"|true|["Ballerina", true] => {return "1";}
        { x: "Ballerina", y: true }|["Bal", "Lang"] => {return "2";}
        { x: "Ballerina", y: false } => {return "3";}
        false|["Ballerina", true] => {return "4";} // unreachable
        12|{ x: "Ballerina", y: true } => {return "3";} // unreachable
    }

    return "4";
}

function invalidEmptyListPatterns(any x) returns string {
    match x {
        [] => {return "Matched with empty array literal";}
        var [] => {return "Matched with empty array variable";} // unreachable
        {} => {return "Matched with empty record literal";}
        var {} => {return "Matched with empty record variable";} // unreachable
        var s => {return "Matched with default";}
    }
}

function invalidEmptyRecordPatterns(any x) returns string {
    match x {
        var {} => {return "Matched with empty record";}
        var {a} => {return "Matched with {a}";} // unreachable
        var {a, b} => {return "Matched with {a, b}";} // unreachable
        var {a, b, c} => {return "Matched with {a, b, c}";} // unreachable
        var s => {return "Matched with default";}
    }
}
