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
package io.ballerinalang.compiler.internal.treegen.targets.node;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNode;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClassContainer;
import io.ballerinalang.compiler.internal.treegen.targets.SourceText;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.INTERNAL_NODE_PACKAGE_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.TS_NODE_OUTPUT_DIR_KEY;
import static io.ballerinalang.compiler.internal.treegen.TreeGenConfig.TS_NODE_TEMPLATE_KEY;

/**
 * Generates type script interfaces for each syntax tree node.
 *
 * @since 2.0.0
 */
public class TSNodeTarget extends AbstractNodeTarget {

    public static final String TS_EXT = "ts";
    public static final String FILE_NAME = "syntax-tree-interfaces";

    public TSNodeTarget(TreeGenConfig config) {
        super(config);
    }

    @Override
    protected String getTemplateName() {
        return this.config.getOrThrow(TS_NODE_TEMPLATE_KEY);
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
    protected String getClassName(TreeNodeClass treeNodeClass) {
        return null;
    }

    @Override
    protected List<String> getImportClasses(SyntaxNode syntaxNode) {
        List<String> importClassList = new ArrayList<>();
        importClassList.add(getClassFQN(config.getOrThrow(INTERNAL_NODE_PACKAGE_KEY), INTERNAL_BASE_NODE_CN));
        return importClassList;
    }

    @Override
    public List<SourceText> execute(SyntaxTree syntaxTree) {
        List<SourceText> sourceTexts = new ArrayList<>();
        List<TreeNodeClass> treeNodeClasses = this.generateTreeNodeClasses(syntaxTree);
        SourceText sourceText = this.getSourceText(treeNodeClasses, getOutputDir(), FILE_NAME);
        sourceTexts.add(sourceText);
        return sourceTexts;
    }

    protected SourceText getSourceText(List<TreeNodeClass> treeNodeClasses, String outputDir, String fileName) {
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

    private TreeNodeClass generateNodeClass(SyntaxNode syntaxNode) {
        List<String> importClassList = getImportClasses(syntaxNode);
        return convertToTreeNodeClass(syntaxNode, getPackageName(), importClassList);
    }

    private List<TreeNodeClass> generateTreeNodeClasses(SyntaxTree syntaxTree) {
        return syntaxTree.nodes().stream().map(this::generateNodeClass).collect(Collectors.toList());
    }
}
