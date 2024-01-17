// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/jballerina.java;
import ballerina/test;

enum Status {
    OPEN,
    CLOSE
}

enum Language {
    ENG = "English",
    TL = "Tamil",
    SI = "Sinhala"
}

enum Race {
    ENG = "English",
    TL = "Tamil",
    SI = "Sinhala"
}

const R = "RED";
const G = "GREEN";
const B = "BLUE";

type Color R|G|B;

public function validateAPI() {
    testEnumArray();
    testFiniteTypeGetName();
}

function testFiniteTypeGetName() {
    testFiniteTypeUnionElements(Language, ["ENG", "TL", "SI"]);
    testFiniteTypeUnionElements(Race, ["ENG", "TL", "SI"]);
    testFiniteTypeUnionElements(Color, ["R", "G", "B"]);
}

function testFiniteTypeUnionElements(typedesc<anydata> t, string[] elements) = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Enums"
} external;

function testEnumArray() {
    Status[] enumArray = createEnumArray("Status");
    addToEnumArray(enumArray, "OPEN");
    test:assertEquals(enumArray.pop(), OPEN);
    enumArray.push(<Status> CLOSE);
    test:assertEquals(enumArray.pop(), CLOSE);
}

function createEnumArray(string s) returns Status[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Enums"
} external;

function addToEnumArray(Status[] statusArray, string s) = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Enums"
} external;
