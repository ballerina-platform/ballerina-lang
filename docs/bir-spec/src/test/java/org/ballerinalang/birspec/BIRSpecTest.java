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
import org.ballerinalang.build.kaitai.Bir;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test cases to verify BIR binary against the spec.
 *
 */
public class BIRSpecTest {

    @Test(description = "A simple test to verify BIR binary of a ballerina source with an empty method")
    public void basicTest() {

        CompileResult result = BCompileUtil.compileAndGetBIR("test-src/basic/function.bal");
        Assert.assertEquals(result.getErrorCount(), 0);

        BPackageSymbol packageSymbol = ((BLangPackage) result.getAST()).symbol;
        BIRPackageFile birPackageFile = packageSymbol.birPackageFile;
        Assert.assertNotNull(birPackageFile);

        byte[] birBinaryContent = birPackageFile.pkgBirBinaryContent;
        Assert.assertNotNull(birBinaryContent);

        BIRNode.BIRPackage originalBir = packageSymbol.bir;
        Bir kaitaiBir = new Bir(new ByteBufferKaitaiStream(birBinaryContent));

        Bir.ConstantPoolSet birConstantPool = kaitaiBir.constantPool();
        Bir.Module birModule = kaitaiBir.module();

        Assert.assertTrue(birConstantPool.constantPoolCount() > 0);
        Assert.assertEquals(birModule.functionCount(), originalBir.functions.size());

        Map<String, BIRNode.BIRFunction> collect = originalBir.functions.stream()
                .collect(Collectors.toMap(func -> func.name.value, func -> func));

        for (Bir.Function func : birModule.functions()) {
            Bir.ConstantPoolEntry constantPoolEntry = birConstantPool.constantPoolEntries().get(func.nameCpIndex());
            Assert.assertTrue(constantPoolEntry.cpInfo() instanceof Bir.StringCpInfo);
            Bir.StringCpInfo functionNameCPInfo = (Bir.StringCpInfo) constantPoolEntry.cpInfo();
            Assert.assertTrue(collect.containsKey(functionNameCPInfo.value()));
        }
    }
}
