 // Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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
 // KIND, either express or implied.  See the License for the
 // specific language governing permissions and limitations
 // under the License.

function testNeverRuntime10() {
    int x = 100;
    boolean _ = x is never;
}

type Record record {|
    int i;
    never[] j;
|};

function testNeverRuntime11() {
    Record x = {i: 1, j: []};
    boolean _ = x is never;
}

function testNeverFieldTypeCheck() {
    record {int x;} r2 = {x: 2, "color": "blue"};
    boolean _ = r2 is record {never x?;};

    record {never? x;} r3 = {x: (), "color": "blue"};
    boolean _ = r3 is record {never x?;};

    record {int? x;} r4 = {x: 2, "color": "blue"};
    boolean _ = r4 is record {never x?;};

    record {int x;} & readonly v3 = {x: 2, "color": "blue"};
    boolean _ = v3 is record {never x?;};

    record {never? x;} & readonly v4 = {x: (), "color": "blue"};
    boolean _ = v4 is record {never x?;};
}
