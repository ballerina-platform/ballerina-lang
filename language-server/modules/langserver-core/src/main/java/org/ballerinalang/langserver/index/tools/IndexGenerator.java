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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.index.DTOUtil;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.LSIndexImpl;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default index is generator.
 */
public class IndexGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

    private List<BPackageSymbol> getBLangPackages() {
        List<BPackageSymbol> bPackageSymbols = new ArrayList<>();
        List<String> packages = Arrays.asList("auth", "cache", "config", "crypto", "encoding", "file", "filepath",
                "grpc", "http", "io", "jdbc", "jms", "jwt", "ldap", "log", "math", "artemis", "nats", "rabbitmq",
                "mime", "oauth2", "observability", "openapi", "reflect", "socket", "streams", "system", "task", "test", 
                "time", "transactions", "websub", "xslt");
        CompilerContext tempCompilerContext = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
        packages.forEach(pkg -> {
            try {
                PackageID pkgID = new PackageID(new org.wso2.ballerinalang.compiler.util.Name("ballerina"),
                        new org.wso2.ballerinalang.compiler.util.Name(pkg),
                        new org.wso2.ballerinalang.compiler.util.Name(""));
                Optional<BPackageSymbol> pkgSymbol = LSPackageLoader.getPackageSymbolById(tempCompilerContext, pkgID);
                pkgSymbol.ifPresent(bPackageSymbols::add);
            } catch (Exception e) {
                logger.error("Cannot Load Package: ballerina/" + pkg);
                throw new RuntimeException("Cannot Load Package: ballerina/" + pkg, e);
            }
        });

        return bPackageSymbols;
    }

    public static void main(String[] args) {
        IndexGenerator indexGenerator = new IndexGenerator();
        LSIndexImpl lsIndex = new LSIndexImpl("classpath:lang-server-index.sql");
        List<BPackageSymbol> bPackageSymbols = indexGenerator.getBLangPackages();
        List<BLangPackageContent> bPackageSymbolDTOs = bPackageSymbols.stream()
                .filter(bPackageSymbol -> !CommonUtil.isInvalidSymbol(bPackageSymbol))
                .map(packageSymbol -> {
                    try {
                        return DTOUtil.getBLangPackageContent(packageSymbol);
                    } catch (Exception e) {
                        logger.error("Error Generating BLangPackageDTO");
                    }
                    return null;
                }).collect(Collectors.toList());
        indexGenerator.insertBLangPackages(bPackageSymbolDTOs, lsIndex);
        File file = new File(IndexGenerator.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        String saveDumpPath = file.getAbsolutePath().replaceAll("classes.*", "");
        // Following is to support both the gradle and maven builds
        if (saveDumpPath.endsWith("build" + CommonUtil.FILE_SEPARATOR)) {
            saveDumpPath += "ballerina-home/main/lib/tools/lang-server/resources/lang-server-index.sql";
        } else {
            saveDumpPath += "lib/tools/lang-server/resources/lang-server-index.sql";
        }
        lsIndex.saveIndexDump(Paths.get(saveDumpPath));
    }

    private void insertBLangPackages(List<BLangPackageContent> pkgContentList, LSIndexImpl lsIndex) {
        try {
            for (BLangPackageContent pkgContent : pkgContentList) {
                int id = ((BPackageSymbolDAO) lsIndex.getDaoFactory().get(DAOType.PACKAGE_SYMBOL))
                        .insert(pkgContent.getPackageSymbolDTO());
                insertBLangFunctions(id, pkgContent.getbInvokableSymbols(), lsIndex);
                insertBLangRecords(id, pkgContent.getRecordTypeSymbols(), lsIndex);
                insertOtherTypes(id, pkgContent.getOtherTypeSymbols(), lsIndex);
                insertBLangObjects(id, pkgContent.getObjectTypeSymbols(), lsIndex);
            }
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangPackages");
        }
    }

    private static void insertBLangFunctions(int pkgEntryId, List<BInvokableSymbol> symbols, LSIndexImpl lsIndex) {
        List<BFunctionSymbolDTO> dtos = symbols.stream()
                .filter(symbol -> !CommonUtil.isInvalidSymbol(symbol))
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
                .filter(recordTypeSymbol -> !CommonUtil.isInvalidSymbol(recordTypeSymbol))
                .map(recordTypeSymbol -> DTOUtil.getRecordTypeSymbolDTO(pkgEntryId, recordTypeSymbol))
                .collect(Collectors.toList());
        try {
            ((BRecordTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.RECORD_TYPE_SYMBOL)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangRecords");
        }
    }

    private void insertOtherTypes(int pkgEntryId, List<BSymbol> symbols, LSIndexImpl lsIndex) {
        List<OtherTypeSymbolDTO> dtos = symbols.stream()
                .filter(symbol -> !CommonUtil.isInvalidSymbol(symbol))
                .map(symbol -> DTOUtil.getOtherTypeSymbolDTO(pkgEntryId, symbol))
                .collect(Collectors.toList());
        try {
            ((BOtherTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OTHER_TYPE_SYMBOL)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert Other Type");
        }
    }

    private void insertBLangObjects(int pkgEntryId, List<BObjectTypeSymbol> objects, LSIndexImpl lsIndex)
            throws LSIndexException {
        List<BFunctionSymbolDTO> attachedFunctions = new ArrayList<>();
        List<Integer> objectIds = new ArrayList<>();

        List<BObjectTypeSymbolDTO> dtos = objects.stream()
                .filter(symbol -> !CommonUtil.isInvalidSymbol(symbol))
                .map(object -> DTOUtil.getObjectTypeSymbolDTO(pkgEntryId, object))
                .collect(Collectors.toList());
        try {
            objectIds = ((BObjectTypeSymbolDAO) lsIndex.getDaoFactory().get(DAOType.OBJECT_TYPE)).insertBatch(dtos);
        } catch (LSIndexException e) {
            logger.error("Error Insert BLangObjects");
        }

        if (objectIds.size() != objects.size()) {
            throw new LSIndexException("Error While inserting Object Type Symbols");
        }

        for (int i = 0; i < objects.size(); i++) {
            attachedFunctions.addAll(getObjectAttachedFunctionDTOs(pkgEntryId, objectIds.get(i), objects.get(i)));
        }

        try {
            ((BFunctionSymbolDAO) lsIndex.getDaoFactory().get(DAOType.FUNCTION_SYMBOL)).insertBatch(attachedFunctions);
        } catch (LSIndexException e) {
            logger.error("Error Updating Endpoint Action Holders");
        }
    }

    private List<BFunctionSymbolDTO> getObjectAttachedFunctionDTOs(int pkgId, int objId, BObjectTypeSymbol symbol) {
        return symbol.attachedFuncs.stream()
                .map(bAttachedFunction -> DTOUtil.getFunctionDTO(pkgId, objId, bAttachedFunction.symbol))
                .collect(Collectors.toList());
    }
}
