/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.doc.gen.core.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.Objects;

/**
 * Apache Freemarker method for formatting text
 * This is invoked by the freemarker templates
 *
 * This method escapes relevant html characters, wraps text
 */
public class FormatDescriptionMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List args) throws TemplateModelException {
        if (args.size() != 1) {
            throw new TemplateModelException("There should be a single argument of type string " +
                    "passed to format description method");
        }

        SimpleScalar arg1 = (SimpleScalar) args.get(0);
        String inputString = arg1.getAsString();

        // Replacing spaces that should not be considered in text wrapping with non breaking spaces
        inputString = replaceLeadingSpaces(inputString);

        inputString = inputString.replaceAll("<", "&lt;");
        inputString = inputString.replaceAll(">", "&gt;");

        // Replacing new line characters
        inputString = replaceNewLines(inputString);

        inputString = inputString.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        inputString = inputString.replaceAll("```([^```]*)```", "<pre>$1</pre>");
        inputString = inputString.replaceAll("`([^`]*)`", "<code>$1</code>");

        return inputString;
    }

    /**
     * Replaces any spaces at the start of a new line with "&nbsp;"
     * This will preserve the tabs entered at the start of a line
     *
     * @param inputString The string which needs to changed
     * @return The string with spaces replaced
     */
    private String replaceLeadingSpaces(String inputString) {
        StringBuilder stringBuilder = new StringBuilder(inputString);
        int i = 0;
        boolean isInsideCode = false;
        while (i < stringBuilder.length()) {
            if (i + 3 < stringBuilder.length() &&
                    Objects.equals(stringBuilder.substring(i, i + 3), "```")) {
                isInsideCode = !isInsideCode;   // Is inside a <pre> code section
                i += 3;
            } else if (stringBuilder.charAt(i) == '`') {
                isInsideCode = !isInsideCode;   // Is inside a <code> code section
                i++;
            } else if (!isInsideCode && stringBuilder.charAt(i) == '\n') {
                i++;
                // Replacing leading spaces
                while (i < stringBuilder.length() && stringBuilder.charAt(i) == ' ') {
                    stringBuilder.replace(i, i + 1, "&nbsp;");
                    i += 6;
                }
            } else {
                i++;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Replaces new line characters outside code segments with "<br>" tags
     *
     * @param inputString The string which needs to changed
     * @return The string with the new lines replaced
     */
    private String replaceNewLines(String inputString) {
        StringBuilder stringBuilder = new StringBuilder(inputString);
        int i = 0;
        boolean isInsideCode = false;
        while (i < stringBuilder.length()) {
            if (i + 3 < stringBuilder.length() &&
                    Objects.equals(stringBuilder.substring(i, i + 3), "```")) {
                isInsideCode = !isInsideCode;   // Is inside a <pre> code section
                i += 3;
            } else if (stringBuilder.charAt(i) == '`') {
                isInsideCode = !isInsideCode;   // Is inside a <code> code section
                i++;
            } else if (!isInsideCode && stringBuilder.charAt(i) == '\n') {
                // Replacing new lines
                stringBuilder.replace(i, i + 1, "<br>");
                i += 4;
            } else {
                i++;
            }
        }
        return stringBuilder.toString();
    }
}
