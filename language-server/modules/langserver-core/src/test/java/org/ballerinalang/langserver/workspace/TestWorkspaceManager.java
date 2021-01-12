/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace;

import io.ballerina.projects.Document;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Contains a set of utility methods to manage projects.
 *
 * @since 2.0.0
 */
public class TestWorkspaceManager {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/project");
    private final String dummyContent = "function foo() {\n}";
    private final BallerinaWorkspaceManager workspaceManager
            = BallerinaWorkspaceManager.getInstance(new LanguageServerContextImpl());

    @Test(dataProvider = "workspace-data-provider")
    public void testOpenDocument(Path filePath) throws IOException {
        // Inputs from lang server
        DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        textDocumentItem.setUri(filePath.toUri().toString());
        textDocumentItem.setText(dummyContent);
        params.setTextDocument(textDocumentItem);

        // Notify workspace manager
        workspaceManager.didOpen(filePath, params);

        // Assert content
        String content = new String(Files.readAllBytes(filePath));
        Optional<Document> document = workspaceManager.document(filePath);
        Assert.assertNotNull(document.get());
        Assert.assertEquals(document.get().syntaxTree().textDocument().toString(), content);
    }

    @Test(dataProvider = "workspace-data-provider", dependsOnMethods = "testOpenDocument")
    public void testUpdateDocument(Path filePath) throws WorkspaceDocumentException {
        // Inputs from lang server
        DidChangeTextDocumentParams params = new DidChangeTextDocumentParams();
        VersionedTextDocumentIdentifier doc = new VersionedTextDocumentIdentifier(filePath.toUri().toString(), 1);
        params.setTextDocument(doc);
        params.getContentChanges().add(new TextDocumentContentChangeEvent(dummyContent));

        // Notify workspace manager
        workspaceManager.didChange(filePath, params);

        Optional<Document> document = workspaceManager.document(filePath);
        Assert.assertNotNull(document.get());
        Assert.assertEquals(document.get().syntaxTree().textDocument().toString(), dummyContent);
    }

    @DataProvider(name = "workspace-data-provider")
    public Object[] dataProvider() {
        return new Path[]{
                RESOURCE_DIRECTORY.resolve("single-file").resolve("main.bal").toAbsolutePath(),
                RESOURCE_DIRECTORY.resolve("myproject").resolve("main.bal").toAbsolutePath()
        };
    }
}
