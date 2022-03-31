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
package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;

public abstract class TypeBuilder {

    public XMLBuilder XML;
    public MapTypeBuilder MAP;

    public interface XMLBuilder {

        XMLBuilder withTypeParam(TypeSymbol typeParam);

        XMLTypeSymbol build();
    }

    public interface MapTypeBuilder {
        MapTypeBuilder withTypeParam(TypeSymbol typeParam);

        MapTypeSymbol build();
    }

    public interface FutureTypeBuilder {
        TypeSymbol withTypeParam(TypeSymbol typeParam);

        FutureTypeSymbol build();
    }

    public interface TypeDescTypeBuilder {
        TypeSymbol withTypeParam(TypeSymbol typeParam);

        TypeDescTypeSymbol build();
    }

    public interface ErrorTypeBuilder {

        ErrorTypeBuilder withTypeParam(TypeSymbol typeParam);

        ErrorTypeBuilder isDistinct();

        ErrorTypeSymbol build();
    }

    public interface FunctionTypeBuilder {

        FunctionTypeBuilder withQualifiers(Qualifier... qualifiers);

        ParamBuilder params();

        FunctionTypeBuilder withParams(ParameterSymbol... params);

        FunctionTypeBuilder withRestParams(ParameterSymbol restParam);

        FunctionTypeBuilder withReturnType(TypeSymbol returnType);

        FunctionTypeBuilder withReturnTypeAnnots(Annotatable... annots);

        FunctionTypeSymbol build();

        interface ParamBuilder {

            ParamBuilder withName(String name);

            ParamBuilder withType(TypeSymbol type);

            ParamBuilder ofKind(ParameterKind kind);

            ParamBuilder build();
        }
    }

}
