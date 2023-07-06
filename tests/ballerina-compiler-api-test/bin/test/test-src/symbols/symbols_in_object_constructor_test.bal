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

function test() {
    var helloVar = client object {
        int item2 = 1;
        private string item1 = "";

        public function init() {
            self.item1 = "Foo";
            self.item2 = 10;
        }

        public isolated transactional function testFunction() returns int {
            int x = self.item2;
        }
    };

    var obj = @v1 object Person {
        string name = name;

        public function getName() returns string {
            return self.name;
        }
    };
}

// utils

string name = "John Doe";

annotation v1 on class;

type Person object {
    string name;

    public function getName() returns string;
};
