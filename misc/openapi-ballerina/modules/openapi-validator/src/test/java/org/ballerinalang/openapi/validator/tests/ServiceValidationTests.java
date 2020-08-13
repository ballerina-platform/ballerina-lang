package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.validator.Filters;
import org.ballerinalang.openapi.validator.OpenApiValidatorException;
import org.ballerinalang.openapi.validator.ServiceValidator;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for serviceValidation.
 */
public class ServiceValidationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/project-based-tests/src/resourceValidation/")
            .toAbsolutePath();
    private OpenAPI api;
    private BLangPackage bLangPackage;
    private BLangService extractBLangservice;
    private List<String> tag = new ArrayList<>();
    private List<String> operation = new ArrayList<>();
    private List<String> excludeTag = new ArrayList<>();
    private List<String> excludeOperation = new ArrayList<>();
    private Diagnostic.Kind kind;
    private DiagnosticLog dLog;
    private Filters filters;

    @Test(description = "test for undocumented Path in contract")
    public void testUndocumentedPath() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstore.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/valid/petstore.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/valid/petstore.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }
    @Test(description = "test for undocumented Method in contract")
    public void testUndocumentedMethod() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreMethod.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/valid/petstoreMethod.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/valid/petstoreMethod.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);

    }

    @Test(description = "test for undocumented TypeMisMatch in contract")
    public void testParameterTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/valid/petstoreParameterTM.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/valid/petstoreParameterTM.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/valid/petstoreParameterTM.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }

    @Test(description = "test for undocumented TypeMisMatch in contract")
    public void testRecordTypeMismatch() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordType.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/petstoreRecordType.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/petstoreRecordType.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }
    @Test(description = "test for undocumented record field  in contract")
    public void testRecordFieldMiss() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstoreRecordFieldMiss.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/petstoreRecordFieldMiss.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/petstoreRecordFieldMiss.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }

    @Test(description = "test for undocumented path parameter  in contract")
    public void testPathParameter() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/petstorePathParameter.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/petstorePathParameter.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/petstorePathParameter.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }

    @Test(description = "test for undocumented field oneOf type record in contract")
    public void testOneofscenario_01() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/oneOf.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/oneOf.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }

    @Test(description = "test for undocumented field oneOf type record in contract")
    public void testOneofscenario_02() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf-scenario02.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/oneOf-scenario02.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/oneOf-scenario02.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }

    @Test(description = "test for undocumented field oneOf type record in contract")
    public void testOneofscenario_03() throws OpenApiValidatorException, UnsupportedEncodingException {
        Path contractPath = RES_DIR.resolve("swagger/invalid/oneOf-scenario03.yaml");
        api = ServiceValidator.parseOpenAPIFile(contractPath.toString());
        bLangPackage = ValidatorTest.getBlangPackage("serviceValidator/ballerina/invalid/oneOf-scenario03.bal");
        extractBLangservice = ValidatorTest.getServiceNode(bLangPackage);
        kind = Diagnostic.Kind.ERROR;
        dLog = ValidatorTest.getDiagnostic("serviceValidator/ballerina/invalid/oneOf-scenario03.bal");
        filters = new Filters(tag, excludeTag, operation, excludeOperation, kind);
        ServiceValidator.validateResource(api, extractBLangservice, filters, kind, dLog);
    }
}
