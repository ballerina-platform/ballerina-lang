// Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public type Marks record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};

public type Person record {
    string name;
    int age;
    Marks grades;
    string city;
};

public function getPerson() returns Person|error{
    Person Kamal = {
        name : "Kamal",
        age : 10,
        grades : {
            maths : 90,
            physics : 99,
            chemistry : 95
        },
        city : "Colombo"
    };

    return Kamal;
}

public function main() {
    Marks marks = check getPerson();
}
