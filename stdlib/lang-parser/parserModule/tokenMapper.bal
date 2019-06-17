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
const int LBRACE = 0;
const int RBRACE = 1;
const int SEMICOLON = 2;
const int COMMA = 3;
const int DOT = 4;
const int COLON = 5;
const int LPAREN = 6;
const int RPAREN = 7;
const int QUESTION_MARK = 8;
const int LEFT_BRACKET = 9;
const int RIGHT_BRACKET = 10;

//documentation markdown
const int HASH = 11;

//binary operators
const int ADD = 12;
const int SUB = 13;
const int DIV = 14;
const int MUL = 15;
const int MOD = 16;

//binary compared operators
const int GT = 17;
const int GT_EQUAL = 18;
const int LT = 19;
const int LT_EQUAL = 20;

//binary equal operators
const int EQUAL = 21;
const int NOT_EQUAL = 22;

//binary ref equal operators
const int REF_NOT_EQUAL = 23;
const int REF_EQUAL = 24;


//unary operators
const int NOT = 25;
//sub and add tokens will be renamed as unaryMinus and unaryPlus,
//in the parser during unary expressions

const int BIT_COMPLEMENT = 26;
const int UNTAINT = 27;
const int UNARY_MINUS = 28;
const int UNARY_PLUS = 29;

// Integer Range Operators.
const int HALF_OPEN_RANGE = 30;

// Compound Assignment operators.
const int COMPOUND_ADD = 31;
const int COMPOUND_SUB = 32;
const int COMPOUND_DIV = 33;
const int COMPOUND_MUL = 34;
const int COMPOUND_RIGHT_SHIFT = 35;
const int COMPOUND_LEFT_SHIFT = 36;
const int COMPOUND_LOGICAL_SHIFT = 37;

//additional symbols
const int RANGE = 38;
const int ELLIPSIS = 39;
const int ELVIS = 40;
const int EQUAL_GT = 41;
const int RARROW = 42;
const int SYNCRARROW = 43;
const int LARROW = 44;
const int ASSIGN = 45;
const int IDENTIFIER = 46;
const int NUMBER = 47;
const int QUOTED_STRING_LITERAL = 48;
const int LEXER_ERROR_TOKEN = 49;
const int PARSER_ERROR_TOKEN = 50;
const int EOF = 51;

//reserved words
const int FINAL = 52;
const int FUNCTION = 53;
const int INT = 54;
const int STRING = 55;
const int CONTINUE = 56;
//Delimiters
const int LEFT_CLOSED_RECORD_DELIMITER = 57;

//>>,>>>,<< newly added tokens
const int DOUBLE_GT = 58;
const int DOUBLE_LT = 59;
const int TRIPLE_GT = 60;

//incomplete token range 61-70
//const int INCOMPLETE_SY = 61;


string[] tokenNames = ["LBRACE", "RBRACE", "SEMICOLON", "COMMA", "DOT", "COLON", "LPAREN", "RPAREN", "QUESTION_MARK",
"LEFT_BRACKET", "RIGHT_BRACKET", "HASH", "ADD", "SUB", "DIV", "MUL", "MOD", "GT", "GT_EQUAL", "LT", "LT_EQUAL",
"EQUAL", "NOT_EQUAL", "REF_NOT_EQUAL", "REF_EQUAL", "NOT", "BIT_COMPLEMENT", "UNTAINT", "UNARY_MINUS", "UNARY_PLUS",
"HALF_OPEN_RANGE", "COMPOUND_ADD", "COMPOUND_SUB", "COMPOUND_DIV", "COMPOUND_MUL", "COMPOUND_RIGHT_SHIFT",
"COMPOUND_LEFT_SHIFT", "COMPOUND_LOGICAL_SHIFT", "RANGE", "ELLIPSIS", "ELVIS", "EQUAL_GT", "RARROW", "SYNCRARROW",
"LARROW", "ASSIGN", "IDENTIFIER", "NUMBER", "QUOTED_STRING_LITERAL", "LEXER_ERROR_TOKEN", "PARSER_ERROR_TOKEN",
"EOF", "FINAL", "FUNCTION", "INT", "STRING", "CONTINUE", "LEFT_CLOSED_RECORD_DELIMITER", "DOUBLE_GT", "DOUBLE_LT",
"TRIPLE_GT"];

//single token symbols
const string rBraceSymbol = "}";
const string lParenSymbol = "(";
const string rParenSymbol = ")";
const string colonSymbol = ":";
const string commaSymbol = ",";
const string lBracketSymbol = "[";
const string rBracketSymbol = "]";
const string hashSymbol = "#";
const string bitComplimentSy = "~";
const string modSymbol = "%";
const string semicolonSym = ";";

const string lBraceSymbol = "{";
const string addSymbol = "+";
const string subSymbol = "-";
const string mulSymbol = "*";
const string divSymbol = "/";
const string quesMarkSym = "?";

const string assignSym = "=";
const string dotSym = ".";
const string notSym = "!";

const string gtSymbol = ">";
const string ltSymbol = "<";
const string singleQuoteSym = "\"";

//double token symbols
const string lClosedRecordSy = "{|";
const string compAddSy = "+=";
const string compSubSy = "-=";
const string comMulSy = "*=";
const string comDivSy = "/=";
const string elvisSy = "?:";

const string equalSym = "==";
const string equalGtSym = "=>";
const string rarrowSym = "->";
const string rangeSym = "..";
const string notEqSym = "!=";

const string gtEqSym = ">=";
const string ltEqSym = "<=";
const string larrowSym = "<-";
const string quotesStringSym = "\"\"";

//triple token Symbols
const string refEqSym = "===";
const string syncRarrowSym = "->>";
const string ellipsisSym = "...";
const string halfOpenRangeSym = "..<";
const string refNotEqSym = "!==";

const string compRShiftSym = ">>=";
const string comLShiftSym = "<<=";
const string eofSym = "EOF";

//quadruple token symbol
const string compLogicalSym = ">>>=";

//incomplete symbols
const string doubleGtSym = ">>";
const string doubleLtSym = "<<";
const string tripleGtSym = ">>>";

//const string atSym = "$";


#object which fills tokens into separate maps based on the size of the symbol
type TokenMapper object {
    map<int> tokenMap = {};

    public function __init() {
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

    //self.tokenMap[atSym] = INCOMPLETE_SY;
    }
};
