package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.ballerinalang.langserver.completions.models.ModelPackage;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the data model, which holds the data items required for filtering the suggestions.
 */
public class SuggestionsFilterDataModel {

    private ParserRuleContext parserRuleContext;
    private List<PossibleToken> possibleTokens;
    private TokenStream tokenStream;
    private Vocabulary vocabulary;
    private int tokenIndex;
    private BLangNode symbolEnvNode;
    private Set<Map.Entry<String, ModelPackage>> packages;
    private SymbolTable symbolTable;
    private List<SymbolInfo> visibleSymbols;
    private String fileName;
    private CompilerContext compilerContext;

    public SuggestionsFilterDataModel() {
        this.visibleSymbols = new ArrayList<>();
    }

    public void initParserContext (Parser parser, ParserRuleContext parserRuleContext,
                                   List<PossibleToken> possibleTokenList) {
        this.parserRuleContext = parserRuleContext;
        this.possibleTokens = possibleTokenList;
        if (parser != null) {
            this.tokenStream = parser.getTokenStream();
            this.vocabulary = parser.getVocabulary();
            this.tokenIndex = parser.getCurrentToken().getTokenIndex();
        }
    }

    /**
     * Get the Context.
     * @return {@link ParserRuleContext} parserRuleContext instance
     */
    public ParserRuleContext getParserRuleContext() {
        return parserRuleContext;
    }

    /**
     * Get the possible tokens list.
     * @return {@link List}
     */
    public List<PossibleToken> getPossibleTokens() {
        return possibleTokens;
    }

    /**
     * Set the possible token list.
     * @param possibleTokens - possible tokens
     */
    public void setPossibleTokens(List<PossibleToken> possibleTokens) {
        this.possibleTokens = possibleTokens;
    }

    /**
     * Get the token stream.
     * @return {@link TokenStream}
     */
    public TokenStream getTokenStream() {
        return this.tokenStream;
    }

    /**
     * Get the vocabulary.
     * @return {@link Vocabulary}
     */
    public Vocabulary getVocabulary() {
        return this.vocabulary;
    }

    /**
     * Get the token index.
     * @return {@link int}
     */
    public int getTokenIndex() {
        return tokenIndex;
    }

    public Set<Map.Entry<String, ModelPackage>> getPackages() {
        return packages;
    }

    public void setPackages(Set<Map.Entry<String, ModelPackage>> packages) {
        this.packages = packages;
    }

    public BLangNode getSymbolEnvNode() {
        return symbolEnvNode;
    }

    public void setSymbolEnvNode(BLangNode symbolEnvNode) {
        this.symbolEnvNode = symbolEnvNode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    /**
     * Get the visible symbols list.
     * @return {@link ArrayList} visible symbols
     */
    public List<SymbolInfo> getVisibleSymbols() {
        return visibleSymbols;
    }

    /**
     * Set the visible symbols list.
     * @param visibleSymbols - list of all visible symbols
     */
    public void setVisibleSymbols(ArrayList<SymbolInfo> visibleSymbols) {
        this.visibleSymbols = visibleSymbols;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public CompilerContext getCompilerContext() {
        return compilerContext;
    }

    public void setCompilerContext(CompilerContext compilerContext) {
        this.compilerContext = compilerContext;
    }
}
