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

import evaluation_tests.other2;

public const publicConstant = "Ballerina";
public const publicInt = 10;
const string constant = "Ballerina";
public const map<string> constMap = {"name": "John"};

public string publicModuleVariable = "public";
string privateModuleVariable = "private";

type Student record {
    string firstName;
    string lastName;
    int intakeYear;
    float score;
};

public type Kid record {
    string firstName;
    string lastName;
    int intakeYear;
    float score;
    other2:Kid friend;
};

public client class Child {

    remote function getName(string firstName, string lastName = "") returns string|error {
        return firstName + lastName;
    }

    remote function getTotalMarks(int maths, int english) returns int {
        future<int> futureSum = @strand {thread: "any"} start sum(maths, english);
        int|error result = wait futureSum;
        if result is int {
            return result;
        } else {
            return -1;
        }
    }
}

public function getSum(int a, int b) returns int {
    future<int> futureSum = @strand {thread: "any"} start addition(a, b);
    int|error result = wait futureSum;
    if result is int {
        return result;
    } else {
        return -1;
    }
}

function addition(int a, int b) returns int {
    return a + b;
}

class 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ {
    public string '1st_name = "John";
    public int 'Ȧɢέ_\ \/\:\@\[\`\{\~\u{03C0} = 0;
    public 'Person_\\\ \/\<\>\:\@\[\`\{\~\u{03C0}_ƮέŞŢ? parent = ();
    string email = "default@abc.com";
    string address = "No 20, Palm grove";

    public function getSum(int a, int b) returns int {
        future<int> futureSum = @strand {thread: "any"} start addition(a, b);
        int|error result = wait futureSum;
        if result is int {
            return result;
        } else {
            return -1;
        }
    }
}

public class Place {
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

class Location {
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

public function sum(int a, int b) returns int {
    return a + b;
}
