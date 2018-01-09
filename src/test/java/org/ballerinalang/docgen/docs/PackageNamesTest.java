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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs;

import org.ballerinalang.docgen.model.PackageName;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test cases to see if common prefix of packages names are extracted correctly.
 */
public class PackageNamesTest {
    
    @Test
    public void oneCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("ballerina.builtin");
        packageNames.add("ballerina.caching");
        packageNames.add("ballerina.config");
        packageNames.add("ballerina.data.sql");
        packageNames.add("ballerina.file");
        packageNames.add("ballerina.io");
        packageNames.add("ballerina.log");
        packageNames.add("ballerina.math");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "ballerina.", "Prefix was not ballerina for: " + packageName);
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "builtin", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "caching", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "config", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(3).suffix, "data.sql", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(4).suffix, "file", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(5).suffix, "io", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(6).suffix, "log", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(7).suffix, "math", "Invalid suffix name.");
    }
    
    @Test
    public void multiplePackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "org.eclipse.core.expressions.",
                                                    "Prefix was not org.eclipse.core.expressions for: " + packageName);
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "org.eclipse.core.expressions", "Invalid suffix name.");
    }
    
    @Test
    public void multiplePackagePrefixReorderedTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions");
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "org.eclipse.core.",
                                                                "Prefix was not org.eclipse.core for: " + packageName);
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "expressions", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "expressions.tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "expressions.samples", "Invalid suffix name.");
    }
    
    @Test
    public void multipleCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions.client");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "org.eclipse.core.expressions.",
                    "Prefix was not org.eclipse.core.expressions for: " + packageName);
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "client", "Invalid suffix name.");
    }
    
    @Test
    public void noCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("a.b.c");
        packageNames.add("x.y.z");
        packageNames.add("foo.bar");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "", "Prefix was found");
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "a.b.c", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "x.y.z", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "foo.bar", "Invalid suffix name.");
    }
    
    @Test
    public void someCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("foo.bar");
        packageNames.add("a.b.c");
        packageNames.add("foo.car");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "", "Prefix was found");
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "foo.bar", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "a.b.c", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "foo.car", "Invalid suffix name.");
    }
    
    @Test
    public void multipleLengthPackagePrefixText() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests.one");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions.client.one.two");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "org.eclipse.core.expressions.",
                    "Prefix was not org.eclipse.core.expressions for: " + packageName);
        }
    
        Assert.assertEquals(packageNameList.get(0).suffix, "tests.one", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "client.one.two", "Invalid suffix name.");
    }
    
    @Test
    public void multipleLengthPackagePrefixReorderedText() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests.one");
        packageNames.add("org.eclipse.core.xyz");
        packageNames.add("org.eclipse.core.expressions.client.one.two");
    
        List<PackageName> packageNameList = PackageName.convertList(packageNames);
        for (PackageName packageName : packageNameList) {
            Assert.assertEquals(packageName.prefix, "org.eclipse.core.",
                                                        "Prefix was not org.eclipse.core for: " + packageName);
        }
        
        Assert.assertEquals(packageNameList.get(0).suffix, "expressions.tests.one", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).suffix, "xyz", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).suffix, "expressions.client.one.two", "Invalid suffix name.");
    }
}
