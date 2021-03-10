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
package org.ballerinalang.bindgen.model;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.utils.BindgenEnv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Class for generating the Ballerina bindings as a syntax tree for a given Java class.
 *
 * @since 2.0.0
 */
public class BindingsGenerator {

    private JClass jClass;
    private SyntaxTree syntaxTree;
    private BindgenEnv env;
    private String templateDirectory = Paths.get("src", "main", "resources", "templates").toString();
    private Path jClassTemplatePath = Paths.get(templateDirectory, "jclass.bal");
    private Path jEmptyClassTemplatePath = Paths.get(templateDirectory, "empty_jclass.bal");
    private Path jErrorTemplatePath = Paths.get(templateDirectory, "jerror.bal");

    public BindingsGenerator(BindgenEnv env) {
        this.env = env;
    }

    public SyntaxTree generate(JClass jClass) throws BindgenException {
        this.jClass = jClass;
        setClassNameAlias();
        if (Throwable.class.isAssignableFrom(jClass.getCurrentClass())) {
            // Generate Ballerina error bindings for Java throwable classes.
            return generateFromTemplate(jErrorTemplatePath);
        } else if (!env.isDirectJavaClass()) {
            // Generate Ballerina empty class bindings for dependent Java classes.
            return generateFromTemplate(jEmptyClassTemplatePath);
        } else {
            // Generate Ballerina class bindings for Java classes.
            syntaxTree = generateFromTemplate(jClassTemplatePath);
            return generateSyntaxTree();
        }
    }

    private SyntaxTree generateSyntaxTree() throws BindgenException {
        if (syntaxTree.containsModulePart()) {
            return new BindgenTreeModifier(jClass, env).transform((ModulePartNode) syntaxTree.rootNode()).syntaxTree();
        } else {
            throw new BindgenException("error: unable to generate the binding class `"
                    + jClass.getCurrentClass().getName() + "`");
        }
    }

    private SyntaxTree generateFromTemplate(Path templatePath) throws BindgenException {
        String content = readTemplateFile(templatePath);
        return replacePlaceholders(content);
    }


    private SyntaxTree replacePlaceholders(String content) {
        Class javaClass = jClass.getCurrentClass();
        String modifiedContent = content.replace("FULL_CLASS_NAME", javaClass.getName())
                .replace("CLASS_TYPE", javaClass.isInterface() ? "interface" : "class")
                .replace("SHORT_CLASS_NAME_CAPS", javaClass.getSimpleName().toUpperCase(Locale.getDefault()))
                .replace("SIMPLE_CLASS_NAME", env.getAliasClassName(javaClass.getName()))
                .replace("ACCESS_MODIFIER", env.hasPublicFlag() ? "public " : "");
        return SyntaxTree.from(TextDocuments.from(modifiedContent));
    }

    /*
     * Set the Ballerina class name for the Java class. By default the simple class name is used.
     * If conflicting class names are found, an incremental integer is appended to the Ballerina class name.
     * */
    private void setClassNameAlias() {
        String className = jClass.getCurrentClass().getName();
        String simpleClassName = jClass.getCurrentClass().getSimpleName();
        if (env.getAliasValue(simpleClassName) == null) {
            env.setAlias(simpleClassName, className);
        } else {
            for (int i = 1; i < 10; i++) {
                String alias = simpleClassName + i;
                if (i == 1 && env.getAliasValue(alias) == null) {
                    // If Ballerina classes requiring duplicate short names exist, rename the initial one
                    // to hold a prefix. By default the initial one does not have a prefix.
                    String previousValue = env.removeAlias(simpleClassName);
                    env.setAlias(alias, previousValue);
                } else if (env.getAliasValue(alias) == null) {
                    env.setAlias(alias, className);
                    break;
                }
            }
        }
    }

    private String readTemplateFile(Path path) throws BindgenException {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new BindgenException("error: unable to read the internal template file", e);
        }
    }
}
