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

type Person record {|
   string name;
   int age;
   map<string> address;
   [string, int] contact_person;
|};

type Student record {
   int id;
   string name;
   int age;
   map<string> address?;
   [string, int] contact_person?;
};

public function main() {
    Student john = {
        id: 0,
        name: "John",
        age: 20,
        address: {city: "colombo", country: "sri lanka"},
        contact_person: ["Jane", 7205589218]
    };

    Person person = john;
}
