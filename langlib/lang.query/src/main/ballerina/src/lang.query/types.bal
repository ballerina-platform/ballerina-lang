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

import ballerina/lang.__internal as internal;
import ballerina/lang.'array as lang_array;
import ballerina/lang.'map as lang_map;
import ballerina/lang.'string as lang_string;
import ballerina/lang.'xml as lang_xml;
import ballerina/lang.'stream as lang_stream;
import ballerina/lang.'table as lang_table;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type any|error;

# A type parameter that is a subtype of `error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type ErrorType error?;

anydata[][] orderFieldValsArr = [];
boolean[] orderDirectionsArr = [];

type _Iterator abstract object {
    public function next() returns record {|Type value;|}|error?;
};

type _Iterable abstract object {
    public function __iterator() returns
        abstract object {
            public function next() returns record {|Type value;|}|error?;
        };
};

type _StreamFunction abstract object {
    public _StreamFunction? prevFunc;
    public function process() returns _Frame|error?;
    public function reset();
};

type _Frame record {|
    (any|error|())...;
|};

type _StreamPipeline object {
    _StreamFunction streamFunction;
    typedesc<Type> resType;

    function init(
            (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection,
            typedesc<Type> resType) {
        self.streamFunction = new _InitFunction(collection);
        self.resType = resType;
    }

    public function next() returns _Frame|error? {
        _StreamFunction sf = self.streamFunction;
        return sf.process();
    }

    public function reset() {
        _StreamFunction sf = self.streamFunction;
        sf.reset();
    }

    function addStreamFunction(_StreamFunction streamFunction) {
        _StreamFunction existingFunc = self.streamFunction;
        streamFunction.prevFunc = existingFunc;
        self.streamFunction = streamFunction;
    }

    public function getStream() returns stream <Type, error?> {
        object {
            public _StreamPipeline pipeline;
            public typedesc<Type> outputType;

            function init(_StreamPipeline pipeline, typedesc<Type> outputType) {
                self.pipeline = pipeline;
                self.outputType = outputType;
            }

            public function next() returns record {|Type value;|}|error? {
                _StreamPipeline p = self.pipeline;
                _Frame|error? f = p.next();
                if (f is _Frame) {
                    Type v = <Type>f["$value$"];
                    // Add orderKey and orderDirection values to respective arrays.
                    if ((!(f["$orderKey$"] is ())) && (!(f["$orderDirection$"] is ()))) {
                        anydata[] orKey = <anydata[]>f["$orderKey$"];
                        // Need to keep the stream value to sort the stream.
                        orKey.push(<anydata>v);
                        orderFieldValsArr.push(orKey);
                        orderDirectionsArr = <boolean[]>f["$orderDirection$"];
                    }
                    return internal:setNarrowType(self.outputType, {value: v});
                } else {
                    return f;
                }
            }
        } itrObj = new (self, self.resType);
        var strm = internal:construct(self.resType, itrObj);
        return strm;
    }
};

type _InitFunction object {
    *_StreamFunction;
    _Iterator? itr;
    boolean resettable = true;
    (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection;

    function init(
            (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection) {
        self.prevFunc = ();
        self.itr = ();
        self.collection = collection;
        self.itr = self._getIterator(collection);
    }

    public function process() returns _Frame|error? {
        _Iterator i = <_Iterator>self.itr;
        record {|(any|error) value;|}|error? v = i.next();
        if (v is record {|(any|error) value;|}) {
            record {|(any|error)...;|} _frame = {...v};
            return _frame;
        }
        return v;
    }

    public function reset() {
        if (self.resettable) {
            self.itr = self._getIterator(self.collection);
        } else {
            panic error("Unable to reset", message = "cannot reset an already consumed iterator.");
        }
    }

    function _getIterator(
            (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection)
                returns _Iterator {
        if (collection is (any|error)[]) {
            return lang_array:iterator(collection);
        } else if (collection is record {}) {
            return lang_map:iterator(collection);
        } else if (collection is map<any|error>) {
            return lang_map:iterator(collection);
        } else if (collection is string) {
            return lang_string:iterator(collection);
        } else if (collection is xml) {
            return lang_xml:iterator(collection);
        } else if (collection is table<map<any|error>>) {
            return lang_table:iterator(collection);
        } else if (collection is _Iterable) {
            return collection.__iterator();
        } else {
            // stream.iterator() is not resettable.
            self.resettable = false;
            return lang_stream:iterator(collection);
        }
    }
};

type _InputFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # from var { firstName: nm1, lastName: nm2 } in personList
    #   frame {nm1: firstName, nm2: lastName}
    # from var dept in deptList
    #   frame {dept: deptList[x]}
    public function (_Frame _frame) returns _Frame|error? inputFunc;

    function init(function (_Frame _frame) returns _Frame|error? inputFunc) {
        self.inputFunc = inputFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns _Frame|error? f = self.inputFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            _Frame|error? cFrame = f(pFrame);
            return cFrame;
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _NestedFromFunction object {
    *_StreamFunction;
    _Iterator? itr;

    public function (_Frame frame) returns any|error? collectionFunc;
    _Frame|error? currentFrame;

    function init(function (_Frame frame) returns any|error? collectionFunc) {
        self.itr = ();
        self.prevFunc = ();
        self.currentFrame = ();
        self.collectionFunc = collectionFunc;
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # from var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame frame) returns any|error? collectionFunc = self.collectionFunc;
        _Frame|error? cf = self.currentFrame;
        _Iterator? itr = self.itr;
        if (cf is ()) {
            cf = pf.process();
            self.currentFrame = cf;
            if (cf is _Frame) {
                any|error? collection = collectionFunc(cf);
                if (collection is any) {
                    itr = self._getIterator(collection);
                    self.itr = itr;
                }
            }
        }
        if (cf is _Frame && itr is _Iterator) {
            record {|(any|error) value;|}|error? v = itr.next();
            if (v is record {|(any|error) value;|}) {
                _Frame _frame = {...cf, ...v};
                return _frame;
            } else if (v is error) {
                return v;
            } else {
                // Move to next frame
                self.currentFrame = ();
                return self.process();
            }
        }
        return cf;
    }

    public function reset() {
        // Reset the state of currentFrame
        self.itr = ();
        self.currentFrame = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }

    function _getIterator(any collection) returns _Iterator {
        if (collection is (any|error)[]) {
            return lang_array:iterator(collection);
        } else if (collection is record {}) {
            return lang_map:iterator(collection);
        } else if (collection is map<any|error>) {
            return lang_map:iterator(collection);
        } else if (collection is string) {
            return lang_string:iterator(collection);
        } else if (collection is xml) {
            return lang_xml:iterator(collection);
        } else if (collection is table<map<any|error>>) {
            return lang_table:iterator(collection);
        } else if (collection is _Iterable) {
            return collection.__iterator();
        } else if (collection is stream <any|error, error?>) {
            return lang_stream:iterator(collection);
        }
        panic error("Unsuppored collection", message = "unsuppored collection type.");
    }
};

type _LetFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # let Company companyRecord = { name: "WSO2" }
    #   frame { companyRecord: { name: "WSO2" }, ...prevFrame }
    public function (_Frame _frame) returns _Frame|error? letFunc;

    function init(function (_Frame _frame) returns _Frame|error? letFunc) {
        self.letFunc = letFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns _Frame|error? f = self.letFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            _Frame|error? cFrame = f(pFrame);
            return cFrame;
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _InnerJoinFunction object {
    *_StreamFunction;
    function (_Frame _frame) returns any lhsKeyFunction;
    function (_Frame _frame) returns any rhsKeyFunction;
    _FrameMultiMap rhsFramesMap = new;
    _Frame[]? rhsCandidates;
    _Frame|error? lhsFrame;

    function init(
            _StreamPipeline pipelineToJoin,
            function (_Frame _frame) returns any lhsKeyFunction,
            function (_Frame _frame) returns any rhsKeyFunction) {
        self.lhsKeyFunction = lhsKeyFunction;
        self.rhsKeyFunction = rhsKeyFunction;
        self.rhsCandidates = ();
        self.prevFunc = ();
        self.lhsFrame = ();
        _Frame|error? f = pipelineToJoin.next();
        while (f is _Frame) {
            self.rhsFramesMap.put(rhsKeyFunction(f).toString(), f);
            f = pipelineToJoin.next();
        }
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        function (_Frame _frame) returns any lhsKF = self.lhsKeyFunction;
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
         _FrameMultiMap rhsFramesMap = self.rhsFramesMap;
        _Frame[]? rhsCandidates = self.rhsCandidates;
        _Frame|error? lhsFrame = self.lhsFrame;
        string lhsKey = "";

        if (lhsFrame is ()) {
            lhsFrame = pf.process();
            self.lhsFrame = lhsFrame;
        }

        if (lhsFrame is _Frame) {
            lhsKey = lhsKF(lhsFrame).toString();
            if (rhsCandidates is ()) {
                rhsCandidates = rhsFramesMap.get(lhsKey);
                self.rhsCandidates = rhsCandidates;
            }
            if (rhsCandidates is _Frame[] && rhsCandidates.length() > 0) {
                _Frame rhsFrame = rhsCandidates.shift();
                self.rhsCandidates = rhsCandidates;
                _Frame joinedFrame = {...lhsFrame, ...rhsFrame};
                return joinedFrame;
            } else {
                // Move to next lhs frame
                self.lhsFrame = ();
                self.rhsCandidates = ();
                return self.process();
            }
        }
        return lhsFrame;
    }

    public function reset() {
        // Reset the state of lhsFrame
        self.lhsFrame = ();
        self.rhsCandidates = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _OuterJoinFunction object {
    *_StreamFunction;
    function (_Frame _frame) returns any lhsKeyFunction;
    function (_Frame _frame) returns any rhsKeyFunction;
    _FrameMultiMap rhsFramesMap = new;
    _Frame[]? rhsCandidates;
    _Frame|error? lhsFrame;
    _Frame nilFrame;

    function init(
            _StreamPipeline pipelineToJoin,
            function (_Frame _frame) returns any lhsKeyFunction,
            function (_Frame _frame) returns any rhsKeyFunction, _Frame nilFrame) {
        self.lhsKeyFunction = lhsKeyFunction;
        self.rhsKeyFunction = rhsKeyFunction;
        self.rhsCandidates = ();
        self.prevFunc = ();
        self.lhsFrame = ();
        self.nilFrame = nilFrame;
        _Frame|error? f = pipelineToJoin.next();
        while (f is _Frame) {
            self.rhsFramesMap.put(rhsKeyFunction(f).toString(), f);
            f = pipelineToJoin.next();
        }
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # outer join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        function (_Frame _frame) returns any lhsKF = self.lhsKeyFunction;
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
         _FrameMultiMap rhsFramesMap = self.rhsFramesMap;
        _Frame[]? rhsCandidates = self.rhsCandidates;
        _Frame|error? lhsFrame = self.lhsFrame;
        _Frame nilFrame = self.nilFrame;
        string lhsKey = "";

        if (lhsFrame is ()) {
            lhsFrame = pf.process();
            self.lhsFrame = lhsFrame;
        }

        if (lhsFrame is _Frame) {
            lhsKey = lhsKF(lhsFrame).toString();
            if (rhsCandidates is ()) {
                rhsCandidates = rhsFramesMap.get(lhsKey);
                self.rhsCandidates = rhsCandidates;
            }

            if (rhsCandidates is _Frame[]) {
                _Frame rhsFrame = rhsCandidates.shift();
                if (rhsCandidates.length() > 0) {
                    self.rhsCandidates = rhsCandidates;
                } else {
                    // Move to next lhs frame in next iteration.
                    self.rhsCandidates = ();
                    self.lhsFrame = ();
                }
                _Frame joinedFrame = {...lhsFrame, ...rhsFrame};
                return joinedFrame;
            } else {
                // rhsCandidates is nil, move to next lhs frame in next iteration.
                _Frame joinedFrame = {...lhsFrame, ...nilFrame};
                self.lhsFrame = ();
                return joinedFrame;
            }
        }
        return lhsFrame;
    }

    public function reset() {
        // Reset the state of lhsFrame
        self.lhsFrame = ();
        self.rhsCandidates = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _FilterFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # i.e
    #   1. on dn equals dept.deptName
    #   2. where person.age >= 70
    # emit the next frame which satisfies the condition
    function (_Frame _frame) returns boolean filterFunc;

    function init(function (_Frame _frame) returns boolean filterFunc) {
        self.filterFunc = filterFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns boolean filterFunc = self.filterFunc;
        _Frame|error? pFrame = pf.process();
        while (pFrame is _Frame && !filterFunc(pFrame)) {
            pFrame = pf.process();
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _OrderByFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # order by person.fname true, person.age false
    function(_Frame _frame) orderFunc;

    function init(function(_Frame _frame) orderFunc) {
        self.orderFunc = orderFunc;
        self.prevFunc = ();
        orderFieldValsArr = [];
        orderDirectionsArr = [];
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) f = self.orderFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            f(pFrame);
            return pFrame;
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _SelectFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # select {
    #   firstName: person.firstName,
    #   lastName: person.lastName,
    #   dept : dept.name
    # };
    public function (_Frame _frame) returns _Frame|error? selectFunc;

    function init(function (_Frame _frame) returns _Frame|error? selectFunc) {
        self.selectFunc = selectFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns _Frame|error? f = self.selectFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            _Frame|error? cFrame = f(pFrame);
            return cFrame;
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _DoFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # do {
    #   count += value;
    # };
    public function (_Frame _frame) doFunc;

    function init(function (_Frame _frame) doFunc) {
        self.doFunc = doFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) f = self.doFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            f(pFrame);
            return pFrame;
        }
        if (pFrame is error) {
            return pFrame;
        }
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _LimitFunction object {
    *_StreamFunction;

    # Desugared function to limit the number of results

    public int lmt;
    public int count = 0;

    function init(int lmt) {
        self.lmt = lmt;
        self.prevFunc = ();
        if (lmt < 0) {
            panic error("Unable to assign limit", message = "limit cannot be < 0.");
        }
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        if (self.count < self.lmt) {
            _Frame|error? pFrame = pf.process();
            self.count += 1;
            return pFrame;
        }
        return ();
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type StreamOrderBy object {
    anydata[][] sortFields = [];
    boolean[] sortTypes = [];

    function init() {
        self.sortFields = orderFieldValsArr;
        self.sortTypes = orderDirectionsArr;
    }

    function topDownMergeSort() returns @tainted Type[] {
        anydata[][] sortFieldsClone = self.sortFields.clone();
        self.topDownSplitMerge(sortFieldsClone, 0, self.sortFields.length(), self.sortFields);
        return self.sortFields;
    }

    function topDownSplitMerge(@tainted anydata[][] sortArrClone, int iBegin, int iEnd, @tainted anydata[][] sortArr) {
        if (iEnd - iBegin < 2) {
            return;
        }
        int iMiddle = (iEnd + iBegin) / 2;
        self.topDownSplitMerge(sortArr, iBegin, iMiddle, sortArrClone);
        self.topDownSplitMerge(sortArr, iMiddle, iEnd, sortArrClone);
        self.topDownMerge(sortArrClone, iBegin, iMiddle, iEnd, sortArr);
    }

    function topDownMerge(@tainted anydata[][] sortArrClone, int iBegin, int iMiddle, int iEnd,
    @tainted anydata[][] sortArr) {
        int i = iBegin;
        int j = iMiddle;
        int k = iBegin;

        while (k < iEnd) {
            if (i < iMiddle && (j >= iEnd || self.sortFunc(sortArrClone[i], sortArrClone[j], 0) < 0)) {
                sortArr[k] = sortArrClone[i];
                i = i + 1;
            } else {
                sortArr[k] = sortArrClone[j];
                j = j + 1;
            }
            k += 1;
        }
    }

    function sortFunc(anydata[] x, anydata[] y, int i) returns @tainted int {
        // () should always come last irrespective of the order direction.
        if (x[i] is ()) {
            if (y[i] is ()) {
                return self.callNextSortFunc(x, y, 0, i + 1);
            }
            return 1;
        } else if (y[i] is ()) {
            return -1;
        } else if (x[i] is string) {
            if (y[i] is string) {
                int c;
                if (self.sortTypes[i]) {
                    c = self.stringSort(<string>x[i], <string>y[i]);
                } else {
                    c = self.stringSort(<string>y[i], <string>x[i]);
                }
                return self.callNextSortFunc(x, y, c, i + 1);
            } else {
                panic error("Inconsistent order field value", message = "order field contain non-string type values");
            }
        } else if (x[i] is (int|float|decimal)) {
             if (y[i] is (int|float|decimal)) {
                 int c;
                 if (self.sortTypes[i]) {
                     c = self.numberSort(<int|float|decimal>x[i], <int|float|decimal>y[i], self.sortTypes[i]);
                 } else {
                     c = self.numberSort(<int|float|decimal>y[i], <int|float|decimal>x[i], self.sortTypes[i]);
                 }
                 return self.callNextSortFunc(x, y, c, i + 1);
             } else {
                 panic error("Inconsistent order field value", message = "order field contain non-numeric values");
             }
        } else if (x[i] is boolean) {
            if (y[i] is boolean) {
                int c;
                if (self.sortTypes[i]) {
                    c = self.booleanSort(<boolean>x[i], <boolean>y[i]);
                } else {
                    c = self.booleanSort(<boolean>y[i], <boolean>x[i]);
                }
                return self.callNextSortFunc(x, y, c, i + 1);
            } else {
                panic error("Inconsistent order field value", message = "order field contain non-boolean type values");
            }
        } else {
            panic error("Unable to perform order by", message = "order field type incorrect");
        }
    }

    public function numberSort(int|float|decimal val1, int|float|decimal val2, boolean dir) returns int {
        if (val1 is int) {
            if (val2 is int) {
                return val1 - val2;
            } else if (val2 is float) {
                return <float>val1 < val2 ? -1 : <float>val1 == val2 ? 0 : 1;
            } else {
                return <decimal>val1 < val2 ? -1 : <decimal>val1 == val2 ? 0 : 1;
            }
        } else if (val1 is float) {
            if (checkNaN(val1)) {
                // need to check the direction because NaN should always come last.
                if (dir) {
                    return 1;
                }
                return -1;
            } else if (val2 is int) {
                return val1 < <float>val2 ? -1 : val1 == <float>val2 ? 0 : 1;
            } else if (val2 is float){
                if (checkNaN(val1)) {
                    if (checkNaN(val2)) {
                        return 0;
                    }
                } else if (checkNaN(val2)) {
                    // need to check the direction because NaN should always come last.
                    if (dir) {
                        return -1;
                    }
                    return 1;
                }
                return val1 < val2 ? -1 : val1 == val2 ? 0 : 1;
            } else {
                return <decimal>val1 < val2 ? -1 : <decimal>val1 == val2 ? 0 : 1;
            }
        } else {
             if (val2 is (int|float)) {
                 return val1 < <decimal>val2 ? -1 : val1 == <decimal>val2 ? 0 : 1;
             } else {
                return val1 < val2 ? -1 : val1 == val2 ? 0 : 1;
             }
        }
    }

    public function stringSort(string st1, string st2) returns int {
        return lang_string:codePointCompare(st1, st2);
    }

    public function booleanSort(boolean b1, boolean b2) returns int {
        if (b1) {
            if (b2) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (b2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    function callNextSortFunc(anydata[] x, anydata[] y, int c, int i) returns @tainted int {
        int result = c;
        if (result == 0 && (self.sortTypes.length() > i) && (i < self.sortFields.length()-1)) {
            result = self.sortFunc(x, y, i);
        }
        return result;
    }
};

type _FrameMultiMap object {

    map<_Frame[]> m;

    function init() {
        self.m = {};
    }

    function put(string k, _Frame v) {
        _Frame[]? vals = self.m[k];
        if (vals is _Frame[]) {
            vals.push(v);
        } else {
            self.m[k] = [v];
        }
    }

    function get(string k) returns _Frame[]? {
        _Frame[]? vals = self.m[k];
        if (vals is _Frame[]) {
            _Frame[] frames = [];
            foreach _Frame v in vals {
                frames.push(v);
            }
            return frames;
        }
    }

};
