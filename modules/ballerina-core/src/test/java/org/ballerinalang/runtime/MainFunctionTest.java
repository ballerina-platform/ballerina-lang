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
//
//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.ballerinalang.core.interpreter.SymScope;
//import org.ballerinalang.model.Application;
//import org.ballerinalang.model.BallerinaFile;
//import org.ballerinalang.model.SymbolName;
//import org.ballerinalang.runtime.deployer.BalDeployer;
//import org.ballerinalang.natives.BuiltInNativeConstructLoader;
//import org.ballerinalang.runtime.internal.GlobalScopeHolder;
//import org.ballerinalang.runtime.internal.ServiceContextHolder;
//import org.ballerinalang.runtime.registry.ApplicationRegistry;
//
//import java.io.File;
//import java.io.PrintStream;
//import java.net.URL;
//
//import static org.ballerinalang.runtime.Constants.SYSTEM_PROP_BAL_ARGS;

/**
 * Test cases for validate main function.
 */
public class MainFunctionTest {

//    private PrintStream original;
//
//    @BeforeClass
//    public void setup() {
//        original = System.out;
//        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
//        if (symScope.lookup(new SymbolName("ballerina.model.system:print_string")) == null) {
//            BuiltInNativeConstructLoader.loadConstructs();
//        }
//        ServiceContextHolder.getInstance().setRuntimeMode(Constants.RuntimeMode.RUN_FILE);
//    }
//
//    @AfterClass
//    public void tearDown() {
//        System.setOut(original);
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
//    @Test(description = "Validate Main function invocation.")
//    public void testMainFunctionDeployment() {
//        Application application = null;
//        System.setProperty(SYSTEM_PROP_BAL_ARGS, "Hello World;Ballerina;");
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
//
//            BallerinaFile ballerinaFileToExecute = ServiceContextHolder.getInstance().getBallerinaFileToExecute();
//
//            Assert.assertNotNull(ballerinaFileToExecute, "There is no Main program to be executed.");
//            BalProgramExecutor.execute(ballerinaFileToExecute);
//
//        } finally {
//            System.setProperty(SYSTEM_PROP_BAL_ARGS, "");
//            System.setOut(original);
//            cleanup(application);
//        }
//    }
}
