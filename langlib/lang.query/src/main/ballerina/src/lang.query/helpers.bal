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

public function createPipeline(
        (any|error)[]|map<any|error>|record{}|string|xml|table<map<any|error>>|stream|_Iterable collection,
        typedesc<Type> resType)
            returns _StreamPipeline {
    return new _StreamPipeline(collection, resType);
}

public function createFromFunction(function(_Frame _frame) returns _Frame|error? fromFunc)
        returns _StreamFunction {
    return new _FromFunction(fromFunc);
}

public function createLetFunction(function(_Frame _frame) returns _Frame|error? letFunc)
        returns _StreamFunction {
    return new _LetFunction(letFunc);
}

public function createJoinFunction(_StreamPipeline joinedPipeline)
        returns _StreamFunction {
    return new _JoinFunction(joinedPipeline);
}

public function createFilterFunction(function(_Frame _frame) returns boolean filterFunc)
        returns _StreamFunction {
    return new _FilterFunction(filterFunc);
}

public function createSelectFunction(function(_Frame _frame) returns _Frame|error? selectFunc)
        returns _StreamFunction {
    return new _SelectFunction(selectFunc);
}

public function createDoFunction(function(_Frame _frame) doFunc) returns _StreamFunction {
    return new _DoFunction(doFunc);
}

public function addStreamFunction(@tainted _StreamPipeline pipeline, @tainted _StreamFunction streamFunction) {
    pipeline.addStreamFunction(streamFunction);
}

public function getStreamFromPipeline(_StreamPipeline pipeline) returns stream<any|error, error?> {
    return pipeline.getStream();
}

public function toArray(stream<Type, error?> strm) returns Type[]|error {
    Type[] arr = [];
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

public function consumeStream(stream<any|error, error?> strm) returns error? {
    any|error? v = strm.next();
    while (!(v is () || v is error)) {
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
}

// TODO: This for debugging purposes, remove once completed.
public function print(any|error? data) = external;

