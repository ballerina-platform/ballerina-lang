/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.test;

import io.ballerina.projects.importresolver.Import;
import io.ballerina.projects.importresolver.ImportUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains cases to test the import util methods.
 *
 * @since 2.0.0
 */
public class TestImportUtil {

    @DataProvider(name = "dependencyGraphs")
    public Object[][] provideDependencyGraph() {
        // dependency graph 1
        Import a = new Import("test", "a", "1.0.0");
        Import b = new Import("test", "b", "1.0.0");
        Import c = new Import("test", "c", "1.0.0");
        Import d = new Import("test", "d", "1.0.0");
        Import e = new Import("test", "e", "1.0.0");
        Import f = new Import("test", "f", "1.0.0");

        a.setDependencies(Arrays.asList(e, f));
        b.setDependencies(Arrays.asList(d, e));
        c.setDependencies(Collections.singletonList(f));
        d.setDependencies(Collections.singletonList(c));

        List<Import> dependencyGraph1 = Arrays.asList(a, b, c, d, e, f);
        List<Import> sortedDependencyGraph1 = Arrays.asList(f, c, e, d, b, a);

        // dependency graph 2
        Import foo = new Import("foo", "foo_module", "1.2.0");
        Import bar = new Import("bar", "bar_module", "1.1.0");

        foo.setDependencies(Collections.singletonList(bar));

        List<Import> dependencyGraph2 = Arrays.asList(foo, bar);
        List<Import> sortedDependencyGraph2 = Arrays.asList(bar, foo);

        return new Object[][] {
                { dependencyGraph1, sortedDependencyGraph1 },
                { dependencyGraph2, sortedDependencyGraph2 }
        };
    }

    @Test(description = "test sort dependencies", dataProvider = "dependencyGraphs")
    public void testSortDependencies(List<Import> dependencyGraph, List<Import> sortedDependencyGraph) {

        List<Import> sorted = ImportUtil.sortDependencies(dependencyGraph);

        Assert.assertEquals(sorted.size(), sortedDependencyGraph.size());
        Assert.assertEquals(sorted, sortedDependencyGraph);

        for (int i = 0; i < sorted.size(); i++) {
            Assert.assertEquals(sorted.get(i), sortedDependencyGraph.get(i));
        }
    }
}

