package org.ballerinalang.test.bir;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.program.BLangFunctions;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TypeGenTest {

    private ProgramFile programFile;
    private String entryPkgName;

    @BeforeClass
    public void readTestBalx() throws IOException {
        Path filepath = Paths.get("target/bir_test.balx");
        ProgramFileReader programFileReader = new ProgramFileReader();
        programFile = programFileReader.readProgram(filepath);
        Debugger debugger = new Debugger(programFile);
        programFile.setDebugger(debugger);
        entryPkgName = programFile.getEntryPkgName();
    }

    @DataProvider()
    public static Iterator<Object[]> bTypes() {
        CompileResult result = BCompileUtil.compile("test-src/bir/types.bal", CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.toString(result.getDiagnostics()));
        PackageNode ast = result.getAST();
        List<TopLevelNode> topLevelNodes = ((BLangPackage) ast).topLevelNodes;
        List<Object[]> testCases = new ArrayList<>();
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == NodeKind.VARIABLE) {
                testCases.add(new Object[]{((BLangVariable) topLevelNode).type});
            }
        }
        return testCases.iterator();
    }

    @Test(dataProvider = "bTypes")
    public void serializeAndDeserializeBTypeTest(BType type) {
        ConstantPool cp = new ConstantPool();
        byte[] typeBinary = serializeBType(type, cp);
        byte[] cpBinary = cp.serialize();
        BValue[] testParseTypes = executeTestFuncInBalx(typeBinary, cpBinary);
        Assert.assertEquals(testParseTypes[0].stringValue(), type.toString(),
                            "Unable to recover type info from " + Arrays.toString(typeBinary));

    }

    private BValue[] executeTestFuncInBalx(byte[] typeBinary, byte[] cpBinary) {
        BValue[] args = {new BByteArray(cpBinary), new BByteArray(typeBinary)};
        return BLangFunctions.invokeEntrypointCallable(programFile, entryPkgName,
                                                       "testParseType", args);
    }

    private byte[] serializeBType(BType type, ConstantPool cp) {
        ByteBuf buff = Unpooled.buffer();
        BIRTypeWriter birTypeWriter = new BIRTypeWriter(buff, cp);
        type.accept(birTypeWriter);
        return Arrays.copyOfRange(buff.nioBuffer().array(), 0, buff.nioBuffer().limit());
    }

}
