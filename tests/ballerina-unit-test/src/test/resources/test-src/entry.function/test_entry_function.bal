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
import ballerina/io;

public function main() returns int {
    return 0;
}

public function noParamEntry() returns int {
    return 1;
}

public function intEntry(int i) {
    io:print(i);
}

function nonPublicEntry(int i) {
    io:print(i);
}

public function jsonEntry(json j) returns json {
    return j;
}

public function xmlEntry(xml x) returns xml {
    return x;
}

public function arrayUnionEntry(int[]|float[]|boolean[]|json[] arr) returns any {
    return arr;
}

public function typedescEntry(typedesc t) returns typedesc {
    return t;
}

public function tupleEntry((int, Employee, string) t) returns string {
    int id;
    Employee e;
    string dept;
    (id, e, dept) = t;

    return "Id: " + <string> id + ", Name: " + e.name + ", Dept: " + dept;
}

public function defaultableParamEntry(int i = 1, boolean b, string s = "default hello", string s2) returns string {
    return <string> i + " " + s + " world: " + s2 + " " + <string> b;
}

public function combinedTypeEntry(int i, float f, string s, byte b, boolean bool, json j, xml x, Employee e,
                                  string... args) returns string {
    string restArgs = "";
    foreach str in args {
        restArgs += str + " ";
    }

    return "integer: " + <string> i + ", float: " + f + ", string: " + s + ", byte: " + <string> <int> b
                + ", boolean: " + <string> bool + ", JSON Name Field: " + j.name.toString() + ", XML Element Name: "
                    + x.getElementName() + ", Employee Name Field: " + e.name + ", string rest args: " + restArgs;
}

public function oneDimensionalArrayEntry(int[] intArr, string[] strArr, float[] floatArr, boolean[] boolArr,
json[] jsonArr, Employee[] empArr) returns string {
    return "integer: " + intArr[1] + ", string: " + strArr[1] + ", float: " + floatArr[1]  + ", boolean: "
        + <string> boolArr[1] + ", json: " + jsonArr[1].toString()
        + ", Employee Name Field: " + empArr[1].name;
}

public function mapEntry(map<int> intMap, map<string> strMap, map<float> floatMap, map<boolean> boolMap,
                            map<json> jsonMap, map<Employee> empMap) returns string {
    return "integer: " + intMap.test + ", string: " + strMap.test + ", float: " + floatMap.test  + ", boolean: "
            + <string> boolMap.test + ", json: " + jsonMap.test.toString() + ", Test Employee Name Field: "
            + empMap.test.name;
}

public function oneSensitiveParamEntry(string s, @sensitive int i, float f = 3.0, json... j) returns int {
    return 1;
}

public function allSensitiveParamsEntry(@sensitive string s, @sensitive int i, @sensitive float f = 3.0,
                                        @sensitive Employee e, @sensitive json... j) returns int {
    return 1;
}

public type Employee record {
   string name;
};
