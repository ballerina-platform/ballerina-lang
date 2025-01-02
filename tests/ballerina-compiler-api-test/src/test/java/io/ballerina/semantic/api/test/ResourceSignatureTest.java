/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.semantic.api.test;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static io.ballerina.tools.text.LinePosition.from;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for class symbols.
 *
 * @since 2.0.0
 */
public class ResourceSignatureTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/resource_signature_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testResourceSignature() {
        ResourceMethodSymbol method = (ResourceMethodSymbol) model.symbol(srcFile, from(17, 22)).get();
        assertEquals(method.resourcePath().kind(), ResourcePath.Kind.PATH_SEGMENT_LIST);
        assertEquals(method.resourcePath().signature(), "store/'order");
        assertEquals(method.signature(), "resource function get store/'order () returns string");
    }
}
