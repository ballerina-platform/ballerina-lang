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

annotation W on type, class, service;

annotation map<int> X on record field;

annotation map<string> Y on object field;

annotation Z on field;

@W
class Bar {
    @Y {
        q: "hello",
        r: "world"
    }
    @Z
    int j = 1;
}

@W
service class Ser {
    @Z
    int j = 1;
}

public type HSC record {|

    string hostRecField = "default_host_name";
    boolean boolRecField = true;
|};

public annotation HSC HSCfa on field;

public annotation HSC HSCsa on service;

int glob = 2;

function getServiceTypeDesc(string hosty) returns typedesc<service object {}> {
    var httpService =
    @HSCsa {
        hostRecField: hosty
    }
    isolated service object {

        @HSCfa {
            hostRecField: hosty
        }
        final string xField = hosty;

        @HSCfa {
            hostRecField: hosty
        }
        final string yField = hosty;
    };

    return typeof httpService;
}

public function testStructureAnnots() returns [typedesc<object {}>, typedesc<service object {}>, typedesc<service object {}>] {
    glob = 123;

    Bar b = @W object {
        @Y {
            q: "hello",
            r: "world"
        }
        @Z
        int j = 1;
    };

    var bar = @W service object {
        @Y {
            q: "hello",
            r: "world"
        }
        @Z
        int j = 1;
    };

    var serTypeDesc = getServiceTypeDesc("closure key");

    return [typeof b, typeof bar, serTypeDesc];
}

