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
import io.ballerina.compiler.api.symbols.MemberTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
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
import static io.ballerina.compiler.api.symbols.TypeDescKind.SINGLETON;
import static io.ballerina.compiler.api.symbols.TypeDescKind.STRING;
import static io.ballerina.compiler.api.symbols.TypeDescKind.TYPE_REFERENCE;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.assertBasicsAndGetSymbol;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDefaultModulesSemanticModel;
import static io.ballerina.semantic.api.test.util.SemanticAPITestUtils.getDocumentForSingleSource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for tuple type symbol.
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
    public void testTupleTypeMembers(int line, int col, TypeDescKind typeKind) {
        Optional<Symbol> symbol = model.symbol(srcFile, LinePosition.from(line, col));
        assertTrue(symbol.isPresent());
        assertEquals(symbol.get().kind(), SymbolKind.TYPE);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.get();
        assertEquals(typeSymbol.typeKind(), typeKind);
    }

    @DataProvider(name = "TupleTypeMemberInfo")
    public Object[][] getTupleTypeMemberInfo() {
        return new Object[][]{
                {16, 52, INT},
                {16, 76, STRING},
                {16, 96, BOOLEAN},
                {28, 33, TYPE_REFERENCE},
                {28, 42, TYPE_REFERENCE},
                {28, 49, TYPE_REFERENCE},
                {31, 35, STRING},
                {31, 55, TYPE_REFERENCE},
        };
    }

    @Test
    public void testConstantAsTupleMember() {
        TypeSymbol sym = (TypeSymbol) assertBasicsAndGetSymbol(model, srcFile, 28, 56, "BConst", SymbolKind.CONSTANT);
        assertEquals(sym.typeKind(), SINGLETON);
    }

    @Test
    public void testTupleTypeDef() {
        List<TypeDescKind> typeDescs = List.of(INT, STRING, BOOLEAN);
        List<List<String>> expAnnotsInTupleType = List.of(
                List.of("Config", "HttpConfig"),
                List.of("Config"),
                List.of("HttpConfig"));

        List<MemberTypeSymbol> tupleMembers = assertAndGetTupleMember(16, 5, "BuiltInTupType");
        assertEquals(tupleMembers.size(), typeDescs.size());

        // check tuple members
        for (int i = 0; i < typeDescs.size(); i++) {
            MemberTypeSymbol memberType = tupleMembers.get(i);
            assertTupleMemberType(memberType, typeDescs.get(i));

            // check annot attachments of tuple members
            List<AnnotationAttachmentSymbol> annotAttachments = memberType.annotAttachments();
            assertEquals(annotAttachments.size(), expAnnotsInTupleType.get(i).size());
            List<String> expAnnots = expAnnotsInTupleType.get(i);
            for (int j = 0; j < expAnnots.size(); j++) {
                assertEquals(annotAttachments.get(j).typeDescriptor().getName().get(), expAnnots.get(j));
            }
        }
    }

    @Test
    public void testTupleTypeWithConstantMember() {
        List<TypeDescKind> typeDescs = List.of(TYPE_REFERENCE, TYPE_REFERENCE, TYPE_REFERENCE, SINGLETON);

        List<MemberTypeSymbol> tupleMembers = assertAndGetTupleMember(28, 5, "UserDefTupType");
        assertEquals(tupleMembers.size(), typeDescs.size());

        for (int i = 0; i < typeDescs.size(); i++) {
            assertTupleMemberType(tupleMembers.get(i), typeDescs.get(i));
        }
    }

    @Test(dataProvider = "AnnotAttachmentsInfo")
    public void testAnnotAttachments(int line, int col, String name) {
        assertBasicsAndGetSymbol(model, srcFile, line, col, name, SymbolKind.ANNOTATION);
    }

    @DataProvider(name = "AnnotAttachmentsInfo")
    public Object[][] getAnnotAttachmentsInfo() {
        return new Object[][]{
                {16, 22, "Config"},
                {16, 41, "HttpConfig"},
                {16, 58, "Config"},
                {16, 86, "HttpConfig"},
                {28, 22, "HttpConfig"},
                {31, 6, "TLSConfig"},
                {31, 17, "Config"},
                {31, 45, "HttpConfig"},
        };
    }

    // utils
    private List<MemberTypeSymbol> assertAndGetTupleMember(int line, int col, String tupleTypeName) {
        Symbol symbol = assertBasicsAndGetSymbol(model, srcFile, line, col, tupleTypeName, SymbolKind.TYPE_DEFINITION);
        assertEquals(((TypeDefinitionSymbol) symbol).typeDescriptor().typeKind(), TypeDescKind.TUPLE);
        TupleTypeSymbol tupleType = (TupleTypeSymbol) ((TypeDefinitionSymbol) symbol).typeDescriptor();
        return tupleType.members();
    }

    private void assertTupleMemberType(MemberTypeSymbol symbol, TypeDescKind typeDescKind) {
        assertEquals(symbol.kind(), SymbolKind.TUPLE_MEMBER);
        assertEquals(symbol.typeDescriptor().typeKind(), typeDescKind);
    }
}
