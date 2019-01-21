package org.ballerinalang.test.crypto;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class KeyParsingTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/crypto/keyparsing-test.bal");
    }

    @Test
    public void testParsingPrivateKeyFromBytes() {
        byte [] keyBytes = ("-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIICXAIBAAKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUp\n" +
                "wmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ5\n" +
                "1s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQABAoGAFijko56+qGyN8M0RVyaRAXz++xTqHBLh\n" +
                "3tx4VgMtrQ+WEgCjhoTwo23KMBAuJGSYnRmoBZM3lMfTKevIkAidPExvYCdm5dYq3XToLkkLv5L2\n" +
                "pIIVOFMDG+KESnAFV7l2c+cnzRMW0+b6f8mR1CJzZuxVLL6Q02fvLi55/mbSYxECQQDeAw6fiIQX\n" +
                "GukBI4eMZZt4nscy2o12KyYner3VpoeE+Np2q+Z3pvAMd/aNzQ/W9WaI+NRfcxUJrmfPwIGm63il\n" +
                "AkEAxCL5HQb2bQr4ByorcMWm/hEP2MZzROV73yF41hPsRC9m66KrheO9HPTJuo3/9s5p+sqGxOlF\n" +
                "L0NDt4SkosjgGwJAFklyR1uZ/wPJjj611cdBcztlPdqoxssQGnh85BzCj/u3WqBpE2vjvyyvyI5k\n" +
                "X6zk7S0ljKtt2jny2+00VsBerQJBAJGC1Mg5Oydo5NwD6BiROrPxGo2bpTbu/fhrT8ebHkTz2epl\n" +
                "U9VQQSQzY1oZMVX8i1m5WUTLPz2yLJIBQVdXqhMCQBGoiuSoSjafUhV7i1cEGpb88h5NBYZzWXGZ\n" +
                "37sJ5QsW+sJyoNde3xH8vdXhzU7eT82D6X/scw9RZz+/6rCJ4p0=\n" +
                "-----END RSA PRIVATE KEY-----").getBytes(StandardCharsets.UTF_8);
        BValue[] args = {new BValueArray(keyBytes), new BString("")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromBytes", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get("algorithm").stringValue(), "RSA");
    }

    @Test
    public void testParsingPrivateKeyFromFile() {
        BValue[] args = {new BString("target/test-classes/datafiles/crypto/private.key"), new BString("")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromFile", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get("algorithm").stringValue(), "RSA");
    }

    @Test
    public void testParsingEncryptedPrivateKeyFromFile() {
        BValue[] args = {new BString("target/test-classes/datafiles/crypto/encryptedPrivate.key"),
                new BString("password")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromFile", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get("algorithm").stringValue(), "RSA");
    }

}
