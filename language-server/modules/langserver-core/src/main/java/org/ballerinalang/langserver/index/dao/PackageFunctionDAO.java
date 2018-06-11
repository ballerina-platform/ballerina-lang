package org.ballerinalang.langserver.index.dao;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Created by nadeeshaan on 6/11/18.
 */
public class PackageFunctionDAO {
    
    private String packageName;
    
    private String packageOrgName;
    
    private String functionName;
    
    private CompletionItem completionItem;

    public PackageFunctionDAO(String packageName, String packageOrgName, String functionName,
                              CompletionItem completionItem) {
        this.packageName = packageName;
        this.packageOrgName = packageOrgName;
        this.functionName = functionName;
        this.completionItem = completionItem;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageOrgName() {
        return packageOrgName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
