/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TupleMemberTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.TypeDescKind.BOOLEAN;
import static io.ballerina.compiler.api.symbols.TypeDescKind.INT;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for tuple member type symbols.
 *
 * @since 2201.4.0
 */
public class SymbolsInTupleTypeSymbolTest {

    private SemanticModel model;
    private Document srcFile;

    private final List<List<String>> expAnnotsInTupleType = List.of(
            List.of("Config", "HttpConfig"),
            List.of("Config"),
            List.of("HttpConfig"));

    @BeforeClass
    public void setup() {
        Project project = BCompileUtil.loadProject("test-src/symbols/symbols_tuple_type_symbol_test.bal");
        model = getDefaultModulesSemanticModel(project);
        srcFile = getDocumentForSingleSource(project);
    }

    @Test(dataProvider = "TupleTypeMemberInfo")
    public void testTupleTypeMembers(int line, int col, TypeDescKind typeKind, List<String> annots) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TUPLE_MEMBER_TYPE);

        TupleMemberTypeSymbol tupleMember = (TupleMemberTypeSymbol) symbol.get();
        assertEquals(tupleMember.getType().typeKind(), typeKind);

        // check annotation attachments
        List<AnnotationAttachmentSymbol> annotSymbols = tupleMember.annotAttachments();
        assertEquals(annotSymbols.size(), annots.size());
        for (int i = 0; i < annotSymbols.size(); i++) {
            assertEquals(annotSymbols.get(i).typeDescriptor().getName().get(), annots.get(i));
        }
    }

    @DataProvider(name = "TupleTypeMemberInfo")
    public Object[][] getTupleTypeMemberInfo() {
        return new Object[][]{
                {16, 45, INT, List.of("Config", "HttpConfig")},
                {16, 69, STRING, List.of("Config")},
                {16, 89, BOOLEAN, List.of("HttpConfig")},
                {19, 35, STRING, List.of("TLSConfig", "Config")},
                {19, 55, INT, List.of("HttpConfig")},
        };
    }

    @Test
    public void testTupleTypeDef() {
        List<TypeDescKind> typeDescs = List.of(INT, STRING, BOOLEAN);

        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(16, 5));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE_DEFINITION);

        assertEquals(((TypeDefinitionSymbol) symbol.get()).typeDescriptor().typeKind(), TypeDescKind.TUPLE);
        TupleTypeSymbol tupleType = (TupleTypeSymbol) ((TypeDefinitionSymbol) symbol.get()).typeDescriptor();

        List<TupleMemberTypeSymbol> tupleMembers = tupleType.tupleTypeMembers();
        assertEquals(tupleMembers.size(), 3);

        // check tuple members
        for (int i = 0; i < typeDescs.size(); i++) {
            assertEquals(tupleMembers.get(i).kind(), SymbolKind.TUPLE_MEMBER_TYPE);
            TupleMemberTypeSymbol memberType = tupleMembers.get(i);
            assertEquals(memberType.getType().typeKind(), typeDescs.get(i));

            // check annot attachments of tuple members
            List<AnnotationAttachmentSymbol> annotAttachments = memberType.annotAttachments();
            assertEquals(annotAttachments.size(), expAnnotsInTupleType.get(i).size());
            List<String> expAnnots = expAnnotsInTupleType.get(i);
            for (int j = 0; j < expAnnots.size(); j++) {
                assertEquals(annotAttachments.get(j).typeDescriptor().getName().get(), expAnnots.get(j));
            }
        }
    }

    @Test(dataProvider = "AnnotAttachmentsInfo")
    public void testAnnotAttachments(int line, int col, String name) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.ANNOTATION);
        assertTrue(symbol.get().getName().isPresent());
        assertEquals(symbol.get().getName().get(), name);
    }

    @DataProvider(name = "AnnotAttachmentsInfo")
    public Object[][] getAnnotAttachmentsInfo() {
        return new Object[][]{
                {16, 15, "Config"},
                {16, 35, "HttpConfig"},
                {16, 52, "Config"},
                {16, 78, "HttpConfig"},
                {19, 6, "TLSConfig"},
                {19, 17, "Config"},
                {19, 45, "HttpConfig"},
        };
    }
}
