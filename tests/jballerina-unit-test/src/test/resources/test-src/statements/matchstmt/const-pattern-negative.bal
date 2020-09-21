// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Finite "A" | "B" | true | 15;

type Rec1 record {|
    int | float a;
|};

function simpleTypes() returns string {
    int x1 = 10;
    match x1 {
        20 => {return "20";}
        false => {return "false";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    boolean x2 = true;
    match x2 {
        20 => {return "20";} // pattern will not be matched
        false => {return "false";}
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    float x3 = 10.1;
    match x3 {
        20 => {return "20";} // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";}
    }

    string x4 = "Ballerina";
    match x4 {
        20 => {return "20"; } // pattern will not be matched
        false => {return "false";} // pattern will not be matched
        21 => {return "21";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";}
        10.4 => {return "10.4";} // pattern will not be matched
    }

    byte x5 = 200;
    match x5 {
        20 => {return "20";}
        false => {return "false";} // pattern will not be matched
        21 => {return "21";}
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    Rec1 r1 = {a: 200};
    match r1 {
        20 => {return "20"; }// pattern will not be matched
        false => {return "false";} // pattern will not be matched
        "Ballerina" => {return "Ballerina";} // pattern will not be matched
        10.4 => {return "10.4";} // pattern will not be matched
    }

    Finite f = "B";
    match f {
        16 => {return "a";} // pattern will not be matched
        false => {return "a";} // pattern will not be matched
        "C" => {return "a";} // pattern will not be matched
    }

    return "Fail";
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

function testNegative1(int v) returns string {
    string s;

    match v {
        1 => {
            s = "ONE";
        }
    }
    return s; // variable 's' may not have been initialized
}

function testNegative2(int v) returns string {
    string s;

    match v {
        1 => {
            s = "ONE";
        }
        _ => {
            match v {
                2 => {
                    s = "TWO";
                }
            }
        }
    }
    return s; // variable 's' may not have been initialized
}
