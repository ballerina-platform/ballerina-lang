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

@Greeting {
    salutation: "Hey🧑🏿‍✈️"
}
type MyRecord record {

};

public function testAnnotation() returns int {
    MyRecord rec = {};
    typedesc<any> t = typeof rec;
    return t.@Greeting?.salutation.toString().length();
}

public type HelloConfiguration record {|
    string salutation = "Hello!";
|};

public annotation HelloConfiguration Greeting on type;
