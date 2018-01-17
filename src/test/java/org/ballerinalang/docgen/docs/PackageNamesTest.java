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

import org.ballerinalang.docgen.model.Link;
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
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "ballerina.", "Prefix was not ballerina for: " + pkgLink);
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "builtin", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "caching", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "config", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(3).packageName.suffix, "data.sql", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(4).packageName.suffix, "file", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(5).packageName.suffix, "io", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(6).packageName.suffix, "log", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(7).packageName.suffix, "math", "Invalid suffix name.");
    }
    
    @Test
    public void multiplePackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "org.eclipse.core.expressions.",
                                                    "Prefix was not org.eclipse.core.expressions for: " + pkgLink);
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "org.eclipse.core.expressions",
                                                                                                "Invalid suffix name.");
    }
    
    @Test
    public void multiplePackagePrefixReorderedTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions");
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "org.eclipse.core.",
                                                                "Prefix was not org.eclipse.core for: " + pkgLink);
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "expressions", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "expressions.tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "expressions.samples", "Invalid suffix name.");
    }
    
    @Test
    public void multipleCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions.client");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "org.eclipse.core.expressions.",
                    "Prefix was not org.eclipse.core.expressions for: " + pkgLink);
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "tests", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "client", "Invalid suffix name.");
    }
    
    @Test
    public void noCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("a.b.c");
        packageNames.add("x.y.z");
        packageNames.add("foo.bar");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "", "Prefix was found");
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "a.b.c", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "x.y.z", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "foo.bar", "Invalid suffix name.");
    }
    
    @Test
    public void someCommonPackagePrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("foo.bar");
        packageNames.add("a.b.c");
        packageNames.add("foo.car");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "", "Prefix was found");
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "foo.bar", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "a.b.c", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "foo.car", "Invalid suffix name.");
    }
    
    @Test
    public void multipleLengthPackagePrefixText() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests.one");
        packageNames.add("org.eclipse.core.expressions.samples");
        packageNames.add("org.eclipse.core.expressions.client.one.two");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "org.eclipse.core.expressions.",
                    "Prefix was not org.eclipse.core.expressions for: " + pkgLink);
        }
    
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "tests.one", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "samples", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "client.one.two", "Invalid suffix name.");
    }
    
    @Test
    public void multipleLengthPackagePrefixReorderedText() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("org.eclipse.core.expressions.tests.one");
        packageNames.add("org.eclipse.core.xyz");
        packageNames.add("org.eclipse.core.expressions.client.one.two");
    
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "org.eclipse.core.",
                                                        "Prefix was not org.eclipse.core for: " + pkgLink);
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "expressions.tests.one", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "xyz", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "expressions.client.one.two",
                                                                                                "Invalid suffix name.");
    }
    
    @Test
    public void noPackageLevelPrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add(".");
        
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "", "Prefix found.");
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, ".", "Invalid suffix name.");
    }
    
    @Test
    public void singlePackageLevelPrefixTest() {
        List<String> packageNames = new ArrayList<>();
        packageNames.add("a");
        packageNames.add("b");
        packageNames.add("c");
        packageNames.add("d");
        
        List<Link> packageNameList = PackageName.convertList(packageNames);
        for (Link pkgLink : packageNameList) {
            Assert.assertEquals(pkgLink.packageName.prefix, "", "Prefix found.");
        }
        
        Assert.assertEquals(packageNameList.get(0).packageName.suffix, "a", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(1).packageName.suffix, "b", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(2).packageName.suffix, "c", "Invalid suffix name.");
        Assert.assertEquals(packageNameList.get(3).packageName.suffix, "d", "Invalid suffix name.");
    }
}
