package org.ballerinalang.bir.parser;

import org.ballerinalang.bir.model.IrPackage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyPkg extends ParserTest {

    @Test(description = "parse empty package")
    public void testIrParse() throws Exception {
        IrPackage irPackage = parseBirWithSameName();
        Assert.assertEquals(irPackage.pakageId, "");
    }

}
