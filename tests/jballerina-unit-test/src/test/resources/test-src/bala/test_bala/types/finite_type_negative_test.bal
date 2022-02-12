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

import finitetypetest/finite_type_project;

function testNullFiniteType() {
    finite_type_project:Bar _ = "null"; // error

    finite_type_project:IntOrNull a = 1; // OK
    finite_type_project:IntOrNullStr b = 1; // OK

    int|null c = 1;
    int|"null" d = 1;
    null e = null;
    "null" f = "null";

    finite_type_project:IntOrNull _ = null; // OK
    finite_type_project:IntOrNull _ = (); // OK
    finite_type_project:IntOrNull _ = "null"; // error
    finite_type_project:IntOrNull _ = a; // OK
    finite_type_project:IntOrNull _ = b; // error
    finite_type_project:IntOrNull _ = c; // OK
    finite_type_project:IntOrNull _ = d; // error
    finite_type_project:IntOrNull _ = e; // OK
    finite_type_project:IntOrNull _ = f; // error

    finite_type_project:IntOrNullStr _ = null; // error
    finite_type_project:IntOrNullStr _ = (); // error
    finite_type_project:IntOrNullStr _ = "null"; // OK
    finite_type_project:IntOrNullStr _ = a; // error
    finite_type_project:IntOrNullStr _ = b; // OK
    finite_type_project:IntOrNullStr _ = c; // error
    finite_type_project:IntOrNullStr _ = d; // OK
    finite_type_project:IntOrNullStr _ = e; // error
    finite_type_project:IntOrNullStr _ = f; // OK
}
