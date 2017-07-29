/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.symbols.TypeSymbolName;

/**
 * This class contain methods to resolve BType from different TypeName implementations.
 *
 * @since 0.92
 */
public class TypeNameResolver {
    private SymbolScope symbolScope;

    public TypeNameResolver(SymbolScope symbolScope) {
        this.symbolScope = symbolScope;
    }

    public BType resolve(BuiltinTypeName typeName) {
        return resolveTypeInternal(typeName);
    }

    public BType resolve(UserDefinedTypeName typeName) {
        return resolveTypeInternal(typeName);
    }

    public BType resolve(ConstrainedBuiltinTypeName typeName) {
        SimpleTypeName constraint = typeName.getConstraint();
        BType constraintType = constraint.resolveBType(this);

        BType constrainedType = null;
        if (typeName.getName().equals(TypeConstants.JSON_TNAME)) {
            constrainedType = new BJSONConstrainedType(constraintType);
        }

        return constrainedType;
    }

    public BType resolve(FunctionTypeName typeName) {
        BType[] paramTypes = new BType[typeName.getParamTypes().length];
        for (int i = 0; i < paramTypes.length; i++) {
            SimpleTypeName simpleTypeName = typeName.getParamTypes()[i];
            paramTypes[i] = simpleTypeName.resolveBType(this);
        }

        BType[] returnParamTypes = new BType[typeName.getReturnParamsTypes().length];
        for (int i = 0; i < returnParamTypes.length; i++) {
            SimpleTypeName simpleTypeName = typeName.getReturnParamsTypes()[i];
            returnParamTypes[i] = simpleTypeName.resolveBType(this);
        }

        BFunctionType functionType = new BFunctionType(symbolScope, paramTypes, returnParamTypes);
        functionType.setParametersFieldsNames(typeName.getParamFieldNames());
        functionType.setReturnsParametersFieldsNames(typeName.getParamFieldNames());
        functionType.setReturnWordAvailable(typeName.isReturnWordAvailable());
        return functionType;
    }

    public BType resolve(SchemaIDTypeName typeName) {
        return null;
    }

    private BType resolveTypeInternal(SimpleTypeName typeName) {
        BLangSymbol symbol = symbolScope.resolve(typeName.getSymbolName());
        if (symbol != null) {
            return (BType) symbol;
        }

        BType bType = null;
        if (typeName.isArrayType()) {
            bType = resolveArrayType(typeName);
        }

        return bType;
    }

    private BType resolveArrayType(SimpleTypeName typeName) {
        TypeSymbolName symbolName = new TypeSymbolName(typeName.getName(), typeName.getPackagePath());
        BLangSymbol symbol = symbolScope.resolve(symbolName);
        if (symbol == null) {
            return null;
        }

        BType arrayType = (BType) symbol;
        int dimensions = typeName.getDimensions();
        for (int i = 0; i < dimensions; i++) {
            arrayType = new BArrayType(arrayType);
        }

        symbolScope.define(typeName.getSymbolName(), arrayType);
        return arrayType;
    }
}
