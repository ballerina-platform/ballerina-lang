// Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

type Address record {|
    string city;
    string country;
|};

type Supplier record {
    Person supplier_details;
    string title;
    string food;
    string email;
    int id;
    Address supplier_address;
};

type User record {|
    Person user;
    string email;
    int ID;

|};

type Person record {
    string name;
    int age;
    int id;
};

public function main() {
    Supplier s = {
        supplier_details: {
            name: "Amala",
            age: 35,
            id: 1234
        },
        title: "Mr.",
        food: "Rice",
        email: "abc@gmail.com",
        id: 1234,
        supplier_address:{
            city: "Gampaha",
            country: "Sri Lanka"
        }
    };

    User user_1 = s;

}
