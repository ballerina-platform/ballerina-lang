// Generated from BallerinaIRParser.g4 by ANTLR 4.5.3
package org.ballerinalang.bir.parser.antlr;

import org.antlr.v4.runtime.NoViableAltException;
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
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
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
			PACKAGE = 1, TYPE = 2, FUNCTION = 3, STRING = 4, INT = 5, BB = 6, GOTO = 7, RETURN = 8,
			LEFT_BRACE = 9, RIGHT_BRACE = 10, QUOTE = 11, LEFT_PARENTHESIS = 12, RIGHT_PARENTHESIS = 13,
			SEMICOLON = 14, Identifier = 15, WS = 16, NEW_LINE = 17, LINE_COMMENT = 18;
	public static final int
			RULE_irPackage = 0, RULE_function = 1, RULE_basicBlock = 2, RULE_op = 3,
			RULE_opGoTo = 4, RULE_opReturn = 5;
	public static final String[] ruleNames = {
			"irPackage", "function", "basicBlock", "op", "opGoTo", "opReturn"
	};

	private static final String[] _LITERAL_NAMES = {
			null, "'package'", "'type'", "'function'", "'string'", "'int'", null,
			"'goto'", "'return'", "'{'", "'}'", "'\"'", "'('", "')'", "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
			null, "PACKAGE", "TYPE", "FUNCTION", "STRING", "INT", "BB", "GOTO", "RETURN",
			"LEFT_BRACE", "RIGHT_BRACE", "QUOTE", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS",
			"SEMICOLON", "Identifier", "WS", "NEW_LINE", "LINE_COMMENT"
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
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitIrPackage(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IrPackageContext irPackage() throws RecognitionException {
		IrPackageContext _localctx = new IrPackageContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_irPackage);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(12);
				match(PACKAGE);
				setState(14);
				_la = _input.LA(1);
				if (_la == Identifier) {
					{
						setState(13);
						match(Identifier);
					}
				}

				setState(16);
				match(LEFT_BRACE);
				setState(20);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == FUNCTION) {
					{
						{
							setState(17);
							function();
						}
					}
					setState(22);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(23);
				match(RIGHT_BRACE);
				setState(24);
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

		public TerminalNode RIGHT_BRACE() {
			return getToken(BallerinaIRParser.RIGHT_BRACE, 0);
		}

		public List<BasicBlockContext> basicBlock() {
			return getRuleContexts(BasicBlockContext.class);
		}

		public BasicBlockContext basicBlock(int i) {
			return getRuleContext(BasicBlockContext.class, i);
		}

		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_function;
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(26);
				match(FUNCTION);
				setState(27);
				match(Identifier);
				setState(28);
				match(LEFT_PARENTHESIS);
				setState(29);
				match(RIGHT_PARENTHESIS);
				setState(30);
				match(LEFT_BRACE);
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
						{
							setState(31);
							basicBlock();
						}
					}
					setState(34);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while (_la == BB);
				setState(36);
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

		public List<OpContext> op() {
			return getRuleContexts(OpContext.class);
		}

		public OpContext op(int i) {
			return getRuleContext(OpContext.class, i);
		}

		public BasicBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_basicBlock;
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitBasicBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BasicBlockContext basicBlock() throws RecognitionException {
		BasicBlockContext _localctx = new BasicBlockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_basicBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(38);
				match(BB);
				setState(39);
				match(LEFT_BRACE);
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la == GOTO || _la == RETURN) {
					{
						{
							setState(40);
							op();
						}
					}
					setState(45);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(46);
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

	public static class OpContext extends ParserRuleContext {
		public TerminalNode SEMICOLON() {
			return getToken(BallerinaIRParser.SEMICOLON, 0);
		}

		public OpGoToContext opGoTo() {
			return getRuleContext(OpGoToContext.class, 0);
		}

		public OpReturnContext opReturn() {
			return getRuleContext(OpReturnContext.class, 0);
		}

		public OpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_op;
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpContext op() throws RecognitionException {
		OpContext _localctx = new OpContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_op);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(50);
				switch (_input.LA(1)) {
					case GOTO: {
						setState(48);
						opGoTo();
					}
					break;
					case RETURN: {
						setState(49);
						opReturn();
					}
					break;
					default:
						throw new NoViableAltException(this);
				}
				setState(52);
				match(SEMICOLON);
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

	public static class OpGoToContext extends ParserRuleContext {
		public TerminalNode GOTO() {
			return getToken(BallerinaIRParser.GOTO, 0);
		}

		public TerminalNode BB() {
			return getToken(BallerinaIRParser.BB, 0);
		}

		public OpGoToContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_opGoTo;
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitOpGoTo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpGoToContext opGoTo() throws RecognitionException {
		OpGoToContext _localctx = new OpGoToContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_opGoTo);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(54);
				match(GOTO);
				setState(55);
				match(BB);
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

	public static class OpReturnContext extends ParserRuleContext {
		public TerminalNode RETURN() {
			return getToken(BallerinaIRParser.RETURN, 0);
		}

		public OpReturnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		@Override
		public int getRuleIndex() {
			return RULE_opReturn;
		}

		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if (visitor instanceof BallerinaIRParserVisitor)
				return ((BallerinaIRParserVisitor<? extends T>) visitor).visitOpReturn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpReturnContext opReturn() throws RecognitionException {
		OpReturnContext _localctx = new OpReturnContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_opReturn);
		try {
			enterOuterAlt(_localctx, 1);
			{
				setState(57);
				match(RETURN);
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
			"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\24>\4\2\t\2\4\3\t" +
			"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\5\2\21\n\2\3\2\3\2\7\2\25\n" +
			"\2\f\2\16\2\30\13\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\6\3#\n\3\r\3\16" +
			"\3$\3\3\3\3\3\4\3\4\3\4\7\4,\n\4\f\4\16\4/\13\4\3\4\3\4\3\5\3\5\5\5\65" +
			"\n\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2\2<\2\16\3\2\2" +
			"\2\4\34\3\2\2\2\6(\3\2\2\2\b\64\3\2\2\2\n8\3\2\2\2\f;\3\2\2\2\16\20\7" +
			"\3\2\2\17\21\7\21\2\2\20\17\3\2\2\2\20\21\3\2\2\2\21\22\3\2\2\2\22\26" +
			"\7\13\2\2\23\25\5\4\3\2\24\23\3\2\2\2\25\30\3\2\2\2\26\24\3\2\2\2\26\27" +
			"\3\2\2\2\27\31\3\2\2\2\30\26\3\2\2\2\31\32\7\f\2\2\32\33\7\2\2\3\33\3" +
			"\3\2\2\2\34\35\7\5\2\2\35\36\7\21\2\2\36\37\7\16\2\2\37 \7\17\2\2 \"\7" +
			"\13\2\2!#\5\6\4\2\"!\3\2\2\2#$\3\2\2\2$\"\3\2\2\2$%\3\2\2\2%&\3\2\2\2" +
			"&\'\7\f\2\2\'\5\3\2\2\2()\7\b\2\2)-\7\13\2\2*,\5\b\5\2+*\3\2\2\2,/\3\2" +
			"\2\2-+\3\2\2\2-.\3\2\2\2.\60\3\2\2\2/-\3\2\2\2\60\61\7\f\2\2\61\7\3\2" +
			"\2\2\62\65\5\n\6\2\63\65\5\f\7\2\64\62\3\2\2\2\64\63\3\2\2\2\65\66\3\2" +
			"\2\2\66\67\7\20\2\2\67\t\3\2\2\289\7\t\2\29:\7\b\2\2:\13\3\2\2\2;<\7\n" +
			"\2\2<\r\3\2\2\2\7\20\26$-\64";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());

	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}