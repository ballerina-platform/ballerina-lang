/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.langserver.util;

import com.google.gson.JsonArray;
import org.ballerinalang.composer.service.workspace.langserver.consts.LangServerConstants;
import org.ballerinalang.composer.service.workspace.langserver.consts.SymbolKind;
import org.ballerinalang.composer.service.workspace.langserver.dto.SymbolInformationDTO;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.utils.BallerinaProgramContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provider for the workspace symbols
 */
public class WorkspaceSymbolProvider {

    private static Logger logger = LoggerFactory.getLogger(WorkspaceSymbolProvider.class);
    private static BallerinaProgramContentProvider contentProvider = BallerinaProgramContentProvider.getInstance();

    /**
     * Constructor
     */
    public WorkspaceSymbolProvider() {
    }

    /**
     * Router to get the particular symbol type
     * @param query symbol query string
     * @return SymbolInformationDTO array
     */
    public SymbolInformationDTO[] getSymbols(String query) {
        switch (query) {
            case LangServerConstants.BUILTIN_TYPES:
                return getBuiltinTypes();
            case LangServerConstants.PACKAGES:
                return getPackages();
            default:
                logger.warn("Invalid symbol query found");
                return new SymbolInformationDTO[0];
        }
    }

    /**
     * Get the builtin types
     * @return SymbolInformationDTO array
     */
    private SymbolInformationDTO[] getBuiltinTypes() {
        JsonArray builtinTypes = contentProvider.builtinTypes();
        ArrayList<SymbolInformationDTO> symbolInformationArr = new ArrayList<>();
        for (int itr = 0; itr < builtinTypes.size(); itr++) {
            SymbolInformationDTO symbolInfo = new SymbolInformationDTO();
            symbolInfo.setName(builtinTypes.get(itr).getAsString());
            symbolInfo.setKind(SymbolKind.BUILTIN_TYPE);
            symbolInformationArr.add(symbolInfo);
        }

        return symbolInformationArr.toArray(new SymbolInformationDTO[0]);
    }

    /**
     * Get the packages
     * @return SymbolInformationDTO array
     */
    public SymbolInformationDTO[] getPackages() {
        Map<String, ModelPackage> packages = contentProvider.getAllPackages();
        ArrayList<SymbolInformationDTO> symbolInformationArr = new ArrayList<>();

        for (Map.Entry<String, ModelPackage> entry : packages.entrySet()) {
            SymbolInformationDTO symbolInfo = new SymbolInformationDTO();
            symbolInfo.setName(entry.getKey());
            symbolInfo.setKind(SymbolKind.PACKAGE_DEF);
            symbolInformationArr.add(symbolInfo);
        }

        return symbolInformationArr.toArray(new SymbolInformationDTO[0]);
    }
}
