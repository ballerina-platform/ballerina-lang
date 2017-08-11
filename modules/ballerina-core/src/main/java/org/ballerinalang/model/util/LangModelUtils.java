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
package org.ballerinalang.model.util;

import org.ballerinalang.model.ActionSymbolName;
import org.ballerinalang.model.FunctionSymbolName;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.types.BType;

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

    public static FunctionSymbolName getFuncSymNameWithParams(String identifier, String pkgPath, BType[] types) {
        return new FunctionSymbolName(identifier, pkgPath, types.length);
    }

    public static SymbolName getTypeMapperSymName(String pkgName, BType source, BType target) {
        return new SymbolName("." + source + "->" + "." + target, pkgName);
    }

    public static SymbolName getConnectorSymName(String identifier, String pkgPath) {
        return new SymbolName(identifier, pkgPath);
    }

    public static SymbolName getResourceSymName(String resourceName, String pkgPath, String serviceName) {
        return new SymbolName(serviceName + "." + resourceName, pkgPath);
    }

    public static ActionSymbolName getActionSymName(String actionName, String pkgPath, String connectorName,
                                                    BType[] types) {
        return new ActionSymbolName(connectorName + "." + actionName, pkgPath, types.length);
    }

    public static ActionSymbolName getNativeActionSymName(String actionName, String connectorName,
                                              String pkgPath, BType[] types) {
        return new ActionSymbolName("NativeAction" + "." + connectorName + "." + actionName, pkgPath,
                types.length);
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
