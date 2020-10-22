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

# The lexemes which are tokenized are pushed to the parser buffer reader.
# ParserBufferReader functions as a circular buffer and the capacity of the buffer can be specified.
public class ParserBufferReader {
    //size of the token array
    private int capacity;
    //pointer which reads the token array
    private int readPointer = 0;
    //counter to fill the buffer initially
    private int bufferSize = 0;
    private Lexer lexer;
    private Token[] tokenArray = [];

    public function init(Lexer lexer, int capacity = 5) {
        self.lexer = lexer;
        self.capacity = capacity;
        self.fillTokenBuffer();
    }

    //fill the token array
    function fillTokenBuffer() {
        while (self.bufferSize < self.capacity) {
            Token token = self.lexer.nextToken();
            self.tokenArray[self.bufferSize] = token;
            if (token.tokenType == EOF) {
                break;
            }
            self.bufferSize = self.bufferSize + 1;
        }
    }

    function isEOFToken() returns boolean {
        return self.tokenArray[self.readPointer].tokenType == EOF;
    }

    function consumeToken() returns Token {
        Token returnToken = self.tokenArray[self.readPointer];
        //a token will be stored , to replace the cosumed token
        self.storeToken();
        self.readPointer = (self.readPointer + 1) % self.capacity;
        return returnToken;
    }

    function storeToken() {
        self.tokenArray[self.readPointer] = self.lexer.nextToken();
    }

    function lookAheadToken(int lookAheadCount = 1) returns Token {
        int a = (self.readPointer - 1 + lookAheadCount) % self.capacity;
        return self.tokenArray[a];
    }
}
