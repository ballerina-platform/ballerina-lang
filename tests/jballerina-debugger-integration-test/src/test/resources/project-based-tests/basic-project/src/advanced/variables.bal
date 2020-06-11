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

type Student record {
    string name;
    int age;
    Grades grades;
};

type Address record {|
    string city;
    string country;
|};

type Grades record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};

type Employee record {
    int id;
    string name;
    float salary;
};

type Person object {
    public string name = "";
    public int age = 0;
    public Person? parent = ();
    private string email = "default@abc.com";
    string address = "No 20, Palm grove";
};

public type AnonPerson record {
    string name;
    int age;
    object {
        public string city;
        public string country;

        public function init(string city, string country) {
            self.city = city;
            self.country = country;
        }

        public function value() returns string {
            return self.city + ", " + self.country;
        }
    } address;
};

type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET"|"TIMER";

public function variables() {

    //------------------------ basic, simple types ------------------------//

    var varVariable = ();
    boolean booleanVar = true;
    int intVar = 20;
    float floatVar = -10.0;
    decimal decimalVar = 3;
    string stringVar = "foo";

    //------------------------ basic, structured types ------------------------//

    any[] arrayVar = [1, 20, -10.0, "foo"];
    [int, string] tupleVar = [20, "foo"];
    map<string> mapVar = {line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka"};

    Student john = {
        name: "John Doe",
        age: 20,
        grades: {
            maths: 80,
            physics: 75,
            chemistry: 65
        }
    };

    record {|string city; string country;|} anonRecord = {city: "London", country: "UK"};

    //table<Employee> tableVar = table {
    //    {key id, name, salary},
    //    [
    //        {1, "Mary", 300.5},
    //        {2, "John", 200.5},
    //        {3, "Jim", 330.5}
    //    ]
    //};

    xml xmlVar = xml `Hello, world!`;

    error errorVar = error("SimpleErrorType", message = "Simple error occurred");


    //------------------------ basic, behavioral types ------------------------//

    function (string, string) returns string anonFunctionVar =
        function (string x, string y) returns string {
        return x + y;
    };

    future<int> futureVar = start sum(40, 50);
    _ = wait futureVar;

    Person objectVar = new;

    AnonPerson anonObjectVar = {
        name: "John Doe",
        age: 25,
        address: new ("Colombo", "Sri Lanka")
    };

    //service serviceVar = service {
    //    resource function onMessage(string message) {
    //        io:println("Response received from server: " + message);
    //    }
    //    resource function onError(error err) {
    //        io:println("Error reported from server: " + err.reason() + " - " + <string>err.detail()["message"]);
    //    }
    //    resource function onComplete() {
    //        io:println("Server Complete Sending Responses.");
    //    }
    //};

    typedesc<int> typedescVar = int;


    //------------------------ Other types ------------------------//

    string|error unionVar = "foo";
    string? optionalVar = "foo";
    any anyVar = "foo";
    anydata anydataVar = "foo";
    byte byteVar = 128;
    json jsonVar = {name: "apple", color: "red", price: 40};
}

function sum(int a, int b) returns int {
    return a + b;
}
