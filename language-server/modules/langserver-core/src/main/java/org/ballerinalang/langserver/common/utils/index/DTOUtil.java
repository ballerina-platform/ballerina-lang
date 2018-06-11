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
package org.ballerinalang.langserver.common.utils.index;

import com.google.gson.Gson;
import org.ballerinalang.langserver.common.utils.completion.BLangFunctionUtil;
import org.ballerinalang.langserver.common.utils.completion.BLangPackageUtil;
import org.ballerinalang.langserver.index.dto.BFunctionDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.ObjectType;
import org.ballerinalang.langserver.index.dto.PackageIDDTO;
import org.ballerinalang.langserver.index.dto.TypeDTO;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for DTO manipulation.
 */
public class DTOUtil {
    
    private static final String GET_CALLER_ACTIONS = "getCallerActions";

    private static final Gson gson = new Gson();

    public static TypeDTO getTypeDTO(BType bType) {
        BTypeSymbol typeSymbol;
        String name = "";
        PackageID packageID = null;
        List<TypeDTO> memberTypes = new ArrayList<>();
        if (bType instanceof BArrayType) {
            typeSymbol = ((BArrayType) bType).eType.tsymbol;
            name = typeSymbol.getName().getValue();
            packageID = typeSymbol.pkgID;
        } else if (bType instanceof BUnionType) {
            BUnionType unionType = (BUnionType) bType;
            unionType.getMemberTypes().forEach(memberBType -> {
                memberTypes.add(getTypeDTO(memberBType));
            });
            name = bType.toString();
        } else {
            typeSymbol = bType.tsymbol;
            name = typeSymbol.getName().getValue();
            packageID = typeSymbol.pkgID;
        }
        
        PackageIDDTO packageIDDTO = packageID == null ? null :
                new PackageIDDTO(
                        packageID.getName().getValue(),
                        packageID.getOrgName().getValue(),
                        packageID.getPackageVersion().getValue()
                );

        return new TypeDTO(packageIDDTO, name, memberTypes);
    }
    
    public static BPackageSymbolDTO getBLangPackageDTO(BPackageSymbol packageSymbol) {
        PackageID packageID = packageSymbol.pkgID;
        PackageIDDTO packageIDDTO = new PackageIDDTO(
                packageID.getName().getValue(),
                packageID.getOrgName().getValue(),
                packageID.getPackageVersion().getValue()
        );
        BPackageSymbolDTO packageSymbolDTO = new BPackageSymbolDTO(packageIDDTO);
            packageSymbol.scope.entries.entrySet().forEach(entry -> {
            BSymbol symbol = entry.getValue().symbol;
            if (symbol.kind != null) {
                switch (symbol.kind) {
                    case OBJECT:
                        packageSymbolDTO.getObjectTypeSymbols().add((BObjectTypeSymbol) symbol);
                        break;
                    case RECORD:
                        packageSymbolDTO.getRecordTypeSymbols().add((BRecordTypeSymbol) symbol);
                        break;
                    case FUNCTION:
                        packageSymbolDTO.getBInvokableSymbols().add((BInvokableSymbol) symbol);
                        break;
                    case SERVICE:
                        packageSymbolDTO.getbServiceSymbols().add((BServiceSymbol) symbol);
                    default:
                        break;
                }
            }
        });

        return packageSymbolDTO;
    }

    public static BLangServiceDTO getServiceDTO(int pkgEntryId, BServiceSymbol bServiceSymbol) {
        return new BLangServiceDTO(pkgEntryId, bServiceSymbol.getName().getValue());
    }

    public static BFunctionDTO getFunctionDTO(int pkgEntryId, BInvokableSymbol bInvokableSymbol) {
        CompletionItem completionItem = BLangFunctionUtil.getFunctionCompletionItem(bInvokableSymbol);
        return new BFunctionDTO(pkgEntryId, -1, bInvokableSymbol.getName().getValue(), completionItem);
    }

    public static BObjectTypeSymbolDTO getObjectTypeSymbolDTO(int pkgEntryId, BObjectTypeSymbol bObjectTypeSymbol,
                                                     ObjectType type) {
        CompletionItem completionItem = null;
        if(type == ObjectType.OBJECT) {
            completionItem = BLangPackageUtil.getBTypeCompletionItem(bObjectTypeSymbol.getName().getValue());
        }
        return new BObjectTypeSymbolDTO(pkgEntryId, bObjectTypeSymbol.getName().getValue(), null, type, completionItem);
    }

    public static BRecordTypeSymbolDTO getRecordTypeSymbolDTO(int pkgEntryId, BRecordTypeSymbol recordTypeSymbol) {
        CompletionItem completionItem = BLangPackageUtil.getBTypeCompletionItem(recordTypeSymbol.getName().getValue());
        return new BRecordTypeSymbolDTO(pkgEntryId, recordTypeSymbol.getName().getValue(), null, completionItem);
    } 

    private static BLangResourceDTO getResourceDTO(int serviceEntryId, BLangResource bLangResource) {
        return new BLangResourceDTO(serviceEntryId, bLangResource.getName().getValue());
    }
    
    public static String completionItemToJSON(CompletionItem completionItem) {
        return gson.toJson(completionItem);
    }

    public static ObjectCategories getObjectCategories(List<BObjectTypeSymbol> objectTypeSymbols) {
        ObjectCategories objectCategories = new ObjectCategories();

        for (BObjectTypeSymbol objectTypeSymbol : objectTypeSymbols) {
            // Filter the getCallerActions function from the objects function list
            BAttachedFunction callerActionsFunction = objectTypeSymbol.attachedFuncs.stream()
                    .filter(bAttachedFunction -> GET_CALLER_ACTIONS.equals(bAttachedFunction.funcName.getValue()))
                    .findAny().orElse(null);
            if (callerActionsFunction != null) {
                BType endpointActionsHolderType = callerActionsFunction.type.retType;
                BObjectTypeSymbol endpointActionHolderSymbol =
                        filterObjectTypeSymbolByBType(endpointActionsHolderType, objectTypeSymbols);
                objectCategories.endpointActionHolders.add(endpointActionHolderSymbol);
                objectCategories.endpoints.add(objectTypeSymbol);
            }
        }
        
        objectTypeSymbols.removeAll(objectCategories.endpointActionHolders);
        objectTypeSymbols.removeAll(objectCategories.endpoints);
        // Add the remaining objects to the objects list since those are neither Caller or Endpoint objects
        objectCategories.objects.addAll(objectTypeSymbols);

        return objectCategories;
    }
    
    // TODO: Optimize Further
    private static BObjectTypeSymbol filterObjectTypeSymbolByBType(BType bType,
                                                                   List<BObjectTypeSymbol> objectTypeSymbols) {
        if (!(bType instanceof BObjectType)) {
            return null;
        }
        return objectTypeSymbols.stream()
                .filter(bObjectTypeSymbol -> bObjectTypeSymbol == ((BObjectType) bType).tsymbol)
                .findFirst()
                .orElse(null);
    }

    /**
     * Object categories uses as a vessel to carry categorized objects in a package.
     * 
     * {endpoints}                  holds the clients and listeners.
     * {endpointActionHolders}      holds the endpoint actions for the endpoints.
     * {objects}                    holds all the other objects.
     * 
     * Note: number of endpoints EQUAL to number of endpointActionHolders.
     * Order of the endpoints EQUAL to the order of endpointActionHolders
     */
    public static class ObjectCategories {

        private List<BObjectTypeSymbol> endpoints = new ArrayList<>();
        private List<BObjectTypeSymbol> endpointActionHolders = new ArrayList<>();
        private List<BObjectTypeSymbol> objects = new ArrayList<>();

        public List<BObjectTypeSymbol> getEndpoints() {
            return endpoints;
        }

        public List<BObjectTypeSymbol> getEndpointActionHolders() {
            return endpointActionHolders;
        }

        public List<BObjectTypeSymbol> getObjects() {
            return objects;
        }
    }
}
