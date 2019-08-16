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

function testRecordUnreachablePattern() returns string {
    OpenedFoo k = {s: "A", i: 12};
    any a = k;
    match k {
        var{x: name, y: age} => {return "A";}
        var{x: name, y: [p, q]} => {return "A";} // unreachable since y is matched above
        var{x: name} => {return "A";}
        var{x: name} => {return "A";} // unreachable since same type
    }

    return "Default";
}

function testTupleUnreachablePattern() returns string {
    any an = 112;
    match an {
        var [a, b, {s, i}] => {return "A";}
        var [a, b, c] => {return "A";}
        var [a, b, c, d] => {return "A";}
        var [a, b, c, {e, f}] => {return "A";} // unreachable
        var [a, b, c, [s, i]] => {return "A";} // unreachable

        var [a, {b}] => {return "A";}
        var [a, {b, c}] => {return "A";} // unreachable
    }

    return "Default";
}

function testSimpleRules() returns string {
    any k = 1;
    match k {
        var {a} => {return "A";}
        var {a, b} => {return "A";} // unreachable
        var {a} => {return "A";} // unreachable
    }

    match k {
        var [a, b] => {return "A";}
        var [a, b, c] => {return "A";}
        var [a, b] => {return "A";} // unreachable
    }

    match k {
        var x => {return "A";}
        var y => {return "A";} // unreachable
        var {a, b} => {return "A";} // unreachable
        var [a, b] => {return "A";} // unreachable
        var y => {return "A";} // unreachable
    }

    return "Default"; // unreachable
}

function testMixedVariables() returns string {
    any k = 1;
    match k {
        var {x: name, y: [p, q]} => {return "A";}
        var [a, b, c, {e, f}] => {return "A";}
        var {x: name, y: age} => {return "A";}
        var [a, b, c, d] => {return "A";}
        var x => {return "A";}
    }

    match k {
        var {x: name, y: age} => {return "A";}
        var [a, b, c, d] => {return "A";}
        var {x: name, y: [p, q]} => {return "A";} // unreachable
        var [a, b, c, {e, f}] => {return "A";} // unreachable
        var x => {return "A";}
    }

    return "Default"; // unreachable
}

function testClosedRecordPatterns() returns string {
    any k = 1;
    match k {
        var { var1: name } => {return "A";}
        var { var1: name2 } => {return "B";} // unreachable
        var { var3: name } => {return "C";}
        var { var3: name } => {return "D";} // unreachable
        var [a, b, {var1, var2: [d, {var2}]}, c] => {return "E";}
        var [a, b, {var1, var2: [d, { var2 }]}, c] => {return "D";} // unreachable
    }

    return "Default";
}

function testWithTypeGuard() returns string {
    any k = 1;
    match k {
        var [a, b] if a is string => {return "A";}
        var [a, b] => {return "A";}
        var [a, b] if a is int => {return "A";} // unreachable
        var [a, b, c] if a is boolean => {return "A";}
        var [a, b, c] if a is boolean => {return "A";} // unreachable
        var [a, b] if a is string => {return "A";} // unreachable
    }

    return "A";
}

function testUnreachableReturnStmt() returns string {
    any k = 1;
    match k {
        var [a, b] if a is string => {return "A";}
        var x => {x = "A";}
    }

    match k {
        var [a, b] if a is string => {return "A";}
        var x => {return  "B";}
    }

    return "A"; // unreachable
}

function testUnreachableCode() returns string {
    any k = 1;
    match k { // match statement has a static value default pattern and a binding value default pattern
        var x => {return "A";}
        _ => {return "A";}
    }
}

function testUnreachableCode2() returns string { // function must return a result
    any k = 1;
    match k { // match statement has a static value default pattern and a binding value default pattern
        var x => {x = "A";}
        _ => {k = "A";}
    }
}

function testUnreachableCode3() returns string {
    any k = 1;
    match k {
        var x => {return "A";}
        _ => {return "A";}
        "12" => {return "A";} // unreachable pattern
        var [a, b] => {return "A";} // unreachable pattern
    }

    return "A";
}
