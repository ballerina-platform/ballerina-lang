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

type Student record {
    readonly string name;
    readonly int id?;
};

function testInvalidUpdateOfRecordWithSimpleReadonlyFields() {
    Student st = {
        name: "Maryam"
    };

    st.name = "Mary";
    st["name"] = "Jo";

    // Should fail at runtime.
    string str = "name";
    st[str] = "Amy";
}

type Employee record {
    readonly Details details;
    string department;
};

type Details record {
    string name;
    int id;
};

function testRecordWithStructuredReadonlyFields() {
    Details details = {
        name: "Kim",
        id: 1000
    };

    Employee e = {
        details,
        department: "finance"
    };

    e.details = details;
    e.details.name = "Jo";
    e.details["id"] = 400;

    // Should fail at runtime.
    string str = "details";
    e[str] = details;
}

type Customer record {
    readonly string name;
    int id;
};

function testInvalidUpdateOfReadonlyFieldInUnion() {
    Customer customer = {
        name: "Jo",
        id: 1234
    };

    Student|Customer sd = customer;
    sd.name = "May"; // invalid
    sd.id = 4567; // valid
}
