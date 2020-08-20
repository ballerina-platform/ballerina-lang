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

    private static BPackageSymbol compile(String testSource) {

        CompileResult result = BCompileUtil.compileAndGetBIR(TEST_RESOURCE_ROOT + testSource);
        Assert.assertEquals(result.getErrorCount(), 0);

        return ((BLangPackage) result.getAST()).symbol;
    }

    private static byte[] getBIRBinary(BPackageSymbol packageSymbol) {

        BIRPackageFile birPackageFile = packageSymbol.birPackageFile;
        Assert.assertNotNull(birPackageFile);

        byte[] birBinaryContent = birPackageFile.pkgBirBinaryContent;
        Assert.assertNotNull(birBinaryContent);

        return birBinaryContent;
    }

    static void assertFunctions(String testSource) {
        BPackageSymbol packageSymbol = compile(testSource);
        byte[] birBinaryContent = getBIRBinary(packageSymbol);
        BIRNode.BIRPackage originalBir = packageSymbol.bir;
        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(birBinaryContent));
        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = kaitaiBir.constantPool().constantPoolEntries();
        Bir.Module birModule = kaitaiBir.module();

        List<BIRNode.BIRFunction> expectedFunctions = originalBir.functions;
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
        BPackageSymbol packageSymbol = compile(testSource);
        byte[] birBinaryContent = getBIRBinary(packageSymbol);
        BIRNode.BIRPackage originalBir = packageSymbol.bir;
        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(birBinaryContent));
        ArrayList<Bir.ConstantPoolEntry> constantPoolEntries = kaitaiBir.constantPool().constantPoolEntries();
        Bir.Module birModule = kaitaiBir.module();

        List<BIRNode.BIRConstant> expectedConstants = originalBir.constants;
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
            assertConstantValue(actualConstant.typeTag(), constantPoolEntries, actualConstant.constantValue(),
                    expectedConstant.constValue.value);
        }
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
            case CP_ENTRY_PACKAGE:
            case CP_ENTRY_BYTE:
            case CP_ENTRY_FLOAT:
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

    private static void assertConstantValue(Bir.TypeTagEnum typeTag, ArrayList<Bir.ConstantPoolEntry> cPEntries,
                                            KaitaiStruct actualValueInfo, Object expectedValue) {

        switch (typeTag) {
            case TYPE_TAG_INT:
                Bir.IntConstantInfo intConstantInfo = (Bir.IntConstantInfo) actualValueInfo;
                assertConstantPoolEntry(cPEntries.get(intConstantInfo.valueCpIndex()), expectedValue);
                break;
            case TYPE_TAG_BYTE:
                Bir.ByteConstantInfo byteConstantInfo = (Bir.ByteConstantInfo) actualValueInfo;
                assertConstantPoolEntry(cPEntries.get(byteConstantInfo.valueCpIndex()), expectedValue);
                break;
            default:
                Assert.fail(String.format("Unknown constant value type: %s", typeTag.name()));
        }
    }
}
