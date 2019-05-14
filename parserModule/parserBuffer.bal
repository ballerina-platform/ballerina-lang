import ballerina/io;

public type ParserBufferReader object {
    private int capacity;
    private int readToken = 0;
    private int bufferSize = 0;
    private Lexer lex2;
    private Token[] tokenArray = [];

    public function __init(Lexer lex2, int capacity = 5) {
        self.lex2 = lex2;
        self.capacity = capacity;
        self.fillTokenBuffer();
    }

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

    function printTokenBuffer() {
        foreach Token item in self.tokenArray {
            io:println(item);
        }
    }

    function isEOFToken() returns boolean {
        boolean checker = false;
        if (self.tokenArray[self.readToken].tokenType == EOF) {
            checker = true;
        }
        return checker;
    }
    function consumeToken() returns Token {
        Token returnToken = self.tokenArray[self.readToken];
        self.storeToken();
        self.readToken = (self.readToken + 1) % self.capacity;
        return returnToken;
    }
    function storeToken() {
        self.tokenArray[self.readToken] = self.lex2.nextToken();
    }
    function lookAheadToken(int lookAheadCount = 1) returns Token {
        int a = (self.readToken - 1 + lookAheadCount) % self.capacity;
        return self.tokenArray[a];
    }

};