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

# Class is an alias for a `string` which represents a Java class name.
public type Class string;

# Identifier is an alias for a `string` that represents a Java identifier.
public type Identifier string;

# ArrayType represents a Java array type. It is used to specify parameter
# types in `Constructor` and `Method` annotations.
#
# + class - Element class of the array type
# + dimensions - dimensions of the array type
public type ArrayType record {|
    Class class;
    byte dimensions;
|};

# ConstructorData describes a Java constructor. If the `paramTypes` field is not specified,
# then parameter types are inferred from the corresponding Ballerina function.
#
# + class - The class in which the constructor exists
# + paramTypes - An optional field that describes parameter types of the constructor
public type ConstructorData record {|
    Class class;
    (Class | ArrayType)[] paramTypes?;
|};

# MethodData describes a Java method. If the `paramTypes` field is not specified,
# then parameter types are inferred from the corresponding Ballerina function.
#
# + name - an optional field that describes name of the Java method. If this field is not provided,
#          then the name is inferred from the Ballerina function name
# + class - the class in which the method exists
# + paramTypes - an optional field that describes parameter types of the method
public type MethodData record {|
    Identifier name?;
    Class class;
    (Class | ArrayType)[] paramTypes?;
|};

# FieldData describes a Java field.
#
# + name - an optional field that describes the name of the Java field. If this field is not provided,
#          then the name is inferred from the Ballerina function name
# + class - the class in which the field exists
public type FieldData record {|
    Identifier name;
    Class class;
|};

# Constructor annotation describes a Java constructor that provides an implementation to a Ballerina function
# which has an external function body. The Ballerina function body is marked as `external` means that the
# implementation of the function is not provided in the Ballerina source module.
#
# Following code snippet shows an example usage of this annotation. Here the `newJavaLinkedList` Ballerina function's
# implementation is provided by the default constructor of `java.util.UUID.LinkedList` class.
#
#       import ballerina/java;
#
#       function newJavaLinkedList() returns handle = @java:Constructor {
#           class: "java.util.LinkedList"
#       } external;
#
public const annotation ConstructorData Constructor on source external;

# Method annotation describes a Java method that provides an implementation to a Ballerina function
# which has an external function body. The Ballerina function body is marked as `external` means that the
# implementation of the function is not provided in the Ballerina source module.
#
# Following code snippet shows an example usage of this annotation. Here the `getUUID` Ballerina function's
# implementation is provided by the `java.util.UUID.randomUUID` static method.
#
#       import ballerina/java;
#
#       function getUUID() returns handle = @java:Method {
#           name: "randomUUID",
#           class: "java.util.UUID"
#       } external;
#
# The `name` field is optional. If it is not provided, the name of the Java method is inferred
# from the Ballerina function.
public const annotation MethodData Method on source external;

# FieldGet annotation describes a Java Field acccess that provides an implementation to a Ballerina function
# which has an external function body.
public const annotation FieldData FieldGet on source external;

# FieldSet annotation describes a Java Field mutate that provides an implementation to a Ballerina function
# which has an external function body.
public const annotation FieldData FieldSet on source external;

