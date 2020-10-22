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

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type any|error;

# A type parameter that is a subtype of `error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type ErrorType error|never;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type1 any|error;

# Selects the members from a stream for which a function returns true.
#
# + stm - the stream
# + func - a predicate to apply to each member to test whether it should be selected
# + return - new stream only containing members of `stm` for which `func` evaluates to true
public isolated function filter(stream<Type,ErrorType> stm, @isolatedParam function(Type val) returns boolean func)
   returns stream<Type,ErrorType>  {
    FilterSupport itrObj = new(stm, func);
    return internal:construct(internal:getElementType(typeof stm), itrObj);
}

# Returns the next element in the stream wrapped in a record or () if the stream ends.
#
# + strm - The stream
# + return - If the stream has elements, return the element wrapped in a record with single field called `value`,
#            otherwise returns ()
public function next(stream<Type, ErrorType> strm) returns record {| Type value; |}|ErrorType? {
    object {
        public function next() returns record {|Type value;|}|ErrorType?;
    } iteratorObj = <object {
                            public function next() returns record {|Type value;|}|ErrorType?;
                     }>internal:getIteratorObj(strm);
    var val = iteratorObj.next();
    if (val is ()) {
        return ();
    } else if (val is error) {
        return val;
    } else {
        return internal:setNarrowType(internal:getElementType(typeof strm), {value : val.value});
    }
}

# Applies a function to each member of a stream and returns a stream of the results.
#
# + stm - the stream
# + func - a function to apply to each member
# + return - new stream containing result of applying `func` to each member of `stm` in order
public isolated function 'map(stream<Type,ErrorType> stm, @isolatedParam function(Type val) returns Type1 func)
   returns stream<Type1,ErrorType> {
    MapSupport  iteratorObj = new(stm, func);
    return internal:construct(internal:getReturnType(func), iteratorObj);
}

// Refer to issue https://github.com/ballerina-platform/ballerina-lang/issues/21527
# Combines the members of a stream using a combining function.
# The combining function takes the combined value so far and a member of the stream,
# and returns a new combined value.
#
# + stm - the stream
# + func - combining function
# + initial - initial value for the first argument of combining function
# + return - result of combining the members of `stm` using the combining function
public isolated function reduce(stream<Type,ErrorType> stm, @isolatedParam function(Type1 accum, Type val) returns Type1 func, Type1 initial)
   returns Type1|ErrorType {
    any | error reducedValue = initial;
    while (true) {
        var nextVal = next(stm);
        if (nextVal is ()) {
            return reducedValue;
        } else if (nextVal is error) {
            return nextVal;
        } else {
            var value = nextVal?.value;
            reducedValue = func(reducedValue, value);
        }
    }
}

# Applies a function to each member of a stream.
# The Combining function is applied to each member of stream in order.
#
# + stm - the stream
# + func - a function to apply to each member
# + return - An error if iterating the stream encounters an error
public isolated function forEach(stream<Type,ErrorType> stm, @isolatedParam function(Type val) returns () func) returns ErrorType? {
    var nextVal = next(stm);
    while(true) {
        if (nextVal is ()) {
            return;
        } else if (nextVal is error) {
            return nextVal;
        } else {
            var value = nextVal.value;
            func(value);
        }
        nextVal = next(stm);
    }
}

# Returns an iterator over a stream.
#
# + stm - the stream
# + return - a new iterator object that will iterate over the members of `stm`.
public isolated function iterator(stream<Type,ErrorType> stm) returns object {
    public isolated function next() returns record {|
        Type value;
    |}|ErrorType?;
}{
    return internal:getIteratorObj(stm);
}

# Closes a stream.
# This releases any system resources being used by the stream.
#
# + stm - the stream to close
# + return - () if the close completed successfully, otherwise an error
public isolated function close(stream<Type,ErrorType> stm) returns ErrorType? {
    var itrObj = internal:getIteratorObj(stm);
    if (itrObj is object {
        public function next() returns record {|Type value;|}|ErrorType?;
        public function close() returns ErrorType?;
    }) {
        return itrObj.close();
    }
}
