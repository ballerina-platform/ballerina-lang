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

// import ballerina/lang.__internal as internal;
// import ballerina/lang.'array as lang_array;
// import ballerina/lang.'map as lang_map;
// import ballerina/lang.'string as lang_string;
// import ballerina/lang.'xml as lang_xml;
// import ballerina/lang.'stream as lang_stream;
// import ballerina/lang.'table as lang_table;
import ballerina/lang.'object as lang_object;
// import ballerina/lang.'function;

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


type nextRecord record {|Type value;|};

//Distinct error to identify errors thrown from query body
public type Error error;

//Distinct error to identify errors thrown from query pipeline
public type CompleteEarlyError error;

public type QueryErrorTypes CompleteEarlyError|Error;