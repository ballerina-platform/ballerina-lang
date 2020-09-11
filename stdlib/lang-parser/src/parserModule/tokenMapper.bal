// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//separators
const LBRACE = 0;
const RBRACE = 1;
const SEMICOLON = 2;
const COMMA = 3;
const DOT = 4;
const COLON = 5;
const LPAREN = 6;
const RPAREN = 7;
const QUESTION_MARK = 8;
const LEFT_BRACKET = 9;
const RIGHT_BRACKET = 10;

//documentation markdown
const HASH = 11;

//binary operators
const ADD = 12;
const SUB = 13;
const DIV = 14;
const MUL = 15;
const MOD = 16;

//binary compared operators
const GT = 17;
const GT_EQUAL = 18;
const LT = 19;
const LT_EQUAL = 20;

//binary equal operators
const EQUAL = 21;
const NOT_EQUAL = 22;

//binary ref equal operators
const REF_NOT_EQUAL = 23;
const REF_EQUAL = 24;

//unary operators
const NOT = 25;
const BIT_COMPLEMENT = 26;
const UNTAINT = 27;
const UNARY_MINUS = 28;
const UNARY_PLUS = 29;

// Integer Range Operators.
const HALF_OPEN_RANGE = 30;

// Compound Assignment operators.
const COMPOUND_ADD = 31;
const COMPOUND_SUB = 32;
const COMPOUND_DIV = 33;
const COMPOUND_MUL = 34;
const COMPOUND_RIGHT_SHIFT = 35;
const COMPOUND_LEFT_SHIFT = 36;
const COMPOUND_LOGICAL_SHIFT = 37;

//additional symbols
const RANGE = 38;
const ELLIPSIS = 39;
const ELVIS = 40;
const EQUAL_GT = 41;
const RARROW = 42;
const SYNCRARROW = 43;
const LARROW = 44;
const ASSIGN = 45;
const IDENTIFIER = 46;
const NUMBER = 47;
const QUOTED_STRING_LITERAL = 48;
const LEXER_ERROR_TOKEN = 49;
const PARSER_ERROR_TOKEN = 50;
const EOF = 51;

//reserved words
const FINAL = 52;
const FUNCTION = 53;
const INT = 54;
const STRING = 55;
const CONTINUE = 56;
//Delimiters
const LEFT_CLOSED_RECORD_DELIMITER = 57;

//>>,>>>,<< newly added tokens
const DOUBLE_GT = 58;
const DOUBLE_LT = 59;
const TRIPLE_GT = 60;

string[] tokenNames = ["LBRACE", "RBRACE", "SEMICOLON", "COMMA", "DOT", "COLON", "LPAREN", "RPAREN", "QUESTION_MARK",
    "LEFT_BRACKET", "RIGHT_BRACKET", "HASH", "ADD", "SUB", "DIV", "MUL", "MOD", "GT", "GT_EQUAL", "LT", "LT_EQUAL",
    "EQUAL", "NOT_EQUAL", "REF_NOT_EQUAL", "REF_EQUAL", "NOT", "BIT_COMPLEMENT", "UNTAINT", "UNARY_MINUS", "UNARY_PLUS",
    "HALF_OPEN_RANGE", "COMPOUND_ADD", "COMPOUND_SUB", "COMPOUND_DIV", "COMPOUND_MUL", "COMPOUND_RIGHT_SHIFT",
    "COMPOUND_LEFT_SHIFT", "COMPOUND_LOGICAL_SHIFT", "RANGE", "ELLIPSIS", "ELVIS", "EQUAL_GT", "RARROW", "SYNCRARROW",
    "LARROW", "ASSIGN", "IDENTIFIER", "NUMBER", "QUOTED_STRING_LITERAL", "LEXER_ERROR_TOKEN", "PARSER_ERROR_TOKEN",
    "EOF", "FINAL", "FUNCTION", "INT", "STRING", "CONTINUE", "LEFT_CLOSED_RECORD_DELIMITER", "DOUBLE_GT", "DOUBLE_LT",
    "TRIPLE_GT"];

//single token symbols
const rBraceSymbol = "}";
const lParenSymbol = "(";
const rParenSymbol = ")";
const colonSymbol = ":";
const commaSymbol = ",";
const lBracketSymbol = "[";
const rBracketSymbol = "]";
const hashSymbol = "#";
const bitComplimentSy = "~";
const modSymbol = "%";
const semicolonSym = ";";

const lBraceSymbol = "{";
const addSymbol = "+";
const subSymbol = "-";
const mulSymbol = "*";
const divSymbol = "/";
const quesMarkSym = "?";

const assignSym = "=";
const dotSym = ".";
const notSym = "!";

const gtSymbol = ">";
const ltSymbol = "<";
const singleQuoteSym = "\"";

//double token symbols
const lClosedRecordSy = "{|";
const compAddSy = "+=";
const compSubSy = "-=";
const comMulSy = "*=";
const comDivSy = "/=";
const elvisSy = "?:";

const equalSym = "==";
const equalGtSym = "=>";
const rarrowSym = "->";
const rangeSym = "..";
const notEqSym = "!=";

const gtEqSym = ">=";
const ltEqSym = "<=";
const larrowSym = "<-";
const quotesStringSym = "\"\"";

//triple token Symbols
const refEqSym = "===";
const syncRarrowSym = "->>";
const ellipsisSym = "...";
const halfOpenRangeSym = "..<";
const refNotEqSym = "!==";

const compRShiftSym = ">>=";
const comLShiftSym = "<<=";
const eofSym = "EOF";

//quadruple token symbol
const compLogicalSym = ">>>=";

//incomplete symbols
const doubleGtSym = ">>";
const doubleLtSym = "<<";
const tripleGtSym = ">>>";

# object which fills tokens into separate maps based on the size of the symbol
class TokenMapper {
    map<int> tokenMap = {}

    public function init() {
        self.fillTokenMap();
    }

    function fillTokenMap() {
        self.tokenMap[rBraceSymbol] = RBRACE;
        self.tokenMap[lParenSymbol] = LPAREN;
        self.tokenMap[rParenSymbol] = RPAREN;
        self.tokenMap[colonSymbol] = COLON;
        self.tokenMap[commaSymbol] = COMMA;
        self.tokenMap[lBracketSymbol] = LEFT_BRACKET;
        self.tokenMap[rBracketSymbol] = RIGHT_BRACKET;
        self.tokenMap[hashSymbol] = HASH;
        self.tokenMap[bitComplimentSy] = BIT_COMPLEMENT;
        self.tokenMap[modSymbol] = MOD;
        self.tokenMap[semicolonSym] = SEMICOLON;

        self.tokenMap[lBraceSymbol] = LBRACE;
        self.tokenMap[addSymbol] = ADD;
        self.tokenMap[subSymbol] = SUB;
        self.tokenMap[mulSymbol] = MUL;
        self.tokenMap[divSymbol] = DIV;
        self.tokenMap[quesMarkSym] = QUESTION_MARK;

        self.tokenMap[assignSym] = ASSIGN;
        self.tokenMap[dotSym] = DOT;
        self.tokenMap[notSym] = NOT;
        self.tokenMap[gtSymbol] = GT;
        self.tokenMap[ltSymbol] = LT;

        self.tokenMap[lClosedRecordSy] = LEFT_CLOSED_RECORD_DELIMITER;
        self.tokenMap[compAddSy] = COMPOUND_ADD;
        self.tokenMap[compSubSy] = COMPOUND_SUB;
        self.tokenMap[comMulSy] = COMPOUND_MUL;
        self.tokenMap[comDivSy] = COMPOUND_DIV;
        self.tokenMap[elvisSy] = ELVIS;

        self.tokenMap[equalSym] = EQUAL;
        self.tokenMap[equalGtSym] = EQUAL_GT;
        self.tokenMap[rarrowSym] = RARROW;
        self.tokenMap[rangeSym] = RANGE;
        self.tokenMap[notEqSym] = NOT_EQUAL;
        self.tokenMap[gtEqSym] = GT_EQUAL;
        self.tokenMap[ltEqSym] = LT_EQUAL;
        self.tokenMap[larrowSym] = LARROW;

        self.tokenMap[refEqSym] = REF_EQUAL;
        self.tokenMap[syncRarrowSym] = SYNCRARROW;
        self.tokenMap[ellipsisSym] = ELLIPSIS;
        self.tokenMap[halfOpenRangeSym] = HALF_OPEN_RANGE;
        self.tokenMap[refNotEqSym] = REF_NOT_EQUAL;
        self.tokenMap[compRShiftSym] = COMPOUND_RIGHT_SHIFT;
        self.tokenMap[comLShiftSym] = COMPOUND_LEFT_SHIFT;

        self.tokenMap[compLogicalSym] = COMPOUND_LOGICAL_SHIFT;

        self.tokenMap[doubleGtSym] = DOUBLE_GT;
        self.tokenMap[doubleLtSym] = DOUBLE_LT;
        self.tokenMap[tripleGtSym] = TRIPLE_GT;

    }
};
