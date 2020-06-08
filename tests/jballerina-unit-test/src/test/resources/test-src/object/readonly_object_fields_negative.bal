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

type Student object {
    readonly string name;
    readonly int id;
    
    function __init(string n, int i) {
        self.name = n;
        self.id = i;
    }
};

function testInvalidUpdateOfObjectWithSimpleReadonlyFields() {
    Student st = new ("Maryam", 5);

    st.name = "Mary";
}

type Employee object {
    readonly Details details;
    string department;

    function __init(Details & readonly details, string department) {
        self.details = details;
        self.department = department;
    }
};

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredReadonlyFields() {
    Details details = {
        name: "Kim",
        id: 1000
    };

    Employee e = new (details, "finance");

    e.details = details;
    e.details.name = "Jo";
}

type Customer object {
    readonly string name;
    int id;

    function __init(string n, int i) {
        self.name = n;
        self.id = i;
    }
};

function testInvalidUpdateOfReadonlyFieldInUnion() {
    Customer customer = new ("Jo", 1234);

    Student|Customer sd = customer;
    sd.name = "May";
}
