//  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

int a = 5;
final string b = "MyString";
string _ = "This is wildcard binding pattern";
[boolean, float, string] [c, d, e] = [true, 2.25, "Dulmina"];
[boolean, float] f = [true, 0.4];

type Person record {
    string name;
    int age;
    string country;
};

type SampleErrorData record {
    string message?;
    error cause?;
    string info;
    boolean fatal;
};

type SampleError error<SampleErrorData>;

// mapping binding pattern with record type desc
Person {name: firstName, age: personAge, ...otherDetails} = getPerson();
// error binding pattern
var error(reason, info = info, fatal = fatal) = getSampleError();

function getPerson() returns Person {
    Person person = {
        name: "Dulmina",
        age: 26,
        country: "Sri Lanka",
        "occupation": "Software Engineer"
    };
    return person;
}

function getSampleError() returns SampleError {
    SampleError e = SampleError("Sample Error", info = "Detail Msg", fatal = true);
    return e;
}
