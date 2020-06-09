// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents a Java class name.
public type Class string;

# Represents a Java identifier.
public type Identifier string;

# Represents a Java array type. It is used to specify the parameter types in the `java:Constructor` and `java:Method`
# annotations.
#
# + class - Element class of the array type
# + dimensions - Dimensions of the array type
public type ArrayType record {|
    Class class;
    byte dimensions;
|};

# Describes a Java constructor. If the `paramTypes` field is not specified, then the parameter types are inferred from
# the corresponding Ballerina function.
#
# + class - The class in which the constructor exists
# + paramTypes - An optional field, which describes the parameter types of the constructor
public type ConstructorData record {|
    Class class;
    (Class | ArrayType)[] paramTypes?;
|};

# Describes a Java method. If the `paramTypes` field is not specified, then the parameter types are inferred from the
# corresponding Ballerina function.
#
# + name - An optional field, which describes the name of the Java method. If this field is not provided, then the name
#          is inferred from the Ballerina function name
# + class - The class in which the method exists
# + paramTypes - An optional field, which describes the parameter types of the method
public type MethodData record {|
    Identifier name?;
    Class class;
    (Class | ArrayType)[] paramTypes?;
|};

# Describes a Java field.
#
# + name - An optional field, which describes the name of the Java field. If this field is not provided,
#          then the name is inferred from the Ballerina function name
# + class - The class in which the field exists
public type FieldData record {|
    Identifier name;
    Class class;
|};

# Describes a Java class that corresponds to a Ballerina object.
#
# + class - The Java class represented by the Ballerina object.
public type ObjectData record {|
    Class class;
|};

# Describes a Java constructor, which provides an implementation of a Ballerina function of which the body is marked as
# `external`. If the Ballerina function body is marked as `external`, it means that the implementation of the
# function is not provided in the Ballerina source module.
#
# The following code snippet shows an example usage of this annotation. Here, the `newJavaLinkedList` Ballerina function's
# implementation is provided by the default constructor of the `java.util.LinkedList` class.
# ```ballerina
# function newJavaLinkedList() returns handle = @java:Constructor {
#      class: "java.util.LinkedList"
# } external;
# ```
public const annotation ConstructorData Constructor on source external;

# Describes a Java method, which provides an implementation of a Ballerina function of which the body is marked as
# `external`. If the Ballerina function body is marked as `external`, it means that the implementation of the
# function is not provided in the Ballerina source module.
#
# The following code snippet shows an example usage of this annotation. Here, the `getUUID` Ballerina function's
# implementation is provided by the `java.util.UUID.randomUUID` static method.
# ```ballerina
# function getUUID() returns handle = @java:Method {
#     name: "randomUUID",
#     class: "java.util.UUID"
# } external;
# ```
# The `name` field is optional. If it is not provided, the name of the Java method is inferred
# from the Ballerina function.
public const annotation MethodData Method on source external;

# Describes a Java Field access, which provides an implementation of a Ballerina function of which the body is marked as
# `external`.
# ```ballerina
# function getError() returns handle = @java:FieldGet {
#     name:"err",
#     class:"java/lang/System"
# } external;
# ```
public const annotation FieldData FieldGet on source external;

# Describes a Java Field mutate, which provides an implementation of a Ballerina function of which the body is marked as
# `external`.
# ```ballerina
# function setContractId(handle contractId) = @java:FieldSet {
#   name:"contractId",
#   class:"org/lang/impl/JavaFieldAccessMutate"
# } external;
# ```
public const annotation FieldData FieldSet on source external;

# Describes the Java class representing a Ballerina binding.
# ```ballerina
# @java:Binding {
#   class: "java.io.File"
# }
# ```
public const annotation ObjectData Binding on object type;
