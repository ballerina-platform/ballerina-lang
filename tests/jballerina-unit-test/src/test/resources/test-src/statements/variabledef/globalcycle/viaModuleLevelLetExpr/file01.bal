// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type SimpleType record {
    string? field1;
};

string fieldOne = let string? f1 = conf1.field1 in f1 is string ? f1 : "default-value-1";

SimpleType conf1 = {
    field1: fieldOne
};

string modVar = let string[] ar = [letRef] in ar[0];

type RecordTypeWithDefaultLetExpr record {
    int code = let int m = moduleCode in m + 1;
};
