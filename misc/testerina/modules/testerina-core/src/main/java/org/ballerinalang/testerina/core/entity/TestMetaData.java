/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core.entity;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.tool.util.BFileUtil;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.HashMap;

/**
 * Java class to hold the meta data of a specific module's test suit.
 *
 * @since 1.2.o
 */
public class TestMetaData {
    private String initFunctionName;
    private String startFunctionName;
    private String stopFunctionName;
    private String testInitFunctionName;
    private String testStartFunctionName;
    private String testStopFunctionName;
    private PackageID packageID;
    private boolean hasTestablePackages;
    private String packageName;
    private HashMap<String, String> callableFunctionNames;
    private HashMap<String, String> testFunctionNames;

    public TestMetaData(BLangPackage bLangPackage, String packageName) {
        this.initFunctionName = bLangPackage.initFunction.name.value;
        this.startFunctionName = bLangPackage.startFunction.name.value;
        this.stopFunctionName = bLangPackage.stopFunction.name.value;
        this.testInitFunctionName = bLangPackage.getTestablePkg().initFunction.name.value;
        this.testStartFunctionName = bLangPackage.getTestablePkg().startFunction.name.value;
        this.testStopFunctionName = bLangPackage.getTestablePkg().stopFunction.name.value;
        this.packageID = bLangPackage.packageID;
        this.hasTestablePackages = bLangPackage.hasTestablePackage();
        this.packageName = packageName;
        this.computePackageFunctions(bLangPackage);
    }

    public void setStartFunctionName(String startFunctionName) {
        this.startFunctionName = startFunctionName;
    }

    public void setStopFunctionName(String stopFunctionName) {
        this.stopFunctionName = stopFunctionName;
    }

    public void setTestInitFunctionName(String testInitFunctionName) {
        this.testInitFunctionName = testInitFunctionName;
    }

    public void setTestStartFunctionName(String testStartFunctionName) {
        this.testStartFunctionName = testStartFunctionName;
    }

    public void setTestStopFunctionName(String testStopFunctionName) {
        this.testStopFunctionName = testStopFunctionName;
    }

    public void setHasTestablePackages(boolean hasTestablePackages) {
        this.hasTestablePackages = hasTestablePackages;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public TestMetaData () {}

    public String getInitFunctionName() {
        return initFunctionName;
    }

    public void setInitFunctionName(String initFunctionName) {
        this.initFunctionName = initFunctionName;
    }

    public String getStartFunctionName() {
        return startFunctionName;
    }

    public String getStopFunctionName() {
        return stopFunctionName;
    }

    public String getTestInitFunctionName() {
        return testInitFunctionName;
    }

    public String getTestStartFunctionName() {
        return testStartFunctionName;
    }

    public String getTestStopFunctionName() {
        return testStopFunctionName;
    }

    public PackageID getPackageID() {
        return packageID;
    }

    public void setPackageID(PackageID packageID) {
        this.packageID = packageID;
    }

    public void setCallableFunctionNames(HashMap<String, String> callableFunctionNames) {
        this.callableFunctionNames = callableFunctionNames;
    }

    public void setTestFunctionNames(HashMap<String, String> testFunctionNames) {
        this.testFunctionNames = testFunctionNames;
    }

    private void computePackageFunctions(BLangPackage bLangPackage) {
        this.callableFunctionNames = new HashMap<>();
        this.testFunctionNames = new HashMap<>();

        bLangPackage.functions.stream().forEach(function -> {
            try {
                String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                        bLangPackage.packageID.name.value,
                        getClassName(function.pos.src.cUnitName));
                callableFunctionNames.put(function.name.value, functionClassName);
            } catch (RuntimeException e) {
                // we do nothing here
            }
        });

        bLangPackage.getTestablePkg().functions.stream().forEach(function -> {
            try {
                String functionClassName = BFileUtil.getQualifiedClassName(bLangPackage.packageID.orgName.value,
                        bLangPackage.packageID.name.value,
                        getClassName(function.pos.src.cUnitName));
                testFunctionNames.put(function.name.value, functionClassName);
            } catch (RuntimeException e) {
                // we do nothing here
            }
        });
    }

    private static String getClassName(String function) {
        return function.replace(".bal", "").replace("/", ".");
    }

    public HashMap<String, String> getNormalFunctionNames() {
        return callableFunctionNames;
    }

    public HashMap<String, String> getTestFunctionNames() {
        return testFunctionNames;
    }

    public boolean isHasTestablePackages() {
        return hasTestablePackages;
    }

    public String getPackageName() {
        return packageName;
    }
}
