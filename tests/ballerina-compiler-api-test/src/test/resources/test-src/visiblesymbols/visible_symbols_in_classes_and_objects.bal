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

class Vehicle {
    string vehicleType;

    function init(string vType) {
        self.vehicleType = vType;
    }

    function getType() returns string {
        var obj = object {
            string name = "";
            int age;

            function init() {
                self.age = 0;
                var s = self.generateName();
            }

            function generateName() returns string {
                return self.name;
            }
        };
        return self.vehicleType;
    }
}

function testObjectConstructor() {
    int y = 0;

    var obj = object {
        string name = "";
        int age;

        function init() {
            self.age = 0;
            var s = self.getName();
        }

        function getName() returns string {
            return self.name;
        }
    };
}
