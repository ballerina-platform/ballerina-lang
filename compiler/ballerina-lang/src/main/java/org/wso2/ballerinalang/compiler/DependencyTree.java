package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
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
    private SymbolTable symbolTable;
    private PrintStream outStream = System.out;

    private DependencyTree(CompilerContext context) {
        context.put(DEPENDENCY_TREE_KEY, this);
        symbolTable = SymbolTable.getInstance(context);
    }

    public static DependencyTree getInstance(CompilerContext context) {
        DependencyTree binaryFileWriter = context.get(DEPENDENCY_TREE_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new DependencyTree(context);
        }
        return binaryFileWriter;
    }

    public void listDependencyPackages(BLangPackage packageNode) {
        outStream.println(packageNode.packageID.bvmAlias() + ":" + packageNode.packageID.version);
        outStream.println(DependencyTree.renderDependencyTree(packageNode, symbolTable, 0));
    }
    /**
     * Render dependency tree of package.
     *
     * @param packageNode package
     * @param symbolTable SymbolTable
     * @return dependency tree
     */
    public static String renderDependencyTree(BLangPackage packageNode, SymbolTable symbolTable, int depth) {
        List<StringBuilder> lines = renderDependencyTreeLines(packageNode, symbolTable, depth);
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
     * @param symbolTable Symbol table
     * @param depth
     * @return list of strings with strings to render the dependency tree
     */
    public static List<StringBuilder> renderDependencyTreeLines(BLangPackage packageNode, SymbolTable symbolTable,
                                                                int depth) {
        List<StringBuilder> result = new LinkedList<>();
        if (depth > 0) {
            result.add(new StringBuilder().append(packageNode.packageID.bvmAlias()));
        }
        Iterator<BLangImportPackage> iterator = packageNode.getImports().iterator();
        while (iterator.hasNext()) {
            SymbolEnv symbolEnv = symbolTable.pkgEnvMap.get(iterator.next().symbol);
            BLangPackage enclPkg = symbolEnv.enclPkg;
            List<StringBuilder> subtree = renderDependencyTreeLines(enclPkg, symbolTable, 1);
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
}
