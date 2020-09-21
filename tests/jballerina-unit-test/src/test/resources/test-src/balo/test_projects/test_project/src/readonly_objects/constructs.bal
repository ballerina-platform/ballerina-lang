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

public type Details record {|
    string name;
    int yob;
|};

public type Department record {
    string name = "IT";
};

public readonly class Employee {
    public Details details;
    public Department dept;
    public int id;

    public function init(Details & readonly details, Department & readonly dept, int id) {
        self.details = details;
        self.dept = dept;
        self.id = id;
    }

    public function getId() returns int {
        return self.id;
    }
}

public type Controller object {
    public function getValue() returns int;
};

public type Config record {|
    Controller & readonly c1;
    Controller c2;
    Controller c3;
|};

public readonly class DefaultController {
    public final string id = "default";
    public map<int>? & readonly mp = ();

    public function getValue() returns int {
        return 0;
    }
}

public readonly class CustomController {
    public string id;

    public function init(string id) {
        self.id = id;
    }

    public function getValue() returns int {
        return 120;
    }
}

public class MutableController {
    public string id = "mutable";

    public function getValue() returns int {
        return 200;
    }
}
