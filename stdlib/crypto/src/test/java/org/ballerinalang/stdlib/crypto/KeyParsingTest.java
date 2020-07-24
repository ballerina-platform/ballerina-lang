/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.stdlib.crypto;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for ballerina.crypto native functions relevant to private/public key parsing.
 *
 * @since 0.990.3
 */
public class KeyParsingTest {

    private CompileResult compileResult;
    private String resourceRoot;
    private Path sourceRoot;
    private Path confRoot;

    @BeforeClass
    public void setup() {
        resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        sourceRoot = Paths.get(resourceRoot, "test-src");
        confRoot = Paths.get(resourceRoot, "datafiles");
        compileResult = BCompileUtil.compileOffline(sourceRoot.resolve("keyparsing-test.bal").toString());
    }

    @Test(description = "Check parsing encrypted private-key from a p12 file.")
    public void testParsingEncryptedPrivateKeyFromP12() {
        BValue[] args = {new BString(confRoot.resolve("testKeystore.p12").toString()), new BString("ballerina"),
                new BString("ballerina"), new BString("ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromP12", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD).stringValue(),
                "RSA");
    }

    @Test(description = "Check parsing public-key from a p12 file.")
    public void testParsingPublicKeyFromP12() {
        BValue[] args = {new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPublicKeyFromP12", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD).stringValue(),
                            "RSA");
        Assert.assertTrue(((BMap) returnValues[0]).get(Constants.PUBLIC_KEY_RECORD_CERTIFICATE_FIELD) instanceof BMap);
        BMap<String, BValue> certificate = (BMap<String, BValue>) ((BMap) returnValues[0]).get("certificate");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SERIAL_FIELD).stringValue(),
                            "2097012467");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_ISSUER_FIELD).stringValue(),
                            "CN=localhost,OU=WSO2,O=WSO2,L=Mountain View,ST=CA,C=US");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SUBJECT_FIELD).stringValue(),
                            "CN=localhost,OU=WSO2,O=WSO2,L=Mountain View,ST=CA,C=US");
        Assert.assertTrue(certificate.get(Constants.CERTIFICATE_RECORD_NOT_BEFORE_FIELD) instanceof BMap);
        Assert.assertTrue(certificate.get(Constants.CERTIFICATE_RECORD_NOT_AFTER_FIELD) instanceof BMap);
        Assert.assertTrue(
                certificate.get(Constants.CERTIFICATE_RECORD_SIGNATURE_FIELD) instanceof BValueArray);
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SIGNATURE_ALG_FIELD).stringValue(),
                            "SHA256withRSA");
    }

    @Test(description = "Check attempting to read a private key from a non-existing p12 file.",
            expectedExceptions = BLangRuntimeException.class)
    public void testParsingEncryptedPrivateKeyFromInvalidLocation() {
        BValue[] args = {new BString(confRoot.resolve("testKeystore.p12.invalid").toString()), new BString("ballerina"),
                new BString("ballerina"), new BString("ballerina")};
        BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromP12", args);
    }

    @Test(description = "Check attempting to read a public key from a non-existing p12 file.",
            expectedExceptions = BLangRuntimeException.class)
    public void testParsingPublicKeyFromInvalidLocation() {
        BValue[] args = {new BString(confRoot.resolve("testKeystore.p12.invalid").toString()),
                new BString("ballerina"), new BString("ballerina")};
        BRunUtil.invoke(compileResult, "testParsingPublicKeyFromP12", args);
    }

    @Test(description = "Check parsing public-key from a JWK parameters.")
    public void testParsingPublicKeyFromJwk() {
        String modulus = "luZFdW1ynitztkWLC6xKegbRWxky-5P0p4ShYEOkHs30QI2VCuR6Qo4Bz5rTgLBrky03W1GAVrZxuvKRGj9V9-" +
                "PmjdGtau4CTXu9pLLcqnruaczoSdvBYA3lS9a7zgFU0-s6kMl2EhB-rk7gXluEep7lIOenzfl2f6IoTKa2fVgVd3YKiSGsy" +
                "L4tztS70vmmX121qm0sTJdKWP4HxXyqK9neolXI9fYyHOYILVNZ69z_73OOVhkh_mvTmWZLM7GM6sApmyLX6OXUp8z0pkY-v" +
                "T_9-zRxxQs7GurC4_C1nK3rI_0ySUgGEafO1atNjYmlFN-M3tZX6nEcA6g94IavyQ";
        String exponent = "AQAB";
        BValue[] args = {new BString(modulus), new BString(exponent)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPublicKeyFromJwk", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD).stringValue(),
                            "RSA");
    }
}
