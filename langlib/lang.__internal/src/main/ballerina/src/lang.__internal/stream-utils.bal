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

# This file contains the utility functions implemented natively which are used by the 'stream' langib module.

# A type parameter that is a subtype of `any|error`.
# Has the special semantic that when used in a declaration
# all uses in the declaration must refer to same type.
@typeParam
public type ErrorType error?;

@typeParam
public type Type any|error;

@typeParam
public type Type1 any|error;


# Sets the narrowed type of the `value`.
#
# + td - The narrowed type to be set.
# + val - The value of which the type being set.
# + return - The value with the narrowed type.
public function setNarrowType(typedesc<Type> td, record {|Type value;|} val) returns record {|Type value;|} = external;

# Takes in an iterator object and returns a new stream out of it.
#
# + td - A type description.
# + iteratorObj - An iterator object.
# + return - New stream containing results of `iteratorObj` object's next function invocations.
public function construct(typedesc<Type> td, abstract object { public function next() returns
        record {|Type value;|}|ErrorType?;} iteratorObj) returns stream<Type, ErrorType> = external;

# Takes a typedesc of an array or stream and returns the typedesc of the element or constraint type.
#
# + td - An array or stream type desc.
# + return - The typedesc of the element, constraint type.
public function getElementType(typedesc<Type[]> | typedesc<stream<Type>> td) returns typedesc<Type> = external;

# Change the given filter function's parameter to any|error and returns the same function.
#
# + func - The input filter function
# + return - The input function with the changed parameter type
public function getFilterFunc(any func) returns function(Type) returns boolean = external;

# Change the given map function's parameter to any|error and returns the same function.
#
# + func - The input map function
# + return - The input function with the changed parameter type
public function getMapFunc(any func) returns function(Type) returns Type1 = external;

# Get the return type of a given function
#
# + func - The input function
# + return - The typedesc of the return type of the input function
public function getReturnType(any func) returns typedesc<Type> = external;

# Takes in a stream and returns the iterator object of that stream.
#
# + strm - The stream
# + return - An abstract object which is iterable
public function getIteratorObj(stream<Type, ErrorType> strm) returns abstract object { public function next() returns
    record {|Type value;|}|ErrorType?;} |
    abstract object {
        public function next() returns record {|Type value;|}|ErrorType?;
        public function close() returns ErrorType?;
    } = external;

