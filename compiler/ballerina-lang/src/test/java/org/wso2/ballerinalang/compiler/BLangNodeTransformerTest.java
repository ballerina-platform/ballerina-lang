/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.ClassUtils;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * Node Transformer Tests for the {@link @BLangCompUnitGen} class.
 *
 * @since 1.3.0
 */
public class BLangNodeTransformerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BLangNodeTransformerTest.class);
    private static final JsonParser JSON_PARSER = new JsonParser();
    public static final Path RES_DIR = Paths.get("src/test/resources/node-tree/").toAbsolutePath();

    private CompilerContext context;
    private SourceDirectoryManager sourceDirectoryManager;
    private SourceDirectory sourceDirectory;
    private PackageLoader packageLoader;
    private final JsonParser jsonParser = new JsonParser();

    @BeforeClass
    public void init() {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(COMPILER_PHASE, CompilerPhase.COMPILER_PLUGIN.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());
        options.put(OFFLINE, String.valueOf(true));
        options.put(TEST_ENABLED, String.valueOf(true));
        options.put(SKIP_TESTS, String.valueOf(false));
        options.put(CompilerOptionName.PROJECT_DIR, RES_DIR.resolve("src").toString());

        this.context = context;
        this.sourceDirectoryManager = SourceDirectoryManager.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
        this.packageLoader = PackageLoader.getInstance(this.context);
    }

    @Test(dataProvider = "testTransformationTestProvider", enabled = false)
    public void testTransformation(String configName, String sourcePackage)
            throws IOException, IllegalAccessException {
        // Get expected result json
        JsonObject expJsonObj = fileContentAsObject(configName);

        // Get actual result json
        PackageID packageID = this.sourceDirectoryManager.getPackageID(sourcePackage);
        BLangPackage bLangPackage = this.packageLoader.loadEntryPackage(packageID, null, new EmptyPrintStream());
        Set<Class<?>> skipList = new HashSet<>();
        String jsonStr = generateFieldJson(bLangPackage.getClass(), bLangPackage, skipList, bLangPackage);
        JsonObject actualJsonObj = jsonParser.parse(jsonStr).getAsJsonObject();

        // Fix test cases replacing expected using responses
//        if (!expJsonObj.equals(actualJsonObj)) {
//        java.nio.file.Files.write(RES_DIR.resolve(configName),
//                                  actualJsonObj.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
//        }
        Assert.assertEquals(actualJsonObj, expJsonObj);
    }

    @DataProvider
    public static Object[][] testTransformationTestProvider() {
        return new Object[][]{
                {"function-def.json", "function-def.bal"},
                {"basic-literals.json", "basic-literals.bal"},
                {"function-invoc.json", "function-invoc.bal"},
                {"records.json", "records.bal"},
                {"types.json", "types.bal"},
                {"stmt.json", "stmt.bal"},
                {"expr.json", "expr.bal"}
        };
    }

    private String generateFieldJson(Class<?> claz, Object object, Set<Class<?>> skipList,
                                     Object referenceNode) throws IllegalAccessException {
        return generateFieldJson(claz, object, skipList, new HashMap<>(), referenceNode, new int[]{1});
    }

    private String generateFieldJson(Class<?> claz, Object object, Set<Class<?>> skipList,
                                     Map<Object, String> visitedList,
                                     Object referenceNode, int[] level) throws IllegalAccessException {
        Field[] fields = claz.getFields();
        StringBuilder builder = new StringBuilder();
        StringJoiner joiner = new StringJoiner(",\n");
        String tabSpace = generateSpaces(level[0]);
        level[0]++;
        String tabSpaceInner = generateSpaces(level[0]);
        builder.append("{\n");
        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers())) {
                // Skip, non-public
                continue;
            }

            if (skipList.contains(field.getDeclaringClass())) {
                // Skip, from the skip-list
//                joiner.add(tabSpaceInner + "\"" + field.getName() + "\": \"<Skipped-FrmList>\"");
            } else if (field.getDeclaringClass().equals(field.getType())) {
                // Skip, fields with declaring class' type
//                joiner.add(tabSpaceInner + "\"" + field.getName() + "\": \"<Skipped-SC>\"");
            } else if (field.getType().isEnum()) {
                // Enum classes
                if (field.get(object) != null) {
                    joiner.add(tabSpaceInner + "\"" + field.getName() + "\": \"" + field.get(object).toString() + "\"");
                }
            } else {
                StringBuilder sBuilder = new StringBuilder("\"" + field.getName() + "\": ");
                String val = generateFieldValueJson(field.getType(), field.get(object), skipList, visitedList, sBuilder,
                                                    referenceNode, level);
                joiner.add(tabSpaceInner + val);
            }
        }
        builder.append(joiner.toString()).append("\n").append(tabSpace).append("}");
        level[0]--;
        return builder.toString();
    }

    private String generateFieldValueJson(Class<?> type, Object objVal,
                                          Set<Class<?>> skipList, Map<Object, String> visitedList,
                                          StringBuilder builder, Object referenceNode, int[] level)
            throws IllegalAccessException {
        if (handleCollections(type, objVal, skipList, visitedList, builder, referenceNode, level)) {
            return builder.toString();
        }
        if (objVal == null) {
            // Value is NULL
            builder.append("null");
            return builder.toString();
        } else if (ClassUtils.isPrimitiveOrWrapper(objVal.getClass()) || objVal instanceof String) {
            // Value is a builtin type
            if (objVal instanceof String) {
                builder.append("\"");
                builder.append(objVal.toString().replaceAll("\"", "\\\\\""));
                builder.append("\"");
            } else {
                builder.append(objVal);
            }
            return builder.toString();
        } else {
            if (visitedList.containsKey(objVal)) {
                builder.append(visitedList.get(objVal));
            } else {
                level[0]++;
                visitedList.put(objVal, "\"<Duplicated>\""); // Mark pending for recursive calls
                String json = generateFieldJson(objVal.getClass(), objVal, skipList, visitedList, referenceNode, level);
                builder.append(json);
                level[0]--;
            }
            return builder.toString();
        }
    }

    private boolean handleCollections(Class<?> type, Object objVal, Set<Class<?>> skipList,
                                      Map<Object, String> visitedList, StringBuilder builder, Object referenceNode,
                                      int[] level) throws IllegalAccessException {
        if (objVal instanceof Map) {
            builder.append("{");
            Iterator<Map.Entry> entries = ((Map) objVal).entrySet().iterator();
            StringJoiner joiner = new StringJoiner(",");
            while (entries.hasNext()) {
                Map.Entry entry = entries.next();
                StringBuilder sBuilder = new StringBuilder("\"" + entry.getKey() + "\": ");
                joiner.add(generateFieldValueJson(entry.getValue().getClass(), entry.getValue(), skipList, visitedList,
                                                  sBuilder, referenceNode, level));
            }
            builder.append(joiner.toString());
            builder.append("}");
            return true;
        } else if (objVal instanceof Collection) {
            // Lists, Sets
            builder.append("[");
            StringJoiner joiner = new StringJoiner(",");
            for (Object item : (Collection) objVal) {
                joiner.add(generateFieldValueJson(item.getClass(), item, skipList, visitedList, new StringBuilder(""),
                                                  referenceNode, level));
            }
            builder.append(joiner.toString());
            builder.append("]");
            return true;
        } else if (type.isArray()) {
            // Array
            builder.append("[");
            StringJoiner joiner = new StringJoiner(",");
            if (objVal != null) {
                // Convert primitive type arrays
                Object[] objArr;
                if (objVal instanceof Object[]) {
                    objArr = (Object[]) objVal;
                } else {
                    int arrLength = Array.getLength(objVal);
                    Object[] outputArray = new Object[arrLength];
                    for (int i = 0; i < arrLength; ++i) {
                        outputArray[i] = Array.get(objVal, i);
                    }
                    objArr = outputArray;
                }
                for (Object next : objArr) {
                    joiner.add(generateFieldValueJson(next.getClass(), next, skipList, visitedList,
                                                      new StringBuilder(""), referenceNode, level));
                }
            }
            builder.append(joiner.toString());
            builder.append("]");
            return true;
        }
        return false;
    }

    private String generateSpaces(int count) {
        return new String(new char[count * 2]).replace('\0', ' ');
    }

    /**
     * Get the file content.
     *
     * @param filePath path to the file
     * @return {@link JsonObject} file content as a jsonObject
     */
    private static JsonObject fileContentAsObject(String filePath) {
        String contentAsString = "";
        try {
            contentAsString = new String(Files.readAllBytes(RES_DIR.resolve(filePath)));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return JSON_PARSER.parse(contentAsString).getAsJsonObject();
    }

    /**
     * Represents an empty print stream to avoid writing to the standard print stream.
     */
    private static class EmptyPrintStream extends PrintStream {
        public EmptyPrintStream() throws UnsupportedEncodingException {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }, true, "UTF-8");
        }
    }
}
