/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import org.eclipse.lsp4j.TextDocumentIdentifier;

import javax.annotation.Nonnull;

/**
 * Request format for ConfigSchemaGenerator endpoint.
 *
 * @since 2.0.0
 */
public class PackageConfigSchemaRequest {

    private TextDocumentIdentifier documentIdentifier;

    protected TextDocumentIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(@Nonnull TextDocumentIdentifier documentIdentifier) {
        this.documentIdentifier = new TextDocumentIdentifier(documentIdentifier.getUri());
    }
}
