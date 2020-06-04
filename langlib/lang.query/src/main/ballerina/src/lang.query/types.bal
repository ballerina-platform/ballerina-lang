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

public type _Iterator abstract object {
    public function next() returns record {|Type value;|}|error?;
};

public type _Iterable abstract object {
    public function __iterator() returns _Iterator;
};

public type _StreamFunction abstract object {
    public _StreamFunction? prevFunc;
    public function process() returns _Frame|error?;
    public function reset();
};

public type _Frame record{|
    (any|error)...;
|};

public type _StreamPipeline object {
    _StreamFunction streamFunction;
    typedesc<Type> resType;

    public function __init(
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

    public function addStreamFunction(_StreamFunction streamFunction) {
        _StreamFunction existingFunc = self.streamFunction;
        streamFunction.prevFunc = existingFunc;
        self.streamFunction = streamFunction;
    }

    public function getStream() returns stream<Type, error?> {
        object {
            public _StreamPipeline pipeline;
            public typedesc<Type> outputType;

            public function __init(_StreamPipeline pipeline, typedesc<Type> outputType) {
                self.pipeline = pipeline;
                self.outputType = outputType;
            }

            public function next() returns record {|Type value;|}|error? {
                _StreamPipeline p = self.pipeline;
                _Frame|error? f = p.next();
                if (f is _Frame) {
                    Type v = <Type> f["$value$"];
                    return internal:setNarrowType(self.outputType, {value : v});
                } else {
                    return f;
                }
            }
        } itrObj = new(self, self.resType);
        var strm = internal:construct(self.resType, itrObj);
        return strm;
    }
};

public type _InitFunction object {
    *_StreamFunction;
    _Iterator? itr;
    boolean resettable = true;
    (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection;

    public function __init(
            (Type)[]|map<Type>|record{}|string|xml|table<map<Type>>|stream<Type, error?>|_Iterable collection) {
        self.prevFunc = ();
        self.itr = ();
        self.collection = collection;
        self.itr = self._getIterator(collection);
    }

    public function process() returns _Frame|error? {
        _Iterator i = <_Iterator> self.itr;
        record{|(any|error) value;|}|error? v = i.next();
        if (v is record{|(any|error) value;|}) {
            record{|(any|error)...;|} _frame = {...v};
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
        } else if (collection is record{}) {
            return lang_map:iterator(collection);
        } else if (collection is map<any|error>) {
            return lang_map:iterator(collection);
        } else if (collection is string) {
            return lang_string:iterator(collection);
        } else if (collection is xml) {
            return lang_xml:iterator(collection);
        }  else if (collection is table<map<any|error>>) {
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

public type _InputFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # from var { firstName: nm1, lastName: nm2 } in personList
    #   frame {nm1: firstName, nm2: lastName}
    # from var dept in deptList
    #   frame {dept: deptList[x]}
    public function(_Frame _frame) returns _Frame|error? inputFunc;

    public function __init(function(_Frame _frame) returns _Frame|error? inputFunc) {
        self.inputFunc = inputFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) returns _Frame|error? f = self.inputFunc;
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

public type _LetFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # let Company companyRecord = { name: "WSO2" }
    #   frame { companyRecord: { name: "WSO2" }, ...prevFrame }
    public function(_Frame _frame) returns _Frame|error? letFunc;

    public function __init(function(_Frame _frame) returns _Frame|error? letFunc) {
        self.letFunc = letFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) returns _Frame|error? f = self.letFunc;
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

public type _JoinFunction object {
    *_StreamFunction;

    _StreamPipeline pipelineToJoin;
    _Frame|error? currentFrame;

    public function __init(_StreamPipeline pipelineToJoin) {
        self.pipelineToJoin = pipelineToJoin;
        self.prevFunc = ();
        self.currentFrame = ();
    }

    # Desugared function to do;
    # from var ... in listA from from var ... in listB
    # from var ... in streamA join var ... in streamB
    # + return - merged two frames { ...frameA, ...frameB }
    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
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
};

public type _FilterFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # i.e
    #   1. on dn equals dept.deptName
    #   2. where person.age >= 70
    # emit the next frame which satisfies the condition
    function(_Frame _frame) returns boolean filterFunc;

    public function __init(function(_Frame _frame) returns boolean filterFunc) {
        self.filterFunc = filterFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) returns boolean filterFunc = self.filterFunc;
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

public type _SelectFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # select {
    #   firstName: person.firstName,
    #   lastName: person.lastName,
    #   dept : dept.name
    # };
    public function(_Frame _frame) returns _Frame|error? selectFunc;

    public function __init(function(_Frame _frame) returns _Frame|error? selectFunc) {
        self.selectFunc = selectFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) returns _Frame|error? f = self.selectFunc;
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

public type _DoFunction object {
    *_StreamFunction;

    # Desugared function to do;
    # do {
    #   count += value;
    # };
    public function(_Frame _frame) doFunc;

    public function __init(function(_Frame _frame) doFunc) {
        self.doFunc = doFunc;
        self.prevFunc = ();
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
        function(_Frame _frame) f = self.doFunc;
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

public type _LimitFunction object {
    *_StreamFunction;

    # Desugared function to limit the number of results

    public int lmt;
    public int count = 0;

    public function __init(int lmt) {
        self.lmt = lmt;
        self.prevFunc = ();
        if (lmt < 0) {
            panic error("Unable to assign limit", message = "limit cannot be < 0.");
        }
    }

    public function process() returns _Frame|error? {
        _StreamFunction pf = <_StreamFunction> self.prevFunc;
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
