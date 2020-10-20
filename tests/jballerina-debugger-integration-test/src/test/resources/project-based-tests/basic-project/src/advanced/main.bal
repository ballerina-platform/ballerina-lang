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

type '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_Student record {
    string '1st_name;
    int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{2324};
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
    readonly int id;
    string name;
    float salary;
};

type EmployeeTable table<Employee> key(id);

class 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{2324}_ƮέŞŢ {
    public string '1st_name = "John";
    public int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{2324} = 0;
    public 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{2324}_ƮέŞŢ? parent = ();
    string email = "default@abc.com";
    string address = "No 20, Palm grove";

    public function getSum(int a, int b) returns int {
        return a + b;
    }
}

public class Location {
   public string city;
   public string country;

   public function init(string city, string country) {
       self.city = city;
       self.country = country;
   }

   public function value() returns string {
       return self.city + ", " + self.country;
   }
}

public type AnonPerson object {
    public string '1st_name;
    public int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{2324};
    public 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{2324}_ƮέŞŢ? parent;
    string email;
    string address;
};

type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET"|"TIMER";

class OddNumberGenerator {
    int i = 1;

    public function next() returns record {|int value;|}|error? {
        self.i += 2;
        return {value: self.i};
    }
}

// constants
const gv01_nameWithoutType = "Ballerina";
const string gv02_nameWithType = "Ballerina";
const map<string> gv03_nameMap = {"name":"John"};
const gv04_nilWithoutType = ();
const () gv05_nilWithType = ();

// global variables
var gv06_stringValue = "Ballerina";
var gv07_decimalValue = 100.0d;
var gv08_byteValue = <byte>2;
var gv09_floatValue = 2.0;
json gv10_jsonVar = {name:"John", age:20};
var 'gv11_\ \/\:\@\[\`\{\~\u{2324}_IL = "IL with global var";

public function main() {
    //------------------------ basic, simple type variables ------------------------//

    var v01_varVariable = ();
    boolean v02_booleanVar = true;
    int v03_intVar = 20;
    float v04_floatVar = -10.0;
    decimal v05_decimalVar = 3.5;

    //------------------------ basic, sequence type variables ------------------------//

    string v06_stringVar = "foo";
    xml v07_xmlVar = xml `<person gender="male"><firstname>Praveen</firstname><lastname>Nada</lastname></person>`;

    //------------------------ basic, structured type variables ------------------------//

    any[] v08_arrayVar = [1, 20, -10.0, "foo"];
    [int, string] v09_tupleVar = [20, "foo"];
    map<string> v10_mapVar = {line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka"};

    '\ \/\:\@\[\`\{\~\u{2324}_123_ƮέŞŢ_Student v11_recordVar = {
        '1st_name: "John Doe",
        'Ȧɢέ_\ \/\:\@\[\`\{\~⌤: 20,
        grades: {
            maths: 80,
            physics: 75,
            chemistry: 65
        }
    };

    record {|string city; string country;|} v12_anonRecord = {city: "London", country: "UK"};

    EmployeeTable v25_tableVar = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Peter", salary: 750.0}
    ];

    error v13_errorVar = error("SimpleErrorType", message = "Simple error occurred");

    //------------------------ basic, behavioral type variables ------------------------//

    function (string, string) returns string v14_anonFunctionVar =
        function (string x, string y) returns string {
        return x + y;
    };

    future<int> v15_futureVar = start sum(40, 50);
    _ = wait v15_futureVar;

    'Person_\\\ \/\<\>\:\@\[\`\{\~\u{2324}_ƮέŞŢ v16_objectVar = new;

    AnonPerson v17_anonObjectVar = new 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{2324}_ƮέŞŢ();

    typedesc<int> v18_typedescVar = int;
    stream<int, error> v26_oddNumberStream = new stream<int, error>(new OddNumberGenerator());

    //------------------------ Other types ------------------------//

    string|error v19_unionVar = "foo";
    string? v20_optionalVar = "foo";
    any v21_anyVar = 15.0;
    anydata v22_anydataVar = 619;
    byte v23_byteVar = 128;
    json v24_jsonVar = {name: "apple", color: "red", price: 40};
    xml<never> v27_neverVar = <xml<never>> 'xml:concat();

    // quoted identifiers
    string 'v28_\ \/\:\@\[\`\{\~\u{2324}_var = "IL with special characters in var";
    string 'v29_üňĩćőđę_var = "IL with unicode characters in var";
    json 'v30_ĠĿŐΒȂɭ_\ \/\:\@\[\`\{\~\u{2324}_json = {};
}
