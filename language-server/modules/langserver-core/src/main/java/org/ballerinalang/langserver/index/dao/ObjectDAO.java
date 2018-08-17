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
package org.ballerinalang.langserver.index.dao;

import org.eclipse.lsp4j.CompletionItem;

/**
 * DAO for ObjectTypeSymbol.
 */
public class ObjectDAO {

    private String packageName;

    private String packageOrgName;

    private String objectName;

    private boolean isPrivate;

    private CompletionItem completionItem;

    public ObjectDAO(String packageName, String packageOrgName, String objectName, boolean isPrivate,
                     CompletionItem completionItem) {
        this.packageName = packageName;
        this.packageOrgName = packageOrgName;
        this.objectName = objectName;
        this.isPrivate = isPrivate;
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

    public boolean isPrivate() {
        return isPrivate;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }
}
