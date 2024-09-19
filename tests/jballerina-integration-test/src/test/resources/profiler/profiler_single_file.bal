// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import ballerina/test;
import ballerina/jballerina.java;

// public function '\.\<init\>() returns string {
//     return "this is a user defined function";
// }

class Person {
    public string firstName;
    public string lastName;

    function init(string firstName, string lastName) {
        self.firstName = firstName;
        self.lastName = lastName;
    }

    // function '\$init\$() returns string {
    //     return self.firstName + " " + self.lastName;
    // }
}

type '\$anonType\$_0 record {
    string name;
    int '\$_field;
};

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}

public function main() {
    Person person = new("John", "Doe");
    test:assertEquals("John", person.firstName);
    // test:assertEquals("John Doe", person.'\$init\$());
    // test:assertEquals("this is a user defined function", '\.\<init\>());

    record {|
        string name;
        int '\$_field;
    |} type_var1 = { name: "anon1", '\$_field: 10 };

    test:assertEquals("anon1", type_var1.name);
    test:assertEquals(10, type_var1.'\$_field);

    '\$anonType\$_0 type_var2 = { name: "anon2", '\$_field: 15 };

    test:assertEquals("anon2", type_var2.name);
    test:assertEquals(15, type_var2.'\$_field);

    print("Tests passed");
}
