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
package io.ballerina.projects.plugins.codeaction;

import io.ballerina.compiler.syntax.tree.SyntaxTree;

/**
 * Represents edits made to a given text document.
 */
public class DocumentEdit {

    private String fileUri;
    private SyntaxTree originalSyntaxTree;
    private SyntaxTree modifiedSyntaxTree;

    public DocumentEdit(String fileUri, SyntaxTree originalSyntaxTree, SyntaxTree modifiedSyntaxTree) {
        this.fileUri = fileUri;
        this.originalSyntaxTree = originalSyntaxTree;
        this.modifiedSyntaxTree = modifiedSyntaxTree;
    }

    public String getFileUri() {
        return fileUri;
    }

    public SyntaxTree getOriginalSyntaxTree() {
        return originalSyntaxTree;
    }

    public SyntaxTree getModifiedSyntaxTree() {
        return modifiedSyntaxTree;
    }
}
