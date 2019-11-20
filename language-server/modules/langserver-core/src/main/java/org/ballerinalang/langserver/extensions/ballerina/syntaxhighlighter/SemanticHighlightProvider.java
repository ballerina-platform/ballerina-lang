package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import com.google.common.primitives.Ints;
import org.ballerinalang.langserver.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.util.Arrays;

public class SemanticHighlightProvider {

public void getHighlights(ExtendedLanguageClient client, LSContext context, WorkspaceDocumentManager docManager) throws CompilationFailedException {

    LSModuleCompiler.getBLangPackages(context, docManager, null, true, true, true);

    List<SemanticHighlightProvider.HighlightInfo> list = new ArrayList<SemanticHighlightProvider.HighlightInfo>();
    context.put(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY, list);

    SemanticHighlightingVisitor semanticHighlightingVisitor = new SemanticHighlightingVisitor(context);
    BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
    bLangPackage.accept(semanticHighlightingVisitor);

    Map<Integer, int[]> map = new HashMap<Integer, int[]>();
    context.get(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY)
            .forEach(element->{
                int[] token = {element.identifier.pos.sCol-1, element.identifier.pos.eCol - element.identifier.pos.sCol, element.scopeEnum.getValue()};
                int line = element.identifier.pos.sLine-1;
                if(map.get(line) != null){
                    int[] cur = map.get(line);
                    map.put(line, Ints.concat(cur,token));
                }
                else
                {
                    map.put(line, token);
                }
            });

    for (Map.Entry<Integer, int[]> entry : map.entrySet()) {
        String token = Arrays.toString(entry.getValue());
        String encodedToken = Base64.getEncoder().encodeToString(token.getBytes());

        SemanticHighlightingInformation semanticHighlightingInformation = new SemanticHighlightingInformation(entry.getKey(), encodedToken);
        client.publishTextHighlighting(semanticHighlightingInformation);
    }

}

public static class HighlightInfo {

    ScopeEnum scopeEnum;
    BLangIdentifier identifier;

    public HighlightInfo(ScopeEnum scopeEnum, BLangIdentifier identifier) {
        this.scopeEnum = scopeEnum;
        this.identifier = identifier;
    }

    public ScopeEnum getScopeEnum() {
        return scopeEnum;
    }

    public BLangIdentifier getIdentifier() {
        return identifier;
    }
}


}