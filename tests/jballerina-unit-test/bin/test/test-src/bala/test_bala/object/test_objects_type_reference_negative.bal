// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import testorg/foo;

class Manager1 {
    string dpt = "HR";

    *foo:Employee1;

    function init(string name, int age=25) {
        self.name = name;
        self.age = age;
        self.salary = 3000.0;
    }

    public function getBonus(float ratio, int months=6) returns float {
        return self.salary*ratio*<float>months;
    }
}

class Manager2 {
    string dpt = "HR";

    *foo:Manager1;  // Referring a non-abstract object in a BALA
}

class NamedPerson {
    *foo:NormalPerson;
}

class Emp {
    *foo:Employee7;

    public function getEmploymentDuration() returns int {
        return 5;
    }
}

readonly class FrameImpl {
    *foo:Frame;
}
