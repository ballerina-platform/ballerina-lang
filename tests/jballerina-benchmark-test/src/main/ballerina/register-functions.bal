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

// NOTE: Make sure to add the function name to benchmarkFunctions.txt as well.

public function registerSingleExecFunctions() {
    addSingleExecFunction("benchmarkLoopWithQuery", benchmarkLoopWithQuery);
    addSingleExecFunction("benchmarkLoopWithForeach", benchmarkLoopWithForeach);
    addSingleExecFunction("benchmarkLoopWithWhile", benchmarkLoopWithWhile);
    addSingleExecFunction("benchmarkNestedLoopWithQuery", benchmarkNestedLoopWithQuery);
    addSingleExecFunction("benchmarkNestedLoopWithForeach", benchmarkNestedLoopWithForeach);
    addSingleExecFunction("benchmarkNestedLoopWithWhile", benchmarkNestedLoopWithWhile);
    addSingleExecFunction("benchmarkJoinWithQuery", benchmarkJoinWithQuery);
    addSingleExecFunction("benchmarkJoinWithForeach", benchmarkJoinWithForeach);
    addSingleExecFunction("benchmarkJoinWithWhile", benchmarkJoinWithWhile);
}

public function registerMultiExecFunctions() {
    addMultiExecFunction("benchmarkStringJsonValueOf", benchmarkStringJsonValueOf);
    addMultiExecFunction("benchmarkStringLength", benchmarkStringLength);
    addMultiExecFunction("benchmarkStringLastIndexOf", benchmarkStringLastIndexOf);
    addMultiExecFunction("benchmarkStringSubstring", benchmarkStringSubstring);
    addMultiExecFunction("benchmarkStringToLower", benchmarkStringToLower);
    addMultiExecFunction("benchmarkStringToUpper", benchmarkStringToUpper);
    addMultiExecFunction("benchmarkStringTrim", benchmarkStringTrim);
    addMultiExecFunction("benchmarkStringIntValueOf", benchmarkStringIntValueOf);
    addMultiExecFunction("benchmarkStringFloatValueOf", benchmarkStringFloatValueOf);
    addMultiExecFunction("benchmarkStringBooleanValueOf", benchmarkStringBooleanValueOf);
    addMultiExecFunction("benchmarkStringValueOf", benchmarkStringValueOf);
    addMultiExecFunction("benchmarkStringHasPrefix", benchmarkStringHasPrefix);
    addMultiExecFunction("benchmarkStringHasSuffix", benchmarkStringHasSuffix);
    addMultiExecFunction("benchmarkStringIndexOf", benchmarkStringIndexOf);
    addMultiExecFunction("benchmarkStringEqualsIgnoreCase", benchmarkStringEqualsIgnoreCase);
    addMultiExecFunction("benchmarkStringConcat", benchmarkStringConcat);
    addMultiExecFunction("benchmarkFloatAddition", benchmarkFloatAddition);
    addMultiExecFunction("benchmarkFloatAdditionWithReturn", benchmarkFloatAdditionWithReturn);
    addMultiExecFunction("benchmarkFloatMultiplication", benchmarkFloatMultiplication);
    addMultiExecFunction("benchmarkFloatMultiplicationWithReturn", benchmarkFloatMultiplicationWithReturn);
    addMultiExecFunction("benchmarkFloatSubtraction", benchmarkFloatSubtraction);
    addMultiExecFunction("benchmarkFloatSubtractionWithReturn", benchmarkFloatSubtractionWithReturn);
    addMultiExecFunction("benchmarkFloatDivision", benchmarkFloatDivision);
    addMultiExecFunction("benchmarkFloatDivisionWithReturn", benchmarkFloatDivisionWithReturn);
}
