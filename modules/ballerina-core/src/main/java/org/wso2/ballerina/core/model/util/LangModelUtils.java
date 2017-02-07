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
package org.wso2.ballerina.core.model.util;

import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.BType;

/**
 * Language model utils.
 */
public class LangModelUtils {

    public static SymbolName getSymNameWithParams(String identifier, ParameterDef[] parameterDefs) {
        StringBuilder stringBuilder = new StringBuilder(identifier);
        for (ParameterDef param : parameterDefs) {
            stringBuilder.append(".").append(param.getType());
        }
        return new SymbolName(stringBuilder.toString());
    }

    public static SymbolName getSymNameWithParams(String identifier, String pkgPath, ParameterDef[] parameterDefs) {
        StringBuilder stringBuilder = new StringBuilder(identifier);
        for (ParameterDef param : parameterDefs) {
            stringBuilder.append(".").append(param.getType());
        }
        return new SymbolName(stringBuilder.toString(), pkgPath);
    }

    public static SymbolName getSymNameWithParams(String identifier, String pkgPath, BType[] types) {
        StringBuilder sBuilder = new StringBuilder(identifier);
        for (BType type : types) {
            sBuilder.append(".").append(type);
        }

        return new SymbolName(sBuilder.toString(), pkgPath);
    }

    public static SymbolName getTypeConverterSymName(String pkgName, ParameterDef[] parameterDefs,
                                                     ParameterDef[] returnParams) {
        StringBuilder stringBuilder = new StringBuilder(pkgName + ":");
        for (ParameterDef param : parameterDefs) {
            stringBuilder.append(".").append(param.getType());
        }
        stringBuilder.append("->");
        for (ParameterDef param : returnParams) {
            stringBuilder.append("_").append(param.getType());
        }
        return new SymbolName(stringBuilder.toString());
    }

    public static SymbolName getTypeConverterSymNameWithoutPackage(BType source,
                                                     BType target) {
        return new SymbolName("." + source + "->" + "." + target, "ballerina.lang.convertors");
    }

    public static SymbolName getConnectorSymName(String identifier, String pkgPath) {
        String prefix;
        if (pkgPath == null) {
            prefix = identifier;
        } else {
            prefix = pkgPath + ":" + identifier;
        }

        return new SymbolName(prefix);
    }

    public static SymbolName getActionSymName(String actionName, String connectorName,
                                              String pkgPath, BType[] types) {
        String prefix;
        if (pkgPath == null) {
            prefix = connectorName + "." + actionName;
        } else {
            prefix = pkgPath + ":" + connectorName + "." + actionName;
        }

        StringBuilder sBuilder = new StringBuilder(prefix);
        for (BType type : types) {
            sBuilder.append("_").append(type);
        }

        return new SymbolName(sBuilder.toString());
    }
    
    /**
     * Get the symbol name of a struct field.
     * 
     * @param fieldName         Local name of the field
     * @param structName        Name os the struct to which this field belongs to
     * @param pkgPath           Package name of the struct
     * @return                  Symbol name of a struct field
     */
    public static SymbolName getStructFieldSymName(String fieldName, String structName, String pkgPath) {
        String prefix;
        if (pkgPath == null) {
            prefix = structName + "." + fieldName;
        } else {
            prefix = pkgPath + ":" + structName + "." + fieldName;
        }

        StringBuilder sBuilder = new StringBuilder(prefix);
        return new SymbolName(sBuilder.toString());
    }

    public static BType[] getTypesOfParams(ParameterDef[] parameterDefs) {
        BType[] types = new BType[parameterDefs.length];
        for (int i = 0; i < parameterDefs.length; i++) {
            types[i] = parameterDefs[i].getType();
        }
        return types;
    }

    public static String getNodeLocationStr(NodeLocation nodeLocation) {
        return nodeLocation.getFileName() + ":" + nodeLocation.getLineNumber() + ": ";
    }
}
