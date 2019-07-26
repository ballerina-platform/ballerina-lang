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
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.index.dataholder.BLangPackageContent;
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for DTO manipulation.
 */
public class DTOUtil {

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
        List<BSymbol> otherTypes = new ArrayList<>();

            packageSymbol.scope.entries.entrySet().forEach(entry -> {
            BSymbol symbol = entry.getValue().symbol;
            Optional<BSymbol> bTypeSymbol;
            if (symbol.kind != null) {
                switch (symbol.kind) {
                    case OBJECT:
                        // Omit ballerina services from indexes.
                        if ((symbol.flags & Flags.SERVICE) != Flags.SERVICE) {
                            objects.add((BObjectTypeSymbol) symbol);
                        }
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
            } else if ((bTypeSymbol = FilterUtils.getBTypeEntry(entry.getValue())).isPresent()) {
                otherTypes.add(bTypeSymbol.get());
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
        CompletionItem completionItem = BFunctionCompletionItemBuilder.build(bInvokableSymbol, null);
        boolean isPrivate = !((bInvokableSymbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
        boolean isAttached = (bInvokableSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED;
        boolean isAction = (bInvokableSymbol.flags & Flags.REMOTE) == Flags.REMOTE;
        return new BFunctionSymbolDTO.BFunctionDTOBuilder()
                .setPackageId(pkgId)
                .setObjectId(objectId)
                .setName(bInvokableSymbol.getName().getValue())
                .setCompletionItem(completionItem)
                .setPrivate(isPrivate)
                .setAction(isAction)
                .setAttached(isAttached)
                .build();
    }

    /**
     * Get the BObjectTypeSymbolDTO for the Object Type symbol.
     *
     * @param pkgId                             Package Entry ID
     * @param symbol                            BObjectTypeSymbol to generate DAO
     * @return {@link BObjectTypeSymbolDTO}     Generated DTO
     */
    public static BObjectTypeSymbolDTO getObjectTypeSymbolDTO(int pkgId, BObjectTypeSymbol symbol) {
        boolean isPrivate = !((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC);
        CompletionItem completionItem = BTypeCompletionItemBuilder.build(symbol, symbol.getName().getValue());

        return new BObjectTypeSymbolDTO.BObjectTypeSymbolDTOBuilder()
                .setPackageId(pkgId)
                .setName(symbol.getName().getValue())
                .setType(ObjectType.get(symbol))
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
        CompletionItem completionItem = BTypeCompletionItemBuilder.build(symbol, symbol.getName().getValue());
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
    public static OtherTypeSymbolDTO getOtherTypeSymbolDTO(int pkgId, BSymbol symbol) {
        CompletionItem completionItem = BTypeCompletionItemBuilder.build(symbol, symbol.getName().getValue());

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
}
