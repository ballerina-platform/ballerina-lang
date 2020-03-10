/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.internal.parser.incremental;

import io.ballerinalang.compiler.internal.parser.CharReader;
import io.ballerinalang.compiler.internal.parser.tree.STIdentifier;
import io.ballerinalang.compiler.internal.parser.tree.STLiteralValueToken;
import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.STNodeList;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxTrivia;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;

import java.util.ArrayList;
import java.util.List;

import static io.ballerinalang.compiler.internal.parser.tree.SyntaxKind.IDENTIFIER_TOKEN;

public class BallerinaLexer {
    private CharReader reader;

    public BallerinaLexer(CharReader reader) {
        this.reader = reader;
    }

    public STToken lexSyntaxToken() {
        // Read leading trivia
        List<STNode> leadingTriviaList = new ArrayList<>(10);
        lexSyntaxTrivia(leadingTriviaList, true);

        TokenDataHolder tokenData = lexSyntaxTokenInternal();

        // Read trailing trivia
        List<STNode> trailingTriviaList = new ArrayList<>(10);
        lexSyntaxTrivia(trailingTriviaList, false);
        return createToken(tokenData, leadingTriviaList, trailingTriviaList);
    }

    // TODO We should be able to set the lexer state, mode etc
    public void reset(int offset) {
        reader.reset(offset);
    }

    private STToken createToken(TokenDataHolder tokenData, List<STNode> leadingTrivia,
                                List<STNode> trailingTrivia) {
        STNodeList leading = new STNodeList(leadingTrivia);
        STNodeList trailing = new STNodeList(trailingTrivia);

        switch (tokenData.kind) {
            case IDENTIFIER_TOKEN:
                return new STIdentifier(tokenData.text, leading, trailing);
            case NUMERIC_LITERAL_TOKEN:
                // TODO improve this to support other literal token types
                return new STLiteralValueToken(tokenData.kind, tokenData.text,
                        tokenData.intValue, leading, trailing);
            default:
                // Keywords and other tokens such as . ; {
                return new STToken(tokenData.kind, leading, trailing);
        }
    }

    private TokenDataHolder lexSyntaxTokenInternal() {
        TokenDataHolder tokenData = new TokenDataHolder();
        reader.mark();
        char c = reader.peek();
        switch (c) {
            case ';':
                reader.advance();
                tokenData.kind = SyntaxKind.SEMICOLON_TOKEN;
                break;
            case '{':
                reader.advance();
                tokenData.kind = SyntaxKind.OPEN_BRACE_TOKEN;
                break;
            case '}':
                reader.advance();
                tokenData.kind = SyntaxKind.CLOSE_BRACE_TOKEN;
                break;
            case '[':
                reader.advance();
                tokenData.kind = SyntaxKind.OPEN_BRACKET_TOKEN;
                break;
            case ']':
                reader.advance();
                tokenData.kind = SyntaxKind.CLOSE_BRACKET_TOKEN;
                break;
            case '(':
                reader.advance();
                tokenData.kind = SyntaxKind.OPEN_PAREN_TOKEN;
                break;
            case ')':
                reader.advance();
                tokenData.kind = SyntaxKind.CLOSE_PAREN_TOKEN;
                break;
            case '.':
                reader.advance();
                tokenData.kind = SyntaxKind.DOT_TOKEN;
                break;
            case '=':
                reader.advance();
                tokenData.kind = SyntaxKind.EQUAL_TOKEN;
                break;
            case '+':
                reader.advance();
                tokenData.kind = SyntaxKind.PLUS_TOKEN;
                break;
            case '-':
                reader.advance();
                tokenData.kind = SyntaxKind.MINUS_TOKEN;
                break;
            case '/':
                reader.advance();
                tokenData.kind = SyntaxKind.SLASH_TOKEN;
                break;
            case '%':
                reader.advance();
                tokenData.kind = SyntaxKind.PERCENT_TOKEN;
                break;
            case '*':
                reader.advance();
                tokenData.kind = SyntaxKind.ASTERISK_TOKEN;
                break;

            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case '_':
                scanKeywordOrIdentifier(tokenData);
                break;
            case '0':
                reader.advance();
                tokenData.kind = SyntaxKind.NUMERIC_LITERAL_TOKEN;
                tokenData.text = "0";
                tokenData.intValue = 0;
                // TODO handle this case.
                break;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                scanNumber(tokenData);
                break;
            default:
                if (reader.isEOF()) {
                    tokenData.kind = SyntaxKind.EOF_TOKEN;
                }
                // TODO Handle
        }
        return tokenData;
    }

    private void lexSyntaxTrivia(List<STNode> triviaList, boolean isLeading) {
        while (true) {
            reader.mark();
            char c = reader.peek();
            switch (c) {
                case ' ':
                case '\t':
                case '\f':
                    this.addTrivia(scanWhitespaces(), triviaList);
                    break;
                case '\r':
                case '\n':
                    this.addTrivia(scanEndOfLine(), triviaList);
                    if (isLeading) {
                        break;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private SyntaxTrivia scanWhitespaces() {
        while (true) {
            boolean done = false;
            char c = reader.peek();
            switch (c) {
                case ' ':
                case '\t':
                case '\f':
                    reader.advance();
                    break;
                case '\r':
                case '\n':
                    done = true;
                    break;
                default:
                    done = true;
            }
            if (done) {
                // TODO use SyntaxNodeFactory to create nodes and tokens
                return new SyntaxTrivia(SyntaxKind.WHITESPACE_TRIVIA, reader.getMarkedChars());
            }
        }
    }

    private SyntaxTrivia scanEndOfLine() {
        char c = reader.peek();
        switch (c) {
            case '\r':
                reader.advance();
                if (reader.peek() == '\n') {
                    reader.advance();
                }
                return new SyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, reader.getMarkedChars());
            case '\n':
                reader.advance();
                return new SyntaxTrivia(SyntaxKind.END_OF_LINE_TRIVIA, reader.getMarkedChars());
            default:
                return null;
        }
    }

    private void scanKeywordOrIdentifier(TokenDataHolder tokenData) {
        reader.advance();
        while (true) {
            boolean done = false;
            char c = reader.peek();
            switch (c) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '_':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    reader.advance();
                    break;
                default:
                    // TODO handle unicode and other cases here..
                    done = true;
            }

            if (done) {
                tokenData.text = reader.getMarkedChars();
                tokenData.kind = SyntaxUtils.keywordKind(tokenData.text);
                if (tokenData.kind == SyntaxKind.NONE) {
                    tokenData.kind = IDENTIFIER_TOKEN;
                }
                return;
            }
        }
    }

    private void scanNumber(TokenDataHolder tokenData) {
        reader.advance();

        while (true) {
            boolean done = false;
            char c = reader.peek();
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    reader.advance();
                    break;
                default: {
                    done = true;
                }
            }
            if (done) {
                tokenData.text = reader.getMarkedChars();
                tokenData.kind = SyntaxKind.NUMERIC_LITERAL_TOKEN;
                tokenData.intValue = Long.parseLong(tokenData.text);
                return;
            }
        }
    }

    // TODO We should use an abstraction for List<SyntaxTrivia>
    private void addTrivia(SyntaxTrivia trivia, List<STNode> triviaList) {
        if (trivia == null) {
            return;
        }

        triviaList.add(trivia);
    }

    private class TokenDataHolder {
        SyntaxKind kind;
        String text;
        long intValue;
    }
}
