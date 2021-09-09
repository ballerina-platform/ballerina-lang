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
package org.ballerinalang.langserver.simulator;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the editor used by the end user. An  editor consists of a set of open tabs.
 *
 * @since 2.0.0
 */
public class Editor {

    private static final Logger logger = LoggerFactory.getLogger(Editor.class);

    private BallerinaLanguageServer languageServer;
    private Endpoint endpoint;

    private final List<EditorTab> tabs = new ArrayList<>();
    private EditorTab activeTab;

    private Editor(BallerinaLanguageServer languageServer, Endpoint endpoint) {
        this.languageServer = languageServer;
        this.endpoint = endpoint;
    }

    /**
     * Simulates opening the editor. Here we initialize the language server.
     *
     * @return Editor instance
     */
    public static Editor open() {
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();

        EditorOutputStream outputStream = new EditorOutputStream();
        Endpoint endpoint = TestUtil.initializeLanguageSever(languageServer, outputStream);

        Editor editor = new Editor(languageServer, endpoint);
        outputStream.setEditor(editor);
        return editor;
    }

    public EditorTab openFile(Path filePath) {
        EditorTab editorTab = tabs.stream()
                .filter(tab -> tab.filePath().equals(filePath))
                .findFirst()
                .orElseGet(() -> {
                    EditorTab tab = new EditorTab(filePath, endpoint, languageServer);
                    tabs.add(tab);
                    return tab;
                });
        this.activeTab = editorTab;
        return editorTab;
    }

    public void closeFile(Path filePath) {
        tabs.removeIf(tab -> {
            if (filePath.equals(tab.filePath())) {
                tab.close();
                return true;
            }
            return false;
        });
    }

    public void closeTab(EditorTab tab) {
        tabs.remove(tab);
    }

    public void close() {
        this.languageServer.shutdown();
        tabs.forEach(tab -> tab.close());
    }

    public EditorTab activeTab() {
        return activeTab;
    }
}
