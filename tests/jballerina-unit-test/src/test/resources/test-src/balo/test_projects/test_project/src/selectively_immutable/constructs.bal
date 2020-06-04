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

import ballerina/lang.'xml;

public function getXmlComments() returns ['xml:Comment, 'xml:Comment] {
    'xml:Comment & readonly a = xml `<!--I'm a comment-->`;
    'xml:Comment b = xml `<!--I'm another comment-->`;
    return [a, b];
}

public function getXmlPIs() returns ['xml:ProcessingInstruction, 'xml:ProcessingInstruction] {
    'xml:ProcessingInstruction & readonly a = xml `<?pi a="1234" bc="def"?>`;
    'xml:ProcessingInstruction b = xml `<?pi a="4567" bc="def"?>`;
    return [a, b];
}

public function getXmlElements() returns ['xml:Element, 'xml:Element] {
    'xml:Element & readonly a = xml `<Student><name>Emma</name><id>6040</id></Student>`;
    'xml:Element b = xml `<Student><name>Jo</name><id>6040</id></Student>`;
    return [a, b];
}

public type Employee record {|
    Details details;
    string department;
|};

public type Details record {|
    string name;
    int id;
|};

public function getSelectivelyImmutableListTypes() returns [any, any, any, any] {
    int[] & readonly a = [1, 2];
    int[] b = [1, 2];

    Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [Employee, Employee] & readonly c = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    [Employee, Employee] d = [{details: {name: "Amy", id: 1234}, department: "IT"},
                              {details: {name: "Jo", id: 5678}, department: "IT"}];

    return [a, b, c, d];
}

public function getSelectivelyImmutableMappingTypes() returns [any, any, any, any, any] {
    boolean bool = false;

    map<boolean> & readonly a = {
        a: true,
        bool,
        c: false
    };

    map<int> b = {
        a: 1,
        b: 2
    };

    Employee & readonly c = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    Employee d = {
        details: {
            name: "Mary",
            id: 4567
        },
        department: "IT"
    };

    Student & readonly e = {
        details: {
            name: "Jo",
            id: 4567
        },
        "math": ["P", 75],
        "science": ["P", 65]
    };
    return [a, b, c, d, e];
}

public type Identifier record {|
    readonly string name;
    int id;
|};

public type RESULT "P"|"F";

public type Student record {|
    Details details;
    [RESULT, int]...;
|};
