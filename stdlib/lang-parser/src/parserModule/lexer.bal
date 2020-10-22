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

import ballerina/stringutils;

# Lexer, tokenize the lexemes.
# the characters pulled from the bufferReader is passed through
# symbol recognizers, identifers, reserved words and numeric recognizers
# to build tokens
public class Lexer {
    TokenMapper tokenMapper = new;
    //token position relative to the line number
    int position = 0;
    //line number of the token in the source
    int lineNum = 1;
    //index of the token relative to the input file
    int tokenIndex = 0;
    //initializing the whitespace stack
    WhiteSpaceStack wsStack = new;
    //instance of the bufferReader object
    private BufferReader buffer;

    public function init(BufferReader buffer) {
        self.buffer = buffer;
    }

    //consume and return the character from the buffer
    public function nextLexeme() returns @tainted string {
        return self.buffer.consume();
    }

    # The characters from the buffer stream will be tokenized and a token will be returned.
    #
    # + return - Token
    public function nextToken() returns @tainted Token {
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
                    self.wsStack.push(currChar);
                } else if (isTab(currChar)) {
                    self.position = self.position + 3;
                    self.wsStack.push(currChar);
                } else {
                    self.wsStack.push(currChar);
                }
                continue;
            }

            //tokenization for symbols
            string validSymbol = currChar;
            //track if the symbols is in the symbol map
            boolean isValidSymbol = true;
            //count to track the length of the symbol
            int endPosCount = 0;
            //check if the token is found in the map
            var tokenSymbol = self.tokenMapper.tokenMap[validSymbol];

            if (tokenSymbol is int) {
                endPosCount += 1;
                //check EOF
                if (self.buffer.lookAhead() == END_OF_FILE) {
                    isValidSymbol = false;
                }
                while (isValidSymbol) {
                    //check EOF
                    if (self.buffer.lookAhead() == END_OF_FILE) {
                        break;
                    }
                    //check with the look ahead token to see if there is a valid symbol
                    string validSymbol2 = validSymbol + self.buffer.lookAhead();
                    var tokenSymbol2 = self.tokenMapper.tokenMap[validSymbol2];
                    if (tokenSymbol2 is int) {
                        endPosCount += 1;
                        //consume the lexeme
                        currChar = self.nextLexeme();
                        self.position += 1;
                        validSymbol = validSymbol2;
                        tokenSymbol = tokenSymbol2;
                    } else {
                        isValidSymbol = false;
                    }
                }
                //check if the token is an incomplete token,(no valid token found)
                if (tokenSymbol >= 61 && tokenSymbol <= 70) {
                    self.tokenIndex += 1;
                    return self.getToken(LEXER_ERROR_TOKEN, validSymbol, self.position - (endPosCount - 1));
                } else {
                    self.tokenIndex += 1;
                    return self.getToken(tokenSymbol, validSymbol, self.position - (endPosCount - 1));
                }
            } else if (currChar == singleQuoteSym) { //quoted string literal
                string stringLiteral = currChar;
                //empty string literal - ex:""
                if (self.buffer.lookAhead() == singleQuoteSym) {
                    currChar = self.nextLexeme();
                    self.position += 1;
                    self.tokenIndex += 1;
                    return self.getToken(QUOTED_STRING_LITERAL, quotesStringSym, self.position - 1);
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
                    stringLiteral = stringLiteral + currChar;
                    if (self.buffer.lookAhead() == singleQuoteSym) {
                        currChar = self.nextLexeme();
                        self.position += 1;
                        strPos += 1;
                        stringLiteral = stringLiteral + currChar;
                        incompleteStr = false;
                        self.tokenIndex += 1;
                        return self.getToken(QUOTED_STRING_LITERAL, stringLiteral, self.position - strPos);
                    }
                    //loop until Eof
                    if (self.buffer.lookAhead() == END_OF_FILE) {
                        incompleteStr = false;
                        //if no inverted comma is found,
                        //all the characters until Eof is captured and returned as an Lexer Error Token
                        return self.getToken(LEXER_ERROR_TOKEN, stringLiteral, self.position - strPos);
                    }
                }
            } else if (isDigit(currChar)) { // integers
                string numb = currChar;
                int digitPos = 0;
                //keep track if the number is a valid number
                boolean validNum = true;
                //append the number while the lookahead character is also a number
                while (isDigit(self.buffer.lookAhead())) {
                    currChar = self.nextLexeme();
                    self.position += 1;
                    digitPos += 1;
                    numb = numb + currChar;
                }
                //no identifier can be names with number to the front, if occurred, it is returned as an lexer Error Token
                if (isLetter(self.buffer.lookAhead())) {
                    //consume all the characters which are letters
                    while (isLetter(self.buffer.lookAhead())) {
                        currChar = self.nextLexeme();
                        self.position += 1;
                        digitPos += 1;
                        numb = numb + currChar;
                    }
                    self.tokenIndex += 1;
                    return self.getToken(LEXER_ERROR_TOKEN, numb, self.position - digitPos);
                }
                if (validNum) {
                    self.tokenIndex += 1;
                    return self.getToken(NUMBER, numb, self.position - digitPos);
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

                int checkReserved = getReservedKey(word);
                if (checkReserved != EOF) {
                    self.tokenIndex += 1;
                    return self.getToken(checkReserved, word, self.position - wordLength);
                } else {
                    self.tokenIndex += 1;
                    return self.getToken(IDENTIFIER, word, self.position - wordLength);
                }
            }
        }
        self.tokenIndex += 1;
        self.position += 1;
        return self.getToken(EOF, eofSym, self.position);
    }

    # Method to build the Token based on the given parameters.
    #
    # + tkType - Token type.
    # + tkText - Token text.
    # + startPosition - Starting position of the token.
    # + return - Token.
    function getToken(int tkType, string tkText, int startPosition) returns Token {
        return {
            tokenType: tkType,
            text: tkText,
            startPos: startPosition,
            endPos: self.position,
            lineNumber: self.lineNum,
            index: self.tokenIndex,
            whiteSpace: self.wsStack.getWhiteSpace()
        }
    }
};

# Token record.
#
# + tokenType - type of the Token.
# + text - input text.
# + startPos - starting column number of the token.
# + endPos - ending column number of the token.
# + lineNumber - lineNumber with respect to the source code.
# + index - index of the token with respect to the source code.
# + whiteSpace - whiteSpace prior to the token is attached here.
public type Token record {
    int tokenType;
    string text;
    int startPos;
    int endPos;
    int lineNumber;
    int index;
    string? whiteSpace;
};

# Checks whether the current character is a white space.
#
# + return - boolean true if the character is a whitespace character.
function isWhiteSpace(string currentCharacter) returns boolean {
    string spaceRegEx = "[ \t\r\n\f]";
    return stringutils:matches(currentCharacter, spaceRegEx);
}

# Checks whether the current character is a Enter.
#
# + return - boolean true if character is newline whitespace.
function isEnter(string currentCharacter) returns boolean {
    string enterRegEx = "[\n]";
    return stringutils:matches(currentCharacter, enterRegEx);
}

# Checks whether the current character is a tab space.
#
# + return - boolean true if character is tab whitespace.
function isTab(string currentCharacter) returns boolean {
    string tabRegEx = "[\t]";
    return stringutils:matches(currentCharacter, tabRegEx);
}

# Checks whether the current character is a digit.
#
# + return - boolean true if character is a digit.
function isDigit(string currentChar) returns boolean {
    string regExNumber = "[0-9]";
    return stringutils:matches(currentChar, regExNumber);
}

# Checks whether the current character is a letter based on regex.
#
# + return - boolean true if character is a letter.
function isLetter(string currentChar) returns boolean {
    string regExString = "[a-zA-Z]+";
    return stringutils:matches(currentChar, regExString);
}

# Checks whether the built word is a reserved word or not.
#
# + return - int of the reserved word.
function getReservedKey(string word) returns int {
    match word {
        "function" => {
            return FUNCTION;
        }
        "int" => {
            return INT;
        }
        "string" => {
            return STRING;
        }
        "untaint" => {
            return UNTAINT;
        }
        "final" => {
            return FINAL;
        }
        "continue" => {
            return CONTINUE;
        }
    }
    return EOF;
}

# If a whitespace is reached it will be pushed to the stack.
# once getWhitespace() method is called
# stack will be popped and all the whitespaces which were infront of the character will be added to the token whitespcae field
class WhiteSpaceStack {
    string[] spaceStack = [];
    int top;

    public function init(int top = 0) {
        self.top = top;
    }

    function push(string wSpace1) {
        self.spaceStack[self.top] = wSpace1;
        self.top = self.top + 1;
    }

    function pop() returns string {
        self.top = self.top - 1;
        return self.spaceStack[self.top];
    }

    # If the whitespace stack is not empty, pop the stack and return the whitespace characters else return null.
    #
    # + return - whitespace string or null
    function getWhiteSpace() returns string? {
        string space = "";

        if (self.top != 0) {
            while (self.top > 0) {
                space = self.pop() + space;
            }
            return space;
        }
    }
}
