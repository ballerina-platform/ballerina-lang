package org.ballerinalang.langserver.extensions.document;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;
import org.ballerinalang.langserver.extensions.ballerina.document.TypeSymbolResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test to validate TypeSymbol Retrieval.
 */
public class TypeSymbolRetrieval {

    private Endpoint serviceEndpoint;

    private final Path mainEmptyFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainEmpty.bal");

    private final Path serviceHttpCallModified = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("serviceHttpCallModified.bal");


    @BeforeClass
    public void startLangServer() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "TypeSymbol retrieval with main method")
    public void test1() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(mainEmptyFile);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(2, 0, 2, 0,
                false,
                "INSERT", gson
                .fromJson("{\"STATEMENT\":\"var testVar = \\\"Send\\\" + \\\" Help!\\\";\"}"
                        , JsonObject.class));
        TypeSymbolResponse typeSymbolResponse = LSExtensionTestUtil
                .getTypeSymbol(inputFile.toString(),
                        "testVar",
                        new ASTModification[] {modification1}, this.serviceEndpoint);

        JsonElement typeSymbol = typeSymbolResponse.getTypeData().getAsJsonObject()
                .get("typeSymbol").getAsJsonObject().get("typeKind");
        Assert.assertEquals(typeSymbol.getAsString(), "string");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @Test(description = "TypeSymbol retrieval with resource functions")
    public void test2() throws IOException {
        Path inputFile = LSExtensionTestUtil.createTempFile(serviceHttpCallModified);
        TestUtil.openDocument(serviceEndpoint, inputFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(6, 0, 6, 0, false,
                "INSERT", gson
                .fromJson("{\"STATEMENT\":\"var testVar = \\\"Send\\\" + \\\" Help!\\\";\"}"
                        , JsonObject.class));
        TypeSymbolResponse typeSymbolResponse = LSExtensionTestUtil
                .getTypeSymbol(inputFile.toString(),
                        "testVar",
                        new ASTModification[] {modification1}, this.serviceEndpoint);

        JsonElement typeSymbol = typeSymbolResponse.getTypeData().getAsJsonObject().get("typeSymbol")
                .getAsJsonObject().get("typeKind");
        Assert.assertEquals(typeSymbol.getAsString(), "string");

        TestUtil.closeDocument(this.serviceEndpoint, inputFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}
