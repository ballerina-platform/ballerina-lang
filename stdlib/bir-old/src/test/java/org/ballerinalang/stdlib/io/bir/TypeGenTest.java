package org.ballerinalang.stdlib.io.bir;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.debugger.Debugger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.writer.BIRTypeWriter;
import org.wso2.ballerinalang.compiler.bir.writer.ConstantPool;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Test cases for bir type gen.
 */
public class TypeGenTest {

    public static final String EXPECTED_PREFIX = "// expected: ";
    public static final int EXPECTED_PREFIX_LEN = EXPECTED_PREFIX.length();

    private static Path resourceDir = Paths.get("src/test/resources").toAbsolutePath();
    private ProgramFile programFile;
    private String entryPkgName;

    @BeforeClass
    public void readTestBalx() throws IOException {
        Path filepath = Paths.get(System.getProperty(BLangConstants.BALLERINA_HOME), "bir_test.balx");
        ProgramFileReader programFileReader = new ProgramFileReader();
        programFile = programFileReader.readProgram(filepath);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        entryPkgName = programFile.getEntryPkgName();
    }

    @DataProvider()
    public static Iterator<Object[]> bTypes() throws IOException {
        String sourceFilePath = "test-src/bir/types.bal";
        List<String> source = Files.readAllLines(resourceDir.resolve(sourceFilePath));
        CompileResult result = BCompileUtil.compile(sourceFilePath, CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.toString(result.getDiagnostics()));
        PackageNode ast = result.getAST();
        List<TopLevelNode> topLevelNodes = ((BLangPackage) ast).topLevelNodes;
        List<Object[]> testCases = new ArrayList<>();
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == NodeKind.VARIABLE) {
                int typeStartLine = ((BLangVariable) topLevelNode).pos.sLine - 1;
                String comment = source.get(typeStartLine - 1);

                Assert.assertTrue(comment.startsWith(EXPECTED_PREFIX),
                                  "missing the expected type string for " + source.get(typeStartLine));

                testCases.add(new Object[]{((BLangVariable) topLevelNode).type,
                                           comment.substring(EXPECTED_PREFIX_LEN)});
            }
        }
        return testCases.iterator();
    }

    @Test(dataProvider = "bTypes")
    public void serializeAndDeserializeBTypeTest(BType type, String source) {
        ConstantPool cp = new ConstantPool();
        byte[] typeBinary = serializeBType(type, cp);
        byte[] cpBinary = cp.serialize();
        try {
            BValue testParseTypes = executeTestFuncInBalx(typeBinary, cpBinary);
            Assert.assertEquals(testParseTypes.stringValue(), source,
                                "Unable to recover type info from " + Arrays.toString(typeBinary));
        } catch (Exception e) {
            throw new AssertionError("Error deserializeing" + Arrays.toString(typeBinary), e);
        }

    }

    private BValue executeTestFuncInBalx(byte[] typeBinary, byte[] cpBinary) {
        BValue[] args = {new BValueArray(cpBinary), new BValueArray(typeBinary)};

        PackageInfo packageInfo = programFile.getPackageInfo(entryPkgName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo("testParseType");
        if (functionInfo == null) {
            throw new RuntimeException("Function 'testParseType' is not defined");
        }

        return BLangProgramRunner.runProgram(programFile, functionInfo, args);
    }

    private byte[] serializeBType(BType type, ConstantPool cp) {
        ByteBuf buff = Unpooled.buffer();
        BIRTypeWriter birTypeWriter = new BIRTypeWriter(buff, cp);
        birTypeWriter.visitType(type);
        return Arrays.copyOfRange(buff.nioBuffer().array(), 0, buff.nioBuffer().limit());
    }

}
