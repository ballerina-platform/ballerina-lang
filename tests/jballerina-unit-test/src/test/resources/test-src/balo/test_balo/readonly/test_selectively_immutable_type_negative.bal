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

import testorg/selectively_immutable as se;

function testNonReadOnlyValueForReadOnlyCET() {
    se:MixedRecord & readonly rec1 = se:getMixedRecord();

    se:MixedRecord rec2 = se:getMixedRecord();
    map<json> & readonly mp = rec2.p;

    se:Details d = {
        name: "May",
        id: 1234
    };

    se:Employee & readonly emp = {
        details: d,
        department: "IT"
    };

    se:Student st = {
        details: {
            name: "May",
            id: 1234
        },
        "math": ["P", 80]
    };

    se:ReadOnlyStudent rs = st;
}

class Obj {
    int i;

    function init(int i) {
        self.i = i;
    }
}

type ABAny se:AB|any;

function testInvalidAssignmentToWideReadOnlyIntersection() {
    ABAny & readonly x = new Obj(1);
}

function testInvalidReaodOnlyRecordUpdates() {
    se:MixedRecord rec = se:getMixedRecord();
    rec.m.details = {
        name: "Anne",
        id: 1234
    };
    rec["m"]["details"] = {
        name: "Anne",
        id: 1234
    };

    se:Details & readonly details = {
        name: "Anne",
        id: 2345
    };

    details.name = "Jo";
    details["id"] = 1234;
}

function testInvalidReaodOnlyObjectUpdates() {
    var c1 = se:getImmutableConfig();
    c1.name = "new name";

    se:Config & readonly c2 = new se:MyConfig("client config");
    c2.name = "new name";

    se:MyConfig c3 = new se:MyConfig("client config");
    c3.name = "new name";
}
