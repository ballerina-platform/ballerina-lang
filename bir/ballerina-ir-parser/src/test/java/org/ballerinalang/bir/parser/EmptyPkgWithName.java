package org.ballerinalang.bir.parser;

import org.ballerinalang.bir.model.IrPackage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EmptyPkgWithName extends ParserTest {

    @Test(description = "parse empty package")
    public void testIrParse() throws Exception {
        IrPackage irPackage = parseBirWithSameName();
        Assert.assertEquals(irPackage.pakageId, "my_org/my-pkg:1.2.0");
    }

}
