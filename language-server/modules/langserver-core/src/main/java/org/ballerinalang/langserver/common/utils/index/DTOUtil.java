package org.ballerinalang.langserver.common.utils.index;

import org.ballerinalang.langserver.common.utils.completion.BLangFunctionUtil;
import org.ballerinalang.langserver.index.dto.BLangFunctionDTO;
import org.ballerinalang.langserver.index.dto.BLangObjectDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.ObjectType;
import org.ballerinalang.langserver.index.dto.PackageIDDTO;
import org.ballerinalang.langserver.index.dto.TypeDTO;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class DTOUtil {
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

    private BLangServiceDTO getServiceDTO(int pkgEntryId, BServiceSymbol bServiceSymbol) {
        return new BLangServiceDTO(pkgEntryId, bServiceSymbol.getName().getValue());
    }

    private BLangFunctionDTO getFunctionDTO(int pkgEntryId, BInvokableSymbol bInvokableSymbol) {
        CompletionItem completionItem = BLangFunctionUtil.getFunctionCompletionItem(bInvokableSymbol);
        return new BLangFunctionDTO(pkgEntryId, -1, bInvokableSymbol.getName().getValue(), completionItem);
    }

//    private BLangObjectDTO getObjectDTO(int pkgEntryId, BObjectTypeSymbol bObjectTypeSymbol, ObjectType type) {
//        return new BLangObjectDTO(pkgEntryId, bObjectTypeSymbol.getName().getValue(), bObjectTypeSymbol.fields, type);
//    }
//
//    private BLangRecordDTO getRecordDTO(int pkgEntryId, BLangRecord bLangRecord) {
//        return new BLangRecordDTO(pkgEntryId, bLangRecord.getName().getValue(), bLangRecord.fields);
//    } 

    private BLangResourceDTO getResourceDTO(int serviceEntryId, BLangResource bLangResource) {
        return new BLangResourceDTO(serviceEntryId, bLangResource.getName().getValue());
    }
}
