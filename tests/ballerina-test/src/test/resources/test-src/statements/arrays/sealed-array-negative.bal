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

function accessInvalidIndexOfSealedArray() returns int {
    int[5] sealedArray = [1, 2, 3, 4, 5];
    int result = sealedArray[5];
    return result;
}

function accessInvalidIndexOfSealedArrayUsingKeyword() returns string {
    sealed string[] sealedArray = ["1", "2", "3", "4", "5"];
    string result = sealedArray[5];
    return result;
}

function initializeInvalidSizedSealedArray() {
    boolean[4] sealedArray1 = [true, true, false];
    boolean[4] sealedArray2 = [true, true, false, false, true];
}

function assignValueToInvalidIndex() {
    int[5] sealedArray1;
    sealed int[] sealedArray2 = [1, 2, 3, 4, 5];
    sealedArray1[5] = 12;
    sealedArray2[5] = 12;
    sealed boolean[] x;
}

function arrayAssignments() {
    int[3] x1 = [3, 4, 5];
    int[] x2 = [1, 2, 3];
    int[3] x3 = x1;
    int[3] x4 = x2;
}

function functionParametersAndReturns() returns int {
    boolean[3] sealedArray = [true, false, false];

    string[2] returnedStrArray = mockFunction(sealedArray);

    return lengthof returnedStrArray;
}

function mockFunction(boolean[4] sealedArray) returns (string[]) {
    sealed string[] sealedStrArray = ["Sam", "Smith"];
    return sealedStrArray;
}
