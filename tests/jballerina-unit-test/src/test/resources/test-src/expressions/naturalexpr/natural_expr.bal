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

import ballerina/jballerina.java;

final natural:Generator generator = new Generator();

# Represents a person who plays a sport.
type SportsPerson record {|
    # First name of the person
    string firstName;
    # Last name of the person
    string lastName;
    # Year the person was born
    int yearOfBirth;
    # Sport that the person plays
    string sport;
|};

function getPopularSportsPerson(string nameSegment, int decadeStart)
      returns SportsPerson|error? => natural (generator) {
    Who is a popular sportsperson that was born in the decade starting
    from ${decadeStart} with ${nameSegment} in their name?
};

isolated client class Generator {
    remote isolated function generate(
            natural:Prompt prompt, typedesc<anydata> td) returns td|error = @java:Method {
                'class: "org.ballerinalang.test.expressions.natural.NaturalExpressionTest",
                name: "generateProxy"
            } external;

    public isolated function generateData('natural:Prompt prompt, typedesc<anydata> td) returns anydata|error {
        if td !is typedesc<SportsPerson?> {
            return error("Unexpected type: " + td.toString());
        }

        string[] & readonly strings = prompt.strings;
        anydata[] insertions = prompt.insertions;

        if strings != [string `    Who is a popular sportsperson that was born in the decade starting
    from `, " with ", string ` in their name?
`] || insertions != [1990, "Simone"] {
            return error("Unexpected prompt");
        }

        return <SportsPerson> {
            firstName: "Simone",
            lastName: "Biles",
            yearOfBirth: 1997,
            sport: "Gymnastics"
        };
    }
}

function testNaturalExpr() {
    SportsPerson? person = checkpanic getPopularSportsPerson("Simone", 1990);
    assertTrue({
        firstName: "Simone",
        lastName: "Biles",
        yearOfBirth: 1997,
        sport: "Gymnastics"
    }, person);
}

isolated function assertTrue(anydata expected, anydata actual) {
    if expected != actual {
        panic error(string `Expected ${expected.toString()}, found ${actual.toString()}`);
    }
}
