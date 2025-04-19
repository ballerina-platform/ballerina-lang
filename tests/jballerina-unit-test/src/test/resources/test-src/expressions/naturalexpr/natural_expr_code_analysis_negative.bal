// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

function fn() returns string[]|error {
    int mdl1;
    int mdl2 = 1;
    string day1 = "2025-04-16";
    string day2;
    return natural (
            mdl1, // error
            mdl2 // OK
        ) {
        What days were ${
            day1 // OK
        } and ${
            day2 // error
        }?
    };
}

type Blog record {|
    string title;
    string content;
|};

string[] categories = [];
final int[] & readonly ratings = [1, 2, 3];

type IsolatedObject isolated object {};

final IsolatedObject finalIsolatedModel = object {};
final object {} nonIsolatedModel = object {};

isolated function categorize(Blog blog) returns string|error? => natural (
        finalIsolatedModel, // OK
        nonIsolatedModel // error
    ) {
    Select a suitable category for this blog from ${
        categories // error
    } and a rating out of ${
        ratings // OK
    }
};

type Person record {
    string firstName;
    string lastName;
};

string val = check natural (<record { int id; }> {
        id: 1,
        "kind": "basic", // OK
        name: "default" // error
    }) {
        Describe this person in one sentence.

        ${<Person> {
                firstName: "Mary",
                lastName: "Anne",
                interests: ["photography"], // error
                "age": 30 // OK
            }
        }
    };
