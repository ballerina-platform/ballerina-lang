// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

@personAnnot {
    id: 1,
    perm: {a: 1, b: 2}
}
public type AG string;

type T1 [@member int, @member int, @member AG];

function testTupleAnnot() {
    [@Annot string, @Annot int] thisOne = ["aaa", 2];
}

// utils
type Person record {|
    int id;
    map<int> perm;
|};

const annotation Person personAnnot on type;
const annotation member on field;
annotation Annot;
