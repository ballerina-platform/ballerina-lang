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

class _IteratorObject {
    handle javaStrm;

    function init(handle javaItr) {
        self.javaStrm = javaItr;
    }
    public isolated function next() returns record {|Type value;|}|CompletionType {
        return nextJava(self.javaStrm);
    }
}

type nextRecord record {|Type value;|};

public isolated function nextJava(handle javaStrm) returns record {|Type value;|}|CompletionType = @java:Method {
        'class: "io.ballerina.runtime.internal.query.pipeline.IteratorObject",
        name: "next",
        paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline"]
} external;

function toStream(handle strm) returns stream<Type, CompletionType> = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "toStream",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline"]
} external;

function createPipeline(
        Type[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection,
        typedesc<Type> constraintTd, typedesc<CompletionType> completionTd, boolean isLazyLoading) returns handle {
    return createStreamPipeline(collection, constraintTd, completionTd, isLazyLoading);
}

function createStreamPipeline(
        Type[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection,
        typedesc<Type> constraintTd, typedesc<CompletionType> completionTd, boolean isLazyLoading) returns handle = @java:Method {
            'class: "io.ballerina.runtime.internal.query.pipeline.StreamPipeline",
            name: "initStreamPipeline",
            paramTypes: ["java.lang.Object","io.ballerina.runtime.api.values.BTypedesc","io.ballerina.runtime.api.values.BTypedesc","boolean"]
} external;

// function createStreamPipelineFromStream(stream<Type, CompletionType> strm, typedesc<Type> constraintTd, typedesc<CompletionType> completionTd, boolean isLazyLoading) returns handle = @java:Method {
//     'class: "io.ballerina.runtime.internal.query.pipeline.StreamPipeline",
//     name: "initStreamPipelineFromBStream",
//     paramTypes: ["java.lang.Object","io.ballerina.runtime.api.values.BTypedesc","io.ballerina.runtime.api.values.BTypedesc","boolean"]
// } external;

function createInputFunction(function(_Frame _frame) returns _Frame|error? inputFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.FromClause",
    name: "initFromClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createSelectFunction(function(_Frame _frame) returns _Frame|error? selectFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.SelectClause",
    name: "initSelectClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function addStreamFunction(handle pipeline, handle jClause) = @java:Method {
    'class: "io.ballerina.runtime.internal.query.pipeline.StreamPipeline",
    name: "addStreamFunction",
    paramTypes: ["java.lang.Object","java.lang.Object"]
} external;

function createFilterFunctionOld(function(_Frame _frame) returns boolean|error filterFunc)
        returns _StreamFunction {
    return new _FilterFunction(filterFunc);
}

function createFilterFunction(function(_Frame _frame) returns boolean|error filterFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.WhereClause",
    name: "initWhereClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createLimitFunctionOld(function (_Frame _frame) returns int limitFunction) returns _StreamFunction {
    return new _LimitFunction(limitFunction);
}

function createLimitFunction(function (_Frame _frame) returns int limitFunction) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.LimitClause",
    name: "initLimitClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createLetFunctionOld(function(_Frame _frame) returns _Frame|error? letFunc)
        returns _StreamFunction {
    return new _LetFunction(letFunc);
}

function createLetFunction(function(_Frame _frame) returns _Frame|error? letFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.LetClause",
    name: "initLetClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createOrderByFunctionOld(function(_Frame _frame) returns error? orderFunc)
        returns _StreamFunction {
    return new _OrderByFunction(orderFunc);
}

function createOrderByFunction(function(_Frame _frame) returns error? orderFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.OrderByClause",
    name: "initOrderByClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createGroupByFunctionOld(string[] keys, string[] nonGroupingKeys) returns _StreamFunction {
    return new _GroupByFunction(keys, nonGroupingKeys);
}

function createGroupByFunction(string[] keys, string[] nonGroupingKeys) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.GroupByClause",
    name: "initGroupByClause",
    paramTypes: ["io.ballerina.runtime.api.values.BArray","io.ballerina.runtime.api.values.BArray"]
} external;

function createCollectFunctionOld(string[] nonGroupingKeys, function(_Frame _frame) returns _Frame|error? collectFunc) returns _StreamFunction {
    return new _CollectFunction(nonGroupingKeys, collectFunc);
}

function createCollectFunction(string[] nonGroupingKeys, function(_Frame _frame) returns _Frame|error? collectFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.CollectClause",
    name: "initCollectClause",
    paramTypes: ["io.ballerina.runtime.api.values.BArray","io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function toArray(handle strm, Type[] arr, boolean isReadOnly) returns Type[]|error {
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

function createArrayOld(stream<Type, CompletionType> strm, Type[] arr) returns Type[]|error {
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

function createArray(handle strm, Type[] arr) returns Type[]|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createArray",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline","io.ballerina.runtime.api.values.BArray"]
} external;

function collectQueryOld(stream<Type, CompletionType> strm) returns Type|error {
    record {| Type value; |}|error? v = strm.next();
    return v is record {| Type value; |} ? v.value : v;
}

function collectQuery(handle strm) returns Type|error {
   Type|error v = collectQueryJava(strm);
   return v is record {| Type value; |} ? v.value : v;
}

function collectQueryJava(handle strm) returns Type|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "collectQuery",
    paramTypes: ["java.lang.Object"]
} external;

// function collectQuery(handle strm) returns Type|error = @java:Method {
//     'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
//     name: "collectQuery",
//     paramTypes: ["java.util.stream.Stream"]
// } external;

function consumeStreamOld(stream<Type, CompletionType> strm) returns any|error {
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

function consumeStream(handle javaPipeline) returns any|error {
    stream<Type, CompletionType> strm = toStream(javaPipeline);
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

// function consumeStreamJava(handle strm) returns any|error = @java:Method {
//     'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
//     name: "consumeStream",
//     paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline"]
// } external;

function createNestedFromFunctionOld(function(_Frame _frame) returns _Frame|error? collectionFunc)
        returns _StreamFunction {
    return new _NestedFromFunction(collectionFunc);
}

function createNestedFromFunction(function(_Frame _frame) returns _Frame|error? collectionFunc)
        returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.NestedFromClause",
    name: "initNestedFromClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createInnerJoinFunctionOld(
        _StreamPipeline joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction) returns _StreamFunction {
    return new _InnerJoinFunction(joinedPipeline, lhsKeyFunction, rhsKeyFunction);
}

function createInnerJoinFunction(
        handle joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.InnerJoinClause",
    name: "initInnerJoinClause",
    paramTypes: ["java.lang.Object","io.ballerina.runtime.api.values.BFunctionPointer","io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createOuterJoinFunctionOld(
        _StreamPipeline joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction, _Frame nilFrame) returns _StreamFunction {
    return new _OuterJoinFunction(joinedPipeline, lhsKeyFunction, rhsKeyFunction, nilFrame);
}

function createOuterJoinFunction(
        handle joinedPipeline,
        function (_Frame _frame) returns any lhsKeyFunction,
        function (_Frame _frame) returns any rhsKeyFunction) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.OuterJoinClause",
    name: "initOuterJoinClause",
    paramTypes: ["java.lang.Object","io.ballerina.runtime.api.values.BFunctionPointer","io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createOnConflictFunctionOld(function(_Frame _frame) returns _Frame|error? onConflictFunc)
        returns _StreamFunction => new _OnConflictFunction(onConflictFunc);


function createOnConflictFunction(function(_Frame _frame) returns _Frame|error? onConflictFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.OnConflictClause",
    name: "initOnConflictClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function createDoFunctionOld(function(_Frame _frame) returns any|error doFunc) returns _StreamFunction {
    return new _DoFunction(doFunc);
}

function createDoFunction(function(_Frame _frame) returns any|error doFunc) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.clauses.DoClause",
    name: "initDoClause",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function getStreamFromPipelineOld(_StreamPipeline pipeline) returns stream<Type, CompletionType> {
    return pipeline.getStream();
}

function getStreamFromPipeline(handle pipeline) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.pipeline.StreamPipeline",
    name: "getStreamFromPipeline",
    paramTypes: ["java.lang.Object"]
} external;

function getStreamForOnConflictFromPipelineOld(_StreamPipeline pipeline) returns stream<Type, CompletionType>
    => pipeline.getStreamForOnConflict();

function getStreamForOnConflictFromPipeline(handle pipeline) returns handle = @java:Method {
    'class: "io.ballerina.runtime.internal.query.pipeline.StreamPipeline",
    name: "getStreamFromPipeline",
    paramTypes: ["java.lang.Object"]
} external;

function toXMLOld(stream<Type, CompletionType> strm, boolean isReadOnly) returns xml|error {
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

function toXML(handle strm, boolean isReadOnly) returns xml|error {
    xml result = 'xml:concat();
    result = check createXML(strm);
    if isReadOnly {
        createImmutableValue(result);
    }

    return result;
}

function createXML(handle strm) returns xml|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createXML",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline"]
} external;

function toStringOld(stream<Type, CompletionType> strm) returns string|error {
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

function toString(handle strm) returns string|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "toString",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline"]
} external;


function addToTable(handle strm, table<map<Type>> tbl, boolean isReadOnly) 
    returns table<map<Type>>|error {
    if isReadOnly {
        // TODO: Properly fix readonly scenario - Issue lang/#36721
        // In this case tbl will be an immutable table. Therefore, we will create a new mutable table. Next, we will
        // pass the newly created table into createTableWithKeySpecifier() to add the key specifier details from the
        // original table variable (tbl). Then the newly created table variable will be populated using createTable()
        // and make it immutable with createImmutableTable().
        table<map<Type>> tempTbl = table [];
        table<map<Type>> tbl2 = createTableWithKeySpecifier(tbl, typeof(tempTbl));
        table<map<Type>> tempTable = check createTable(strm, tbl2);
        return createImmutableTable(tbl, tempTable.toArray());
    }
    return createTable(strm, tbl);
}

function createTableOld(stream<Type, CompletionType> strm, table<map<Type>> tbl) returns table<map<Type>>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while (v is record {| Type value; |}) {
        error? e = trap tbl.add(<map<Type>> checkpanic v.value);
        if (e is error) {
            tbl.put(<map<Type>> checkpanic v.value);
        }
        v = strm.next();
    }
    if v is error {
        return v;
    }
    return tbl;
}

function createTable(handle strm, table<map<Type>> tbl) returns table<map<Type>>|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createTable",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline","io.ballerina.runtime.api.values.BTable"]
} external;

function addToTableForOnConflict(handle strm, table<map<Type>> tbl, boolean isReadOnly) 
    returns table<map<Type>>|error {
    if isReadOnly {
        // TODO: Properly fix readonly scenario - Issue lang/#36721
        // In this case tbl will be an immutable table. Therefore, we will create a new mutable table. Next, we will
        // pass the newly created table into createTableWithKeySpecifier() to add the key specifier details from the
        // original table variable (tbl). Then the newly created table variable will be populated using createTable()
        // and make it immutable with createImmutableTable().
        table<map<Type>> tempTbl = table [];
        table<map<Type>> mutableTableRef = createTableWithKeySpecifier(tbl, typeof(tempTbl));
        _ = check createTableForOnConflict(strm, mutableTableRef);
        return createImmutableTable(tbl, mutableTableRef.toArray());
    }
    return createTableForOnConflict(strm, tbl);
}

function createTableForOnConflictOld(stream<Type, CompletionType> strm, table<map<Type>> tbl) 
    returns table<map<Type>>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while v is record {| Type value; |} {
        record {|Type v; error? err;|}|error value = v.value.ensureType();
        if value is error {
            return value;
        }
        map<Type> tblValue = check value.v.ensureType();
        error? e = trap tbl.add(tblValue);
        error? err = value.err;
        if e is error && err is error {
            return err;
        }
        if e is error && err is () {
            tbl.put(<map<Type>> checkpanic value.v);
        }
        v = strm.next();
    }
    return v is error ? v : tbl;
}

function createTableForOnConflict(handle strm, table<map<Type>> tbl) returns table<map<Type>>|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createTableForOnConflict",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline","io.ballerina.runtime.api.values.BTable"]
} external;

function addToMap(handle strm, map<Type> mp, boolean isReadOnly) returns map<Type>|error {
    if isReadOnly {
        // In this case mp will be an immutable map. Therefore, we will create a new mutable map and pass it to the
        // createMap() (because we can't update immutable map). Then it will populate the members into it and the
        // resultant map will be passed into createImmutableValue() to make it immutable.
        map<Type> mp2 = {};
        createImmutableValue(check createMap(strm, mp2));
        return mp2;
    }
    return createMap(strm, mp);
}

function createMapOld(stream<Type, CompletionType> strm, map<Type> mp) returns map<Type>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while v is record {| Type value; |} {
        [string, Type]|error value = trap (<[string, Type]> checkpanic v.value);
        if value !is error {
            string key = value[0];
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

function createMap(handle strm, map<Type> mp) returns map<Type>|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createMap",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline","io.ballerina.runtime.api.values.BMap"]
} external;

function addToMapForOnConflict(handle strm, map<Type> mp, boolean isReadOnly) 
    returns map<Type>|error {
    if isReadOnly {
        // In this case mp will be an immutable map. Therefore, we will create a new mutable map and pass it to the
        // createMap() (because we can't update immutable map). Then it will populate the members into it and the
        // resultant map will be passed into createImmutableValue() to make it immutable.
        map<Type> mp2 = {};
        createImmutableValue(check createMapForOnConflict(strm, mp2));
        return mp2;
    }
    return createMapForOnConflict(strm, mp);
}

function createMapForOnConflictOld(stream<Type, CompletionType> strm, map<Type> mp) returns map<Type>|error {
    record {| Type value; |}|CompletionType v = strm.next();
    while v is record {| Type value; |} {
        record {|Type v; error? err;|}|error value = v.value.ensureType();
        if value is error {
            return value;
        }
        [string, Type]|error keyValue = value.v.ensureType();
        if keyValue is error {
            return keyValue;
        }
        string key = keyValue[0];
        error? err = value.err;
        if mp.hasKey(key) && err is error {
            return err;
        }
        mp[key] = keyValue[1];
        v = strm.next();
    }
    return v is error ? v : mp;
}

function createMapForOnConflict(handle strm, map<Type> mp) returns map<Type>|error = @java:Method {
    'class: "io.ballerina.runtime.internal.query.utils.CollectionUtil",
    name: "createMapForOnConflict",
    paramTypes: ["io.ballerina.runtime.internal.query.pipeline.StreamPipeline","io.ballerina.runtime.api.values.BMap"]
} external;


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

# Prepare `error` as a distinct `Error`.
#
# + err - `error` instance
# + return - Prepared `Error` instance
public isolated function prepareQueryBodyError(error err) returns Error {
    Error queryError = error Error("", err);
    return queryError;
}

# Prepare `error` as a distinct `CompleteEarlyError`.
#
# + err - `error` instance
# + return - Prepared `CompleteEarlyError` instance
public isolated function prepareCompleteEarlyError(error err) returns CompleteEarlyError {
    CompleteEarlyError completeEarlyErr = error CompleteEarlyError("", err);
    return completeEarlyErr;
}

public isolated function getQueryErrorRootCause(error err) returns error {
    error? cause = error:cause(err);
    return cause is error ? cause : err;
}
