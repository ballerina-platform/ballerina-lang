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

import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.index.DTOUtil;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.langserver.index.ObjectType;
import org.ballerinalang.langserver.index.dao.BFunctionSymbolDAO;
import org.ballerinalang.langserver.index.dao.BObjectTypeSymbolDAO;
import org.ballerinalang.langserver.index.dao.BOtherTypeSymbolDAO;
import org.ballerinalang.langserver.index.dao.BPackageSymbolDAO;
import org.ballerinalang.langserver.index.dao.BRecordTypeSymbolDAO;
import org.ballerinalang.langserver.index.dao.DAOType;
import org.ballerinalang.langserver.index.dataholder.BLangPackageContent;
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Default index is generator.
 */
public class IndexGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

    private List<BPackageSymbol> getBLangPackages() {
        List<BPackageSymbol> bPackageSymbols = new ArrayList<>();
        List<String> packages = Arrays.asList("auth", "builtin", "cache", "config", "crypto", "file", "grpc", "h2",
                "http", "io", "jms", "log", "math", "mb", "mime", "mysql", "reflect", "runtime", "sql",
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
        LSIndexImpl lsIndex = new LSIndexImpl("classpath:lang-server-index.sql");
        List<BPackageSymbol> bPackageSymbols = indexGenerator.getBLangPackages();
        List<BLangPackageContent> bPackageSymbolDTOs = bPackageSymbols.stream()
                .map(packageSymbol -> {
                    try {
                        return DTOUtil.getBLangPackageContent(packageSymbol);
                    } catch (Exception e) {
                        logger.error("Error Generating BLangPackageDTO");
                    }
                    return null;
                }).collect(Collectors.toList());
        indexGenerator.insertBLangPackages(bPackageSymbolDTOs, lsIndex);
        ClassLoader classLoader = indexGenerator.getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("")).getFile());
        String saveDumpPath = file.getAbsolutePath().replace("classes", "");
        lsIndex.saveIndexDump(Paths.get(saveDumpPath + "lib/tools/lang-server/resources/lang-server-index.sql"));
    }

    private void insertBLangPackages(List<BLangPackageContent> pkgContentList, LSIndexImpl lsIndex) {
        try {
            for (BLangPackageContent pkgContent : pkgContentList) {
                int id = ((BPackageSymbolDAO) lsIndex.getDaoFactory().get(DAOType.PACKAGE_SYMBOL))
                        .insert(pkgContent.getPackageSymbolDTO());
                DTOUtil.ObjectCategories objectCategories =
                        DTOUtil.getObjectCategories(pkgContent.getObjectTypeSymbols());
                insertBLangFunctions(id, pkgContent.getbInvokableSymbols(), lsIndex);
                insertBLangRecords(id, pkgContent.getRecordTypeSymbols(), lsIndex);
                insertOtherTypes(id, pkgContent.getOtherTypeSymbols(), lsIndex);
                insertBLangObjects(id, objectCategories, lsIndex);
            }
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangPackages");
        }
    }

    private static void insertBLangFunctions(int pkgEntryId, List<BInvokableSymbol> symbols, LSIndexImpl lsIndex) {
        List<BFunctionSymbolDTO> dtos = symbols.stream()
                .map(bInvokableSymbol -> DTOUtil.getFunctionDTO(pkgEntryId, bInvokableSymbol))
                .collect(Collectors.toList());
        try {
            ((BFunctionSymbolDAO) lsIndex.getDaoFactory().get(DAOType.FUNCTION_SYMBOL)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangFunctions");
        }
    }

    private void insertBLangRecords(int pkgEntryId, List<BRecordTypeSymbol> symbols, LSIndexImpl lsIndex) {
        List<BRecordTypeSymbolDTO> dtos = symbols.stream()
                .map(recordTypeSymbol -> DTOUtil.getRecordTypeSymbolDTO(pkgEntryId, recordTypeSymbol))
                .collect(Collectors.toList());
        try {
            ((BRecordTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.RECORD_TYPE_SYMBOL)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangRecords");
        }
    }

    private void insertOtherTypes(int pkgEntryId, List<BTypeSymbol> symbols, LSIndexImpl lsIndex) {
        List<OtherTypeSymbolDTO> dtos = symbols.stream()
                .map(symbol -> DTOUtil.getOtherTypeSymbolDTO(pkgEntryId, symbol))
                .collect(Collectors.toList());
        try {
            ((BOtherTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OTHER_TYPE_SYMBOL)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert Other Type");
        }
    }

    private void insertBLangObjects(int pkgEntryId, DTOUtil.ObjectCategories categories, LSIndexImpl lsIndex) {
        List<BFunctionSymbolDTO> objectAttachedFunctions = new ArrayList<>();
        List<Integer> epIds = insertBLangObjects(pkgEntryId, categories.getEndpoints(), ObjectType.ENDPOINT, lsIndex);
        List<Integer> actionHolderIds = insertBLangObjects(pkgEntryId, categories.getEndpointActionHolders(),
                ObjectType.ACTION_HOLDER, lsIndex);
        List<Integer> objectIds = insertBLangObjects(pkgEntryId, categories.getObjects(), ObjectType.OBJECT, lsIndex);

        for (int i = 0; i < categories.getEndpointActionHolders().size(); i++) {
            objectAttachedFunctions.addAll(getObjectAttachedFunctionDTOs(pkgEntryId, actionHolderIds.get(i),
                    categories.getEndpointActionHolders().get(i)));
        }

        for (int i = 0; i < categories.getObjects().size(); i++) {
            objectAttachedFunctions.addAll(getObjectAttachedFunctionDTOs(pkgEntryId, objectIds.get(i),
                    categories.getObjects().get(i)));
        }

        try {
            ((BObjectTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OBJECT_TYPE))
                    .updateActionHolderIDs(epIds, actionHolderIds);
            ((BFunctionSymbolDAO) lsIndex.getDaoFactory().get(DAOType.FUNCTION_SYMBOL))
                    .insertBatch(objectAttachedFunctions);
        } catch (LSIndexException e) {
            logger.error("Error Updating Endpoint Action Holders");
        }
    }
    
    private List<BFunctionSymbolDTO> getObjectAttachedFunctionDTOs(int pkgId, int objId, BObjectTypeSymbol symbol) {
        return symbol.attachedFuncs.stream()
                .map(bAttachedFunction -> DTOUtil.getFunctionDTO(pkgId, objId, bAttachedFunction.symbol))
                .collect(Collectors.toList());
    }

    private static List<Integer> insertBLangObjects(int pkgEntryId, List<BObjectTypeSymbol> bLangObjects,
                                                    ObjectType type, LSIndexImpl lsIndex) {
        List<BObjectTypeSymbolDTO> dtos = bLangObjects.stream()
                .map(object -> DTOUtil.getObjectTypeSymbolDTO(pkgEntryId, object, type))
                .collect(Collectors.toList());
        try {
            return ((BObjectTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OBJECT_TYPE)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangObjects");
        }
        return new ArrayList<>();
    }
}
