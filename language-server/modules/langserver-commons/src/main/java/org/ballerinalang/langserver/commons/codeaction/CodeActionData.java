/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.commons.codeaction;

import org.eclipse.lsp4j.Range;

/**
 * Code action data.
 *
 * @since 2201.2.1
 */
public class CodeActionData {
    String extName;
    String codeActionName;
    String fileUri;
    Range range;
    Object actionData;

    public CodeActionData(String codeActionName, String fileUri, Range range, Object actionData) {
        this.codeActionName = codeActionName;
        this.fileUri = fileUri;
        this.range = range;
        this.actionData = actionData;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public String getCodeActionName() {
        return codeActionName;
    }

    public void setCodeActionName(String codeActionName) {
        this.codeActionName = codeActionName;
    }

    public Object getActionData() {
        return actionData;
    }

    public void setActionData(Object actionData) {
        this.actionData = actionData;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }
}
