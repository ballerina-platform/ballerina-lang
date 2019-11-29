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

// This files defines Ballerina types for Java types

import ballerina/bir;

public const JBYTE_KIND = "byte";
public const JCHAR_KIND = "char";
public const JSHORT_KIND = "short";
public const JINT_KIND = "int";
public const JLONG_KIND = "long";
public const JFLOAT_KIND = "float";
public const JDOUBLE_KIND = "double";
public const JBOOLEAN_KIND = "boolean";
public const JVOID_KIND = "void";
public const JARRAY_KIND = "array";
public const JREF_KIND = "ref";
public const JNO_TYPE_KIND = "no";

public type JTypeKind JBYTE_KIND | JCHAR_KIND | JSHORT_KIND | JINT_KIND | JLONG_KIND | JFLOAT_KIND
                                | JDOUBLE_KIND | JBOOLEAN_KIND | JVOID_KIND | JARRAY_KIND | JREF_KIND | JNO_TYPE_KIND;

public type JType record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JTypeKind jTypeKind;
    anydata...;
|};

public type JByte record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JBYTE_KIND jTypeKind = JBYTE_KIND;
|};

public type JChar record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JCHAR_KIND jTypeKind = JCHAR_KIND;
|};

public type JShort record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JSHORT_KIND jTypeKind = JSHORT_KIND;
|};

public type JInt record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JINT_KIND jTypeKind = JINT_KIND;
|};

public type JLong record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JLONG_KIND jTypeKind = JLONG_KIND;
|};

public type JFloat record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JFLOAT_KIND jTypeKind = JFLOAT_KIND;
|};

public type JDouble record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JDOUBLE_KIND jTypeKind = JDOUBLE_KIND;
|};

public type JBoolean record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JBOOLEAN_KIND jTypeKind = JBOOLEAN_KIND;
|};

public type JVoid record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JVOID_KIND jTypeKind = JVOID_KIND;
|};

public type JPrimitiveType JByte | JChar | JShort | JInt | JLong | JFloat | JDouble | JBoolean | JVoid;

public type JArrayType record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JARRAY_KIND jTypeKind = JARRAY_KIND;
    JType elementType;
|};

public type JRefType record {
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JREF_KIND jTypeKind = JREF_KIND;
    string typeValue;
    boolean isInterface = false;
    boolean isArray = false;
};

public type JNoType record {|
    bir:PLATFORM_TYPE_NAME typeName = bir:PLATFORM_TYPE_NAME;
    JNO_TYPE_KIND jTypeKind = JNO_TYPE_KIND;
|};

public type JType JPrimitiveType | JRefType | JNoType | JArrayType;

public function getJTypeFromTypeName(string typeName) returns JPrimitiveType|JRefType {
    if (typeName == JBYTE_KIND) {
        return <JByte> {};
    } else if (typeName == JCHAR_KIND) {
        return <JChar> {};
    } else if (typeName == JSHORT_KIND) {
        return <JShort> {};
    } else if (typeName == JINT_KIND) {
        return <JInt> {};
    } else if (typeName == JLONG_KIND) {
        return <JLong> {};
    } else if (typeName == JFLOAT_KIND) {
        return <JFloat> {};
    } else if (typeName == JDOUBLE_KIND) {
        return <JDouble> {};
    } else if (typeName == JBOOLEAN_KIND) {
        return <JBoolean> {};
    } else {
        return <JRefType> {
            typeValue: typeName
        };
    }
}

public function getJArrayTypeFromTypeName(string typeName, byte dimensions) returns JArrayType {
    JArrayType arrayType = { elementType : getJTypeFromTypeName(typeName)};
    int i = 1;
    while(i < (<int>dimensions)) {
        JArrayType temp = { elementType : arrayType};
        arrayType = temp;
        i += 1;
    }

    return arrayType;
}
