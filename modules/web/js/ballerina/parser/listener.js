/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var BallerinaListener = require('./antlr-gen/BallerinaListener').BallerinaListener,
    WS = require('./antlr-gen/BallerinaParser').BallerinaParser.WS,
    HIDDEN_CHANNEL = require('antlr4/Token').Token.HIDDEN_CHANNEL,
    _ = require('lodash');

var BLangParserListener = function(parser) {
    this.parser = parser;
};

BLangParserListener.prototype = Object.create(BallerinaListener.prototype);
BLangParserListener.prototype.constructor = BLangParserListener;

/**
 * Get all whitespace to the right from the given token until next token
 * @param token {Token} token
 */
BLangParserListener.prototype.getWhitespaceToRight = function(token) {
    var parser =  this.parser,
        hiddenTokensToRight = parser.getTokenStream().getHiddenTokensToRight(token.tokenIndex, HIDDEN_CHANNEL),
        whiteSpace = null;

    hiddenTokensToRight.forEach(function(hiddenToken){
        if(_.isEqual(hiddenToken.type, WS)){
            whiteSpace = (_.isNil(whiteSpace)) ? hiddenToken.text : whiteSpace + hiddenToken.text;
        }
    });

    return whiteSpace;
};

BLangParserListener.prototype.enterPackageDeclaration = function(ctx) {
    var tokenStream = ctx.parser.getTokenStream();
};

BLangParserListener.prototype.exitPackageDeclaration = function(ctx) {

    var packageNameToken = ctx.packageName(),
        packageFQN = packageNameToken.getText();

    var wsBetWeenPackageKeywordAndPackageNameStart = this.getWhitespaceToRight(ctx.start),
        wsBetWeenPackageNameEndAndSemicolon = this.getWhitespaceToRight(packageNameToken.stop),
        wsBetWeenSemicolonAndNextToken = this.getWhitespaceToRight(ctx.stop);
};

// Enter a parse tree produced by BallerinaParser#importDeclaration.
BallerinaListener.prototype.enterImportDeclaration = function(ctx) {
};

// Exit a parse tree produced by BallerinaParser#importDeclaration.
BallerinaListener.prototype.exitImportDeclaration = function(ctx) {
    var packageNameToken = ctx.packageName(),
        packageFQN = packageNameToken.getText();

    var wsBetWeenImportKeywordAndPackageNameStart = this.getWhitespaceToRight(ctx.start),
        wsBetWeenPackageNameEndAndSemicolon = this.getWhitespaceToRight(packageNameToken.stop),
        wsBetWeenSemicolonAndNextToken = this.getWhitespaceToRight(ctx.stop);
};

exports.BLangParserListener = BLangParserListener;