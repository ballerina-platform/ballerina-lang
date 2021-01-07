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

@display {iconPath: "fooIconPath.icon", label: "Foo function"}
function foo(int i, string k) returns int {
    return i;
}

@display {iconPath: "barIconPath.icon", label: "Bar class"}
class Bar {
    @display {label: "k method"}
    function k() {

    }
}

@display {iconPath: "RecIcon.icon", label: "The main record!"}
type Rec record {|
    string s;
    int i;
|};

@display {iconPath: "service.icon", label: "service", misc: "Other info"}
service on new L() {
    @display {iconPath: "k-func.icon", label: "k method"}
    function k() {
    }

    @display {iconPath: "k.icon", label: "resource method"}
    function resource get k() {
    }
}

