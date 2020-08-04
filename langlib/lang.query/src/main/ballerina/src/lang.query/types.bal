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

    function (_Frame _frame) returns boolean onCondition;
    _StreamPipeline pipelineToJoin;
    _Frame|error? currentFrame;

    function init(_StreamPipeline pipelineToJoin, function (_Frame _frame) returns boolean onCondition) {
        self.pipelineToJoin = pipelineToJoin;
        self.onCondition = onCondition;
        self.prevFunc = ();
        self.currentFrame = ();
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        function (_Frame _frame) returns boolean onCondition = self.onCondition;
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        _StreamPipeline j = self.pipelineToJoin;
        _Frame|error? cf = self.currentFrame;
        if (cf is ()) {
            cf = pf.process();
            self.currentFrame = cf;
        }
        if (cf is _Frame) {
            _Frame|error? f = j.next();
            if (f is _Frame) {
                _Frame jf = {...f, ...cf};
                if (onCondition(jf)) {
                    return jf;
                }
                return self.process();
            } else if (f is error) {
                return f;
            } else {
                // Move to next frame
                self.currentFrame = pf.process();
                j.reset();
                return self.process();
            }
        }
        return cf;
    }

    public function reset() {
        // Reset the state of currentFrame
        self.currentFrame = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
};

type _OuterJoinFunction object {
    *_StreamFunction;

    function (_Frame _frame) returns boolean onCondition;
    _StreamPipeline pipelineToJoin;
    _Frame|error? currentFrame;

    function init(_StreamPipeline pipelineToJoin, function (_Frame _frame) returns boolean onCondition) {
        self.pipelineToJoin = pipelineToJoin;
        self.onCondition = onCondition;
        self.prevFunc = ();
        self.currentFrame = ();
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # outer join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        function (_Frame _frame) returns boolean onCondition = self.onCondition;
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        _StreamPipeline j = self.pipelineToJoin;
        _Frame|error? cf = self.currentFrame;
        if (cf is ()) {
            cf = pf.process();
            self.currentFrame = cf;
        }
        if (cf is _Frame) {
            _Frame|error? f = j.next();
            if (f is _Frame) {
                _Frame jf = {...cf, ...f};
                if (!onCondition(jf)) {
                    jf = {...cf, ...self.getNilFrame(f)};
                }
                return jf;
            } else if (f is error) {
                return f;
            } else {
                // Move to next frame
                self.currentFrame = pf.process();
                j.reset();
                return self.process();
            }
        }
        return cf;
    }

    public function reset() {
        // Reset the state of currentFrame
        self.currentFrame = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }

    function getNilFrame(_Frame f) returns _Frame {
        _Frame nilFrame = {};
        foreach var e in f.entries() {
            if (e[1] is _Frame) {
                nilFrame[e[0]] = self.getNilFrame(<_Frame>e[1]);
            } else {
                nilFrame[e[0]] = ();
            }
        }
        return nilFrame;
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
    public string[] sortFields;
    public boolean[] sortTypes;

    function init(string[] sortFields, boolean[] sortTypes) {
        self.sortFields = sortFields;
        self.sortTypes = sortTypes;
    }

    public function topDownMergeSort(@tainted Type[] events) returns @tainted Type[] {
        int index = 0;
        int n = events.length();
        Type[] b = [];
        while (index < n) {
            b[index] = events[index];
            index += 1;
        }
        self.topDownSplitMerge(b, 0, n, events);
        return events;
    }

    function topDownSplitMerge(@tainted Type[] b, int iBegin, int iEnd,@tainted Type[] a) {
        if (iEnd - iBegin < 2) {
            return;
        }
        int iMiddle = (iEnd + iBegin) / 2;
        self.topDownSplitMerge(a, iBegin, iMiddle, b);
        self.topDownSplitMerge(a, iMiddle, iEnd, b);
        self.topDownMerge(b, iBegin, iMiddle, iEnd, a);
    }

    function topDownMerge(@tainted Type[] a, int iBegin, int iMiddle, int iEnd,@tainted Type[] b) {
        int i = iBegin;
        int j = iMiddle;

        int k = iBegin;
        while (k < iEnd) {
            if (i < iMiddle && (j >= iEnd || self.sortFunc(a[i], a[j], 0) < 0)) {
                b[k] = a[i];
                i = i + 1;
            } else {
                b[k] = a[j];
                j = j + 1;
            }
            k += 1;
        }
    }

    function sortFunc(Type x, Type y, int fieldIndex) returns @tainted int {
        map<anydata> xMapValue = <map<anydata>>x;
        map<anydata> yMapValue = <map<anydata>>y;

        var xFieldValue = xMapValue.get(self.sortFields[fieldIndex]);
        var yFieldValue = yMapValue.get(self.sortFields[fieldIndex]);

        if (xFieldValue is ()) {
            if (yFieldValue is ()) {
                return 0;
            } else {
                return 1;
            }
        } else if (yFieldValue is ()) {
            return -1;
        } else if (xFieldValue is (int|float|decimal)) {
            if (yFieldValue is (int|float|decimal)) {
                int c;
                if (self.sortTypes[fieldIndex]) {
                    c = self.numberSort(xFieldValue, yFieldValue);
                } else {
                    c = self.numberSort(yFieldValue, xFieldValue);
                }
                return self.callNextSortFunc(x, y, c, fieldIndex + 1);
            } else {
                panic error("Inconsistent order field value",
                    message = self.sortFields[fieldIndex] + " order field contain non-numeric values");
            }
        } else if (xFieldValue is string) {
            if (yFieldValue is string) {
                int c;
                if (self.sortTypes[fieldIndex]) {
                    c = self.stringSort(xFieldValue, yFieldValue);
                } else {
                    c = self.stringSort(yFieldValue, xFieldValue);
                }
                return self.callNextSortFunc(x, y, c, fieldIndex + 1);
            } else {
                panic error("Inconsistent order field value",
                    message = self.sortFields[fieldIndex] + " order field contain non-string type values");
            }
        } else if (xFieldValue is boolean) {
            if (yFieldValue is boolean) {
                int c;
                if (self.sortTypes[fieldIndex]) {
                    c = self.booleanSort(xFieldValue, yFieldValue);
                } else {
                    c = self.booleanSort(yFieldValue, xFieldValue);
                }
                return self.callNextSortFunc(x, y, c, fieldIndex + 1);
            } else {
                panic error("Inconsistent order field value",
                    message = self.sortFields[fieldIndex] + " order field contain non-boolean type values");
            }
        } else {
            panic error("Unable to perform order by",
                message = self.sortFields[fieldIndex] + " field type incorrect");
        }
    }

    public function numberSort(int|float|decimal val1, int|float|decimal val2) returns int {
        if (val1 is int) {
            if (val2 is int) {
                return val1 - val2;
            } else if (val2 is float) {
                return <float>val1 < val2 ? -1 : <float>val1 == val2 ? 0 : 1;
            } else {
                return <decimal>val1 < val2 ? -1 : <decimal>val1 == val2 ? 0 : 1;
            }
        } else if (val1 is float) {
            if (val2 is int) {
                return val1 < <float > val2 ? -1 : val1 == <float>val2 ? 0 : 1;
            } else if (val2 is float) {
                return val1 < val2 ? -1 : val1 == val2 ? 0 : 1;
            } else {
                return <decimal>val1 < val2 ? -1 : <decimal>val1 == val2 ? 0 : 1;
            }
        } else {
            if (val2 is (int|float)) {
                return val1 < <decimal > val2 ? -1 : val1 == <decimal>val2 ? 0 : 1;
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

    function callNextSortFunc(Type x, Type y, int c, int fieldIndex) returns @tainted int {
        int result = c;
        if (result == 0 && (self.sortTypes.length() > fieldIndex)) {
            result = self.sortFunc(x, y, fieldIndex);
        }
        return result;
    }

};
