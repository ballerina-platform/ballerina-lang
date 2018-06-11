package org.ballerinalang.langserver.index.dao;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Created by nadeeshaan on 6/11/18.
 */
public class ObjectDAO {
    private String packageName;

    private String packageOrgName;

    private String objectName;

    private CompletionItem completionItem;

    public ObjectDAO(String packageName, String packageOrgName, String objectName,
                     CompletionItem completionItem) {
        this.packageName = packageName;
        this.packageOrgName = packageOrgName;
        this.objectName = objectName;
        this.completionItem = completionItem;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageOrgName() {
        return packageOrgName;
    }

    public String getObjectName() {
        return objectName;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
