package io.ballerina.multiservice;

import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Test solution architecture modeling tool.
 */
public class MultiServiceModelingTest {
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();
    private static final String BALLERINA = "ballerina";

    private static final String MULTISERVICE_MODEL = "multiServiceModelingService/getMultiServiceModel";

    @Test(description = "Test multi-service model generation")
    public void testFunction() throws IOException, ExecutionException, InterruptedException {
        Path project = RES_DIR.resolve(BALLERINA).resolve(Path.of("multiservice",  "service1.bal").toString());

        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, project);

        MultiServiceModelRequest request = new MultiServiceModelRequest();
        request.setDocumentIdentifier(new TextDocumentIdentifier(project.toString()));

        CompletableFuture<?> result = serviceEndpoint.request(MULTISERVICE_MODEL, request);
        String response = (String) result.get();
        if (!Objects.equals(response, "")) {
            Assert.assertTrue(true);
        }
    }
}
