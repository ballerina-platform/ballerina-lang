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

public type Manager record {|
    string name;
    int id;
|} ;

public type Teacher readonly & record {
    string name = "";
    int id = 0;
};

public type Farmer record {|
    readonly string name = "";
    readonly int id = 0;
|};

public enum HttpVersion {
    HTTP_1_1,
    HTTP_2
}

public type IntMap map<int>;
public type ManagerMap map<Manager>;

configurable int num4 = ?;
configurable string word5 = ?;

final string symbol1 = "!";
configurable string symbol2 = ?;
configurable string symbol3 = ?;

public type Symbols record {|
    string symbol1 = symbol1;
    string symbol2 = symbol2;
    string symbol3 = getSymbol();
    string symbol4;
|};

public isolated function getWord() returns string {
    return word5;
}

public isolated function getNumber() returns int {
    return num4;
}

isolated function getSymbol() returns string {
    return symbol3;
}

public type Person record {
    string name;
    Address address = {};
};

public type Address record {|
    Country country = {};
|};

public type Country record {
    string name = "LK";
};
