// Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package internal;

import pkg1;

function testObjectInitFunctionWithDefaultableParams() returns [int, int, int, int, int] {
    pkg1:TempCache? cache1 = new();
    pkg1:TempCache? cache2 = new(10000, capacity = 10);
    pkg1:TempCache? cache3 = new(20000, evictionFactor = 0.1);
    pkg1:TempCache? cache4 = new(30000, capacity = 10, evictionFactor = 0.2);
    pkg1:TempCache? cache5 = new(expiryTimeInMillis = 40000, capacity = 20, evictionFactor = 0.3);

    if (cache1 is pkg1:TempCache && cache2 is pkg1:TempCache && cache3 is pkg1:TempCache && cache4 is pkg1:TempCache && cache5 is pkg1:TempCache) {
        return [cache1.expiryTimeInMillis, cache2.expiryTimeInMillis, cache3.expiryTimeInMillis, cache4.expiryTimeInMillis, cache5.expiryTimeInMillis];
    }
    return [0, 0, 0, 0, 0];
}

class InitObjOne {
    float f;

    public function init(float i) {
        self.f = i;
    }
}

class InitObjTwo {
    int f;
    public function init(int i) {
        self.f = i;
    }
}

function testObjectInitFunctionWithDefaultableParams2() returns [float, int] {
    InitObjOne|InitObjTwo|int f1 = new(1.1);
    InitObjOne|InitObjTwo|float f2 = new(1);

    if (f1 is InitObjOne && f2 is InitObjTwo) {
        return [f1.f, f2.f];
    }

    return [0.0, 0];
}
