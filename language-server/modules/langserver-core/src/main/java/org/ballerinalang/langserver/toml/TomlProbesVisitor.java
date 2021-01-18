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

import io.ballerina.toml.syntax.tree.KeyValueNode;
import io.ballerina.toml.syntax.tree.NodeVisitor;
import io.ballerina.toml.syntax.tree.NumericLiteralNode;
import io.ballerina.toml.syntax.tree.StringLiteralNode;
import io.ballerina.toml.syntax.tree.SyntaxKind;
import io.ballerina.toml.syntax.tree.TableNode;
import io.ballerina.toml.syntax.tree.Token;
import io.ballerina.toml.syntax.tree.ValueNode;

import java.util.Optional;

/**
 * Visitor for collecting probe details from toml syntax tree.
 *
 * @since 2.0.0
 */
public class TomlProbesVisitor extends NodeVisitor {

    private ProbeStore store = new ProbeStore();

    @Override
    public void visit(TableNode tableNode) {
        if (TomlSyntaxTreeUtil.toDottedString(tableNode.identifier()).equals("cloud.deployment.probes.liveness")) {
            Probe livenessProbe = extractProbeInfo(tableNode);
            this.store.setLiveness(livenessProbe);
        }
        if (TomlSyntaxTreeUtil.toDottedString(tableNode.identifier()).equals("cloud.deployment.probes.readiness")) {
            Probe readinessProbe = extractProbeInfo(tableNode);
            this.store.setReadiness(readinessProbe);
        }
    }

    private Probe extractProbeInfo(TableNode tableNode) {
        Probe probe = new Probe();
        for (KeyValueNode keyValueNode : tableNode.fields()) {
            String key = TomlSyntaxTreeUtil.toDottedString(keyValueNode.identifier());
            if (key.equals("port")) {
                ValueNode value = keyValueNode.value();
                if (value.kind() == SyntaxKind.DEC_INT) {
                    NumericLiteralNode numericLiteralNode = (NumericLiteralNode) value;
                    String port = numericLiteralNode.value().text();
                    probe.setPort(new ProbeValue<>(Integer.parseInt(port), numericLiteralNode.value()));
                }
            }
            if (key.equals("path")) {
                ValueNode value = keyValueNode.value();
                if (value.kind() == SyntaxKind.STRING_LITERAL) {
                    StringLiteralNode stringLiteralNode = (StringLiteralNode) value;
                    Optional<Token> content = stringLiteralNode.content();
                    String path = "";
                    if (content.isPresent()) {
                        path = content.get().text();
                        probe.setPath(new ProbeValue<>(path, content.get()));
                    }

                }
            }
        }
        return probe;
    }

    public ProbeStore getStore() {
        return store;
    }
}
