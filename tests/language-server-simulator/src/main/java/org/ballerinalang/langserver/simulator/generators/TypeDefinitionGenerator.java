/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.simulator.generators;

import org.ballerinalang.annotation.JavaSPIService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Type definition code snippet generator.
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class TypeDefinitionGenerator extends CodeSnippetGenerator {

    private final List<String> generatedTypeDefNames = new ArrayList<>();
    private int typeCount = 0;

    @Override
    public String generate() {
        switch (random.nextInt(3)) {
            case 1:
                return generateUnionType();
            case 2:
                return generateRecordType();
            case 3:
            default:
                return generateObjectTypeDef();
        }
    }

    public String generateRecordType() {
        String typeName = "Rec" + typeCount;

        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 2 + random.nextInt(100); i++) {
            String field;
            if (random.nextBoolean() || generatedTypeDefNames.isEmpty()) {
                field = String.format("\t%s field%d;", primitiveTypes.get(random.nextInt(primitiveTypes.size())), i);
            } else {
                field = String.format("\t%s field%d;",
                        generatedTypeDefNames.get(random.nextInt(generatedTypeDefNames.size())), i);
            }
            fields.add(field);
        }

        typeCount++;
        generatedTypeDefNames.add(typeName);
        return String.format("%ntype %s {|%n%s%n|};%n", typeName, String.join("\n", fields));
    }

    public String generateUnionType() {
        // Member types
        Set<String> memberTypes = new HashSet<>();
        for (int i = 0; i < 2 + random.nextInt(3); i++) {
            String memberType;
            do {
                if (random.nextBoolean() || generatedTypeDefNames.isEmpty()) {
                    memberType = primitiveTypes.get(random.nextInt(primitiveTypes.size()));
                } else {
                    memberType = generatedTypeDefNames.get(random.nextInt(generatedTypeDefNames.size()));
                }
            } while (memberTypes.contains(memberType));
            memberTypes.add(memberType);
        }

        String typeName = "Type" + typeCount;
        typeCount++;
        generatedTypeDefNames.add(typeName);
        return String.format("%ntype %s %s;%n", typeName, String.join(" | ", memberTypes));
    }

    public String generateObjectTypeDef() {
        String typeName = "ObjectDef" + typeCount;
        typeCount++;
        generatedTypeDefNames.add(typeName);
        return "\ntype " + typeName + " object {\n" +
                "\n\tpublic function doSomething() returns UnkType;\n" +
                "};\n";
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.TYPE_DEFINITION;
    }
}
