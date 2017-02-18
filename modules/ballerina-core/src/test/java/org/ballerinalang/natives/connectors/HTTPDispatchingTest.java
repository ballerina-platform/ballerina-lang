/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.natives.connectors;

import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.http.HTTPResourceDispatcher;
import org.ballerinalang.services.dispatchers.http.HTTPServiceDispatcher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test the functionality of HTTP Dispatchers.
 *
 * @since 0.8.0
 */
public class HTTPDispatchingTest {

    @BeforeClass
    public void setup() {
        // Resister HTTP Dispatchers
        DispatcherRegistry.getInstance().registerServiceDispatcher(new HTTPServiceDispatcher());
        DispatcherRegistry.getInstance().registerResourceDispatcher(new HTTPResourceDispatcher());
    }

    @Test
    public void testHTTPDispatching() {

        // Create Ballerina Model
//        Service service = new Service(new Identifier("service1"));
//        service.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_BASE_PATH, "/base1"));
//        Resource resource = new Resource();
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_PATH, "/sub"));
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_METHOD_GET));
//
//        Resource[] resources = new Resource[1];
//        resources[0] = resource;
//        service.setResources(resources);
//
//        BallerinaFile ballerinaFile = new BallerinaFile();
//        ballerinaFile.addService(service);
//        Package aPackage = new Package("org.sample.test");
//        aPackage.addFiles(ballerinaFile);
//        Application application = new Application("Ballerina-Resource-Test-App");
//        application.addPackage(aPackage);
//
//        // Register application
//        ApplicationRegistry.getInstance().registerApplication(application);
//
//        // Prepare the message
//        CarbonMessage cMsg = new DefaultCarbonMessage();
//        cMsg.setProperty(org.wso2.carbon.messaging.Constants.TO, "/base1/sub/foo");
//        cMsg.setProperty(Constants.HTTP_METHOD, "GET");
//        Context balContext = new Context(cMsg);
//        balContext.setProperty(org.ballerinalang.runtime.Constants.PROTOCOL, Constants.PROTOCOL_HTTP);
//
//        // Send the message
//        ServiceDispatcher serviceDispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(
//                Constants.PROTOCOL_HTTP);
//        Assert.assertTrue(serviceDispatcher.dispatch(balContext, null), "HTTP Dispatching failed");

    }

    @Test
    public void testDefaultBasePathDispatching() {

        // Create Ballerina Model
//        Service service = new Service(new Identifier("service2"));
//        service.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_BASE_PATH, Constants.DEFAULT_BASE_PATH));
//        Resource resource = new Resource();
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_PATH, "/sub"));
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_METHOD_GET));
//
//        Resource[] resources = new Resource[1];
//        resources[0] = resource;
//        service.setResources(resources);
//
//        BallerinaFile ballerinaFile = new BallerinaFile();
//        ballerinaFile.addService(service);
//        Package aPackage = new Package("org.sample.test");
//        aPackage.addFiles(ballerinaFile);
//        Application application = new Application("Ballerina-Resource-Test-App");
//        application.addPackage(aPackage);
//
//        // Register application
//        ApplicationRegistry.getInstance().registerApplication(application);
//
//        // Prepare the message
//        CarbonMessage cMsg = new DefaultCarbonMessage();
//        cMsg.setProperty(org.wso2.carbon.messaging.Constants.TO, "/sub/foo");
//        cMsg.setProperty(Constants.HTTP_METHOD, "GET");
//        Context balContext = new Context(cMsg);
//        balContext.setProperty(org.ballerinalang.runtime.Constants.PROTOCOL, Constants.PROTOCOL_HTTP);
//
//        // Send the message
//        ServiceDispatcher serviceDispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(
//                Constants.PROTOCOL_HTTP);
//        Assert.assertTrue(serviceDispatcher.dispatch(balContext, null), "HTTP default base path dispatchers failed");

    }

    @Test
    public void testDefaultResourcePathDispatching() {

        // Create Ballerina Model
//        Service service = new Service(new Identifier("service3"));
//        service.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_BASE_PATH, "/base3"));
//        Resource resource = new Resource();
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_NAME_PATH, Constants.DEFAULT_SUB_PATH));
//        resource.addAnnotation(new Annotation(Constants.ANNOTATION_METHOD_GET));
//
//        Resource[] resources = new Resource[1];
//        resources[0] = resource;
//        service.setResources(resources);
//
//        BallerinaFile ballerinaFile = new BallerinaFile();
//        ballerinaFile.addService(service);
//        Package aPackage = new Package("org.sample.test");
//        aPackage.addFiles(ballerinaFile);
//        Application application = new Application("Ballerina-Resource-Test-App");
//        application.addPackage(aPackage);
//
//        // Register application
//        ApplicationRegistry.getInstance().registerApplication(application);
//
//        // Prepare the message
//        CarbonMessage cMsg = new DefaultCarbonMessage();
//        cMsg.setProperty(org.wso2.carbon.messaging.Constants.TO, "/base3/abc/def");
//        cMsg.setProperty(Constants.HTTP_METHOD, "GET");
//        Context balContext = new Context(cMsg);
//        balContext.setProperty(org.ballerinalang.runtime.Constants.PROTOCOL, Constants.PROTOCOL_HTTP);
//
//        // Send the message
//        ServiceDispatcher serviceDispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(
//                Constants.PROTOCOL_HTTP);
//        Assert.assertTrue(serviceDispatcher.dispatch(balContext, null), "HTTP default resource path dispatch failed");

    }

}
