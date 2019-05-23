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
const int QUOTED_STRING_LITERAL = 13;
const int FINAL = 14;
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
//sub and add tokens will be renamed as unaryMinus and unaryPlus,
//in the parser during unary expressions
const int UNARY_MINUS = 53;
const int UNARY_PLUS = 54;

const int BIT_COMPLEMENT = 55;
const int UNTAINT = 56;

string[] tokenNames = ["LBRACE", "RBRACE", "LPAREN", "RPAREN", "ADD", "ASSIGN", "SEMICOLON", "IDENTIFIER", "EQUALITY",
"EOF", "NUMBER", "EQUAL", "REF_EQUAL", "QUOTED_STRING_LITERAL", "FINAL", "FUNCTION", "INT", "STRING",
"SUB", "DIV", "MUL","LEXER_ERROR_TOKEN","COLON","PARSER_ERROR_TOKEN","COMMA",
"DOT","RANGE","ELLIPSIS","HALF_OPEN_RANGE","LEFT_BRACKET","RIGHT_BRACKET","QUESTION_MARK","ELVIS","HASH",
"EQUAL_GT","COMPOUND_ADD","RARROW","COMPOUND_SUB","SYNCRARROW","COMPOUND_MUL","COMPOUND_DIV","MOD",
"NOT","REF_NOT_EQUAL","GT","GT_EQUAL","COMPOUND_RIGHT_SHIFT","COMPOUND_LOGICAL_SHIFT",
"LT","LT_EQUAL","LARROW","COMPOUND_LEFT_SHIFT","NOT_EQUAL","UNARY_MINUS","UNARY_PLUS",
"BIT_COMPLEMENT","UNTAINT"];


public type Lexer object {
	//token position relative to the line number
	int position = 0;
	//line number of the token in the source
	int lineNum = 1;
	//index of the token relative to the input file
	int tokenIndex = 0;
	//initializing the whitespace stack
	WhiteSpaceStack wpStack = new ();
	//instance of the bufferReader object
    private BufferReader buffer;

    public function __init(BufferReader buffer) {
        self.buffer = buffer;
    }

	//consume and return the character from the buffer
    public function nextLexeme() returns string {
        return self.buffer.consume();
    }

	//the characters from the buffer stream will be tokenized and a lexer token will be returned
    function nextToken() returns Token{
		//current character
        string currChar = "";
		//while the currChar is not Eof, consume the lexemes and build tokens
        while (!self.buffer.isEOF()) {
			//obtain the current character from the buffer
            currChar = self.nextLexeme();
            self.position += 1;
			//check if the current lexeme is a whitespace, if so increase the position and lineNumbers
			//relevant to the whitespace type and push the whitespaces to the stack.
            if (isWhiteSpace(currChar)) {
                int tabCount = 0;
                if (isEnter(currChar)) {
                    self.position = 0;
                    self.lineNum += 1;
                    self.wpStack.push(currChar);
					//self.wpStack.push("newLine ");
                } else if(isTab(currChar)){
					self.position = self.position + 3;
					self.wpStack.push(currChar);
					//self.wpStack.push("tab ");
                }else{
					self.wpStack.push(currChar);
					//self.wpStack.push("space ");
				}
                continue;
            }
            if (currChar == "}") {
				self.tokenIndex += 1;
                return {tokenType: RBRACE, text: currChar, startPos: self.position, endPos: self.position,
					lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
            } else if (currChar == "(") {
				self.tokenIndex += 1;
                return {tokenType: LPAREN, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum , index: self.tokenIndex ,whiteSpace: self.getWhiteSpace()};
            } else if (currChar == "{") {
				self.tokenIndex += 1;
                return {tokenType: LBRACE, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
            } else if (currChar == ")") {
				self.tokenIndex += 1;
                return {tokenType: RPAREN, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
            } else if (currChar == ":") {
				self.tokenIndex += 1;
				return {tokenType: COLON, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
			} else if (currChar == ","){
				self.tokenIndex += 1;
				return {tokenType: COMMA, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
			}else if (currChar == "=") {
                if (self.buffer.lookAhead() == "=") {
                    currChar = self.nextLexeme();
                    self.position += 1;
                    if (self.buffer.lookAhead() == "=") {
                        currChar = self.nextLexeme();
                        self.position += 1;
						self.tokenIndex += 1;
                        return {tokenType: REF_EQUAL, text: "===", startPos: self.position - 2 , endPos:self.position,
                            lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
                    }
					self.tokenIndex += 1;
                    return {tokenType: EQUAL, text: "==", startPos: self.position - 1 , endPos:self.position, lineNumber:
                    self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
                }else if (self.buffer.lookAhead() == ">"){
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: EQUAL_GT, text: "=>", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
                return {tokenType: ASSIGN, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
            }else if (currChar == ";") {
				self.tokenIndex += 1;
                return {tokenType: SEMICOLON, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
            } else if (currChar == "+") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: COMPOUND_ADD, text: "+=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
                return {tokenType: ADD, text: currChar, startPos: self.position, endPos: self.position, lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
            } else if (currChar == "-") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: COMPOUND_SUB, text: "-=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}else if (self.buffer.lookAhead() == ">"){
					currChar = self.nextLexeme();
					self.position += 1;
					if (self.buffer.lookAhead() == ">") {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: SYNCRARROW, text: "->>", startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}
					self.tokenIndex += 1;
					return {tokenType: RARROW, text: "->", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
                return {tokenType: SUB, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
            } else if (currChar == "*") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: COMPOUND_MUL, text: "*=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
                return {tokenType: MUL, text: currChar, startPos: self.position, endPos: self.position, lineNumber
                : self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
            }else if(currChar == "."){
				if (self.buffer.lookAhead() == ".") {
					currChar = self.nextLexeme();
					self.position += 1;
					if (self.buffer.lookAhead() == ".") {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: ELLIPSIS, text: "...", startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}else if (self.buffer.lookAhead() == "<"){
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: HALF_OPEN_RANGE, text: "..<", startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}
					self.tokenIndex += 1;
					return {tokenType: RANGE, text: "..", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
				return {tokenType: DOT, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
			}else if(currChar == "["){
				self.tokenIndex += 1;
				return {tokenType: LEFT_BRACKET, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			} else if (currChar == "]"){
				self.tokenIndex += 1;
				return {tokenType: RIGHT_BRACKET, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			}else if(currChar == "?"){
				if (self.buffer.lookAhead() == ":") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return { tokenType: ELVIS, text: "?:", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
				return {tokenType: QUESTION_MARK, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
			}else if (currChar == "#"){
				self.tokenIndex += 1;
				return {tokenType: HASH, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			}else if (currChar == "~"){
				self.tokenIndex += 1;
				return {tokenType: BIT_COMPLEMENT, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			}else if (currChar == "/") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: COMPOUND_DIV, text: "/=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
                return { tokenType: DIV, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
                self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
            }else if(currChar == "%"){
				self.tokenIndex += 1;
				return { tokenType: MOD, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			}else if (currChar == "!") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: REF_NOT_EQUAL, text: "!==", startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}
					self.tokenIndex += 1;
					return {tokenType: NOT_EQUAL, text: "!=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}
				self.tokenIndex += 1;
				return {tokenType: NOT, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
			}else if (currChar == ">") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: GT_EQUAL, text: ">=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}else if (self.buffer.lookAhead() == ">"){
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: COMPOUND_RIGHT_SHIFT, text: ">>=", startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}else if (self.buffer.lookAhead() == ">"){
						if (self.buffer.lookAhead() == "=") {
							currChar = self.nextLexeme();
							self.position += 1;
							self.tokenIndex += 1;
							return {tokenType: COMPOUND_LOGICAL_SHIFT, text: ">>>=", startPos: self.position - 1 , endPos:self.position, lineNumber:
							self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
						}
					}
				}
				self.tokenIndex += 1;
				return {tokenType: GT, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace() };
			}else if (currChar == "<") {
				if (self.buffer.lookAhead() == "=") {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: LT_EQUAL, text: "<=", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}else if (self.buffer.lookAhead() == "-"){
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: LARROW, text: "<-", startPos: self.position - 1 , endPos:self.position, lineNumber:
					self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
				}else if(self.buffer.lookAhead() == "<"){
					if (self.buffer.lookAhead() == "=") {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: COMPOUND_LEFT_SHIFT, text: "<<=", startPos: self.position - 1 , endPos:self.position, lineNumber:
						self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
					}
				}
				self.tokenIndex += 1;
				return {tokenType: LT, text: currChar, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
			} else if (currChar == "\"") {
                string str = currChar;
				//empty string literal - ex:""
                if (self.buffer.lookAhead() == "\"") {
                    currChar = self.nextLexeme();
                    self.position += 1;
					self.tokenIndex += 1;
                    return {tokenType: QUOTED_STRING_LITERAL, text: "\"\"", startPos: self.position - 1 , endPos: self.position,
                        lineNumber: self.lineNum, index: self.tokenIndex , whiteSpace: self.getWhiteSpace()} ;
                }
				//keeps track whether the string literal is incompelete
                boolean incompleteStr = true;
				//length of the string
                int strPos = 0;

				//run while we consume another inverted comma and complete the string literal
                while (incompleteStr) {
                    currChar = self.nextLexeme();
                    self.position += 1;
                    strPos += 1;
					//append the string with the current character
                    str = str + currChar;
                    if (self.buffer.lookAhead() == "\"") {
                        currChar = self.nextLexeme();
						self.position += 1;
						strPos += 1;
                        str = str + currChar;
                        incompleteStr = false;
						self.tokenIndex += 1;
                        return {tokenType: QUOTED_STRING_LITERAL, text: str, startPos: self.position - strPos , endPos:
                        self.position, lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
                    }
					//loop until Eof
                    if (self.buffer.lookAhead() == "") {
                        incompleteStr = false;
                        //if no inverted comma is found,all the characters until Eof is captured and returned as an Lexer Error Token
						return {tokenType: LEXER_ERROR_TOKEN, text: str, startPos: self.position - strPos , endPos:
						self.position, lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};

                    }
                }
            } else if (isDigit(currChar)) { // integers
                string numb = currChar;
				int digitPos = 0;
				//keep track if the number is a valid number
				boolean validNum = true;
				//append the number while the lookahead character is also a number
				while(isDigit(self.buffer.lookAhead())){
					currChar = self.nextLexeme();
					    self.position += 1;
					    digitPos += 1;
					    numb = numb + currChar;
				}
				//no identifier can be names with number to the front, if occured, it is returned as an lexer Error Token
				if(isLetter(self.buffer.lookAhead())){
					validNum = false;
					//consume all the characters which are letters
					while(isLetter(self.buffer.lookAhead())){
						currChar = self.nextLexeme();
						self.position += 1;
						digitPos += 1;
						numb = numb + currChar;
					}
					self.tokenIndex += 1;
					return {tokenType: LEXER_ERROR_TOKEN, text: numb, startPos: self.position - digitPos , endPos:self.position, lineNumber:
					self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
				}
				if(validNum){
					self.tokenIndex += 1;
					return {tokenType: NUMBER, text: numb, startPos: self.position - digitPos , endPos:self.position, lineNumber:
					self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
				}
            } else if (isLetter(currChar)) {
                string word = currChar;
                int wordLength = 0;
                while (isLetter(self.buffer.lookAhead()) || isDigit(self.buffer.lookAhead()) || self.buffer.lookAhead() == "_") {
                    currChar = self.nextLexeme();
                    self.position += 1;
                    wordLength += 1;
                    word = word + currChar;
                }
                int checkReserved = isReserved(word);
                if (checkReserved != EOF) {
					self.tokenIndex += 1;
                    return {tokenType: checkReserved, text: word, startPos: self.position - wordLength , endPos:self.position,
                        lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
                }
                else {
					self.tokenIndex += 1;
                    return {tokenType: IDENTIFIER, text: word, startPos: self.position - wordLength , endPos:self.position,
                        lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.getWhiteSpace() };
                }
            }
        }
		self.tokenIndex += 1;
        return {tokenType: EOF, text: "EOF", startPos: self.position, endPos: self.position, lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.getWhiteSpace()};
    }

	#if the whitespace stack is not empty, pop the stack and return the whitespace characters else return null
 	# + return - whitespace string or null
	function getWhiteSpace() returns string?{
		string space= "";

		if(self.wpStack.top != 0){
			while(self.wpStack.top > 0){
				space = self.wpStack.pop();
				//space = self.wpStack.pop() + space;
			}
			return space;
		} else {
			return null;
		}
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

#check whether the current character is a white space
# + return -  boolean true if the character is a whitepsace character
function isWhiteSpace(string currentCharacter) returns boolean {
    string spaceRegEx = "[ \t\r\n\f]";
    boolean|error whiteSp = currentCharacter.matches(spaceRegEx);

    if (whiteSp is error) {
        panic whiteSp;
    } else {
        return whiteSp;
    }
}

#check whether the current character is a Enter
# + return - boolean true if character is newline whitespace
function isEnter(string currentCharacter) returns boolean {
    string enterRegEx = "[\n]";
    boolean|error enterSpace = currentCharacter.matches(enterRegEx);

    if (enterSpace is error) {
        panic enterSpace;
    } else {
        return enterSpace;
    }
}
#check whether the current character is a tab space
# + return - boolean true if character is tab whitespace
function isTab(string currentCharacter) returns boolean {
    string tabRegEx = "[\t]";
    boolean|error enterTabSpace = currentCharacter.matches(tabRegEx);

    if (enterTabSpace is error) {
        panic enterTabSpace;
    } else {
        return enterTabSpace;
    }
}

#check whether the current character is a digit
# + return - boolean true if character is a digit
function isDigit(string currentChar) returns boolean {
    string regExNumber = "[0-9]";
    boolean|error isNumb = currentChar.matches(regExNumber);

    if (isNumb is error) {
        panic isNumb;
    } else {
        return isNumb;
    }
}

#check whether the current character is a letter based on regex
# + return - boolean true if character is a letter
function isLetter(string currentChar) returns boolean {
    string regExString = "[a-zA-Z]+";
    boolean|error letter = currentChar.matches(regExString);

    if (letter is error) {
        panic letter;
    } else {
        return letter;
    }
}

#check whether the built word is a reserved word or not
# + return - int of the reserved word
function isReserved(string word) returns int {
    int resWord = EOF;

    match word {
        "function" => resWord = FUNCTION;
        "int" => resWord = INT;
        "string" => resWord = STRING;
		"untaint" => resWord = UNTAINT;
		"final" => resWord = FINAL;
    }

    return resWord;
}

#if a whitespace is reached it will be pushed to the stack.
#once getWhitespace() method is called
#stack will be popped and all the whitespaces which were infront of the character will be added to the token whitespcae field
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

