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

package org.ballerinalang.langserver.test.template.io;

import org.apache.commons.io.IOUtils;
import org.ballerinalang.langserver.test.TestGeneratorException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Represents a File Template. This class responsible of rendering all placeholders into a given source file.
 */
public class FileTemplate {
    /**
     * File name of the source file located in ./resources/test/template.
     */
    private final String fileName;
    /**
     * List of placeholders to keep track of placeholder content.
     */
    private Map<String, String> placeHolders = new HashMap<>();

    public FileTemplate(String templateFileName) {
        this.fileName = templateFileName;
    }

    /**
     * Appends the contents into the placeholder.
     *
     * @param placeHolderName place holder name
     * @param content         content
     */
    public void append(String placeHolderName, String content) {
        String oldContent = placeHolders.get(placeHolderName);
        placeHolders.put(placeHolderName, (oldContent == null ? "" : oldContent) + content);
    }

    /**
     * Preprends the contents into the placeholder.
     *
     * @param placeHolderName place holder name
     * @param content         content
     */
    public void prepend(String placeHolderName, String content) {
        String oldContent = placeHolders.get(placeHolderName);
        placeHolders.put(placeHolderName, content + (oldContent == null ? "" : oldContent));
    }

    /**
     * Replaces place holder content.
     *
     * @param placeHolderName place holder name
     * @param content         content
     */
    public void put(String placeHolderName, String content) {
        placeHolders.put(placeHolderName, content);
    }

    /**
     * Renders the template.
     *
     * @return rendered string
     * @throws TestGeneratorException when rendering fails
     */
    public String getRenderedContent() throws TestGeneratorException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream(
                "test" + File.separator + "template" + File.separator + fileName
        );

        try {
            final String[] result = {IOUtils.toString(resource, StandardCharsets.UTF_8)};
            //Replace placeholders
            placeHolders.forEach(
                    (key, value) -> {
                        result[0] = result[0].replaceAll("\\$\\$\\$" + key + "\\$\\$\\$", quoteReplacement(value));
                    }
            );
            //Clear pending placeholders
            result[0] = result[0].replaceAll("\\$\\$\\$.*\\$\\$\\$", "");
            return result[0];
        } catch (IOException e) {
            throw new TestGeneratorException("Error occurred while populating template", e);
        }
    }
}
