/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.observability.tracing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.ballerina.testobserve.tracing.extension.BMockSpan;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Base Test Case which is inherited by all the Tracing Test Cases.
 */
@Test(groups = "tracing-test")
public class HttpTracingBaseTest extends BaseTest {     // TODO: Move this test case to ballerina/http module
    private static BServerInstance servicesServerInstance;
    private static BServerInstance backendServerInstance;

    private static final String OBESERVABILITY_TEST_BIR = System.getProperty("observability.test.utils");
    private static final String RESOURCE_LOCATION = "src" + File.separator + "test" + File.separator +
            "resources" + File.separator + "observability" + File.separator + "tracing" + File.separator;
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";
    private static final String TEST_OBSERVE_JAR = "ballerina-testobserve-0.0.0.jar";

    private static final String DEST_FUNCTIONS_JAR = File.separator + "bre" + File.separator + "lib"
            + File.separator + TEST_NATIVES_JAR;

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    private void setup() throws Exception {
        final String serverHome = balServer.getServerHome();

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),  Paths.get(Paths.get(System.getProperty
                (TEST_NATIVES_JAR)).getParent() +  File.separator + TEST_NATIVES_JAR).toFile());

        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)), new File(serverHome + DEST_FUNCTIONS_JAR));

        // copy to bre/libs
        Path observeTestBaloPath =
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-balo", "repo", "ballerina", "testobserve");
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "lib", "repo", "ballerina").toFile());

        // copy to bir-cache
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());
        FileUtils.copyDirectoryToDirectory(
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir", "ballerina", "testobserve").toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());

        // copy code-gen-ed generated-jar
        copyFile(Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir-jar", TEST_OBSERVE_JAR).toFile(),
                Paths.get(serverHome, "bre", "lib", TEST_OBSERVE_JAR).toFile());

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "observability" + File.separator + "tracing").getAbsolutePath();

        String configFile = new File(RESOURCE_LOCATION + "ballerina.conf").getAbsolutePath();
        String[] args = new String[] { "--b7a.config.file=" + configFile };

        // Don't use 9898 port here. It is used in metrics test cases.
        {
            int[] requiredPorts = new int[]{10010, 10011};
            servicesServerInstance = new BServerInstance(balServer);
            servicesServerInstance.startServer(basePath, "backend", null, args, requiredPorts);
        }
        {
            int[] requiredPorts = new int[]{9090, 9091};
            backendServerInstance = new BServerInstance(balServer);
            backendServerInstance.startServer(basePath, "httptracing", null, args, requiredPorts);
        }
    }

    @AfterGroups(value = "tracing-test", alwaysRun = true)
    private void cleanup() throws Exception {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
        backendServerInstance.removeAllLeechers();
        backendServerInstance.shutdownServer();
    }

    private void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }

    private List<BMockSpan> getFinishedSpans(int port, String service) throws IOException {
        String requestUrl = "http://localhost:" + port + "/mock-tracer/spans/"
                + URLEncoder.encode(service, StandardCharsets.UTF_8.toString());
        String data = HttpClientRequest.doGet(requestUrl).getData();
        Type type = new TypeToken<List<BMockSpan>>() { }.getType();
        return new Gson().fromJson(data, type);
    }

    protected List<BMockSpan> getFinishedSpans(String serviceName, String resource) throws IOException {
        return getFinishedSpans(9090, serviceName).stream()
                .filter(span -> Objects.equals(span.getTags().get("resource"), resource))
                .collect(Collectors.toList());
    }

    protected List<BMockSpan> getFinishedSpans(String serviceName) throws IOException {
        return getFinishedSpans(9090, serviceName);
    }

    protected List<BMockSpan> getEchoBackendFinishedSpans() throws IOException {
        return getFinishedSpans(10010, "echoServiceOne");
    }

    @SafeVarargs
    protected final Map<String, String> toMap(Map.Entry<String, String>... mapEntries) {
        return Stream.of(mapEntries)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
