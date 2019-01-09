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

public type FooRecord record {
    string fooFieldOne;
    !...
};

public type BarRecord record {
    int barFieldOne;
    !...
};

public type BazRecord record {
    float bazFieldOne;
};

public type BazRecordTwo record {
    float bazFieldOne;
    string bazFieldTwo;
};

public type BazRecordThree record {
    string|float bazFieldOne;
};

public type FooObject object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};

public type BarObject object {
    public int barFieldOne;

    public function __init(int barFieldOne) {
        self.barFieldOne = barFieldOne;
    }

    public function getBarFieldOne() returns int {
        return self.barFieldOne;
    }
};

public type BazObject object {
    public BarObject bazFieldOne;
    public BarObject? bazFieldTwo = ();
    public BazObject? bazFieldThree = ();

    public function __init(BarObject bazFieldOne) {
        self.bazFieldOne = bazFieldOne;
    }

    public function getBazFieldOne() returns BarObject {
        return self.bazFieldOne;
    }
};

# Util method expected to be used with the result of a trapped expression. 
# Validates that `result` is of type `error` and that the error has the reason specified as `expectedReason`,
# and fails with the `invalidReasonFailureMessage` string if the reasons mismatch.
# 
# + result - the result of the trapped expression
# + expectedReason - the reason the error is expected to have
# + invalidReasonFailureMessage - the failure message on reason mismatch
public function assertErrorReason(any|error result, string expectedReason, string invalidReasonFailureMessage) {
    if (result is error) {
        test:assertEquals(result.reason(), expectedReason, msg = invalidReasonFailureMessage);
    } else {
        test:assertFail(msg = "expected expression to panic");
    }
}
