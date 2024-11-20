// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org).
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

type Person record {|
    int id;
    string fname;
    string lname;
|};

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

public function main() {

    // simple binding pattern
    var profession = "Software Engineer";

    // list binding pattern
    [int, [string, string]] [id, [firstName, _]] = getDetails();

    // mapping binding pattern
    string givenName;
    string surname;
    {fname: givenName, lname: surname} = getPerson();

    // error binding pattern
    var error(_, cause, code = code, reason = reason) = getSampleError();

    // binding patterns inside a match statement
    matchCommand(["Remove", "*", true]);
}

function getDetails() returns [int, [string, string]] {
    return [
        1234,
        ["John", "Doe"]
    ];
}

function getPerson() returns Person {
    Person person = {id: 1001, fname: "Anne", lname: "Frank"};
    return person;
}

function getSampleError() returns SampleError {
    return error("Transaction Failure", error("Database Error"), code = 20, reason = "deadlock condition");
}

function matchCommand(any commands) {
    match commands {
        var [show] => {
            string name = "show";
        }
        // The list binding pattern below binds lists that contain three list items
        // where the third element in the list is the boolean value `true`.
        var [remove, all, isDir] if isDir is true => {
            string name = "remove";
        }
        // The list binding pattern below binds lists that contain three list items.
        var [remove, all, _] => {
            string name = "remove";
        }
        // The list binding pattern below binds lists that contain two list items,
        // in which the second list item is also a list of two items.
        var [copy, [file1, file2]] => {
            string name = "copy";
        }
        _ => {
            string name = "unknown";
        }
    }
}
