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
package org.ballerinalang.bindgen.utils;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.io.IOUtils;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.model.JError;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DEFAULT_TEMPLATE_DIR;

/**
 * Class for generating the Ballerina bindings as a syntax tree for a given Java class.
 *
 * @since 2.0.0
 */
public class BindgenFileGenerator {

    private JClass jClass;
    private Class<?> currentClass;
    private SyntaxTree syntaxTree;

    private final BindgenEnv env;
    private final Path jClassTemplatePath = Paths.get(DEFAULT_TEMPLATE_DIR, "jclass.bal");
    private final Path jEmptyClassTemplatePath = Paths.get(DEFAULT_TEMPLATE_DIR, "empty_jclass.bal");
    private final Path jErrorTemplatePath = Paths.get(DEFAULT_TEMPLATE_DIR, "jerror.bal");
    private static final CharSequence UNIX_FS = "/";

    public BindgenFileGenerator(BindgenEnv env) {
        this.env = env;
    }

    public SyntaxTree generate(JError jError) throws BindgenException {
        this.currentClass = jError.getCurrentClass();
        return generateFromTemplate(jErrorTemplatePath, jError.getShortExceptionName());
    }

    public SyntaxTree generate(JClass jClass) throws BindgenException {
        this.jClass = jClass;
        this.currentClass = jClass.getCurrentClass();
        setClassNameAlias();
        if (!env.isDirectJavaClass()) {
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
            return new BindgenTreeModifier(jClass, env).transform(syntaxTree.rootNode()).syntaxTree();
        } else {
            throw new BindgenException("error: unable to generate the binding class '"
                    + jClass.getCurrentClass().getName() + "'");
        }
    }

    private SyntaxTree generateFromTemplate(Path filePath) throws BindgenException {
        return generateFromTemplate(filePath, env.getAlias(currentClass.getName()));
    }

    private SyntaxTree generateFromTemplate(Path filePath, String alias) throws BindgenException {
        String content = readTemplateFile(filePath);
        return replacePlaceholders(content, alias);
    }

    private SyntaxTree replacePlaceholders(String content, String alias) {
        String modifiedContent = content.replace("FULL_CLASS_NAME", currentClass.getName())
                .replace("CLASS_TYPE", currentClass.isInterface() ? "interface" : "class")
                .replace("INS_CLASS_NAME", currentClass.getName().replace("$", "\\$"))
                .replace("SIMPLE_CLASS_NAME_CAPS", alias.toUpperCase(Locale.getDefault()))
                .replace("SIMPLE_CLASS_NAME", alias)
                .replace("ACCESS_MODIFIER", env.hasPublicFlag() ? "public " : "");
        return SyntaxTree.from(TextDocuments.from(modifiedContent));
    }

    /*
     * Set the Ballerina class name for the Java class. By default the simple class name is used.
     * If conflicting class names are found, an incremental integer is appended to the Ballerina class name.
     * */
    private void setClassNameAlias() {
        if (env.getAlias(currentClass.getName()) != null) {
            return;
        }
        String className = currentClass.getName();
        String simpleClassName = currentClass.getSimpleName();
        if (env.getAliasClassName(simpleClassName) == null && env.getAliasClassName(simpleClassName + 1) == null) {
            env.setAlias(simpleClassName, className);
        } else {
            int i = 1;
            while (true) {
                String alias = simpleClassName + i;
                if (i == 1 && env.getAliasClassName(alias) == null) {
                    // If Ballerina classes requiring duplicate short names exist, rename the initial one
                    // to hold a prefix. By default the initial one does not have a prefix.
                    String previousClassName = env.removeAlias(simpleClassName);
                    env.setAlias(alias, previousClassName);
                } else if (env.getAliasClassName(alias) == null) {
                    env.setAlias(alias, className);
                    break;
                }
                i++;
            }
        }
    }

    private String readTemplateFile(Path filePath) throws BindgenException {
        try {
            // 'java.lang.ClassLoader#getResourceAsStream' accepts only UNIX file separators.
            String unixPath = filePath.toString().replace(File.separator, UNIX_FS);
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(unixPath);
            return IOUtils.toString(stream, UTF_8);
        } catch (Exception e) {
            throw new BindgenException("error: unable to read the internal template file: " + e.getMessage(), e);
        }
    }
}
