/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.types;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.ValueComparisonUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BCompileUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test semtypes using compiler front-end for parsing.
 *
 * @since 3.0.0
 */
public class SemTypeTest {

    @DataProvider(name = "fileNameProvider")
    public Object[] fileNameProvider() {
        File dataDir = resolvePath("test-src/data").toFile();
        List<String> testFiles = Arrays.stream(dataDir.listFiles())
                .map(File::getAbsolutePath)
                .filter(name -> name.endsWith(".bal") &&
                        !skipList().contains(name.substring(name.lastIndexOf("/") + 1)))
                .collect(Collectors.toList());

        // blocked on https://github.com/ballerina-platform/ballerina-lang/issues/28334 and
        // https://github.com/ballerina-platform/ballerina-lang/issues/32722
        ignore(testFiles, "float-singleton2.bal");
        ignore(testFiles, "int-singleton.bal");
        // due to https://github.com/ballerina-platform/ballerina-lang/issues/35204
        ignore(testFiles, "function.bal");

        // due to https://github.com/ballerina-platform/ballerina-lang/issues/35203
        ignore(testFiles, "int-singleton2.bal");

        include(testFiles,
                "test-src/simple-type/type-test.bal",
               // "test-src/simple-type/list-type-test.bal",
               // "test-src/simple-type/map-type-test.bal",
               // due to https://github.com/ballerina-platform/ballerina-lang/issues/35203
               // "test-src/simple-type/int-singleton-altered.bal",
               // "test-src/simple-type/function-altered.bal",
                "test-src/simple-type/float-altered.bal");

        return testFiles.toArray(new String[0]);
        //return new Object[]{"test-src/data/error2.bal"};
    }

    public final HashSet<String> skipList() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("error2.bal");
        hashSet.add("readonly1.bal");
        hashSet.add("xml-sequence.bal");
        hashSet.add("list-fixed.bal");
        hashSet.add("xml.bal");
        hashSet.add("contextual.bal");
        hashSet.add("list-type-test.bal");
        hashSet.add("fixed-length-array-2-3-e.bal");
        hashSet.add("fixed-length-array.bal");
        hashSet.add("fixed-length-array-2-2-e.bal");
        hashSet.add("int-subtype.bal");
        hashSet.add("basic.bal");
        hashSet.add("table.bal");
        hashSet.add("xml-never.bal");
        hashSet.add("bdddiff1.bal");
        hashSet.add("fixed-length-array-2-4-e.bal");
        hashSet.add("tuple1.bal");
        hashSet.add("fixed-length-array-2.bal");
        hashSet.add("map-type-test.bal");
        hashSet.add("function-altered.bal");
        hashSet.add("tuple4.bal");
        hashSet.add("never.bal");
        hashSet.add("xml-singleton.bal");
        hashSet.add("fixed-length-array-tuple.bal");
        hashSet.add("error1.bal");
        hashSet.add("readonly2.bal");
        return hashSet;
    }

    @DataProvider(name = "fileNameProviderFunc")
    public Object[] fileNameProviderFunc() {
        File dataDir = resolvePath("test-src/localVar").toFile();
        List<String> testFiles = Arrays.stream(dataDir.listFiles())
                .map(File::getAbsolutePath)
                .filter(name -> name.endsWith(".bal"))
                .collect(Collectors.toList());

        return testFiles.toArray(new String[0]);
    }

    @DataProvider(name = "type-rel-provider")
    public Object[] typeRelTestFileNameProvider() {
        File dataDir = resolvePath("test-src/type-rel").toFile();
        List<File> balFiles = new ArrayList<>();
        listAllBalFiles(dataDir, balFiles);
        Collections.sort(balFiles);

        List<SemTypeAssertionTransformer.TypeAssertion> tests = new ArrayList<>();
        for (File file : balFiles) {
            String fileName = file.getAbsolutePath();
            BCompileUtil.PackageSyntaxTreePair pair = BCompileUtil.compileSemType(fileName);
            List<SemTypeAssertionTransformer.TypeAssertion> assertions = SemTypeAssertionTransformer
                    .getTypeAssertionsFrom(fileName, pair.syntaxTree, pair.bLangPackage.semtypeEnv);
            tests.addAll(assertions);
        }
        return tests.toArray();
    }

    public void listAllBalFiles(File file, List<File> balFiles) {
        if (file.isFile()) {
            return;
        }
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                listAllBalFiles(f, balFiles);
            }
            if (f.getName().endsWith(".bal")) {
                balFiles.add(f);
            }
        }
    }

    private void include(List<String> testFiles, String... fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            testFiles.add(i, fileNames[i]);
        }
    }

    private void ignore(List<String> testFiles, String fileName) {
        int index = -1;
        for (int i = 0; i < testFiles.size(); i++) {
            if (testFiles.get(i).endsWith(fileName)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            testFiles.remove(index);
        }
    }

    @Test(dataProvider = "fileNameProvider")
    public void initialTest(String fileName) {
        List<String> subtypeRels = getSubtypeRels(fileName);
        List<String> expectedRels = extractSubtypeRelations(fileName);
        // Commented code will get expected content for this test to pass.
        // Useful for taking a diff.
        //String text = toText(subtypeRels);
        Assert.assertEquals(subtypeRels, expectedRels);
    }

    @Test(dataProvider = "fileNameProviderFunc")
    public void funcTest(String fileName) {
        BCompileUtil.PackageSyntaxTreePair packageSyntaxTreePair = BCompileUtil.compileSemType(fileName);
        BLangPackage bLangPackage = packageSyntaxTreePair.bLangPackage;;
        ensureNoErrors(bLangPackage);
        List<String[]> vars = extractVarTypes(fileName);
        Context tc = Context.from(bLangPackage.semtypeEnv);
        Scope globalScope = bLangPackage.symbol.scope;
        bLangPackage.functions.forEach(func -> {
            Scope scope = func.getBody().scope;
            vars.forEach(v -> {
                if (v.length != 2) {
                    Assert.fail("test structure should be `variable = Type`");
                }
                SemType t1 = scope.lookup(new Name(v[0])).symbol.type.semType();
                SemType t2 = globalScope.lookup(new Name(v[1])).symbol.type.semType();

                String msg = "semtype in local scope is different from global scope";
                Assert.assertTrue(SemTypes.isSubtype(tc, t1, t2), msg);
                Assert.assertTrue(SemTypes.isSubtype(tc, t2, t1), msg);
            });
        });
    }

    @Test(dataProvider = "type-rel-provider")
    public void testSemTypeAssertions(SemTypeAssertionTransformer.TypeAssertion typeAssertion) {
        switch (typeAssertion.kind) {
            case NON:
                Assert.assertFalse(SemTypes.isSubtype(typeAssertion.context, typeAssertion.lhs, typeAssertion.rhs),
                                   formatFailingAssertionDescription(typeAssertion));
                Assert.assertFalse(SemTypes.isSubtype(typeAssertion.context, typeAssertion.rhs, typeAssertion.lhs),
                                   formatFailingAssertionDescription(typeAssertion));
                break;
            case SUB:
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context, typeAssertion.lhs, typeAssertion.rhs),
                                  formatFailingAssertionDescription(typeAssertion));
                Assert.assertFalse(SemTypes.isSubtype(typeAssertion.context, typeAssertion.rhs, typeAssertion.lhs),
                        formatFailingAssertionDescription(typeAssertion));
                break;
            case SAME:
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context, typeAssertion.lhs, typeAssertion.rhs),
                                  formatFailingAssertionDescription(typeAssertion));
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context, typeAssertion.rhs, typeAssertion.lhs),
                                  formatFailingAssertionDescription(typeAssertion));
        }
    }

    @NotNull
    private String formatFailingAssertionDescription(SemTypeAssertionTransformer.TypeAssertion typeAssertion) {
        return typeAssertion.text + "\n in: " + typeAssertion.file;
    }


    private String toText(List<String> expectedRels) {
        StringJoiner joiner = new StringJoiner("\n// ", "// ", "");
        for (String rel : expectedRels) {
            joiner.add(rel);
        }
        return joiner.toString();
    }

    private List<String> getSubtypeRels(String sourceFilePath) {
        BLangPackage bLangPackage = BCompileUtil.compileSemType(sourceFilePath).bLangPackage;
        // xxxx-e.bal pattern is used to test bal files where jBallerina type checking doesn't support type operations
        // such as intersection. Make sure not to use nBallerina type negation (!) with this as jBallerina compiler
        // front end doesn't generate AST from those.
        if (!sourceFilePath.endsWith("-e.bal")) {
            ensureNoErrors(bLangPackage);
        }
        Context typeCheckContext = Context.from(bLangPackage.semtypeEnv);

        // Map<String, SemType> typeMap = bLangPackage.semtypeEnv.getTypeNameSemTypeMap();
        // TODO: use above line instead of below, once sem-type resolving is done directly.

        Map<String, SemType> typeMap = new LinkedHashMap<>();
        List<BLangTypeDefinition> typeDefs = bLangPackage.typeDefinitions;
        for (BLangTypeDefinition typeDef : typeDefs) {
            SemType s = typeDef.getBType().semType();
            if (s != null) {
                typeMap.put(typeDef.name.value, s);
            }
        }

        List<TypeRel> subtypeRelations = new ArrayList<>();
        List<String> typeNameList = typeMap.keySet().stream()
                .filter(n -> !n.startsWith("$anon"))
                .sorted(SemTypeTest::ballerinaStringCompare)
                .collect(Collectors.toList());
        int size = typeNameList.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                String name1 = typeNameList.get(i);
                String name2 = typeNameList.get(j);

                SemType t1 = typeMap.get(name1);
                SemType t2 = typeMap.get(name2);
                if (SemTypes.isSubtype(typeCheckContext, t1, t2)) {
                    subtypeRelations.add(TypeRel.rel(name1, name2));
                }
                if (SemTypes.isSubtype(typeCheckContext, t2, t1)) {
                    subtypeRelations.add(TypeRel.rel(name2, name1));
                }
            }
        }

        return subtypeRelations.stream()
                .map(TypeRel::toString)
                .sorted(SemTypeTest::ballerinaStringCompare)
                .collect(Collectors.toList());
    }

    private void ensureNoErrors(BLangPackage bLangPackage) {
        List<Diagnostic> errors = bLangPackage.getDiagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity() == DiagnosticSeverity.ERROR)
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            Assert.fail(errors.stream()
                            .map(d -> d.toString())
                            .reduce("", (a, b) -> a + "\n" + b));
        }
    }

    private static int ballerinaStringCompare(String o1, String o2) {
        return ValueComparisonUtils.compareValues(StringUtils.fromString(o1), StringUtils.fromString(o2), "");
    }

    List<String> extractSubtypeRelations(String fileName) {
        try {
            Path path = resolvePath(fileName);
            Stream<String> lines = Files.lines(Path.of(path.toString()));
            return lines.filter(s -> s.startsWith("// ") && s.contains("<:"))
                    .map(s -> s.substring(3).strip())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        return null;
    }

    List<String[]> extractVarTypes(String fileName) {
        try {
            Path path = resolvePath(fileName);
            Stream<String> lines = Files.lines(Path.of(path.toString()));
            return lines.filter(s -> s.startsWith("// "))
                    .map(s -> s.substring(3).replaceAll("\\s", "").split("="))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Assert.fail(e.toString());
        }
        return null;
    }

    private Path resolvePath(String fileName) {
        return Paths.get("src/test/resources").resolve(fileName);
    }

    /**
     * Represent subtype relationship.
     *
     * @since 3.0.0
     */
    public static class TypeRel {
        public final String superType;
        public final String subType;

        public TypeRel(String subType, String superType) {
            this.superType = superType;
            this.subType = subType;
        }

        public static TypeRel rel(String sub, String sup) {
            return new TypeRel(sub, sup);
        }

        @Override
        public String toString() {
            return subType + "<:" + superType;
        }
    }
}
