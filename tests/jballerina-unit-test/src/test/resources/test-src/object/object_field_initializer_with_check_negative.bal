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

class Foo {
    function a = function () returns error? {
        // OK, since it is enclosed in the function, and not directly used in the default value.
        int q = check int:fromString("invalid");
        return;
    };
    int b = check int:fromString("invalid"); // error.
    int[]|error c = check f1(); // error.
    string[] d = ["", check f3()]; // error.
}

type MyError distinct error;

isolated function f1() returns int[]|error => [];

isolated function f2() returns MyError|object { function a; int b; int c; } {
    var v = object {
            function a = function () returns error? {
                // OK, since it is enclosed in the function, and not directly used in the default value.
                int _ = check int:fromString("invalid");
                record {|
                    any x = check f1(); // error.
                |} _ = {};
                return;
            };
            int b = check int:fromString("invalid"); // error.
            int c = 0;
    };
    return v;
}

isolated function f3() returns string|error|MyError => "";

object {
    any|error a;
    object {
        function a;
        int b;
        int c;
    } b;
    string c;
} rec = object {
    any|error a = check f1(); // error.
    object {
        function a;
        int b;
        int c;
    } b = check f2(); // error.
    string c = check f3(); // error.
};

isolated function func() {
    object {
        any|error a;
        record {|
            function a = function () returns error? {
                // OK, since it is enclosed in the function, and not directly used in the default value.
                int _ = check int:fromString("invalid");
                return;
            };
            int b = check int:fromString("invalid"); // error.
            int c = check int:fromString(check f3()); // error.
        |} b;
        object {} rec2;
    } _ = object {
        any|error a = check f1(); // error.
        record {|
            function a = function () returns error? {
                // OK, since it is enclosed in the function, and not directly used in the default value.
                int _ = check int:fromString("invalid");
                return;
            };
            int b = check int:fromString("invalid"); // error.
            int c = check int:fromString(check f3()); // error.
        |} b = {};
        object {} rec2 = object {
            any|error a = check f1(); // error.
        };
        object {
            function a;
            int b;
            int c;
        }[] d = [check f2(), check f2()]; // error.
    };
}

class Bar {
    function a = function () returns error? {
        // OK, since it is enclosed in the function, and not directly used in the default value.
        int _ = check int:fromString("invalid");
        return;
    };
    int b = check int:fromString("invalid"); // error.
    int[]|error c = check f1(); // error.
    string[] d = ["", check f3()]; // error.

    function init() returns MyError? {
        return;
    }
}

function f4() returns MyError|object { function a; int b; int c; } {
    var v = object {
                function a = function () returns error? {
                    // OK, since it is enclosed in the function, and not directly used in the default value.
                    int _ = check int:fromString("invalid");
                    object { function a; int b; int c; } _ = check f2();
                    return;
                };
                int b = check int:fromString("invalid"); // error.
                int c = 0;

                function init() returns MyError? {
                    if self.b > 1 {
                        return error("error!");
                    }
                    return;
                }
    };
    return v;
}

type MyErrorTwo distinct error;

var ob = object {
    any|error a = check f1(); // error.
    object {
        function a;
        int b;
        int c;
    } b = check f2(); // OK.
    string c = check f3(); // error.

    function init() returns MyError|MyErrorTwo? {
        return;
    }
};

function func2() returns error? {
    var ob2 = object {
        any|error a = check f2(); // error.

        function init() returns MyErrorTwo? {
            return;
        }
    };

    any _ = check ob;
    any _ = check ob2;
    return;
}

class Baz {
    any a = check new Qux(function () returns error? { // error
                              // OK, since it is enclosed in the function, and not directly used in the default value.
                              int _ = check int:fromString("invalid");
                              return;
                          });
    any b = check new Qux(() => check int:fromString("invalid")); // error for outer check
}

class Quux {
    any a = check new Qux(function () returns error? { // error
                              // OK, since it is enclosed in the function, and not directly used in the default value.
                              int _ = check int:fromString("invalid");
                              return;
                          });
    any b = check new Qux(() => check int:fromString("invalid")); // error for outer check

    function init() returns MyError? {
        return;
    }
}

class Qux {
    isolated function init(function () returns int|error? f) returns error? {
        return;
    }
}

class C1 {
    int i = let int x = check fn() in x + check fn();
}

class C2 {
    int i = let int x = check fn() in x + check fn();

    function init() returns MyError? {
        return;
    }
}

var v = object {
    int i = let int x = check fn() in x + 1;
};

var w = object {
    int i = let int x = check fn() in x + 1;

    function init() returns MyError? {
        return;
    }
};

function testObjectConstructor() {
    var _ = object {
        int i = let int x = check fn() in x + 2 * check fn();
    };
    var y = object {
        int i = let int y = check fn() in y + 2 * check fn();

        function init() returns MyError? {
            return;
        }
    };

    any|error a = w;
    any|error b = y;
}

isolated function fn() returns int|error => 0;
