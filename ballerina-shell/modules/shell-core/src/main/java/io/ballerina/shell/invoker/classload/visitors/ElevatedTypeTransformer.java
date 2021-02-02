/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.invoker.classload.visitors;

import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.shell.invoker.classload.ElevatedType;

/**
 * Visits the type of a type symbol and elevates the type  to either any/error/any|error.
 * If the type can be assigned to any the transformed result would be ANY or NONE.
 * If the type can be assigned to error the transformed result would be ERROR.
 * If the type can be assigned to any|error the transformed result would be ANY_ERROR.
 *
 * @since 2.0.0
 */
public class ElevatedTypeTransformer extends TypeSymbolTransformer<ElevatedType> {
    @Override
    protected void visit(UnionTypeSymbol symbol) {
        ElevatedType currentType = ElevatedType.NONE;
        for (TypeSymbol memberSymbol : symbol.memberTypeDescriptors()) {
            ElevatedType memberElevatedType = transformType(memberSymbol);
            if (currentType == ElevatedType.ANY_ERROR) {
                // Current type is already elevated to the top most.
                // None of the member types will change the type.
                break;
            } else if (currentType == ElevatedType.NONE) {
                // Current type is not set.
                // So, accept any type assigned by the member.
                currentType = memberElevatedType;
            } else if (memberElevatedType == ElevatedType.ANY_ERROR) {
                // Member is a top most type.
                // So the current type would get elevated to top most.
                currentType = ElevatedType.ANY_ERROR;
            } else if (currentType != memberElevatedType &&
                    memberElevatedType != ElevatedType.NONE) {
                // Not elevated type/member type are only any or error.
                // If they are not equal, will get elevated to any|error.
                // Otherwise no change.
                currentType = ElevatedType.ANY_ERROR;
            }
        }
        setState(currentType);
    }

    @Override
    protected void visit(IntersectionTypeSymbol symbol) {
        // Logic is similar to [UnionTypeSymbol] visit method.
        ElevatedType currentType = ElevatedType.ANY_ERROR;
        for (TypeSymbol memberSymbol : symbol.memberTypeDescriptors()) {
            ElevatedType memberElevatedType = transformType(memberSymbol);
            if (currentType.equals(ElevatedType.ANY_ERROR)) {
                currentType = memberElevatedType;
            } else if (currentType == ElevatedType.NONE) {
                break;
            } else if (memberElevatedType == ElevatedType.NONE) {
                currentType = ElevatedType.NONE;
            } else if (currentType != memberElevatedType &&
                    memberElevatedType != ElevatedType.ANY_ERROR) {
                currentType = ElevatedType.NONE;
            }
        }
        setState(currentType);
    }

    @Override
    protected void visit(ErrorTypeSymbol symbol) {
        setState(ElevatedType.ERROR);
    }

    @Override
    protected void visit(TypeReferenceTypeSymbol symbol) {
        setState(transformType(symbol.typeDescriptor()));
    }

    @Override
    protected void visit(TypeSymbol symbol) {
        setState(ElevatedType.ANY);
    }

    @Override
    protected void resetState() {
        setState(ElevatedType.NONE);
    }
}
