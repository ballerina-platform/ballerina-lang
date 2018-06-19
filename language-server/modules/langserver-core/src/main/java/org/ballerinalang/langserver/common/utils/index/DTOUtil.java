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
import org.ballerinalang.langserver.common.utils.completion.BInvokableSymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.BPackageSymbolUtil;
import org.ballerinalang.langserver.index.dto.BFunctionDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.ObjectType;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.PackageIDDTO;
import org.ballerinalang.langserver.index.dto.TypeDTO;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(DTOUtil.class); 

    /**
     * Get the TypeDTO for the BType.
     * @param bType                 bType to generate the DAO
     * @return {@link TypeDTO}      Generated DTO
     */
    public static TypeDTO getTypeDTO(BType bType) {
        BTypeSymbol typeSymbol;
        String name;
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

    /**
     * Get the BPackageSymbolDTO for the package symbol.
     * @param packageSymbol                 packageSymbol to generate the DAO
     * @return {@link BPackageSymbolDTO}    Generated DTO
     */
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
                        break;
                    default:
                        break;
                }
            } else if (symbol instanceof BTypeSymbol) {
                packageSymbolDTO.getOtherTypeSymbols().add((BTypeSymbol) symbol);
            }
        });

        return packageSymbolDTO;
    }

    /**
     * Get the BLangServiceDTO for the service symbol.
     * @param pkgEntryId                    Package Entry ID
     * @param bServiceSymbol                BServiceSymbol to generate DAO
     * @return {@link BLangServiceDTO}      Generated DTO
     */
    public static BLangServiceDTO getServiceDTO(int pkgEntryId, BServiceSymbol bServiceSymbol) {
        return new BLangServiceDTO(pkgEntryId, bServiceSymbol.getName().getValue());
    }

    /**
     * Get the BLangResourceDTO for the BLangResourceDTO.
     * @param serviceEntryId                Service Entry ID
     * @param bLangResource                 BServiceSymbol to generate DAO
     * @return {@link BLangResourceDTO}     Generated DTO
     */
    public static BLangResourceDTO getResourceDTO(int serviceEntryId, BLangResource bLangResource) {
        return new BLangResourceDTO(serviceEntryId, bLangResource.getName().getValue());
    }

    /**
     * Get the BFunctionDTO for the invokable symbol.
     * @param pkgEntryId                Package Entry ID
     * @param bInvokableSymbol          BInvokableSymbol to generate DAO
     * @return {@link BFunctionDTO}     Generated DTO
     */
    public static BFunctionDTO getFunctionDTO(int pkgEntryId, BInvokableSymbol bInvokableSymbol) {
        return getFunctionDTO(pkgEntryId, -1, bInvokableSymbol);
    }

    /**
     * Get the BFunctionDTO for the invokable symbol.
     * @param pkgEntryId                Package Entry ID
     * @param bInvokableSymbol          BInvokableSymbol to generate DAO
     * @param objectId                  ObjectId which the function is attached
     * @return {@link BFunctionDTO}     Generated DTO
     */
    public static BFunctionDTO getFunctionDTO(int pkgEntryId, int objectId, BInvokableSymbol bInvokableSymbol) {
        CompletionItem completionItem = BInvokableSymbolUtil.getFunctionCompletionItem(bInvokableSymbol);
        return new BFunctionDTO(pkgEntryId, objectId, bInvokableSymbol.getName().getValue(), completionItem);
    }

    /**
     * Get the BObjectTypeSymbolDTO for the Object Type symbol.
     * @param pkgEntryId                        Package Entry ID
     * @param bObjectTypeSymbol                 BObjectTypeSymbol to generate DAO
     * @param type                              ObjectType
     * @return {@link BObjectTypeSymbolDTO}     Generated DTO
     */
    public static BObjectTypeSymbolDTO getObjectTypeSymbolDTO(int pkgEntryId, BObjectTypeSymbol bObjectTypeSymbol,
                                                     ObjectType type) {
        CompletionItem completionItem = null;
        if (type == ObjectType.OBJECT) {
            completionItem = BPackageSymbolUtil.getBTypeCompletionItem(bObjectTypeSymbol.getName().getValue());
        }

        return new BObjectTypeSymbolDTO(pkgEntryId, bObjectTypeSymbol.getName().getValue(), null, type, completionItem);
    }

    /**
     * Get the BRecordTypeSymbolDTO for the Object Type symbol.
     * @param pkgEntryId                        Package Entry ID
     * @param recordTypeSymbol                  BRecordTypeSymbol to generate DAO
     * @return {@link BRecordTypeSymbolDTO}     Generated DTO
     */
    public static BRecordTypeSymbolDTO getRecordTypeSymbolDTO(int pkgEntryId, BRecordTypeSymbol recordTypeSymbol) {
        CompletionItem completionItem = BPackageSymbolUtil
                .getBTypeCompletionItem(recordTypeSymbol.getName().getValue());
        return new BRecordTypeSymbolDTO(pkgEntryId, recordTypeSymbol.getName().getValue(), null, completionItem);
    }

    /**
     * Get the OtherTypeSymbolDTO for the Object Type symbol.
     * @param pkgEntryId                        Package Entry ID
     * @param otherTypeSymbol                   BTypeSymbol to generate DAO
     * @return {@link BRecordTypeSymbolDTO}     Generated DTO
     */
    public static OtherTypeSymbolDTO getOtherTypeSymbolDTO(int pkgEntryId, BTypeSymbol otherTypeSymbol) {
        CompletionItem completionItem = BPackageSymbolUtil
                .getBTypeCompletionItem(otherTypeSymbol.getName().getValue());
        return new OtherTypeSymbolDTO(pkgEntryId, otherTypeSymbol.getName().getValue(), null, completionItem);
    }

    /**
     * Convert CompletionItem to JSON format.
     * @param completionItem    CompletionItem to convert to String
     * @return {@link String}   JSON String of CompletionItem
     */
    public static String completionItemToJSON(CompletionItem completionItem) {
        return gson.toJson(completionItem);
    }

    /**
     * Get the Categorized objects from the provided BObjectTypeSymbol list.
     * @param objectTypeSymbols             List of ObjectTypeSymbols
     * @return {@link ObjectCategories}     Categorised ObjectSymbols
     */
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
                        filterObjectTypeSymbolByBType(endpointActionsHolderType);
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

    // Private Methods
    // TODO: Optimize Further
    private static BObjectTypeSymbol filterObjectTypeSymbolByBType(BType bType) {
        if (!(bType instanceof BObjectType) || !(((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol)) {
            return null;
        }

        return (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
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
