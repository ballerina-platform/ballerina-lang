/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.symbols.Documentation;
import org.ballerinalang.model.elements.MarkdownDocAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the markdown documentation attachment.
 *
 * @since 2.0.0
 */
public class BallerinaDocumentation implements Documentation {

    private MarkdownDocAttachment markdownDocAttachment;

    public BallerinaDocumentation(MarkdownDocAttachment markdownDocAttachment) {
        this.markdownDocAttachment = markdownDocAttachment;
    }

    /**
     * Get the description of the documentation.
     *
     * @return {@link Optional} description
     */
    @Override
    public Optional<String> description() {
        if (markdownDocAttachment == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(markdownDocAttachment.description);
    }

    /**
     * Get the parameter list in the documentation as a map where parameter name being the key.
     *
     * @return {@link Map} of parameter names and descriptions
     */
    @Override
    public Map<String, String> parameterMap() {
        HashMap<String, String> paramMap = new HashMap<>();
        if (this.markdownDocAttachment != null) {
            this.markdownDocAttachment.parameters
                    .forEach(parameter -> paramMap.put(parameter.name, parameter.description));
        }

        return paramMap;
    }

    /**
     * Get the return value description.
     *
     * @return {@link Optional} return description
     */
    @Override
    public Optional<String> returnDescription() {
        if (this.markdownDocAttachment == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.markdownDocAttachment.returnValueDescription);
    }
}
