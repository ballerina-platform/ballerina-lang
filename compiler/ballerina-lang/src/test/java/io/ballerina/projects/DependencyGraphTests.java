package io.ballerina.projects;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraphTests {

    @Test
    public void testGetLevels_singleNode() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        dependencies.put("A", new HashSet<>());

        DependencyGraph<String> graph = DependencyGraph.from(dependencies);
        List<List<String>> levels = graph.getLevels();

        Assert.assertEquals(levels.size(), 1);
        Assert.assertEquals(levels.get(0), Arrays.asList("A"));
    }

    @Test
    public void testGetLevels_linearDependency() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        dependencies.put("A", new HashSet<>(Arrays.asList("B")));
        dependencies.put("B", new HashSet<>(Arrays.asList("C")));
        dependencies.put("C", new HashSet<>());

        DependencyGraph<String> graph = DependencyGraph.from(dependencies);
        List<List<String>> levels = graph.getLevels();

        Assert.assertEquals(levels.size(), 3);
        Assert.assertEquals(levels.get(0), Arrays.asList("A"));
        Assert.assertEquals(levels.get(1), Arrays.asList("B"));
        Assert.assertEquals(levels.get(2), Arrays.asList("C"));
    }

    @Test
    public void testGetLevels_branchingDependency() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        dependencies.put("A", new HashSet<>(Arrays.asList("B", "C")));
        dependencies.put("B", new HashSet<>());
        dependencies.put("C", new HashSet<>());

        DependencyGraph<String> graph = DependencyGraph.from(dependencies);
        List<List<String>> levels = graph.getLevels();

        Assert.assertEquals(levels.size(), 2);
        Assert.assertEquals(levels.get(0), Arrays.asList("A"));
        Assert.assertTrue(levels.get(1).contains("B"));
        Assert.assertTrue(levels.get(1).contains("C"));
    }

    @Test
    public void testGetLevels_complexDependency() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        dependencies.put("A", new HashSet<>(Arrays.asList("B", "C")));
        dependencies.put("B", new HashSet<>(Arrays.asList("D")));
        dependencies.put("C", new HashSet<>(Arrays.asList("D")));
        dependencies.put("D", new HashSet<>());

        DependencyGraph<String> graph = DependencyGraph.from(dependencies);
        List<List<String>> levels = graph.getLevels();

        Assert.assertEquals(levels.size(), 3);
        Assert.assertEquals(levels.get(0), Arrays.asList("A"));
        Assert.assertTrue(levels.get(1).contains("B"));
        Assert.assertTrue(levels.get(1).contains("C"));
        Assert.assertEquals(levels.get(2), Arrays.asList("D"));
    }

    @Test
    public void testGetLevels_noDependencies() {
        Map<String, Set<String>> dependencies = new HashMap<>();
        dependencies.put("A", new HashSet<>());
        dependencies.put("B", new HashSet<>());
        dependencies.put("C", new HashSet<>());

        DependencyGraph<String> graph = DependencyGraph.from(dependencies);
        List<List<String>> levels = graph.getLevels();

        Assert.assertEquals(levels.size(), 1);
        Assert.assertTrue(levels.get(0).contains("A"));
        Assert.assertTrue(levels.get(0).contains("B"));
        Assert.assertTrue(levels.get(0).contains("C"));
    }
}
