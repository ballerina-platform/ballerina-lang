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

/**
 * Resolvable code action data.
 *
 * @since 2201.2.0
 */

public class ResolvableCodeAction extends CodeAction {

    private static final Gson GSON = new Gson();

    private ResolvableCodeAction() {
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

    public static ResolvableCodeAction from(CodeAction codeAction) {
        ResolvableCodeAction resolvableCodeAction = new ResolvableCodeAction();
        resolvableCodeAction.setTitle(codeAction.getTitle());
        resolvableCodeAction.setKind(codeAction.getKind());
        resolvableCodeAction.setData(codeAction.getDiagnostics());
        resolvableCodeAction.setCommand(codeAction.getCommand());
        String jsonString = GSON.toJson(codeAction.getData());
        CodeActionData codeActionData = GSON.fromJson(jsonString, CodeActionData.class);
        resolvableCodeAction.setData(codeActionData);
        return resolvableCodeAction;
    }

}
