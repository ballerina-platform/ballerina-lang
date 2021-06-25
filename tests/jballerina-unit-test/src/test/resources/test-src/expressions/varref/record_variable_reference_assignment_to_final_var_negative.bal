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

public function testAssigningValuesToFinalVars() {
    record {
        string s;
        int i;
        float f;
    } rec1 = {
        s: "hello",
        i: 2,
        f: 1.0
    };
    final Baz {s, i, f} = rec1;
    {s, i, f} = rec1;

    record {
        string s2;
        record {
            int i3;
            boolean b3;
        } r2;
        float f2;
    } rec2 = {
        s2: "hello",
        r2: {
            i3: 1,
            b3: true
        },
        f2: 1.0
    };
    final var {s2, r2: {i3: iv, b3}, ...m2} = rec2;
    {s2, r2: {i3: iv, b3}, ...m2} = rec2;
}

type Baz record {
    string s;
    int i;
    float f;
};
