package org.ballerinalang.composer.service.workspace.langserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.ResponseMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentPositionParams;
import org.ballerinalang.composer.service.workspace.rest.datamodel.InMemoryPackageRepository;
import org.ballerinalang.composer.service.workspace.suggetions.CapturePossibleTokenStrategy;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilter;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;

/**
 * Server manager mimics the completion item resolving logic
 */
public class ServerManager {

    private static final String BAL_EXTENSION = ".bal";

    /**
     * Get completions as a string payload
     * @param message Request message
     * @return {@link String}
     */
    public static String getCompletions(RequestMessage message) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        CompilerOptions options;
        String compilationUnitId = getRandomCompilationUnitId();
        ArrayList<CompletionItem> completionItems;
        ArrayList<SymbolInfo> symbols = new ArrayList<>();

        JsonObject params = gson.toJsonTree(message.getParams()).getAsJsonObject();
        TextDocumentPositionParams posParams = gson.fromJson(params.toString(), TextDocumentPositionParams.class);

        Position position = posParams.getPosition();
        String textContent = posParams.getText();
        CompilerContext compilerContext = new CompilerContext();

        options = CompilerOptions.getInstance(compilerContext);
        options.put(COMPILER_PHASE, CompilerPhase.TYPE_CHECK.toString());
        SuggestionsFilterDataModel filterDataModel = new SuggestionsFilterDataModel();

        List<Name> names = new ArrayList<>();
        names.add(new org.wso2.ballerinalang.compiler.util.Name("."));
        PackageID tempPackageID = new PackageID(names, new org.wso2.ballerinalang.compiler.util.Name("0.0.0"));
        InMemoryPackageRepository inMemoryPackageRepository = new InMemoryPackageRepository(tempPackageID, "",
                compilationUnitId, textContent.getBytes(StandardCharsets.UTF_8));
        compilerContext.put(PackageRepository.class, inMemoryPackageRepository);

        CapturePossibleTokenStrategy errStrategy = new CapturePossibleTokenStrategy(compilerContext,
                position, filterDataModel);
        compilerContext.put(DefaultErrorStrategy.class, errStrategy);

        Compiler compiler = Compiler.getInstance(compilerContext);
        // here we need to compile the whole package
        compiler.compile(compilationUnitId);
        BLangPackage bLangPackage = (BLangPackage) compiler.getAST();

        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(compilationUnitId, compilerContext,
                symbols, position, filterDataModel);
        bLangPackage.accept(treeVisitor);
        // Set the symbol table
        filterDataModel.setSymbolTable(treeVisitor.getSymTable());

        // Filter the suggestions
        SuggestionsFilter suggestionsFilter = new SuggestionsFilter();

        completionItems = suggestionsFilter.getCompletionItems(filterDataModel, symbols);
        // Create the response message for client request
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId(message.getId());
        responseMessage.setResult(completionItems.toArray(new CompletionItem[0]));

        return gson.toJson(responseMessage);
    }

    private static String getRandomCompilationUnitId() {
        return UUID.randomUUID().toString().replace("-", "") + BAL_EXTENSION;
    }
}
