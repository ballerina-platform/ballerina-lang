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

import testorg/foo as foo;

public class PartTimeEmployee1 {
    *foo:Employee3;

    int shares = 2000;
    float salary;

    function getBonus(float ratio, int months) returns float {
        return <float>(self.salary) * ratio * <float>months * <float>self.shares;
    }
}

public class PartTimeEmployee2 {
    *foo:Employee3;

    int shares = 2000;
    private float salary;

    private function getBonus(float ratio, int months) returns float {
        return <float>(self.salary) * ratio * <float>months * <float>self.shares;
    }
}

public class PartTimeEmployee3 {
    *foo:Employee4;

    float salary;
}

public class PartTimeEmployee4 {
    *foo:Employee5;

    function getBonus(float ratio, int months) returns float {
        return 1000.00 * ratio * <float>months;
    }
}

readonly service class FooClass {
    *foo:FooObj;

    isolated remote function execute(int aVar, int bVar) {
    }
}

readonly service class BarClass {
    *foo:FooObj;

    isolated remote function execute(string cVar, int dVar, int eVar) {
    }
}

readonly service class BazClass {
    *foo:FooObj;

    public isolated function execute(string aVar, int bVar) {
    }
}
