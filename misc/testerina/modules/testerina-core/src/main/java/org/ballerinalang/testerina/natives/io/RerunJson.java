// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.testerina.natives.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class RerunJson {

    private static final Type JSON_TYPE = new TypeToken<Map<String, Map<String, String[]>>>() {
    }.getType();
    private static final String TEST_NAMES_KEY = "testNames";
    private static final String SUB_TEST_NAMES_KEY = "subTestNames";
    
    private static final String RERUN_JSON_FILE = "rerun_test.json";
    private static final String CACHE_DIRECTORY = "cache";

    public static void writeContent(BArray testNames, BArray subTestNames,
                                    BString targetPath, BString moduleName) throws Exception {

        final Map<String, String[]> moduleMap = new LinkedHashMap<>();
        moduleMap.put(TEST_NAMES_KEY, testNames.getStringArray());
        moduleMap.put(SUB_TEST_NAMES_KEY, subTestNames.getStringArray());

        Gson gson = new Gson();
        Path jsonPath = Paths.get(targetPath.getValue()).resolve(RERUN_JSON_FILE);
        Map<String, Map<String, String[]>> outerMap;
        if (Files.exists(jsonPath)) {
            BufferedReader bufferedReader = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8);
            outerMap = gson.fromJson(bufferedReader, JSON_TYPE);
        } else {
            outerMap = new LinkedHashMap<>();
        }
        outerMap.put(moduleName.getValue(), moduleMap);

        File jsonFile = new File(jsonPath.toString());
        FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
        Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        String json = gson.toJson(outerMap);
        writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        writer.flush();
    }

    public static BArray readContent(BString targetPath, BString moduleName) throws Exception {

        Gson gson = new Gson();
        Path jsonPath = Paths.get(targetPath.getValue()).resolve(RERUN_JSON_FILE);
        BufferedReader bufferedReader = Files.newBufferedReader(jsonPath, StandardCharsets.UTF_8);
        Map<String, Map<String, String[]>> outerMap = gson.fromJson(bufferedReader, JSON_TYPE);
        Map<String, String[]> moduleMap = outerMap.get(moduleName.getValue());
        BArray[] values = {StringUtils.fromStringArray(moduleMap.get(TEST_NAMES_KEY)),
                StringUtils.fromStringArray(moduleMap.get(SUB_TEST_NAMES_KEY))};
        return new ArrayValueImpl(values, PredefinedTypes.TYPE_JSON_ARRAY);
    }

}
