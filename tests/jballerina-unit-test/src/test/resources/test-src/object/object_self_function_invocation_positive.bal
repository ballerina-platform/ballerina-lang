// Copyright (c) 2026 WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

class C {
    int n;
    private int pending;

    function init(int n) {
        self.n = n;
    }
}

function take(C value) {
}

function testUninitializedPrivateFieldPassedAsSelf() {
    C c = new C(1);
    take(c);
}

class E {
    int n;
    private int pending;

    function init(int n) {
        self.n = n;
        self.helper();
    }

    function helper() {
        take(self);
    }
}

function testUninitializedPrivateFieldPassedAsSelfFromNestedMethod() {
    E _ = new E(1);
}
