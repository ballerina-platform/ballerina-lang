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

public type IntRest record {|
    int ...;
|};

public type FloatRest record {|
    float ...;
|};

public type StringRest record {|
    string ...;
|};

public type BooleanRest record {|
    boolean ...;
|};

public type AnydataRest record {|
    anydata ...;
|};

public type Player record {|
    readonly int id;
    Player...;
|};

public type PlayerArray Player[];

public type PlayerMap map<Player>;

public type PlayerTable table<Player> key(id);

public type PlayerMapTable map<table<Player> key(id)>;

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
