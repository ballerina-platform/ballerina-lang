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

// Detail Types
type Detail record {
    string x;
};

type DetailTwo record {|
    int x?;
    string...;
|};

type DetailThree record {
    int x;
};

type DetailTypeFour map<string>;

// Error Types
type ErrorOne error<Detail>;

type ErrorTwo error<DetailTwo>;

type ErrorThree error<DetailThree>;

type ErrorFour error<record { int x; }>;

type ErrorFive error<DetailTypeFour>;

//Intersection Types
type IntersectionError ErrorOne & ErrorTwo;

type IntersectionErrorTwo ErrorOne & ErrorThree;

type IntersectionErrorThree ErrorOne & ErrorFour;

type IntersectionErrorFour ErrorOne & ErrorFive;

type DistinctErrorIntersection distinct ErrorOne & ErrorFive;

function testRecordAndMapIntersection() {
    var err = error IntersectionErrorFour("message", x = "x", z = 10);
    DistinctErrorIntersection err2 = error IntersectionErrorFour("message", x = "x");
}
