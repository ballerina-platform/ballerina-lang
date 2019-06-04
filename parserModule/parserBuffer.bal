import ballerina/io;

#The lexemes which are tokenized are pushed to the parser buffer reader.
#ParserBufferReader functions as a circular buffer and the capacity of the buffer can be specified.
public type ParserBufferReader object {
    //size of the token array
    private int capacity;
    //pointer which reads the token array
    private int readPointer = 0;
    //counter to fill the buffer initialy
    private int bufferSize = 0;
    private Lexer lex2;
    private Token[] tokenArray = [];

    public function __init(Lexer lex2, int capacity = 5) {
        self.lex2 = lex2;
        self.capacity = capacity;
        self.fillTokenBuffer();
    }

    //fill the token array
    function fillTokenBuffer() {
        while (self.bufferSize < self.capacity) {
            Token token =self.lex2.nextToken();
            self.tokenArray[self.bufferSize] = token;
            if (token.tokenType == EOF) {
                break;
            }
            self.bufferSize = self.bufferSize + 1;
        }
    }

    function isEOFToken() returns boolean {
        boolean isEof = false;
        if (self.tokenArray[self.readPointer].tokenType == EOF) {
            isEof = true;
        }

        return isEof;
    }

    function consumeToken() returns Token {
        Token returnToken = self.tokenArray[self.readPointer];
        //a token will be stored , to replace the cosumed token
        self.storeToken();
        self.readPointer = (self.readPointer + 1) % self.capacity;

        return returnToken;
    }

    function storeToken() {
        self.tokenArray[self.readPointer] = self.lex2.nextToken();
    }

    function lookAheadToken(int lookAheadCount = 1) returns Token {
        int a = (self.readPointer - 1 + lookAheadCount) % self.capacity;
        return self.tokenArray[a];
    }
    function printTokenBuffer() {
            foreach Token item in self.tokenArray {
                io:println(item);
            }
    }

};