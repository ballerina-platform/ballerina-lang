/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.composer.service.workspace;

import org.ballerinalang.composer.service.workspace.model.Action;
import org.ballerinalang.composer.service.workspace.model.Annotation;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.model.Parameter;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorkspaceUtils {
    private WorkspaceUtils() {}

    /**
     * Get All Native Packages
     * @return {Map} <Package name, package functions and connectors>
     * */
    public static Map<String, ModelPackage> getAllPackages(GlobalScope globalScope) {
        Map<String, ModelPackage> packages = new HashMap<>();

//        globalScope.getSymbolMap().values().stream().forEach(symbol -> {
//            if (symbol instanceof NativePackageProxy) {
//                ((NativePackageProxy) symbol).load().getSymbolMap().values().stream().forEach(bLangSymbol -> {
//                    NativeUnitProxy nativeUnitProxy = (NativeUnitProxy) bLangSymbol;
//                    if (nativeUnitProxy.load() instanceof AbstractNativeFunction) {
//                        extractFunction(packages, nativeUnitProxy);
//                    } else if (nativeUnitProxy.load() instanceof AbstractNativeConnector) {
//                        extractConnector(packages, nativeUnitProxy);
//                    }
//                });
//            }
//        });
        return packages;
    }

    /**
     * Extract connectors from ballerina lang
     * @param packages packages to send
     * @param bLangSymbol Native unit of symbol
     * */
    private void extractConnector(Map<String, ModelPackage> packages, NativeUnitProxy bLangSymbol) {
//        AbstractNativeConnector abstractNativeConnector = (AbstractNativeConnector) bLangSymbol.load();
//        if (packages.containsKey(abstractNativeConnector.getPackagePath())) {
//            ModelPackage modelPackage = packages.get(abstractNativeConnector.getPackagePath());
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());
//
//            List<Parameter> returnParameters = new ArrayList<>();
//            addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());
//
//            List<Annotation> annotations = new ArrayList<>();
//            List<Action> actions = new ArrayList<>();
//            addActions(actions, abstractNativeConnector.getActions());
//
//            modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
//                    annotations, actions, parameters, returnParameters));
//        } else {
//            ModelPackage modelPackage = new ModelPackage();
//            modelPackage.setName(abstractNativeConnector.getPackagePath());
//
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());
//
//            List<Parameter> returnParameters = new ArrayList<>();
//            addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());
//
//            List<Annotation> annotations = new ArrayList<>();
//            List<Action> actions = new ArrayList<>();
//            addActions(actions, abstractNativeConnector.getActions());
//
//            modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
//                    annotations, actions, parameters, returnParameters));
//            packages.put(abstractNativeConnector.getPackagePath(), modelPackage);
//        }
    }

    /**
     * Extract Functions from ballerina lang.
     * @param packages packages to send.
     * @param bLangSymbol Native unit of symbol.
     * */
    private void extractFunction(Map<String, ModelPackage> packages, NativeUnitProxy bLangSymbol) {
        AbstractNativeFunction abstractNativeFunction = (AbstractNativeFunction) bLangSymbol.load();
//        if (packages.containsKey(abstractNativeFunction.getPackagePath())) {
//            ModelPackage modelPackage = packages.get(abstractNativeFunction.getPackagePath());
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());
//
//            List<Parameter> returnParameters = new ArrayList<>();
//            addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());
//
//            List<Annotation> annotations = new ArrayList<>();
//            addAnnotations(annotations, abstractNativeFunction.getAnnotations());
//
//            modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
//                    annotations, parameters, returnParameters));
//        } else {
//            ModelPackage modelPackage = new ModelPackage();
//            modelPackage.setName(abstractNativeFunction.getPackagePath());
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());
//
//            List<Parameter> returnParameters = new ArrayList<>();
//            addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());
//
//            List<Annotation> annotations = new ArrayList<>();
//            addAnnotations(annotations, abstractNativeFunction.getAnnotations());
//
//            modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
//                    annotations, parameters, returnParameters));
//            packages.put(abstractNativeFunction.getPackagePath(), modelPackage);
//        }
    }
}
