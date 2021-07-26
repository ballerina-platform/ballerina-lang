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

public type GrantConfig ClientCredentialsGrantConfig|PasswordGrantConfig|RefreshTokenGrantConfig;

public type ClientCredentialsGrantConfig record {|
    int|string clientId;
    anydata clientSecret;
    ClientConfiguration clientConfig;
|};

public type PasswordGrantConfig record {|
    (string|int)[] password;
    ClientConfiguration clientConfig?;
|};

public type RefreshTokenGrantConfig record {|
    string token;
    float|int timeLimit;
    ClientConfiguration clientConfig;
|};

public type ClientConfiguration record {|
    HttpVersion httpVersion?;
    map<anydata> customHeaders;
|};

public type One 1;

public type Two 2.5;

public type Three "three";

