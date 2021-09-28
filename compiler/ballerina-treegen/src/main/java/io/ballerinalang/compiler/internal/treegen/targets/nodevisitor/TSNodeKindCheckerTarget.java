/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.internal.treegen.targets.nodevisitor;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClassContainer;
import io.ballerinalang.compiler.internal.treegen.targets.SourceText;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.TS_NODE_KIND_CHECKER_TEMPLATE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.TS_NODE_OUTPUT_DIR_KEY;

/**
 * Generates a node visitor TS kind checker class for the TS syntax tree.
 *
 * @since 2.0.0
 */
public class TSNodeKindCheckerTarget extends AbstractNodeVisitorTarget {
    public static final String TS_EXT = "ts";
    public static final String FILE_NAME = "check-kind-util";

    public TSNodeKindCheckerTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    protected String getTemplateName() {
        return this.config.getOrThrow(TS_NODE_KIND_CHECKER_TEMPLATE_KEY);
    }

    @Override
    protected String getPackageName() {
        return null;
    }

    @Override
    protected String getOutputDir() {
        return this.config.getOrThrow(TS_NODE_OUTPUT_DIR_KEY);
    }

    @Override
    protected String getClassName() {
        return null;
    }

    @Override
    protected String getSuperClassName() {
        return null;
    }

    @Override
    protected List<String> getImportClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<SourceText> execute(SyntaxTree syntaxTree) {
        List<TreeNodeClass> treeNodeClasses = generateNodeClasses(syntaxTree);
        return Collections.singletonList(
                this.getSourceTextForTs(treeNodeClasses, getOutputDir(), FILE_NAME));
    }

    private List<TreeNodeClass> generateNodeClasses(SyntaxTree syntaxTree) {
        return syntaxTree.nodes()
                .stream()
                .map(syntaxNode -> convertToTreeNodeClass(syntaxNode,
                        getPackageName(), new ArrayList<>()))
                .collect(Collectors.toList());
    }

    protected SourceText getSourceTextForTs(List<TreeNodeClass> treeNodeClasses, String outputDir, String fileName) {
        String content = generateTextContent(treeNodeClasses);
        Path filePath = getFilePath(outputDir, fileName);
        return new SourceText(filePath, content);
    }

    private Path getFilePath(String outputDir, String fileName) {
        return Paths.get(outputDir, getClassFileName(fileName)).toAbsolutePath();
    }

    private String getClassFileName(String className) {
        return className + DOT + TS_EXT;
    }

    private String generateTextContent(List<TreeNodeClass> treeNodeClasses) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(getTemplateName());
        Writer writer = new StringWriter();
        TreeNodeClassContainer treeNodeClassContainer = new TreeNodeClassContainer(treeNodeClasses);
        mustache.execute(writer, treeNodeClassContainer);
        return this.prepareText(writer.toString());
    }

    private String prepareText(String inputText) {
        return inputText.replace("&lt;", "<").replace("&gt;", ">").replace("&#9;", "\t").replace(" &#10;", "\n");
    }
}
