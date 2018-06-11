package org.ballerinalang.langserver.index.dto;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangFunctionDTO {

    private int packageId;
    
    private int objectId;
    
    private String name;
    
    CompletionItem completionItem;

    public BLangFunctionDTO(int packageId, int objectId, String name, CompletionItem completionItem) {
        this.packageId = packageId;
        this.objectId = objectId;
        this.name = name;
        this.completionItem = completionItem;
    }

    public int getPackageId() {
        return packageId;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getName() {
        return name;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
