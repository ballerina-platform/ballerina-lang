/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.index.tools;

import org.ballerinalang.langserver.common.utils.index.DTOUtil;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.dto.BFunctionDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.ObjectType;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default index is generator.
 */
public class IndexGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

    private List<BPackageSymbol> getBLangPackages() {
        List<BPackageSymbol> bPackageSymbols = new ArrayList<>();
        List<String> packages = Arrays.asList("auth", "builtin", "cache", "config", "crypto", "file", "grpc", "h2",
                "http", "io", "jdbc", "jms", "log", "math", "mb", "mime", "mysql", "reflect", "runtime", "sql",
                "swagger", "system", "task", "time", "transactions", "websub");
        CompilerContext tempCompilerContext = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
        packages.forEach(pkg -> {
            try {
                PackageID packageID = new PackageID(new org.wso2.ballerinalang.compiler.util.Name("ballerina"),
                        new org.wso2.ballerinalang.compiler.util.Name(pkg),
                        new org.wso2.ballerinalang.compiler.util.Name(""));
                BPackageSymbol bPackageSymbol = LSPackageLoader.getPackageSymbolById(tempCompilerContext, packageID);
                bPackageSymbols.add(bPackageSymbol);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                logger.error("Cannot Load Package: ballerina/" + pkg);
            }
        });

        return bPackageSymbols;
    }

    public static void main(String[] args) {
        IndexGenerator indexGenerator = new IndexGenerator();
        LSIndexImpl.getInstance().init(null);
        List<BPackageSymbol> bPackageSymbols = indexGenerator.getBLangPackages();
        List<BPackageSymbolDTO> bPackageSymbolDTOs = bPackageSymbols.stream()
                .map(packageSymbol -> {
                    try {
                        return DTOUtil.getBLangPackageDTO(packageSymbol);
                    } catch (Exception e) {
                        logger.error("Error Generating BLangPackageDTO");
                    }
                    return null;
                }).collect(Collectors.toList());
        indexGenerator.insertBLangPackages(bPackageSymbolDTOs);
        String saveDumpPath = Paths.get("modules/langserver-core/target/").toAbsolutePath().toString();
        LSIndexImpl.getInstance()
                .saveIndexDump(Paths.get(new File(saveDumpPath + File.separator + "lang-server-index.sql").toURI()));
    }

    private void insertBLangPackages(List<BPackageSymbolDTO> packageSymbolDTOs) {
        // TODO: introduce DTO factory
        List<Integer> generatedPkgKeys;
        try {
            generatedPkgKeys = LSIndexImpl.getInstance().getQueryProcessor()
                    .batchInsertBPackageSymbols(packageSymbolDTOs);
        } catch (SQLException e) {
            logger.error("Error Insert BLangPackages");
            return;
        }
        for (int i = 0; i < packageSymbolDTOs.size(); i++) {
            DTOUtil.ObjectCategories objectCategories =
                    DTOUtil.getObjectCategories(packageSymbolDTOs.get(i).getObjectTypeSymbols());
            insertBLangFunctions(generatedPkgKeys.get(i), packageSymbolDTOs.get(i).getBInvokableSymbols());
            insertBLangRecords(generatedPkgKeys.get(i), packageSymbolDTOs.get(i).getRecordTypeSymbols());
            insertBLangObjects(generatedPkgKeys.get(i), objectCategories);
        }
    }

//    private void insertBLangServices(int pkgEntryId, List<BLangService> bLangServices) {
//        List<Integer> generatedKeys;
//        List<List<BLangResource>> bLangResources = new ArrayList<>();
//        List<BLangServiceDTO> bLangServiceDTOs = bLangServices.stream().map(bLangService -> {
//            bLangResources.add(bLangService.getResources());
//            return this.getServiceDTO(pkgEntryId, bLangService);
//        }).collect(Collectors.toList());
//        try {
//            generatedKeys = LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangServices(bLangServiceDTOs);
//            for (int i = 0; i < generatedKeys.size(); i++) {
//                this.insertBLangResources(generatedKeys.get(i), bLangResources.get(i));
//            }
//        } catch (SQLException e) {
//            // TODO: Handle Properly
//        }
//    }

    private static void insertBLangFunctions(int pkgEntryId, List<BInvokableSymbol> bInvokableSymbols) {
        List<BFunctionDTO> bFunctionDTOs = bInvokableSymbols.stream()
                .map(bInvokableSymbol -> DTOUtil.getFunctionDTO(pkgEntryId, bInvokableSymbol))
                .collect(Collectors.toList());
        try {
            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangFunctions(bFunctionDTOs);
        } catch (SQLException | IOException e) {
            logger.error("Error Insert BLangFunctions");
        }
    }

    private void insertBLangRecords(int pkgEntryId, List<BRecordTypeSymbol> bRecordTypeSymbols) {
        List<BRecordTypeSymbolDTO> bRecordTypeSymbolDTOs = bRecordTypeSymbols.stream()
                .map(recordTypeSymbol -> DTOUtil.getRecordTypeSymbolDTO(pkgEntryId, recordTypeSymbol))
                .collect(Collectors.toList());
        try {
            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangRecords(bRecordTypeSymbolDTOs);
        } catch (SQLException | IOException e) {
            logger.error("Error Insert BLangRecords");
        }
    }

    private void insertBLangObjects(int pkgEntryId, DTOUtil.ObjectCategories categories) {
        List<Integer> epIds = insertBLangObjects(pkgEntryId, categories.getEndpoints(), ObjectType.ENDPOINT);
        List<Integer> actionHolderIds = insertBLangObjects(pkgEntryId, categories.getEndpointActionHolders(),
                ObjectType.ACTION_HOLDER);
        insertBLangObjects(pkgEntryId, categories.getObjects(), ObjectType.OBJECT);
        try {
            LSIndexImpl.getInstance().getQueryProcessor().batchUpdateActionHolderId(epIds, actionHolderIds);
        } catch (SQLException e) {
            logger.error("Error Updating Endpoint Action Holders");
        }
    }

    private static List<Integer> insertBLangObjects(int pkgEntryId, List<BObjectTypeSymbol> bLangObjects,
                                                    ObjectType type) {
        List<BObjectTypeSymbolDTO> bLangObjectDTOs = bLangObjects.stream()
                .map(object -> DTOUtil.getObjectTypeSymbolDTO(pkgEntryId, object, type))
                .collect(Collectors.toList());
        try {
            return LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangObjects(bLangObjectDTOs);
        } catch (SQLException | IOException e) {
            logger.error("Error Insert BLangObjects");
        }
        return new ArrayList<>();
    }

//    private void insertBLangResources(int serviceEntryId, List<BLangResource> bLangResources) {
//        List<BLangResourceDTO> bLangResourceDTOs = bLangResources.stream()
//                .map(resource -> this.getResourceDTO(serviceEntryId, resource))
//                .collect(Collectors.toList());
//        try {
//            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangResources(bLangResourceDTOs);
//        } catch (SQLException e) {
//            // TODO: Handle Properly
//        }
//    }
}
