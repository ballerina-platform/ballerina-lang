// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

import ballerina/test;

public function testErrorUnionWithDefaultWorkerFunction() {
    Class c = new(10);
    test:assertEquals(10, checkpanic c.getId());
}

public class Class {
    private int id;

    public function init(int id) {
        self.id = id;
    }

    public function getId() returns int|error {
        worker w1 returns error? {
            self.id -> function;
        }
        return <- w1;
    }
}
