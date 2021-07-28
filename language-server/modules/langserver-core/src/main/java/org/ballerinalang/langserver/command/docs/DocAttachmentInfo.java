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
package org.ballerinalang.langserver.command.docs;

import io.ballerina.compiler.api.symbols.Documentation;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Position;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Holds the meta information required for the documentation attachment. This class expects a {@link LinkedHashMap} for
 * the parameters in order to keep the ordering of the parameters at the same time.
 *
 * @since 0.985.0
 */
public class DocAttachmentInfo implements Documentation {

    private final String description;
    private final Map<String, String> parameters;
    private final String returnDesc;
    private final String deprecatedDesc;
    private final Position docStart;
    private final String padding;

    public DocAttachmentInfo(String description, LinkedHashMap<String, String> parameters, String returnDesc,
                             String deprecatedDesc, Position docStart, String padding) {
        this.description = description;
        this.parameters = parameters;
        this.returnDesc = returnDesc;
        this.deprecatedDesc = deprecatedDesc;
        this.docStart = docStart;
        this.padding = padding;
    }

    public DocAttachmentInfo(String description, Position docStart, String padding) {
        this.description = description;
        this.parameters = new LinkedHashMap<>();
        this.returnDesc = null;
        this.deprecatedDesc = null;
        this.docStart = docStart;
        this.padding = padding;
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public Map<String, String> parameterMap() {
        return parameters;
    }

    @Override
    public Optional<String> returnDescription() {
        return Optional.ofNullable(returnDesc);
    }

    @Override
    public Optional<String> deprecatedDescription() {
        return Optional.ofNullable(deprecatedDesc);
    }

    @Override
    public Map<String, String> deprecatedParametersMap() {
        return Collections.emptyMap();
    }

    public Position getDocStartPos() {
        return docStart;
    }

    public DocAttachmentInfo mergeDocAttachment(Documentation other) {
        String description = other.description().orElse(this.description);
        // NOTE: we prioritize current markdown attachment's params since it has the correct order and correct positions
        LinkedHashMap<String, String> newParamsMap = new LinkedHashMap<>();
        if (!this.parameters.isEmpty()) {
            parameters.forEach((key, value) -> newParamsMap.put(key, other.parameterMap().getOrDefault(key, value)));
        }

        // Check if no return type present -> handles removal of return type descriptor
        String returnValueDescription = null;
        if (this.returnDesc != null) {
            returnValueDescription = other.returnDescription().orElse(this.returnDesc);
        }

        // Check if deprecated description is present -> handles removal of deprecated description
        String deprecatedDescription = null;
        if (this.deprecatedDesc != null) {
            deprecatedDescription = other.deprecatedDescription().orElse(this.deprecatedDesc);
        }

        return new DocAttachmentInfo(description, newParamsMap, returnValueDescription, deprecatedDescription,
                docStart, padding);
    }

    public String getDocumentationString() {
        return getDocumentationString(true);
    }

    public String getDocumentationString(boolean newlineAtEnd) {
        StringBuilder result = new StringBuilder();
        String[] descriptionLines = this.description.trim().split("(\r)?\n");
        for (String descriptionLine : descriptionLines) {
            result.append(String.format("%s# %s%n", padding, descriptionLine.trim()));
        }

        if (!parameters.isEmpty()) {
            StringJoiner paramJoiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);
            for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                String[] parameterLines = parameter.getValue().trim().split("(\r)?\n");
                paramJoiner.add(String.format("%s# + %s - %s", padding, parameter.getKey(), parameterLines[0].trim()));
                for (String parameterLine : Arrays.copyOfRange(parameterLines, 1, parameterLines.length)) {
                    paramJoiner.add(String.format("%s# %s", padding, parameterLine.trim()));
                }
                
            }
            result.append(String.format("%s#%n%s%n", padding, paramJoiner.toString()));
        }

        if (returnDesc != null) {
            String[] returnDescLines = this.returnDesc.trim().split("(\r)?\n");
            result.append(String.format("%s# + return - %s%n", padding, returnDescLines[0].trim()));
            for (String returnDescLine : Arrays.copyOfRange(returnDescLines, 1, returnDescLines.length)) {
                result.append(String.format("%s# %s%n", padding, returnDescLine.trim()));
            }
        }

        if (deprecatedDesc != null) {
            result.append(String.format("%s# # Deprecated%n", padding));
            String[] deprecatedDescLines = deprecatedDesc.trim().split("(\r)?\n");
            for (String deprecatedLine : deprecatedDescLines) {
                result.append(String.format("%s# %s%n", padding, deprecatedLine));
            }
        }

        if (newlineAtEnd) {
            return result.toString().trim() + CommonUtil.MD_LINE_SEPARATOR + padding;
        } else {
            return result.toString().trim();
        }
    }
}
