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

import {DefaultErrorStrategy} from 'antlr4/error/ErrorStrategy;

class BLangParserErrorStrategy extends DefaultErrorStrategy {

    reportNoViableAlternative(recognizer, e) {
        var tokens = recognizer.getTokenStream();
        var input;
        if (tokens !== null) {
            if (e.startToken.type === Token.EOF) {
                input = "<EOF>";
            } else {
                input = tokens.getText(new Interval(e.startToken, e.offendingToken));
            }
        } else {
            input = "<unknown input>";
        }
        var msg = "no viable alternative at input " + this.escapeWSAndQuote(input);
        recognizer.notifyErrorListeners(msg, e.offendingToken, e);
    }

    reportInputMismatch(recognizer, e) {
        var msg = "mismatched input " + this.getTokenErrorDisplay(e.offendingToken) +
            " expecting " + e.getExpectedTokens().toString(recognizer.literalNames, recognizer.symbolicNames);
        recognizer.notifyErrorListeners(msg, e.offendingToken, e);
    }

    reportFailedPredicate(recognizer, e) {
        var ruleName = recognizer.ruleNames[recognizer._ctx.ruleIndex];
        var msg = "rule " + ruleName + " " + e.message;
        recognizer.notifyErrorListeners(msg, e.offendingToken, e);
    }

    reportUnwantedToken(recognizer) {
        if (DefaultErrorStrategy.prototype.inErrorRecoveryMode.call(this, recognizer)) {
            return;
        }
        DefaultErrorStrategy.prototype.beginErrorCondition.call(this, recognizer);
        var t = recognizer.getCurrentToken();
        var tokenName = this.getTokenErrorDisplay(t);
        var expecting = this.getExpectedTokens(recognizer);
        var msg = "extraneous input " + tokenName + " expecting " +
            expecting.toString(recognizer.literalNames, recognizer.symbolicNames);
        recognizer.notifyErrorListeners(msg, t, null);
    }
    
    reportMissingToken(recognizer) {
        if (DefaultErrorStrategy.prototype.inErrorRecoveryMode.call(this, recognizer)) {
            return;
        }
        DefaultErrorStrategy.prototype.beginErrorCondition.call(this, recognizer);
        var t = recognizer.getCurrentToken();
        var expecting = DefaultErrorStrategy.prototype.getExpectedTokens.call(this, recognizer);
        var msg = "missing " + expecting.toString(recognizer.literalNames, recognizer.symbolicNames) +
            " at " + DefaultErrorStrategy.prototype.getTokenErrorDisplay.call(this, t);
        recognizer.notifyErrorListeners(msg, t, null);
    }

}