/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.configurations.ConfigModuleDetails;
import io.ballerina.projects.configurations.ConfigVariable;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class to read configurable variables defined in a package.
 *
 * @since 2.0.0
 */
public class ConfigReader {

    /**
     * Retrieve configurable variables in a package as a per module map.
     *
     * @param packageInstance to read configurable variables
     * @return A map with ConfigModuleDetails(module details) as key and
     * List of ConfigVariable (configurable variables in the module) as value
     */
    public static Map<ConfigModuleDetails, List<ConfigVariable>> getConfigVariables(Package packageInstance) {
        Map<ConfigModuleDetails, List<ConfigVariable>> configDetails = new HashMap<>();
        for (Module module : packageInstance.modules()) {
            getConfigs(module.moduleName(), module.moduleContext().bLangPackage(), configDetails);
        }
        return configDetails;
    }

    /**
     * Update provided map with the configurable variable details for the given module.
     *
     * @param moduleName ModuleName to retrieve module details
     * @param bLangPackage to retrieve configurable variable details
     * @param configDetails Map to store the configurable variables against module
     */
    private static void getConfigs(ModuleName moduleName,
                                   BLangPackage bLangPackage, Map<ConfigModuleDetails,
            List<ConfigVariable>> configDetails) {
        List<ConfigVariable> configVariables = new ArrayList<>();
        PackageID currentPkgId = bLangPackage.symbol.pkgID;
        for (Scope.ScopeEntry entry : bLangPackage.symbol.scope.entries.values()) {
            BSymbol symbol = entry.symbol;
            // Filter configurable variables
            if (symbol != null && symbol.tag == SymTag.VARIABLE && Symbols.isFlagOn(symbol.flags,
                    Flags.CONFIGURABLE)) {
                if (symbol instanceof BVarSymbol) {
                    BVarSymbol varSymbol = (BVarSymbol) symbol;
                    configVariables.add(new ConfigVariable(
                            varSymbol.name.value, varSymbol.type, Symbols.isFlagOn(varSymbol.flags, Flags.REQUIRED)));
                }
            }
        }
        if (!configVariables.isEmpty()) {
            // Add configurable variable details for the current package
            configDetails.put(getConfigModuleDetails(moduleName, currentPkgId, true), configVariables);
        }
    }

    /**
     * Get module details to store the configurable variables against.
     *
     * @param moduleName to retrieve module details
     * @param packageID to retrieve package details
     * @param isCurrentPackage flag to denote current package or not
     * @return module details stored in object ConfigModuleDetails
     */
    private static ConfigModuleDetails getConfigModuleDetails(ModuleName moduleName, PackageID packageID,
                                                              boolean isCurrentPackage) {
        String orgName = packageID.getOrgName().getValue();
        String packageName = packageID.getPkgName().getValue();
        String moduleNameVal = moduleName.isDefaultModuleName() ? moduleName.toString() : moduleName.moduleNamePart();
        return new ConfigModuleDetails(orgName, packageName, moduleNameVal, isCurrentPackage);
    }

}
