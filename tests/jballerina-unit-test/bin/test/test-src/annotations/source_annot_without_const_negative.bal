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

annotation map<string> v0 on source var;
annotation Foo v1 on source annotation;
annotation Bar v2 on source const, type;
annotation map<anydata> v3 on return, source external;
annotation map<int> v4 on source const, source external;
annotation Foo v5 on source type;
annotation Bar v6 on source listener, return;
annotation Bar v7 on class, source function;
annotation Bar v8 on source object function, source parameter;
annotation map<string> v9 on source worker;

type Foo record {
    string val1;
};

type Bar record {|
    string val1;
    int val2;
|};
