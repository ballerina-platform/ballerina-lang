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

import org.apache.commons.io.IOUtils;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Represents a template based renderer output.
 *
 * @since 0.985.0
 */
public class TemplateBasedRendererOutput implements RendererOutput {
    private String content;
    /**
     * List of placeholders to keep track of placeholder content.
     */
    private Map<String, String> placeHolders = new HashMap<>();

    /**
     * Returns a new FileTemplate.
     *
     * @param templateFileName File name of the source file located in ./resources/test/template.
     * @throws TestGeneratorException when reading template file failed
     */
    public TemplateBasedRendererOutput(String templateFileName) throws TestGeneratorException {
        InputStream resource = TemplateBasedRendererOutput.class.getClassLoader().getResourceAsStream(
                "testgen" + File.separator + "template" + File.separator + templateFileName
        );
        try {
            this.content = IOUtils.toString(resource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TestGeneratorException("Error occurred while reading template file:" + templateFileName, e);
        }
    }

    /**
     * Appends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    public void append(PlaceHolder placeHolder, String content) {
        String placeHolderName = placeHolder.getName();
        String oldContent = placeHolders.get(placeHolderName);
        placeHolders.put(placeHolderName, (oldContent == null ? "" : oldContent) + content);
    }

    /**
     * Prepends the contents into the placeholder.
     *
     * @param placeHolder place holder
     * @param content     content
     */
    public void prepend(PlaceHolder placeHolder, String content) {
        String placeHolderName = placeHolder.getName();
        String oldContent = placeHolders.get(placeHolderName);
        placeHolders.put(placeHolderName, content + (oldContent == null ? "" : oldContent));
    }

    /**
     * Replaces place holder content.
     *
     * @param placeHolder place holder name
     * @param content     content
     */
    public void put(PlaceHolder placeHolder, String content) {
        String placeHolderName = placeHolder.getName();
        placeHolders.put(placeHolderName, content);
    }

    /**
     * Renders the template.
     *
     * @return rendered string
     */
    public List<TextEdit> getRenderedTextEdits() {
        Position position = new Position(0, 0);
        Range range = new Range(position, position);
        TextEdit textEdit = new TextEdit(range, getRenderedContent());
        List<TextEdit> edits = new ArrayList<>();
        edits.add(textEdit);
        return edits;
    }

    /**
     * Renders the template.
     *
     * @return rendered string
     */
    @Override
    public String getRenderedContent() {
        final String[] result = {content};
        //Replace placeholders
        placeHolders.forEach(
                (key, value) -> {
                    result[0] = result[0].replaceAll("\\$\\{" + key + "}", quoteReplacement(value));
                }
        );
        //Clear pending placeholders
        result[0] = result[0].replaceAll("\\$\\{.*}[\\r\\n|\\r|\\n]*", "");
        return result[0];
    }

    /**
     * Returns True when creating a new test file.
     *
     * @return True when creating a new file, False otherwise
     */
    public boolean isNewTestFile() {
        return true;
    }
}
