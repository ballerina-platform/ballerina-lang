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

type PersonRecord record {|
    readonly string name;
    int age = 0;
    string location?;
|};

type PersonObject object {
    public string fname;
    string lname;

    public function getFullName() returns string;
};

public class PersonClass {
    public final string fName = "Anonymous";
    private string lName;
    int age = 0;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

type BooleanSubtype readonly & record {|
    boolean value;
|};

function booleanSubtypeUnion(anydata d1, anydata d2) returns BooleanSubtype {
    BooleanSubtype v1 = <BooleanSubtype>d1;
    BooleanSubtype v2 = <BooleanSubtype>d2;
    return v1.value == v2.value ? v1 : true;
}
