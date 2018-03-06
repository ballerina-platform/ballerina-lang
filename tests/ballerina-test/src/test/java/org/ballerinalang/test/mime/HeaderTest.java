package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.Constants.ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

public class HeaderTest {

    private CompileResult compileResult;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String entityStruct = ENTITY;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/header-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test whether the case is ignored when dealing with http headers")
    public void testToStringOnMediaType() {
        BString headerName = new BString("Content-Type");
        BString headerValue = new BString("application/json");
        BString headerNameToBeUsedForRetrieval = new BString("CONTENT-TYPE");
        BValue[] args = {headerName, headerValue, headerNameToBeUsedForRetrieval};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/json");
    }
}
