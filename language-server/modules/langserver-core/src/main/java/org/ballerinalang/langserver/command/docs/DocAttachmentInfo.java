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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Holds the meta information required for the documentation attachment.
 *
 * @since 0.985.0
 */
public class DocAttachmentInfo implements Documentation {
    private final String description;
    private final Map<String, String> parameters;
    private final String returnDesc;
    private final Position docStart;

    public DocAttachmentInfo(String description, Map<String, String> parameters, String returnDesc,
                             Position docStart) {
        this.description = description;
        this.parameters = parameters;
        this.returnDesc = returnDesc;
        this.docStart = docStart;
    }


    public DocAttachmentInfo(String description, Position docStart) {
        this.description = description;
        this.parameters = new HashMap<>();
        this.returnDesc = null;
        this.docStart = docStart;
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

    public Position getDocStartPos() {
        return docStart;
    }

    public DocAttachmentInfo mergeDocAttachment(Documentation other) {
        String description = other.description().orElse(this.description);
        // NOTE: we prioritize current markdown attachment's params since it has the correct order and correct positions
        Map<String, String> newParamsMap = new LinkedHashMap<>();
        if (!this.parameters.isEmpty()) {
            parameters.forEach((key, value) -> newParamsMap.put(key, other.parameterMap().getOrDefault(key, value)));
        }
        String returnValueDescription = (this.returnDesc != null) ?
                other.returnDescription().orElse(this.returnDesc) : null;
        return new DocAttachmentInfo(description, newParamsMap, returnValueDescription, docStart);
    }

    public String getDocumentationString() {
        String offsetStr = String.join("", Collections.nCopies(docStart.getCharacter(), " "));
        String result = String.format("# %s%n", this.description.trim());

        if (!parameters.isEmpty()) {
            StringJoiner paramJoiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);
            for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                paramJoiner.add(String.format("%s# + %s - %s", offsetStr, parameter.getKey(), parameter.getValue()));
            }
            result += String.format("#%n%s%n", paramJoiner.toString());
        }

        if (returnDesc != null) {
            result += String.format("%s# + return - %s", offsetStr, this.returnDesc.trim());
        }

        return result.trim() + CommonUtil.MD_LINE_SEPARATOR;
    }
}
