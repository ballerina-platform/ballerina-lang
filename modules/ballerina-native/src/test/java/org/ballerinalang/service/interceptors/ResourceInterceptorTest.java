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
package org.ballerinalang.service.interceptors;

import org.ballerinalang.runtime.config.BLangConfigurationManager;
import org.ballerinalang.runtime.config.ConfigConstants;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test Cases for testing resource interceptors.
 */
public class ResourceInterceptorTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = EnvironmentInitializer.setupProgramFile("ballerina/bre/service/echoService.bal");
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

    @Test(priority = 0)
    public void testSuccessfulRequestIntercept() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2");
    }

    @Test(priority = 1)
    public void testFailedRequestIntercept() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        CarbonMessage response = Services.invoke(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "invalid login ");
    }

    @Test(priority = 2)
    public void testFailedRequestInterceptInvalidLogin() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "bob");
        cMsg.setHeader("password", "bob");
        CarbonMessage response = Services.invoke(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "invalid login bob");
    }

    @Test(priority = 10)
    public void testLoadFromSystemProperty() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment2.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
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

    @Test(priority = 20)
    public void testGlobalVariable() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment3.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2 res1 (2)");
    }

    @Test(priority = 21)
    public void testGlobalVariableMultipleRequest() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment3.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
        String value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (1) res2 res1 (2)");

        cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        response = Services.invoke(cMsg);
        value = response.getHeader("test");
        Assert.assertEquals(value, "req1 res1 (3) res2 res1 (4)");
    }

    @Test(priority = 30)
    public void testFaultyInterceptor() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment4.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
        String value = ((StringDataSource) response.getMessageDataSource()).getValue();
        Assert.assertEquals(value, "ballerina.lang.errors:NullReferenceError");
        // TODO Fix this.
//        String http_status_code = response.getHeader("HTTP_STATUS_CODE");
//        Assert.assertEquals(http_status_code, "500");
    }

    @Test(priority = 31)
    public void testReturnMessageNull() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment5.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/echo/message", "POST");
        cMsg.setHeader("username", "admin");
        cMsg.setHeader("password", "admin");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNull(response);
    }

    @Test(priority = 40, expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*no exported package found called foo.bar.*")
    public void testInvalidPackageName() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment6.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
    }

    @Test(priority = 41, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*no such file or directory.*foo.bmz.*")
    public void testInvalidArchiveName() {
        BLangConfigurationManager.getInstance().clear();
        BLangRuntimeRegistry.getInstance().clear();
        System.setProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF,
                "src/test/resources/ballerina/bre/conf/deployment7.yaml");
        BLangRuntimeRegistry.getInstance().initialize();
    }
}
