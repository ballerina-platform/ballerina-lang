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
import ballerina/lang.'xml;

function createPipeline(
        (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection,
        typedesc<Type> resType)
            returns _StreamPipeline {
    return new _StreamPipeline(collection, resType);
}

function createInputFunction(function(_Frame _frame) returns _Frame|error? inputFunc)
        returns _StreamFunction {
    return new _InputFunction(inputFunc);
}

function createNestedFromFunction(function(_Frame _frame) returns any|error? collectionFunc)
        returns _StreamFunction {
    return new _NestedFromFunction(collectionFunc);
}

function createLetFunction(function(_Frame _frame) returns _Frame|error? letFunc)
        returns _StreamFunction {
    return new _LetFunction(letFunc);
}

function createInnerJoinFunction(
        _StreamPipeline joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction) returns _StreamFunction {
    return new _InnerJoinFunction(joinedPipeline, lhsKeyFunction, rhsKeyFunction);
}

function createOuterJoinFunction(
        _StreamPipeline joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction, _Frame nilFrame) returns _StreamFunction {
    return new _OuterJoinFunction(joinedPipeline, lhsKeyFunction, rhsKeyFunction, nilFrame);
}

function createFilterFunction(function(_Frame _frame) returns boolean filterFunc)
        returns _StreamFunction {
    return new _FilterFunction(filterFunc);
}

function createOrderByFunction(function(_Frame _frame) orderFunc)
        returns _StreamFunction {
    return new _OrderByFunction(orderFunc);
}

function createSelectFunction(function(_Frame _frame) returns _Frame|error? selectFunc)
        returns _StreamFunction {
    return new _SelectFunction(selectFunc);
}

function createDoFunction(function(_Frame _frame) doFunc) returns _StreamFunction {
    return new _DoFunction(doFunc);
}

function createLimitFunction(int lmt) returns _StreamFunction {
    return new _LimitFunction(lmt);
}

function addStreamFunction(@tainted _StreamPipeline pipeline, @tainted _StreamFunction streamFunction) {
    pipeline.addStreamFunction(streamFunction);
}

function getStreamFromPipeline(_StreamPipeline pipeline) returns stream<Type, error?> {
    return pipeline.getStream();
}

function sortStream(stream<Type, error?> strm, @tainted Type[] arr, int lmt) returns stream<Type, error?> {
    Type[] streamValArr = [];
    record {| Type value; |}|error? v = strm.next();
    while (v is record {| Type value; |}) {
        streamValArr.push(v.value);
        v = strm.next();
    }

    StreamOrderBy streamOrderByObj = new StreamOrderBy();
    var sortedArr = <@untainted>streamOrderByObj.topDownMergeSort();

    int i = 0;
    int k = 0;
    // Add the sorted stream values to arr
    foreach var e in sortedArr {
        if (!(lmt == 0) && (k == lmt)) {
            break;
        }
        int j = 0;
        while (j < streamValArr.length()) {
            if (e is anydata[]) {
                if (streamValArr[j] is map<anydata>|string|xml) {
                    if (e[e.length()-1] == <map<anydata>|string|xml>streamValArr[j]) {
                        arr[i] = streamValArr[j];
                        var val = streamValArr.remove(j);
                        i += 1;
                    }
                }
            }
            j += 1;
        }
        k += 1;
    }

    return arr.toStream();
}

function toArray(stream<Type, error?> strm, Type[] arr) returns Type[]|error {
    record {| Type value; |}|error? v = strm.next();
    while (v is record {| Type value; |}) {
        arr.push(v.value);
        v = strm.next();
    }
    if (v is error) {
        return v;
    }

    return arr;
}

function toXML(stream<Type, error?> strm) returns xml {
    xml result = 'xml:concat();
    record {| Type value; |}|error? v = strm.next();
    while (v is record {| Type value; |}) {
        Type value = v.value;
        if (value is xml) {
            result = result + value;
        }
        v = strm.next();
    }
    return result;
}

function toString(stream<Type, error?> strm) returns string {
    string result = "";
    record {| Type value; |}|error? v = strm.next();
    while (v is record {| Type value; |}) {
        Type value = v.value;
        if (value is string) {
            result += value;
        }
        v = strm.next();
    }
    return result;
}

function addToTable(stream<Type, error?> strm, table<map<Type>> tbl, error? err) returns table<map<Type>>|error {
    record {| Type value; |}|error? v = strm.next();
    while (v is record {| Type value; |}) {
        error? e = trap tbl.add(<map<Type>> v.value);
        if (e is error) {
            if (err is error) {
                return err;
            }
            return e;
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return tbl;
}

function consumeStream(stream<Type, error?> strm) returns error? {
    any|error? v = strm.next();
    while (!(v is () || v is error)) {
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
}

// Check whether a float is NaN
function checkNaN(float x) returns boolean = external;

// TODO: This for debugging purposes, remove once completed.
function print(any|error? data) = external;
