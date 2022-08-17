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

import com.google.gson.reflect.TypeToken;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;

import com.google.gson.Gson;
import io.ballerina.runtime.internal.values.ArrayValueImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class RerunJson {

    private static final Type JSON_TYPE = new TypeToken<Map<String, String[]>>() {}.getType();
    private static final String TEST_NAMES_KEY = "testNames";
    private static final String SUB_TEST_NAMES_KEY = "subTestNames";

    public static void writeContent(BArray testNames, BArray subTestNames, BString targetPath) throws Exception {

        final Map<String, Object> map = new LinkedHashMap<>();
        map.put(TEST_NAMES_KEY, testNames.getStringArray());
        map.put(SUB_TEST_NAMES_KEY, subTestNames.getStringArray());

        File jsonFile = new File(Paths.get(targetPath.getValue()).toString());
        FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
        Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        String json = gson.toJson(map);
        writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        writer.flush();
    }

    public static BArray readContent(BString targetPath) throws Exception {

        Gson gson = new Gson();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(targetPath.getValue()), StandardCharsets.UTF_8);
        Map<String, String[]> map = gson.fromJson(bufferedReader, JSON_TYPE);
        BArray[] values = {StringUtils.fromStringArray(map.get(TEST_NAMES_KEY)),
                StringUtils.fromStringArray(map.get(SUB_TEST_NAMES_KEY))};
        return new ArrayValueImpl(values, PredefinedTypes.TYPE_JSON_ARRAY);
    }
}