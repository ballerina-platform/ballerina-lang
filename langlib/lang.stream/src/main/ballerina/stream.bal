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
type ErrorType error;

# A type parameter that is a subtype of `error?`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
# This represents the result type of an iterator.
@typeParam
type CompletionType error?;

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
type Type1 any|error;

# Selects the members from a stream for which a function returns true.
#
# ```ballerina
# stream<int> scores = [45, 60, 75, 30, 90].toStream();
# scores.filter(score => score > 50).next() ⇒ {"value":60}
# ```
#
# + stm - the stream
# + func - a predicate to apply to each member to test whether it should be selected
# + return - new stream only containing members of parameter `stm` for which parameter `func` evaluates to true
public isolated function filter(stream<Type,CompletionType> stm, @isolatedParam function(Type val) returns boolean func)
        returns stream<Type,CompletionType>  {
    FilterSupport itrObj = new(stm, func);
    return <stream<Type,CompletionType>>internal:construct(internal:getElementType(typeof stm),
        internal:getCompletionType(typeof stm), itrObj);
}

# Returns the next element in the stream wrapped in a record or () if the stream ends.
#
# ```ballerina
# stream<int> scores = [45, 60, 75, 30, 90].toStream();
# scores.next() ⇒ {"value":45}
# ```
#
# + stm - The stream
# + return - If the stream has elements, return the element wrapped in a record with single field called `value`,
#            otherwise returns ()
public isolated function next(stream<Type, CompletionType> stm) returns record {| Type value; |}|CompletionType {
    object {
        public isolated function next() returns record {|Type value;|}|CompletionType;
    } iteratorObj = <object {
                            public isolated function next() returns record {|Type value;|}|CompletionType;
                     }>internal:getIteratorObj(stm);
    var val = iteratorObj.next();
    if (val is ()) {
        return ();
    } else if (val is error) {
        return val;
    } else {
        return internal:setNarrowType(internal:getElementType(typeof stm), {value : val.value});
    }
}

# Applies a function to each member of a stream and returns a stream of the results.
#
# ```ballerina
# stream<float> ms = [14.5f, 45.5f, 6.8f, 4f].toStream();
# stream<float> cms = ms.map(m => m * 100.0);
# cms.next() ⇒ {"value":1450.0}
# ```
#
# + stm - the stream
# + func - a function to apply to each member
# + return - new stream containing result of applying parameter `func` to each member of parameter `stm` in order
public isolated function 'map(stream<Type,CompletionType> stm, @isolatedParam function(Type val) returns Type1 func)
        returns stream<Type1,CompletionType> {
    MapSupport iteratorObj = new(stm, func);
    return internal:construct(internal:getReturnType(func), internal:getCompletionType(typeof stm), iteratorObj);
}

// Refer to issue https://github.com/ballerina-platform/ballerina-lang/issues/21527
# Combines the members of a stream using a combining function.
#
# The combining function takes the combined value so far and a member of the stream,
# and returns a new combined value.
#
# ```ballerina
# stream<int> scores = [45, 60, 75, 30, 90].toStream();
# scores.reduce(isolated function (int total, int score) returns int => total + score, 0) ⇒ 300
# ```
#
# + stm - the stream
# + func - combining function
# + initial - initial value for the first argument of combining parameter `func`
# + return - result of combining the members of parameter `stm` using parameter `func`
public isolated function reduce(stream<Type,ErrorType?> stm,
        @isolatedParam function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1|ErrorType {
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
#
# The parameter `func` is applied to each member of parameter `stm` stream in order.
#
# ```ballerina
# stream<int> scores = [45, 60, 75, 30, 90].toStream();
# int total = 0;
# scores.forEach(function(int score) {
#     total += score;
# });
# total ⇒ 300
# ```
#
# + stm - the stream
# + func - a function to apply to each member
# + return - () if the close completed successfully, otherwise an error
public isolated function forEach(stream<Type,CompletionType> stm,
        @isolatedParam function(Type val) returns () func) returns CompletionType {
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
# ```ballerina
# stream<int> scores = [45, 60, 75, 30, 90].toStream();
# object {
#     public isolated function next() returns record {|int value;|}?;
# } iterator = scores.iterator();
# iterator.next() ⇒ {"value":45}
# ```
#
# + stm - the stream
# + return - a new iterator object that will iterate over the members of parameter `stm`.
public isolated function iterator(stream<Type,CompletionType> stm) returns object {
    public isolated function next() returns record {|
        Type value;
    |}|CompletionType;
}{
    return internal:getIteratorObj(stm);
}

# Closes a stream.
#
# This releases any system resources being used by the stream.
# Closing a stream that has already been closed has no effect and returns `()`.
#
# ```ballerina
# stream<int, error?> strm = new;
# check strm.close();
# ```
#
# + stm - the stream to close
# + return - () if the close completed successfully, otherwise an error
public isolated function close(stream<Type,CompletionType> stm) returns CompletionType? {
    var itrObj = internal:getIteratorObj(stm);
    if (itrObj is object {
        public isolated function next() returns record {|Type value;|}|CompletionType;
        public isolated function close() returns CompletionType?;
    }) {
        return itrObj.close();
    }
    return;
}
