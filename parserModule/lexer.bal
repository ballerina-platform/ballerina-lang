import ballerina/io;
import ballerina/log;

# Lexer tokenize the lexemes.
# the characters pulled from the bufferReader is passed through
# symbol recognizers, identifers, reesrved words and numeric recognizers
# to build tokens
public type Lexer object {
	TokenMapper tknMapper = new ();
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

	#the characters from the buffer stream will be tokenized and a token will be returned
 	# +return - Token
    public function nextToken() returns Token{
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

			//build symbols based on a map lookup
            var singleTokenSy = self.tknMapper.singleSymbol[currChar];
			//single token symbols
            if(singleTokenSy is int){
				string doubleSymbol = currChar + self.buffer.lookAhead();
				var doubleTokenSy = self.tknMapper.doubleSymbol[doubleSymbol];
				//double token symbols
				if(doubleTokenSy is int){
					currChar = self.nextLexeme();
                    self.position += 1;
					string tripleSymbol = doubleSymbol + self.buffer.lookAhead();
					var tripleTokenSy = self.tknMapper.tripleSymbol[tripleSymbol];
					//tripe token symbols
					if(tripleTokenSy is int){
						currChar = self.nextLexeme();
                        self.position += 1;
                        self.tokenIndex += 1;
                        return {tokenType: tripleTokenSy, text: tripleSymbol, startPos: self.position - 2 , endPos:self.position,
                              lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
					}
					self.tokenIndex += 1;
					return {tokenType: doubleTokenSy, text: doubleSymbol, startPos: self.position - 1 , endPos:self.position,
						lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
				}
            	self.tokenIndex += 1;
            	return {tokenType: singleTokenSy, text: currChar, startPos: self.position, endPos: self.position,
                					lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace()};
            } else if (currChar == gtSymbol) {
				if (self.buffer.lookAhead() == assignSym) {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: GT_EQUAL, text: gtEqSym, startPos: self.position - 1 , endPos:self.position,
						lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
				} else if (self.buffer.lookAhead() == gtSymbol){
					if (self.buffer.lookAhead() == assignSym) {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: COMPOUND_RIGHT_SHIFT, text: compRShiftSym, startPos: self.position - 2 , endPos:self.position,
							lineNumber: self.lineNum, index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
					} else if (self.buffer.lookAhead() == gtSymbol){
						if (self.buffer.lookAhead() == assignSym) {
							currChar = self.nextLexeme();
							self.position += 1;
							self.tokenIndex += 1;
							return {tokenType: COMPOUND_LOGICAL_SHIFT, text: compLogicalSym, startPos: self.position - 1 ,
								endPos:self.position, lineNumber: self.lineNum , index: self.tokenIndex,
								whiteSpace: self.wpStack.getWhiteSpace() };
						}
					}
				}
				self.tokenIndex += 1;
				return {tokenType: GT, text: gtSymbol, startPos: self.position, endPos: self.position,
					lineNumber: self.lineNum, index: self.tokenIndex , whiteSpace: self.wpStack.getWhiteSpace() };
			} else if (currChar == ltSymbol) {
				if (self.buffer.lookAhead() == assignSym) {
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: LT_EQUAL, text: ltEqSym, startPos: self.position - 1 , endPos:self.position,
						lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
				} else if (self.buffer.lookAhead() == subSymbol){
					currChar = self.nextLexeme();
					self.position += 1;
					self.tokenIndex += 1;
					return {tokenType: LARROW, text: larrowSym, startPos: self.position - 1 , endPos:self.position,
						lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
				} else if(self.buffer.lookAhead() == ltSymbol){
					if (self.buffer.lookAhead() == assignSym) {
						currChar = self.nextLexeme();
						self.position += 1;
						self.tokenIndex += 1;
						return {tokenType: COMPOUND_LEFT_SHIFT, text: comLShiftSym, startPos: self.position - 1 , endPos:self.position,
							lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
					}
				}
				self.tokenIndex += 1;
				return {tokenType: LT, text: ltSymbol, startPos: self.position, endPos: self.position, lineNumber:
				self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
			} else if (currChar == singleQuoteSym) {
                string str = currChar;
				//empty string literal - ex:""
                if (self.buffer.lookAhead() == singleQuoteSym) {
                    currChar = self.nextLexeme();
                    self.position += 1;
					self.tokenIndex += 1;
                    return {tokenType: QUOTED_STRING_LITERAL, text: quotesStringSym, startPos: self.position - 1 , endPos: self.position,
                        lineNumber: self.lineNum, index: self.tokenIndex , whiteSpace: self.wpStack.getWhiteSpace()} ;
                }
				//keeps track whether the string literal is incomplete
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
                    if (self.buffer.lookAhead() == singleQuoteSym) {
                        currChar = self.nextLexeme();
						self.position += 1;
						strPos += 1;
                        str = str + currChar;
                        incompleteStr = false;
						self.tokenIndex += 1;
                        return {tokenType: QUOTED_STRING_LITERAL, text: str, startPos: self.position - strPos ,
							endPos: self.position, lineNumber: self.lineNum  , index: self.tokenIndex,
							whiteSpace: self.wpStack.getWhiteSpace()};
                    }
					//loop until Eof
                    if (self.buffer.lookAhead() == "") {
                        incompleteStr = false;
                        //if no inverted comma is found,all the characters until Eof is captured and returned as an Lexer Error Token
						return {tokenType: LEXER_ERROR_TOKEN, text: str, startPos: self.position - strPos ,
							endPos: self.position, lineNumber: self.lineNum  , index: self.tokenIndex,
							whiteSpace: self.wpStack.getWhiteSpace()};

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
					return {tokenType: LEXER_ERROR_TOKEN, text: numb, startPos: self.position - digitPos ,
						endPos:self.position, lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace()};
				}
				if(validNum){
					self.tokenIndex += 1;
					return {tokenType: NUMBER, text: numb, startPos: self.position - digitPos , endPos:self.position,
						lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace()};
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
                        lineNumber: self.lineNum  , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace()};
                } else {
					self.tokenIndex += 1;
                    return {tokenType: IDENTIFIER, text: word, startPos: self.position - wordLength , endPos:self.position,
                        lineNumber: self.lineNum , index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace() };
                }
            }
        }
		self.tokenIndex += 1;
        return {tokenType: EOF, text: eofSym, startPos: self.position, endPos: self.position, lineNumber: self.lineNum,
			index: self.tokenIndex, whiteSpace: self.wpStack.getWhiteSpace()};
    }
};


# Token record
#
# + tokenType - type of the Token 
# + text - input text 
# + startPos - starting column number of the token
# + endPos - ending column number of the token
# + lineNumber - lineNumber with respect to the source code
# + index - index of the token with respect to the source code 
# + whiteSpace - whiteSpace prior to the token is attached here
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
        "function" =>resWord = FUNCTION;
        "int" =>resWord = INT;
        "string" => resWord = STRING;
		"untaint" => resWord = UNTAINT;
		"final" => resWord = FINAL;
		"continue" => resWord = CONTINUE;
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

   #if the whitespace stack is not empty, pop the stack and return the whitespace characters else return null
   # + return - whitespace string or null
   	function getWhiteSpace() returns string?{
   		string space= "";
   
   		if(self.top != 0){
   			while(self.top > 0){
   				space = self.pop();
   				//space = self.wpStack.pop() + space;
   			}
   			return space;
   		}
   		return ();
   
   	}
};
