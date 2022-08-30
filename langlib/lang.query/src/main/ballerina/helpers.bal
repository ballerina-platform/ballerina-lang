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
import ballerina/jballerina.java;
import ballerina/lang.'error;

function createPipeline(
        Type[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection,
        typedesc<Type> constraintTd, typedesc<CompletionType> completionTd)
            returns _StreamPipeline {
    return new _StreamPipeline(collection, constraintTd, completionTd);
}

function createInputFunction(function(_Frame _frame) returns _Frame|error? inputFunc)
        returns _StreamFunction {
    return new _InputFunction(inputFunc);
}

function createNestedFromFunction(function(_Frame _frame) returns _Frame|error? collectionFunc)
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

function createFilterFunction(function(_Frame _frame) returns boolean|error filterFunc)
        returns _StreamFunction {
    return new _FilterFunction(filterFunc);
}

function createOrderByFunction(function(_Frame _frame) returns error? orderFunc)
        returns _StreamFunction {
    return new _OrderByFunction(orderFunc);
}

function createSelectFunction(function(_Frame _frame) returns _Frame|error? selectFunc)
        returns _StreamFunction {
    return new _SelectFunction(selectFunc);
}

function createDoFunction(function(_Frame _frame) returns any|error doFunc) returns _StreamFunction {
    return new _DoFunction(doFunc);
}

function createLimitFunction(function (_Frame _frame) returns int limitFunction) returns _StreamFunction {
    return new _LimitFunction(limitFunction);
}

function addStreamFunction(@tainted _StreamPipeline pipeline, @tainted _StreamFunction streamFunction) {
    pipeline.addStreamFunction(streamFunction);
}

function getStreamFromPipeline(_StreamPipeline pipeline) returns stream<Type, CompletionType> {
    return pipeline.getStream();
}

function toArray(stream<Type, CompletionType> strm, Type[] arr, boolean isReadOnly) returns Type[]|error {
    if isReadOnly {
        // In this case arr will be an immutable array. Therefore, we will create a new mutable array and pass it to the
        // createArray() (because we can't update immutable array). Then it will populate the elements into it and the
        // resultant array will be passed into createImmutableValue() to make it immutable.
        Type[] tempArr = [];
        createImmutableValue(check createArray(strm, tempArr));
        return tempArr;
    }
    return createArray(strm, arr);
}

function createArray(stream<Type, CompletionType> strm, Type[] arr) returns Type[]|error {
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

function toXML(stream<Type, CompletionType> strm, boolean isReadOnly) returns xml|error {
    xml result = 'xml:concat();
    record {| Type value; |}|CompletionType v = strm.next();
    while (v is record {| Type value; |}) {
        Type value = v.value;
        if (value is xml) {
            result = result + value;
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }

    if isReadOnly {
        createImmutableValue(result);
    }

    return result;
}

function toString(stream<Type, CompletionType> strm) returns string|error {
    string result = "";
    record {| Type value; |}|CompletionType v = strm.next();
    while (v is record {| Type value; |}) {
        Type value = v.value;
        if (value is string) {
            result += value;
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return result;
}

function addToTable(stream<Type, CompletionType> strm, table<map<Type>> tbl, error? err, boolean isReadOnly) returns table<map<Type>>|error {
    if isReadOnly {
        // TODO: Properly fix readonly scenario - Issue lang/#36721
        // In this case tbl will be an immutable table. Therefore, we will create a new mutable table. Next, we will
        // pass the newly created table into createTableWithKeySpecifier() to add the key specifier details from the
        // original table variable (tbl). Then the newly created table variable will be populated using createTable()
        // and make it immutable with createImmutableTable().
        table<map<Type>> tempTbl = table [];
        table<map<Type>> tbl2 = createTableWithKeySpecifier(tbl, typeof(tempTbl));
        table<map<Type>> tempTable = check createTable(strm, tbl2, err);
        return createImmutableTable(tbl, tempTable.toArray());
    }
    return createTable(strm, tbl, err);
}

function createTable(stream<Type, CompletionType> strm, table<map<Type>> tbl, error? err) returns table<map<Type>>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while (v is record {| Type value; |}) {
        error? e = trap tbl.add(<map<Type>> checkpanic v.value);
        if (e is error) {
            if (err is error) {
                return err;
            }
            tbl.put(<map<Type>> checkpanic v.value);
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return tbl;
}

function addToMap(stream<Type, CompletionType> strm, map<Type> mp, error? err, boolean isReadOnly) returns map<Type>|error {
// Here, `err` is used to get the expression of on-conflict clause
    if isReadOnly {
        // In this case mp will be an immutable map. Therefore, we will create a new mutable map and pass it to the
        // createMap() (because we can't update immutable map). Then it will populate the members into it and the
        // resultant map will be passed into createImmutableValue() to make it immutable.
        map<Type> mp2 = {};
        createImmutableValue(check createMap(strm, mp2, err));
        return mp2;
    }
    return createMap(strm, mp, err);
}

function createMap(stream<Type, CompletionType> strm, map<Type> mp, error? err) returns map<Type>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while (v is record {| Type value; |}) {
        [string, Type]|error value = trap (<[string, Type]> checkpanic v.value);
        if value !is error {
            string key = value[0];
            if mp.hasKey(key) && err is error {
                return err;
            }
            mp[key] = value[1];
        } else {
            return value;
        }

        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return mp;
}

function consumeStream(stream<Type, CompletionType> strm) returns any|error {
    any|error? v = strm.next();
    while (!(v is () || v is error)) {
        if (v is _Frame && v.hasKey("value") && v.get("value") != ()) {
            return v.get("value");
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
}

function createImmutableTable(table<map<Type>> tbl, Type[] arr) returns table<map<Type>> & readonly = @java:Method {
    'class: "org.ballerinalang.langlib.query.CreateImmutableType",
    name: "createImmutableTable"
} external;

function createTableWithKeySpecifier(table<map<Type>> tbl, typedesc tableType) returns table<map<Type>> = @java:Method {
    'class: "org.ballerinalang.langlib.query.CreateImmutableType",
    name: "createTableWithKeySpecifier"
} external;

function createImmutableValue(any mutableValue) = @java:Method {
    'class: "org.ballerinalang.langlib.query.CreateImmutableType",
    name: "createImmutableValue"
} external;

# Log and prepare `error` as a `Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `Error` instance
public isolated function prepareQueryBodyError(error err) returns Error {
    Error queryError = error Error("", err);
    return queryError;
}

# Log and prepare `error` as a `Error`.
#
# + message - Error message
# + err - `error` instance
# + return - Prepared `Error` instance
public isolated function prepareCompleteEarlyError(error err) returns CompleteEarlyError {
    CompleteEarlyError completeEarlyErr = error CompleteEarlyError("", err);
    return completeEarlyErr;
}

public isolated function unwrapQueryError(error err) returns error {
    error? cause = error:cause(err);
    return cause is error ? cause : err;
}
