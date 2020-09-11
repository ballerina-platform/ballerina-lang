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

xmlns "http://sample.com/wso2/a1" as ns;

string name = "";

function printName(any name) {

    xmlns "http://sample.com/wso2/a2" as ns;
    var name = formatName(name);

    if (<string>name == ""){
        xmlns "http://sample.com/wso2/a3" as ns;
        var getName = function () returns (string) {
            string name = "Ballerina";
            return "";
        };
        name = getName();
    }

    xmlns "http://sample.com/wso2/a3" as ns;
}

function formatName(any a) returns any {
    xmlns "http://sample.com/wso2/a3" as ns;
    return a;
}

class User {

    public string name = "";


    function f(string name) {
        xmlns "http://sample.com/wso2/a4" as ns;
        string name = "";
        xmlns "http://sample.com/wso2/a4" as ns;
    }
}

service ser = service {



    resource function res(string name) {
        xmlns "http://sample.com/wso2/a6" as ns;
        string name = "";
        xmlns "http://sample.com/wso2/a7" as ns;
    }
};

function testLambdaFunctions() returns string {
    int x = 0;

    var fn = function () returns string {
        string x = "Inside a lambda function";
        return x;
    };

    if (true) {
        float f = 12.34;

        var fn2 = function () returns string {
            string x = "Inside second lambda function";
            string f = "This should cause a compile error too";
            return x;
        };
    }

    return fn();
}

function testNestedLambdaFunctions() returns string {
    int x = 0;

    var fn = function () returns string {
        string x = "Inside a lambda function";

        var fn = function (string x) returns string {
            string name = "This is valid";
            return x;
        };

        return x;
    };

    return fn();
}

function testFuncParams(string param) {
    string param = "This is invalid";
}

type Person record {
    string name = "Smith";
};
string Person = "Smith"; // This variable name equals type name 'Person'

function testTypeNameAsVariableInSameScope1() {
}

const Student = "Anne";
class Student { // This type name equals const name 'Student'
    int studentId = 0;
}

function testTypeNameAsVariableInSameScope2() {
}

const Person1 = "Person";
const Person2 = "Person";
type Person Person1 | Person2; // This type name equals const name 'Person'

class Vehicle {
    string name = "Car";
}

function testTypeNameAsVariableInDifferentScope1() {
    string Vehicle = "Van";
    Vehicle vehicle = new;
}

string returnVal = "Return value";

function testVarAsReturnType() returns returnVal {
}
