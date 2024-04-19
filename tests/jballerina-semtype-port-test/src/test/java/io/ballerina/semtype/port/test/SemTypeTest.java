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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.semtype.port.test;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.ValueComparisonUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.types.Context;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.test.BCompileUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
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
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test semtypes using compiler front-end for parsing.
 *
 * @since 2201.10.0
 */
public class SemTypeTest {

    @DataProvider(name = "dataDirFileNameProvider")
    public Object[] dataDirFileNameProvider() {
        File dataDir = resolvePath("test-src/data").toFile();
        List<String> testFiles = Arrays.stream(dataDir.listFiles())
                .map(File::getAbsolutePath)
                .filter(name -> name.endsWith(".bal") &&
                        !dataDirSkipList().contains(name.substring(name.lastIndexOf(File.separator) + 1)))
                .collect(Collectors.toList());

        include(testFiles,
                "test-src/simple-type/float-altered.bal",
                // "test-src/simple-type/function-altered.bal", // func type not supported yet
                "test-src/simple-type/int-singleton-altered.bal",
                "test-src/simple-type/list-type-test.bal",
                "test-src/simple-type/map-type-test.bal",
                "test-src/simple-type/type-test.bal"
        );

        return testFiles.toArray(new String[0]);
        //return new Object[]{"test-src/data/error2.bal"};
    }

    public final HashSet<String> dataDirSkipList() {
        HashSet<String> hashSet = new HashSet<>();
        // error type not supported yet
        hashSet.add("basic.bal");
        hashSet.add("error1.bal");
        hashSet.add("error2.bal");

        // readonly type not supported yet
        hashSet.add("fixed-length-array.bal");
        hashSet.add("fixed-length-array-tuple.bal");

        // causes a stack overflow with mappingSubtypeIsEmpty
        hashSet.add("hard.bal");
        // func type not supported yet
        hashSet.add("function.bal"); // due to https://github.com/ballerina-platform/ballerina-lang/issues/35204
        hashSet.add("never.bal");

        // readonly type not supported yet
        hashSet.add("readonly1.bal");
        hashSet.add("readonly2.bal");

        // table type not supported yet
        hashSet.add("table.bal");
        return hashSet;
    }

    @DataProvider(name = "fileNameProviderFunc")
    public Object[] fileNameProviderFunc() {
        File dataDir = resolvePath("test-src/localVar").toFile();
        List<String> testFiles = Arrays.stream(dataDir.listFiles())
                .map(File::getAbsolutePath)
                .filter(name -> name.endsWith(".bal"))
                .toList();

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
            List<SemTypeAssertionTransformer.TypeAssertion> assertions = getTypeAssertions(file);
            tests.addAll(assertions);
        }
        return tests.toArray();
    }

    @NotNull
    private static List<SemTypeAssertionTransformer.TypeAssertion> getTypeAssertions(File file) {
        String fileName = file.getAbsolutePath();
        BCompileUtil.PackageSyntaxTreePair pair = BCompileUtil.compileSemType(fileName);
        BLangPackage pkgNode = pair.bLangPackage;

        List<BLangNode> typeAndClassDefs = new ArrayList<>();
        typeAndClassDefs.addAll(pkgNode.constants);
        typeAndClassDefs.addAll(pkgNode.typeDefinitions);
        SemTypeResolver typeResolver = new SemTypeResolver();
        Context typeCheckContext = Context.from(pkgNode.semtypeEnv);

        List<SemTypeAssertionTransformer.TypeAssertion> assertions;
        try {
            typeResolver.defineSemTypes(typeAndClassDefs, typeCheckContext);
            assertions = SemTypeAssertionTransformer.getTypeAssertionsFrom(fileName, pair.syntaxTree,
                    pkgNode.semtypeEnv);
        } catch (Exception e) {
            assertions = new ArrayList<>(List.of(new SemTypeAssertionTransformer.TypeAssertion(
                    null, fileName, null, null, null, e.getMessage()
            )));
        }
        return assertions;
    }

    public void listAllBalFiles(File file, List<File> balFiles) {
        if (file.isFile()) {
            return;
        }
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                listAllBalFiles(f, balFiles);
            }
            String fileName = f.getName();
            if (fileName.endsWith(".bal") && !typeRelDirSkipList().contains(fileName)) {
                balFiles.add(f);
            }
        }
    }

    public final HashSet<String> typeRelDirSkipList() {
        HashSet<String> hashSet = new HashSet<>();
        // intersection with negative (!) atom
        hashSet.add("proj7-t.bal");
        hashSet.add("proj8-t.bal");
        hashSet.add("proj9-t.bal");
        hashSet.add("proj10-t.bal");

        // readonly type not supported yet
        hashSet.add("fixed-length-array-t.bal");
        hashSet.add("fixed-length-array-tuple-t.bal");

        hashSet.add("xml-complex-ro-tv.bal");
        hashSet.add("xml-readonly-tv.bal");
        hashSet.add("xml-te.bal");

        // table type not supported yet
        hashSet.add("anydata-tv.bal");
        hashSet.add("table-t.bal");
        hashSet.add("table2-t.bal");
        return hashSet;
    }

    private void include(List<String> testFiles, String... fileNames) {
        for (int i = 0; i < fileNames.length; i++) {
            testFiles.add(i, fileNames[i]);
        }
    }

    @Test(dataProvider = "dataDirFileNameProvider")
    public void verifyAllSubtypeRelationships(String fileName) {
        List<String> subtypeRels = getSubtypeRels(fileName);
        List<String> expectedRels = extractSubtypeRelations(fileName);
        // Commented code will get expected content for this test to pass.
        // Useful for taking a diff.
        // String text = toText(subtypeRels);
        Assert.assertEquals(subtypeRels, expectedRels);
    }

    @Test(dataProvider = "fileNameProviderFunc")
    public void funcTest(String fileName) {
        BCompileUtil.PackageSyntaxTreePair packageSyntaxTreePair = BCompileUtil.compileSemType(fileName);
        BLangPackage bLangPackage = packageSyntaxTreePair.bLangPackage;
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

    @Test(expectedExceptions = AssertionError.class)
    public void shouldFailForIncorrectTestStructure() {
        File wrongAssertionFile = resolvePath("test-src/type-rel-wrong.bal").toFile();
        List<SemTypeAssertionTransformer.TypeAssertion> typeAssertions = getTypeAssertions(wrongAssertionFile);
        testSemTypeAssertions(typeAssertions.get(0));
    }

    @Test(dataProvider = "type-rel-provider")
    public void testSemTypeAssertions(SemTypeAssertionTransformer.TypeAssertion typeAssertion) {
        if (typeAssertion.kind() == null) {
            Assert.fail(
                    "Exception thrown in " + typeAssertion.fileName() + System.lineSeparator() + typeAssertion.text());
        }

        switch (typeAssertion.kind()) {
            case NON:
                Assert.assertFalse(
                        SemTypes.isSubtype(typeAssertion.context(), typeAssertion.lhs(), typeAssertion.rhs()),
                                   formatFailingAssertionDescription(typeAssertion));
                Assert.assertFalse(
                        SemTypes.isSubtype(typeAssertion.context(), typeAssertion.rhs(), typeAssertion.lhs()),
                                   formatFailingAssertionDescription(typeAssertion));
                break;
            case SUB:
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context(), typeAssertion.lhs(), typeAssertion.rhs()),
                                  formatFailingAssertionDescription(typeAssertion));
                Assert.assertFalse(
                        SemTypes.isSubtype(typeAssertion.context(), typeAssertion.rhs(), typeAssertion.lhs()),
                        formatFailingAssertionDescription(typeAssertion));
                break;
            case SAME:
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context(), typeAssertion.lhs(), typeAssertion.rhs()),
                                  formatFailingAssertionDescription(typeAssertion));
                Assert.assertTrue(SemTypes.isSubtype(typeAssertion.context(), typeAssertion.rhs(), typeAssertion.lhs()),
                                  formatFailingAssertionDescription(typeAssertion));
        }
    }

    @NotNull
    private String formatFailingAssertionDescription(SemTypeAssertionTransformer.TypeAssertion typeAssertion) {
        return typeAssertion.text() + "\n in: " + typeAssertion.fileName();
    }


    private String toText(List<String> expectedRels) {
        StringJoiner joiner = new StringJoiner("\n// ", "// ", "");
        for (String rel : expectedRels) {
            joiner.add(rel);
        }
        return joiner.toString();
    }

    private List<String> getSubtypeRels(String sourceFilePath) {
        BLangPackage pkgNode = BCompileUtil.compileSemType(sourceFilePath).bLangPackage;
        // xxxx-e.bal pattern is used to test bal files where jBallerina type checking doesn't support type operations
        // such as intersection. Make sure not to use nBallerina type negation (!) with this as jBallerina compiler
        // front end doesn't generate AST from those.
        if (!sourceFilePath.endsWith("-e.bal")) {
            ensureNoErrors(pkgNode);
        }

        List<BLangNode> typeAndClassDefs = new ArrayList<>();
        typeAndClassDefs.addAll(pkgNode.constants);
        typeAndClassDefs.addAll(pkgNode.typeDefinitions);

        SemTypeResolver typeResolver = new SemTypeResolver();
        Context typeCheckContext = Context.from(pkgNode.semtypeEnv);
        typeResolver.defineSemTypes(typeAndClassDefs, typeCheckContext);
        Map<String, SemType> typeMap = pkgNode.semtypeEnv.getTypeNameSemTypeMap();

        List<TypeRel> subtypeRelations = new ArrayList<>();
        List<String> typeNameList = typeMap.keySet().stream()
                .filter(n -> !n.startsWith("$anon"))
                .sorted(SemTypeTest::ballerinaStringCompare)
                .toList();
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
                .toList();
        if (!errors.isEmpty()) {
            Assert.fail(errors.stream()
                            .map(Diagnostic::toString)
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
     * @param subType   subtype name
     * @param superType super type name
     * @since 2201.10.0
     */
    private record TypeRel(String subType, String superType) {

        public static TypeRel rel(String sub, String sup) {
            return new TypeRel(sub, sup);
        }

        @Override
        public String toString() {
            return subType + "<:" + superType;
        }
    }
}
