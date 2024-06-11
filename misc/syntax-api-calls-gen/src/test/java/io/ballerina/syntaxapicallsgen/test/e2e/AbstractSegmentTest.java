/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.test.e2e;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGen;
import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;
import io.ballerina.syntaxapicallsgen.test.FileReaderUtils;
import io.ballerina.syntaxapicallsgen.test.TemplateCode;
import net.openhft.compiler.CachedCompiler;
import org.testng.Assert;

import java.io.File;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Test Base class with several helper functions.
 *
 * @since 2.0.0
 */
public abstract class AbstractSegmentTest {
    private static final String TEMPLATE_PACKAGE_NAME = "templatepkg.TemplateCodeImpl";

    /**
     * Creates a segment tree and run it via dynamic class loading.
     *
     * @param config Configuration to run
     * @return Output from the generated code.
     */
    protected SyntaxTree createSegmentAndRun(String sourceCode, SyntaxApiCallsGenConfig config) {
        try {
            String javaCode = SyntaxApiCallsGen.generate(sourceCode, config);
            TemplateCode templateCode = AccessController.doPrivileged(new TemplatePrivilegedAction(javaCode));
            return templateCode.getNode().syntaxTree();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Creates a segment tree and run it via dynamic class loading.
     * Then reads the name and check if the output from the generated code
     * is the same as the source code.
     *
     * @param sourceCode   Input source code.
     * @param formatter    Base formatter name to use.
     * @param templateFile Template to use for dynamic class loading.
     */
    protected void testForGeneratedCode(String sourceCode, SyntaxApiCallsGenConfig.Formatter formatter,
                                        File templateFile)
            throws URISyntaxException {
        sourceCode = sourceCode.trim();
        SyntaxApiCallsGenConfig config = new SyntaxApiCallsGenConfig.Builder()
                .templateFile(templateFile)
                .formatter(formatter)
                .formatterTabStart(2)
                .parserTimeout(10000)
                .ignoreMinutiae(false)
                .parser(SyntaxApiCallsGenConfig.Parser.MODULE)
                .build();
        SyntaxTree tree = createSegmentAndRun(sourceCode, config);
        Assert.assertEquals(tree.toSourceCode().trim(), sourceCode);
    }

    /**
     * Tests if the generated code for the given source code (after being formatted with all the formatters)
     * is valid and the generated code creates the same source code when run.
     *
     * @param sourceCode Input source code
     */
    protected void testAssertionContent(String sourceCode) {
        try {
            File templateDefaultFile = FileReaderUtils.getResourceFile("template-default.java");
            File templateVariableFile = FileReaderUtils.getResourceFile("template-variable.java");
            testForGeneratedCode(sourceCode, SyntaxApiCallsGenConfig.Formatter.DEFAULT, templateDefaultFile);
            testForGeneratedCode(sourceCode, SyntaxApiCallsGenConfig.Formatter.VARIABLE, templateVariableFile);
            testForGeneratedCode(sourceCode, SyntaxApiCallsGenConfig.Formatter.NONE, templateDefaultFile);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Tests if the generated code for the given file (after being formatted with all the formatters)
     * is valid and the generated code creates the same source code when run.
     *
     * @param fileName File name.
     */
    protected void test(String fileName) {
        String sourceCode = FileReaderUtils.readFileAsResource(fileName).trim();
        testAssertionContent(sourceCode);
    }

    /**
     * A class loader for java runner.
     *
     * @since 2.0.0
     */
    private static class SegmentClassLoader extends ClassLoader {
    }

    /**
     * A privileged action runner to execute generated test code.
     *
     * @since 2.0.0
     */
    private static class TemplatePrivilegedAction implements PrivilegedAction<TemplateCode> {
        private final String javaCode;

        private TemplatePrivilegedAction(String javaCode) {
            this.javaCode = javaCode;
        }

        @Override
        public TemplateCode run() {
            try (CachedCompiler compiler = new CachedCompiler(null, null)) {
                ClassLoader classLoader = new SegmentClassLoader();
                Class<?> templateCodeImpl = compiler.loadFromJava(classLoader, TEMPLATE_PACKAGE_NAME, javaCode);
                return (TemplateCode) templateCodeImpl.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
