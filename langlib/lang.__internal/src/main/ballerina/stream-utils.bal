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

import ballerina/jballerina.java;

# This file contains the utility functions implemented natively which are used by the 'stream' langib module.

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
public type ErrorType error|never;

@typeParam
public type Type any|error;

@typeParam
public type Type1 any|error;

# An `EmptyIterator` which returns nil on next() method invocation.
class EmptyIterator {
    public isolated function next() returns record {|Type value;|}? {
        return ();
    }
}

# Sets the narrowed type of the `value`.
#
# + td - The narrowed type to be set.
# + val - The value of which the type being set.
# + return - The value with the narrowed type.
public isolated function setNarrowType(typedesc<Type> td, record {|Type value;|} val)
    returns record {|Type value;|} = @java:Method {
            'class: "org.ballerinalang.langlib.internal.SetNarrowType",
            name: "setNarrowType"
        } external;

# Takes in an iterator object and returns a new stream out of it.
#
# + td - A type description.
# + iteratorObj - An iterator object.
# + return - New stream containing results of `iteratorObj` object's next function invocations.
public isolated function construct(typedesc<Type> td, object { public isolated function next() returns
        record {|Type value;|}|ErrorType?;} iteratorObj = new EmptyIterator())
        returns stream<Type, ErrorType> = @java:Method {
            'class: "org.ballerinalang.langlib.internal.Construct",
            name: "construct"
        } external;

# Takes a typedesc of an array or stream and returns the typedesc of the element or constraint type.
#
# + td - An array or stream type desc.
# + return - The typedesc of the element, constraint type.
public isolated function getElementType(typedesc<Type[]> | typedesc<stream<Type, ErrorType>> td)
returns typedesc<Type> = @java:Method {
    'class: "org.ballerinalang.langlib.internal.GetElementType",
    name: "getElementType"
} external;

# Change the given filter function's parameter to any|error and returns the same function.
#
# + func - The input filter function
# + return - The input function with the changed parameter type
public isolated function getFilterFunc(any func) returns function(Type) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.internal.GetFilterFunc",
    name: "getFilterFunc"
} external;

# Change the given map function's parameter to any|error and returns the same function.
#
# + func - The input map function
# + return - The input function with the changed parameter type
public isolated function getMapFunc(any func) returns function(Type) returns Type1 = @java:Method {
    'class: "org.ballerinalang.langlib.internal.GetMapFunc",
    name: "getMapFunc"
} external;

# Get the return type of a given function
#
# + func - The input function
# + return - The typedesc of the return type of the input function
public isolated function getReturnType(any func) returns typedesc<Type> = @java:Method {
    'class: "org.ballerinalang.langlib.internal.GetReturnType",
    name: "getReturnType"
} external;

# Takes in a stream and returns the iterator object of that stream.
#
# + strm - The stream
# + return - An abstract object which is iterable
public isolated function getIteratorObj(stream<Type, ErrorType> strm) returns
    object {
        public isolated function next() returns record {|Type value;|}|ErrorType?;} |
    object {
        public isolated function next() returns record {|Type value;|}|ErrorType?;
        public isolated function close() returns ErrorType?;
    } = @java:Method {
        'class: "org.ballerinalang.langlib.internal.GetIteratorObj",
        name: "getIteratorObj"
    } external;

# Invoke a non-isolated function as an external function (workaround for isolated invocations).
#
# + func - function to invoke
# + args - args for the function to invoke
# + return - result of the invocation
public isolated function invokeAsExternal(any func, any|error... args) returns any|error = @java:Method {
    'class: "org.ballerinalang.langlib.internal.InvokeAsExternal",
    name: "invokeAsExternal"
} external;
