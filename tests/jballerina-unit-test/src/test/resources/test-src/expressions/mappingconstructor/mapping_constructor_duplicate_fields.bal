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

type myRecord record {|
    int field\[ = 5;
    never field\{?;
|};

function testDuplicateFieldWithEscapeSequence() {
    map<int> _ = {a\\: 454, "a\\": 543};
    map<int> _ = {"a\\": 454, a\\: 543};

    map<int> _ = {a\u{5C}: 454, a\\: 543};
    map<int> _ = {a\u{5C}: 454, "a\\": 543};

    map<int> _ = {a\\: 454, "a\u{5C}": 543};
    map<int> _ = {a\{: 454, "a\u{7B}": 543};

    myRecord recVar1 = {};
    map<int> _ = {...recVar1, "field[": 543};
    map<int> _ = {"field[": 543, ...recVar1};

    int field\( = 6;
    map<int> _ = {field\(, "field(": 34};
}
