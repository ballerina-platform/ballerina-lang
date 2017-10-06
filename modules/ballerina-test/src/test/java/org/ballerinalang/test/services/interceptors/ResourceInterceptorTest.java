/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.services.interceptors;

import org.ballerinalang.runtime.config.BLangConfigurationManager;
import org.ballerinalang.runtime.config.ConfigConstants;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.test.services.testutils.EnvironmentInitializer;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.file.NoSuchFileException;

/**
 * Test Cases for testing resource interceptors.
 */
public class ResourceInterceptorTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = EnvironmentInitializer
                .setupProgramFile("test-src/services/echoService.bal");
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_HOME, "src/test/resources/ballerina");
        BLangRuntimeRegistry.getInstance().initialize();
    }

    @AfterClass
    public void cleanUp() {
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_HOME, "");
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF, "");
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
    }

    @Test(priority = 0, enabled = false)
    public void testSuccessfulRequestIntercept() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2");
    }

    @Test(priority = 1, enabled = false)
    public void testFailedRequestIntercept() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "invalid login ");
    }

    @Test(priority = 2, enabled = false)
    public void testFailedRequestInterceptInvalidLogin() {
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "bob");
        cMsg.setHeader("password", "bob");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "invalid login bob");
    }

    @Test(priority = 10, enabled = false)
    public void testLoadFromSystemProperty() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment2.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1)");
    }

    //    @Test(priority = 11)
    public void testLoadFromSystemPropertyNegative() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/foo.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        Assert.assertFalse(BLangRuntimeRegistry.getInstance().isInterceptionEnabled("http"));
    }

    @Test(priority = 20, enabled = false)
    public void testGlobalVariable() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment3.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2 res1 (2)");
    }

    @Test(priority = 21, enabled = false)
    public void testGlobalVariableMultipleRequest() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment3.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2 res1 (2)");

        cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        response = Services.invokeNew(cMsg);
        value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (3) res2 res1 (4)");
    }

    @Test(priority = 30, enabled = false)
    public void testFaultyInterceptor() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment4.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "ballerina.lang.errors:NullReferenceError");
        // TODO Fix this.
//        String http_status_code = response.getHeader("HTTP_STATUS_CODE");
//        Assert.assertEquals(http_status_code, "500");
    }

    @Test(priority = 31, enabled = false)
    public void testReturnMessageNull() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment5.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        HTTPCarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        HTTPCarbonMessage response = Services.invokeNew(cMsg);
        Assert.assertNull(response);
    }

    @Test(priority = 40, expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*no exported package found called foo.bar.*", enabled = false)
    public void testInvalidPackageName() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment6.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
    }

    @Test(priority = 41, enabled = false)
    public void testInvalidArchiveName() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment7.yaml");
        try {
            BLangRuntimeRegistry.getInstance().initialize();
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchFileException);
            Assert.assertEquals(e.getMessage(), "ballerina: error reading program file " +
                    "'src/test/resources/ballerina/bre/interceptors/foo.balx'");
            return;
        }
        Assert.fail("Test should stop before this.");
    }
}
