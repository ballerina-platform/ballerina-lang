/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.suggetions;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.ballerinalang.model.BallerinaFile;

import java.util.List;

/**
 * This is the data model, which holds the data items required for filtering the suggestions
 */
public class SuggestionsFilterDataModel {

    private ParserRuleContext context;
    private List<PossibleToken> possibleTokens;
    private TokenStream tokenStream;
    private Vocabulary vocabulary;
    private int tokenIndex;
    private BallerinaFile ballerinaFile;


    /**
     * Constructor for SuggestionsFilterDataModel
     +     */
    public SuggestionsFilterDataModel(){
        
    }

    /**
     * Constructor for SuggestionsFilterDataModel
     * @param parser - parser instance
     */
    public SuggestionsFilterDataModel(Parser parser, ParserRuleContext context, List<PossibleToken> posibleTokenList) {
        this.context = context;
        this.possibleTokens = posibleTokenList;
        this.init(parser);
    }

    private void init(Parser parser) {
        this.tokenStream = parser.getTokenStream();
        this.vocabulary = parser.getVocabulary();
        this.tokenIndex = parser.getCurrentToken().getTokenIndex();
    }

    /**
     * Get the Context
     * @return {@link ParserRuleContext} context instance
     */
    public ParserRuleContext getContext() {
        return context;
    }

    /**
     * Get the possible tokens list
     * @return {@link List<PossibleToken>}
     */
    public List<PossibleToken> getPossibleTokens() {
        return possibleTokens;
    }

    /**
     * Set the possible token list
     * @param possibleTokens - possible tokens
     */
    public void setPossibleTokens(List<PossibleToken> possibleTokens) {
        this.possibleTokens = possibleTokens;
    }

    /**
     * Get the token stream
     * @return {@link TokenStream}
     */
    public TokenStream getTokenStream() {
        return this.tokenStream;
    }

    /**
     * Get the vocabulary
     * @return {@link Vocabulary}
     */
    public Vocabulary getVocabulary() {
        return this.vocabulary;
    }

    /**
     * Get the token index
     * @return {@link int}
     */
    public int getTokenIndex() {
        return tokenIndex;
    }

    public BallerinaFile getBallerinaFile() {
        return ballerinaFile;
    }

    public void setBallerinaFile(BallerinaFile ballerinaFile) {
        this.ballerinaFile = ballerinaFile;
    }
}
