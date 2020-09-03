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

type Details record {|
    string name;
    int yob;
|};

type Department record {
    string name = "IT";
};

readonly class Employee {
    Details details;
    Department dept;
    int id;

    function init(Details details, Department dept, int id) {
        self.details = details;
        self.dept = dept;
        self.id = id;
    }

    function invalidUpdate() {
        self.id = 2345;
        self.details.yob = 1998;
    }
}

readonly class Controller {
    map<int|float> config = {
        quota: 10,
        factor: 2.0
    };

    function getConfig() returns map<int|float> {
        return self.config;
    }
}

function testInvalidReadOnlyIntersection() {
    Controller & readonly x = new;
}

readonly class InvalidReadOnlyObject {
    int i = 1;
    future<int> f;

    function init(future<int> g) {
        self.f = g;
    }
}
