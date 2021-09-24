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
package io.ballerinalang.compiler.internal.treegen.targets;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.ballerinalang.compiler.internal.treegen.TreeGenConfig;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNode;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxNodeAttribute;
import io.ballerinalang.compiler.internal.treegen.model.json.SyntaxTree;
import io.ballerinalang.compiler.internal.treegen.model.template.Field;
import io.ballerinalang.compiler.internal.treegen.model.template.TreeNodeClass;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The class {@code Target} represent a generic entity that converts a
 * given {@code SyntaxTree} into source code.
 *
 * @since 1.3.0
 */
public abstract class Target {
    protected static final String INTERNAL_BASE_NODE_CN = "STNode";
    protected static final String SYNTAX_KIND_CN = "SyntaxKind";
    protected static final String EXTERNAL_BASE_NODE_CN = "Node";
    protected static final String EXTERNAL_BASE_NON_TERMINAL_NODE_CN = "NonTerminalNode";

    protected static final String DOT = ".";
    private static final String JAVA_EXT = "java";

    protected final TreeGenConfig config;

    public Target(TreeGenConfig config) {
        this.config = config;
    }

    public abstract List<SourceText> execute(SyntaxTree syntaxTree);

    protected abstract String getTemplateName();

    protected TreeNodeClass convertToTreeNodeClass(SyntaxNode syntaxNode,
                                                   String packageName,
                                                   List<String> importClassNameList) {
        TreeNodeClass nodeClass = new TreeNodeClass(packageName,
                syntaxNode.getName(), syntaxNode.isAbstract(), syntaxNode.getBase(),
                getFields(syntaxNode), syntaxNode.getKind());

        // TODO Can we pass this as part of the constructor
        nodeClass.addImports(importClassNameList);
        return nodeClass;
    }

    protected SourceText getSourceText(Object treeNodeClass, String outputDir, String className) {
        String content = generateTextContent(treeNodeClass);
        Path filePath = getFilePath(outputDir, className);
        return new SourceText(filePath, content);
    }

    private String generateTextContent(Object treeNodeClass) {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(getTemplateName());
        Writer writer = new StringWriter();
        mustache.execute(writer, treeNodeClass);
        return prepareText(writer.toString());
    }

    private Path getFilePath(String outputDir, String className) {
        return Paths.get(outputDir, getClassFileName(className)).toAbsolutePath();
    }

    protected String getClassFQN(String packageName, String className) {
        return packageName + DOT + className;
    }

    private String getClassFileName(String className) {
        return className + DOT + JAVA_EXT;
    }

    private String prepareText(String inputText) {
        return inputText.replace("&lt;", "<").replace("&gt;", ">").replace("&#9;", "\t").replace(" &#10;", "\n");
    }

    private List<Field> getFields(SyntaxNode syntaxNode) {
        List<SyntaxNodeAttribute> attributes = syntaxNode.getAttributes();
        int attributeCount = attributes.size();
        List<Field> fields = new ArrayList<>(attributeCount);
        for (int index = 0; index < attributeCount; index++) {
            SyntaxNodeAttribute attribute = attributes.get(index);
            boolean isLast = (index + 1) == attributeCount;
            fields.add(new Field(attribute.getType(), attribute.getName(), index,
                    attribute.getOccurrenceKind(), attribute.isOptional(), isLast));
        }
        return fields;
    }
}
