/*
 *  Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.projects;

import io.ballerina.projects.DependencyGraph.DependencyGraphBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link DependencyGraph#toTopologicallySortedLevels()}.
 *
 * @since 2201.12.0
 */
public class TopologicallySortedLevelsTest {

    @Test
    public void testDiamondGraph() {
        // A depends on B and C; B and C both depend on D
        // Expected levels: [D] -> [B, C] -> [A]
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("A", "B");
        builder.addDependency("A", "C");
        builder.addDependency("B", "D");
        builder.addDependency("C", "D");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertEquals(levels.size(), 3);
        Assert.assertEquals(levels.get(0), Set.of("D"));
        Assert.assertEquals(levels.get(1), Set.of("B", "C"));
        Assert.assertEquals(levels.get(2), Set.of("A"));
    }

    @Test
    public void testLinearChain() {
        // A -> B -> C -> D (each depends on the next)
        // Expected levels: [D] -> [C] -> [B] -> [A]
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("A", "B");
        builder.addDependency("B", "C");
        builder.addDependency("C", "D");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertEquals(levels.size(), 4);
        Assert.assertEquals(levels.get(0), Set.of("D"));
        Assert.assertEquals(levels.get(1), Set.of("C"));
        Assert.assertEquals(levels.get(2), Set.of("B"));
        Assert.assertEquals(levels.get(3), Set.of("A"));
    }

    @Test
    public void testDisconnectedComponents() {
        // Two independent chains: A -> B, C -> D
        // Expected levels: [B, D] -> [A, C]
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("A", "B");
        builder.addDependency("C", "D");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertEquals(levels.size(), 2);
        Assert.assertEquals(levels.get(0), Set.of("B", "D"));
        Assert.assertEquals(levels.get(1), Set.of("A", "C"));
    }

    @Test
    public void testSingleNode() {
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.add("A");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertEquals(levels.size(), 1);
        Assert.assertEquals(levels.get(0), Set.of("A"));
    }

    @Test
    public void testAllIndependentNodes() {
        // No dependencies between nodes
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.add("A");
        builder.add("B");
        builder.add("C");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertEquals(levels.size(), 1);
        Assert.assertEquals(levels.get(0), Set.of("A", "B", "C"));
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*cyclic dependencies.*")
    public void testCyclicGraphThrowsException() {
        // A -> B -> C -> A (cycle)
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("A", "B");
        builder.addDependency("B", "C");
        builder.addDependency("C", "A");
        DependencyGraph<String> graph = builder.build();

        graph.toTopologicallySortedLevels();
    }

    @Test(expectedExceptions = IllegalStateException.class,
          expectedExceptionsMessageRegExp = ".*cyclic dependencies.*")
    public void testPartialCycleThrowsException() {
        // D -> A -> B -> C -> A (D is acyclic, A-B-C form a cycle)
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("D", "A");
        builder.addDependency("A", "B");
        builder.addDependency("B", "C");
        builder.addDependency("C", "A");
        DependencyGraph<String> graph = builder.build();

        graph.toTopologicallySortedLevels();
    }

    @Test
    public void testLevelsAreUnmodifiable() {
        DependencyGraphBuilder<String> builder = DependencyGraphBuilder.getBuilder();
        builder.addDependency("A", "B");
        DependencyGraph<String> graph = builder.build();

        List<Set<String>> levels = graph.toTopologicallySortedLevels();

        Assert.assertThrows(UnsupportedOperationException.class, () -> levels.add(Set.of("X")));
        Assert.assertThrows(UnsupportedOperationException.class, () -> levels.remove(0));
        Assert.assertThrows(UnsupportedOperationException.class, () -> levels.set(0, Set.of("X")));
        levels.forEach(level -> {
            Assert.assertThrows(UnsupportedOperationException.class, () -> level.add("X"));
            Assert.assertThrows(UnsupportedOperationException.class, () -> level.remove("X"));
            Assert.assertThrows(UnsupportedOperationException.class, level::clear);
        });
    }
}
