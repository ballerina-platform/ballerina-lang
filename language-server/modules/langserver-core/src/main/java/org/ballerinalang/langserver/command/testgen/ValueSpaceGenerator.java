/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.testgen;

import org.apache.commons.lang3.RandomStringUtils;
import org.ballerinalang.langserver.command.testgen.TestGenerator.TestFunctionGenerator;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class is responsible for generating value spaces for a given length.
 *
 * @since 0.985.0
 */
public class ValueSpaceGenerator {
    public static final String PLACE_HOLDER = "{%1}";
    public static final int RANDOM_WORDS_LENGTH = 3;

    /**
     * Returns a value space for a given BLangNode.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bLangNode       BLangNode to evaluate
     * @param template        templates to be modified
     * @return {@link String}  modified templates
     * @see #createTemplateArray(int)
     */
    public static String[] getValueSpaceByNode(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                               BLangNode bLangNode, String[] template) {
        if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
            // Check for tuple assignment eg. (int, int)
            List<BLangExpression> varRefs = ((BLangTupleDestructure) bLangNode).getVariableRefs();
            String[][] list = new String[varRefs.size()][template.length];
            IntStream.range(0, varRefs.size()).forEach(j -> {
                BLangExpression bLangExpression = varRefs.get(j);
                if (bLangExpression.type != null) {
                    String[] values = getValueSpaceByType(importsAcceptor, currentPkgId, bLangExpression.type,
                                                          createTemplateArray(template.length));
                    IntStream.range(0, values.length).forEach(i -> list[i][j] = values[i]);
                }
            });
            IntStream.range(0, template.length).forEach(index -> {
                template[index] = template[index].replace(PLACE_HOLDER, "(" + String.join(", ", list[index]) + ")");
            });
            return template;
        } else if (bLangNode instanceof BLangLiteral) {
            return (String[]) Stream.of(template).map(
                    s -> s.replace(PLACE_HOLDER, ((BLangLiteral) bLangNode).getValue().toString())).toArray();
        } else if (bLangNode instanceof BLangAssignment) {
            return (String[]) Stream.of(template).map(s -> s.replace(PLACE_HOLDER, "0")).toArray();
        } else if (bLangNode instanceof BLangFunctionTypeNode) {
            BLangFunctionTypeNode funcType = (BLangFunctionTypeNode) bLangNode;
            TestFunctionGenerator generator = new TestFunctionGenerator(importsAcceptor, currentPkgId, funcType);
            StringJoiner params = new StringJoiner(", ");
            String[][] valueSpace = generator.getValueSpace();
            String[] typeSpace = generator.getTypeSpace();
            String[] nameSpace = generator.getNamesSpace();
            IntStream.range(0, typeSpace.length - 1).forEach(index -> {
                String type = typeSpace[index];
                String name = nameSpace[index];
                params.add(type + " " + name);
            });
            String returnType = "(" + typeSpace[typeSpace.length - 1] + ")";
            IntStream.range(0, template.length).forEach(index -> {
                String builder = "function (" + params.toString() + ") returns " + returnType + "{ " +
                        ((valueSpace != null) ? "return " + valueSpace[index][funcType.params.size()] : "") + "; }";
                template[index] = template[index].replace(PLACE_HOLDER, builder);
            });
            return template;
        }
        return (bLangNode.type != null) ? getValueSpaceByType(importsAcceptor, currentPkgId, bLangNode.type, template) :
                template;
    }

    /**
     * Returns value space for a given BType.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bType           BType to evaluate
     * @param template        templates to be modified
     * @return {@link String}  modified templates
     */
    public static String[] getValueSpaceByType(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                               BType bType, String[] template) {
        if ((bType.tsymbol == null || bType.tsymbol.name.value.isEmpty()) && bType instanceof BArrayType) {
            BArrayType bArrayType = (BArrayType) bType;
            String[] values = getValueSpaceByTypeSymbol(bArrayType.eType.tsymbol, createTemplateArray(template.length));
            IntStream.range(0, template.length).forEach(
                    index -> template[index] = template[index].replace(PLACE_HOLDER, "[" + values[index] + "]")
            );
            return template;
        } else if (bType instanceof BFiniteType) {
            // Check for finite set assignment
            BFiniteType bFiniteType = (BFiniteType) bType;
            Set<BLangExpression> valueSpace = bFiniteType.getValueSpace();
            if (!valueSpace.isEmpty()) {
                return getValueSpaceByNode(importsAcceptor, currentPkgId, valueSpace.stream().findFirst().get(),
                                           template);
            }
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BType constraintType = ((BMapType) bType).constraint;
            String[] values = getValueSpaceByType(importsAcceptor, currentPkgId, constraintType,
                                                  createTemplateArray(template.length));
            IntStream.range(0, template.length).forEach(index -> {
                template[index] = template[index].replace(PLACE_HOLDER, "{key: " + values[index] + "}");
            });
            return template;
        } else if (bType instanceof BUnionType) {
            // Check for union assignment int|string
            BUnionType bUnionType = (BUnionType) bType;
            Set<BType> memberTypes = bUnionType.getMemberTypes();
            if (!memberTypes.isEmpty()) {
                return getValueSpaceByType(importsAcceptor, currentPkgId, memberTypes.stream().findFirst().get(),
                                           template);
            }
        } else if (bType instanceof BTupleType) {
            // Check for tuple assignment (int, string)
            BTupleType bTupleType = (BTupleType) bType;
            List<BType> tupleTypes = bTupleType.tupleTypes;
            String[][] vSpace = new String[template.length][tupleTypes.size()];
            IntStream.range(0, tupleTypes.size()).forEach(j -> {
                BType type = tupleTypes.get(j);
                String[] values = getValueSpaceByType(importsAcceptor, currentPkgId, type,
                                                      createTemplateArray(template.length));
                IntStream.range(0, values.length).forEach(i -> vSpace[i][j] = values[i]);
            });
            IntStream.range(0, template.length).forEach(index -> {
                template[index] = template[index].replace(PLACE_HOLDER, "(" + String.join(", ", vSpace[index]) + ")");
            });
            return template;
        } else if (bType instanceof BRecordType) {
            BRecordType bRecordType = (BRecordType) bType;
            List<BField> params = bRecordType.fields;
            String[][] list = new String[template.length][params.size()];
            IntStream.range(0, params.size()).forEach(paramIndex -> {
                BField field = params.get(paramIndex);
                String[] values = getValueSpaceByType(importsAcceptor, currentPkgId, field.type,
                                                      createTemplateArray(template.length));
                IntStream.range(0, values.length).forEach(valIndex -> {
                    list[valIndex][paramIndex] = field.name + ": " + values[valIndex];
                });
            });

            IntStream.range(0, template.length).forEach(index -> {
                String paramsStr = String.join(", ", list[index]);
                String newObjStr = "{" + paramsStr + "}";
                template[index] = template[index].replace(PLACE_HOLDER, newObjStr);
            });
            return template;
        } else if (bType instanceof BObjectType && ((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol bStructSymbol = (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
            List<BVarSymbol> params = bStructSymbol.initializerFunc.symbol.params;
            String[][] list = new String[template.length][params.size()];
            IntStream.range(0, params.size()).forEach(paramIndex -> {
                BVarSymbol param = params.get(paramIndex);
                String[] values = getValueSpaceByType(importsAcceptor, currentPkgId, param.type,
                                                      createTemplateArray(template.length));
                IntStream.range(0, values.length).forEach(valIndex -> {
                    list[valIndex][paramIndex] = values[valIndex];
                });
            });

            IntStream.range(0, template.length).forEach(index -> {
                String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, currentPkgId, bStructSymbol.pkgID);
                String paramsStr = String.join(", ", list[index]);
                String newObjStr = "new " + pkgPrefix + bStructSymbol.name.getValue() + "(" + paramsStr + ")";
                template[index] = template[index].replace(PLACE_HOLDER, newObjStr);
            });
            return template;
        } else if (bType instanceof BInvokableType) {
            BInvokableType invokableType = (BInvokableType) bType;
            TestFunctionGenerator generator = new TestFunctionGenerator(importsAcceptor, currentPkgId, invokableType);
            StringJoiner params = new StringJoiner(", ");
            String[][] valueSpace = generator.getValueSpace();
            String[] typeSpace = generator.getTypeSpace();
            String[] nameSpace = generator.getNamesSpace();
            IntStream.range(0, typeSpace.length - 1).forEach(index -> {
                String type = typeSpace[index];
                String name = nameSpace[index];
                params.add(type + " " + name);
            });
            String returnType = "(" + typeSpace[typeSpace.length - 1] + ")";
            IntStream.range(0, template.length).forEach(index -> {
                String functionStr = "function (" + params.toString() + ") returns " + returnType + "{ " +
                        ((valueSpace != null) ? "return " + valueSpace[index][invokableType.paramTypes.size()] : "") +
                        "; }";
                template[index] = template[index].replace(PLACE_HOLDER, functionStr);
            });
            return template;
        }
        return (bType.tsymbol != null) ? getValueSpaceByTypeSymbol(bType.tsymbol, template) :
                (String[]) Stream.of(template).map(s -> s.replace(PLACE_HOLDER, "()")).toArray();
    }

    /**
     * Returns value space for a given BTypeSymbol.
     *
     * @param tSymbol  BTypeSymbol to evaluate
     * @param template templates to be modified
     * @return {@link String}  modified templates
     */
    public static String[] getValueSpaceByTypeSymbol(BTypeSymbol tSymbol, String[] template) {
        if (tSymbol == null) {
            String[] result = new String[]{"()"};
            populateValueSpace(result, template, () -> "()");
            return template;
        }
        Random random = new Random();
        switch (tSymbol.name.getValue()) {
            case "int":
            case "any":
                String[] resultInt = new String[]{"-1", "0", "1"};
                populateValueSpace(resultInt, template,
                                   () -> String.valueOf(random.nextInt((template.length))));
                break;
            case "string":
                String[] resultStr = new String[]{"\"\"", "\"foo\"", "\"bar\""};
                populateValueSpace(resultStr, template,
                                   () -> "\"" + RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH) + "\"");
                break;
            case "float":
                String[] resultFloat = new String[]{"-1.0", "0.0", "1.0"};
                populateValueSpace(resultFloat, template, () -> String.format("%.1f", random.nextDouble()));
                break;
            case "json":
                String[] resultJson = new String[]{"{\"key\": \"value\"}", "{}", "{\"foo\": \"bar\"}"};
                populateValueSpace(resultJson, template, () -> {
                    String key = RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH);
                    String value = RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH);
                    return "{\"" + key + "\": \"" + value + "\"}";
                });
                break;
            case "boolean":
                String[] resultBool = new String[]{"false", "true"};
                populateValueSpace(resultBool, template, () -> String.valueOf(random.nextBoolean()));
                break;
            case "xml":
                String[] resultXml =
                        new String[]{"xml `<msg>hello</msg>`", "xml `<foo>bar</foo>`", "xml `<bar>foo</bar>`"};
                populateValueSpace(resultXml, template, () -> {
                    String key = RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH);
                    String value = RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH);
                    return "xml `<" + key + ">" + value + "</" + key + ">`";
                });
                break;
            case "byte":
                String[] resultBlob = new String[]{"0"};
                populateValueSpace(resultBlob, template, () -> String.valueOf("0"));
                break;
            case "error":
                String[] resultErr = new String[]{"error(\"\")"};
                String msg = RandomStringUtils.randomAlphabetic(RANDOM_WORDS_LENGTH);
                populateValueSpace(resultErr, template, () -> "error(\"" + msg + "\")");
                break;
            default:
                String[] result = new String[]{"()"};
                populateValueSpace(result, template, () -> "()");
                break;
        }
        return template;
    }

    /**
     * Populates defined space into the templates. Random value generator will be used for the spare values.
     *
     * @param definedSpace defined value space
     * @param template     templates set
     * @param random       random value generator
     */
    public static void populateValueSpace(String[] definedSpace, String[] template,
                                          Supplier<String> random) {
        // Populate defined values
        int min = Math.min(definedSpace.length, template.length);
        IntStream.range(0, min).forEach(i -> template[i] = template[i].replace(PLACE_HOLDER, definedSpace[i]));
        // Populate random values
        int max = Math.max(min, template.length);
        IntStream.range(0, max).forEach(index -> template[index] = template[index].replace(PLACE_HOLDER, random.get()));
    }

    /**
     * Returns a set of templates pre-populated with placeholders.
     *
     * @param length length of the templates set
     * @return pre-populated templates
     */
    public static String[] createTemplateArray(int length) {
        String[] templates = new String[length];
        Arrays.fill(templates, PLACE_HOLDER);
        return templates;
    }
}
