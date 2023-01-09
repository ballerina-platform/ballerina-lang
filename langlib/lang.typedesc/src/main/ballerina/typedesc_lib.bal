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

# A record representing an identifier for a module.
# This uniquely identifies a module within a program.
public type ModuleId readonly & record {|
    # the organization
    string organization;
    # the module name
    string name;
    # platform-specified parts of the identifier
    string[] platformParts;
|};

# A record representing a type-id.
# A type-id uniquely identifies an occurrence of a distinct type descriptor
# within a program.
public type TypeId readonly & record {|
    # An identifier for the module.
    ModuleId moduleId;
    # An identifier within the module identified by `moduleId`.
    # If it is a string, then it is the name of a type defined in the module.
    # If it is an integer, then it is compiler-generated.
    (string|int) localId;
|};

# Returns the type-ids induced by a typedesc value.
# 
# ```ballerina
# type Error distinct error;
# 
# type SampleError distinct (Error & error<record {string msg;}>);
# 
# Error.typeIds() ⇒ [{"moduleId":{"organization":"$anon","name":".","platformParts":["0"]},"localId":"Error"}]
# 
# SampleError.typeIds() ⇒ [{"moduleId":{"organization":"$anon","name":".","platformParts":["0"]},"localId":"SampleError"},{"moduleId":{"organization":"$anon","name":".","platformParts":["0"]},"localId":"Error"}]
# 
# SampleError.typeIds(true) ⇒ [{"moduleId":{"organization":"$anon","name":".","platformParts":["0"]},"localId":"SampleError"}]
# ```
# 
# + t - the typedesc
# + primaryOnly - if true, only the primary type-ids will be returned; otherwise, all type-ids will be returned
# + return - an array containing the type-ids induced by `t` or nil if `t` is not definite
public isolated function typeIds(typedesc t, boolean primaryOnly = false) returns readonly & TypeId[]? =
@java:Method {
    'class: "org.ballerinalang.langlib.typedesc.TypeIds",
    name: "typeIds"
} external;
