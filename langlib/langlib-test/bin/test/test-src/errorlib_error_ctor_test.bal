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

function testErrorCause() {
    error cause = error("This is the cause");
    error e = error("This is a wrapper", cause);
    error f = error("This wrap the wrapper", e);

    error? fCause = <error> f.cause();
    error? eCause = e.cause();
    error? causeCause = cause.cause();

    if (fCause !== e) {
        panic error("Assertion failure: expected error cause of `f` to be ref equal to `e`");
    }

    if (eCause !== cause) {
        panic error("Assertion failure: expected error cause of `e` to be ref equal to `cause`");
    }

    if !(causeCause is ()) {
        panic error("Assertion failure: expected error cause of `cause` to be `()`");
    }
}

function testErrorDestructureWithCause() {
    error cause = error("This is the cause");
    error e = error("This is a wrapper", cause);
    var error(message, dCause) = e;

    if (dCause !== cause) {
        panic error("Assertion failure: expected destructured error cause of `e` to be ref equal to `cause`");
    }
}
