/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;

/**
 * Represents the set of type builder interfaces which are used to generate complex types in a single semantic context.
 *
 * @since 2201.2.0
 */
public abstract class TypeBuilder {

    public XML XML_TYPE;
    public MAP MAP_TYPE;
    public FUTURE FUTURE_TYPE;
    public TYPEDESC TYPEDESC_TYPE;
    public STREAM STREAM_TYPE;
    public TUPLE TUPLE_TYPE;
    public ARRAY ARRAY_TYPE;
    public ERROR ERROR_TYPE;
    public SINGLETON SINGLETON_TYPE;
    public TABLE TABLE_TYPE;
    public FUNCTION FUNCTION_TYPE;
    public OBJECT OBJECT_TYPE;
    public RECORD RECORD_TYPE;

    /**
     * Represents the methods required to build the XML type symbol of an XML type descriptor.
     */
    public interface XML {

        /**
         * Sets the type parameter which could be optional when building an XML type. This type parameter shall
         * either be null, or a subtype of the XML type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link XML} instance with the type parameter being set
        */
        XML withTypeParam(TypeSymbol typeParam);

        /**
         * Builds the XML type descriptor with a semantically valid type parameter.
         *
         * @return The built {@link XMLTypeSymbol}
         */
        XMLTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Map type descriptor.
     */
    public interface MAP {

        /**
         * Sets the type parameter which could be optional when building a Map type. This type parameter shall either
         * be null, or a subtype of the Map type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link MAP} instance with the type parameter being set
         */
        MAP withTypeParam(TypeSymbol typeParam);

        /**
         * Build the map type descriptor and returns the map type symbol.
         *
         * @return The {@link MapTypeSymbol} built
         */
        MapTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Future type descriptor.
     */
    public interface FUTURE {

        FUTURE withTypeParam(TypeSymbol typeParam);

        FutureTypeSymbol build();
    }

    public interface TYPEDESC {

        TYPEDESC withTypeParam(TypeSymbol typeParam);

        TypeDescTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Stream type symbol of a Stream type descriptor.
     */
    public interface STREAM {

        STREAM withValueType(TypeSymbol valueType);

        STREAM withCompletionType(TypeSymbol completionType);

        StreamTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Tuple type descriptor.
     */
    public interface TUPLE {

        TUPLE withMemberType(TypeSymbol memberType);

        TUPLE withRestType(TypeSymbol restType);

        TupleTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of an Array type descriptor.
     */
    public interface ARRAY {
        ARRAY withType(TypeSymbol type);

        ARRAY withSize(Integer size);

        ArrayTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of an Error type descriptor.
     */
    public interface ERROR {
        ERROR withTypeParam(TypeSymbol typeParam);

        ErrorTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Singleton type descriptor.
     */
    public interface SINGLETON {

        SINGLETON withValueSpace(Object value, TypeSymbol typeSymbol);
        SingletonTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Table type descriptor.
     */
    public interface TABLE {

        TABLE withRowType(TypeSymbol rowType);
        TABLE withKeyConstraints(TypeSymbol... keyTypes);
        TABLE withKeySpecifiers(String... fieldNames);
        TableTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Map type symbol of a Function type descriptor.
     */
    public interface FUNCTION {

        FUNCTION withParams(ParameterSymbol... parameters);
        FUNCTION withRestParam(ParameterSymbol restParam);
        FUNCTION withReturnType(TypeSymbol returnType);

        PARAMETER_BUILDER params();
        FunctionTypeSymbol build();

        interface PARAMETER_BUILDER {
            PARAMETER_BUILDER withName(String name);
            PARAMETER_BUILDER withType(TypeSymbol type);
            PARAMETER_BUILDER withKind(ParameterKind kind);
            ParameterSymbol build();
        }
    }

    /**
     * Represents the methods required to build the Map type symbol of an Object type descriptor.
     */
    public interface OBJECT {
        OBJECT withQualifier(Qualifier... qualifiers);
        OBJECT withFields(OBJECT_FIELD... fields);
        OBJECT withMethods(OBJECT_METHOD... methods);
        OBJECT withTypeInclusions(TypeReferenceTypeSymbol... inclusions);
        OBJECT_FIELD fields();
        OBJECT_METHOD methods();
        ObjectTypeSymbol build();

        interface OBJECT_FIELD {
            OBJECT_FIELD isPublic();
            OBJECT_FIELD withType(TypeSymbol type);
            OBJECT_FIELD withName(String name);
            boolean isPublicField();
            String getName();
            TypeSymbol getType();
            OBJECT_FIELD build();
        }

        interface OBJECT_METHOD {
            OBJECT_METHOD withQualifiers(Qualifier... qualifiers);
            OBJECT_METHOD withName(String name);
            OBJECT_METHOD withType(FunctionTypeSymbol type);
            String getName();
            FunctionTypeSymbol getType();
            OBJECT_METHOD build();
        }
    }

    /**
     * Represents the methods required to build the Map type symbol of a Record type descriptor.
     */
    public interface RECORD {
        RECORD_FIELD fields();

        RECORD withFields(RECORD_FIELD... fields);
        RECORD withRestField(TypeSymbol restType);
        RECORD withTypeInclusions(TypeReferenceTypeSymbol... typeInclusions);
        RecordTypeSymbol build();

        interface RECORD_FIELD {
            RECORD_FIELD isReadOnly();
            RECORD_FIELD withType(TypeSymbol type);
            RECORD_FIELD withName(String name);
            RECORD_FIELD isOptional();
            RECORD_FIELD hasDefaultExpr();

            boolean isFieldReadOnly();
            TypeSymbol getType();
            String getName();
            boolean isFieldOptional();
            boolean hasFieldDefaultExpr();
            RECORD_FIELD build();
        }
    }
}
