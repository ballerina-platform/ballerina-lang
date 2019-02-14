// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

# Util function to assert the occurrence of a panic, and the error reason.
# Validates that a panic occurs and that the error has the reason specified as `expectedReason`,
# fails with the `invalidReasonFailureMessage` string if the reasons mismatch.
#
# + func - the function to call
# + expectedReason - the reason the error is expected to have
# + invalidReasonFailureMessage - the failure message on reason mismatch
public function assertPanic(function() returns any func, string expectedReason, string invalidReasonFailureMessage) {
    var result = trap func.call();
    if (result is error) {
        test:assertEquals(result.reason(), expectedReason, msg = invalidReasonFailureMessage);
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}
