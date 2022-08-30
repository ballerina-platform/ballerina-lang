/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.symbols;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.BallerinaConstantSymbol;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for constant annotation attachments.
 *
 * @since 2201.2.0
 */
public class ConstAnnotationAttachmentSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/annot_attachment_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test
    public void testValuesInConstantAnnotationAttachment() {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(20, 12));
        assertTrue(symbol.isPresent());
        TypeDefinitionSymbol typeDefSym = (TypeDefinitionSymbol) symbol.get();

        List<AnnotationAttachmentSymbol> attachments = typeDefSym.annotAttachments();
        assertTrue(attachments.size() > 0);

        AnnotationAttachmentSymbol annotAttachment = attachments.get(0);
        assertTrue(annotAttachment.isConstAnnotation());

        assertTrue(annotAttachment.attachmentValue().isPresent());
        ConstantSymbol constantSymbol = annotAttachment.attachmentValue().get();
        BallerinaConstantSymbol.BallerinaConstantValue constVal =
                (BallerinaConstantSymbol.BallerinaConstantValue) constantSymbol.constValue();

        // Test type-descriptor
        assertEquals(constVal.typeDescriptor().typeKind(), TypeDescKind.INTERSECTION);
        assertEquals(((IntersectionTypeSymbol) constVal.typeDescriptor()).memberTypeDescriptors().get(0).typeKind(),
                TypeDescKind.RECORD);
        assertEquals(((IntersectionTypeSymbol) constVal.typeDescriptor()).memberTypeDescriptors().get(1).typeKind(),
                TypeDescKind.READONLY);
        RecordTypeSymbol recTypeSymbol =
                (RecordTypeSymbol) ((IntersectionTypeSymbol) constVal.typeDescriptor()).memberTypeDescriptors().get(0);
        assertEquals(recTypeSymbol.signature(), "record {|1 id; record {|1 a; 2 b;|} perm;|}");

        // Test const value
        assertTrue(constVal.value() instanceof HashMap);
        HashMap valueMap = (HashMap) constVal.value();

        assertTrue(valueMap.get("id") instanceof BallerinaConstantSymbol.BallerinaConstantValue);
        BallerinaConstantSymbol.BallerinaConstantValue idValue =
                (BallerinaConstantSymbol.BallerinaConstantValue) valueMap.get("id");
        assertEquals(idValue.typeDescriptor().typeKind(), TypeDescKind.INT);
        assertEquals(idValue.value(), 1L);

        assertTrue(valueMap.get("perm") instanceof BallerinaConstantSymbol.BallerinaConstantValue);
        BallerinaConstantSymbol.BallerinaConstantValue permValue =
                (BallerinaConstantSymbol.BallerinaConstantValue) valueMap.get("perm");
        assertEquals(permValue.typeDescriptor().typeKind(), TypeDescKind.INTERSECTION);
        assertEquals(((IntersectionTypeSymbol) permValue.typeDescriptor()).effectiveTypeDescriptor().typeKind(),
                TypeDescKind.RECORD);
        assertTrue(permValue.value() instanceof HashMap);
        HashMap permMap = (HashMap) permValue.value();
        assertEquals(((BallerinaConstantSymbol.BallerinaConstantValue) permMap.get("a")).value(), 1L);
        assertEquals(((BallerinaConstantSymbol.BallerinaConstantValue) permMap.get("a")).typeDescriptor().typeKind(),
                TypeDescKind.INT);
        assertEquals(((BallerinaConstantSymbol.BallerinaConstantValue) permMap.get("b")).value(), 2L);
        assertEquals(((BallerinaConstantSymbol.BallerinaConstantValue) permMap.get("b")).typeDescriptor().typeKind(),
                TypeDescKind.INT);
    }
}
