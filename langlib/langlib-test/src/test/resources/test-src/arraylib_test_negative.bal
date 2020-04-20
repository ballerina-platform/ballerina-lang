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

function testPushOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.push(4);
}

function testPopOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int popped = fixedLengthArray.pop();
}

function testPopOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.pop();
}

function testShift() returns [int[], int] {
    int[] s = [1, 2, 3, 4, 5];
    var e = s.shift();
    return [s, e];
}

function testShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    int x = fixedLengthArray.shift();
}

function testUnShiftOnFixedLengthArray() {
    int[1] fixedLengthArray = [1];
    fixedLengthArray.unshift(5);
}

function testShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    int popped = fixedLengthTuple.shift();
}

function testUnShiftOnFixedLengthTuple() {
    [int, int] fixedLengthTuple = [1, 2];
    fixedLengthTuple.unshift();
}

// inferred fixed length arrays
function testPushPopShiftUnshitOnInferredFixedLengthArray() {
    int[*] fixedLengthArray = [1, 2];
    fixedLengthArray.push(4);
    int x = fixedLengthArray.pop();
    x = fixedLengthArray.shift();
    fixedLengthArray.unshift();
}

function testPushOnFixedLengthArrayUnions() {
    int[1] | float[1] fixedLengthArray = <int[1]> [1];
    fixedLengthArray.push(4);
}

function testPushOnFixedLengthTupleUnion() {
    [int, int][1] | [float, float][1] fixedLengthArray = <[float, float][1]> [[1.0, 2.3]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

function testShiftOnTupleWithStringRestParamFixedInherantShapeWithInt() {
    [int, string...] a = [1, "hello","world"];
    int | string x = a.shift();
}

function testShiftOnTupleWithIntRestParamFixedInherantShapeWithIntString() {
    [int, string, int...] a = [1, "hello", 5];
    int | string x = a.shift();
}

// run time panic no compile time error
function testPushOnFixedLengthAndDynamicTupleUnion() {
    [int, int][1] | [float, float][] fixedLengthArray = <[float, float][1]> [[1, 2]];
    fixedLengthArray.push(<[float, float]>[1, 2]);
}

function testPushOnFixedLengthTupleUnions() {
    [int, int] | [float, float] fixedLengthArray = <[float, float]> [1, 2];
    fixedLengthArray.push(<float>1);
}

// run time panic no compile time error
function testShiftOnTupleWithIntRestParamFixedInherantShapeWithInt() {
    [int, int...] a = [1];
    int x = a.shift();
}

// run time panic no compile time error
function testShiftOnUnionOfDifferentTuples() {
    [string, string...]|[string, int] tuples = ["hi", 4];
    var x = tuples.shift();
}

function testShiftOnUnionOfDifferentTuplesTypeNarrowed() {
    [string, string...]|[string, int] tuples = ["hi", 4];
    if (tuples is [string, int]) {
        var x = tuples.shift();
    }
}
