/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects;

import io.ballerina.projects.plugins.codeaction.CodeActionException;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper to include codeactions returned by compiler plugins and exceptions captured in the process.
 *
 * @since 2.0.0
 */
public class CodeActionResult {

    private final List<CodeActionInfo> codeActions = new ArrayList<>();
    private final List<CodeActionException> errors = new ArrayList<>();

    public void addCodeAction(CodeActionInfo codeAction) {
        codeActions.add(codeAction);
    }

    public void addError(CodeActionException ex) {
        errors.add(ex);
    }

    /**
     * Get codeactions provided by compiler plugins.
     *
     * @return Codeactions
     */
    public List<CodeActionInfo> getCodeActions() {
        return codeActions;
    }

    /**
     * Get errors catch while processing compiler plugin codeactions.
     *
     * @return List of errors
     */
    public List<CodeActionException> getErrors() {
        return errors;
    }
}
