/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.ballerina.document;


import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

/**
 * Represents a Ballerina swagger change request.
 *
 * @since 0.983.0
 */
public class ApiDesignDidChangeParams {

    private String oasDefinition;
    private VersionedTextDocumentIdentifier documentIdentifier;

    public VersionedTextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(VersionedTextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public String getOASDefinition() {
        return this.oasDefinition;
    }

    public void setOASDefinition(String oasDefinition) {
        this.oasDefinition = oasDefinition;
    }

}
