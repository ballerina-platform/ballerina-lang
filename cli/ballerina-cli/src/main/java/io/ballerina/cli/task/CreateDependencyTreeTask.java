package io.ballerina.cli.task;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for creating and printing the dependency graph as a human-readable tree.
 */
public class CreateDependencyTreeTask implements Task {
    private final transient PrintStream out;

    public CreateDependencyTreeTask(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Project project) {
        if (ProjectUtils.isProjectEmpty(project)) {
            throw createLauncherException("package is empty. Please add at least one .bal file.");
        }

        
        if (project.currentPackage().getResolution().diagnosticResult().hasErrors()) {            
            throw createLauncherException("package resolution contains errors. Cannot display dependency tree.");
        }
        
       
        DependencyGraph<PackageDescriptor> graph = project.currentPackage().getResolution().dependencyGraph();
        
        
        PackageDescriptor root = project.currentPackage().descriptor();

        
        out.println(root.org() + "/" + root.name() + ":" + root.version());
        
        
        printDependencies(graph, root, "", out);
    }

    /**
     * Recursively traverses the dependency graph and prints the tree structure.
     */
    private void printDependencies(DependencyGraph<PackageDescriptor> graph, PackageDescriptor parent, 
                                   String indent, PrintStream out) {
                                       
       
        Set<PackageDescriptor> rawDependencies = graph.getDependencies(parent);
        
        
        List<PackageDescriptor> dependencies = new ArrayList<>(rawDependencies);
        
        dependencies.sort(Comparator
                .comparing(PackageDescriptor::org)
                .thenComparing(PackageDescriptor::name)
                .thenComparing(PackageDescriptor::version));

        for (int i = 0; i < dependencies.size(); i++) {
            PackageDescriptor dep = dependencies.get(i);
            boolean isLast = (i == dependencies.size() - 1);
            
           
            String prefix = isLast ? "\\--- " : "+--- ";
            String childIndent = isLast ? "    " : "|   ";

           
            out.println(indent + prefix + dep.org() + "/" + dep.name() + ":" + dep.version());

            
            printDependencies(graph, dep, indent + childIndent, out);
        }
    }
}