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
package org.ballerinalang.test.logging;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

/**
 * Contains test cases to test writing of bad sad exceptions to console.
 *
 * @since 2.0.0
 */
public class BadSadTests extends BaseTest {

    @Test
    public void testBadSadInBuild() throws BallerinaTestException, IOException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        Path dependencyPackage = Paths.get("logging/projects-for-badsad-tests/hello");
        CompileResult compileResult = BCompileUtil.compileAndCacheBala(
                dependencyPackage.toString(), Paths.get(balServer.getServerHome(), "repo"));
        Path distRepo = Paths.get(balServer.getServerHome()).resolve("repo");
        Path dependencyBir = Paths.get("build/repo/cache/badsad/hello/0.1.0/bir/hello.bir");
        Files.createDirectories(distRepo.resolve("cache/badsad/hello/0.1.0/bir"));
        Files.copy(dependencyBir,
                distRepo.resolve("cache/badsad/hello/0.1.0/bir/hello.bir"),
                StandardCopyOption.REPLACE_EXISTING);

        String output = bMainInstance.runMainAndReadStdOut("build", new String[] {}, new HashMap<>(),
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/buildCmdBadSad")
                        .toAbsolutePath().toString(), true);
        Assert.assertTrue(output.contains("ballerina: Oh no, something really went wrong."));

        String expected = "java.lang.IllegalStateException: Cannot find the generated jar library for module: hello\n";
        Assert.assertTrue(output.contains(expected), "\nActual:\n" + output + "\nExpected:\n" + expected);
        Assert.assertFalse(Files.exists(
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/buildCmdBadSad")
                        .toAbsolutePath().resolve("ballerina-internal.log")));
    }

    @Test
    public void testBadSadInRun() throws BallerinaTestException, IOException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("run", new String[] {}, new HashMap<>(),
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/runCmdBadSad")
                        .toAbsolutePath().toString(), true);
        Assert.assertTrue(output.contains("ballerina: Oh no, something really went wrong."));

        String expected = "java.lang.ClassCastException: class io.ballerina.runtime.internal.values.ErrorValue cannot" +
                " be cast to class io.ballerina.runtime.internal.values.ArrayValue " +
                "(io.ballerina.runtime.internal.values.ErrorValue and " +
                "io.ballerina.runtime.internal.values.ArrayValue are in unnamed module of loader 'app')\n";
        Assert.assertTrue(output.contains(expected),
                "\nActual:\n" + output + "\nExpected:\n" + expected);
        Assert.assertFalse(Files.exists(
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/buildCmdBadSad")
                        .toAbsolutePath().resolve("ballerina-internal.log")));
    }

    @Test
    public void testBadSadInTest() throws BallerinaTestException, IOException {
        BMainInstance bMainInstance = new BMainInstance(balServer);
        String output = bMainInstance.runMainAndReadStdOut("build", new String[] {}, new HashMap<>(),
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/runCmdBadSad")
                        .toAbsolutePath().toString(), false);

        String expected = "java.lang.ClassCastException: class io.ballerina.runtime.internal.values.ErrorValue cannot" +
                " be cast to class io.ballerina.runtime.internal.values.ArrayValue " +
                "(io.ballerina.runtime.internal.values.ErrorValue and " +
                "io.ballerina.runtime.internal.values.ArrayValue are in unnamed module of loader 'app')\n";
        Assert.assertTrue(output.contains(expected), "\nActual:\n" + output + "\nExpected:\n" + expected);

        output = bMainInstance.runMainAndReadStdOut("test", new String[] {}, new HashMap<>(),
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/runCmdBadSad")
                        .toAbsolutePath().toString(), false);

        Assert.assertTrue(output.contains(expected), "\nActual:\n" + output + "\nExpected:\n" + expected);
        Assert.assertFalse(Files.exists(
                Paths.get("src/test/resources/logging/projects-for-badsad-tests/buildCmdBadSad")
                        .toAbsolutePath().resolve("ballerina-internal.log")));
    }
}
