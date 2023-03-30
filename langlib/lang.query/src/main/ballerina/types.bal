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
import ballerina/lang.'object as lang_object;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type any|error;

# A type parameter that is a subtype of `error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type ErrorType error;

# A type parameter that is a subtype of `error?`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
# This represents the result type of an iterator.
@typeParam
type CompletionType error?;

# An abstract `_Iterator` object.
type _Iterator object {
    public function next() returns record {|Type value;|}|CompletionType;
};

# An abstract `_StreamImplementor` object
type _StreamImplementor object {
    public isolated function next() returns record {|Type value;|}|CompletionType;
};

# An abstract `_CloseableStreamImplementor` object.
type _CloseableStreamImplementor object {
    public isolated function next() returns record {|Type value;|}|CompletionType;
    public isolated function close() returns CompletionType;
};

# An abstract `_Iterable` object.
type _Iterable object {
    *lang_object:Iterable;
};

type _StreamFunction object {
    public _StreamFunction? prevFunc;
    public function process() returns _Frame|error?;
    public function reset();
};

type _Frame record {|
    (any|error|())...;
|};

class _StreamPipeline {
    _StreamFunction streamFunction;
    typedesc<Type> constraintTd;
    typedesc<CompletionType> completionTd;
    boolean isLazyLoading;

    function init(
            Type[]|map<Type>|record {}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection,
            typedesc<Type> constraintTd, typedesc<CompletionType> completionTd, boolean isLazyLoading) {
        self.streamFunction = new _InitFunction(collection);
        self.constraintTd = constraintTd;
        self.completionTd = completionTd;
        self.isLazyLoading = isLazyLoading;
    }

    public isolated function next() returns _Frame|error? {
        _StreamFunction sf = self.streamFunction;
        var res = internal:invokeAsExternal(sf.process);
        if (res is _Frame|error) {
            if (self.isLazyLoading && (res is QueryErrorTypes)) {
                return getQueryErrorRootCause(res);
            }
            return res;
        }
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

    public function getStream() returns stream<Type, CompletionType> {
        IterHelper itrObj = new (self, self.constraintTd);
        var strm = internal:construct(self.constraintTd, self.completionTd, itrObj);
        return strm;
    }
}

class _InitFunction {
    *_StreamFunction;
    _Iterator? itr;
    boolean resettable = true;
    Type[]|map<Type>|record {}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection;

    function init(
            Type[]|map<Type>|record {}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection) {
        self.prevFunc = ();
        self.itr = ();
        self.collection = collection;
        self.itr = self._getIterator(collection);
    }

    public function process() returns _Frame|error? {
        _Iterator i = <_Iterator>self.itr;
        record {|(any|error) value;|}|error? v = i.next();
        if (v is record {|(any|error) value;|}) {
            record {|(any|error)...; |} _frame = {...v};
            return _frame;
        } else if(v is error) {
            return prepareCompleteEarlyError(v);
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
            Type[]|map<Type>|record {}|string|xml|table<map<Type>>|stream<Type, CompletionType>|_Iterable collection)
                returns _Iterator {
        if (collection is Type[]) {
            return lang_array:iterator(collection);
        } else if (collection is record {}) {
            return lang_map:iterator(collection);
        } else if (collection is map<Type>) {
            return lang_map:iterator(collection);
        } else if (collection is xml) {
            return lang_xml:iterator(collection);
        } else if (collection is string) {
            return lang_string:iterator(collection);
        } else if (collection is table<map<Type>>) {
            return lang_table:iterator(collection);
        } else if (collection is _Iterable) {
            return collection.iterator();
        } else {
            // stream.iterator() is not resettable.
            self.resettable = false;
            return lang_stream:iterator(collection);
        }
    }
}

class _InputFunction {
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
            if(cFrame is error) {
                return prepareQueryBodyError(cFrame);
            }
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
}

class _NestedFromFunction {
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
                } else if (collection is error) {
                    return prepareQueryBodyError(collection);
                }
            }
        }
        if (cf is _Frame && itr is _Iterator) {
            record {|(any|error) value;|}|error? v = itr.next();
            if (v is record {|(any|error) value;|}) {
                _Frame _frame = {...cf};
                foreach var [k, val] in v.entries() {
                    _frame[k] = val;
                }
                return _frame;
            } else if (v is error) {
                return prepareCompleteEarlyError(v);
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
        if (collection is Type[]) {
            return lang_array:iterator(collection);
        } else if (collection is record {}) {
            return lang_map:iterator(collection);
        } else if (collection is map<Type>) {
            return lang_map:iterator(collection);
        } else if (collection is xml) {
            return lang_xml:iterator(collection);
        } else if (collection is string) {
            return lang_string:iterator(collection);
        } else if (collection is table<map<Type>>) {
            return lang_table:iterator(collection);
        } else if (collection is _Iterable) {
            return collection.iterator();
        } else if (collection is stream<Type, CompletionType>) {
            return lang_stream:iterator(collection);
        }
        panic error("Unsuppored collection", message = "unsuppored collection type.");
    }
}

class _LetFunction {
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
            if (cFrame is error) {
                return prepareQueryBodyError(cFrame);
            }
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
}

class _InnerJoinFunction {
    *_StreamFunction;
    function (_Frame _frame) returns any|error lhsKeyFunction;
    function (_Frame _frame) returns any|error rhsKeyFunction;
    _FrameMultiMap rhsFramesMap = new;
    error? failureAtJoin = ();
    stream<_Frame>? joinedFramesStream;

    function init(
            _StreamPipeline pipelineToJoin,
            function (_Frame _frame) returns any|error lhsKeyFunction,
            function (_Frame _frame) returns any|error rhsKeyFunction) {
        self.lhsKeyFunction = lhsKeyFunction;
        self.rhsKeyFunction = rhsKeyFunction;
        self.prevFunc = ();
        self.joinedFramesStream = ();

        _Frame|error? f = pipelineToJoin.next();
        while (f is _Frame) {
            any|error rhsKeyFuncResult = rhsKeyFunction(f);
            if rhsKeyFuncResult is error {
                self.failureAtJoin = prepareQueryBodyError(rhsKeyFuncResult);
                return;
            } else {
                self.rhsFramesMap.put(rhsKeyFuncResult.toString(), f);
                f = pipelineToJoin.next();
            }
        }
        if (f is error) {
            self.failureAtJoin = f;
        }
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        if (self.joinedFramesStream is ()) {
            function (_Frame _frame) returns any|error lhsKF = self.lhsKeyFunction;
            _FrameMultiMap rhsFramesMap = self.rhsFramesMap;
            _StreamFunction pf = <_StreamFunction>self.prevFunc;
            _Frame|error? lhsFrame = pf.process();
            _Frame[] joinedFrames = [];
            error? failureAtJoin = self.failureAtJoin;

            if (failureAtJoin is error) {
                fail failureAtJoin;
            }

            while (lhsFrame is _Frame) {
                any|error lhsKFRes = lhsKF(lhsFrame);
                if (lhsKFRes is error) {
                    return prepareQueryBodyError(lhsKFRes);
                }
                string lhsKey = (lhsKFRes).toString();

                _Frame[]? rhsCandidates = rhsFramesMap.get(lhsKey);
                while (rhsCandidates is _Frame[] && rhsCandidates.length() > 0) {
                    _Frame rhsFrame = rhsCandidates.shift();
                    _Frame joinedFrame = {...lhsFrame};
                    foreach var [k, val] in rhsFrame.entries() {
                        joinedFrame[k] = val;
                    }
                    joinedFrames.push(joinedFrame);
                }
                lhsFrame = pf.process();
            }
            self.joinedFramesStream = joinedFrames.toStream();
        }
        stream<_Frame> s = <stream<_Frame>>self.joinedFramesStream;
        record {|_Frame value;|}|error? f = s.next();
        if (f is record {|_Frame value;|}) {
            return f.value;
        }
        return f;
    }

    public function reset() {
        self.joinedFramesStream = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
}

class _OuterJoinFunction {
    *_StreamFunction;
    function (_Frame _frame) returns any|error lhsKeyFunction;
    function (_Frame _frame) returns any|error rhsKeyFunction;
    _FrameMultiMap rhsFramesMap = new;
    _Frame[]? rhsCandidates;
    _Frame|error? lhsFrame;
    _Frame nilFrame;
    error? failureAtJoin = ();

    function init(
            _StreamPipeline pipelineToJoin,
            function (_Frame _frame) returns any|error lhsKeyFunction,
            function (_Frame _frame) returns any|error rhsKeyFunction, _Frame nilFrame) {
        self.lhsKeyFunction = lhsKeyFunction;
        self.rhsKeyFunction = rhsKeyFunction;
        self.rhsCandidates = ();
        self.prevFunc = ();
        self.lhsFrame = ();
        self.nilFrame = nilFrame;
        _Frame|error? f = pipelineToJoin.next();
        while (f is _Frame) {
            any|error rhsKeyFuncResult = rhsKeyFunction(f);
            if rhsKeyFuncResult is error {
                self.failureAtJoin = prepareQueryBodyError(rhsKeyFuncResult);
                return;
            } else {
                self.rhsFramesMap.put(rhsKeyFuncResult.toString(), f);
                f = pipelineToJoin.next();
            }
        }
        if (f is error) {
            self.failureAtJoin = f;
        }
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # outer join var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        function (_Frame _frame) returns any|error lhsKF = self.lhsKeyFunction;
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        _FrameMultiMap rhsFramesMap = self.rhsFramesMap;
        _Frame[]? rhsCandidates = self.rhsCandidates;
        _Frame|error? lhsFrame = self.lhsFrame;
        _Frame nilFrame = self.nilFrame;
        error? failureAtJoin = self.failureAtJoin;
        string lhsKey = "";

        if(failureAtJoin is error) {
            fail failureAtJoin;
        }

        if (lhsFrame is ()) {
            lhsFrame = pf.process();
            self.lhsFrame = lhsFrame;
        }

        if (lhsFrame is _Frame) {
            any|error lhsKFRes = lhsKF(lhsFrame);
            if (lhsKFRes is error) {
                return prepareQueryBodyError(lhsKFRes);
            }
            lhsKey = (lhsKFRes).toString();
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
                _Frame joinedFrame = {...lhsFrame};
                foreach var [k, val] in rhsFrame.entries() {
                    joinedFrame[k] = val;
                }
                return joinedFrame;
            } else {
                // rhsCandidates is nil, move to next lhs frame in next iteration.
                _Frame joinedFrame = {...lhsFrame};
                foreach var [k, val] in nilFrame.entries() {
                    joinedFrame[k] = val;
                }
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
}

class _FilterFunction {
    *_StreamFunction;

    # Desugared function to do;
    # where person.age >= 70
    # emit the next frame which satisfies the condition
    function (_Frame _frame) returns error|boolean filterFunc;

    function init(function (_Frame _frame) returns boolean|error filterFunc) {
        self.filterFunc = filterFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns boolean|error filterFunc = self.filterFunc;
        _Frame|error? pFrame = pf.process();
        while (pFrame is _Frame) {
            boolean|error filterResult = filterFunc(pFrame);
            if filterResult is boolean {
                if !filterResult {
                    pFrame = pf.process();
                } else {
                    return pFrame;
                }
            } else {
                return prepareQueryBodyError(filterResult);
            }
        }
        return pFrame;
    }

    public function reset() {
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
}

class _OrderByFunction {
    *_StreamFunction;

    # Desugared function to do;
    # order by person.fname true, person.age false
    function (_Frame _frame) returns error? orderKeyFunc;
    stream<_Frame>? orderedStream;

    function init(function (_Frame _frame) returns error? orderKeyFunc) {
        self.orderKeyFunc = orderKeyFunc;
        self.orderedStream = ();
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        if (self.orderedStream is ()) {
            _StreamFunction pf = <_StreamFunction>self.prevFunc;
            function (_Frame _frame) returns error? orderKeyFunc = self.orderKeyFunc;
            _Frame|error? f = pf.process();
            boolean[] directions = [];
            _OrderTreeNode oTree = new;
            // consume all events for ordering.
            while (f is _Frame) {
                error? res = orderKeyFunc(f);
                if(res is error) {
                    return prepareQueryBodyError(res);
                }
                oTree.add(f, <any[]>(checkpanic f["$orderDirection$"]), <any[]>(checkpanic f["$orderKey$"]));
                f = pf.process();
            }
            if (f is error) {
                return f;
            }
            self.orderedStream = oTree.get().toStream();
        }

        stream<_Frame> s = <stream<_Frame>>self.orderedStream;
        record {|_Frame value;|}|error? f = s.next();
        if (f is record {|_Frame value;|}) {
            return f.value;
        }
        return f;
    }

    public function reset() {
        self.orderedStream = ();
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
}

class _SelectFunction {
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
            if (cFrame is error) {
                return prepareQueryBodyError(cFrame);
            }
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
}

class _DoFunction {
    *_StreamFunction;

    # Desugared function to do;
    # do {
    #   count += value;
    # };
    public function (_Frame _frame) returns any|error doFunc;

    function init(function (_Frame _frame) returns any|error doFunc) {
        self.doFunc = doFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns any|error f = self.doFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            any|error cFrame = f(pFrame);
            if (cFrame is error) {
                return prepareQueryBodyError(cFrame);
            }
            if cFrame !is () {
                return {"$value$": cFrame};
            }
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
}

class _LimitFunction {
    *_StreamFunction;

    # Desugared function to limit the number of results
    function (_Frame _frame) returns int|error limitFunc;
    public int count = 0;

    function init(function (_Frame _frame) returns int limitFunc) {
        self.limitFunc = limitFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction>self.prevFunc;
        function (_Frame _frame) returns int|error limitFunc = self.limitFunc;
        _Frame|error? pFrame = pf.process();
        if (pFrame is _Frame) {
            int|error lmt = limitFunc(pFrame);
            if (lmt is int) {
                if (lmt < 1) {
                    panic error("Invalid limit", message = "limit cannot be < 1.");
                }
                if (self.count < lmt) {
                    self.count += 1;
                    return pFrame;
                }
                return ();
            } else {
                return prepareQueryBodyError(lmt);
            }
        }
        return pFrame;
    }

    public function reset() {
        self.count = 0;
        _StreamFunction? pf = self.prevFunc;
        if (pf is _StreamFunction) {
            pf.reset();
        }
    }
}

// ---- helper types ----

class _FrameMultiMap {
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
            int l = vals.length();
            int i = 0;
            while (i < l) {
                frames.push(vals[i]);
                i += 1;
            }
            return frames;
        }
    }

}

class IterHelper {
    public _StreamPipeline pipeline;
    public typedesc<Type> outputType;

    function init(_StreamPipeline pipeline, typedesc<Type> outputType) {
        self.pipeline = pipeline;
        self.outputType = outputType;
    }

    public isolated function next() returns record {|Type value;|}|error? {
        _StreamPipeline p = self.pipeline;
        _Frame|error? f = p.next();
        if (f is _Frame) {
            Type v = <Type>f["$value$"];
            return internal:setNarrowType(self.outputType, {value: v});
        } else {
            return f;
        }
    }
}

class _OrderTreeNode {
    any? key = ();
    _Frame[]? frames = ();
    lang_array:SortDirection nodesDirection = lang_array:ASCENDING;
    map<_OrderTreeNode> nodes = {};
    any[] keys = [];

    # adds a _Frame into the _OrderTreeNode tree structure.
    function add(_Frame f, any[] directions, any[] keys) {
        if (keys.length() == 0 && directions.length() == 0) {
            _Frame[]? currentFrames = self.frames;
            if (currentFrames is _Frame[]) {
                currentFrames.push(f);
            } else {
                self.frames = [f];
            }

        } else {
            if (!<boolean>directions.shift()) {
                self.nodesDirection = lang_array:DESCENDING;
            }
            any key = keys.shift();
            string keyStr = key.toString();
            _OrderTreeNode o;
            if (self.nodes.hasKey(keyStr)) {
                o = self.nodes.get(keyStr);
            } else {
                o = new;
                o.key = key;
                self.nodes[keyStr] = o;
                self.keys.push(key);
            }
            o.add(f, directions, keys);
        }
    }

    # do a pre-order tree traversal and return collected leaf frames as orderedFrames.
    # + return - ordered frames.
    function get() returns _Frame[] {
        _Frame[] orderedFrames = [];
        if (self.frames is _Frame[]) {
            _Frame[] frames = <_Frame[]>self.frames;
            int l = frames.length();
            int i = 0;
            while (i < l) {
                orderedFrames.push(frames[i]);
                i += 1;
            }
        } else {
            any[] keys = self.getSortedArray(self.keys);
            foreach any k in keys {
                _OrderTreeNode o = self.nodes.get(k.toString());
                _Frame[] frms = o.get();
                foreach _Frame f in frms {
                    orderedFrames.push(f);
                }
            }
        }
        return orderedFrames;
    }

    # sorting is not supported for any[], therefore have to resolve runtime type and sort it.
    # + return - ordered array.
    function getSortedArray(any[] arr) returns any[] {
        if (arr.length() > 0) {
            int i = 0;
            while (i < arr.length()) {
                if (arr[i] is ()) {
                    i += 1;
                    continue;
                } else if (arr[i] is boolean) {
                    boolean?[] res = [];
                    self.copyArray(arr, res);
                    return res.sort(self.nodesDirection, (v) => v);
                } else if (arr[i] is int) {
                    int?[] res = [];
                    self.copyArray(arr, res);
                    return res.sort(self.nodesDirection, (v) => v);
                } else if (arr[i] is float) {
                    float?[] res = [];
                    self.copyArray(arr, res);
                    return res.sort(self.nodesDirection, (v) => v);
                } else if (arr[i] is decimal) {
                    decimal?[] res = [];
                    self.copyArray(arr, res);
                    return res.sort(self.nodesDirection, (v) => v);
                } else if (arr[i] is string) {
                    string?[] res = [];
                    self.copyArray(arr, res);
                    return res.sort(self.nodesDirection, (v) => v);
                }
            }
        }
        return arr;
    }

    # copy every element of source array into empty target array.
    function copyArray(any[] 'source, any[] 'target) {
        foreach var v in 'source {
            'target.push(v);
        }
    }
}

//Distinct error to identify errors thrown from query body
public type Error distinct error;

//Distinct error to identify errors thrown from query pipeline
public type CompleteEarlyError distinct error;

public type QueryErrorTypes CompleteEarlyError|Error;
