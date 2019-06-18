package org.ballerinalang.messaging.rabbitmq;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * blrh.
 */
public class RabbitMQCompilationTest {
    private static final Path TEST_PATH = Paths.get("src", "test", "resources", "test-src");

    @Test(description = "Successfully compiling Artemis service")
    public void testValidService() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_success.bal").toAbsolutePath()
                .toString());

        Assert.assertEquals(compileResult.toString(), "Compilation Successful");
    }

    @Test(description = "More than expected number of resources in the service")
    public void testMoreResourcesInService() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_more_resources.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Only one resource is allowed in the service", 24, 1);
    }

    @Test(description = "More than expected number of annotations for the service")
    public void testMoreServiceAnnotationsForService() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_more_service_annotation.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "There cannot be more than one Artemis service annotations", 29, 1);
    }

    @Test(description = "One service annotation is mandatory")
    public void testMandatoryServiceAnnotation() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_mandatory_service_annotation.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "There has to be an artemis:ServiceConfig annotation declared for service", 19, 1);
    }

    @Test(description = "Resource returns can only be error or nil")
    public void testResourceReturn() {
        CompileResult compileResult = BCompileUtil.compile(TEST_PATH.resolve("artemis_resource_return.bal")
                .toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0, "Invalid return type: expected error?", 25, 5);
    }

    @Test(description = "Resource without resource parameters")
    public void testNoResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                TEST_PATH.resolve("artemis_no_resource_params.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                        " parameter count = 1)", 25, 5);
    }

    @Test(description = "Resource with multiple resource parameters")
    public void testMultipleResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                TEST_PATH.resolve("artemis_more_resource_params.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(compileResult, 0,
                "Invalid resource signature for onMsg resource: Unexpected parameter count(expected" +
                        " parameter count = 1)", 25, 5);
    }

    @Test(description = "Invalid resource parameters")
    public void testDifferentResourceParams() {
        CompileResult compileResult = BCompileUtil.compile(
                TEST_PATH.resolve("artemis_different_resource_param.bal").toAbsolutePath().toString());

        assertExpectedDiagnosticsLength(compileResult);
        BAssertUtil.validateError(
                compileResult, 0,
                "Invalid resource signature for xyz resource: The first parameter should be an artemis:Message", 25, 5);
    }

    private void assertExpectedDiagnosticsLength(CompileResult compileResult) {
        Assert.assertEquals(compileResult.getDiagnostics().length, 1);
    }

}

