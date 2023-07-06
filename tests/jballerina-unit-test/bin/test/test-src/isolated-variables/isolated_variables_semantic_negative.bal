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

type IsolatedObject isolated object { int a; };

class NonIsolatedObject {
    int a = 1;
}

isolated class IsolatedNonMatchingClass {
    final string[] & readonly a = ["hello", "world"];
    private boolean b;

    isolated function init(boolean b) {
        self.b = b;
    }
}

isolated IsolatedObject v1 = new NonIsolatedObject();

isolated IsolatedObject v2 = new IsolatedNonMatchingClass(true);

isolated final object { }  obj;

isolated class IsolatedClass {
    private int i = 1;
}

function init() {
    lock {
        obj = new IsolatedClass();
    }
}
