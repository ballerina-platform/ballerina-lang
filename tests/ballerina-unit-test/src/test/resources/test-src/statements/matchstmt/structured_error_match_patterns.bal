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

import ballerina/io;

function testBasicErrorMatch() returns string {
    error <string, map<string>> err1 = error("Error Code", { message: "Msg" });
    match err1 {
        var error(reason, detail) => return "Matched with error : " + reason + " " + io:sprintf("%s", detail);
    }
    return "Default";
}

function testBasicErrorMatch2() returns string {
    error <string, map<string>> err1 = error("Error Code", { message: "Msg" });
    (string, map<any>)|error t1 = err1;
    match t1 {
        var (reason, detail) => return "Matched with tuple : " + reason + " " + io:sprintf("%s", detail);
        var error(reason, detail) => return "Matched with error : " + reason + " " + io:sprintf("%s", detail);
    }
    return "Default";
}

function testBasicErrorMatch3() returns string {
    error <string> err1 = error("Error Code");
    (string, map<any>)|error <string> t1 = err1;
    match t1 {
        var (reason, detail) => return "Matched with tuple : " + reason + " " + io:sprintf("%s", detail);
        var error(reason, detail) => return "Matched with error : " + reason + " " + io:sprintf("%s", detail);
    }
    return "Default";
}

type Foo record {
    boolean fatal;
};

type ER1 error <string, Foo>;
type ER2 error <string, map<anydata>>;

function testBasicErrorMatch4() returns string[] {
    ER1 er1 = error("Error 1", { fatal: true, message: 1 });
    ER2 er2 = error("Error 2", { message: "It's fatal", fatal: "fatal string" });
    string[] results = [foo(er1), foo(er2)];
    return results;
}

function foo(ER1|ER2 t1) returns string {
    match t1 {
        var error (reason, { fatal, message }) => {
            if fatal is boolean {
                return "Matched with boolean : " + fatal;
            } else if message is string {
                return "Matched with string : " + message;
            } else {
                return "Matched with fatal as Nil";
            }
        }
    }
    return "Default";
}

function testBasicErrorMatch5() returns string[] {
    Foo f = { fatal: true };
    error err1 = error("Error Code 1");
    error err2 = error("Error Code 1", { message: "Something Wrong" });
    Foo|error fe1 = err1;
    Foo|error fe2 = err2;
    string[] results = [foo2(f), foo2(fe1), foo2(fe2), foo3(fe1), foo3(fe2)];
    return results;
}

function foo2(any|error f) returns string {
    match f {
        var { fatal } => return "Matched with a record : " + io:sprintf("%s", fatal);
        var error (reason) => return "Matched with an error : " + reason;
    }
    return "Default";
}

function foo3(any|error f) returns string {
    match f {
        var { fatal } => return "Matched with a record : " + io:sprintf("%s", fatal);
        var error (reason, detail) => return "Matched with an error : " + reason + " " + io:sprintf("%s", detail);
    }
    return "Default";
}

function testBasicErrorMatch6() returns string[] {
    map<any> m = { key: "value" };
    string[] results = [foo4(m.key), foo4(trap m.invalid)];
    return results;
}

function foo4(any|error a) returns string {
    match a {
        "value" => return "Matched with string";
        var error (reason, detail) => return "Matched with an error " + reason + " " + io:sprintf("%s", detail);
    }
    return "Default";
}

function testErrorWithUnderscore() returns string[] {
    error err1 = error("Error One");
    string[] results = [foo5(err1)];
    return results;
}

function foo5(any|error e) returns string {
    match e {
        var (a, b) => return "Matched with tuple var : " + io:sprintf("%s", a);
        var { a, b } => return "Matched with record var : " + io:sprintf("%s", a);
        var error (reason, _) => return "Matched with error var : " + io:sprintf("%s", reason);
        var x => {
            if x is any {
                return "Matched nothing : " + io:sprintf("%s", x);
            } else {
                return "Matched nothing";
            }
        }
    }
}

function testBasicErrorMatch7() returns string[] {
    Foo f = { fatal: true };
    error err1 = error("Error Code 1");
    error err2 = error("Error Code 1", { message: "Something Wrong" });
    error <string, map<string|boolean>> err3 = error("Error Code 1", { message: "Something Wrong", fatal: true });
    Foo|error fe1 = err1;
    Foo|error fe2 = err2;
    string[] results = [foo6(f), foo6(fe1), foo6(fe2)];
    return results;
}

function foo6(any|error f) returns string {
    match f {
        var { fatal } => return "Matched with a record : " + io:sprintf("%s", fatal);
        var error (reason, { message }) => return "Matched with an error : " + reason + io:sprintf("%s", message);
    }
    return "Default";
}

type Fin1 "Error One"|"Error Two";
type Fin2 "Error Three"|"Error Four";

function testFiniteTypedReasonVariable() returns string[] {
    error<Fin1, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<Fin2, map<string|boolean>> err2 = error("Error Three", { message: "msgTwo", fatal: false });

    error<Fin2, map<string|boolean>>|error<Fin1, map<string|boolean>> err3 = err1;
    error<Fin2, map<string|boolean>>|error<Fin1, map<string|boolean>> err4 = err2;

    string[] results = [matching(err3), matching(err4)];
    return results;
}

function matching(error<Fin2, map<string|boolean>>|error<Fin1, map<string|boolean>> a) returns string {
    match a {
        var error(reason, detail) => return io:sprintf("%s %s", reason, detail);
        var x => return "Failed";
    }
}
