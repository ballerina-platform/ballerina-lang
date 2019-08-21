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

type ClosedPerson record {|
    string name = "";
    int age = 0;
    Address address = {};
|};

type ClosedAddress record {|
    string street = "";
    string city = "";
|};

type Address record {
    string street = "";
    string city = "";
};

type ClosedFoo record {|
    string a = "";
    string b = "";
    string c = "";
    string d = "";
    string e = "";
|};

type ClosedGrades record {|
    int maths = 0;
    int physics = 0;
    int chemistry = 0;
|};

type ClosedBar record {|
    float x = 0.0;
    float y = 0.0;
    float z = 0.0;
|};

function testForeachWithClosedRecords() returns [string[], any[]] {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    string[] fields = [];
    any[] values = [];

    int i = 0;
    foreach var [f, v] in p.entries() {
        fields[i] = f;
        values[i] = v;
        i += 1;
    }

    return [fields, values];
}

function testForeachWithOpenRecords2() returns any[] {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    any[] values = [];

    int i = 0;
    foreach var v in p {
        values[i] = v;
        i += 1;
    }

    return values;
}

function testForeachOpWithClosedRecords() returns any[] {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};
    any[] values = [];

    p.forEach(function (any value) {
        values.push(value);
    });

    return values;
}

function testMapOpWithClosedRecords() returns map<any> {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};

    map<any> newp =  p.'map(function (any value) returns any {
           if value is string {
               return value.toLowerAscii();
           }
           return value;
        });

    return newp;
}

function testFilterOpWithClosedRecords() returns map<string> {
    ClosedFoo f = {a: "A", b: "B", c: "C", d: "D", e: "E"};

    map<string> newf = f.filter(function (string value) returns boolean {
        if (value == "A" || value == "E") {
            return true;
        }
        return false;
    });

    return newf;
}

function testReduceOpWithClosedRecords() returns int {
    ClosedFoo f = {a: "A", b: "B", c: "C", d: "D", e: "E"};
    return f.reduce(function (int count, string val) returns int { return count + 1; }, 0);
}

function testChainedOpsWithClosedRecords() returns map<string> {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> newf = f.'map(function (string value) returns string {
                    return value.toLowerAscii();
                })
                .filter(function (string value) returns boolean {
                    if (value != "aa" && value != "ee") {
                        return true;
                    }
                    return false;
                });

    return newf;
}

function testMapWithAllStringClosedRecord() returns map<string> {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> modFooMap = f.'map(function (string value) returns string {
        return value.toLowerAscii();
    });

    return modFooMap;
}

function testMapWithAllIntClosedRecord(int m, int p, int c) returns map<int> {
    ClosedGrades grades = {maths: m, physics: p, chemistry: c};

    map<int> adjGrades = grades.'map(function (int grade) returns int {
        return grade + 10;
    });

    return adjGrades;
}

function testMapWithAllFloatClosedRecord(float a, float b, float c) returns map<float> {
    ClosedBar bar = {x: a, y: b, z: c};

    map<float> modBar = bar.'map(function (float val) returns float {
        return val + 10;
    });

    return modBar;
}

function testFilterWithAllStringClosedRecord() returns map<string> {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> modFooMap = f.filter(function (string v) returns boolean {
        if (v == "AA" || v == "EE") {
            return true;
        }
        return false;
    });

    return modFooMap;
}

function testFilterWithAllIntClosedRecord() returns map<int> {
    ClosedGrades grades = {maths: 80, physics: 75, chemistry: 65};

    map<int> adjGrades = grades.filter(function (int grade) returns boolean {
        if (grade > 70) {
            return true;
        }
        return false;
    });

    return adjGrades;
}

function testFilterWithAllFloatClosedRecord(float a, float b, float c) returns map<float> {
    ClosedBar bar = {x: a, y: b, z: c};

    map<float> modBar = bar.filter(function (float val) returns boolean {
        if (val > 6) {
            return true;
        }
        return false;
    });

    return modBar;
}

function testTerminalOpsOnAllIntClosedRecord(int m, int p, int c) returns [int, int, int, int, float] {
    ClosedGrades grades = {maths: m, physics: p, chemistry: c};

    int count = grades.reduce(function (int count, int v) returns int { return count + 1; }, 0);
    int max = grades.reduce(function (int max, int v) returns int { return v > max ? v : max; }, 0);
    int min = grades.reduce(function (int min, int v) returns int { return v < min ? v : min; }, 1000000);
    int sum = grades.reduce(function (int sum, int v) returns int { return sum + v; }, 0);
    float avg = grades.reduce(function (float avg, int v) returns float { return avg + <float>v / grades.length(); }, 0.0);

    return [count, max, min, sum, avg];
}

function testTerminalOpsOnAllIntClosedRecord2(int m, int p) returns [int, int, int, int, float] {
    ClosedGrades grades = {maths: m, physics: p};

    int count = grades.reduce(function (int count, int v) returns int { return count + 1; }, 0);
    int max = grades.reduce(function (int max, int v) returns int { return v > max ? v : max; }, 0);
    int min = grades.reduce(function (int min, int v) returns int { return v < min ? v : min; }, 1000000);
    int sum = grades.reduce(function (int sum, int v) returns int { return sum + v; }, 0);
    float avg = grades.reduce(function (float avg, int v) returns float { return avg + <float>v / grades.length(); }, 0.0);

    return [count, max, min, sum, avg];
}

function testChainedOpsWithClosedRecords2() returns map<float> {
    ClosedGrades f = {maths: 80, physics: 75, chemistry: 65};

    map<float> m = f.'map(function (int grade) returns int {
        return grade + 10;
    })
    .'map(function (int grade) returns string {
        if (grade > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter(function (string status) returns boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .'map(function (string status) returns float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });

    return m;
}

function testOpChainsWithTerminalOps(int m, int p, int c) returns [int, int, int, int, float] {
    ClosedGrades f = {maths: m, physics: p, chemistry: c};

    int count = f.'map(mapTo).filter(filter).reduce(function (int count, int v) returns int { return count + 1; }, 0);
    int sum = f.'map(mapTo).filter(filter).reduce(function (int sum, int v) returns int { return sum + v; }, 0);
    int max = f.'map(mapTo).filter(filter).reduce(function (int max, int v) returns int { return v > max ? v : max; }, 0);
    int min = f.'map(mapTo).filter(filter).reduce(function (int min, int v) returns int { return v < min ? v : min; }, 100000);
    int length = f.'map(mapTo).filter(filter).length();
    float avg = f.'map(mapTo).filter(filter).reduce(function (float avg, int v) returns float { return avg + <float>v / length; }, 0.0);

    return [count, sum, max, min, avg];
}

function mapTo(int grade) returns int {
    return grade + 10;
}

function filter(int grade) returns boolean {
    if (grade > 75) {
        return true;
    }
    return false;
}

function testMutability() returns ClosedGrades {
    ClosedGrades grades = {maths: 80, physics: 75, chemistry: 65};

    map<float> m = grades.'map(function (int grade) returns int {
        return grade + 10;
    })
    .'map(function (int grade) returns string {
        if (grade > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter(function (string status) returns boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .'map(function (string status) returns float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });

    return grades;
}
