package org.ballerinalang.langserver.completions.util;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.completions.BallerinaCustomErrorStrategy;
import org.ballerinalang.langserver.completions.InMemoryPackageRepository;
import org.ballerinalang.langserver.completions.SuggestionsFilter;
import org.ballerinalang.langserver.completions.SuggestionsFilterDataModel;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.models.ModelPackage;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;

/**
 * Compilation unit builder is for building ballerina compilation units
 */
public class BallerinaCompletionUtil {
    private static final String BAL_EXTENSION = ".bal";
    private Set<Map.Entry<String, ModelPackage>> packages;

    /**
     * Get the Ballerina Compiler instance
     * @param positionParams - text document position params
     * @return {@link Compiler} Compiler instance
     */
    public static List<CompletionItem> getCompletions(TextDocumentPositionParams positionParams) {
        ArrayList<SymbolInfo> symbols = new ArrayList<>();
        CompilerContext compilerContext = new CompilerContext();
        SuggestionsFilterDataModel filterDataModel = new SuggestionsFilterDataModel();
        String textContent = getDocumentText(positionParams.getTextDocument().getUri());
        CompilerOptions options = CompilerOptions.getInstance(compilerContext);
        String compilationUnitId = getRandomCompilationUnitId();
        List<Name> names = new ArrayList<>();
        names.add(new Name("."));
        PackageID tempPackageID = new PackageID(names, new Name("0.0.0"));
        InMemoryPackageRepository inMemoryPackageRepository = new InMemoryPackageRepository(tempPackageID,
                "", compilationUnitId, textContent.getBytes(StandardCharsets.UTF_8));
        options.put(COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());
        compilerContext.put(PackageRepository.class, inMemoryPackageRepository);
        BallerinaCustomErrorStrategy errStrategy = new BallerinaCustomErrorStrategy(compilerContext,
                positionParams, filterDataModel);
        compilerContext.put(DefaultErrorStrategy.class, errStrategy);
        Compiler compiler = Compiler.getInstance(compilerContext);

        compiler.compile(compilationUnitId);
        BLangPackage bLangPackage = (BLangPackage) compiler.getAST();

        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(compilationUnitId, compilerContext,
                symbols, positionParams, filterDataModel);
        bLangPackage.accept(treeVisitor);
        // Set the symbol table
        filterDataModel.setSymbolTable(treeVisitor.getSymTable());

        // Filter the suggestions
        SuggestionsFilter suggestionsFilter = new SuggestionsFilter();
//        filterDataModel.setPackages(this.getPackages());

        return suggestionsFilter.getCompletionItems(filterDataModel, symbols);
    }

    private static String getRandomCompilationUnitId() {
        return UUID.randomUUID().toString().replace("-", "") + BAL_EXTENSION;
    }

    /**
     * Get packages.
     *
     * @return a map contains package details
     */
    public Set<Map.Entry<String, ModelPackage>> getPackages() {
        return this.packages;
    }

    private static String getDocumentText(String uri) {
        String documentText = "";
        try {
            documentText = new String(Files.readAllBytes(Paths.get(uri)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentText;
    }
}
