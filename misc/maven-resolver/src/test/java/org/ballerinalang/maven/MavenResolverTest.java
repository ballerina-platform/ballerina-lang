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
package org.ballerinalang.maven;

import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;

/**
 * Tests for {@link MavenResolver}.
 */
public class MavenResolverTest {
    String targetRepo = Paths.get("build").toAbsolutePath().toString() + File.separator + "platform-libs";
    MavenResolver resolver = new MavenResolver(targetRepo);

    @Test
    public void testNonTransitiveDependency() {
        try {
            Dependency dependency = resolver.resolve("org.json", "json", "20190722", false);
            String jarPath = Utils.getJarPath(targetRepo, dependency);
            Assert.assertEquals(new File(jarPath).exists(), true);
        } catch (MavenResolverException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTransitiveDependency() {
        try {
            Dependency dependency = resolver.resolve("org.apache.maven", "maven-artifact", "3.6.3", true);
            String jarPath = Utils.getJarPath(targetRepo, dependency.getDepedencies().get(0));
            Assert.assertEquals(new File(jarPath).exists(), true);
        } catch (MavenResolverException e) {
            Assert.fail(e.getMessage());
        }
    }
}
