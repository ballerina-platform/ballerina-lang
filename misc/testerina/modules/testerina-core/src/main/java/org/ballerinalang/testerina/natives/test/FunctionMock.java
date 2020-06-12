package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.Executor;

public class FunctionMock {

    public static void call(ObjectValue caseObj) {
        System.out.println("[FunctionMock] (call) Call function Hit");

        ObjectValue mockFunctionObj = caseObj.getObjectValue(StringUtils.fromString("mockFunctionObj"));
        String mockFunction = caseObj.getStringValue(StringUtils.fromString("mockFunction")).toString();
        ArrayValue args = caseObj.getArrayValue(StringUtils.fromString("argList"));
        Object returnVal = caseObj.get(StringUtils.fromString("returnVal")); //This should be null


        System.out.println("[FunctionMock] (call) Extracted info from FunctionCase. Adding to Case Registry");

        MockRegistry.getInstance().registerCase(mockFunctionObj, mockFunction, args, returnVal);
        System.out.println("[FunctionMock] (call) Added to registry");
    }

    public static void setReturn(ObjectValue caseObj) {
        Object returnVal = caseObj.get(StringUtils.fromString("returnVal"));

        // Assign the return val to the case map

    }

    public static Object mockHandler(ObjectValue mockObj) {
        // Extract case Id info
        String caseId = mockObj.getStringValue(StringUtils.fromString("caseId")).toString();
        MockRegistry.getInstance().getReturnValue(caseId);

        // function type
        Strand strand = Scheduler.getStrand();
        ClassLoader classLoader = FunctionMock.class.getClassLoader(); //mockHandler cannot be static
        String orgName = "wso2";
        String packageName = "ballerina/test";
        String version = "0.0.0";
        String className = "...";
        String methodName = ""; // Set the method name here
        Object paramValues = "10";

        Object returnvalue = "Mock Handler hit";

                //Executor.executeFunction(strand.scheduler, classLoader, orgName, packageName, version, className, methodName, paramValues);

        return returnvalue;

        // Get the actual case and see if...

        // If a return value exists in the case, then just return that value instead


        // Otherwise return the result of the function call __call__functionName
    }

}

