// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class employee {

    public int age = 0;
    public string name = "";
    public string address = "";

    public function init (string name, int a = 10) {
        self.age = a;
        self.name = name;
    }

    public function getAge() {
        self.age = 12;
    }
}

// object with non-public initializer
public class student {
    public int age = 20;
    public string name = "";
    public string address = "";

    function init() {
    }

    public function getAge() {
        self.age = 20;
    }
}
