import ballerina/io;
import ballerina/log;

const int LBRACE = 0;
const int RBRACE = 1;
const int LPAREN = 2;
const int RPAREN = 3;
const int ADD = 4;
const int ASSIGN = 5;
const int SEMICOLON = 6;
const int IDENTIFIER = 7;
const int EQUALITY = 8;
const int EOF = 9;
const int NUMBER = 10;
const int EQUAL = 11;
const int REF_EQUAL = 12;
const int STRING_LITERAL = 13;
const int DOUBLE_QUOTE = 14;
const int FUNCTION = 15;
const int INT = 16;
const int STRING = 17;
const int SUB = 18;
const int DIV = 19;
const int MUL = 20;
const int LEXER_ERROR_TOKEN = 21;
const int COLON = 22;
const int PARSER_ERROR_TOKEN = 23;
const int COMMA = 24;
const int DOT = 25;
const int RANGE = 26;
const int ELLIPSIS = 27;
const int HALF_OPEN_RANGE = 28;
const int LEFT_BRACKET = 29;
const int RIGHT_BRACKET = 30;
const int QUESTION_MARK = 31;
const int ELVIS = 32;
const int HASH = 33;
const int EQUAL_GT = 34;
const int COMPOUND_ADD = 35;
const int RARROW = 36;
const int COMPOUND_SUB = 37;
const int SYNCRARROW = 38;
const int COMPOUND_MUL = 39;
const int COMPOUND_DIV = 40;
const int MOD = 41;
const int NOT = 42;
const int REF_NOT_EQUAL = 43;
const int GT = 44;
const int GT_EQUAL = 45;
const int COMPOUND_RIGHT_SHIFT = 46;
const int COMPOUND_LOGICAL_SHIFT = 47;
const int LT = 48;
const int LT_EQUAL = 49;
const int LARROW = 50;
const int COMPOUND_LEFT_SHIFT = 51;
const int NOT_EQUAL = 52;
//to get the operator procedence, wont be used in builidng the AST
const int UNARY_MINUS = 53;
const int UNARY_PLUS = 54;
string[] tokenNames = ["LBRACE", "RBRACE", "LPAREN", "RPAREN", "ADD", "ASSIGN", "SEMICOLON", "IDENTIFIER", "EQUALITY",
"EOF", "NUMBER", "EQUAL", "REF_EQUAL", "STRING_LITERAL", "DOUBLE_QUOTE", "FUNCTION", "INT", "STRING",
"SUB", "DIV", "MUL","LEXER_ERROR_TOKEN","COLON","PARSER_ERROR_TOKEN","COMMA",
"DOT","RANGE","ELLIPSIS","HALF_OPEN_RANGE","LEFT_BRACKET","RIGHT_BRACKET","QUESTION_MARK","ELVIS","HASH",
"EQUAL_GT","COMPOUND_ADD","RARROW","COMPOUND_SUB","SYNCRARROW","COMPOUND_MUL","COMPOUND_DIV","MOD",
"NOT","REF_NOT_EQUAL","GT","GT_EQUAL","COMPOUND_RIGHT_SHIFT","COMPOUND_LOGICAL_SHIFT",
"LT","LT_EQUAL","LARROW","COMPOUND_LEFT_SHIFT","NOT_EQUAL","UNARY_MINUS","UNARY_PLUS"];

int position = 0;
int lineNum = 1;
int tokenIndex = 0;
WhiteSpaceStack wpStack = new ();

public type Lexer object {
    private BufferReader buffer;

    public function __init(BufferReader buffer) {
        self.buffer = buffer;
    }
    public function nextLexeme() returns string {
        return self.buffer.consume();
    }
    function nextToken() returns Token{
        string currChar = "";
        while (!self.buffer.isEOF()) {
            currChar = self.nextLexeme();
            position += 1;
            if (isWhiteSpace(currChar)) {
                int tabCount = 0;
                if (isEnter(currChar)) {
                    position = 0;
                    lineNum += 1;
                    //wpStack.push(currChar);
					wpStack.push("newLine ");
                } else if(isTab(currChar)){
					position = position + 3;
					//wpStack.push(currChar);
					wpStack.push("tab ");
                }else{
					//wpStack.push(currChar);
					wpStack.push("space ");
				}
                continue;
            }
            if (currChar == "}") {
				tokenIndex += 1;
                return { tokenType: RBRACE, text: currChar, startPos: position, endPos: position, lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace()};
            } else if (currChar == "(") {
				tokenIndex += 1;
                return { tokenType: LPAREN, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum , index: tokenIndex ,whiteSpace: getWhiteSpace()};
            } else if (currChar == "{") {
				tokenIndex += 1;
                return { tokenType: LBRACE, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum , index: tokenIndex, whiteSpace: getWhiteSpace()};
            } else if (currChar == ")") {
				tokenIndex += 1;
                return { tokenType: RPAREN, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum , index: tokenIndex, whiteSpace: getWhiteSpace()};
            } else if (currChar == ":") {
				tokenIndex += 1;
				return { tokenType: COLON, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace()};
			} else if (currChar == ","){
				tokenIndex += 1;
				return { tokenType: COMMA, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace()};
			}else if (currChar == "=") {
                if (self.buffer.lookAhead() == "=") {
                    currChar = self.nextLexeme();
                    position += 1;
                    if (self.buffer.lookAhead() == "=") {
                        currChar = self.nextLexeme();
                        position += 1;
						tokenIndex += 1;
                        return { tokenType: REF_EQUAL, text: "===", startPos: position - 2 , endPos:position,
                            lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
                    }
					tokenIndex += 1;
                    return { tokenType: EQUAL, text: "==", startPos: position - 1 , endPos:position, lineNumber:
                    lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
                }else if (self.buffer.lookAhead() == ">"){
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: EQUAL_GT, text: "=>", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
                return { tokenType: ASSIGN, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
            }else if (currChar == ";") {
				tokenIndex += 1;
                return { tokenType: SEMICOLON, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
            } else if (currChar == "+") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: COMPOUND_ADD, text: "+=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
                return { tokenType: ADD, text: currChar, startPos: position, endPos: position, lineNumber: lineNum , index: tokenIndex, whiteSpace: getWhiteSpace()};
            } else if (currChar == "-") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: COMPOUND_SUB, text: "-=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}else if (self.buffer.lookAhead() == ">"){
					currChar = self.nextLexeme();
					position += 1;
					if (self.buffer.lookAhead() == ">") {
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: SYNCRARROW, text: "->>", startPos: position - 2 , endPos:position,
							lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
					}
					tokenIndex += 1;
					return { tokenType: RARROW, text: "->", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
                return { tokenType: SUB, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
            } else if (currChar == "*") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: COMPOUND_MUL, text: "*=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
                return { tokenType: MUL, text: currChar, startPos: position, endPos: position, lineNumber
                : lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};
            }else if(currChar == "."){
				if (self.buffer.lookAhead() == ".") {
					currChar = self.nextLexeme();
					position += 1;
					if (self.buffer.lookAhead() == ".") {
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: ELLIPSIS, text: "...", startPos: position - 2 , endPos:position,
							lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
					}else if (self.buffer.lookAhead() == "<"){
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: HALF_OPEN_RANGE, text: "..<", startPos: position - 2 , endPos:position,
							lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
					}
					tokenIndex += 1;
					return { tokenType: RANGE, text: "..", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
				return { tokenType: DOT, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
			}else if(currChar == "["){
				tokenIndex += 1;
				return { tokenType: LEFT_BRACKET, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
			} else if (currChar == "]"){
				tokenIndex += 1;
				return { tokenType: RIGHT_BRACKET, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
			}else if(currChar == "?"){
				if (self.buffer.lookAhead() == ":") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: ELVIS, text: "?:", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
				return { tokenType: QUESTION_MARK, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
			}else if (currChar == "#"){
				tokenIndex += 1;
				return { tokenType: HASH, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
			}else if (currChar == "/") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: COMPOUND_DIV, text: "/=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
                return { tokenType: DIV, text: currChar, startPos: position, endPos: position, lineNumber:
                lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
            }else if(currChar == "%"){
				tokenIndex += 1;
				return { tokenType: MOD, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
			}else if (currChar == "!") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: REF_NOT_EQUAL, text: "!==", startPos: position - 2 , endPos:position,
							lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
					}
					tokenIndex += 1;
					return { tokenType: NOT_EQUAL, text: "!=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}
				tokenIndex += 1;
				return { tokenType: NOT, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
			}else if (currChar == ">") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: GT_EQUAL, text: ">=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}else if (self.buffer.lookAhead() == ">"){
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: COMPOUND_RIGHT_SHIFT, text: ">>=", startPos: position - 2 , endPos:position,
							lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace() };
					}else if (self.buffer.lookAhead() == ">"){
						if (self.buffer.lookAhead() == "=") {
							currChar = self.nextLexeme();
							position += 1;
							tokenIndex += 1;
							return { tokenType: COMPOUND_LOGICAL_SHIFT, text: ">>>=", startPos: position - 1 , endPos:position, lineNumber:
							lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
						}
					}
				}
				tokenIndex += 1;
				return { tokenType: GT, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum, index: tokenIndex , whiteSpace: getWhiteSpace() };
			}else if (currChar == "<") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: LT_EQUAL, text: "<=", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}else if (self.buffer.lookAhead() == "-"){
					currChar = self.nextLexeme();
					position += 1;
					tokenIndex += 1;
					return { tokenType: LARROW, text: "<-", startPos: position - 1 , endPos:position, lineNumber:
					lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
				}else if(self.buffer.lookAhead() == "<"){
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						position += 1;
						tokenIndex += 1;
						return { tokenType: COMPOUND_LEFT_SHIFT, text: "<<=", startPos: position - 1 , endPos:position, lineNumber:
						lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
					}
				}
				tokenIndex += 1;
				return { tokenType: LT, text: currChar, startPos: position, endPos: position, lineNumber:
				lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
			} else if (currChar == "\"") {
                string str = currChar;
                if (self.buffer.lookAhead() == "\"") {
                    currChar = self.nextLexeme();
                    position += 1;
					tokenIndex += 1;
                    return { tokenType: STRING_LITERAL, text: "\"\"", startPos: position - 1 , endPos: position,
                        lineNumber: lineNum, index: tokenIndex , whiteSpace: getWhiteSpace()} ;
                }
                boolean completeStr = true;
                int strPos = 0;
                while (completeStr) {
                    currChar = self.nextLexeme();
                    position += 1;
                    strPos += 1;
                    str = str + currChar;
                    if (self.buffer.lookAhead() == "\"") {
                        currChar = self.nextLexeme();
						position += 1;
						strPos += 1;
                        str = str + currChar;
                        completeStr = false;
						tokenIndex += 1;
                        return { tokenType: STRING_LITERAL, text: str, startPos: position - strPos , endPos:
                        position, lineNumber: lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};
                    }
					//loop until Eof
                    if (self.buffer.lookAhead() == "") {
                        completeStr = false;
                        //error err = error("string expected, found incomplete string");
                        //panic err;
						return { tokenType: LEXER_ERROR_TOKEN, text: str, startPos: position - strPos , endPos:
						position, lineNumber: lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};

                    }
                }
            } else if (isDigit(currChar)) {
                string numb = currChar;
				int digitPos = 0;
				boolean validNum = true;
				while(isDigit(self.buffer.lookAhead())){
					currChar = self.nextLexeme();
					    position += 1;
					    digitPos += 1;
					    numb = numb + currChar;
				}
				if(isLetter(self.buffer.lookAhead())){
					validNum = false;
					while(isLetter(self.buffer.lookAhead())){
						currChar = self.nextLexeme();
						position += 1;
						digitPos += 1;
						numb = numb + currChar;
					}
					//error err = error("Unexpected identifier after numeric literal");
					//panic err;
					tokenIndex += 1;
					return { tokenType: LEXER_ERROR_TOKEN, text: numb, startPos: position - digitPos , endPos:position, lineNumber:
					lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};
				}
				if(validNum){
					tokenIndex += 1;
					return { tokenType: NUMBER, text: numb, startPos: position - digitPos , endPos:position, lineNumber:
					lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};
				}
            } else if (isLetter(currChar)) {
                string word = currChar;
                int letPos = 0;
                while (isLetter(self.buffer.lookAhead()) || isDigit(self.buffer.lookAhead()) || self.buffer.lookAhead() == "_") {
                    currChar = self.nextLexeme();
                    position += 1;
                    letPos += 1;
                    word = word + currChar;
                }
                int checkReserved = isReserved(word);
                if (checkReserved != EOF) {
					tokenIndex += 1;
                    return { tokenType: checkReserved, text: word, startPos: position - letPos , endPos:position,
                        lineNumber: lineNum  , index: tokenIndex, whiteSpace: getWhiteSpace()};
                }
                else {
					tokenIndex += 1;
                    return { tokenType: IDENTIFIER, text: word, startPos: position - letPos , endPos:position,
                        lineNumber: lineNum , index: tokenIndex, whiteSpace: getWhiteSpace() };
                }
            }
        }
		tokenIndex += 1;
        return { tokenType: EOF, text: "EOF", startPos: position, endPos: position, lineNumber: lineNum, index: tokenIndex, whiteSpace: getWhiteSpace()};
    }
};


public type Token record{
    int tokenType;
    string text;
    int startPos;
    int endPos;
    int lineNumber;
	int index;
    string? whiteSpace;
};

//check for white spaces
function isWhiteSpace(string currentCharacter) returns boolean {
    string spaceRegEx = "[ \t\r\n\f]";
    boolean|error whiteSp = currentCharacter.matches(spaceRegEx);
    if (whiteSp is error) {
        panic whiteSp;
    } else {
        return whiteSp;
    }
}

//check for Enter
function isEnter(string currentCharacter) returns boolean {
    string enterRegEx = "[\n]";
    boolean|error enterSpace = currentCharacter.matches(enterRegEx);
    if (enterSpace is error) {
        panic enterSpace;
    } else {
        return enterSpace;
    }
}
//check for tab space
function isTab(string currentCharacter) returns boolean {
    string tabRegEx = "[\t]";
    boolean|error enterTabSpace = currentCharacter.matches(tabRegEx);
    if (enterTabSpace is error) {
        panic enterTabSpace;
    } else {
        return enterTabSpace;
    }
}

//check for digits
function isDigit(string currentChar) returns boolean {
    string regExNumber = "[0-9]";
    boolean|error isNumb = currentChar.matches(regExNumber);
    if (isNumb is error) {
        panic isNumb;
    } else {
        return isNumb;
    }
}

function isLetter(string currentChar) returns boolean {
    string regExString = "[a-zA-Z]+";
    boolean|error letter = currentChar.matches(regExString);
    if (letter is error) {
        panic letter;
    } else {
        return letter;
    }
}

function isReserved(string word) returns int {
    int resWord = EOF;
    match word {
        "function" => resWord = FUNCTION;
        "int" => resWord = INT;
        "string" => resWord = STRING;

    }
    return resWord;
}

//stack to keep the whiepsaces
type WhiteSpaceStack object{
   string [] spaceStack  = [];
    int top;
   public function __init(int top = 0) {
       self.top = top;
   }
    function push (string wSpace1){
        self.spaceStack [self.top] = wSpace1;
        self.top = self.top + 1;
    }
    function pop () returns string{
        self.top = self.top - 1;
        string spaceSaved = self.spaceStack[self.top];
        return spaceSaved;
    }

};

function getWhiteSpace() returns string?{
    string space2= "";
    if(wpStack.top != 0){
        while(wpStack.top > 0){
			//space2 = wpStack.pop();
			space2 = wpStack.pop() + space2;
        }
        return space2;
    }
    else {
        return null;
    }
}