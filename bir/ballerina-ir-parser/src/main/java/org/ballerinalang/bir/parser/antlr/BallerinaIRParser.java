// Generated from BallerinaIRParser.g4 by ANTLR 4.5.3
package org.ballerinalang.bir.parser.antlr;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BallerinaIRParser extends Parser {
    static {
        RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            PACKAGE = 1, TYPE = 2, STRING = 3, INT = 4, FUNCTION = 5, BB = 6, LEFT_BRACE = 7, RIGHT_BRACE = 8,
            QUOTE = 9, LEFT_PARENTHESIS = 10, RIGHT_PARENTHESIS = 11, Identifier = 12, WS = 13,
            NEW_LINE = 14, LINE_COMMENT = 15;
    public static final int
            RULE_irPackage = 0, RULE_function = 1, RULE_basicBlock = 2;
    public static final String[] ruleNames = {
            "irPackage", "function", "basicBlock"
    };

    private static final String[] _LITERAL_NAMES = {
            null, "'package'", "'type'", "'string'", "'int'", "'function'", null,
            "'{'", "'}'", "'\"'", "'('", "')'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, "PACKAGE", "TYPE", "STRING", "INT", "FUNCTION", "BB", "LEFT_BRACE",
            "RIGHT_BRACE", "QUOTE", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS", "Identifier",
            "WS", "NEW_LINE", "LINE_COMMENT"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "BallerinaIRParser.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public BallerinaIRParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class IrPackageContext extends ParserRuleContext {
        public TerminalNode PACKAGE() {
            return getToken(BallerinaIRParser.PACKAGE, 0);
        }

        public TerminalNode LEFT_BRACE() {
            return getToken(BallerinaIRParser.LEFT_BRACE, 0);
        }

        public TerminalNode RIGHT_BRACE() {
            return getToken(BallerinaIRParser.RIGHT_BRACE, 0);
        }

        public TerminalNode EOF() {
            return getToken(BallerinaIRParser.EOF, 0);
        }

        public TerminalNode Identifier() {
            return getToken(BallerinaIRParser.Identifier, 0);
        }

        public List<FunctionContext> function() {
            return getRuleContexts(FunctionContext.class);
        }

        public FunctionContext function(int i) {
            return getRuleContext(FunctionContext.class, i);
        }

        public IrPackageContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_irPackage;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).enterIrPackage(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).exitIrPackage(this);
        }
    }

    public final IrPackageContext irPackage() throws RecognitionException {
        IrPackageContext _localctx = new IrPackageContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_irPackage);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(6);
                match(PACKAGE);
                setState(8);
                _la = _input.LA(1);
                if (_la == Identifier) {
                    {
                        setState(7);
                        match(Identifier);
                    }
                }

                setState(10);
                match(LEFT_BRACE);
                setState(14);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == FUNCTION) {
                    {
                        {
                            setState(11);
                            function();
                        }
                    }
                    setState(16);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(17);
                match(RIGHT_BRACE);
                setState(18);
                match(EOF);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FunctionContext extends ParserRuleContext {
        public TerminalNode FUNCTION() {
            return getToken(BallerinaIRParser.FUNCTION, 0);
        }

        public TerminalNode Identifier() {
            return getToken(BallerinaIRParser.Identifier, 0);
        }

        public TerminalNode LEFT_PARENTHESIS() {
            return getToken(BallerinaIRParser.LEFT_PARENTHESIS, 0);
        }

        public TerminalNode RIGHT_PARENTHESIS() {
            return getToken(BallerinaIRParser.RIGHT_PARENTHESIS, 0);
        }

        public TerminalNode LEFT_BRACE() {
            return getToken(BallerinaIRParser.LEFT_BRACE, 0);
        }

        public BasicBlockContext basicBlock() {
            return getRuleContext(BasicBlockContext.class, 0);
        }

        public TerminalNode RIGHT_BRACE() {
            return getToken(BallerinaIRParser.RIGHT_BRACE, 0);
        }

        public FunctionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_function;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).enterFunction(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).exitFunction(this);
        }
    }

    public final FunctionContext function() throws RecognitionException {
        FunctionContext _localctx = new FunctionContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_function);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(20);
                match(FUNCTION);
                setState(21);
                match(Identifier);
                setState(22);
                match(LEFT_PARENTHESIS);
                setState(23);
                match(RIGHT_PARENTHESIS);
                setState(24);
                match(LEFT_BRACE);
                setState(25);
                basicBlock();
                setState(26);
                match(RIGHT_BRACE);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class BasicBlockContext extends ParserRuleContext {
        public TerminalNode BB() {
            return getToken(BallerinaIRParser.BB, 0);
        }

        public TerminalNode LEFT_BRACE() {
            return getToken(BallerinaIRParser.LEFT_BRACE, 0);
        }

        public TerminalNode RIGHT_BRACE() {
            return getToken(BallerinaIRParser.RIGHT_BRACE, 0);
        }

        public BasicBlockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_basicBlock;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).enterBasicBlock(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof BallerinaIRParserListener)
                ((BallerinaIRParserListener) listener).exitBasicBlock(this);
        }
    }

    public final BasicBlockContext basicBlock() throws RecognitionException {
        BasicBlockContext _localctx = new BasicBlockContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_basicBlock);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(28);
                match(BB);
                setState(29);
                match(LEFT_BRACE);
                setState(30);
                match(RIGHT_BRACE);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static final String _serializedATN =
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\21#\4\2\t\2\4\3\t" +
            "\3\4\4\t\4\3\2\3\2\5\2\13\n\2\3\2\3\2\7\2\17\n\2\f\2\16\2\22\13\2\3\2" +
            "\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\2\2\5\2\4" +
            "\6\2\2!\2\b\3\2\2\2\4\26\3\2\2\2\6\36\3\2\2\2\b\n\7\3\2\2\t\13\7\16\2" +
            "\2\n\t\3\2\2\2\n\13\3\2\2\2\13\f\3\2\2\2\f\20\7\t\2\2\r\17\5\4\3\2\16" +
            "\r\3\2\2\2\17\22\3\2\2\2\20\16\3\2\2\2\20\21\3\2\2\2\21\23\3\2\2\2\22" +
            "\20\3\2\2\2\23\24\7\n\2\2\24\25\7\2\2\3\25\3\3\2\2\2\26\27\7\7\2\2\27" +
            "\30\7\16\2\2\30\31\7\f\2\2\31\32\7\r\2\2\32\33\7\t\2\2\33\34\5\6\4\2\34" +
            "\35\7\n\2\2\35\5\3\2\2\2\36\37\7\b\2\2\37 \7\t\2\2 !\7\n\2\2!\7\3\2\2" +
            "\2\4\n\20";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}