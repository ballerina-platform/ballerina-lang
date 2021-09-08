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

import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test semtypes using compiler front-end for parsing.
 *
 * @since 2.0.0
 */
public class SemTypeTest {

    @DataProvider(name = "fileNameProvider")
    public Object[] fileNameProvider() {
        return new Object[]{
                "test-src/simple-type/type-test.bal",
                "test-src/simple-type/list-type-test.bal"
        };
    }

    @Test(dataProvider = "fileNameProvider")
    public void initialTest(String fileName) {
        List<String> subtypeRels = getSubtypeRels(fileName);
        List<String> expectedRels = extractSubtypeRelations(fileName);
        // String text = toText(subtypeRels);
        Assert.assertEquals(subtypeRels, expectedRels);
    }

    private String toText(List<String> expectedRels) {
        StringJoiner joiner = new StringJoiner("\n// ", "// ", "");
        for (String rel : expectedRels) {
            joiner.add(rel);
        }
        return joiner.toString();
    }

    private List<String> getSubtypeRels(String sourceFilePath) {
        BLangPackage bLangPackage = BCompileUtil.compileSemType(sourceFilePath);
        TypeCheckContext typeCheckContext = TypeCheckContext.from(bLangPackage.semtypeEnv);
        Map<String, SemType> typeMap = bLangPackage.semtypeEnv.geTypeNameSemTypeMap();

        List<TypeRel> subtypeRelations = new ArrayList<>();
        List<String> typeNameList = typeMap.keySet().stream().sorted().collect(Collectors.toList());
        int size = typeNameList.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                String name1 = typeNameList.get(i);
                String name2 = typeNameList.get(j);

                SemType t1 = typeMap.get(name1);
                SemType t2 = typeMap.get(name2);
                if (Core.isSubtype(typeCheckContext, t1, t2)) {
                    subtypeRelations.add(TypeRel.rel(name1, name2));
                }
                if (Core.isSubtype(typeCheckContext, t2, t1)) {
                    subtypeRelations.add(TypeRel.rel(name2, name1));
                }
            }
        }
        subtypeRelations.sort(Comparator.comparing(rel -> rel.subType + rel.superType));

        return subtypeRelations.stream().map(TypeRel::toString).collect(Collectors.toList());
    }

    List<String> extractSubtypeRelations(String fileName) {
        try {
            Path path = Paths.get("src/test/resources").resolve(fileName);
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
