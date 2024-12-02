// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

isolated function getAsInt(string s) returns int {
    return 777;
}

type Bar record {
    int a = getAsInt("777");
};

function recordDefaultFunctionWithArgument() returns Bar {
    return {};
}

isolated function returnString() returns string {
    return "hello";
}

type Foo record {
    string b = returnString();
};

function recordDefaultFunction () returns Foo {
    return {};
}
