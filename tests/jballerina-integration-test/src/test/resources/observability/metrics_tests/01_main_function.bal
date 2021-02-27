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

import ballerina/observe;

public function main() returns error? {
    int a = 6;
    int b = 13;

    ObservableAdderClass adder = new ObservableAdder(a, b);
    var sumFromAdder = adder.getSum();

    var sumFromFunc = callCalculateSum(a, b);
    sumFromFunc = callCalculateSum(a, b);
    sumFromFunc = callCalculateSum(a, b);

    if (sumFromAdder != sumFromFunc) {  // Sanity check for sum
        error e = error("Sum from Adder Object (" + sumFromAdder.toString() + ") and sum from Function ("
            + sumFromFunc.toString() + ")");
        return e;
    }
}

function callCalculateSum(int a, int b) returns int {
    return calculateSumWithObservability(a, b);
}

@observe:Observable
function calculateSumWithObservability(int a, int b) returns int {
    var sum = a + b;
    return a + b;
}

type ObservableAdderClass object {
    @observe:Observable
    function getSum() returns int;
};

class ObservableAdder {
    private int firstNumber;
    private int secondNumber;

    function init(int a, int b) {
        self.firstNumber = a;
        self.secondNumber = b;
    }

    function getSum() returns int {
        return self.firstNumber + self.secondNumber;
    }
}
