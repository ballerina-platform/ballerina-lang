/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.runtime.internal.regexp;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.RegExpAssertion;
import io.ballerina.runtime.internal.values.RegExpAtom;
import io.ballerina.runtime.internal.values.RegExpAtomQuantifier;
import io.ballerina.runtime.internal.values.RegExpCapturingGroup;
import io.ballerina.runtime.internal.values.RegExpCharSet;
import io.ballerina.runtime.internal.values.RegExpCharSetRange;
import io.ballerina.runtime.internal.values.RegExpCharacterClass;
import io.ballerina.runtime.internal.values.RegExpDisjunction;
import io.ballerina.runtime.internal.values.RegExpFlagExpression;
import io.ballerina.runtime.internal.values.RegExpFlagOnOff;
import io.ballerina.runtime.internal.values.RegExpLiteralCharOrEscape;
import io.ballerina.runtime.internal.values.RegExpQuantifier;
import io.ballerina.runtime.internal.values.RegExpSequence;
import io.ballerina.runtime.internal.values.RegExpTerm;
import io.ballerina.runtime.internal.values.RegExpValue;

/**
 * Common utility methods used for regular expression manipulation.
 *
 * @since 2201.3.0
 */
public class RegExpFactory {
    private RegExpFactory() {
    }

    public static RegExpValue createRegExpValue(RegExpDisjunction regExpDisjunction) {
        return new RegExpValue(regExpDisjunction);
    }

    public static RegExpDisjunction createReDisjunction(ArrayValue termsList) {
        return new RegExpDisjunction(termsList);
    }

    public static RegExpSequence createReSequence(ArrayValue seqList) {
        return new RegExpSequence(seqList);
    }

    public static RegExpAssertion createReAssertion(BString assertion) {
        return new RegExpAssertion(assertion.getValue());
    }

    public static RegExpAtomQuantifier createReAtomQuantifier(Object reAtom, RegExpQuantifier quantifier) {
        return new RegExpAtomQuantifier(reAtom, quantifier);
    }

    public static RegExpLiteralCharOrEscape createReLiteralCharOrEscape(BString charOrEscape) {
        return new RegExpLiteralCharOrEscape(charOrEscape.getValue());
    }

    public static RegExpCharacterClass createReCharacterClass(BString characterClassStart, BString negation,
                                                              RegExpCharSet reCharSet, BString characterClassEnd) {
        return new RegExpCharacterClass(characterClassStart.getValue(), negation.getValue(), reCharSet,
                characterClassEnd.getValue());
    }

    public static RegExpCharSet createReCharSet(ArrayValue charSet) {
        return new RegExpCharSet(charSet);
    }

    public static RegExpCharSetRange createReCharSetRange(BString lhsCharSetAtom, BString dash,
                                                          BString rhsCharSetAtom) {
        return new RegExpCharSetRange(lhsCharSetAtom.getValue(), dash.getValue(), rhsCharSetAtom.getValue());
    }

    public static RegExpCapturingGroup createReCapturingGroup(BString openParen, RegExpFlagExpression flagExpr,
                                                              RegExpDisjunction reDisjunction, BString closeParen) {
        return new RegExpCapturingGroup(openParen.getValue(), flagExpr, reDisjunction, closeParen.getValue());
    }

    public static RegExpFlagExpression createReFlagExpression(BString questionMark, RegExpFlagOnOff flagsOnOff,
                                                              BString colon) {
        return new RegExpFlagExpression(questionMark.getValue(), flagsOnOff, colon.getValue());
    }

    public static RegExpFlagOnOff createReFlagOnOff(BString flags) {
        return new RegExpFlagOnOff(flags.getValue());
    }

    public static RegExpQuantifier createReQuantifier(BString quantifier, BString nonGreedyChar) {
        return new RegExpQuantifier(quantifier.getValue(), nonGreedyChar.getValue());
    }

    public static RegExpValue parse(String regExpStr) {
        try {
            CharReader charReader = CharReader.from(regExpStr);
            TokenReader tokenReader = new TokenReader(new TreeTraverser(charReader));
            TreeBuilder treeBuilder = new TreeBuilder(tokenReader);
            return treeBuilder.parse();
        } catch (BallerinaException e) {
            throw ErrorCreator.createError(StringUtils.fromString("Failed to parse regular expression: " +
                    e.getMessage()));
        }
    }

    public static RegExpValue translateRegExpConstructs(RegExpValue regExpValue) {
        RegExpDisjunction disjunction = regExpValue.getRegExpDisjunction();
        for (Object s : disjunction.getRegExpSeqList()) {
            if (!(s instanceof RegExpSequence)) {
                continue;
            }
            RegExpSequence seq = (RegExpSequence) s;
            translateRegExpTerms(seq.getRegExpTermsList());
        }
        return new RegExpValue(disjunction);
    }

    private static void translateRegExpTerms(RegExpTerm[] terms) {
        for (RegExpTerm t : terms) {
            if (!(t instanceof RegExpAtomQuantifier)) {
                continue;
            }
            RegExpAtomQuantifier atomQuantifier = (RegExpAtomQuantifier) t;
            Object reAtom = atomQuantifier.getReAtom();
            if (reAtom instanceof RegExpLiteralCharOrEscape) {
                atomQuantifier.setReAtom(translateLiteralCharOrEscape((RegExpLiteralCharOrEscape) reAtom));
            } else if (reAtom instanceof RegExpCharacterClass) {
                atomQuantifier.setReAtom(translateCharacterClass((RegExpCharacterClass) reAtom));
            }
        }
    }

    private static RegExpAtom translateLiteralCharOrEscape(RegExpLiteralCharOrEscape charOrEscape) {
        String value = charOrEscape.getCharOrEscape();
        if (".".equals(value)) {
            return createCharacterClass("^", new String[]{"\\r", "\\n"});
        }
        if ("\\s".equals(value)) {
            return createCharacterClass("", new String[]{"\\t", "\\s", "\\n", "\\r"});
        }
        if ("\\S".equals(value)) {
            return createCharacterClass("^", new String[]{"\\t", "\\s", "\\n", "\\r"});
        }
        if ("&".equals(value)) {
            return createLiteralCharOrEscape("\\&");
        }
        return charOrEscape;
    }

    private static RegExpLiteralCharOrEscape createLiteralCharOrEscape(String charOrEscape) {
        return new RegExpLiteralCharOrEscape(charOrEscape);
    }

    private static RegExpCharacterClass createCharacterClass(String negation, Object[] charSet) {
        return new RegExpCharacterClass("[", negation, new RegExpCharSet(charSet) , "]");
    }

    private static RegExpAtom translateCharacterClass(RegExpCharacterClass charClass) {
        RegExpCharSet charSet = charClass.getReCharSet();
        Object[] charAtoms = charSet.getCharSetAtoms();
        int c = charAtoms.length;
        for (int i = 0; i < c; i++) {
            Object charAtom = charAtoms[i];
            if (charAtom instanceof RegExpCharSetRange) {
                RegExpCharSetRange range = (RegExpCharSetRange) charAtom;
                range.setLhsCharSetAtom(translateCharInCharacterClass(range.getLhsCharSetAtom()));
                range.setRhsCharSetAom(translateCharInCharacterClass(range.getRhsCharSetAtom()));
                continue;
            }
            charAtoms[i] = translateCharInCharacterClass((String) charAtom);
        }
        return charClass;
    }

    private static String translateCharInCharacterClass(String originalValue) {
        if ("&".equals(originalValue)) {
            return "\\&";
        }
        return originalValue;
    }
}
