/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.util;

import org.ballerinalang.jvm.util.RuntimeUtils;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.test.runtime.BTestRunner;
import org.ballerinalang.test.runtime.entity.TestSuite;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.ANON_ORG;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.DOT;

/**
 * Utility methods.
 */
public class TesterinaUtils {

    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;

    /**
     * Cleans up any remaining testerina metadata.
     *
     * @param path The path of the Directory/File to be deleted
     */
    public static void cleanUpDir(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        } catch (IOException e) {
            errStream.println("Error occurred while deleting the dir : " + path.toString() + " with error : "
                                      + e.getMessage());
        }
    }

    /**
     * Execute tests in build.
     *
     * @param sourceRootPath source root path
     * @param testSuite test meta data
     */
    public static void executeTests(Path sourceRootPath, TestSuite testSuite) throws RuntimeException {
        try {
            BTestRunner testRunner = new BTestRunner(outStream, errStream);
            // Run the tests
            testRunner.runTest(testSuite);
            cleanUpDir(sourceRootPath.resolve(TesterinaConstants.TESTERINA_TEMP_DIR));
            if (testRunner.getTesterinaReport().isFailure()) {
                throw new RuntimeException("there are test failures");
            }
        } catch (BallerinaException e) {
            errStream.println("error: " + e.getMessage());
            throw e;
        } catch (Throwable e) {
            RuntimeUtils.silentlyLogBadSad(e);
            throw new RuntimeException("test execution failed due to runtime exception");
        }
    }

    /**
     * Format error message.
     *
     * @param errorMsg error message
     * @return formatted error message
     */
    public static String formatError(String errorMsg) {
        StringBuilder newErrMsg = new StringBuilder();
        errorMsg = errorMsg.replaceAll("\n", "\n\t    ");
        List<String> msgParts = Arrays.asList(errorMsg.split("\n"));

        for (String msg : msgParts) {
            if (msgParts.indexOf(msg) != 0 && !msg.equals("\t    ")) {
                msg = "\t    \t" + msg.trim();
            }
            if (!msg.equals("\t    ")) {
                newErrMsg.append(msg).append("\n");
            }
        }
        return newErrMsg.toString();
    }

    /**
     * Provides Qualified Class Name.
     *
     * @param orgName     Org name
     * @param packageName Package name
     * @param version Package version
     * @param className   Class name
     * @return Qualified class name
     */
    public static String getQualifiedClassName(String orgName, String packageName,
                                               String version, String className) {
        if (!DOT.equals(packageName)) {
            className = packageName.replace('.', '_') + "." + version.replace('.', '_') + "." + className;
        }
        if (!ANON_ORG.equals(orgName)) {
            className = orgName.replace('.', '_') + "." +  className;
        }
        return className;
    }
}
