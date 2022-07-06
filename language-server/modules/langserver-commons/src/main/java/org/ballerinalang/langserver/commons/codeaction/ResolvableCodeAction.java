/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import com.google.gson.Gson;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;

/**
 * Resolvable code action data.
 *
 * @since 2201.2.x
 */

public class ResolvableCodeAction extends CodeAction {

    private static final Gson GSON = new Gson();

    public ResolvableCodeAction() {
    }

    public ResolvableCodeAction(String title) {
        super(title);
    }

    @Override
    public CodeActionData getData() {
        return (CodeActionData) super.getData();
    }

    public void setData(CodeActionData codeActionData) {
        super.setData(codeActionData);
    }

    public static ResolvableCodeAction from(CodeAction jsonObj) {
        ResolvableCodeAction resolvableCodeAction = new ResolvableCodeAction();
        resolvableCodeAction.setTitle(jsonObj.getTitle());
        resolvableCodeAction.setKind(jsonObj.getKind());
        resolvableCodeAction.setData(jsonObj.getDiagnostics());
        resolvableCodeAction.setCommand(jsonObj.getCommand());
        String toJson = GSON.toJson(jsonObj.getData());
        CodeActionData codeActionData = GSON.fromJson(toJson, CodeActionData.class);
        resolvableCodeAction.setData(codeActionData);
        return resolvableCodeAction;
    }

    /**
     * Code action data.
     */
    public static class CodeActionData {
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
}
