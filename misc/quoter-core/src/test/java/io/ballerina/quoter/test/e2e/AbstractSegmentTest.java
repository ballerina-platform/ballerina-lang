/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.quoter.test.e2e;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.quoter.BallerinaQuoter;
import io.ballerina.quoter.config.QuoterConfig;
import io.ballerina.quoter.test.FileReaderUtils;
import io.ballerina.quoter.test.TemplateCode;
import io.ballerina.quoter.test.TestQuoterConfig;
import net.openhft.compiler.CachedCompiler;
import org.testng.Assert;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Test Base class with several helper functions.
 */
public abstract class AbstractSegmentTest {
    private static final String TEMPLATE_PACKAGE_NAME = "templatepkg.TemplateCodeImpl";

    /**
     * Creates a segment tree and run it via dynamic class loading.
     *
     * @param config Configuration to run
     * @return Output from the generated code.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected SyntaxTree createSegmentAndRun(String sourceCode, QuoterConfig config) {
        try {
            String javaCode = BallerinaQuoter.run(sourceCode, config);

            TemplateCode templateCode = AccessController.doPrivileged(
                    (PrivilegedAction<TemplateCode>) () -> {
                        try {
                            ClassLoader classLoader = new SegmentClassLoader();
                            CachedCompiler compiler = new CachedCompiler(null, null);
                            Class templateCodeImpl = compiler.loadFromJava(classLoader, TEMPLATE_PACKAGE_NAME, javaCode);
                            return (TemplateCode) templateCodeImpl.getDeclaredConstructor().newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

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
    protected void testForGeneratedCode(String sourceCode, String formatter, String templateFile) {
        sourceCode = sourceCode.trim();
        int tabSpace = 2;
        QuoterConfig config = new TestQuoterConfig(templateFile, tabSpace, formatter);
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
        testForGeneratedCode(sourceCode, "default", "template-default.java");
        testForGeneratedCode(sourceCode, "variable", "template-variable.java");
        testForGeneratedCode(sourceCode, "none", "template-default.java");
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

    private static class SegmentClassLoader extends ClassLoader {
    }
}
