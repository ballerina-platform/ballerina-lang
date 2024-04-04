/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.annotations;

import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Class to test ExternalDependency annotation.
 *
 * @since 2201.9.0
 */
public class ExternalDependencyAnnotationTest {

    private BLangPackage pkgNode;

    @BeforeClass
    public void setup() {
        CompileResult result = BCompileUtil.compile("test-src/annotations/external_dependency_annotation.bal");
        pkgNode = (BLangPackage) result.getAST();
    }

    @Test(description = "Test the ExternalDependency annotation attach points")
    public void testExternalDependencyAttachPoints() {
        Set<AttachPoint> attachPoints = pkgNode.functions.get(0).annAttachments.get(0).annotationSymbol.points;
        List<String> expectedAttachPoints = Arrays.asList("type", "class", "function");

        assertEquals(attachPoints.size(), 3);
        assertTrue(attachPoints.stream()
                .allMatch(attachPoint -> expectedAttachPoints.contains(attachPoint.point.getValue())));
    }

    @Test(description = "Test the ExternalDependency annotation used on functions")
    public void testExternalDependencyAnnotOnFunctions() {
        List<BLangFunction> functions = pkgNode.functions;
        assertEquals(functions.get(0).annAttachments.get(0).annotationName.value, "ExternalDependency");
    }

    @Test(description = "Test the ExternalDependency annotation used on classes")
    public void testExternalDependencyAnnotOnClasses() {
        assertEquals(pkgNode.classDefinitions.get(0).annAttachments.get(0).annotationName.value, "ExternalDependency");
    }

    @Test(description = "Test the ExternalDependency annotation used on type definitions")
    public void testExternalDependencyAnnotOnTypeDefinitions() {
        List<BLangTypeDefinition> typeDefinitions = pkgNode.typeDefinitions;
        assertEquals(typeDefinitions.get(0).annAttachments.get(0).annotationName.value, "ExternalDependency");
    }

    @AfterClass
    public void tearDown() {
        pkgNode = null;
    }
}
