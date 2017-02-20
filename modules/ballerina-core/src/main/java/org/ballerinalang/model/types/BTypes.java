/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.util.exceptions.SemanticException;

import static org.ballerinalang.model.util.LangModelUtils.getNodeLocationStr;

/**
 * This class contains various methods manipulate {@link BType}s in Ballerina.
 *
 * @since 0.8.0
 */
public class BTypes {
    public static BType typeInt;
    public static BType typeLong;
    public static BType typeFloat;
    public static BType typeDouble;
    public static BType typeBoolean;
    public static BType typeString;
    public static BType typeXML;
    public static BType typeJSON;
    public static BType typeMessage;
    public static BType typeMap;
    public static BType typeException;
    public static BType typeDatatable;

    private static boolean initialized = false;

    private BTypes() {
    }

    public static synchronized void loadBuiltInTypes(GlobalScope globalScope) {
        if (!initialized) {
            createBuiltInTypes(globalScope);
        }

        globalScope.define(typeInt.getSymbolName(), typeInt);
        globalScope.define(typeLong.getSymbolName(), typeLong);
        globalScope.define(typeFloat.getSymbolName(), typeFloat);
        globalScope.define(typeDouble.getSymbolName(), typeDouble);
        globalScope.define(typeBoolean.getSymbolName(), typeBoolean);
        globalScope.define(typeString.getSymbolName(), typeString);
        globalScope.define(typeXML.getSymbolName(), typeXML);
        globalScope.define(typeJSON.getSymbolName(), typeJSON);
        globalScope.define(typeMessage.getSymbolName(), typeMessage);
        globalScope.define(typeMap.getSymbolName(), typeMap);
        globalScope.define(typeException.getSymbolName(), typeException);
        globalScope.define(typeDatatable.getSymbolName(), typeDatatable);

        TypeLattice.loadImplicitCastLattice(globalScope);
        TypeLattice.loadExplicitCastLattice(globalScope);

    }

    private static void createBuiltInTypes(GlobalScope globalScope) {
        typeInt = new BIntegerType(TypeConstants.INT_TNAME, null, globalScope);
        typeLong = new BLongType(TypeConstants.LONG_TNAME, null, globalScope);
        typeFloat = new BFloatType(TypeConstants.FLOAT_TNAME, null, globalScope);
        typeDouble = new BDoubleType(TypeConstants.DOUBLE_TNAME, null, globalScope);
        typeBoolean = new BBooleanType(TypeConstants.BOOLEAN_TNAME, null, globalScope);
        typeString = new BStringType(TypeConstants.STRING_TNAME, null, globalScope);
        typeXML = new BXMLType(TypeConstants.XML_TNAME, null, globalScope);
        typeJSON = new BJSONType(TypeConstants.JSON_TNAME, null, globalScope);
        typeMessage = new BMessageType(TypeConstants.MESSAGE_TNAME, null, globalScope);
        typeMap = new BMapType(TypeConstants.MAP_TNAME, null, globalScope);
        typeException = new BExceptionType(TypeConstants.EXCEPTION_TNAME, null, globalScope);
        typeDatatable = new BDataTableType(TypeConstants.DATATABLE_TNAME, null, globalScope);
        initialized = true;
    }

    public static BArrayType getArrayType(String elementTypeName) {
        return null;
    }

    public static BType resolveType(SimpleTypeName typeName, SymbolScope symbolScope, NodeLocation location) {
        BType bType = null;
        BLangSymbol symbol = symbolScope.resolve(typeName.getSymbolName());
        if (symbol instanceof NativeUnitProxy) {
            AbstractNativeConnector connector = (AbstractNativeConnector) ((NativeUnitProxy) symbol).load();
            bType = connector;
        } else if (symbol instanceof BType) {
            bType = (BType) symbol;
        }

        if (bType != null) {
            return bType;
        }

        // Now check whether this is an arrays type
        if (typeName.isArrayType()) {
            BLangSymbol bTypeSymbol = symbolScope.resolve(new SymbolName(typeName.getName(), 
                    typeName.getPackagePath()));
            if (bTypeSymbol instanceof BType) {
                bType = (BType) bTypeSymbol;
            }
        }

        // If bType is not null, then element type of this arrays type is available.
        // We should define the arrays type here.
        if (bType != null) {
            BArrayType bArrayType = new BArrayType(typeName.getSymbolName().getName(),
                    bType, typeName.getPackagePath(), bType.getSymbolScope());
            bType.getSymbolScope().define(typeName.getSymbolName(), bArrayType);
            return bArrayType;
        }


        throw new SemanticException(getNodeLocationStr(location) + "undefined type '" + typeName + "'");
    }

    public static boolean isValueType(BType type) {
        if (type == BTypes.typeInt ||
                type == BTypes.typeString ||
                type == BTypes.typeLong ||
                type == BTypes.typeFloat ||
                type == BTypes.typeDouble ||
                type == BTypes.typeBoolean) {
            return true;
        }

        return false;
    }
}
