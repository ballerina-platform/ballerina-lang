/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.docs;


import org.ballerinalang.docgen.Generator;
import org.ballerinalang.docgen.docs.utils.WorkspaceUtils;
import org.ballerinalang.docgen.model.ActionDoc;
import org.ballerinalang.docgen.model.AnnotationDoc;
import org.ballerinalang.docgen.model.ConnectorDoc;
import org.ballerinalang.docgen.model.EnumDoc;
import org.ballerinalang.docgen.model.FunctionDoc;
import org.ballerinalang.docgen.model.GlobalVariableDoc;
import org.ballerinalang.docgen.model.PackageName;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.StructDoc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML document writer test.
 */
public class HtmlDocTest {

    @BeforeClass()
    public void setup() {
    }

    @Test(description = "Multiple packages should be shown even when one page is generated")
    public void testMultiPackage() throws Exception {
        List<PackageName> packages = new ArrayList<>();
        packages.add(new PackageName("a.b.c", ""));
        packages.add(new PackageName("x.y", ""));
        packages.add(new PackageName("x.y.z", ""));

        BLangPackage bLangPackage = createPackage("package x.y;");
        Page page = Generator.generatePage(bLangPackage, packages);

        Assert.assertEquals(page.links.size() , 3);
        Assert.assertFalse(page.links.get(0).active);
        Assert.assertTrue(page.links.get(1).active);
    }


    @Test(description = "Empty package should give an empty page")
    public void testEmptyPackage() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y;");
        Page page = generatePage(bLangPackage);
        Assert.assertTrue(page.constructs.isEmpty());
    }

    @Test(description = "Functions in a package should be shown in the constructs")
    public void testFunctions() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; public function hello(){}");
        Page page = generatePage(bLangPackage);
        Assert.assertEquals(page.constructs.size(), 1);
        Assert.assertEquals(page.constructs.get(0).name, "hello");
    }

    @Test(description = "Structs in a package should be shown in the constructs")
    public void testStructs() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; public struct Message {}");
        Page page = generatePage(bLangPackage);
        Assert.assertEquals(page.constructs.size(), 1);
        Assert.assertEquals(page.constructs.get(0).name, "Message");
    }

    @Test(description = "One function with a struct bindings in a package should be grouped together shown in the " +
            "constructs")
    public void testFunctionsWithStructBindings() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "public function <Message m>hello(){} " +
                "public struct Message { string message; int id;}");
        Page page = generatePage(bLangPackage);
        Assert.assertEquals(page.constructs.size(), 1);
        Assert.assertEquals(page.constructs.get(0).name, "Message");
        Assert.assertEquals(page.constructs.get(0).children.get(0).name, "hello");
    }

    @Test(description = "One function without a struct bindings in a package should not be grouped together with the" +
            "structs shown in the constructs")
    public void testFunctionsWithoutStructBindings() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "public function hello(){} " +
                "public struct Message { string message; int id;}");
        Page page = generatePage(bLangPackage);
        Assert.assertEquals(page.constructs.size(), 2);
        Assert.assertEquals(page.constructs.get(0).name, "Message");
        Assert.assertEquals(page.constructs.get(1).name, "hello");
    }

    @Test(description = "Functions with struct bindings in a package should be grouped together and functions" +
            "without struct bindings should be isolated as shown in the constructs")
    public void testFunctionsWithWithoutStructBindings() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "public function <Message m>hello(){} " +
                "public struct Message { string message; int id;} " +
                "public function sayBye(){}");

        Page page = generatePage(bLangPackage);
        Assert.assertEquals(page.constructs.size(), 2);
        Assert.assertEquals(page.constructs.get(0).name, "Message");
        Assert.assertEquals(page.constructs.get(0).children.get(0).name, "hello");
        Assert.assertEquals(page.constructs.get(1).name, "sayBye");
    }

    @Test(description = "Function properties should be available via construct")
    public void testFunctionsPropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description { value:\"This function would say hello\"}" +
                "@Param { value:\"message: The message sent\" }" +
                "@Return { value:\"int representation of the message\" }" +
                "public function sayHello(string message)(int){}");

        FunctionDoc functionDoc = Generator.createDocForNode(bLangPackage.getFunctions().get(0));
        Assert.assertEquals(functionDoc.name, "sayHello", "Function name should be extracted");
        Assert.assertEquals(functionDoc.description, "This function would say hello", "Description of the " +
                "function should be extracted");
        Assert.assertEquals(functionDoc.parameters.get(0).name, "message", "Parameter name should be extracted");
        Assert.assertEquals(functionDoc.parameters.get(0).dataType, "string", "Parameter type should be extracted");
        Assert.assertEquals(functionDoc.parameters.get(0).description, "The message sent", "Description of the " +
                "parameter should be extracted");
        Assert.assertEquals(functionDoc.returnParams.get(0).dataType, "int", "Return parameter type " +
                "should be extracted");
        Assert.assertEquals(functionDoc.returnParams.get(0).description, "int representation of the message",
                "Description of the return parameter should be extracted");

    }

    @Test(description = "Connector properties should be available via construct")
    public void testConnectorPropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description { value:\"Http client connector for outbound HTTP requests\"}\n" +
                "@Param { value:\"serviceUri: Url of the service\" }\n" +
                "@Param { value:\"n: connector options\" }" +
                "connector HttpClient (string serviceUri, int n) {" +
                "@Description { value:\"The POST action implementation of the HTTP ConnectorDoc\"}\n" +
                "@Param { value:\"path: Resource path \" }\n" +
                "@Param { value:\"req: An HTTP Request struct\" }\n" +
                "@Return { value:\"The response message\" }\n" +
                "@Return { value:\"Error occured during HTTP client invocation\" }\n" +
                "action post(string path, string req) (string, int) { return \"value within filter\"; }}");

        ConnectorDoc connectorDoc = Generator.createDocForNode(bLangPackage.getConnectors().get(0));
        Assert.assertEquals(connectorDoc.name, "HttpClient", "Connector name should be extracted");
        Assert.assertEquals(connectorDoc.description, "Http client connector for outbound HTTP requests",
                "Description of the connector should be extracted");
        Assert.assertEquals(connectorDoc.parameters.get(0).name, "serviceUri", "Parameter name should be extracted");
        Assert.assertEquals(connectorDoc.parameters.get(0).dataType, "string", "Parameter type should be extracted");
        Assert.assertEquals(connectorDoc.parameters.get(0).description, "Url of the service",
                "Description of the parameter type should be extracted");

        // For actions inside the connector
        ActionDoc actionDoc = (ActionDoc) connectorDoc.children.get(0);
        Assert.assertEquals(actionDoc.name, "post", "Action name should be extracted");
        Assert.assertEquals(actionDoc.description, "The POST action implementation of the HTTP ConnectorDoc",
                "Description of the action should be extracted");
        Assert.assertEquals(actionDoc.parameters.get(0).name, "path", "Parameter name should be extracted");
        Assert.assertEquals(actionDoc.parameters.get(0).dataType, "string", "Parameter type should be extracted");
        Assert.assertEquals(actionDoc.parameters.get(0).description, "Resource path", "Description of the " +
                "parameter should be extracted");
        Assert.assertEquals(actionDoc.returnParams.get(1).dataType, "int", "Return parameter type should be extracted");
        Assert.assertEquals(actionDoc.returnParams.get(1).description, "Error occured during HTTP client invocation",
                "Description of the return parameter should be extracted");
    }

    @Test(description = "Struct properties should be available via construct")
    public void testStructPropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description { value:\"Message sent by the client\"}" +
                "@Field {value:\"count: Number of retries\"}\n" +
                "@Field {value:\"interval: Retry interval in millisecond\"}" +
                "struct Message {int interval;int count;}");

        StructDoc structDoc = Generator.createDocForNode(bLangPackage.getStructs().get(0));
        Assert.assertEquals(structDoc.name, "Message", "Struct name should be extracted");
        Assert.assertEquals(structDoc.description, "Message sent by the client", "Description of the " +
                "struct should be extracted");

        // Struct fields
        Assert.assertEquals(structDoc.fields.get(0).name, "interval", "Struct field name should be extracted");
        Assert.assertEquals(structDoc.fields.get(0).dataType, "int", "Struct field type should be extracted");
        Assert.assertEquals(structDoc.fields.get(0).description, "Retry interval in millisecond",
                "Description of the struct field should be extracted");
    }

    @Test(description = "Enum properties should be available via construct")
    public void testEnumPropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description { value:\"The direction of the parameter\"}\n" +
                "@Field { value:\"IN: IN parameters are used to send values to stored procedures\"}\n" +
                "@Field { value:\"OUT: OUT parameters are used to get values from stored procedures\"}\n" +
                "public enum Direction { IN,OUT}");

        EnumDoc enumDoc = Generator.createDocForNode(bLangPackage.getEnums().get(0));
        Assert.assertEquals(enumDoc.name, "Direction", "Enum name should be extracted");
        Assert.assertEquals(enumDoc.description, "The direction of the parameter", "Description of the " +
                "enum should be extracted");

        // Enumerators inside the enum
        Assert.assertEquals(enumDoc.enumerators.get(0).name, "IN", "Enumerator name should be extracted");
        Assert.assertEquals(enumDoc.enumerators.get(0).description, "IN parameters are used to send values to " +
                "stored procedures", "Description of the enumerator should be extracted");
    }

    @Test(description = "Global variables should be available via construct")
    public void testGlobalVariablePropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description { value:\"The Read Append access mode\"}\n" +
                "public const string RA = \"RA\";");

        GlobalVariableDoc globalVariableDoc = Generator.createDocForNode(bLangPackage.getGlobalVariables().get(0));
        Assert.assertEquals(globalVariableDoc.name, "RA", "Global variable name should be extracted");
        Assert.assertEquals(globalVariableDoc.dataType, "string", "Global variable type should be extracted");
        Assert.assertEquals(globalVariableDoc.description, "The Read Append access mode", "Description of the " +
                "global variable should be extracted");
    }

    @Test(description = "Annotation properties should be available via construct")
    public void testAnnotationPropertiesExtracted() throws Exception {
        BLangPackage bLangPackage = createPackage("package x.y; " +
                "@Description {value: \"AnnotationDoc to upgrade connection from HTTP to WS in the " +
                                "same base path\"}\n" +
                "@Field {value:\"upgradePath:Upgrade path for the WebSocket service from " +
                                "HTTP to WS\"}\n" +
                "@Field {value:\"serviceName:Name of the WebSocket service where the HTTP service should " +
                "               upgrade to\"}\n" +
                "public annotation webSocket attach service<> {\n" +
                "    string upgradePath;\n" +
                "    string serviceName;\n" +
                "}");

        AnnotationDoc annotationDoc = Generator.createDocForNode(bLangPackage.getAnnotations().get(0));
        Assert.assertEquals(annotationDoc.name, "webSocket", "Annotation name should be extracted");
        Assert.assertEquals(annotationDoc.description, "AnnotationDoc to upgrade connection from HTTP to WS " +
                "in the same base path", "Description of the annotation should be extracted");

        // Annotation Fields
        Assert.assertEquals(annotationDoc.attributes.get(0).name, "upgradePath", "Annotation attribute name " +
                "should be extracted");
        Assert.assertEquals(annotationDoc.attributes.get(0).dataType, "string", "Annotation attribute type " +
                "should be extracted");
        Assert.assertEquals(annotationDoc.attributes.get(0).description, "Upgrade path for the WebSocket service " +
                "from HTTP to WS", "Description of the annotation attribute should be extracted");
    }

    /**
     * Create the package from the bal file
     * @param source bal file which contains
     * @return BLangPackage
     */
    private BLangPackage createPackage(String source) {
        return WorkspaceUtils.getBallerinaFileForContent("untitled.bal", source);
    }

    /**
     * Generate the api page using the package
     * @param balPackage bal package
     * @return page generated
     */
    private Page generatePage(BLangPackage balPackage) {
        List<PackageName> packages = new ArrayList<>();
        packages.add(new PackageName((balPackage.symbol).pkgID.name.value, ""));
        return Generator.generatePage(balPackage, packages);
    }
}
