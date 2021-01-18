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
package org.ballerinalang.langserver.toml;

import io.ballerina.toml.syntax.tree.Node;

/**
 * Represents a value in Kubernetes.toml Probe.
 *
 * @param <T> Value type
 * @since 2.0.0
 */
public class ProbeValue<T> {

    private final Node node;
    private final T value;

    public ProbeValue(T value, Node node) {
        this.node = node;
        this.value = value;
    }

    public Node getNode() {
        return node;
    }

    public T getValue() {
        return value;
    }
}
