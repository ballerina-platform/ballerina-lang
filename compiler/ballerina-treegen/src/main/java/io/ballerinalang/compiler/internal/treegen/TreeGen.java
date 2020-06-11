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
package io.ballerinalang.compiler.internal.treegen;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.targets.SourceText;
import io.ballerinalang.compiler.internal.treegen.targets.Target;
import io.ballerinalang.compiler.internal.treegen.targets.node.ExternalNodeTarget;
import io.ballerinalang.compiler.internal.treegen.targets.node.InternalNodeTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.ExternalNodeVisitorTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.InternalNodeTransformerTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.InternalNodeVisitorTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.InternalTreeModifierTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.NodeFactoryTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.NodeTransformerTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.STNodeFactoryTarget;
import io.ballerinalang.compiler.internal.treegen.targets.nodevisitor.TreeModifierTarget;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.SYNTAX_TREE_DESCRIPTOR_KEY;

/**
 * The driver of the treegen tool.
 *
 * @since 1.3.0
 */
public class TreeGen {

    public static void main(String[] args) {
        // 1) Load the configuration properties
        TreeGenConfig config = TreeGenConfig.getInstance();
        // 2) Get the syntax tree model by parsing the descriptor
        SyntaxTree syntaxTree = getSyntaxTree(config);
        // 3) Initialize the registered source code generation targets
        List<Target> targetList = populateAvailableTargets(config);
        // 4) Run above targets and write the content to files
        targetList.stream()
                .map(target -> target.execute(syntaxTree))
                .flatMap(Collection::stream)
                .forEach(TreeGen::writeSourceTextFile);
    }

    private static List<Target> populateAvailableTargets(TreeGenConfig config) {
        List<Target> targetList = new ArrayList<>();
        targetList.add(new InternalNodeTarget(config));
        targetList.add(new ExternalNodeTarget(config));
        targetList.add(new STNodeFactoryTarget(config));
        targetList.add(new InternalNodeVisitorTarget(config));
        targetList.add(new ExternalNodeVisitorTarget(config));
        targetList.add(new NodeTransformerTarget(config));
        targetList.add(new InternalNodeTransformerTarget(config));
        targetList.add(new TreeModifierTarget(config));
        targetList.add(new InternalTreeModifierTarget(config));
        targetList.add(new NodeFactoryTarget(config));
        return targetList;
    }

    private static SyntaxTree getSyntaxTree(TreeGenConfig config) {
        try (InputStreamReader reader = new InputStreamReader(getSyntaxTreeStream(config), Charsets.UTF_8)) {
            return new Gson().fromJson(reader, SyntaxTree.class);
        } catch (Throwable e) {
            throw new TreeGenException("Failed to parse the syntax tree descriptor. Reason: " + e.getMessage(), e);
        }
    }

    private static InputStream getSyntaxTreeStream(TreeGenConfig config) {
        String jsonFilePath = config.getOrThrow(SYNTAX_TREE_DESCRIPTOR_KEY);
        InputStream syntaxTreeStream = TreeGen.class.getClassLoader().getResourceAsStream(jsonFilePath);
        if (Objects.isNull(syntaxTreeStream)) {
            throw new TreeGenException("Syntax tree descriptor is not available. file-name: " + jsonFilePath);
        }
        return syntaxTreeStream;
    }

    private static void writeSourceTextFile(SourceText sourceText) {
        try (BufferedWriter writer = Files.newBufferedWriter(sourceText.filePath, Charsets.UTF_8)) {
            writer.write(sourceText.content);
        } catch (IOException e) {
            throw new TreeGenException("Failed to write the content to " + sourceText.filePath +
                    ". Reason: " + e.getMessage(), e);
        }
    }
}

