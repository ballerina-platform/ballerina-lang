/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.internal.treegen.model.json;

/**
 * Represents metadata related to a syntax node as per the syntax_node_metadata.json.
 *
 * @since 2201.8.0
 */
public class SyntaxNodeMetadata {

    private final String createdYear;
    private final String since;

    public SyntaxNodeMetadata(String createdYear, String since) {
        this.createdYear = createdYear;
        this.since = since;
    }

    public String getCreatedYear() {
        return createdYear;
    }

    public String getSince() {
        return since;
    }
}
