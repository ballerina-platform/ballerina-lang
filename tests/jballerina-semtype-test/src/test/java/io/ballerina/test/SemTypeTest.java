/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.test;

import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.test.utils.Constants.COMMENT;
import static io.ballerina.test.utils.Constants.DISABLED_FILE;
import static io.ballerina.test.utils.Constants.FAILING_FILE;
import static io.ballerina.test.utils.Constants.SUBTYPE_SYMBOL;

/**
 * Test semantic type relations.
 * The notation T1<:T2 means T1 is a subtype of T2.
 *
 * @since 2.0.0
 */
public class SemTypeTest {

    private final Types types = Types.getInstance(new CompilerContext());

    @DataProvider(name = "filePathProvider")
    public Object[] filePathProvider() {
        File dir = resolvePath("test-src").toFile();
        List<String> files = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            String fileName = file.getName();
            if (fileName.endsWith(FAILING_FILE) || fileName.endsWith(DISABLED_FILE)) {
                continue;
            }
            files.add(file.getAbsolutePath());
        }
        return files.toArray();
    }

    @Test(dataProvider = "filePathProvider")
    public void testSubTypeRelationship(String filePath) throws IOException {
        Set<String> actualRelations = actualSubTypeRelations(filePath);
        Set<String> expectedRelations = expectedSubTypeRelations(filePath);
        Assert.assertEquals(actualRelations.size(), expectedRelations.size());
        actualRelations.removeAll(expectedRelations);
        Assert.assertTrue(actualRelations.isEmpty());
    }

    private Set<String> actualSubTypeRelations(String filePath) {
        CompileResult compileResult = BCompileUtil.compile(filePath);
        BLangPackage bLangPackage = (BLangPackage) compileResult.getAST();
        List<BTypeDefinitionSymbol> bTypeDefinitionSymbols = new ArrayList<>();
        for (Scope.ScopeEntry value : bLangPackage.symbol.scope.entries.values()) {
            BSymbol bSymbol = value.symbol;
            if (bSymbol.kind == SymbolKind.TYPE_DEF && bSymbol.origin == SymbolOrigin.SOURCE) {
                bTypeDefinitionSymbols.add((BTypeDefinitionSymbol) bSymbol);
            }
        }

        Set<String> relations = new HashSet<>();
        int len = bTypeDefinitionSymbols.size();
        for (int i = 0; i < len; i++) {
            BTypeDefinitionSymbol td1 = bTypeDefinitionSymbols.get(i);
            BType t1 = td1.type;
            String n1 = td1.name.getValue();
            for (int j = i + 1; j < len; j++) {
                BTypeDefinitionSymbol td2 = bTypeDefinitionSymbols.get(j);
                BType t2 = td2.type;
                String n2 = td2.name.getValue();

                computeSubTypeRelation(t1, t2, n1, n2, relations);
                computeSubTypeRelation(t2, t1, n2, n1, relations);
            }
        }
        return relations;
    }

    private void computeSubTypeRelation(BType sourceType, BType targetType, String sourceTypeName,
                                        String targetTypeName, Set<String> relations) {
        if (types.isAssignable(sourceType, targetType)) {
            relations.add(relation(sourceTypeName, targetTypeName));
        }
    }

    private Set<String> expectedSubTypeRelations(String filePath) throws IOException {
        Set<String> relations = new HashSet<>();
        List<String> lines = Files.readAllLines(resolvePath(filePath));
        for (String line : lines) {
            // This check can be changed based on other notations.
            if (line.startsWith(COMMENT) && line.contains(SUBTYPE_SYMBOL)) {
                relations.add(line.substring(3));
            }
        }
        return relations;
    }

    private Path resolvePath(String filepath) {
        return Paths.get("src/test/resources").resolve(filepath);
    }

    private String relation(String subType, String superType) {
        return subType + SUBTYPE_SYMBOL + superType;
    }
}
