/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.birspec;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import org.ballerinalang.build.kaitai.Bir;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to help with testing BIR model.
 */
class BIRTestUtils {

    private static final String TEST_RESOURCE_ROOT = "test-src/";

    private static BIRCompileResult compile(String testSource) {

        CompileResult result = BCompileUtil.compileAndGetBIR(TEST_RESOURCE_ROOT + testSource);
        Assert.assertEquals(result.getErrorCount(), 0);

        BPackageSymbol packageSymbol = ((BLangPackage) result.getAST()).symbol;

        BIRPackageFile birPackageFile = packageSymbol.birPackageFile;
        Assert.assertNotNull(birPackageFile);

        byte[] birBinaryContent = birPackageFile.pkgBirBinaryContent;
        Assert.assertNotNull(birBinaryContent);

        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(birBinaryContent));
        return new BIRCompileResult(packageSymbol.bir, kaitaiBir);
    }

    static void assertFunctions(String testSource) {

        BIRCompileResult compileResult = compile(testSource);
        BIRNode.BIRPackage expectedBIR = compileResult.getExpectedBIR();
        Bir actualBIR = compileResult.getActualBIR();

        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = actualBIR.constantPool().constantPoolEntries();
        Bir.Module birModule = actualBIR.module();

        List<BIRNode.BIRFunction> expectedFunctions = expectedBIR.functions;
        ArrayList<Bir.Function> actualFunctions = birModule.functions();
        Assert.assertEquals(birModule.functionCount(), expectedFunctions.size());

        for (int i = 0; i < expectedFunctions.size(); i++) {
            Bir.Function actualFunction = actualFunctions.get(i);
            BIRNode.BIRFunction expectedFunction = expectedFunctions.get(i);

            // assert name
            Bir.ConstantPoolEntry constantPoolEntry = constantPoolEntries.get(actualFunction.nameCpIndex());
            assertConstantPoolEntry(constantPoolEntry, expectedFunction.name.value);
        }
    }

    static void assertConstants(String testSource) {

        BIRCompileResult compileResult = compile(testSource);
        BIRNode.BIRPackage expectedBIR = compileResult.getExpectedBIR();
        Bir actualBIR = compileResult.getActualBIR();

        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = actualBIR.constantPool().constantPoolEntries();
        Bir.Module birModule = actualBIR.module();

        List<BIRNode.BIRConstant> expectedConstants = expectedBIR.constants;
        ArrayList<Bir.Constant> actualConstants = birModule.constants();
        Assert.assertEquals(birModule.constCount(), expectedConstants.size());

        for (int i = 0; i < expectedConstants.size(); i++) {
            Bir.Constant actualConstant = actualConstants.get(i);
            BIRNode.BIRConstant expectedConstant = expectedConstants.get(i);

            // assert name
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.nameCpIndex()), expectedConstant.name.value);

            // assert flags
            assertFlags(actualConstant.flags(), expectedConstant.flags);

            // assert type
            assertConstantPoolEntry(constantPoolEntries.get(actualConstant.typeCpIndex()), expectedConstant.type);

            // assert value
            assertConstantValue(actualConstant.constantValue(), expectedConstant.constValue.value, constantPoolEntries);
        }
    }

    static void assertTypeDefs(String testSource) {
        BIRCompileResult compileResult = compile(testSource);
        BIRNode.BIRPackage expectedBIR = compileResult.getExpectedBIR();
        Bir actualBIR = compileResult.getActualBIR();

        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = actualBIR.constantPool().constantPoolEntries();
        Bir.Module birModule = actualBIR.module();

        List<BIRNode.BIRTypeDefinition> expectedTypeDefs = expectedBIR.typeDefs;
        ArrayList<Bir.TypeDefinition> actualTypeDefinitions = birModule.typeDefinitions();

        Assert.assertEquals(actualTypeDefinitions.size(), expectedTypeDefs.size());
    }

    private static void assertConstantPoolEntry(Bir.ConstantPoolEntry constantPoolEntry, Object expectedValue) {

        switch (constantPoolEntry.tag()) {
            case CP_ENTRY_INTEGER:
                Bir.IntCpInfo intCpInfo = (Bir.IntCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(intCpInfo.value(), (long) expectedValue);
                break;
            case CP_ENTRY_STRING:
                Bir.StringCpInfo stringCpInfo = (Bir.StringCpInfo) constantPoolEntry.cpInfo();
                Assert.assertEquals(stringCpInfo.value(), (String) expectedValue);
                break;
            case CP_ENTRY_SHAPE:
                assertType(constantPoolEntry, (BType) expectedValue);
                break;
            case CP_ENTRY_FLOAT:
                Bir.FloatCpInfo floatCpInfo = (Bir.FloatCpInfo) constantPoolEntry.cpInfo();
                double expectedFloatVal = expectedValue instanceof String ? Double.parseDouble((String) expectedValue)
                        : (Double) expectedValue;
                Assert.assertEquals(floatCpInfo.value(), expectedFloatVal);
                break;
            case CP_ENTRY_PACKAGE:
            case CP_ENTRY_BYTE:
            case CP_ENTRY_BOOLEAN:
            default:
                Assert.fail(String.format("Unknown constant pool entry: %s", constantPoolEntry.tag().name()));
        }
    }

    private static void assertType(Bir.ConstantPoolEntry constantPoolEntry, BType expectedValue) {

        Bir.TypeInfo typeInfo = ((Bir.ShapeCpInfo) constantPoolEntry.cpInfo()).shape();
        Assert.assertEquals(typeInfo.typeTag().id(), expectedValue.tag);
        Assert.assertEquals(typeInfo.nameAsStr(), expectedValue.name.getValue());
        assertFlags(typeInfo.typeFlag(), expectedValue.flags);
    }

    private static void assertFlags(int actualFlags, int expectedFlags) {

        Assert.assertEquals(actualFlags, expectedFlags, "Invalid flags");
    }

    private static void assertConstantValue(Bir.ConstantValue actualConstantValue, Object expectedValue,
                                            ArrayList<Bir.ConstantPoolEntry> constantPoolEntries) {

        KaitaiStruct constantValueInfo = actualConstantValue.constantValueInfo();
        Bir.TypeTagEnum typeTag = actualConstantValue.type().shape().typeTag();
        switch (typeTag) {
            case TYPE_TAG_INT:
                Bir.IntConstantInfo intConstantInfo = (Bir.IntConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(intConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_BYTE:
                Bir.ByteConstantInfo byteConstantInfo = (Bir.ByteConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(byteConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_STRING:
                Bir.StringConstantInfo stringConstantInfo = (Bir.StringConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(stringConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_FLOAT:
                Bir.FloatConstantInfo floatConstantInfo = (Bir.FloatConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(floatConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_DECIMAL:
                Bir.DecimalConstantInfo decimalConstantInfo = (Bir.DecimalConstantInfo) constantValueInfo;
                assertConstantPoolEntry(constantPoolEntries.get(decimalConstantInfo.valueCpIndex()), expectedValue);
                break;
            default:
                Assert.fail(String.format("Unknown constant value type: %s", typeTag.name()));
        }
    }

    /**
     * Class to hold both expected and actual compile result of BIR.
     */
    static class BIRCompileResult {

        private BIRNode.BIRPackage expectedBIR;
        private Bir actualBIR;

        BIRCompileResult(BIRNode.BIRPackage expectedBIR, Bir actualBIR) {

            this.expectedBIR = expectedBIR;
            this.actualBIR = actualBIR;
        }

        BIRNode.BIRPackage getExpectedBIR() {

            return expectedBIR;
        }

        Bir getActualBIR() {

            return actualBIR;
        }
    }
}
