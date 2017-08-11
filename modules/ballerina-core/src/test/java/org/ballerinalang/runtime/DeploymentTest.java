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
package org.ballerinalang.runtime;

//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.ballerinalang.core.interpreter.SymScope;
//import org.ballerinalang.model.Application;
//import org.ballerinalang.model.Package;
//import org.ballerinalang.model.SymbolName;
//import org.ballerinalang.runtime.deployer.BalDeployer;
//import org.ballerinalang.natives.BuiltInNativeConstructLoader;
//import org.ballerinalang.runtime.internal.GlobalScopeHolder;
//import org.ballerinalang.runtime.internal.ServiceContextHolder;
//import org.ballerinalang.runtime.registry.ApplicationRegistry;
//
//import java.io.File;
//import java.net.URL;

/**
 * Test Cases for Ballerina Deployer.
 */
public class DeploymentTest {

//    Application application;
//
//    @BeforeClass
//    public void setup() {
//        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
//        if (symScope.lookup(new SymbolName("ballerina.model.system:print_string")) == null) {
//            BuiltInNativeConstructLoader.loadConstructs();
//        }
//        ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
//    }
//
//    @AfterClass
//    public void tearDown() {
//        cleanup(null);
//    }
//
//    private void cleanup(Application application) {
//        ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.ERROR);
//        ServiceContextHolder.getInstance().setBallerinaFileToExecute(null);
//        if (application != null) {
//            // Force unregister.
//            ApplicationRegistry.getInstance().unregisterApplication(application);
//        }
//    }
//
//    private File getFileToTest(String pathToFile) {
//        URL fileResource = DeploymentTest.class.getClassLoader().getResource(pathToFile);
//        return new File(fileResource.getFile());
//    }
//
//    @Test(description = "Validate public and private function deployment.")
//    public void testFunctionDeployment() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
//            final String applicationName = "testDeploymentFunction.bal";
//            File file = getFileToTest("samples/deployment/testDeploymentFunction.bal");
//
//            BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.SERVER,
//                    "Deployment mode set to error.");
//
//            application = ApplicationRegistry.getInstance().getApplication(applicationName);
//            Package aPackage = application.getPackage("samples.deployment");
//            Assert.assertTrue(aPackage.getPublicFunctions().containsKey("testDeployment_int_int"),
//                    "Public function deployment failed.");
//            Assert.assertTrue(aPackage.getPrivateFunctions().containsKey("testDeployment_int_string"),
//                    "Private function deployment failed.");
//
//            // TODO : Fix this once M3 is out.
//            new BalDeployer().undeployBalFile(applicationName);
//            Assert.assertNull(ApplicationRegistry.getInstance().getApplication(applicationName),
//                    "Application still exits.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate Service deployment.")
//    public void testServiceDeployment() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
//            final String applicationName = "testDeploymentService.bal";
//            File file = getFileToTest("samples/deployment/testDeploymentService.bal");
//
//            int serviceDeployed = BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(serviceDeployed, 1, "Deployed services count didn't match.");
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.SERVER,
//                    "Deployment mode set to error.");
//
//            application = ApplicationRegistry.getInstance().getApplication(applicationName);
//            Package aPackage = application.getPackage("samples.deployment");
//            Assert.assertNotNull(aPackage, "Service deployment failed.");
//
//            // TODO : Fix this once M3 is out.
//            new BalDeployer().undeployBalFile(applicationName);
//            Assert.assertNull(ApplicationRegistry.getInstance().getApplication(applicationName),
//                    "Application still exits.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate Link errors while doing the service deployment.")
//    public void testServiceDeploymentLinkError() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
//            File file = getFileToTest("samples/deployment/testInvalidFunction.bal");
//
//            BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.SERVER,
//                    "Deployment mode set to error.");
//
//            application = ApplicationRegistry.getInstance().getApplication("testInvalidFunction.bal");
//            Assert.assertNull(application, "Application is created, But it shouldn't.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate non .bal deployment with Service Deployment.")
//    public void testServiceDeploymentInvalidFile() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.SERVER);
//            File file = getFileToTest("samples/deployment/testDeployment.txt");
//
//            int servicesDeployed = BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.SERVER,
//                    "Deployment mode set to error.");
//            Assert.assertEquals(servicesDeployed, 0, "There shouldn't be any services deployed.");
//
//            application = ApplicationRegistry.getInstance().getApplication("testDeployment.txt");
//            Assert.assertNull(application, "Application can't be deployed.");
//        } finally {
//            if (application != null) {
//                // Force unregister.
//                ApplicationRegistry.getInstance().unregisterApplication(application);
//            }
//        }
//    }
//
//    @Test(description = "Validate Main function deployment.")
//    public void testMainFunctionDeployment() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
//            File file = getFileToTest("samples/deployment/testMain.bal");
//
//            int servicesDeployed = BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.RUN_FILE,
//                    "Deployment mode set to Error.");
//            Assert.assertEquals(servicesDeployed, 0, "There shouldn't be any services deployed.");
//
//            application = ApplicationRegistry.getInstance().getApplication("testMain.bal");
//            Assert.assertNull(application, "Application can't be deployed.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate deployment when no main file.")
//    public void testNoMainFunctionDeployment() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
//            File file = getFileToTest("samples/deployment/testNoMain.bal");
//
//            BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.ERROR,
//                    "Deployment mode not set to Error.");
//            application = ApplicationRegistry.getInstance().getApplication("testNoMain.bal");
//            Assert.assertNull(application, "Application can't be deployed.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate non .bal deployment with main function.")
//    public void testMainFunctionDeploymentInvalidFile() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
//            File file = getFileToTest("samples/deployment/testMain.txt");
//
//            BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.ERROR,
//                    "Deployment mode not set to Error.");
//            application = ApplicationRegistry.getInstance().getApplication("testMain.txt");
//            Assert.assertNull(application, "Application can't be deployed.");
//        } finally {
//            cleanup(application);
//        }
//    }
//
//    @Test(description = "Validate Link errors while doing the Main function Deployment.")
//    public void testMainDeploymentLinkError() {
//        Application application = null;
//        try {
//            ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
//            File file = getFileToTest("samples/deployment/testInvalidFunction.bal");
//
//            BalDeployer.deployBalFile(file);
//
//            Assert.assertEquals(ServiceContextHolder.getInstance().getRuntimeMode(), Constants.RuntimeMode.ERROR,
//                    "Deployment mode not set to error.");
//
//            application = ApplicationRegistry.getInstance().getApplication("testInvalidFunction.bal");
//            Assert.assertNull(application, "Application is created, But it shouldn't.");
//        } finally {
//            cleanup(application);
//        }
//    }

}
