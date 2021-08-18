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

type SingleDistinctError distinct error;
type SingleDistinctError2 distinct error;
type DoubleDistinctError distinct error & distinct error;
type DoubleDistinctError2 distinct error & distinct error;
type DistinctErrorAndSingleDistinctError distinct error & SingleDistinctError;
type DistinctErrorAndSingleDistinctError2 distinct error & SingleDistinctError;
type GroupedDistinctError distinct (distinct error & distinct SingleDistinctError);
type GroupedDoubleDistinctError distinct (distinct error & distinct SingleDistinctError & distinct SingleDistinctError);

function testDistinctErrorIntersectionAssignability() {
    SingleDistinctError se = error("Single");
    SingleDistinctError2 se2 = se; // incompatible types: expected 'SingleDistinctError2', found 'SingleDistinctError'
    DoubleDistinctError de = se; // incompatible types: expected 'DoubleDistinctError', found 'SingleDistinctError'
    se = de; //  incompatible types: expected 'SingleDistinctError', found 'DoubleDistinctError'
    DoubleDistinctError2 de2 = de; // incompatible types: expected 'DoubleDistinctError2', found 'DoubleDistinctError'

    DistinctErrorAndSingleDistinctError dse = error("DAndS");
    se = dse;
    dse = se; // incompatible types: expected 'DistinctErrorAndSingleDistinctError', found 'SingleDistinctError'

    DistinctErrorAndSingleDistinctError2 dde = se; // incompatible types: expected 'DistinctErrorAndSingleDistinctError2', found 'SingleDistinctError'
    se = dde;
    dse = dde; //  incompatible types: expected 'DistinctErrorAndSingleDistinctError', found 'DistinctErrorAndSingleDistinctError2'

    distinct error inlineDE = se; // incompatible types: expected 'error<map<ballerina/lang.value:1.0.0:Cloneable>>', found 'SingleDistinctError'
    se = inlineDE; // incompatible types: expected 'SingleDistinctError', found 'error'

    distinct SingleDistinctError dsde = error("dsde"); // todo: Create an issue to fix this, dsde = sde1 shold be an error.
    SingleDistinctError sde1 = dsde;
    dsde = sde1;

    se = error GroupedDistinctError("GroupedDistinctError");
    se = error GroupedDoubleDistinctError("GroupedDoubleDistinctError");
}
