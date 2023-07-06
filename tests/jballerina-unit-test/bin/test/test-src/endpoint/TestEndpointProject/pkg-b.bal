// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import endpointproject.mod.ab;

function testCheck () returns error? {
    var a = testCheckFunction();

    if (a is error) {
        if (a.message() == "i1") {
            return;
        }
        panic error("Expected error message: , found: i1" + a.message());
    }
    panic error("Expected error, found: " + (typeof a).toString());
}

function testCheckFunction () returns error? {
    ab:DummyEndpoint dummyEp = ab:getDummyEndpoint();
    check dummyEp->invoke1("foo");
    return ();
}

function testNewEP(string a) {
    ab:DummyEndpoint ep1 = new;
    string r = ep1->invoke2(a);

    if (r != "donedone") {
        panic error("Expected: donedone, found: " + r);
    }
}
