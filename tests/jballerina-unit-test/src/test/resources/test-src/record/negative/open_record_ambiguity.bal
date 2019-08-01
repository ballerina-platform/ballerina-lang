// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type InMemoryModeConfig record {
    string name = "";
    string username = "";
    string password = "";
    map<any> dbOptions = {};
};

public type ServerModeConfig record {
    string host;
    int port;
    *InMemoryModeConfig;
};

public type EmbeddedModeConfig record {
    string path;
    *InMemoryModeConfig;
};

function testAmbiguityResolution() returns [string, string, string] {
    string s1 = init({});
    string s2 = init({host:"localhost", port:9090});
    string s3 = init({path:"localhost:9090"});
    return [s1, s2, s3];
}

function init(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig rec) returns string {
    if (rec is ServerModeConfig) {
        return "Server mode configuration";
    } else if (rec is EmbeddedModeConfig) {
        return "Embedded mode configuration";
    } else {
        return "In-memory mode configuration";
    }
}

public type A record {
    string a;
    string b;
    string c;
};

public type B record {
    string f;
    int g?;
    *A;
};

public type C record {
    string i;
    *A;
};

function testAmbiguityResolution2() returns [string, string, string, string] {
    string s1 = resolve({a:"", b:"", c:""});
    string s2 = resolve({a:"", b:"", c:"", f:""});
    string s3 = resolve({a:"", b:"", c:"", f:"", g:0});
    string s4 = resolve({a:"", b:"", c:"", i:""});
    return [s1, s2, s3, s4];
}

function resolve(A|B|C rec) returns string {
    if (rec is A) {
        return "A";
    } else {
        return "B|C";
    }
}
