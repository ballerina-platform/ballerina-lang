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
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BJSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class PersistenceUtils {
    public static boolean reloaded = false;

    public static BJSON generateJsonContext(WorkerExecutionContext ctx) {
        //prepare serializable context
        SerializableExecutionContext serializableContext = new SerializableExecutionContext();
        serializableContext.setIp(ctx.ip);
        serializableContext.setLocalProps(ctx.localProps);
        serializableContext.setRetRegIndexes(ctx.retRegIndexes);
        serializableContext.setRunInCaller(ctx.runInCaller);
        SerializableWorkerData workerdata = new SerializableWorkerData();
        //set workerLocal
        if (ctx.workerLocal != null) {
            workerdata.byteRegs = ctx.workerLocal.byteRegs;
            workerdata.doubleRegs = ctx.workerLocal.doubleRegs;
            workerdata.intRegs = ctx.workerLocal.intRegs;
            workerdata.longRegs = ctx.workerLocal.longRegs;
            workerdata.stringRegs = ctx.workerLocal.stringRegs;
            serializableContext.setWorkerLocal(workerdata);
        }

        //set workerResult
        if (ctx.workerResult != null) {
            workerdata.byteRegs = ctx.workerResult.byteRegs;
            workerdata.doubleRegs = ctx.workerResult.doubleRegs;
            workerdata.intRegs = ctx.workerResult.intRegs;
            workerdata.longRegs = ctx.workerResult.longRegs;
            workerdata.stringRegs = ctx.workerResult.stringRegs;
            serializableContext.setWorkerResult(workerdata);
        }
        //        serializableContext.setState(ctx.state);
        //        serializableContext.setWorkerLocal(ctx.workerLocal);
        //        serializableContext.setWorkerResult(ctx.workerResult);

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(serializableContext);

        //        System.out.println("single lement: " + json );
        BJSON ctxJson = new BJSON(json);

        if (ctx.parent != null) {
            JSONUtils.setElement(ctxJson, "parent", generateJsonContext(ctx.parent));
        }

        return ctxJson;
    }

    public static String getJson(WorkerExecutionContext ctx) {
        SerializableExecutionContext serializableCtx = generateSerializableObj(ctx);
        Gson gson = new GsonBuilder().create();
        return gson.toJson(serializableCtx);
    }

    public static SerializableExecutionContext generateSerializableObj(WorkerExecutionContext ctx) {
        SerializableExecutionContext serializableContext = new SerializableExecutionContext();
        //        serializableContext.setCode(ctx.code);
        //        serializableContext.setConstPool(ctx.constPool);
        //        serializableContext.setGlobalProps(ctx.globalProps);
        serializableContext.setIp(ctx.ip);
        serializableContext.setLocalProps(ctx.localProps);
        serializableContext.setRetRegIndexes(ctx.retRegIndexes);
        serializableContext.setRunInCaller(ctx.runInCaller);

        //set workerLocal
        if (ctx.workerLocal != null) {
            SerializableWorkerData workerdata = new SerializableWorkerData();
            workerdata.byteRegs = ctx.workerLocal.byteRegs;
            workerdata.doubleRegs = ctx.workerLocal.doubleRegs;
            workerdata.intRegs = ctx.workerLocal.intRegs;
            workerdata.longRegs = ctx.workerLocal.longRegs;
            workerdata.stringRegs = ctx.workerLocal.stringRegs;
            serializableContext.setWorkerLocal(workerdata);
        }

        //set workerResult
        if (ctx.workerResult != null) {
            SerializableWorkerData resultData = new SerializableWorkerData();
            resultData.byteRegs = ctx.workerResult.byteRegs;
            resultData.doubleRegs = ctx.workerResult.doubleRegs;
            resultData.intRegs = ctx.workerResult.intRegs;
            resultData.longRegs = ctx.workerResult.longRegs;
            resultData.stringRegs = ctx.workerResult.stringRegs;
            serializableContext.setWorkerResult(resultData);
        }
        if (ctx.parent != null) {
            serializableContext.parent = generateSerializableObj(ctx.parent);
        }

        return serializableContext;
    }

    public static WorkerExecutionContext reloadContext(String jsonCtx, WorkerExecutionContext currentContext) {
        Gson gson = new GsonBuilder().create();
        SerializableExecutionContext serializableContext = gson.fromJson(jsonCtx, SerializableExecutionContext.class);

        return getContext(serializableContext, currentContext);

    }

    public static WorkerExecutionContext getContext(SerializableExecutionContext serializableContext,
            WorkerExecutionContext currentContext) {
        WorkerExecutionContext newCtx = currentContext;

        if (serializableContext.parent != null) {
            newCtx.parent = getContext(serializableContext.parent, currentContext.parent);
        } else {
            newCtx.parent = null;
        }
        if (newCtx.workerLocal != null) {
            newCtx.workerLocal.refRegs = currentContext.workerLocal.refRegs;
            newCtx.workerLocal.byteRegs = serializableContext.workerLocal.byteRegs;
            newCtx.workerLocal.doubleRegs = serializableContext.workerLocal.doubleRegs;
            newCtx.workerLocal.intRegs = serializableContext.workerLocal.intRegs;
            newCtx.workerLocal.longRegs = serializableContext.workerLocal.longRegs;
            newCtx.workerLocal.stringRegs = serializableContext.workerLocal.stringRegs;
        }

        if (newCtx.workerResult != null) {
            newCtx.workerResult.byteRegs = serializableContext.workerResult.byteRegs;
            newCtx.workerResult.doubleRegs = serializableContext.workerResult.doubleRegs;
            newCtx.workerResult.intRegs = serializableContext.workerResult.intRegs;
            newCtx.workerResult.longRegs = serializableContext.workerResult.longRegs;
            newCtx.workerResult.stringRegs = serializableContext.workerResult.stringRegs;
        }

        newCtx.state = serializableContext.state;
        //        newCtx.globalProps = serializableContext.globalProps;
        newCtx.localProps = serializableContext.localProps;
        newCtx.ip = serializableContext.ip;
        newCtx.retRegIndexes = serializableContext.retRegIndexes;
        newCtx.runInCaller = serializableContext.runInCaller;

        return newCtx;
    }

//    private static WorkerData getWorkerData(SerializableWorkerData serializableData) {
//        WorkerData workerData = new WorkerData();
//        workerData.byteRegs = serializableData.byteRegs;
//        workerData.doubleRegs = serializableData.doubleRegs;
//        workerData.intRegs = serializableData.intRegs;
//        workerData.longRegs = serializableData.longRegs;
//        workerData.stringRegs = serializableData.stringRegs;
//
//        return workerData;
//    }

    public static void saveJsonFIle(String jsonStr, String filename) {
        //        File f = new File("test.json");
        //        if (!f.exists()) {
        try (PrintWriter out = new PrintWriter(filename + ".json")) {
            out.println(jsonStr);
            out.close();
        } catch (FileNotFoundException e) {
            //ignore
        }
    }

    public static WorkerExecutionContext reloadContext(WorkerExecutionContext context, String messageName) {
        File f = new File(messageName + ".json");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            String json = bufferedReader.readLine();
            reloaded = true;
            return reloadContext(json, context);
        } catch (FileNotFoundException e) {
            //error
        } catch (IOException e) {
            //error
        }
        return null;
    }
}