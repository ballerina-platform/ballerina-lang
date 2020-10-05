package org.ballerinalang.test.observability.tracing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
public class BaseTestCase extends BaseTest {
    private static BServerInstance servicesServerInstance;

    private static final String OBESERVABILITY_TEST_BIR = System.getProperty("observability.test.utils");
    private static final String TEST_NATIVES_JAR = "observability-test-natives.jar";
    private static final String TEST_OBSERVE_JAR = "ballerina-testobserve-0.0.0.jar";

    @BeforeGroups(value = "tracing-test", alwaysRun = true)
    private void setup() throws Exception {
        final String serverHome = balServer.getServerHome();

        // Copy for Ballerina.toml reference to natives Jar
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(Paths.get(System.getProperty(TEST_NATIVES_JAR)).getParent().toString(), TEST_NATIVES_JAR)
                        .toFile());

        // Copy to bre/libs
        copyFile(new File(System.getProperty(TEST_NATIVES_JAR)),
                Paths.get(serverHome, "bre", "lib", TEST_NATIVES_JAR).toFile());
        copyFile(Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir-jar", TEST_OBSERVE_JAR).toFile(),
                Paths.get(serverHome, "bre", "lib", TEST_OBSERVE_JAR).toFile());

        // Copy to lib/repo
        Path observeTestBaloPath =
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-balo", "repo", "ballerina", "testobserve");
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "lib", "repo", "ballerina").toFile());

        // Copy to bir-cache
        FileUtils.copyDirectoryToDirectory(observeTestBaloPath.toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());
        FileUtils.copyDirectoryToDirectory(
                Paths.get(OBESERVABILITY_TEST_BIR, "build", "generated-bir", "ballerina", "testobserve").toFile(),
                Paths.get(serverHome, "bir-cache", "ballerina").toFile());

        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "observability" + File.separator + "tracing").getAbsolutePath();

        String configFile = Paths.get("src", "test", "resources", "observability", "tracing", "ballerina.conf")
                .toFile().getAbsolutePath();
        String[] args = new String[] { "--b7a.config.file=" + configFile };

        // Don't use 9898 port here. It is used in metrics test cases.
        servicesServerInstance = new BServerInstance(balServer);
        servicesServerInstance.startServer(basePath, "testservices", null, args, null);
    }

    @AfterGroups(value = "tracing-test", alwaysRun = true)
    private void cleanup() throws Exception {
        servicesServerInstance.removeAllLeechers();
        servicesServerInstance.shutdownServer();
    }

    private void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }

    protected List<BMockSpan> getFinishedSpans(String serviceName, String resource) throws IOException {
        return getFinishedSpans(serviceName).stream()
                .filter(span -> Objects.equals(span.getTags().get("resource"), resource))
                .collect(Collectors.toList());
    }

    protected List<BMockSpan> getFinishedSpans(String service) throws IOException {
        String requestUrl = "http://localhost:9090/mockTracer/getMockTraces";
        String data = HttpClientRequest.doPost(requestUrl, service, Collections.emptyMap()).getData();
        Type type = new TypeToken<List<BMockSpan>>() { }.getType();
        return new Gson().fromJson(data, type);
    }

    @SafeVarargs
    protected final Map<String, String> toMap(Map.Entry<String, String>... mapEntries) {
        return Stream.of(mapEntries)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
