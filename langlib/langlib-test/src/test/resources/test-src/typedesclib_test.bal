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

import ballerina/lang.'typedesc;

type Error distinct error;

type MyError distinct error;

type MyOtherError distinct (Error & error<record { string msg; }>);

public function testGeTypeIds() {
    var tids = MyError.typeIds();
    assertSingleTypeId(tids, "MyError");
    assertReadonlyness(tids);

    var eTids = Error.typeIds();
    assertSingleTypeId(eTids, "Error");
    assertReadonlyness(eTids);

    error e = error MyError("MyError");
    typedesc<error> tdesc = typeof e;
    typedesc:TypeId[]? ids = tdesc.typeIds();
    assertSingleTypeId(ids, "MyError");
    assertReadonlyness(ids);

    MyOtherError oe = error("Hello", msg = "msg");
    tids = (typeof oe).typeIds();
    var k = MyOtherError.typeIds();

    assertReadonlyness(tids);
}

function assertSingleTypeId(typedesc:TypeId[]? tids, string localId) {
    if (tids is typedesc:TypeId[]) {
        if (tids.length() != 1) {
            panic error("Assertion error: Expected length of one, found: " + tids.length().toString());
        }

        typedesc:TypeId tid = tids[0];
        if (tid.localId != localId) {
            panic error("Assertion error: Expected localId: MyError, found: " + tid.localId.toString());
        }
        assertReadonlyness(tid);
    } else {
        panic error("Assertion error: expected array of: " + typedesc:TypeId.toString() + "found: " + (typeof tids).toString());
    }
}

function assertReadonlyness(any a) {
    if !(a is readonly) {
        panic error("Assertion error: value is not a readonly value");
    }
}