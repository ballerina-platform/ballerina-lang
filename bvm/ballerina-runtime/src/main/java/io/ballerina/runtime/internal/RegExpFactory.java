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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.RegExpAssertion;
import io.ballerina.runtime.internal.values.RegExpAtom;
import io.ballerina.runtime.internal.values.RegExpAtomQuantifier;
import io.ballerina.runtime.internal.values.RegExpCapturingGroup;
import io.ballerina.runtime.internal.values.RegExpCharSet;
import io.ballerina.runtime.internal.values.RegExpCharacterClass;
import io.ballerina.runtime.internal.values.RegExpDisjunction;
import io.ballerina.runtime.internal.values.RegExpFlagExpression;
import io.ballerina.runtime.internal.values.RegExpFlagOnOff;
import io.ballerina.runtime.internal.values.RegExpLiteralCharOrEscape;
import io.ballerina.runtime.internal.values.RegExpQuantifier;
import io.ballerina.runtime.internal.values.RegExpSequence;
import io.ballerina.runtime.internal.values.RegExpTerm;
import io.ballerina.runtime.internal.values.RegExpValue;

import java.util.StringJoiner;

/**
 * Common utility methods used for regular expression manipulation.
 *
 * @since 2201.3.0
 */
public class RegExpFactory {

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

    public static RegExpAtomQuantifier createReAtomQuantifier(RegExpAtom reAtom, RegExpQuantifier quantifier) {
        return new RegExpAtomQuantifier(reAtom, quantifier);
    }

    public static RegExpLiteralCharOrEscape createReLiteralCharOrEscape(BString charOrEscape) {
        return new RegExpLiteralCharOrEscape(charOrEscape.getValue());
    }

    public static RegExpCharacterClass createReCharacterClass(BString characterClassStart, RegExpCharSet reCharSet,
                                                              BString characterClassEnd) {
        return new RegExpCharacterClass(characterClassStart.getValue(), reCharSet, characterClassEnd.getValue());
    }

    public static RegExpCharSet createReCharSet(BString charSet) {
        return new RegExpCharSet(charSet.getValue());
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

    public static RegExpQuantifier createReQuantifier(BString quantifier) {
        return new RegExpQuantifier(quantifier.getValue());
    }

    public static RegExpDisjunction translateRegExpConstructs(RegExpValue regExpValue) {
        RegExpDisjunction disjunction = regExpValue.getRegExpDisjunction();
        for (Object s : disjunction.getRegExpSeqList()) {
            if (s == null) {
                break;
            }
            if (!(s instanceof RegExpSequence)) {
                continue;
            }
            RegExpSequence seq = (RegExpSequence) s;
            for (RegExpTerm t : seq.getRegExpTermsList()) {
                if (!(t instanceof RegExpAtomQuantifier)) {
                    continue;
                }
                RegExpAtomQuantifier atomQuantifier = (RegExpAtomQuantifier) t;
                RegExpAtom reAtom = atomQuantifier.getReAtom();
                if (reAtom instanceof RegExpLiteralCharOrEscape) {
                    atomQuantifier.setReAtom(translateLiteralCharOrEscape((RegExpLiteralCharOrEscape) reAtom));
                } else if (reAtom instanceof RegExpCharacterClass) {
                    atomQuantifier.setReAtom(translateCharacterClass((RegExpCharacterClass) reAtom));
                }
            }
        }
        return disjunction;
    }

    private static RegExpAtom translateLiteralCharOrEscape(RegExpLiteralCharOrEscape charOrEscape) {
        String value = charOrEscape.getCharOrEscape();
        if (".".equals(value)) {
            return createCharacterClass("[^", "\\r\\n");
        }
        if ("\\s".equals(value)) {
            return createCharacterClass("[", "\\t\\s\\n\\r");
        }
        if ("\\S".equals(value)) {
            return createCharacterClass("^[", "\\t\\s\\n\\r");
        }
        return charOrEscape;
    }

    private static RegExpCharacterClass createCharacterClass(String classStart, String charSet) {
        return new RegExpCharacterClass(classStart, new RegExpCharSet(charSet), "]");
    }

    private static RegExpAtom translateCharacterClass(RegExpCharacterClass charClass) {
        RegExpCharSet charSet = charClass.getReCharSet();
        String chars = charSet.getCharSet();
        String translatedChars = translateCharSet(chars, "&&");
        charSet.setCharSet(translatedChars);
        return charClass;
    }

    private static String translateCharSet(String chars, String translateChars) {
        String[] charArr = chars.split(translateChars);
        if (charArr.length == 1) {
            return chars;
        }
        StringJoiner sj = new StringJoiner("\\&\\&");
        for (String str : charArr) {
            sj.add(str);
        }
        return sj.toString();
    }
}
