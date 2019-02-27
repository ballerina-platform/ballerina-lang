package org.ballerinalang.stdlib.path;

import org.ballerinalang.model.values.BError;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.InvalidPathException;

import static org.ballerinalang.stdlib.path.Constants.ERROR_REASON_PREFIX;
import static org.ballerinalang.stdlib.path.Utils.UNKNOWN_MESSAGE;
import static org.ballerinalang.stdlib.path.Utils.UNKNOWN_REASON;

public class UtilsTest {

    @Test
    public void testGetPathError() {
        InvalidPathException exp = new InvalidPathException("/User/ballerina/path\\test", "Invalid path format");
        String reason = "INVALID_PATH";

        // Get Path error with reason and throwable.
        BError error1 = Utils.getPathError(reason, exp);
        Assert.assertEquals(error1.reason, ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error1.details.stringValue(), "{\"message\":\"Invalid path format: " +
                "/User/ballerina/path\\test\"}");

        // Get Path error without reason.
        BError error2 = Utils.getPathError(null, exp);
        Assert.assertEquals(error2.reason, ERROR_REASON_PREFIX + UNKNOWN_REASON);
        Assert.assertEquals(error2.details.stringValue(), "{\"message\":\"Invalid path format: " +
                "/User/ballerina/path\\test\"}");

        // Get Path error without throwable.
        BError error3 = Utils.getPathError(reason, null);
        Assert.assertEquals(error3.reason, ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error3.details.stringValue(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");

        // Get Path error without both reason and throwable.
        BError error4 = Utils.getPathError(null, null);
        Assert.assertEquals(error4.reason, ERROR_REASON_PREFIX + UNKNOWN_REASON);
        Assert.assertEquals(error4.details.stringValue(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");

        // Get Path error without throwable message.
        Exception exp2 = new Exception();
        BError error5 = Utils.getPathError(reason, exp2);
        Assert.assertEquals(error5.reason, ERROR_REASON_PREFIX + reason);
        Assert.assertEquals(error5.details.stringValue(), "{\"message\":\"" + UNKNOWN_MESSAGE + "\"}");
    }
}
