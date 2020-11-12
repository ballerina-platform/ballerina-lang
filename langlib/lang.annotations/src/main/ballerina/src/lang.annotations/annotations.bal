// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Denote that the return value is tainted.
public const annotation tainted on parameter, return, source listener, source var, source type;

# Denote that the return value is untainted, parameter expect untainted value, type cast mark value untainted,
# denote a listener as producing untainted arguments to service resource params.
public const annotation untainted on return, parameter, source type, source listener;

# Denotes annotated type is a parametric type.
public const annotation typeParam on source type;

# Denotes annotated type is a builtin sub type.
public const annotation builtinSubtype on source type;

# Denotes that the annotated parameter expects an `isolated` value when used in an `isolated` context.
public const annotation isolatedParam on source parameter;

# Defaultable argument names. This is for internal use.
#
# + args - Defaultable argument names are set at compile time.
type ArgsData record {|
    string[] args = [];
|};

# Defaultable annotation data generated at compile time. This is for internal use.
annotation ArgsData DefaultableArgs on function;

# An annotation that marks a program element as deprecated.
#
# The usage of a deprecated program element is not recommended due to
# various reasons. Hence, the compiler issues a warning when such an element is used.
public const annotation deprecated on source type, source class, source const, source annotation,
                source function, source parameter, source object function, source object field;

//# Defines a disptcher to be used for concurrent execution of strands.
//#
//# + dispatcher - Dispatcher identifier.
//type Dispatcher record {|
//    string dispatcher = "DEFAULT";
//|};

//# Denotes that the new strand should run concurrently.
//public annotation Dispatcher concurrent on worker,start;

public type Thread "parent" | "any";

# Describes Strand execution details for the runtime.
#
# + name - name of the strand.
# + policy - specifies the dispatching policy (not yet supported).
# + thread - specifies whether strand should run on parent strand's thread or in any available thread.
public type StrandData record {|
	string name?;
	string policy?;
	Thread thread = "parent";
|};

# Denotes new Strand execution semantics.
public const annotation StrandData strand on source worker;

# Denotes icon metadata related to types and functions.
public const annotation record {| string path; |} icon on source type, source function, source class;
