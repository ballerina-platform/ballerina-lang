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
package org.ballerinalang.openapi.validator;

import org.ballerinalang.util.diagnostic.Diagnostic;

import java.util.List;

/**
 * This for model the all tag, operations, excludeTags and excludeOperations filters.
 */
public class Filters {
    private List<String> tag;
    private List<String> excludeTag;
    private List<String> operation;
    private List<String> excludeOperation;
    private Diagnostic.Kind kind;

    public Filters(List<String> tag, List<String> excludeTag, List<String> operation,
                   List<String> excludeOperation, Diagnostic.Kind kind) {
        this.tag = tag;
        this.excludeTag = excludeTag;
        this.operation = operation;
        this.excludeOperation = excludeOperation;
        this.kind = kind;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public List<String> getExcludeTag() {
        return excludeTag;
    }

    public void setExcludeTag(List<String> excludeTag) {
        this.excludeTag = excludeTag;
    }

    public List<String> getOperation() {
        return operation;
    }

    public void setOperation(List<String> operation) {
        this.operation = operation;
    }

    public List<String> getExcludeOperation() {
        return excludeOperation;
    }

    public Diagnostic.Kind getKind() {
        return kind;
    }

    public void setKind(Diagnostic.Kind kind) {
        this.kind = kind;
    }

    public void setExcludeOperation(List<String> excludeOperation) {
        this.excludeOperation = excludeOperation;
    }
}
