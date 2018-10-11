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
package org.ballerinalang.langserver.index;

import com.google.gson.Gson;
import org.ballerinalang.langserver.common.utils.completion.BInvokableSymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.BPackageSymbolUtil;
import org.ballerinalang.langserver.index.dataholder.BLangPackageContent;
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for DTO manipulation.
 */
public class DTOUtil {
    
    private static final String GET_CALLER_ACTIONS = "getCallerActions";

    private static final Gson gson = new Gson();
    
    /**
     * Get the BLangPackageContent for the package symbol.
     * @param packageSymbol                     packageSymbol to generate the DAO
     * @return {@link BLangPackageContent}      Package content object
     */
    public static BLangPackageContent getBLangPackageContent(BPackageSymbol packageSymbol) {
        PackageID packageID = packageSymbol.pkgID;
        BPackageSymbolDTO packageSymbolDTO = new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                .setName(packageID.getName().getValue())
                .setOrgName(packageID.getOrgName().getValue())
                .setVersion(packageID.getPackageVersion().getValue())
                .build();
        
        List<BObjectTypeSymbol> objects = new ArrayList<>();
        List<BRecordTypeSymbol> records = new ArrayList<>();
        List<BInvokableSymbol> invokableSymbols = new ArrayList<>();
        List<BTypeSymbol> otherTypes = new ArrayList<>();

            packageSymbol.scope.entries.entrySet().forEach(entry -> {
            BSymbol symbol = entry.getValue().symbol;
            if (symbol.kind != null) {
                switch (symbol.kind) {
                    case OBJECT:
                        objects.add((BObjectTypeSymbol) symbol);
                        break;
                    case RECORD:
                        records.add((BRecordTypeSymbol) symbol);
                        break;
                    case FUNCTION:
                        invokableSymbols.add((BInvokableSymbol) symbol);
                        break;
                    default:
                        break;
                }
            } else if (symbol instanceof BTypeSymbol) {
                otherTypes.add((BTypeSymbol) symbol);
            }
        });
        
        return new BLangPackageContent.BLangPackageContentBuilder()
                .setPackageSymbolDTO(packageSymbolDTO)
                .setObjectTypeSymbols(objects)
                .setbInvokableSymbols(invokableSymbols)
                .setRecordTypeSymbols(records)
                .setOtherTypeSymbols(otherTypes)
                .build();
    }

    /**
     * Get the BFunctionDTO for the invokable symbol.
     * @param pkgEntryId                Package Entry ID
     * @param bInvokableSymbol          BInvokableSymbol to generate DAO
     * @return {@link BFunctionSymbolDTO}     Generated DTO
     */
    public static BFunctionSymbolDTO getFunctionDTO(int pkgEntryId, BInvokableSymbol bInvokableSymbol) {
        return getFunctionDTO(pkgEntryId, -1, bInvokableSymbol);
    }

    /**
     * Get the BFunctionDTO for the invokable symbol.
     * @param pkgId                     Package Entry ID
     * @param bInvokableSymbol          BInvokableSymbol to generate DAO
     * @param objectId                  ObjectId which the function is attached
     * @return {@link BFunctionSymbolDTO}     Generated DTO
     */
    public static BFunctionSymbolDTO getFunctionDTO(int pkgId, int objectId, BInvokableSymbol bInvokableSymbol) {
        CompletionItem completionItem = BInvokableSymbolUtil.getFunctionCompletionItem(bInvokableSymbol);
        boolean isPrivate = !((bInvokableSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
        boolean isAttached = (bInvokableSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED;
        return new BFunctionSymbolDTO.BFunctionDTOBuilder()
                .setPackageId(pkgId)
                .setObjectId(objectId)
                .setName(bInvokableSymbol.getName().getValue())
                .setCompletionItem(completionItem)
                .setPrivate(isPrivate)
                .setAttached(isAttached)
                .build();
    }

    /**
     * Get the BObjectTypeSymbolDTO for the Object Type symbol.
     *
     * @param pkgId                         Package Entry ID
     * @param symbol                        BObjectTypeSymbol to generate DAO
     * @param type                              ObjectType
     * @return {@link BObjectTypeSymbolDTO}     Generated DTO
     */
    public static BObjectTypeSymbolDTO getObjectTypeSymbolDTO(int pkgId, BObjectTypeSymbol symbol, ObjectType type) {
        CompletionItem completionItem = null;
        boolean isPrivate = !((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
        if (type == ObjectType.OBJECT) {
            completionItem = BPackageSymbolUtil.getBTypeCompletionItem(symbol.getName().getValue());
        }
        
        return new BObjectTypeSymbolDTO.BObjectTypeSymbolDTOBuilder()
                .setPackageId(pkgId)
                .setName(symbol.getName().getValue())
                .setType(type)
                .setPrivate(isPrivate)
                .setCompletionItem(completionItem)
                .build();
    }

    /**
     * Get the BRecordTypeSymbolDTO for the Object Type symbol.
     *
     * @param pkgId                             Package Entry ID
     * @param symbol                            BRecordTypeSymbol to generate DAO
     * @return {@link BRecordTypeSymbolDTO}     Generated DTO
     */
    public static BRecordTypeSymbolDTO getRecordTypeSymbolDTO(int pkgId, BRecordTypeSymbol symbol) {
        CompletionItem completionItem = BPackageSymbolUtil.getBTypeCompletionItem(symbol.getName().getValue());
        boolean isPrivate = !((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
        
        return new BRecordTypeSymbolDTO.BRecordTypeSymbolDTOBuilder()
                .setPackageId(pkgId)
                .setName(symbol.getName().getValue())
                .setPrivate(isPrivate)
                .setCompletionItem(completionItem)
                .build();
    }

    /**
     * Get the OtherTypeSymbolDTO for the Object Type symbol.
     * @param pkgId                             Package Entry ID
     * @param symbol                            BTypeSymbol to generate DAO
     * @return {@link BRecordTypeSymbolDTO}     Generated DTO
     */
    public static OtherTypeSymbolDTO getOtherTypeSymbolDTO(int pkgId, BTypeSymbol symbol) {
        CompletionItem completionItem = BPackageSymbolUtil.getBTypeCompletionItem(symbol.getName().getValue());
        
        return new OtherTypeSymbolDTO.OtherTypeSymbolDTOBuilder()
                .setPackageId(pkgId)
                .setName(symbol.getName().getValue())
                .setCompletionItem(completionItem)
                .build();
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
     * Get the Completion Item from the json.
     * 
     * @param jsonVal   Json value to convert
     * @return {@link CompletionItem}   Converted Completion item
     */
    public static CompletionItem jsonToCompletionItem(String jsonVal) {
        return gson.fromJson(jsonVal, CompletionItem.class);
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

    /////////////////////
    // Private Methods //
    /////////////////////
    
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
