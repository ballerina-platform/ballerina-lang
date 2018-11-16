/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.testgen.renderer;

import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;

/**
 * Represents a Renderer output.
 *
 * @since 0.985.0
 */
public interface RendererOutput {
    /**
     * Appends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    void append(PlaceHolder placeHolder, String content);

    /**
     * Prepends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    void prepend(PlaceHolder placeHolder, String content);

    /**
     * Replaces place holder content.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    void put(PlaceHolder placeHolder, String content);

    /**
     * Returns rendered text-edits.
     *
     * @return rendered edits
     */
    List<TextEdit> getRenderedTextEdits();

    /**
     * Returns rendered content.
     *
     * @return rendered string
     */
    String getRenderedContent();

    /**
     * Returns True when creating a new test file.
     *
     * @return True when creating a new file, False otherwise
     */
    boolean isNewTestFile();
}
