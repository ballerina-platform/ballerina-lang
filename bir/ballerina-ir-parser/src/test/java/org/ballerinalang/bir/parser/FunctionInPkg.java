package org.ballerinalang.bir.parser;

import org.ballerinalang.bir.model.IrBasicBlock;
import org.ballerinalang.bir.model.IrFunction;
import org.ballerinalang.bir.model.IrPackage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FunctionInPkg extends ParserTest {

    @Test(description = "parse empty function")
    public void testIrParse() throws Exception {
        IrPackage irPackage = parseBirWithSameName();
        Assert.assertEquals(irPackage.pakageId, "");
        Assert.assertEquals(irPackage.functions.size(), 1);
        IrFunction function = irPackage.functions.get(0);
        Assert.assertEquals(function.name, "my-func");
        Assert.assertEquals(function.bbs.size(), 1);
        IrBasicBlock irBasicBlock = function.bbs.get(0);
        Assert.assertEquals(irBasicBlock.index, 0);
    }

}
