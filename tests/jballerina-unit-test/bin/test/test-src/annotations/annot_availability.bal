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

annotation W on type, class;
annotation map<int> X on record field;
annotation map<string> Y on object field;
annotation Z on field;

@W
type Foo record {
    string s;
    @Z
    @X {
        p: glob
    } int i;
};

@W
class Bar {
    @Y {
        q: "hello",
        r: "world"
    }
    @Z
    int j = 1;
}

@W
service class Ser {
    @Z
    int j = 1;
}

int glob = 2;

public function testStructureAnnots() returns [typedesc<record {}>, typedesc<object {}>, typedesc<service object {}>] {
    glob = 123;

    Foo f = {s: "", i: 1};
    Bar b = new;

    var bar = new Ser();

    return [typeof f, typeof b, typeof bar];
}
