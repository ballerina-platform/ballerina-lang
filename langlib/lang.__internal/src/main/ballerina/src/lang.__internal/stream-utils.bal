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
public type PureType any|error;

# Sets the narrowed type of the `value`.
#
# + td - The narrowed type to be set.
# + val - The value of which the type being set.
# + return - The value with the narrowed type.
public function setNarrowType(typedesc<PureType> td, record {|PureType value;|} val) returns record {|PureType value;|} = external;

# Takes in a lambda function and returns a new stream out of it.
#
# + td - A type description.
# + iteratorObj - An iterator object.
# + return - New stream containing results of `iteratorObj` object's next function invocations.
public function construct(typedesc<PureType> td, abstract object { public function next() returns
        record {|PureType value;|}|ErrorType?;} iteratorObj) returns stream<PureType, ErrorType> = external;

# Takes a typedesc of an array, stream and returns the typedesc of the element, constraint type.
#
# + td - An array or stream type description.
# + return - The typedesc of the element, constraint type.
public function getElementType(typedesc<PureType[]> | typedesc<stream<PureType>> td) returns typedesc<PureType> = external;

# Gets the saved filter function from native data.
#
# + func - Object in which the filter function is saved
# + return - filter function with parameterized function type
public function getFilterFunc(any func) returns function(PureType) returns boolean = external;

# Saves the filter function as native data in iterator object.
#
# + iteratorObj - Object in which the filter function is saved
# + func - filter function with specific function type
public function setFilterFunc(abstract object { public function next() returns record {|PureType value;|}|ErrorType?;} iteratorObj, function(PureType val) returns boolean func) = external;

