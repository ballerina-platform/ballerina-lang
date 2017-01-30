/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.runtime.registry;

import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.util.LangModelUtils;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;

import java.util.HashMap;

/**
 * The place where all the package definitions are stored.
 *
 * @since 0.8.0
 */
public class PackageRegistry {

    HashMap<String, Package> packages = new HashMap<String, Package>();

    private static PackageRegistry instance = new PackageRegistry();

    private PackageRegistry() {
    }

    public static PackageRegistry getInstance() {
        return instance;
    }

//    public void registerPackage(Package aPackage) {
//        packages.put(aPackage.getFullyQualifiedName(), aPackage);
//    }

//    public Package getPackage(String fqn) {
//        return packages.get(fqn);
//    }

    /**
     * Register Native Function.
     *
     * @param function AbstractNativeFunction instance.
     */
    public void registerNativeFunction(AbstractNativeFunction function) {
        Package aPackage = packages
                .computeIfAbsent(function.getPackagePath(), k -> new Package(function.getPackagePath()));

        if (function.isPublic()) {
            aPackage.getPublicFunctions().put(function.getName(), function);
        } else {
            aPackage.getPrivateFunctions().put(function.getName(), function);
        }

        String funcName = function.getName();
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(funcName, function.getParameterDefs());
        Symbol symbol = new Symbol(function);

        GlobalScopeHolder.getInstance().insert(symbolName, symbol);
    }

    /**
     * Register Native Action.
     *
     * @param action AbstractNativeAction instance.
     */
    public void registerNativeAction(AbstractNativeAction action) {
        Package aPackage = packages
                .computeIfAbsent(action.getPackagePath(), k -> new Package(action.getPackagePath()));
        aPackage.getActions().put(action.getName(), action);

        String actionName = action.getSymbolName().getName();
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(actionName, action.getParameterDefs());
        Symbol symbol = new Symbol(action);

        GlobalScopeHolder.getInstance().insert(symbolName, symbol);

    }

    /**
     * Unregister Native function.
     *
     * @param function AbstractNativeFunction instance.
     */
    public void unregisterNativeFunctions(AbstractNativeFunction function) {
        Package aPackage = packages.get(function.getPackagePath());
        if (aPackage == null) {
            // Nothing to do.
            return;
        }
        if (function.isPublic()) {
            aPackage.getPublicFunctions().remove(function.getName());
        } else {
            aPackage.getPrivateFunctions().remove(function.getName());
        }
    }

    /**
     * Unregister Native Action.
     *
     * @param action AbstractNativeAction instance.
     */
    public void unregisterNativeActions(AbstractNativeAction action) {
        Package aPackage = packages.get(action.getPackagePath());
        if (aPackage == null) {
            // Nothing to do.
            return;
        }
        aPackage.getActions().remove(action.getName());
    }

    /**
     * Register Native Connector.
     *
     * @param connector AbstractNativeAction instance.
     */
    public void registerNativeConnector(AbstractNativeConnector connector) {

        String connectorName = connector.getSymbolName().getName();
        //SymbolName symbolName = SymbolUtils.getSymNameWithParams(CONNECTOR_NAME, connector.getParameters());
        Symbol symbol = new Symbol(connector);

        GlobalScopeHolder.getInstance().insert(new SymbolName(connectorName), symbol);

    }

//    /**
//     * Unregister Native Action.
//     *
//     * @param action AbstractNativeAction instance.
//     */
//    public void unregisterNativeConnector(AbstractNativeAction action) {
//        Package aPackage = packages.get(action.getPackageName());
//        if (aPackage == null) {
//            // Nothing to do.
//            return;
//        }
//        aPackage.getActions().remove(action.getName());
//    }
}
