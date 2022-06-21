package org.ballerinalang.test.services;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClientServiceDeclTest {
    CompileResult negative;

    @BeforeClass
    public void setup() {
        negative = BCompileUtil.
                compile("test-src/services/client_service_test_negative.bal");
    }
    @Test()
    public void testServiceDecl() {
        int index = 0;
        BAssertUtil.validateError(negative, index++,
                "incompatible types: expected 'string', found 'int'", 12, 21);
        BAssertUtil.validateError(negative, index++,
                "undefined resource path", 13, 23);
        BAssertUtil.validateError(negative, index++,
                "undefined resource path", 14, 23);
    }
}
