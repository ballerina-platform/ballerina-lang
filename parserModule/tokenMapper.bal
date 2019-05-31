//import ballerina/io;

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
const int UNARY_MINUS = 26;
const int UNARY_PLUS = 27;
const int BIT_COMPLEMENT = 28;
const int UNTAINT = 29;

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


string[] tokenNames = ["LBRACE", "RBRACE", "SEMICOLON", "COMMA", "DOT", "COLON", "LPAREN", "RPAREN", "QUESTION_MARK",
"LEFT_BRACKET", "RIGHT_BRACKET", "HASH", "ADD", "SUB", "DIV", "MUL", "MOD", "GT", "GT_EQUAL", "LT", "LT_EQUAL",
"EQUAL", "NOT_EQUAL", "REF_NOT_EQUAL", "NOT", "UNARY_MINUS", "UNARY_PLUS", "BIT_COMPLEMENT", "UNTAINT",
"HALF_OPEN_RANGE", "COMPOUND_ADD", "COMPOUND_SUB", "COMPOUND_DIV", "COMPOUND_MUL", "COMPOUND_RIGHT_SHIFT",
"COMPOUND_LEFT_SHIFT", "COMPOUND_LOGICAL_SHIFT", "RANGE", "ELLIPSIS", "ELVIS", "EQUAL_GT", "RARROW", "SYNCRARROW",
"LARROW", "ASSIGN", "IDENTIFIER", "NUMBER", "QUOTED_STRING_LITERAL", "LEXER_ERROR_TOKEN", "PARSER_ERROR_TOKEN",
"EOF", "FINAL", "FUNCTION", "INT", "STRING", "CONTINUE", "LEFT_CLOSED_RECORD_DELIMITER"];

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

//double token symbols
const string lCosedRecordSy = "{|";
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

//triple token Symbols
const string refEqSym = "===";
const string syncRarrowSym = "->>";
const string ellipsisSym = "...";
const string halfOpenRangeSym = "..<";
const string refNotEqSym = "!==";


#object which fills tokens into separate maps based on the size of the symbol
type TokenMapper object {
    map<int> singleSymbol = {};
    map<int> doubleSymbol = {};
    map<int> tripleSymbol = {};

    public function __init() {
        self.fillSingleSymbolMap();
		self.fillDoubleSymbolMap();
        self.fillTripleSymbolMap();
    }

    function fillSingleSymbolMap() {
        self.singleSymbol[rBraceSymbol] = RBRACE;
        self.singleSymbol[lParenSymbol] = LPAREN;
        self.singleSymbol[rParenSymbol] = RPAREN;
        self.singleSymbol[colonSymbol] = COLON;
        self.singleSymbol[commaSymbol] = COMMA;
        self.singleSymbol[lBracketSymbol] = LEFT_BRACKET;
        self.singleSymbol[rBracketSymbol] = RIGHT_BRACKET;
        self.singleSymbol[hashSymbol] = HASH;
        self.singleSymbol[bitComplimentSy] = BIT_COMPLEMENT;
        self.singleSymbol[modSymbol] = MOD;
        self.singleSymbol[semicolonSym] = SEMICOLON;

        self.singleSymbol[lBraceSymbol] = LBRACE;
        self.singleSymbol[addSymbol] = ADD;
        self.singleSymbol[subSymbol] = SUB;
        self.singleSymbol[mulSymbol] = MUL;
        self.singleSymbol[divSymbol] = DIV;
        self.singleSymbol[quesMarkSym] = QUESTION_MARK;

        self.singleSymbol[assignSym] = ASSIGN;
        self.singleSymbol[dotSym] = DOT;
        self.singleSymbol[notSym] = NOT;
    }

    function fillDoubleSymbolMap() {
        self.doubleSymbol[lCosedRecordSy] = LEFT_CLOSED_RECORD_DELIMITER;
        self.doubleSymbol[compAddSy] = COMPOUND_ADD;
        self.doubleSymbol[compSubSy] = COMPOUND_SUB;
        self.doubleSymbol[comMulSy] = COMPOUND_MUL;
        self.doubleSymbol[comDivSy] = COMPOUND_DIV;
        self.doubleSymbol[elvisSy] = ELVIS;

        self.doubleSymbol[equalSym] = EQUAL;
        self.doubleSymbol[equalGtSym] = EQUAL_GT;
        self.doubleSymbol[rarrowSym] = RARROW;
        self.doubleSymbol[rangeSym] = RANGE;
        self.doubleSymbol[notEqSym] = NOT_EQUAL;
    }

    function fillTripleSymbolMap() {
        self.tripleSymbol[refEqSym] = REF_EQUAL;
        self.tripleSymbol[syncRarrowSym] = SYNCRARROW;
        self.tripleSymbol[ellipsisSym] = ELLIPSIS;
        self.tripleSymbol[halfOpenRangeSym] = HALF_OPEN_RANGE;
        self.tripleSymbol[refNotEqSym] = REF_NOT_EQUAL;
        
    }

};
