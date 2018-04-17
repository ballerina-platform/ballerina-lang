/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
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
package org.ballerinalang.bre.bvm.persistency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableRefType;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.siddhi.query.api.expression.condition.In;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class PersistenceUtils {
    public static boolean reloaded = false;

    private static boolean initialized = false;

    private static List<Class> serializableClasses = new ArrayList<>();

    public synchronized static void init() {
        if (initialized) {
            return;
        }
        serializableClasses.add(String.class);
        serializableClasses.add(Integer.class);
        serializableClasses.add(Long.class);
        serializableClasses.add(Double.class);
        serializableClasses.add(Float.class);
        serializableClasses.add(Boolean.class);

        serializableClasses.add(InetSocketAddress.class);

        initialized = true;
    }

    public static boolean isSerializable(Object o) {
        if (o == null) {
            return true;
        }
        if (serializableClasses.contains(o.getClass())) {
            return true;
        }
        return false;
    }

    public static boolean isPrimitiveType(Object v) {
        if (v instanceof String ||
                v instanceof Integer ||
                v instanceof Long ||
                v instanceof Double ||
                v instanceof Float ||
                v instanceof Boolean) {
            return true;
        }
        return false;
    }

    public static String getJson(WorkerExecutionContext ctx) {
        return "{}";
    }

    public static void saveJsonFIle(String jsonStr, File file) {
        //        File f = new File("test.json");
        //        if (!f.exists()) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(jsonStr);
            out.close();
        } catch (FileNotFoundException e) {
            //ignore
        }
    }

    public static SerializableRefType serializeRefType(BRefType refType, SerializableState state) {
        return null;
    }
}