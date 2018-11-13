 // Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 //
 // WSO2 Inc. licenses this file to you under the Apache License,
 // Version 2.0 (the "License"); you may not use this file except
 // in compliance with the License.
 // You may obtain a copy of the License at
 //
 //   http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing,
 // software distributed under the License is distributed on an
 // "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 // KIND, either express or implied.  See the License for the
 // specific language governing permissions and limitations
 // under the License.
 
 import ballerina/http;
 import ballerina/io;
 
 function testDataflow_1() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            msg = "3";
        }
    } else if (true) {
        msg = "4";
    } else {
        msg = "5";
    }

    return msg;
}

function testDataflow_2() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            // do nothing
        } else {
            msg = "2";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_3() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else if (true) {
            msg = "2";
        } else {
            // do nothing
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_4() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        }
    } else {
        msg = "2";
    }
    
    return msg;
}

function testDataflow_5() returns string {
    string msg;
    if (true) {
        if (true) {
            //do nothing
        } else {
            msg = "1";
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_6() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            msg = "1";
        }
    } else {
        // do nothing
    }

    return msg;
}

function testDataflow_7() returns string {
    string msg;
    if (true) {
        msg = "1";
        if (true) {
        } else {
        }
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_8() returns string {
    string msg;
    if (true) {
        if (true) {
            // do nothing
        } else {
            // do nothing
        }
        msg = "1";
    } else {
        msg = "4";
    }

    return msg;
}

function testDataflow_9() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            msg = "2.1.1.1.2";
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testDataflow_10() returns string {
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            if (true) {
                if (true) {
                    if (true) {
                        if (true) {
                            msg = "2.1.1.1.1";
                        } else if (true) {
                            // do nothing
                        } else {
                            msg = "2.1.1.1.3";
                        }
                    } else {
                        msg = "2.1.1.2";
                    }
                } else {
                    msg = "2.1.2";
                }
            } else {
                msg = "2.2";
            }
        }
    } else {
        msg = "3";
    }

    return msg;
}

function testUninitializedVarReferrencing() {
    map m;
    int a;
    string s;
    error e = error(s);

    // assigning uninitialized var
    int b = a;

    // increment uninitialized var
    a = a + 1;

    // panic uninitialized var
    if (false) {
        panic getError(s);
    }

    // uninitialized var in:
    //    - 'if' condition
    //    - braced expr
    //    - binary expr
    if (a > 4) {
        // do nothing
    }

    // uninitialized var foreach
    foreach val in m {
        m["msg"] = "hello";
    }

    // uninitialized var in while
    while (4 > a) {
        a = a + 1;
    }

    // uninitialized var in conversion
    string str = <string> a;

    // uninitialized var XML
    xml x1 = xml`<foo id="{{a}}" xmlns:ns0="{{a}}">
                    <bar> hello {{a}}</bar>
                    <!-- refering to uninitialized {{a}} -->
                    <?target content {{a}}?>
                </foo>`;
                
    // uninitialized var in string template
    string text = string `hello {{a}}`;

    // uninitialized var index/field based access
    _ = m.foo;
    _ = m[s];

    // uninitialized var in function invocation, expression statement
    _ = m.hasKey(s);
    foo(a, str = s, s);

    // uninitialized var in xml attribute access
    xml x;
    _ = x@[s];

    // uninitialized var in range expression
    int[] range = a...a+5;
}

function foo(int a, string str = "hello", string... args) {
    // do nothing
}

function getError(string msg) returns error {
    return error(msg);
}

function testDataflow_11() returns string {
    if (true) {
        string msg;
        if (true) {
            msg = "1";
        } else {
            return msg;
        }
        return msg;
    } else {
        return "n/a";
    }
}

public int globalVar;

function updateGlobalVar() {
    globalVar = 4;
}

function testGlobalVar() returns int {
    return globalVar;
}

type Foo object {
    int a = globalVar;
    int b;
    int c;
    int d;
    int e;

    new (c, e=4, f, int x) {
        self.a = globalVar;
        self.b = e;
    }

    function getGlobalVar() returns int {
        return globalVar;
    }

    function getA() returns int {
        return self.a;
    }

    function getB() returns int {
        self.d = 46;
        return self.b;
    }

    function getC() returns int {
        return self.c;
    }

    function getD() returns int {
        return self.d;
    }
};

function testIfFollowedByIf() returns int {
    int x;
    if (true) {
    } else if (true) {
    } else {
    }

    if (true) {
        x = 1;
    } else if (true) {
        x = 2;
    } else {
        x = 3;
    }

    return x;
}

function testMatch_1() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => val = "string";
        any => val = "any";
    }

    return val;
}

function testMatch_2() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => {}
        any => val = "any";
    }

    return val;
}

function testMatch_3() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => val = "string";
        any a => {
            match a {
                int => val = "int";
                string => { val = "string";}
                any => val = "any";
            }
        }
    }

    return val;
}

function testMatch_4() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => val = "string";
        any a => {
            match a {
                int => val = "int";
                string => {}
                any => val = "any";
            }
        }
    }

    return val;
}

function testMatch_5() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => val = "string";
        any a => {
            match a {
                int => val = "int";
                string => {
                    if (true) {
                        val = "true string";
                    } else {
                        val = "false string";
                    }
                }
                any => val = "any";
            }
        }
    }

    return val;
}

function testMatch_6() returns string {
    any x = 6;
    string val;
    match x {
        int => val = "int";
        string => val = "string";
        any a => {
            match a {
                int => val = "int";
                string => {
                    if (true) {
                        val = "true string";
                    } else {

                    }
                }
                any => val = "any";
            }
        }
    }

    return val;
}

endpoint http:NonListener echoEP {
    port:9090
};

service<http:Service> echo bind echoEP {

    string x;
    string y = "sample value";

    echo_1(endpoint conn, http:Request req) {
        string a = x;
        a = y;
        x = "init within echo_1";
    }

    echo_2(endpoint conn, http:Request req) {
        string a = x;
        a = y;
        x = "init within echo_2";
    }
}

string yyy;
function testWorkers() returns string {
    worker w1 {
        io:println(yyy);
        yyy = "w1";
        return yyy;
    }

    worker w2 {
        io:println(yyy);
        yyy = "w2";
    }
}

function testCounpundAssignment() {
    int a;
    a += 2;
}


function testUninitVsPartiallyInit() returns (string, string) {
    string a;
    string b;
    if (true) {
        if (true) {
            // do nothing
        } else if (true) {
            b = "something";            
        } else {
            // do nothing
        }
    } else {
        // do nothing
    }

    return (a, b);
}

type A object {
    public int a;
    private int b;
    int c;

    new () {
        self.a = 1;
        self.b = 2;
        self.c = 3;
    }
};

type B object {
    public int a;
    private int b;
    int c;

    new (a, b, c) {
    }
};

type C object {
    public int a;
    private int b;
    int c;

    new () {
    }
};

public int publicGlobalVar_1;
int publicGlobalVar_2;

public type D record {
    string a;
    int b;
    int c;
};

endpoint http:Listener testEP {
    port: 9092
};

service<http:Service> testService bind testEP {

    int a;

    resource_1(endpoint caller, http:Request req) {
        a = 5;
        int b = a;
        int c;

        if (true) {
            done;
        } else {
            c = 3;
        }

        int d = c;
    }

    resource_2(endpoint caller, http:Request req) {
        int b = a;
    }
}

function testDataflowWithPanic_1() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        if (true) {
            msg = "1";
        } else {
            panic e;
        }
    } else {
        msg = "n/a";
    }

    return msg;
}

function testDataflowWithNestedPanic_1() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        panic e;
        if (true) {
            msg = "1";
        } else {
            panic e;
        }
    } else {
        // do nothing
    }

    return msg;
}

function testDataflowWithNestedPanic_2() returns string {
    error e = error("some error");
    string msg;
    if (true) {
        panic e;
        if (true) {
            // do nothing
        } else {
            panic e;
        }
    } else {
        msg = "1";
    }

    return msg;
}

type E object {
    public int a;
    private int b;
    int c;
};

type Age record {
    int age;
    string format;
};

type Person record {
    string name;
    boolean married;
    Age age;
    (string, int) extra;
    !...
};

function testVariableAssignment() returns (string, boolean, int, string) {
    string fName;
    boolean married;
    int theAge;
    string format;
    map theMap;

    {name: fName, married, age: {age: theAge, format}, ...theMap} = getPerson();
    return (fName, married, theAge, format);
}

function getPerson() returns Person {
    return {name: "Peter", married: true, age: {age:12, format: "Y"}};
}
