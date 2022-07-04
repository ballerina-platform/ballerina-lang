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

package io.ballerina.compiler.api;

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
         * be null, or a subtype of the Any type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link MAP} instance with the type parameter being set
         */
        MAP withTypeParam(TypeSymbol typeParam);

        /**
         * Build the map type descriptor and returns the map type symbol.
         *
         * @return The built {@link MapTypeSymbol}
         */
        MapTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Future type symbol of a Future type descriptor.
     */
    public interface FUTURE {

        /**
         * Sets the type parameter which could be optional when building a Future type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link FUTURE} instance with the type parameter being set
         */
        FUTURE withTypeParam(TypeSymbol typeParam);

        /**
         * Build the future type descriptor and returns the future type symbol.
         *
         * @return The built {@link FutureTypeSymbol}
         */
        FutureTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Typedesc type symbol of a Typedesc type descriptor.
     */
    public interface TYPEDESC {

        /**
         * Sets the type parameter which could be optional when building a Typedesc type.
         *
         * @param typeParam     The optional type parameter
         * @return The {@link TYPEDESC} instance with the type parameter being set
         */
        TYPEDESC withTypeParam(TypeSymbol typeParam);

        /**
         * Build the Typedesc type descriptor and returns the Typedesc type symbol.
         *
         * @return The built {@link TypeDescTypeSymbol}
         */
        TypeDescTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Stream type symbol of a Stream type descriptor.
     */
    public interface STREAM {

        /**
         * Sets the value type parameter which is used to build the Stream type. The value type shall be considered
         * as Any type if not provided when building the stream type descriptor.
         *
         * @param valueType     The value type parameter
         * @return The {@link STREAM} instance with the value type parameter being set
         */
        STREAM withValueType(TypeSymbol valueType);

        /**
         * Sets the completion type parameter which could be optional when building a Stream type. If the completion
         * type is not being set, the Nil type shall be used as the completion type when building the stream type
         * descriptor.
         *
         * @param completionType     The optional completion type parameter.
         * @return The {@link STREAM} instance with the value type parameter being set
         */
        STREAM withCompletionType(TypeSymbol completionType);

        /**
         * Build the Stream type descriptor and returns the Stream type symbol.
         *
         * @return The built {@link StreamTypeSymbol}
         */
        StreamTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Tuple type symbol of a Tuple type descriptor.
     */
    public interface TUPLE {

        /**
         * Sets the member type parameters used to build the Tuple type descriptor. This method could be called
         * repetitively with valid non-null type symbols.
         *
         * @param memberType     The member type parameter
         * @return The {@link TUPLE} instance with the member type parameter being included.
         */
        TUPLE withMemberType(TypeSymbol memberType);

        /**
         * Sets the rest type parameter which could be optional when building a Tuple type.
         *
         * @param restType     The optional rest type parameter
         * @return The {@link TUPLE} instance with the rest type parameter being set
         */
        TUPLE withRestType(TypeSymbol restType);

        /**
         * Build the Tuple type descriptor and returns the Tuple type symbol.
         *
         * @return The built {@link TupleTypeSymbol}
         */
        TupleTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Array type symbol of an Array type descriptor.
     */
    public interface ARRAY {

        /**
         * Sets the mandatory type parameter which is used to build the array type symbol. A non-null type symbol
         * which is also not an array type symbol is expected, or else an exception is thrown.
         *
         * @param type     The type parameter
         * @return The {@link ARRAY} instance with the type parameter being set
         */
        ARRAY withType(TypeSymbol type);

        /**
         * Sets the size of the array type descriptor. If a positive integer is provided, the array is built as a
         * closed state array with the provided size, or else an open state array would be built.
         *
         * @param size     The optional size of the array
         * @return The {@link ARRAY} instance with the size being set
         */
        ARRAY withSize(Integer size);

        /**
         * Sets the state of the array to Inferred and reset the size of the array.
         *
         * @return The {@link ARRAY} instance with the inferred state being set.
         */
        ARRAY withInferredSize();

        /**
         * Build the Array type descriptor and returns the Array type symbol.
         *
         * @return The built {@link ArrayTypeSymbol}
         */
        ArrayTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Error type symbol of an Error type descriptor.
     */
    public interface ERROR {

        /**
         * Sets the type parameter to be used when building an error type symbol, which could either be null or a
         * valid error detail record type.
         *
         * @param typeParam     The optional detail type parameter
         * @return The {@link TYPEDESC} instance with the type parameter being set
         */
        ERROR withTypeParam(TypeSymbol typeParam);

        /**
         * Build the Error type descriptor and returns the Error type symbol.
         *
         * @return The built {@link ErrorTypeSymbol}
         */
        ErrorTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Singleton type symbol of a Singleton type descriptor.
     */
    public interface SINGLETON {

        /**
         * Sets the value and its type symbol which is used to build the singleton type symbol. The value is expected
         * to be not null and belonging to matching type provided in the type symbol.
         *
         * @param value     The value of the singleton
         * @param typeSymbol The type symbol which the value provided is expected to be belonging to
         * @return The {@link SINGLETON} instance with the value and its type symbol being set
         */
        SINGLETON withValueSpace(Object value, TypeSymbol typeSymbol);

        /**
         * Build the Singleton type descriptor and returns the Singleton type symbol.
         *
         * @return The built {@link SingletonTypeSymbol}
         */
        SingletonTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Table type symbol of a Table type descriptor.
     */
    public interface TABLE {

        /**
         * Sets the row type to be used when building the table type symbol. The row type parameter is expected to be
         * a non-null valid type symbol.
         *
         * @param rowType     The non-null row type parameter
         * @return The {@link TABLE} instance with the row type parameter being set
         */
        TABLE withRowType(TypeSymbol rowType);

        /**
         * Sets the key constraint types to be used when building the table type symbol. The type symbols provided as
         * the key constraints shall be present as readonly and required fields of the row type parameter.
         *
         * @param keyTypes     The type symbols of the key constraints
         * @return The {@link TABLE} instance with the key constraints being set
         */
        TABLE withKeyConstraints(TypeSymbol... keyTypes);

        /**
         * Sets the key fields to be used when building the table type symbol. The key field names provided as the
         * key specifiers shall be present as readonly and required fields of the row type parameter.
         *
         * @param fieldNames     The set of key field names
         * @return The {@link TABLE} instance with the key specifiers being set
         */
        TABLE withKeySpecifiers(String... fieldNames);

        /**
         * Build the Table type descriptor and returns the Table type symbol.
         *
         * @return The built {@link TableTypeSymbol}
         */
        TableTypeSymbol build();
    }

    /**
     * Represents the methods required to build the Function type symbol of a Function type descriptor.
     */
    public interface FUNCTION {

        /**
         * Sets the parameter type symbols to be used when building the function type symbol.
         *
         * @param parameters     The set of parameters of the function type descriptor
         * @return The {@link FUNCTION} instance with the parameters being set
         */
        FUNCTION withParams(ParameterSymbol... parameters);

        /**
         * Sets the rest parameter type symbol to be used when building the function type symbol.
         *
         * @param restParam     The rest parameter of the function type descriptor
         * @return The {@link FUNCTION} instance with the rest parameter being set
         */
        FUNCTION withRestParam(ParameterSymbol restParam);

        /**
         * Sets the return type symbol to be used when building the function type symbol.
         *
         * @param returnType     The return type of the function type descriptor
         * @return The {@link FUNCTION} instance with the return type symbol being set
         */
        FUNCTION withReturnType(TypeSymbol returnType);

        /**
         * Returns a new instance of the Parameter builder to be used for building function parameters.
         *
         * @return A new instance of the {@link PARAMETER_BUILDER}
         */
        PARAMETER_BUILDER params();

        /**
         * Build the Function type descriptor and returns the Function type symbol.
         *
         * @return The built {@link FutureTypeSymbol}
         */
        FunctionTypeSymbol build();

        /**
         * Represents the methods required to build the Parameter type symbol of a function parameter.
         */
        interface PARAMETER_BUILDER {

            /**
             * Sets the name to be used when building the parameter symbol which would expect a non-null value, or
             * else an exception shall be thrown.
             *
             * @param name     The name of the function parameter
             * @return The {@link PARAMETER_BUILDER} instance with the name being set
             */
            PARAMETER_BUILDER withName(String name);

            /**
             * Sets the type symbol to be used when building the parameter symbol which would expect a non-null value,
             * or else an exception shall be thrown.
             *
             * @param type     The type of the function parameter
             * @return The {@link PARAMETER_BUILDER} instance with the type being set
             */
            PARAMETER_BUILDER withType(TypeSymbol type);

            /**
             * Sets the parameter kind to be used when building the parameter symbol. Required parameter kind would
             * be used as the default value if not provided.
             *
             * @param kind     The kind of the function parameter
             * @return The {@link PARAMETER_BUILDER} instance with the kind being set
             */
            PARAMETER_BUILDER withKind(ParameterKind kind);

            /**
             * Build the function parameter type descriptor and returns the parameter type symbol.
             *
             * @return The built {@link ParameterSymbol}
             */
            ParameterSymbol build();
        }
    }

    /**
     * Represents the methods required to build the Object type symbol of an Object type descriptor.
     */
    public interface OBJECT {

        /**
         * Sets the qualifiers to be used when building the object type symbol.
         *
         * @param qualifiers     The set of qualifiers of the object type descriptor
         * @return The {@link OBJECT} instance with the qualifiers being set
         */
        OBJECT withQualifier(Qualifier... qualifiers);

        /**
         * Sets the fields to be used when building the object type symbol.
         *
         * @param fields     The set of fields of the object type descriptor
         * @return The {@link OBJECT} instance with the fields being set
         */
        OBJECT withFields(OBJECT_FIELD... fields);

        /**
         * Sets the methods to be used when building the object type symbol.
         *
         * @param methods     The set of methods of the object type descriptor
         * @return The {@link OBJECT} instance with the methods being set
         */
        OBJECT withMethods(OBJECT_METHOD... methods);

        /**
         * Sets the type inclusions to be used when building the object type symbol.
         *
         * @param inclusions     The set of type inclusions of the object type descriptor
         * @return The {@link OBJECT} instance with the type inclusions being set
         */
        OBJECT withTypeInclusions(TypeReferenceTypeSymbol... inclusions);

        /**
         * Returns a new instance of the Object Field data holder.
         *
         * @return A new instance of the {@link OBJECT_FIELD}
         */
        OBJECT_FIELD fields();

        /**
         * Returns a new instance of the Object Method data holder.
         *
         * @return A new instance of the {@link OBJECT_METHOD}
         */
        OBJECT_METHOD methods();

        /**
         * Build the Object type descriptor and returns the Object type symbol.
         *
         * @return The built {@link ObjectTypeSymbol}
         */
        ObjectTypeSymbol build();

        /**
         * Represents the data holder used to store the required data of an object field when building an object type
         * descriptor.
         */
        interface OBJECT_FIELD {

            /**
             * Defines the object field as public.
             *
             * @return The {@link OBJECT_FIELD} which is defined as a public field.
             */
            OBJECT_FIELD isPublic();

            /**
             * Sets the type symbol of the object field.
             *
             * @param type The type of the object field
             * @return The {@link OBJECT_FIELD} instance with the type being set
             */
            OBJECT_FIELD withType(TypeSymbol type);

            /**
             * Sets the name of the object field.
             *
             * @param name The type of the object field
             * @return The {@link OBJECT_FIELD} instance with the name being set
             */
            OBJECT_FIELD withName(String name);

            /**
             * Check if the object field is set as public
             *
             * @return True if the object field is to be defined as public or else, False.
             */
            boolean isPublicField();

            /**
             * Gets the name value of the object field
             *
             * @return The name of the object field
             */
            String getName();

            /**
             * Gets the type symbol of the object field
             *
             * @return The type of the object field
             */
            TypeSymbol getType();

            /**
             * Gets the instance of this object field data holder if the name is not null. Otherwise, an exception
             * will be thrown.
             *
             * @return This instance of the {@link OBJECT_FIELD}
             */
            OBJECT_FIELD get();
        }

        /**
         * Represents the data holder used to store the required data of an object method when building an object type
         * descriptor.
         */
        interface OBJECT_METHOD {

            /**
             * Sets the qualifiers of the object method.
             *
             * @param qualifiers     The set of qualifiers of the object method
             * @return The {@link OBJECT_METHOD} instance with the qualifiers being set
             */
            OBJECT_METHOD withQualifiers(Qualifier... qualifiers);

            /**
             * Sets the name of the object method.
             *
             * @param name The type of the object method
             * @return The {@link OBJECT_METHOD} instance with the name being set
             */
            OBJECT_METHOD withName(String name);

            /**
             * Sets the type symbol of the object method.
             *
             * @param type The type of the object method
             * @return The {@link OBJECT_METHOD} instance with the type being set
             */
            OBJECT_METHOD withType(FunctionTypeSymbol type);

            /**
             * Gets the name value of the object method
             *
             * @return The name of the object method
             */
            String getName();

            /**
             * Gets the function type symbol of the object method
             *
             * @return The type of the object method
             */
            FunctionTypeSymbol getType();

            /**
             * Gets the instance of this object method data holder if the name is not null. Otherwise, an exception
             * will be thrown.
             *
             * @return This instance of the {@link OBJECT_METHOD}
             */
            OBJECT_METHOD get();
        }
    }

    /**
     * Represents the methods required to build the Record type symbol of a Record type descriptor.
     */
    public interface RECORD {

        /**
         * Returns a new instance of the Record Field data holder.
         *
         * @return A new instance of the {@link RECORD_FIELD}
         */
        RECORD_FIELD fields();

        /**
         * Sets the fields to be used when building the record type symbol.
         *
         * @param fields     The set of fields of the record type descriptor
         * @return The {@link RECORD} instance with the fields being set
         */
        RECORD withFields(RECORD_FIELD... fields);

        /**
         * Sets the rest field type symbol to be used when building the record type symbol.
         *
         * @param restType     The rest field type symbol of the record type descriptor
         * @return The {@link RECORD} instance with the rest field type symbol being set
         */
        RECORD withRestField(TypeSymbol restType);

        /**
         * Sets the type inclusions to be used when building the record type symbol.
         *
         * @param typeInclusions     The set of type inclusions of the record type descriptor
         * @return The {@link RECORD} instance with the type inclusions being set
         */
        RECORD withTypeInclusions(TypeReferenceTypeSymbol... typeInclusions);

        /**
         * Build the Record type descriptor and returns the Record type symbol.
         *
         * @return The built {@link RecordTypeSymbol}
         */
        RecordTypeSymbol build();

        /**
         * Represents the data holder used to store the required data of a record field when building a record type
         * descriptor.
         */
        interface RECORD_FIELD {

            /**
             * Defines the record field as readonly.
             *
             * @return The {@link RECORD_FIELD} which is defined as readonly.
             */
            RECORD_FIELD isReadOnly();

            /**
             * Sets the type symbol of the record field.
             *
             * @param type The type of the record field
             * @return The {@link RECORD_FIELD} instance with the type being set
             */
            RECORD_FIELD withType(TypeSymbol type);

            /**
             * Sets the name of the record field.
             *
             * @param name The type of the record field
             * @return The {@link RECORD_FIELD} instance with the name being set
             */
            RECORD_FIELD withName(String name);

            /**
             * Defines the record field as optional.
             *
             * @return The {@link RECORD_FIELD} which is defined as optional.
             */
            RECORD_FIELD isOptional();

            /**
             * Defines the record field as having a default value expression.
             *
             * @return The {@link RECORD_FIELD} which is defined as having a default value expression.
             */
            RECORD_FIELD hasDefaultExpr();

            /**
             * Check if the record field is set as readonly.
             *
             * @return True if the record field is to be defined as readonly or else, False.
             */
            boolean isFieldReadOnly();

            /**
             * Gets the type symbol of the record field
             *
             * @return The type of the record field
             */
            TypeSymbol getType();

            /**
             * Gets the name value of the record field
             *
             * @return The name of the record field
             */
            String getName();

            /**
             * Check if the record field is set as optional.
             *
             * @return True if the record field is to be defined as optional or else, False.
             */
            boolean isFieldOptional();

            /**
             * Check if the record field is set as having a default value expression.
             *
             * @return True if the record field is to be defined as having a default value expression or else, False.
             */
            boolean hasFieldDefaultExpr();

            /**
             * Gets the instance of this record field data holder if the name is not null. Otherwise, an exception
             * will be thrown.
             *
             * @return This instance of the {@link RECORD_FIELD}
             */
            RECORD_FIELD get();
        }
    }
}
