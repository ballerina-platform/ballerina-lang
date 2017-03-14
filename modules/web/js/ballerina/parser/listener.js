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
 /*jshint esversion: 6 */
import {BallerinaListener} from './antlr-gen/BallerinaListener';
import {BallerinaParser} from './antlr-gen/BallerinaParser';
import {Token} from 'antlr4/Token';
import BLangModelBuilder from './model-builder';
import _ from 'lodash';

class BLangParserListener extends BallerinaListener {
    constructor(parser){
        super();
        this.parser = parser;
        this.modelBuilder = new BLangModelBuilder();
    }

    getWhitespaceToRight (token) {
        var parser =  this.parser,
            hiddenTokensToRight = parser.getTokenStream().getHiddenTokensToRight(token.tokenIndex, Token.HIDDEN_CHANNEL),
            whiteSpace = null;

        if(!_.isNil(hiddenTokensToRight)){
          hiddenTokensToRight.forEach(function(hiddenToken){
              if(_.isEqual(hiddenToken.type, BallerinaParser.WS)){
                  whiteSpace = (_.isNil(whiteSpace)) ? hiddenToken.text : whiteSpace + hiddenToken.text;
              }
          });
        }

        return whiteSpace;
    }

    exitPackageDeclaration(ctx) {
      if(!_.isNil(ctx)) {
        var packageNameToken = ctx.packageName(),
            packageFQN = packageNameToken.getText(),
            whitespaceTokens = [];

        var wsBetWeenPackageKeywordAndPackageNameStart = this.getWhitespaceToRight(ctx.start),
            wsBetWeenPackageNameEndAndSemicolon = this.getWhitespaceToRight(packageNameToken.stop),
            wsBetWeenSemicolonAndNextToken = this.getWhitespaceToRight(ctx.stop);

        whitespaceTokens.push(wsBetWeenPackageKeywordAndPackageNameStart);
        whitespaceTokens.push(wsBetWeenPackageNameEndAndSemicolon);
        whitespaceTokens.push(wsBetWeenSemicolonAndNextToken);

        this.modelBuilder.createPackageDeclaration(packageFQN, whitespaceTokens);
      }
    }

    exitImportDeclaration(ctx) {
      if(!_.isNil(ctx)) {
          var packageNameToken = ctx.packageName(),
              packageFQN = packageNameToken.getText(),
              whitespaceTokens = [];

          var wsBetWeenImportKeywordAndPackageNameStart = this.getWhitespaceToRight(ctx.start),
              wsBetWeenPackageNameEndAndSemicolon = this.getWhitespaceToRight(packageNameToken.stop),
              wsBetWeenSemicolonAndNextToken = this.getWhitespaceToRight(ctx.stop);

          whitespaceTokens.push(wsBetWeenImportKeywordAndPackageNameStart);
          whitespaceTokens.push(wsBetWeenPackageNameEndAndSemicolon);
          whitespaceTokens.push(wsBetWeenSemicolonAndNextToken);

          this.modelBuilder.createImportDeclaration(packageFQN, whitespaceTokens);
      }
    }

    getLineNumber(ctx){
        console.log(ctx);
    }

    getASTRoot(){
        return this.modelBuilder.getASTRoot();
    }

}

export default BLangParserListener;
