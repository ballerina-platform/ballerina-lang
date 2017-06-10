package org.ballerinalang.core.parser.whitespace;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeValue;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParserErrorStrategy;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.ballerinalang.util.parser.antlr4.WhiteSpaceRegions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Test whitespace capturing in verbose mode.
 */
public class WhiteSpaceCaptureTest {

    protected BallerinaFile bFile;

    @BeforeClass
    public void setup() throws IOException {
        File file = new File(WhiteSpaceCaptureTest.class
                .getClassLoader().getResource("samples/parser/whitespace/whitespace.bal")
                .getFile());

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(file));
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaParserErrorStrategy errorStrategy = new BallerinaParserErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(true, ballerinaToken,
                bLangModelBuilder, file.toPath());
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        bFile = bLangModelBuilder.build();
    }

    @Test(description = "Test captured whitespace region at file start")
    public void testWhiteSpaceCaptureInBFileStart() {
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_START),
                System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of import declarations")
    public void testWhiteSpaceCaptureInImportDec() {
        ImportPackage importPackage = bFile.getImportPackages()[0];
        Assert.assertNotNull(importPackage);
        Assert.assertEquals(importPackage.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_IMPORT_KEYWORD_TO_PKG_NAME_START), "   ");
        Assert.assertEquals(importPackage.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_PKG_NAME_END_TO_NEXT), "    ");
        Assert.assertEquals(importPackage.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_AS_KEYWORD_TO_IDENTIFIER), "  ");
        Assert.assertEquals(importPackage.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_IDENTIFIER_TO_IMPORT_DEC_END), "   ");
        Assert.assertEquals(importPackage.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + "  ");
    }

    @Test(description = "Test captured whitespace regions of package declaration node")
    public void testWhiteSpaceCaptureInPackageDeclaration() {
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_KEYWORD_TO_PKG_NAME_START), "  ");
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_NAME_END_TO_SEMICOLON), "   ");
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_DEC_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of resource definition node")
    public void testWhiteSpaceCaptureInResourceDefinition() {
        Resource resource = ((Service) bFile.getCompilationUnits()[0]).getResources()[0];
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_RESOURCE_KEYWORD_TO_IDENTIFIER), "   ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_IDENTIFIER_TO_PARAM_LIST_START), "    ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_PARAM_LIST_START_TO_FIRST_PARAM), "     ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_PARAM_LIST_END_TO_BODY_START), "  ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_BODY_START_TO_FIRST_CHILD),
                System.lineSeparator() + System.lineSeparator() + "      ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_END_TO_NEXT_TOKEN),
                System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of annotation attachment node")
    public void testWhiteSpaceCaptureInAnnotationAttachment() {
        AnnotationAttachment annotationAttachment = ((Service) bFile.getCompilationUnits()[0])
                                                            .getResources()[0].getAnnotations()[1];
        Assert.assertEquals(annotationAttachment.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATCHMNT_AT_KEYWORD_TO_IDENTIFIER), "  ");
        Assert.assertEquals(annotationAttachment.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATCHMNT_IDENTIFIER_TO_ATTRIB_LIST_START),
                        "   ");
        Assert.assertEquals(annotationAttachment.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATCHMNT_ATTRIB_LIST_START_TO_FIRST_ATTRIB),
                        "    ");
        Assert.assertEquals(annotationAttachment.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + "  ");
    }


    @Test(description = "Test captured whitespace regions of annotation attribute node")
    public void testWhiteSpaceCaptureInAnnotationAttribute() {
        AnnotationAttachment annotationAttachment = ((Service) bFile.getCompilationUnits()[0])
                .getResources()[0].getAnnotations()[1];
        AnnotationAttributeValue attribute = annotationAttachment.getAttribute("value2");
        Assert.assertNotNull(attribute);
        Assert.assertEquals(attribute.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_KEY_START_TO_LAST_TOKEN), " ");
        Assert.assertEquals(attribute.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_KEY_TO_COLON), "   ");
        Assert.assertEquals(attribute.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_COLON_TO_VALUE_START), "    ");
        Assert.assertEquals(attribute.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_START_TO_LAST_TOKEN), "    ");
        Assert.assertEquals(attribute.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_END_TO_NEXT_TOKEN), "     ");
        AnnotationAttributeValue[] valueArray = attribute.getValueArray();
        Assert.assertNotNull(valueArray);
        AnnotationAttributeValue arrayValue = valueArray[0];
        Assert.assertEquals(arrayValue.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_START_TO_LAST_TOKEN), "     ");
        Assert.assertEquals(arrayValue.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATTRIB_VALUE_END_TO_NEXT_TOKEN), "");

    }

    @Test(description = "Test captured whitespace regions of function definition node")
    public void testWhiteSpaceCaptureInFunctionDef() {
        Function function = (Function) bFile.getCompilationUnits()[2];
        Assert.assertEquals(function.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.FUNCTION_DEF_FUNCTION_KEYWORD_TO_IDENTIFIER_START), " ");
        Assert.assertEquals(function.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.FUNCTION_DEF_IDENTIFIER_TO_PARAM_LIST_START), " ");
        Assert.assertEquals(function.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.FUNCTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START),
                System.lineSeparator() + "                            ");
        Assert.assertEquals(function.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.FUNCTION_DEF_BODY_START_TO_LAST_TOKEN), "  ");
        Assert.assertEquals(function.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.FUNCTION_DEF_BODY_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of connector definition node")
    public void testWhiteSpaceCaptureInConnectorDef() {
        BallerinaConnectorDef connector = (BallerinaConnectorDef) bFile.getCompilationUnits()[4];
        Assert.assertEquals(connector.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONNECTOR_DEF_CONNECTOR_KEYWORD_TO_IDENTIFIER), " ");
        Assert.assertEquals(connector.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONNECTOR_DEF_IDENTIFIER_TO_PARAM_LIST_START), "  ");
        Assert.assertEquals(connector.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONNECTOR_DEF_PARAM_LIST_END_TO_BODY_START), "   ");
        Assert.assertEquals(connector.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONNECTOR_DEF_BODY_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of action definition node")
    public void testWhiteSpaceCaptureInActionDef() {
        BallerinaConnectorDef connector = (BallerinaConnectorDef) bFile.getCompilationUnits()[4];
        Map<Integer, String> ws = connector.getActions()[0].getWhiteSpaceDescriptor()
                                                        .getWhiteSpaceRegions();
        Assert.assertEquals(ws.get(WhiteSpaceRegions.ACTION_DEF_ACTION_KEYWORD_TO_IDENTIFIER_START), " ");
        Assert.assertEquals(ws.get(WhiteSpaceRegions.ACTION_DEF_IDENTIFIER_TO_PARAM_LIST_START), "  ");
        Assert.assertEquals(ws.get(WhiteSpaceRegions.ACTION_DEF_PARAM_LIST_END_TO_RETURN_PARAM_START), "    ");
        Assert.assertEquals(ws.get(WhiteSpaceRegions.ACTION_DEF_BODY_START_TO_LAST_TOKEN), "   ");
        Assert.assertEquals(ws.get(WhiteSpaceRegions.ACTION_DEF_BODY_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of struct definition node")
    public void testWhiteSpaceCaptureInStructDef() {
        StructDef structDef = (StructDef) bFile.getCompilationUnits()[6];
        Assert.assertNotNull(structDef);
        Assert.assertEquals(structDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.STRUCT_DEF_STRUCT_KEYWORD_TO_IDENTIFIER), "  ");
        Assert.assertEquals(structDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.STRUCT_DEF_IDENTIFIER_TO_BODY_START), "   ");
        Assert.assertEquals(structDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.STRUCT_DEF_BODY_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator());
    }


    @Test(description = "Test captured whitespace regions of type mapper definition node")
    public void testWhiteSpaceCaptureInTypeMapperDef() {
        BTypeMapper typeMapper = (BTypeMapper) bFile.getCompilationUnits()[8];
        Assert.assertNotNull(typeMapper);
        Assert.assertEquals(typeMapper.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.TYPE_MAP_DEF_TYPE_MAPPER_KEYWORD_TO_IDENTIFIER), " ");
        Assert.assertEquals(typeMapper.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.TYPE_MAP_DEF_IDENTIFIER_PARAM_WRAPPER_START), "  ");
        Assert.assertEquals(typeMapper.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions()
                .get(WhiteSpaceRegions.TYPE_MAP_DEF_PARAM_WRAPPER_END_TO_RETURN_TYPE_WRAPPER_START), "    ");
        Assert.assertEquals(typeMapper.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.TYPE_MAP_DEF_RETURN_TYPE_WRAPPER_TO_BODY_START), "     ");
        Assert.assertEquals(typeMapper.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.TYPE_MAP_DEF_BODY_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator());
    }


    @Test(description = "Test captured whitespace regions of constant definition node")
    public void testWhiteSpaceCaptureInConstantDef() {
        ConstDef constDef = (ConstDef) bFile.getCompilationUnits()[9];
        Assert.assertNotNull(constDef);
        Assert.assertEquals(constDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONST_DEF_CONST_KEYWORD_TO_VAL_TYPE), " ");
        Assert.assertEquals(constDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONST_DEF_VAL_TYPE_TO_IDENTIFIER), "  ");
        Assert.assertEquals(constDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions()
                .get(WhiteSpaceRegions.CONST_DEF_IDENTIFIER_TO_EQUAL_OPERATOR), "   ");
        Assert.assertEquals(constDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONST_DEF_EQUAL_OPERATOR_TO_LITERAL_START), "    ");
        Assert.assertEquals(constDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.CONST_DEF_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of global variable definition node")
    public void testWhiteSpaceCaptureInGlobalVariableDef() {
        GlobalVariableDef varDef = (GlobalVariableDef) bFile.getCompilationUnits()[12];
        Assert.assertNotNull(varDef);
        Assert.assertEquals(varDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.GLOBAL_VAR_DEF_TYPE_NAME_TO_IDENTIFIER), " ");
        Assert.assertEquals(varDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.GLOBAL_VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR), "  ");
        Assert.assertEquals(varDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions()
                .get(WhiteSpaceRegions.GLOBAL_VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START), "   ");
        Assert.assertEquals(varDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.GLOBAL_VAR_DEF_END_TO_LAST_TOKEN), "    ");
        Assert.assertEquals(varDef.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.GLOBAL_VAR_DEF_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
    }

    @Test(description = "Test captured whitespace regions of variable definition node")
    public void testWhiteSpaceCaptureInVariableDef() {
        Service service = (Service) bFile.getCompilationUnits()[0];
        WhiteSpaceDescriptor whiteSpaceDescriptor = service.getVariableDefStmts()[0].getVariableDef()
                .getWhiteSpaceDescriptor();
        Map<Integer, String> wsRegions = whiteSpaceDescriptor
                .getWhiteSpaceRegions();
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.VAR_DEF_TYPE_NAME_TO_IDENTIFIER), " ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.VAR_DEF_IDENTIFIER_TO_EQUAL_OPERATOR), "  ");
        Assert.assertEquals(wsRegions
                .get(WhiteSpaceRegions.VAR_DEF_EQUAL_OPERATOR_TO_EXPRESSION_START), "   ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.VAR_DEF_END_TO_LAST_TOKEN), "    ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.VAR_DEF_END_TO_NEXT_TOKEN), System.lineSeparator()
                + System.lineSeparator() + System.lineSeparator() + System.lineSeparator() + "  ");
    }

    @Test(description = "Test captured whitespace regions of a parameter definition node")
    public void testWhiteSpaceCaptureInParamDef() {
        Resource resource = ((Service) bFile.getCompilationUnits()[0]).getResources()[0];
        ParameterDef messageParamDef = resource.getParameterDefs()[0];
        WhiteSpaceDescriptor whiteSpaceDescriptor = messageParamDef.getWhiteSpaceDescriptor();
        Map<Integer, String> wsRegions = whiteSpaceDescriptor.getWhiteSpaceRegions();

        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.PARAM_DEF_TYPENAME_START_TO_LAST_TOKEN), "     ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.PARAM_DEF_TYPENAME_TO_IDENTIFIER), "    ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.PARAM_DEF_END_TO_NEXT_TOKEN), "   ");
    }

    @Test(description = "Test captured whitespace regions of a worker declaration node")
    public void testWhiteSpaceCaptureInWorkerDeclaration() {
        Resource resource = ((Service) bFile.getCompilationUnits()[0]).getResources()[0];
        Worker worker = resource.getWorkers()[0];
        WhiteSpaceDescriptor whiteSpaceDescriptor = worker.getWhiteSpaceDescriptor();
        Map<Integer, String> wsRegions = whiteSpaceDescriptor.getWhiteSpaceRegions();

        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.WORKER_DEC_PRECEDING_WHITESPACE),
                System.lineSeparator() + System.lineSeparator() + "    ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.WORKER_DEC_WORKER_KEYWORD_TO_IDENTIFIER), "  ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.WORKER_DEC_IDENTIFIER_TO_BODY_START), "   ");
        Assert.assertEquals(wsRegions.get(WhiteSpaceRegions.WORKER_DEC_END_TO_NEXT_TOKEN),
                System.lineSeparator() + System.lineSeparator() + "  ");
    }
}
