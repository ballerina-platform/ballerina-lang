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

import testOrg/configLib.mod1 as configLib;

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
    County country = {};
};

public type County record {
    string name = "SL";
};

public type PersonArray Person;

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

public type IntMap map<int>;

public type StudentMap map<Student>;

public type Product record {
};

public type ProductArray Product[];

public type ProductMap map<Product>;

public type ProductTable table<Product>;

public type ProductMapTable map<table<Product>>;

public type Owner record {
    readonly int id;
};

public type OwnerArray Owner[];

public type OwnerMap map<Owner>;

public type OwnerTable table<Owner> key(id);

public type OwnerMapTable map<table<Owner> key(id)>;

public type Member readonly & record {
    int id;
};

public type MemberArray Member[];

public type MemberMap map<Member>;

public type MemberTable table<Member> key(id);

public type MemberMapTable map<table<Member> key(id)>;

public type Player record {|
    readonly int id;
    Player...;
|};

public type PlayerArray Player[];

public type PlayerMap map<Player>;

public type PlayerTable table<Player> key(id);

public type PlayerMapTable map<table<Player> key(id)>;


configurable int num3 = ?;
configurable string word4 = ?;

final int num1 = 1;
configurable int num2 = ?;

public type Numbers record {|
    int num1 = num1;
    int num2 = num2;
    int num3 = getNumber();
    int num4 = configLib:getNumber();
    int num5;
|};

public isolated function getWord() returns string {
    return word4;
}

public isolated function getNumber() returns int {
    return num3;
}

type Staff record {|
    readonly int id;
    readonly string name = "Default";
    readonly float salary?;
|};

public type StaffTable table<Staff> key(id);
