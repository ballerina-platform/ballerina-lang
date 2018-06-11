package org.ballerinalang.langserver.index.dto;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BPackageSymbolDTO {
    
    private PackageIDDTO packageID;
    
    private List<BRecordTypeSymbol> recordTypeSymbols = new ArrayList<>();
    
    private List<BObjectTypeSymbol> objectTypeSymbols = new ArrayList<>();
    
    private List<BInvokableSymbol> bInvokableSymbols = new ArrayList<>();
    
    private List<BServiceSymbol> bServiceSymbols = new ArrayList<>();
    
    public BPackageSymbolDTO(PackageIDDTO packageID) {
        this.packageID = packageID;
    }

    public PackageIDDTO getPackageID() {
        return packageID;
    }

    public List<BRecordTypeSymbol> getRecordTypeSymbols() {
        return recordTypeSymbols;
    }

    public List<BObjectTypeSymbol> getObjectTypeSymbols() {
        return objectTypeSymbols;
    }

    public List<BInvokableSymbol> getBInvokableSymbols() {
        return bInvokableSymbols;
    }

    public List<BServiceSymbol> getbServiceSymbols() {
        return bServiceSymbols;
    }
}
