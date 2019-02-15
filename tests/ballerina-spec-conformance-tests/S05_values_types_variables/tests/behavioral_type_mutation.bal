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

public type BazObjectOne object {
    public int bazOneFieldOne;

    public function __init(int bazOneFieldOne) {
        self.bazOneFieldOne = bazOneFieldOne;
    }

    public function getbazOneFieldOne() returns int {
        return self.bazOneFieldOne;
    }
};

public type BazObjectTwo object {
    public BazObjectOne bazTwoFieldOne;
    public BazObjectOne? bazTwoFieldTwo = ();
    public BazObjectTwo? bazTwoFieldThree = ();

    public function __init(BazObjectOne bazTwoFieldOne) {
        self.bazTwoFieldOne = bazTwoFieldOne;
    }

    public function getbazTwoFieldOne() returns BazObjectOne {
        return self.bazTwoFieldOne;
    }
};

// Whether a behavioural value is mutable depends on its basic type: some of the behavioural basic types
// allow mutation, and some do not. Mutation cannot change the basic type of a value.
@test:Config {}
function testBehaviouralBasicTypeMutation() {
    BazObjectOne b1 = new(100);
    BazObjectTwo b2 = new(b1);
    b1.bazOneFieldOne = I;
    test:assertEquals(b2.bazTwoFieldOne.bazOneFieldOne, I, msg = "expected object member to have been updated");

    BazObjectOne b3 = new(100);
    BazObjectTwo b4 = new(b3);
    b4.bazTwoFieldTwo = b3;
    test:assertTrue(b4.bazTwoFieldOne === b4.bazTwoFieldTwo, msg = "expected values to be at the same location");
}
