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

int nonIsolatedVar = 0;

//////////////////// Service Declarations ////////////////////

service on new Listener() {
    private int i = 0;
    int j = 0;

    // Inferred isolated.
    resource function get . () {
    }

    // Inferred isolated.
    remote function foo() {
        lock {
            self.i += 1 + self.j;
        }
    }

    function func() {
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

isolated service "ser2" on new Listener() {
    private int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i += 1 + nonIsolatedVar;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

// Inferred isolated.
service "ser3" on new Listener() {
    final int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() returns int {
        return self.i + 1 + nonIsolatedVar;
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

service "ser4" on new Listener() {
    private int i = 0;
    int j = 1;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i + = 1 + nonIsolatedVar + self.j;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

//////////////////// Service Classes ////////////////////

service class Serv1 {
    private int i = 0;
    int j = 0;

    // Inferred isolated.
    resource function get . () {
    }

    // Inferred isolated.
    remote function foo() {
        lock {
            self.i += 1 + self.j;
        }
    }

    function func() {
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

isolated service class Serv2 {
    private int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i += 1 + nonIsolatedVar;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

// Inferred isolated.
service class Serv3 {
    final int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() returns int {
        return self.i + 1 + nonIsolatedVar;
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

service class Serv4 {
    private int i = 0;
    int j = 1;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i + = 1 + nonIsolatedVar + self.j;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
}

//////////////////// Service Object Constructors ////////////////////

var s1 = service object {
    private int i = 0;
    int j = 0;

    // Inferred isolated.
    resource function get . () {
    }

    // Inferred isolated.
    remote function foo() {
        lock {
            self.i += 1 + self.j;
        }
    }

    function func() {
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
};

var s2 = isolated service object {
    private int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i += 1 + nonIsolatedVar;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
};

var s3 = service object {
    final int i = 0;

    resource function get . () {
        self.func();
    }

    remote function foo() returns int {
        return self.i + 1 + nonIsolatedVar;
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
};

service object {} s4 = service object {
    private int i = 0;
    int j = 1;

    resource function get . () {
        self.func();
    }

    remote function foo() {
        lock {
            self.i + = 1 + nonIsolatedVar + self.j;
        }
    }

    function func() {
        nonIsolatedFunc();
    }

    resource function post bar(string[] mutableVal) {
        _ = start mutableValueAccess(mutableVal);
    }

    resource function post path(T1 t1) {
        _ = start mutableValueAccess(t1.arg1);
    }

    resource function post path2(T1 t1) {
        NonIsolatedObj obj = new;
        _ = start modifyObject(getObj(obj));
    }
};

function nonIsolatedFunc() {
    nonIsolatedVar += 1;
}

// No warnings should be logged for these since these objects and methods are either
// explicitly isolated or are inferred to be isolated.
isolated service on new Listener() {
    private int i = 0;

    resource function get . () {
    }

    remote function foo() {
        lock {
            self.i + = 1;
        }
    }
}

service "serv" on new Listener() {
    final int i = 0;

    resource function get . () returns int[] {
        return [self.i];
    }

    remote function foo() {
    }
}

public isolated service class Serv {
    final int i = 1;

    isolated remote function foo() {
    }

    isolated resource function get i () returns int => self.i;

    // No warnings are logged for non-resource/remote methods
    // since these aren't called by the listener.
    function func() {
        nonIsolatedVar += 1;
    }
}

// Inferred isolated.
var s5 = service object {
    final int i = 0;

    isolated resource function get . () {
        self.func();
    }

    isolated remote function foo() returns int {
        return self.i + 1;
    }

    isolated function func() {
    }
};

var s6 = isolated service object {
    final int i = 0;
    private int j = 1;

    isolated resource function get . () {
    }

    isolated remote function foo() returns int {
        lock {
            return self.i + self.j + 1;
        }
    }
};

isolated function mutableValueAccess(string[] value) {
}

isolated function modifyObject(object {} obj) {
}

type T1 record {|
    string[] arg1;
|};

class NonIsolatedObj {
    int x = 1;

    function fn() {
    }
}

function getObj(NonIsolatedObj obj) returns NonIsolatedObj => obj;

public class Listener {
    public isolated function 'start() returns error? {
        return;
    }

    public isolated function gracefulStop() returns error? {
        return;
    }

    public isolated function immediateStop() returns error? {
        return;
    }

    public isolated function detach(service object {} s) returns error? {
        return;
    }

    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return;
    }
}
