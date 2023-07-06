// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

annotation map<string> v0 on var;
const annotation Foo v1 on annotation;
annotation Bar v2 on const, type;
const annotation map<anydata> v3 on return, external;
const annotation map<anydata> v4 on listener;

type Foo record {
    string val1;
};

type Bar record {|
    string val1;
    int val2;
|};
