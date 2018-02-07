package org.ballerinalang.langserver.hover;

import com.google.gson.Gson;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

/**
 * Tests hover feature.
 */
public class HoverProviderTest {
    private static final String TESTS_SAMPLES = "src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "hover";
    private static final String ROOT_DIR = Paths.get("").toAbsolutePath().toString() + File.separator;
    private static final String SAMPLES_COPY_DIR = ROOT_DIR + "samples" + File.separator + "hover";
    private String balPath = SAMPLES_COPY_DIR + File.separator + "test" + File.separator + "hover.bal";

    private BallerinaLanguageServer ballerinaLanguageServer;
    private Endpoint serviceEndpoint;
    private Gson gson;
    private String balFileContent;

    @BeforeMethod
    public void loadLangServer() throws IOException {
        ballerinaLanguageServer = new BallerinaLanguageServer();
        serviceEndpoint = ServiceEndpoints.toEndpoint(ballerinaLanguageServer);
        gson = new Gson();
        File source = new File(TESTS_SAMPLES);
        File destination = new File(SAMPLES_COPY_DIR);
        org.apache.commons.io.FileUtils.copyDirectory(source, destination);
        byte[] encoded = Files.readAllBytes(Paths.get(balPath));
        balFileContent = new String(encoded);
    }

    @Test(description = "Test Hover for built in functions", dataProvider = "hoverBuiltinFuncPosition")
    public void hoverForBuiltInFunctionTest(Position position, String expectedFile)
            throws URISyntaxException, InterruptedException, IOException {
        Assert.assertEquals(getHoverResponseMessageAsString(position),
                getExpectedValue(expectedFile),
                "Did not match the hover content for " + expectedFile
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's functions", dataProvider = "hoverCurrentPackageFuncPosition")
    public void hoverForCurrentPackageFunctionTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(getHoverResponseMessageAsString(position),
                getExpectedValue(expectedFile),
                "Did not match the hover content for " + expectedFile
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's enums", dataProvider = "hoverCurrentPackageEnumPosition")
    public void hoverForCurrentPackageEnumTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(getHoverResponseMessageAsString(position),
                getExpectedValue(expectedFile),
                "Did not match the hover content for " + expectedFile
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @Test(description = "Test Hover for current package's structs", dataProvider = "hoverCurrentPackageStructPosition")
    public void hoverForCurrentPackageStructTest(Position position, String expectedFile)
            throws InterruptedException, IOException {
        Assert.assertEquals(getHoverResponseMessageAsString(position),
                getExpectedValue(expectedFile),
                "Did not match the hover content for " + expectedFile
                        + " and position line:" + position.getLine() + " character:" + position.getCharacter());
    }

    @DataProvider(name = "hoverBuiltinFuncPosition")
    public Object[][] getBuiltinFunctionPositions() {
        return new Object[][]{
                {new Position(39, 7), "builtin-function1.json"},
                {new Position(40, 19), "builtin-function2.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageFuncPosition")
    public Object[][] getCurrentPackageFunctionPositions() {
        return new Object[][]{
                {new Position(41, 15), "currentPkg-function1.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageEnumPosition")
    public Object[][] getCurrentPackageEnumPositions() {
        return new Object[][]{
                {new Position(29, 20), "currentPkg-enum.json"},
                {new Position(30, 7), "currentPkg-enum.json"},
                {new Position(30, 20), "currentPkg-enum.json"},
                {new Position(31, 12), "currentPkg-enum.json"},
                {new Position(32, 8), "currentPkg-enum.json"},
                {new Position(32, 14), "currentPkg-enum.json"}
        };
    }

    @DataProvider(name = "hoverCurrentPackageStructPosition")
    public Object[][] getCurrentPackageStructPositions() {
        return new Object[][]{
                {new Position(42, 7), "currentPkg-struct.json"},
                {new Position(47, 19), "currentPkg-struct.json"},
                {new Position(48, 8), "currentPkg-struct.json"}
        };
    }

    @AfterClass
    public void cleanSamplesCopy() throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(new File(ROOT_DIR + "samples"));
    }

    /**
     * Get the hover response message as a string.
     *
     * @param position hovering position.
     * @return json string value of the response
     */
    private String getHoverResponseMessageAsString(Position position) throws InterruptedException {
        TextDocumentPositionParams positionParams = new TextDocumentPositionParams();
        TextDocumentIdentifier identifier = new TextDocumentIdentifier();
        identifier.setUri(Paths.get(balPath).toUri().toString());
        positionParams.setTextDocument(identifier);
        positionParams.setPosition(position);

        DidOpenTextDocumentParams documentParams = new DidOpenTextDocumentParams();
        TextDocumentItem textDocumentItem = new TextDocumentItem();
        textDocumentItem.setUri(identifier.getUri());
        textDocumentItem.setText(balFileContent);
        documentParams.setTextDocument(textDocumentItem);

        serviceEndpoint.notify("textDocument/didOpen", documentParams);
        sleep(200);
        CompletableFuture result = serviceEndpoint.request("textDocument/hover", positionParams);
        sleep(200);
        ResponseMessage jsonrpcResponse = new ResponseMessage();
        try {
            jsonrpcResponse.setId("123");
            jsonrpcResponse.setResult(result.get());
        } catch (InterruptedException e) {
            ResponseError responseError = new ResponseError();
            responseError.setCode(-32002);
            responseError.setMessage("Attempted to retrieve the result of a task/s" +
                    "that was aborted by throwing an exception");
            jsonrpcResponse.setError(responseError);
        } catch (ExecutionException e) {
            ResponseError responseError = new ResponseError();
            responseError.setCode(-32001);
            responseError.setMessage("Current thread was interrupted");
            jsonrpcResponse.setError(responseError);
        }

        return gson.toJson(jsonrpcResponse);
    }

    /**
     * Get the expected value from the expected file.
     *
     * @param expectedFile json file which contains expected content.
     * @return string content read from the json file.
     */
    private String getExpectedValue(String expectedFile) throws IOException {
        String expectedFilePath = SAMPLES_COPY_DIR + File.separator + "test" + File.separator
                + "expected" + File.separator + expectedFile;
        byte[] expectedByte = Files.readAllBytes(Paths.get(expectedFilePath));
        return new String(expectedByte);
    }
}
