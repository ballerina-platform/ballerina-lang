package org.ballerinalang.langserver.index.dao;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Created by nadeeshaan on 6/11/18.
 */
public class RecordDAO {
    private String packageName;

    private String packageOrgName;

    private String recordName;

    private CompletionItem completionItem;

    public RecordDAO(String packageName, String packageOrgName, String recordName,
                              CompletionItem completionItem) {
        this.packageName = packageName;
        this.packageOrgName = packageOrgName;
        this.recordName = recordName;
        this.completionItem = completionItem;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageOrgName() {
        return packageOrgName;
    }

    public String getRecordName() {
        return recordName;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
