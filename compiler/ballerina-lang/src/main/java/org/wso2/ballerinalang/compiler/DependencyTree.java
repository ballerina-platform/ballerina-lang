package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to render the dependency tree.
 */
public class DependencyTree {
    private static final CompilerContext.Key<DependencyTree> DEPENDENCY_TREE_KEY =
            new CompilerContext.Key<>();
    private PrintStream outStream = System.out;

    private DependencyTree(CompilerContext context) {
        context.put(DEPENDENCY_TREE_KEY, this);
    }

    public static DependencyTree getInstance(CompilerContext context) {
        DependencyTree binaryFileWriter = context.get(DEPENDENCY_TREE_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new DependencyTree(context);
        }
        return binaryFileWriter;
    }

    /**
     * Render dependency tree of package.
     *
     * @param packageSymbol package symbol
     * @param depth         depth
     * @return dependency tree
     */
    private static String renderDependencyTree(BPackageSymbol packageSymbol, int depth) {
        List<StringBuilder> lines = renderDependencyTreeLines(packageSymbol, depth);
        StringBuilder sb = new StringBuilder(lines.size() * 20);
        for (StringBuilder line : lines) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Render dependency tree lines.
     *
     * @param packageNode package
     * @param depth       depth
     * @return list of strings with strings to render the dependency tree
     */
    private static List<StringBuilder> renderDependencyTreeLines(BPackageSymbol packageNode, int depth) {
        List<StringBuilder> result = new LinkedList<>();
        if (depth > 0) {
            result.add(new StringBuilder().append(packageNode.pkgID.toString()));
        }
        Iterator<BPackageSymbol> iterator = packageNode.imports.iterator();
        while (iterator.hasNext()) {
            List<StringBuilder> subtree = renderDependencyTreeLines(iterator.next(), 1);
            if (iterator.hasNext()) {
                addSubtree(result, subtree);
            } else {
                addLastSubtree(result, subtree);
            }
        }
        return result;
    }

    /**
     * Render a subtree of the dependency tree.
     *
     * @param result  list of strings with the result
     * @param subtree list of strings of the subtree
     */
    private static void addSubtree(List<StringBuilder> result, List<StringBuilder> subtree) {
        Iterator<StringBuilder> iterator = subtree.iterator();
        result.add(iterator.next().insert(0, "├── "));
        while (iterator.hasNext()) {
            result.add(iterator.next().insert(0, "│   "));
        }
    }

    /**
     * Renders the last subtree of the dependency tree.
     *
     * @param result  list of strings with the result
     * @param subtree list of strings of the subtree
     */
    private static void addLastSubtree(List<StringBuilder> result, List<StringBuilder> subtree) {
        Iterator<StringBuilder> iterator = subtree.iterator();
        result.add(iterator.next().insert(0, "└── "));
        while (iterator.hasNext()) {
            result.add(iterator.next().insert(0, "    "));
        }
    }

    /**
     * List dependency packages.
     *
     * @param packageNode package node
     */
    void listDependencyPackages(BLangPackage packageNode) {
        String pkgIdAsStr = packageNode.symbol.pkgID.toString();
        if (packageNode.symbol.pkgID.isUnnamed) {
            pkgIdAsStr = packageNode.symbol.pkgID.sourceFileName.value;
        }
        outStream.println(pkgIdAsStr);
        outStream.println(DependencyTree.renderDependencyTree(packageNode.symbol, 0));
    }
}
