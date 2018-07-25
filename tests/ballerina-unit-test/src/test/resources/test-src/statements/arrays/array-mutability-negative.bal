// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Super Type
type Person record {
    string name,
    !...
};

// Assignable to Person type
type Employee record {
    string name,
    boolean intern,
};

function mismatchingCovariace() {
    Employee[] x1;
    Person[] x2 = x1;

    Person[] x3;
    Employee[] x4 = x3; // Compile Error

    Employee[][] x5;
    Person[][] x6 = x5;

    Person[][] x7;
    Employee[][] x8 = x7; // Compile Error

    Employee[][] x9;
    Person[] x10 = x9; // Compile Error
    Person[][][] x11 = x9; // Compile Error

    (int|Person?)[] x12;
    (int|Person|boolean?)[] x13 = x12;
    (int|Person)[] x14 = x12; // Compile Error
}
