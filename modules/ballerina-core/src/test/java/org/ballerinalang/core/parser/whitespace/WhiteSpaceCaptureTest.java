package org.ballerinalang.core.parser.whitespace;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
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
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_START), "\n\n");
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
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.IMPORT_DEC_END_TO_NEXT_TOKEN), "\n\n\n  ");
    }

    @Test(description = "Test captured whitespace regions of package declaration node")
    public void testWhiteSpaceCaptureInPackageDeclaration() {
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_KEYWORD_TO_PKG_NAME_START), "  ");
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_NAME_END_TO_SEMICOLON), "   ");
        Assert.assertEquals(bFile.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.BFILE_PKG_DEC_END_TO_NEXT_TOKEN), "\n\n\n");
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
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_BODY_START_TO_FIRST_CHILD), "\n\n      ");
        Assert.assertEquals(resource.getWhiteSpaceDescriptor()
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.RESOURCE_DEF_END_TO_NEXT_TOKEN), "\n");
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
                .getWhiteSpaceRegions().get(WhiteSpaceRegions.ANNOTATION_ATCHMNT_END_TO_NEXT_TOKEN), "\n\n  ");
    }
}
