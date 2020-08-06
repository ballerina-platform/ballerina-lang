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

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit Tests for {@link MavenResolver}.
 *
 *  @since 2.0.0
 */
public class MavenResolverUnitTest {
    MavenResolver resolver = mock(MavenResolver.class);

    @Test
    public void testNonTransitiveDependency() {
        String groupId = "org.apache.maven";
        String artifactId = "maven-artifact";
        String version = "3.6.3";
        List<Dependency> dependencies = Arrays.asList(new Dependency("org.codehaus.plexus", "plexus-utils",
                "3.2.1"), new Dependency("org.apache.commons", "commons-lang3", "3.8.1"));
        Dependency dependency = new Dependency(groupId, artifactId, version);
        dependency.setDepedencies(dependencies);
        try {
            when(resolver.resolve(groupId, artifactId, version,
                    true)).thenReturn(dependency);
            Dependency resolvedDependency = resolver.resolve(groupId, artifactId, version,
                    true);
            Assert.assertEquals(resolvedDependency.getArtifactId(), artifactId);
            Assert.assertEquals(resolvedDependency.getGroupId(), groupId);
            Assert.assertEquals(resolvedDependency.getVersion(), version);
            Assert.assertTrue(resolvedDependency.getDepedencies().size() > 0);
        } catch (MavenResolverException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTransitiveDependency() {
        String groupId = "org.json";
        String artifactId = "json";
        String version = "20190722";
        try {
            when(resolver.resolve(groupId, artifactId, version,
                    false)).thenReturn(new Dependency(groupId, artifactId, version));
            Dependency resolvedDependency = resolver.resolve(groupId, artifactId, version,
                    false);
            Assert.assertEquals(resolvedDependency.getArtifactId(), artifactId);
            Assert.assertEquals(resolvedDependency.getGroupId(), groupId);
            Assert.assertEquals(resolvedDependency.getVersion(), version);
            Assert.assertEquals(resolvedDependency.getDepedencies().size(), 0);
        } catch (MavenResolverException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAddRemoteRepository() {
        String id = "wso2-release";
        String url = "http://maven.wso2.org/nexus/content/repositories/releases/";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Assert.assertEquals(id, args[0]);
            Assert.assertEquals(url, args[1]);
            return "called with arguments: " + Arrays.toString(args);
        }).when(resolver).addRepository(any(String.class), any(String.class));
        resolver.addRepository(id, url);
    }

    @Test
    public void testAddRemoteRepositoryWithCredentials() {
        String id = "ballerina-github";
        String url = "https://maven.pkg.github.com/ballerina-platform/ballerina-update-tool";
        String username = "username";
        String password = "password";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            Assert.assertEquals(args.length, 4);
            Assert.assertEquals(id, args[0]);
            Assert.assertEquals(url, args[1]);
            Assert.assertEquals(username, args[2]);
            Assert.assertEquals(password, args[3]);
            return "called with arguments: " + Arrays.toString(args);
        }).when(resolver).addRepository(any(String.class), any(String.class), any(String.class), any(String.class));
        resolver.addRepository(id, url, username, password);
    }
}
