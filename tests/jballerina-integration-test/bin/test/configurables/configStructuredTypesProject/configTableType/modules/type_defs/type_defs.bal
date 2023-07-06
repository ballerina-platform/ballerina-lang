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

public type Student record {
    string name = "";
    int id = 444;
};

public type Employee readonly & record {
    string name = "";
    int id = 0;
};

public type Officer record {|
    readonly string name = "";
    readonly int id = 0;
|};

public type Person readonly & record {
    string name;
    int id;
    Address address;
};

public type Address record {
    string city;
    Country country = {};
};

public type Country record {
    string name = "SL";
};

public type Lecturer record {|
    string name;
    Department department1;
    Department department2?;
    readonly Department department3;
|};

public type Department readonly & record {|
    string name;
|};

public type Lawyer readonly & record {|
    string name;
    Place place1;
    Place place2?;
    readonly Place place3;
|};

public type Place record {|
    string city;
|};

type Staff record {|
    readonly int id;
    readonly string name = "Default";
    readonly float salary?;
|};

public type StaffTable table<Staff> key(id);
