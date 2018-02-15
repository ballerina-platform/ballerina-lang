// Generated from Toml.g4 by ANTLR 4.5.3
package org.ballerinalang.launcher.toml.antlr4;

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
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TomlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, ALPHA=23, SPACE=24, 
		HYPHEN=25, PERIOD=26, QUOTATION_MARK=27, UNDERSCORE=28, COLON=29, COMMA=30, 
		SLASH=31, APOSTROPHE=32, EQUALS=33, HASH=34, COMMENT=35, BASICUNESCPAED=36, 
		MLBASICUNESCAPED=37, LITERALCHAR=38, MLLITERALCHAR=39, PLUS=40, DIGIT19=41, 
		DIGIT07=42, DIGIT01=43, E=44, INF=45, NAN=46, TRUE=47, FALSE=48, UPPERCASE_T=49, 
		LOWERCASE_T=50, UPPERCASE_Z=51, DIGIT=52, HEXDIG=53;
	public static final int
		RULE_toml = 0, RULE_alpha = 1, RULE_expression = 2, RULE_ws = 3, RULE_wschar = 4, 
		RULE_newline = 5, RULE_keyval = 6, RULE_key = 7, RULE_unquotedKey = 8, 
		RULE_quotedKey = 9, RULE_keyvalSep = 10, RULE_val = 11, RULE_string = 12, 
		RULE_basicString = 13, RULE_basicChar = 14, RULE_escaped = 15, RULE_escapeSeqChar = 16, 
		RULE_mlBasicString = 17, RULE_mlBasicStringDelim = 18, RULE_mlBasicBody = 19, 
		RULE_mlBasicChar = 20, RULE_literalString = 21, RULE_mlLiteralString = 22, 
		RULE_mlLiteralStringDelim = 23, RULE_mlLiteralBody = 24, RULE_integer = 25, 
		RULE_minus = 26, RULE_hexPrefix = 27, RULE_octPrefix = 28, RULE_binPrefix = 29, 
		RULE_decInt = 30, RULE_unsignedDecInt = 31, RULE_hexInt = 32, RULE_octInt = 33, 
		RULE_binInt = 34, RULE_floatingPoint = 35, RULE_floatIntPart = 36, RULE_frac = 37, 
		RULE_decimalPoint = 38, RULE_zeroPrefixableInt = 39, RULE_exp = 40, RULE_specialFloat = 41, 
		RULE_bool = 42, RULE_dateTime = 43, RULE_dateFullyear = 44, RULE_dateMonth = 45, 
		RULE_dateMday = 46, RULE_timeDelim = 47, RULE_timeHour = 48, RULE_timeMinute = 49, 
		RULE_timeSecond = 50, RULE_timeSecfrac = 51, RULE_timeNumoffset = 52, 
		RULE_timeOffset = 53, RULE_partialTime = 54, RULE_fullDate = 55, RULE_fullTime = 56, 
		RULE_offsetDateTime = 57, RULE_localDateTime = 58, RULE_localDate = 59, 
		RULE_localTime = 60, RULE_array = 61, RULE_arrayOpen = 62, RULE_arrayClose = 63, 
		RULE_arrayValues = 64, RULE_arrayvalsNonEmpty = 65, RULE_arraySep = 66, 
		RULE_table = 67, RULE_stdTable = 68, RULE_stdTableOpen = 69, RULE_stdTableClose = 70, 
		RULE_tableKeySep = 71, RULE_inlineTable = 72, RULE_inlineTableOpen = 73, 
		RULE_inlineTableClose = 74, RULE_inlineTableSep = 75, RULE_inlineTableKeyvals = 76, 
		RULE_inlineTableKeyvalsNonEmpty = 77, RULE_arrayTable = 78, RULE_arrayTableOpen = 79, 
		RULE_arrayTableClose = 80;
	public static final String[] ruleNames = {
		"toml", "alpha", "expression", "ws", "wschar", "newline", "keyval", "key", 
		"unquotedKey", "quotedKey", "keyvalSep", "val", "string", "basicString", 
		"basicChar", "escaped", "escapeSeqChar", "mlBasicString", "mlBasicStringDelim", 
		"mlBasicBody", "mlBasicChar", "literalString", "mlLiteralString", "mlLiteralStringDelim", 
		"mlLiteralBody", "integer", "minus", "hexPrefix", "octPrefix", "binPrefix", 
		"decInt", "unsignedDecInt", "hexInt", "octInt", "binInt", "floatingPoint", 
		"floatIntPart", "frac", "decimalPoint", "zeroPrefixableInt", "exp", "specialFloat", 
		"bool", "dateTime", "dateFullyear", "dateMonth", "dateMday", "timeDelim", 
		"timeHour", "timeMinute", "timeSecond", "timeSecfrac", "timeNumoffset", 
		"timeOffset", "partialTime", "fullDate", "fullTime", "offsetDateTime", 
		"localDateTime", "localDate", "localTime", "array", "arrayOpen", "arrayClose", 
		"arrayValues", "arrayvalsNonEmpty", "arraySep", "table", "stdTable", "stdTableOpen", 
		"stdTableClose", "tableKeySep", "inlineTable", "inlineTableOpen", "inlineTableClose", 
		"inlineTableSep", "inlineTableKeyvals", "inlineTableKeyvalsNonEmpty", 
		"arrayTable", "arrayTableOpen", "arrayTableClose"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\t'", "'\r'", "'\n'", "'\r\n'", "'\\\"'", "'\\\\'", "'\\/'", "'\\b'", 
		"'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\'", "'0x'", "'0o'", "'0b'", "'['", 
		"']'", "'{'", "'}'", "'[['", "']]'", null, "' '", "'-'", "'.'", "'\"'", 
		"'_'", "':'", "','", "'/'", "'''", "'='", "'#'", null, null, null, null, 
		null, "'+'", null, null, null, "'e'", "'inf'", "'nan'", "'true'", "'false'", 
		"'T'", "'t'", "'Z'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, "ALPHA", 
		"SPACE", "HYPHEN", "PERIOD", "QUOTATION_MARK", "UNDERSCORE", "COLON", 
		"COMMA", "SLASH", "APOSTROPHE", "EQUALS", "HASH", "COMMENT", "BASICUNESCPAED", 
		"MLBASICUNESCAPED", "LITERALCHAR", "MLLITERALCHAR", "PLUS", "DIGIT19", 
		"DIGIT07", "DIGIT01", "E", "INF", "NAN", "TRUE", "FALSE", "UPPERCASE_T", 
		"LOWERCASE_T", "UPPERCASE_Z", "DIGIT", "HEXDIG"
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
	public String getGrammarFileName() { return "Toml.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TomlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class TomlContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<NewlineContext> newline() {
			return getRuleContexts(NewlineContext.class);
		}
		public NewlineContext newline(int i) {
			return getRuleContext(NewlineContext.class,i);
		}
		public TomlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_toml; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterToml(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitToml(this);
		}
	}

	public final TomlContext toml() throws RecognitionException {
		TomlContext _localctx = new TomlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_toml);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			expression();
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2 || _la==T__3) {
				{
				{
				setState(163);
				newline();
				setState(164);
				expression();
				}
				}
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphaContext extends ParserRuleContext {
		public TerminalNode ALPHA() { return getToken(TomlParser.ALPHA, 0); }
		public TerminalNode TRUE() { return getToken(TomlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(TomlParser.FALSE, 0); }
		public TerminalNode NAN() { return getToken(TomlParser.NAN, 0); }
		public TerminalNode INF() { return getToken(TomlParser.INF, 0); }
		public TerminalNode E() { return getToken(TomlParser.E, 0); }
		public TerminalNode UPPERCASE_T() { return getToken(TomlParser.UPPERCASE_T, 0); }
		public TerminalNode LOWERCASE_T() { return getToken(TomlParser.LOWERCASE_T, 0); }
		public TerminalNode UPPERCASE_Z() { return getToken(TomlParser.UPPERCASE_Z, 0); }
		public AlphaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alpha; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterAlpha(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitAlpha(this);
		}
	}

	public final AlphaContext alpha() throws RecognitionException {
		AlphaContext _localctx = new AlphaContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_alpha);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALPHA) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public KeyvalContext keyval() {
			return getRuleContext(KeyvalContext.class,0);
		}
		public TableContext table() {
			return getRuleContext(TableContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitExpression(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expression);
		try {
			setState(182);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				ws();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(174);
				ws();
				setState(175);
				keyval();
				setState(176);
				ws();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(178);
				ws();
				setState(179);
				table();
				setState(180);
				ws();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WsContext extends ParserRuleContext {
		public List<WscharContext> wschar() {
			return getRuleContexts(WscharContext.class);
		}
		public WscharContext wschar(int i) {
			return getRuleContext(WscharContext.class,i);
		}
		public WsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ws; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterWs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitWs(this);
		}
	}

	public final WsContext ws() throws RecognitionException {
		WsContext _localctx = new WsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_ws);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(184);
					wschar();
					}
					}
				}
				setState(189);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WscharContext extends ParserRuleContext {
		public TerminalNode SPACE() { return getToken(TomlParser.SPACE, 0); }
		public WscharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wschar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterWschar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitWschar(this);
		}
	}

	public final WscharContext wschar() throws RecognitionException {
		WscharContext _localctx = new WscharContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_wschar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << SPACE))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NewlineContext extends ParserRuleContext {
		public NewlineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_newline; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterNewline(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitNewline(this);
		}
	}

	public final NewlineContext newline() throws RecognitionException {
		NewlineContext _localctx = new NewlineContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_newline);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			_la = _input.LA(1);
			if ( !(_la==T__2 || _la==T__3) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyvalContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public KeyvalSepContext keyvalSep() {
			return getRuleContext(KeyvalSepContext.class,0);
		}
		public ValContext val() {
			return getRuleContext(ValContext.class,0);
		}
		public KeyvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterKeyval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitKeyval(this);
		}
	}

	public final KeyvalContext keyval() throws RecognitionException {
		KeyvalContext _localctx = new KeyvalContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_keyval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			key();
			setState(195);
			keyvalSep();
			setState(196);
			val();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyContext extends ParserRuleContext {
		public UnquotedKeyContext unquotedKey() {
			return getRuleContext(UnquotedKeyContext.class,0);
		}
		public QuotedKeyContext quotedKey() {
			return getRuleContext(QuotedKeyContext.class,0);
		}
		public KeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitKey(this);
		}
	}

	public final KeyContext key() throws RecognitionException {
		KeyContext _localctx = new KeyContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_key);
		try {
			setState(200);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case T__17:
			case T__21:
			case ALPHA:
			case SPACE:
			case HYPHEN:
			case PERIOD:
			case UNDERSCORE:
			case EQUALS:
			case E:
			case INF:
			case NAN:
			case TRUE:
			case FALSE:
			case UPPERCASE_T:
			case LOWERCASE_T:
			case UPPERCASE_Z:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(198);
				unquotedKey();
				}
				break;
			case QUOTATION_MARK:
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(199);
				quotedKey();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnquotedKeyContext extends ParserRuleContext {
		public List<AlphaContext> alpha() {
			return getRuleContexts(AlphaContext.class);
		}
		public AlphaContext alpha(int i) {
			return getRuleContext(AlphaContext.class,i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(TomlParser.HYPHEN); }
		public TerminalNode HYPHEN(int i) {
			return getToken(TomlParser.HYPHEN, i);
		}
		public List<TerminalNode> UNDERSCORE() { return getTokens(TomlParser.UNDERSCORE); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(TomlParser.UNDERSCORE, i);
		}
		public UnquotedKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unquotedKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterUnquotedKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitUnquotedKey(this);
		}
	}

	public final UnquotedKeyContext unquotedKey() throws RecognitionException {
		UnquotedKeyContext _localctx = new UnquotedKeyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_unquotedKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALPHA) | (1L << HYPHEN) | (1L << UNDERSCORE) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z) | (1L << DIGIT))) != 0)) {
				{
				setState(206);
				switch (_input.LA(1)) {
				case ALPHA:
				case E:
				case INF:
				case NAN:
				case TRUE:
				case FALSE:
				case UPPERCASE_T:
				case LOWERCASE_T:
				case UPPERCASE_Z:
					{
					setState(202);
					alpha();
					}
					break;
				case DIGIT:
					{
					setState(203);
					match(DIGIT);
					}
					break;
				case HYPHEN:
					{
					setState(204);
					match(HYPHEN);
					}
					break;
				case UNDERSCORE:
					{
					setState(205);
					match(UNDERSCORE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuotedKeyContext extends ParserRuleContext {
		public BasicStringContext basicString() {
			return getRuleContext(BasicStringContext.class,0);
		}
		public LiteralStringContext literalString() {
			return getRuleContext(LiteralStringContext.class,0);
		}
		public QuotedKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterQuotedKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitQuotedKey(this);
		}
	}

	public final QuotedKeyContext quotedKey() throws RecognitionException {
		QuotedKeyContext _localctx = new QuotedKeyContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_quotedKey);
		try {
			setState(213);
			switch (_input.LA(1)) {
			case QUOTATION_MARK:
				enterOuterAlt(_localctx, 1);
				{
				setState(211);
				basicString();
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(212);
				literalString();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyvalSepContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode EQUALS() { return getToken(TomlParser.EQUALS, 0); }
		public KeyvalSepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyvalSep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterKeyvalSep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitKeyvalSep(this);
		}
	}

	public final KeyvalSepContext keyvalSep() throws RecognitionException {
		KeyvalSepContext _localctx = new KeyvalSepContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_keyvalSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(215);
			ws();
			setState(216);
			match(EQUALS);
			setState(217);
			ws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public BoolContext bool() {
			return getRuleContext(BoolContext.class,0);
		}
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public InlineTableContext inlineTable() {
			return getRuleContext(InlineTableContext.class,0);
		}
		public DateTimeContext dateTime() {
			return getRuleContext(DateTimeContext.class,0);
		}
		public FloatingPointContext floatingPoint() {
			return getRuleContext(FloatingPointContext.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public ValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_val; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitVal(this);
		}
	}

	public final ValContext val() throws RecognitionException {
		ValContext _localctx = new ValContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_val);
		try {
			setState(226);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(219);
				string();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(220);
				bool();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(221);
				array();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(222);
				inlineTable();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(223);
				dateTime();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(224);
				floatingPoint();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(225);
				integer();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringContext extends ParserRuleContext {
		public MlBasicStringContext mlBasicString() {
			return getRuleContext(MlBasicStringContext.class,0);
		}
		public BasicStringContext basicString() {
			return getRuleContext(BasicStringContext.class,0);
		}
		public MlLiteralStringContext mlLiteralString() {
			return getRuleContext(MlLiteralStringContext.class,0);
		}
		public LiteralStringContext literalString() {
			return getRuleContext(LiteralStringContext.class,0);
		}
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_string);
		try {
			setState(232);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(228);
				mlBasicString();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(229);
				basicString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(230);
				mlLiteralString();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(231);
				literalString();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BasicStringContext extends ParserRuleContext {
		public List<TerminalNode> QUOTATION_MARK() { return getTokens(TomlParser.QUOTATION_MARK); }
		public TerminalNode QUOTATION_MARK(int i) {
			return getToken(TomlParser.QUOTATION_MARK, i);
		}
		public List<BasicCharContext> basicChar() {
			return getRuleContexts(BasicCharContext.class);
		}
		public BasicCharContext basicChar(int i) {
			return getRuleContext(BasicCharContext.class,i);
		}
		public BasicStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basicString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBasicString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBasicString(this);
		}
	}

	public final BasicStringContext basicString() throws RecognitionException {
		BasicStringContext _localctx = new BasicStringContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_basicString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(QUOTATION_MARK);
			setState(238);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << ALPHA) | (1L << SPACE) | (1L << HYPHEN) | (1L << PERIOD) | (1L << UNDERSCORE) | (1L << COLON) | (1L << COMMA) | (1L << SLASH) | (1L << APOSTROPHE) | (1L << EQUALS) | (1L << HASH) | (1L << BASICUNESCPAED) | (1L << PLUS) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) {
				{
				{
				setState(235);
				basicChar();
				}
				}
				setState(240);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(241);
			match(QUOTATION_MARK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BasicCharContext extends ParserRuleContext {
		public EscapedContext escaped() {
			return getRuleContext(EscapedContext.class,0);
		}
		public AlphaContext alpha() {
			return getRuleContext(AlphaContext.class,0);
		}
		public TerminalNode BASICUNESCPAED() { return getToken(TomlParser.BASICUNESCPAED, 0); }
		public TerminalNode SPACE() { return getToken(TomlParser.SPACE, 0); }
		public TerminalNode PLUS() { return getToken(TomlParser.PLUS, 0); }
		public TerminalNode HYPHEN() { return getToken(TomlParser.HYPHEN, 0); }
		public TerminalNode PERIOD() { return getToken(TomlParser.PERIOD, 0); }
		public TerminalNode UNDERSCORE() { return getToken(TomlParser.UNDERSCORE, 0); }
		public TerminalNode COLON() { return getToken(TomlParser.COLON, 0); }
		public TerminalNode COMMA() { return getToken(TomlParser.COMMA, 0); }
		public TerminalNode SLASH() { return getToken(TomlParser.SLASH, 0); }
		public TerminalNode APOSTROPHE() { return getToken(TomlParser.APOSTROPHE, 0); }
		public TerminalNode EQUALS() { return getToken(TomlParser.EQUALS, 0); }
		public TerminalNode HASH() { return getToken(TomlParser.HASH, 0); }
		public BasicCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basicChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBasicChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBasicChar(this);
		}
	}

	public final BasicCharContext basicChar() throws RecognitionException {
		BasicCharContext _localctx = new BasicCharContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_basicChar);
		try {
			setState(257);
			switch (_input.LA(1)) {
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
				enterOuterAlt(_localctx, 1);
				{
				setState(243);
				escaped();
				}
				break;
			case ALPHA:
			case E:
			case INF:
			case NAN:
			case TRUE:
			case FALSE:
			case UPPERCASE_T:
			case LOWERCASE_T:
			case UPPERCASE_Z:
				enterOuterAlt(_localctx, 2);
				{
				setState(244);
				alpha();
				}
				break;
			case BASICUNESCPAED:
				enterOuterAlt(_localctx, 3);
				{
				setState(245);
				match(BASICUNESCPAED);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 4);
				{
				setState(246);
				match(SPACE);
				}
				break;
			case PLUS:
				enterOuterAlt(_localctx, 5);
				{
				setState(247);
				match(PLUS);
				}
				break;
			case HYPHEN:
				enterOuterAlt(_localctx, 6);
				{
				setState(248);
				match(HYPHEN);
				}
				break;
			case PERIOD:
				enterOuterAlt(_localctx, 7);
				{
				setState(249);
				match(PERIOD);
				}
				break;
			case UNDERSCORE:
				enterOuterAlt(_localctx, 8);
				{
				setState(250);
				match(UNDERSCORE);
				}
				break;
			case COLON:
				enterOuterAlt(_localctx, 9);
				{
				setState(251);
				match(COLON);
				}
				break;
			case COMMA:
				enterOuterAlt(_localctx, 10);
				{
				setState(252);
				match(COMMA);
				}
				break;
			case SLASH:
				enterOuterAlt(_localctx, 11);
				{
				setState(253);
				match(SLASH);
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 12);
				{
				setState(254);
				match(APOSTROPHE);
				}
				break;
			case EQUALS:
				enterOuterAlt(_localctx, 13);
				{
				setState(255);
				match(EQUALS);
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 14);
				{
				setState(256);
				match(HASH);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EscapedContext extends ParserRuleContext {
		public EscapeSeqCharContext escapeSeqChar() {
			return getRuleContext(EscapeSeqCharContext.class,0);
		}
		public EscapedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escaped; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterEscaped(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitEscaped(this);
		}
	}

	public final EscapedContext escaped() throws RecognitionException {
		EscapedContext _localctx = new EscapedContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_escaped);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			escapeSeqChar();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EscapeSeqCharContext extends ParserRuleContext {
		public EscapeSeqCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escapeSeqChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterEscapeSeqChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitEscapeSeqChar(this);
		}
	}

	public final EscapeSeqCharContext escapeSeqChar() throws RecognitionException {
		EscapeSeqCharContext _localctx = new EscapeSeqCharContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_escapeSeqChar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlBasicStringContext extends ParserRuleContext {
		public List<MlBasicStringDelimContext> mlBasicStringDelim() {
			return getRuleContexts(MlBasicStringDelimContext.class);
		}
		public MlBasicStringDelimContext mlBasicStringDelim(int i) {
			return getRuleContext(MlBasicStringDelimContext.class,i);
		}
		public MlBasicBodyContext mlBasicBody() {
			return getRuleContext(MlBasicBodyContext.class,0);
		}
		public MlBasicStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlBasicString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlBasicString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlBasicString(this);
		}
	}

	public final MlBasicStringContext mlBasicString() throws RecognitionException {
		MlBasicStringContext _localctx = new MlBasicStringContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_mlBasicString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			mlBasicStringDelim();
			setState(264);
			mlBasicBody();
			setState(265);
			mlBasicStringDelim();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlBasicStringDelimContext extends ParserRuleContext {
		public List<TerminalNode> QUOTATION_MARK() { return getTokens(TomlParser.QUOTATION_MARK); }
		public TerminalNode QUOTATION_MARK(int i) {
			return getToken(TomlParser.QUOTATION_MARK, i);
		}
		public MlBasicStringDelimContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlBasicStringDelim; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlBasicStringDelim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlBasicStringDelim(this);
		}
	}

	public final MlBasicStringDelimContext mlBasicStringDelim() throws RecognitionException {
		MlBasicStringDelimContext _localctx = new MlBasicStringDelimContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_mlBasicStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			match(QUOTATION_MARK);
			setState(268);
			match(QUOTATION_MARK);
			setState(269);
			match(QUOTATION_MARK);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlBasicBodyContext extends ParserRuleContext {
		public List<MlBasicCharContext> mlBasicChar() {
			return getRuleContexts(MlBasicCharContext.class);
		}
		public MlBasicCharContext mlBasicChar(int i) {
			return getRuleContext(MlBasicCharContext.class,i);
		}
		public List<NewlineContext> newline() {
			return getRuleContexts(NewlineContext.class);
		}
		public NewlineContext newline(int i) {
			return getRuleContext(NewlineContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public MlBasicBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlBasicBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlBasicBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlBasicBody(this);
		}
	}

	public final MlBasicBodyContext mlBasicBody() throws RecognitionException {
		MlBasicBodyContext _localctx = new MlBasicBodyContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_mlBasicBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << MLBASICUNESCAPED))) != 0)) {
				{
				setState(277);
				switch (_input.LA(1)) {
				case T__4:
				case T__5:
				case T__6:
				case T__7:
				case T__8:
				case T__9:
				case T__10:
				case T__11:
				case MLBASICUNESCAPED:
					{
					setState(271);
					mlBasicChar();
					}
					break;
				case T__2:
				case T__3:
					{
					setState(272);
					newline();
					}
					break;
				case T__12:
					{
					{
					setState(273);
					match(T__12);
					setState(274);
					ws();
					setState(275);
					newline();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlBasicCharContext extends ParserRuleContext {
		public TerminalNode MLBASICUNESCAPED() { return getToken(TomlParser.MLBASICUNESCAPED, 0); }
		public EscapedContext escaped() {
			return getRuleContext(EscapedContext.class,0);
		}
		public MlBasicCharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlBasicChar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlBasicChar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlBasicChar(this);
		}
	}

	public final MlBasicCharContext mlBasicChar() throws RecognitionException {
		MlBasicCharContext _localctx = new MlBasicCharContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_mlBasicChar);
		try {
			setState(284);
			switch (_input.LA(1)) {
			case MLBASICUNESCAPED:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				match(MLBASICUNESCAPED);
				}
				break;
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
				enterOuterAlt(_localctx, 2);
				{
				setState(283);
				escaped();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LiteralStringContext extends ParserRuleContext {
		public List<TerminalNode> APOSTROPHE() { return getTokens(TomlParser.APOSTROPHE); }
		public TerminalNode APOSTROPHE(int i) {
			return getToken(TomlParser.APOSTROPHE, i);
		}
		public List<TerminalNode> LITERALCHAR() { return getTokens(TomlParser.LITERALCHAR); }
		public TerminalNode LITERALCHAR(int i) {
			return getToken(TomlParser.LITERALCHAR, i);
		}
		public LiteralStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literalString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterLiteralString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitLiteralString(this);
		}
	}

	public final LiteralStringContext literalString() throws RecognitionException {
		LiteralStringContext _localctx = new LiteralStringContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_literalString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(APOSTROPHE);
			setState(290);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LITERALCHAR) {
				{
				{
				setState(287);
				match(LITERALCHAR);
				}
				}
				setState(292);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(293);
			match(APOSTROPHE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlLiteralStringContext extends ParserRuleContext {
		public List<MlLiteralStringDelimContext> mlLiteralStringDelim() {
			return getRuleContexts(MlLiteralStringDelimContext.class);
		}
		public MlLiteralStringDelimContext mlLiteralStringDelim(int i) {
			return getRuleContext(MlLiteralStringDelimContext.class,i);
		}
		public MlLiteralBodyContext mlLiteralBody() {
			return getRuleContext(MlLiteralBodyContext.class,0);
		}
		public MlLiteralStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlLiteralString; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlLiteralString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlLiteralString(this);
		}
	}

	public final MlLiteralStringContext mlLiteralString() throws RecognitionException {
		MlLiteralStringContext _localctx = new MlLiteralStringContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_mlLiteralString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(295);
			mlLiteralStringDelim();
			setState(296);
			mlLiteralBody();
			setState(297);
			mlLiteralStringDelim();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlLiteralStringDelimContext extends ParserRuleContext {
		public List<TerminalNode> APOSTROPHE() { return getTokens(TomlParser.APOSTROPHE); }
		public TerminalNode APOSTROPHE(int i) {
			return getToken(TomlParser.APOSTROPHE, i);
		}
		public MlLiteralStringDelimContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlLiteralStringDelim; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlLiteralStringDelim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlLiteralStringDelim(this);
		}
	}

	public final MlLiteralStringDelimContext mlLiteralStringDelim() throws RecognitionException {
		MlLiteralStringDelimContext _localctx = new MlLiteralStringDelimContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_mlLiteralStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(APOSTROPHE);
			setState(300);
			match(APOSTROPHE);
			setState(301);
			match(APOSTROPHE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MlLiteralBodyContext extends ParserRuleContext {
		public List<TerminalNode> MLLITERALCHAR() { return getTokens(TomlParser.MLLITERALCHAR); }
		public TerminalNode MLLITERALCHAR(int i) {
			return getToken(TomlParser.MLLITERALCHAR, i);
		}
		public List<NewlineContext> newline() {
			return getRuleContexts(NewlineContext.class);
		}
		public NewlineContext newline(int i) {
			return getRuleContext(NewlineContext.class,i);
		}
		public MlLiteralBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mlLiteralBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMlLiteralBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMlLiteralBody(this);
		}
	}

	public final MlLiteralBodyContext mlLiteralBody() throws RecognitionException {
		MlLiteralBodyContext _localctx = new MlLiteralBodyContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_mlLiteralBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << MLLITERALCHAR))) != 0)) {
				{
				setState(305);
				switch (_input.LA(1)) {
				case MLLITERALCHAR:
					{
					setState(303);
					match(MLLITERALCHAR);
					}
					break;
				case T__2:
				case T__3:
					{
					setState(304);
					newline();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(309);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerContext extends ParserRuleContext {
		public DecIntContext decInt() {
			return getRuleContext(DecIntContext.class,0);
		}
		public HexIntContext hexInt() {
			return getRuleContext(HexIntContext.class,0);
		}
		public OctIntContext octInt() {
			return getRuleContext(OctIntContext.class,0);
		}
		public BinIntContext binInt() {
			return getRuleContext(BinIntContext.class,0);
		}
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInteger(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_integer);
		try {
			setState(314);
			switch (_input.LA(1)) {
			case HYPHEN:
			case PLUS:
			case DIGIT19:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(310);
				decInt();
				}
				break;
			case T__13:
				enterOuterAlt(_localctx, 2);
				{
				setState(311);
				hexInt();
				}
				break;
			case T__14:
				enterOuterAlt(_localctx, 3);
				{
				setState(312);
				octInt();
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 4);
				{
				setState(313);
				binInt();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MinusContext extends ParserRuleContext {
		public MinusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minus; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterMinus(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitMinus(this);
		}
	}

	public final MinusContext minus() throws RecognitionException {
		MinusContext _localctx = new MinusContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_minus);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			match(HYPHEN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HexPrefixContext extends ParserRuleContext {
		public HexPrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexPrefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterHexPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitHexPrefix(this);
		}
	}

	public final HexPrefixContext hexPrefix() throws RecognitionException {
		HexPrefixContext _localctx = new HexPrefixContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_hexPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			match(T__13);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OctPrefixContext extends ParserRuleContext {
		public OctPrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_octPrefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterOctPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitOctPrefix(this);
		}
	}

	public final OctPrefixContext octPrefix() throws RecognitionException {
		OctPrefixContext _localctx = new OctPrefixContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_octPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			match(T__14);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BinPrefixContext extends ParserRuleContext {
		public BinPrefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binPrefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBinPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBinPrefix(this);
		}
	}

	public final BinPrefixContext binPrefix() throws RecognitionException {
		BinPrefixContext _localctx = new BinPrefixContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_binPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
			match(T__15);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecIntContext extends ParserRuleContext {
		public UnsignedDecIntContext unsignedDecInt() {
			return getRuleContext(UnsignedDecIntContext.class,0);
		}
		public MinusContext minus() {
			return getRuleContext(MinusContext.class,0);
		}
		public TerminalNode PLUS() { return getToken(TomlParser.PLUS, 0); }
		public DecIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDecInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDecInt(this);
		}
	}

	public final DecIntContext decInt() throws RecognitionException {
		DecIntContext _localctx = new DecIntContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_decInt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			switch (_input.LA(1)) {
			case HYPHEN:
				{
				setState(324);
				minus();
				}
				break;
			case PLUS:
				{
				setState(325);
				match(PLUS);
				}
				break;
			case DIGIT19:
			case DIGIT:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(328);
			unsignedDecInt();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnsignedDecIntContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public TerminalNode DIGIT19() { return getToken(TomlParser.DIGIT19, 0); }
		public List<TerminalNode> UNDERSCORE() { return getTokens(TomlParser.UNDERSCORE); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(TomlParser.UNDERSCORE, i);
		}
		public UnsignedDecIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedDecInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterUnsignedDecInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitUnsignedDecInt(this);
		}
	}

	public final UnsignedDecIntContext unsignedDecInt() throws RecognitionException {
		UnsignedDecIntContext _localctx = new UnsignedDecIntContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_unsignedDecInt);
		int _la;
		try {
			setState(340);
			switch (_input.LA(1)) {
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(330);
				match(DIGIT);
				}
				break;
			case DIGIT19:
				enterOuterAlt(_localctx, 2);
				{
				setState(331);
				match(DIGIT19);
				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==UNDERSCORE || _la==DIGIT) {
					{
					setState(335);
					switch (_input.LA(1)) {
					case DIGIT:
						{
						setState(332);
						match(DIGIT);
						}
						break;
					case UNDERSCORE:
						{
						setState(333);
						match(UNDERSCORE);
						setState(334);
						match(DIGIT);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(339);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HexIntContext extends ParserRuleContext {
		public HexPrefixContext hexPrefix() {
			return getRuleContext(HexPrefixContext.class,0);
		}
		public List<TerminalNode> HEXDIG() { return getTokens(TomlParser.HEXDIG); }
		public TerminalNode HEXDIG(int i) {
			return getToken(TomlParser.HEXDIG, i);
		}
		public TerminalNode UNDERSCORE() { return getToken(TomlParser.UNDERSCORE, 0); }
		public HexIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hexInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterHexInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitHexInt(this);
		}
	}

	public final HexIntContext hexInt() throws RecognitionException {
		HexIntContext _localctx = new HexIntContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_hexInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			hexPrefix();
			setState(346);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(343);
					match(HEXDIG);
					}
					}
				}
				setState(348);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			}
			setState(352);
			switch (_input.LA(1)) {
			case HEXDIG:
				{
				setState(349);
				match(HEXDIG);
				}
				break;
			case UNDERSCORE:
				{
				setState(350);
				match(UNDERSCORE);
				setState(351);
				match(HEXDIG);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OctIntContext extends ParserRuleContext {
		public OctPrefixContext octPrefix() {
			return getRuleContext(OctPrefixContext.class,0);
		}
		public List<TerminalNode> DIGIT07() { return getTokens(TomlParser.DIGIT07); }
		public TerminalNode DIGIT07(int i) {
			return getToken(TomlParser.DIGIT07, i);
		}
		public TerminalNode UNDERSCORE() { return getToken(TomlParser.UNDERSCORE, 0); }
		public OctIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_octInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterOctInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitOctInt(this);
		}
	}

	public final OctIntContext octInt() throws RecognitionException {
		OctIntContext _localctx = new OctIntContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_octInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			octPrefix();
			setState(358);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(355);
					match(DIGIT07);
					}
					}
				}
				setState(360);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			setState(364);
			switch (_input.LA(1)) {
			case DIGIT07:
				{
				setState(361);
				match(DIGIT07);
				}
				break;
			case UNDERSCORE:
				{
				setState(362);
				match(UNDERSCORE);
				setState(363);
				match(DIGIT07);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BinIntContext extends ParserRuleContext {
		public BinPrefixContext binPrefix() {
			return getRuleContext(BinPrefixContext.class,0);
		}
		public List<TerminalNode> DIGIT01() { return getTokens(TomlParser.DIGIT01); }
		public TerminalNode DIGIT01(int i) {
			return getToken(TomlParser.DIGIT01, i);
		}
		public TerminalNode UNDERSCORE() { return getToken(TomlParser.UNDERSCORE, 0); }
		public BinIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBinInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBinInt(this);
		}
	}

	public final BinIntContext binInt() throws RecognitionException {
		BinIntContext _localctx = new BinIntContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_binInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			binPrefix();
			setState(370);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(367);
					match(DIGIT01);
					}
					} 
				}
				setState(372);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			}
			setState(376);
			switch (_input.LA(1)) {
			case DIGIT01:
				{
				setState(373);
				match(DIGIT01);
				}
				break;
			case UNDERSCORE:
				{
				setState(374);
				match(UNDERSCORE);
				setState(375);
				match(DIGIT01);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FloatingPointContext extends ParserRuleContext {
		public FloatIntPartContext floatIntPart() {
			return getRuleContext(FloatIntPartContext.class,0);
		}
		public ExpContext exp() {
			return getRuleContext(ExpContext.class,0);
		}
		public FracContext frac() {
			return getRuleContext(FracContext.class,0);
		}
		public SpecialFloatContext specialFloat() {
			return getRuleContext(SpecialFloatContext.class,0);
		}
		public FloatingPointContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatingPoint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterFloatingPoint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitFloatingPoint(this);
		}
	}

	public final FloatingPointContext floatingPoint() throws RecognitionException {
		FloatingPointContext _localctx = new FloatingPointContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_floatingPoint);
		int _la;
		try {
			setState(387);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(378);
				floatIntPart();
				setState(384);
				switch (_input.LA(1)) {
				case E:
					{
					setState(379);
					exp();
					}
					break;
				case PERIOD:
					{
					setState(380);
					frac();
					setState(382);
					_la = _input.LA(1);
					if (_la==E) {
						{
						setState(381);
						exp();
						}
					}

					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(386);
				specialFloat();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FloatIntPartContext extends ParserRuleContext {
		public DecIntContext decInt() {
			return getRuleContext(DecIntContext.class,0);
		}
		public FloatIntPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatIntPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterFloatIntPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitFloatIntPart(this);
		}
	}

	public final FloatIntPartContext floatIntPart() throws RecognitionException {
		FloatIntPartContext _localctx = new FloatIntPartContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_floatIntPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			decInt();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FracContext extends ParserRuleContext {
		public DecimalPointContext decimalPoint() {
			return getRuleContext(DecimalPointContext.class,0);
		}
		public ZeroPrefixableIntContext zeroPrefixableInt() {
			return getRuleContext(ZeroPrefixableIntContext.class,0);
		}
		public FracContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frac; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterFrac(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitFrac(this);
		}
	}

	public final FracContext frac() throws RecognitionException {
		FracContext _localctx = new FracContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_frac);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			decimalPoint();
			setState(392);
			zeroPrefixableInt();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecimalPointContext extends ParserRuleContext {
		public TerminalNode PERIOD() { return getToken(TomlParser.PERIOD, 0); }
		public DecimalPointContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decimalPoint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDecimalPoint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDecimalPoint(this);
		}
	}

	public final DecimalPointContext decimalPoint() throws RecognitionException {
		DecimalPointContext _localctx = new DecimalPointContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_decimalPoint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
			match(PERIOD);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ZeroPrefixableIntContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public List<TerminalNode> UNDERSCORE() { return getTokens(TomlParser.UNDERSCORE); }
		public TerminalNode UNDERSCORE(int i) {
			return getToken(TomlParser.UNDERSCORE, i);
		}
		public ZeroPrefixableIntContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_zeroPrefixableInt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterZeroPrefixableInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitZeroPrefixableInt(this);
		}
	}

	public final ZeroPrefixableIntContext zeroPrefixableInt() throws RecognitionException {
		ZeroPrefixableIntContext _localctx = new ZeroPrefixableIntContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_zeroPrefixableInt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			match(DIGIT);
			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==UNDERSCORE || _la==DIGIT) {
				{
				setState(400);
				switch (_input.LA(1)) {
				case DIGIT:
					{
					setState(397);
					match(DIGIT);
					}
					break;
				case UNDERSCORE:
					{
					setState(398);
					match(UNDERSCORE);
					setState(399);
					match(DIGIT);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpContext extends ParserRuleContext {
		public TerminalNode E() { return getToken(TomlParser.E, 0); }
		public FloatIntPartContext floatIntPart() {
			return getRuleContext(FloatIntPartContext.class,0);
		}
		public ExpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterExp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitExp(this);
		}
	}

	public final ExpContext exp() throws RecognitionException {
		ExpContext _localctx = new ExpContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_exp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(E);
			setState(406);
			floatIntPart();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SpecialFloatContext extends ParserRuleContext {
		public MinusContext minus() {
			return getRuleContext(MinusContext.class,0);
		}
		public TerminalNode INF() { return getToken(TomlParser.INF, 0); }
		public TerminalNode NAN() { return getToken(TomlParser.NAN, 0); }
		public TerminalNode PLUS() { return getToken(TomlParser.PLUS, 0); }
		public SpecialFloatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialFloat; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterSpecialFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitSpecialFloat(this);
		}
	}

	public final SpecialFloatContext specialFloat() throws RecognitionException {
		SpecialFloatContext _localctx = new SpecialFloatContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_specialFloat);
		int _la;
		try {
			setState(413);
			switch (_input.LA(1)) {
			case HYPHEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(408);
				minus();
				}
				break;
			case PLUS:
			case INF:
			case NAN:
				enterOuterAlt(_localctx, 2);
				{
				setState(410);
				_la = _input.LA(1);
				if (_la==PLUS) {
					{
					setState(409);
					match(PLUS);
					}
				}

				setState(412);
				_la = _input.LA(1);
				if ( !(_la==INF || _la==NAN) ) {
				_errHandler.recoverInline(this);
				} else {
					consume();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoolContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(TomlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(TomlParser.FALSE, 0); }
		public BoolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBool(this);
		}
	}

	public final BoolContext bool() throws RecognitionException {
		BoolContext _localctx = new BoolContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_bool);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(415);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateTimeContext extends ParserRuleContext {
		public OffsetDateTimeContext offsetDateTime() {
			return getRuleContext(OffsetDateTimeContext.class,0);
		}
		public LocalDateTimeContext localDateTime() {
			return getRuleContext(LocalDateTimeContext.class,0);
		}
		public LocalDateContext localDate() {
			return getRuleContext(LocalDateContext.class,0);
		}
		public LocalTimeContext localTime() {
			return getRuleContext(LocalTimeContext.class,0);
		}
		public DateTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDateTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDateTime(this);
		}
	}

	public final DateTimeContext dateTime() throws RecognitionException {
		DateTimeContext _localctx = new DateTimeContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_dateTime);
		try {
			setState(421);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(417);
				offsetDateTime();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(418);
				localDateTime();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(419);
				localDate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(420);
				localTime();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateFullyearContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public DateFullyearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateFullyear; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDateFullyear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDateFullyear(this);
		}
	}

	public final DateFullyearContext dateFullyear() throws RecognitionException {
		DateFullyearContext _localctx = new DateFullyearContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_dateFullyear);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
			match(DIGIT);
			setState(424);
			match(DIGIT);
			setState(425);
			match(DIGIT);
			setState(426);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateMonthContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public DateMonthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateMonth; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDateMonth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDateMonth(this);
		}
	}

	public final DateMonthContext dateMonth() throws RecognitionException {
		DateMonthContext _localctx = new DateMonthContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_dateMonth);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			match(DIGIT);
			setState(429);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateMdayContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public DateMdayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateMday; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDateMday(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDateMday(this);
		}
	}

	public final DateMdayContext dateMday() throws RecognitionException {
		DateMdayContext _localctx = new DateMdayContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_dateMday);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431);
			match(DIGIT);
			setState(432);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeDelimContext extends ParserRuleContext {
		public TerminalNode UPPERCASE_T() { return getToken(TomlParser.UPPERCASE_T, 0); }
		public TerminalNode LOWERCASE_T() { return getToken(TomlParser.LOWERCASE_T, 0); }
		public TimeDelimContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeDelim; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeDelim(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeDelim(this);
		}
	}

	public final TimeDelimContext timeDelim() throws RecognitionException {
		TimeDelimContext _localctx = new TimeDelimContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_timeDelim);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SPACE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeHourContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public TimeHourContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeHour; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeHour(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeHour(this);
		}
	}

	public final TimeHourContext timeHour() throws RecognitionException {
		TimeHourContext _localctx = new TimeHourContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_timeHour);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(436);
			match(DIGIT);
			setState(437);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeMinuteContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public TimeMinuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeMinute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeMinute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeMinute(this);
		}
	}

	public final TimeMinuteContext timeMinute() throws RecognitionException {
		TimeMinuteContext _localctx = new TimeMinuteContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_timeMinute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(439);
			match(DIGIT);
			setState(440);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeSecondContext extends ParserRuleContext {
		public List<TerminalNode> DIGIT() { return getTokens(TomlParser.DIGIT); }
		public TerminalNode DIGIT(int i) {
			return getToken(TomlParser.DIGIT, i);
		}
		public TimeSecondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeSecond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeSecond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeSecond(this);
		}
	}

	public final TimeSecondContext timeSecond() throws RecognitionException {
		TimeSecondContext _localctx = new TimeSecondContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_timeSecond);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(442);
			match(DIGIT);
			setState(443);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeSecfracContext extends ParserRuleContext {
		public TerminalNode PERIOD() { return getToken(TomlParser.PERIOD, 0); }
		public TerminalNode DIGIT() { return getToken(TomlParser.DIGIT, 0); }
		public TimeSecfracContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeSecfrac; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeSecfrac(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeSecfrac(this);
		}
	}

	public final TimeSecfracContext timeSecfrac() throws RecognitionException {
		TimeSecfracContext _localctx = new TimeSecfracContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_timeSecfrac);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
			match(PERIOD);
			setState(446);
			match(DIGIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeNumoffsetContext extends ParserRuleContext {
		public TimeHourContext timeHour() {
			return getRuleContext(TimeHourContext.class,0);
		}
		public TerminalNode COLON() { return getToken(TomlParser.COLON, 0); }
		public TimeMinuteContext timeMinute() {
			return getRuleContext(TimeMinuteContext.class,0);
		}
		public TerminalNode HYPHEN() { return getToken(TomlParser.HYPHEN, 0); }
		public TimeNumoffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeNumoffset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeNumoffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeNumoffset(this);
		}
	}

	public final TimeNumoffsetContext timeNumoffset() throws RecognitionException {
		TimeNumoffsetContext _localctx = new TimeNumoffsetContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_timeNumoffset);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			_la = _input.LA(1);
			if ( !(_la==HYPHEN || _la==PLUS) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(449);
			timeHour();
			setState(450);
			match(COLON);
			setState(451);
			timeMinute();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TimeOffsetContext extends ParserRuleContext {
		public TerminalNode UPPERCASE_Z() { return getToken(TomlParser.UPPERCASE_Z, 0); }
		public TimeNumoffsetContext timeNumoffset() {
			return getRuleContext(TimeNumoffsetContext.class,0);
		}
		public TimeOffsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_timeOffset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTimeOffset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTimeOffset(this);
		}
	}

	public final TimeOffsetContext timeOffset() throws RecognitionException {
		TimeOffsetContext _localctx = new TimeOffsetContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_timeOffset);
		try {
			setState(455);
			switch (_input.LA(1)) {
			case UPPERCASE_Z:
				enterOuterAlt(_localctx, 1);
				{
				setState(453);
				match(UPPERCASE_Z);
				}
				break;
			case HYPHEN:
			case PLUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(454);
				timeNumoffset();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PartialTimeContext extends ParserRuleContext {
		public TimeHourContext timeHour() {
			return getRuleContext(TimeHourContext.class,0);
		}
		public List<TerminalNode> COLON() { return getTokens(TomlParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(TomlParser.COLON, i);
		}
		public TimeMinuteContext timeMinute() {
			return getRuleContext(TimeMinuteContext.class,0);
		}
		public TimeSecondContext timeSecond() {
			return getRuleContext(TimeSecondContext.class,0);
		}
		public TimeSecfracContext timeSecfrac() {
			return getRuleContext(TimeSecfracContext.class,0);
		}
		public PartialTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_partialTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterPartialTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitPartialTime(this);
		}
	}

	public final PartialTimeContext partialTime() throws RecognitionException {
		PartialTimeContext _localctx = new PartialTimeContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_partialTime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(457);
			timeHour();
			setState(458);
			match(COLON);
			setState(459);
			timeMinute();
			setState(460);
			match(COLON);
			setState(461);
			timeSecond();
			setState(463);
			_la = _input.LA(1);
			if (_la==PERIOD) {
				{
				setState(462);
				timeSecfrac();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullDateContext extends ParserRuleContext {
		public DateFullyearContext dateFullyear() {
			return getRuleContext(DateFullyearContext.class,0);
		}
		public List<TerminalNode> HYPHEN() { return getTokens(TomlParser.HYPHEN); }
		public TerminalNode HYPHEN(int i) {
			return getToken(TomlParser.HYPHEN, i);
		}
		public DateMonthContext dateMonth() {
			return getRuleContext(DateMonthContext.class,0);
		}
		public DateMdayContext dateMday() {
			return getRuleContext(DateMdayContext.class,0);
		}
		public FullDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterFullDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitFullDate(this);
		}
	}

	public final FullDateContext fullDate() throws RecognitionException {
		FullDateContext _localctx = new FullDateContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_fullDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(465);
			dateFullyear();
			setState(466);
			match(HYPHEN);
			setState(467);
			dateMonth();
			setState(468);
			match(HYPHEN);
			setState(469);
			dateMday();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullTimeContext extends ParserRuleContext {
		public PartialTimeContext partialTime() {
			return getRuleContext(PartialTimeContext.class,0);
		}
		public TimeOffsetContext timeOffset() {
			return getRuleContext(TimeOffsetContext.class,0);
		}
		public FullTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterFullTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitFullTime(this);
		}
	}

	public final FullTimeContext fullTime() throws RecognitionException {
		FullTimeContext _localctx = new FullTimeContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_fullTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(471);
			partialTime();
			setState(472);
			timeOffset();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OffsetDateTimeContext extends ParserRuleContext {
		public FullDateContext fullDate() {
			return getRuleContext(FullDateContext.class,0);
		}
		public TimeDelimContext timeDelim() {
			return getRuleContext(TimeDelimContext.class,0);
		}
		public FullTimeContext fullTime() {
			return getRuleContext(FullTimeContext.class,0);
		}
		public OffsetDateTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offsetDateTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterOffsetDateTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitOffsetDateTime(this);
		}
	}

	public final OffsetDateTimeContext offsetDateTime() throws RecognitionException {
		OffsetDateTimeContext _localctx = new OffsetDateTimeContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_offsetDateTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(474);
			fullDate();
			setState(475);
			timeDelim();
			setState(476);
			fullTime();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocalDateTimeContext extends ParserRuleContext {
		public FullDateContext fullDate() {
			return getRuleContext(FullDateContext.class,0);
		}
		public TimeDelimContext timeDelim() {
			return getRuleContext(TimeDelimContext.class,0);
		}
		public PartialTimeContext partialTime() {
			return getRuleContext(PartialTimeContext.class,0);
		}
		public LocalDateTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localDateTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterLocalDateTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitLocalDateTime(this);
		}
	}

	public final LocalDateTimeContext localDateTime() throws RecognitionException {
		LocalDateTimeContext _localctx = new LocalDateTimeContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_localDateTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(478);
			fullDate();
			setState(479);
			timeDelim();
			setState(480);
			partialTime();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocalDateContext extends ParserRuleContext {
		public FullDateContext fullDate() {
			return getRuleContext(FullDateContext.class,0);
		}
		public LocalDateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localDate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterLocalDate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitLocalDate(this);
		}
	}

	public final LocalDateContext localDate() throws RecognitionException {
		LocalDateContext _localctx = new LocalDateContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_localDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			fullDate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocalTimeContext extends ParserRuleContext {
		public PartialTimeContext partialTime() {
			return getRuleContext(PartialTimeContext.class,0);
		}
		public LocalTimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localTime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterLocalTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitLocalTime(this);
		}
	}

	public final LocalTimeContext localTime() throws RecognitionException {
		LocalTimeContext _localctx = new LocalTimeContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_localTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(484);
			partialTime();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayContext extends ParserRuleContext {
		public ArrayOpenContext arrayOpen() {
			return getRuleContext(ArrayOpenContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public ArrayCloseContext arrayClose() {
			return getRuleContext(ArrayCloseContext.class,0);
		}
		public ArrayValuesContext arrayValues() {
			return getRuleContext(ArrayValuesContext.class,0);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArray(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_array);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(486);
			arrayOpen();
			setState(488);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				{
				setState(487);
				arrayValues();
				}
				break;
			}
			setState(490);
			ws();
			setState(491);
			arrayClose();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayOpenContext extends ParserRuleContext {
		public ArrayOpenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayOpen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayOpen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayOpen(this);
		}
	}

	public final ArrayOpenContext arrayOpen() throws RecognitionException {
		ArrayOpenContext _localctx = new ArrayOpenContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_arrayOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(493);
			match(T__16);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayCloseContext extends ParserRuleContext {
		public ArrayCloseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayClose; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayClose(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayClose(this);
		}
	}

	public final ArrayCloseContext arrayClose() throws RecognitionException {
		ArrayCloseContext _localctx = new ArrayCloseContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_arrayClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			match(T__17);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayValuesContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<ArrayvalsNonEmptyContext> arrayvalsNonEmpty() {
			return getRuleContexts(ArrayvalsNonEmptyContext.class);
		}
		public ArrayvalsNonEmptyContext arrayvalsNonEmpty(int i) {
			return getRuleContext(ArrayvalsNonEmptyContext.class,i);
		}
		public List<ArraySepContext> arraySep() {
			return getRuleContexts(ArraySepContext.class);
		}
		public ArraySepContext arraySep(int i) {
			return getRuleContext(ArraySepContext.class,i);
		}
		public ArrayValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayValues; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayValues(this);
		}
	}

	public final ArrayValuesContext arrayValues() throws RecognitionException {
		ArrayValuesContext _localctx = new ArrayValuesContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_arrayValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			ws();
			setState(498);
			arrayvalsNonEmpty();
			setState(505);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(499);
				arraySep();
				setState(500);
				ws();
				setState(501);
				arrayvalsNonEmpty();
				}
				}
				setState(507);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayvalsNonEmptyContext extends ParserRuleContext {
		public ValContext val() {
			return getRuleContext(ValContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public ArrayvalsNonEmptyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayvalsNonEmpty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayvalsNonEmpty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayvalsNonEmpty(this);
		}
	}

	public final ArrayvalsNonEmptyContext arrayvalsNonEmpty() throws RecognitionException {
		ArrayvalsNonEmptyContext _localctx = new ArrayvalsNonEmptyContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_arrayvalsNonEmpty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(508);
			val();
			setState(509);
			ws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArraySepContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(TomlParser.COMMA, 0); }
		public ArraySepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arraySep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArraySep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArraySep(this);
		}
	}

	public final ArraySepContext arraySep() throws RecognitionException {
		ArraySepContext _localctx = new ArraySepContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_arraySep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(511);
			match(COMMA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableContext extends ParserRuleContext {
		public StdTableContext stdTable() {
			return getRuleContext(StdTableContext.class,0);
		}
		public ArrayTableContext arrayTable() {
			return getRuleContext(ArrayTableContext.class,0);
		}
		public TableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTable(this);
		}
	}

	public final TableContext table() throws RecognitionException {
		TableContext _localctx = new TableContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_table);
		try {
			setState(515);
			switch (_input.LA(1)) {
			case T__16:
				enterOuterAlt(_localctx, 1);
				{
				setState(513);
				stdTable();
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 2);
				{
				setState(514);
				arrayTable();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StdTableContext extends ParserRuleContext {
		public StdTableOpenContext stdTableOpen() {
			return getRuleContext(StdTableOpenContext.class,0);
		}
		public List<KeyContext> key() {
			return getRuleContexts(KeyContext.class);
		}
		public KeyContext key(int i) {
			return getRuleContext(KeyContext.class,i);
		}
		public StdTableCloseContext stdTableClose() {
			return getRuleContext(StdTableCloseContext.class,0);
		}
		public List<TableKeySepContext> tableKeySep() {
			return getRuleContexts(TableKeySepContext.class);
		}
		public TableKeySepContext tableKeySep(int i) {
			return getRuleContext(TableKeySepContext.class,i);
		}
		public StdTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stdTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterStdTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitStdTable(this);
		}
	}

	public final StdTableContext stdTable() throws RecognitionException {
		StdTableContext _localctx = new StdTableContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_stdTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(517);
			stdTableOpen();
			setState(518);
			key();
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERIOD) {
				{
				{
				setState(519);
				tableKeySep();
				setState(520);
				key();
				}
				}
				setState(526);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(527);
			stdTableClose();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StdTableOpenContext extends ParserRuleContext {
		public StdTableOpenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stdTableOpen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterStdTableOpen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitStdTableOpen(this);
		}
	}

	public final StdTableOpenContext stdTableOpen() throws RecognitionException {
		StdTableOpenContext _localctx = new StdTableOpenContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_stdTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(529);
			match(T__16);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StdTableCloseContext extends ParserRuleContext {
		public StdTableCloseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stdTableClose; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterStdTableClose(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitStdTableClose(this);
		}
	}

	public final StdTableCloseContext stdTableClose() throws RecognitionException {
		StdTableCloseContext _localctx = new StdTableCloseContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_stdTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(531);
			match(T__17);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableKeySepContext extends ParserRuleContext {
		public TerminalNode PERIOD() { return getToken(TomlParser.PERIOD, 0); }
		public TableKeySepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableKeySep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterTableKeySep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitTableKeySep(this);
		}
	}

	public final TableKeySepContext tableKeySep() throws RecognitionException {
		TableKeySepContext _localctx = new TableKeySepContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_tableKeySep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(533);
			match(PERIOD);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableContext extends ParserRuleContext {
		public InlineTableOpenContext inlineTableOpen() {
			return getRuleContext(InlineTableOpenContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public InlineTableCloseContext inlineTableClose() {
			return getRuleContext(InlineTableCloseContext.class,0);
		}
		public InlineTableKeyvalsContext inlineTableKeyvals() {
			return getRuleContext(InlineTableKeyvalsContext.class,0);
		}
		public InlineTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTable(this);
		}
	}

	public final InlineTableContext inlineTable() throws RecognitionException {
		InlineTableContext _localctx = new InlineTableContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_inlineTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			inlineTableOpen();
			setState(537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(536);
				inlineTableKeyvals();
				}
				break;
			}
			setState(539);
			ws();
			setState(540);
			inlineTableClose();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableOpenContext extends ParserRuleContext {
		public InlineTableOpenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTableOpen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTableOpen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTableOpen(this);
		}
	}

	public final InlineTableOpenContext inlineTableOpen() throws RecognitionException {
		InlineTableOpenContext _localctx = new InlineTableOpenContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_inlineTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(542);
			match(T__18);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableCloseContext extends ParserRuleContext {
		public InlineTableCloseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTableClose; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTableClose(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTableClose(this);
		}
	}

	public final InlineTableCloseContext inlineTableClose() throws RecognitionException {
		InlineTableCloseContext _localctx = new InlineTableCloseContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_inlineTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(T__19);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableSepContext extends ParserRuleContext {
		public InlineTableSepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTableSep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTableSep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTableSep(this);
		}
	}

	public final InlineTableSepContext inlineTableSep() throws RecognitionException {
		InlineTableSepContext _localctx = new InlineTableSepContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_inlineTableSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(546);
			match(COMMA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableKeyvalsContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<InlineTableKeyvalsNonEmptyContext> inlineTableKeyvalsNonEmpty() {
			return getRuleContexts(InlineTableKeyvalsNonEmptyContext.class);
		}
		public InlineTableKeyvalsNonEmptyContext inlineTableKeyvalsNonEmpty(int i) {
			return getRuleContext(InlineTableKeyvalsNonEmptyContext.class,i);
		}
		public List<InlineTableSepContext> inlineTableSep() {
			return getRuleContexts(InlineTableSepContext.class);
		}
		public InlineTableSepContext inlineTableSep(int i) {
			return getRuleContext(InlineTableSepContext.class,i);
		}
		public InlineTableKeyvalsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTableKeyvals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTableKeyvals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTableKeyvals(this);
		}
	}

	public final InlineTableKeyvalsContext inlineTableKeyvals() throws RecognitionException {
		InlineTableKeyvalsContext _localctx = new InlineTableKeyvalsContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_inlineTableKeyvals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(548);
			ws();
			setState(549);
			inlineTableKeyvalsNonEmpty();
			setState(556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(550);
				inlineTableSep();
				setState(551);
				ws();
				setState(552);
				inlineTableKeyvalsNonEmpty();
				}
				}
				setState(558);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InlineTableKeyvalsNonEmptyContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public KeyvalSepContext keyvalSep() {
			return getRuleContext(KeyvalSepContext.class,0);
		}
		public ValContext val() {
			return getRuleContext(ValContext.class,0);
		}
		public InlineTableKeyvalsNonEmptyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inlineTableKeyvalsNonEmpty; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterInlineTableKeyvalsNonEmpty(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitInlineTableKeyvalsNonEmpty(this);
		}
	}

	public final InlineTableKeyvalsNonEmptyContext inlineTableKeyvalsNonEmpty() throws RecognitionException {
		InlineTableKeyvalsNonEmptyContext _localctx = new InlineTableKeyvalsNonEmptyContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_inlineTableKeyvalsNonEmpty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(559);
			key();
			setState(560);
			keyvalSep();
			setState(561);
			val();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayTableContext extends ParserRuleContext {
		public ArrayTableOpenContext arrayTableOpen() {
			return getRuleContext(ArrayTableOpenContext.class,0);
		}
		public List<KeyContext> key() {
			return getRuleContexts(KeyContext.class);
		}
		public KeyContext key(int i) {
			return getRuleContext(KeyContext.class,i);
		}
		public ArrayTableCloseContext arrayTableClose() {
			return getRuleContext(ArrayTableCloseContext.class,0);
		}
		public List<TableKeySepContext> tableKeySep() {
			return getRuleContexts(TableKeySepContext.class);
		}
		public TableKeySepContext tableKeySep(int i) {
			return getRuleContext(TableKeySepContext.class,i);
		}
		public ArrayTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayTable(this);
		}
	}

	public final ArrayTableContext arrayTable() throws RecognitionException {
		ArrayTableContext _localctx = new ArrayTableContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_arrayTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			arrayTableOpen();
			setState(564);
			key();
			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERIOD) {
				{
				{
				setState(565);
				tableKeySep();
				setState(566);
				key();
				}
				}
				setState(572);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(573);
			arrayTableClose();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayTableOpenContext extends ParserRuleContext {
		public ArrayTableOpenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayTableOpen; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayTableOpen(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayTableOpen(this);
		}
	}

	public final ArrayTableOpenContext arrayTableOpen() throws RecognitionException {
		ArrayTableOpenContext _localctx = new ArrayTableOpenContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_arrayTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(575);
			match(T__20);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayTableCloseContext extends ParserRuleContext {
		public ArrayTableCloseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayTableClose; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterArrayTableClose(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitArrayTableClose(this);
		}
	}

	public final ArrayTableCloseContext arrayTableClose() throws RecognitionException {
		ArrayTableCloseContext _localctx = new ArrayTableCloseContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_arrayTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(577);
			match(T__21);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\67\u0246\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\3\2\3\2\3\2"+
		"\3\2\7\2\u00a9\n\2\f\2\16\2\u00ac\13\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\5\4\u00b9\n\4\3\5\7\5\u00bc\n\5\f\5\16\5\u00bf\13\5\3\6\3"+
		"\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\5\t\u00cb\n\t\3\n\3\n\3\n\3\n\7\n\u00d1"+
		"\n\n\f\n\16\n\u00d4\13\n\3\13\3\13\5\13\u00d8\n\13\3\f\3\f\3\f\3\f\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u00e5\n\r\3\16\3\16\3\16\3\16\5\16\u00eb"+
		"\n\16\3\17\3\17\7\17\u00ef\n\17\f\17\16\17\u00f2\13\17\3\17\3\17\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20"+
		"\u0104\n\20\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u0118\n\25\f\25\16\25\u011b\13\25"+
		"\3\26\3\26\5\26\u011f\n\26\3\27\3\27\7\27\u0123\n\27\f\27\16\27\u0126"+
		"\13\27\3\27\3\27\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\32\3\32\7\32"+
		"\u0134\n\32\f\32\16\32\u0137\13\32\3\33\3\33\3\33\3\33\5\33\u013d\n\33"+
		"\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \5 \u0149\n \3 \3 \3!\3"+
		"!\3!\3!\3!\7!\u0152\n!\f!\16!\u0155\13!\5!\u0157\n!\3\"\3\"\7\"\u015b"+
		"\n\"\f\"\16\"\u015e\13\"\3\"\3\"\3\"\5\"\u0163\n\"\3#\3#\7#\u0167\n#\f"+
		"#\16#\u016a\13#\3#\3#\3#\5#\u016f\n#\3$\3$\7$\u0173\n$\f$\16$\u0176\13"+
		"$\3$\3$\3$\5$\u017b\n$\3%\3%\3%\3%\5%\u0181\n%\5%\u0183\n%\3%\5%\u0186"+
		"\n%\3&\3&\3\'\3\'\3\'\3(\3(\3)\3)\3)\3)\7)\u0193\n)\f)\16)\u0196\13)\3"+
		"*\3*\3*\3+\3+\5+\u019d\n+\3+\5+\u01a0\n+\3,\3,\3-\3-\3-\3-\5-\u01a8\n"+
		"-\3.\3.\3.\3.\3.\3/\3/\3/\3\60\3\60\3\60\3\61\3\61\3\62\3\62\3\62\3\63"+
		"\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3\67"+
		"\3\67\5\67\u01ca\n\67\38\38\38\38\38\38\58\u01d2\n8\39\39\39\39\39\39"+
		"\3:\3:\3:\3;\3;\3;\3;\3<\3<\3<\3<\3=\3=\3>\3>\3?\3?\5?\u01eb\n?\3?\3?"+
		"\3?\3@\3@\3A\3A\3B\3B\3B\3B\3B\3B\7B\u01fa\nB\fB\16B\u01fd\13B\3C\3C\3"+
		"C\3D\3D\3E\3E\5E\u0206\nE\3F\3F\3F\3F\3F\7F\u020d\nF\fF\16F\u0210\13F"+
		"\3F\3F\3G\3G\3H\3H\3I\3I\3J\3J\5J\u021c\nJ\3J\3J\3J\3K\3K\3L\3L\3M\3M"+
		"\3N\3N\3N\3N\3N\3N\7N\u022d\nN\fN\16N\u0230\13N\3O\3O\3O\3O\3P\3P\3P\3"+
		"P\3P\7P\u023b\nP\fP\16P\u023e\13P\3P\3P\3Q\3Q\3R\3R\3R\2\2S\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\2\n\4\2\31\31."+
		"\65\4\2\3\5\32\32\3\2\5\6\3\2\7\16\3\2/\60\3\2\61\62\4\2\32\32\63\64\4"+
		"\2\33\33**\u023d\2\u00a4\3\2\2\2\4\u00ad\3\2\2\2\6\u00b8\3\2\2\2\b\u00bd"+
		"\3\2\2\2\n\u00c0\3\2\2\2\f\u00c2\3\2\2\2\16\u00c4\3\2\2\2\20\u00ca\3\2"+
		"\2\2\22\u00d2\3\2\2\2\24\u00d7\3\2\2\2\26\u00d9\3\2\2\2\30\u00e4\3\2\2"+
		"\2\32\u00ea\3\2\2\2\34\u00ec\3\2\2\2\36\u0103\3\2\2\2 \u0105\3\2\2\2\""+
		"\u0107\3\2\2\2$\u0109\3\2\2\2&\u010d\3\2\2\2(\u0119\3\2\2\2*\u011e\3\2"+
		"\2\2,\u0120\3\2\2\2.\u0129\3\2\2\2\60\u012d\3\2\2\2\62\u0135\3\2\2\2\64"+
		"\u013c\3\2\2\2\66\u013e\3\2\2\28\u0140\3\2\2\2:\u0142\3\2\2\2<\u0144\3"+
		"\2\2\2>\u0148\3\2\2\2@\u0156\3\2\2\2B\u0158\3\2\2\2D\u0164\3\2\2\2F\u0170"+
		"\3\2\2\2H\u0185\3\2\2\2J\u0187\3\2\2\2L\u0189\3\2\2\2N\u018c\3\2\2\2P"+
		"\u018e\3\2\2\2R\u0197\3\2\2\2T\u019f\3\2\2\2V\u01a1\3\2\2\2X\u01a7\3\2"+
		"\2\2Z\u01a9\3\2\2\2\\\u01ae\3\2\2\2^\u01b1\3\2\2\2`\u01b4\3\2\2\2b\u01b6"+
		"\3\2\2\2d\u01b9\3\2\2\2f\u01bc\3\2\2\2h\u01bf\3\2\2\2j\u01c2\3\2\2\2l"+
		"\u01c9\3\2\2\2n\u01cb\3\2\2\2p\u01d3\3\2\2\2r\u01d9\3\2\2\2t\u01dc\3\2"+
		"\2\2v\u01e0\3\2\2\2x\u01e4\3\2\2\2z\u01e6\3\2\2\2|\u01e8\3\2\2\2~\u01ef"+
		"\3\2\2\2\u0080\u01f1\3\2\2\2\u0082\u01f3\3\2\2\2\u0084\u01fe\3\2\2\2\u0086"+
		"\u0201\3\2\2\2\u0088\u0205\3\2\2\2\u008a\u0207\3\2\2\2\u008c\u0213\3\2"+
		"\2\2\u008e\u0215\3\2\2\2\u0090\u0217\3\2\2\2\u0092\u0219\3\2\2\2\u0094"+
		"\u0220\3\2\2\2\u0096\u0222\3\2\2\2\u0098\u0224\3\2\2\2\u009a\u0226\3\2"+
		"\2\2\u009c\u0231\3\2\2\2\u009e\u0235\3\2\2\2\u00a0\u0241\3\2\2\2\u00a2"+
		"\u0243\3\2\2\2\u00a4\u00aa\5\6\4\2\u00a5\u00a6\5\f\7\2\u00a6\u00a7\5\6"+
		"\4\2\u00a7\u00a9\3\2\2\2\u00a8\u00a5\3\2\2\2\u00a9\u00ac\3\2\2\2\u00aa"+
		"\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\3\3\2\2\2\u00ac\u00aa\3\2\2\2"+
		"\u00ad\u00ae\t\2\2\2\u00ae\5\3\2\2\2\u00af\u00b9\5\b\5\2\u00b0\u00b1\5"+
		"\b\5\2\u00b1\u00b2\5\16\b\2\u00b2\u00b3\5\b\5\2\u00b3\u00b9\3\2\2\2\u00b4"+
		"\u00b5\5\b\5\2\u00b5\u00b6\5\u0088E\2\u00b6\u00b7\5\b\5\2\u00b7\u00b9"+
		"\3\2\2\2\u00b8\u00af\3\2\2\2\u00b8\u00b0\3\2\2\2\u00b8\u00b4\3\2\2\2\u00b9"+
		"\7\3\2\2\2\u00ba\u00bc\5\n\6\2\u00bb\u00ba\3\2\2\2\u00bc\u00bf\3\2\2\2"+
		"\u00bd\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\t\3\2\2\2\u00bf\u00bd\3"+
		"\2\2\2\u00c0\u00c1\t\3\2\2\u00c1\13\3\2\2\2\u00c2\u00c3\t\4\2\2\u00c3"+
		"\r\3\2\2\2\u00c4\u00c5\5\20\t\2\u00c5\u00c6\5\26\f\2\u00c6\u00c7\5\30"+
		"\r\2\u00c7\17\3\2\2\2\u00c8\u00cb\5\22\n\2\u00c9\u00cb\5\24\13\2\u00ca"+
		"\u00c8\3\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\21\3\2\2\2\u00cc\u00d1\5\4\3"+
		"\2\u00cd\u00d1\7\66\2\2\u00ce\u00d1\7\33\2\2\u00cf\u00d1\7\36\2\2\u00d0"+
		"\u00cc\3\2\2\2\u00d0\u00cd\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00cf\3\2"+
		"\2\2\u00d1\u00d4\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3"+
		"\23\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d5\u00d8\5\34\17\2\u00d6\u00d8\5,\27"+
		"\2\u00d7\u00d5\3\2\2\2\u00d7\u00d6\3\2\2\2\u00d8\25\3\2\2\2\u00d9\u00da"+
		"\5\b\5\2\u00da\u00db\7#\2\2\u00db\u00dc\5\b\5\2\u00dc\27\3\2\2\2\u00dd"+
		"\u00e5\5\32\16\2\u00de\u00e5\5V,\2\u00df\u00e5\5|?\2\u00e0\u00e5\5\u0092"+
		"J\2\u00e1\u00e5\5X-\2\u00e2\u00e5\5H%\2\u00e3\u00e5\5\64\33\2\u00e4\u00dd"+
		"\3\2\2\2\u00e4\u00de\3\2\2\2\u00e4\u00df\3\2\2\2\u00e4\u00e0\3\2\2\2\u00e4"+
		"\u00e1\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e3\3\2\2\2\u00e5\31\3\2\2"+
		"\2\u00e6\u00eb\5$\23\2\u00e7\u00eb\5\34\17\2\u00e8\u00eb\5.\30\2\u00e9"+
		"\u00eb\5,\27\2\u00ea\u00e6\3\2\2\2\u00ea\u00e7\3\2\2\2\u00ea\u00e8\3\2"+
		"\2\2\u00ea\u00e9\3\2\2\2\u00eb\33\3\2\2\2\u00ec\u00f0\7\35\2\2\u00ed\u00ef"+
		"\5\36\20\2\u00ee\u00ed\3\2\2\2\u00ef\u00f2\3\2\2\2\u00f0\u00ee\3\2\2\2"+
		"\u00f0\u00f1\3\2\2\2\u00f1\u00f3\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f3\u00f4"+
		"\7\35\2\2\u00f4\35\3\2\2\2\u00f5\u0104\5 \21\2\u00f6\u0104\5\4\3\2\u00f7"+
		"\u0104\7&\2\2\u00f8\u0104\7\32\2\2\u00f9\u0104\7*\2\2\u00fa\u0104\7\33"+
		"\2\2\u00fb\u0104\7\34\2\2\u00fc\u0104\7\36\2\2\u00fd\u0104\7\37\2\2\u00fe"+
		"\u0104\7 \2\2\u00ff\u0104\7!\2\2\u0100\u0104\7\"\2\2\u0101\u0104\7#\2"+
		"\2\u0102\u0104\7$\2\2\u0103\u00f5\3\2\2\2\u0103\u00f6\3\2\2\2\u0103\u00f7"+
		"\3\2\2\2\u0103\u00f8\3\2\2\2\u0103\u00f9\3\2\2\2\u0103\u00fa\3\2\2\2\u0103"+
		"\u00fb\3\2\2\2\u0103\u00fc\3\2\2\2\u0103\u00fd\3\2\2\2\u0103\u00fe\3\2"+
		"\2\2\u0103\u00ff\3\2\2\2\u0103\u0100\3\2\2\2\u0103\u0101\3\2\2\2\u0103"+
		"\u0102\3\2\2\2\u0104\37\3\2\2\2\u0105\u0106\5\"\22\2\u0106!\3\2\2\2\u0107"+
		"\u0108\t\5\2\2\u0108#\3\2\2\2\u0109\u010a\5&\24\2\u010a\u010b\5(\25\2"+
		"\u010b\u010c\5&\24\2\u010c%\3\2\2\2\u010d\u010e\7\35\2\2\u010e\u010f\7"+
		"\35\2\2\u010f\u0110\7\35\2\2\u0110\'\3\2\2\2\u0111\u0118\5*\26\2\u0112"+
		"\u0118\5\f\7\2\u0113\u0114\7\17\2\2\u0114\u0115\5\b\5\2\u0115\u0116\5"+
		"\f\7\2\u0116\u0118\3\2\2\2\u0117\u0111\3\2\2\2\u0117\u0112\3\2\2\2\u0117"+
		"\u0113\3\2\2\2\u0118\u011b\3\2\2\2\u0119\u0117\3\2\2\2\u0119\u011a\3\2"+
		"\2\2\u011a)\3\2\2\2\u011b\u0119\3\2\2\2\u011c\u011f\7\'\2\2\u011d\u011f"+
		"\5 \21\2\u011e\u011c\3\2\2\2\u011e\u011d\3\2\2\2\u011f+\3\2\2\2\u0120"+
		"\u0124\7\"\2\2\u0121\u0123\7(\2\2\u0122\u0121\3\2\2\2\u0123\u0126\3\2"+
		"\2\2\u0124\u0122\3\2\2\2\u0124\u0125\3\2\2\2\u0125\u0127\3\2\2\2\u0126"+
		"\u0124\3\2\2\2\u0127\u0128\7\"\2\2\u0128-\3\2\2\2\u0129\u012a\5\60\31"+
		"\2\u012a\u012b\5\62\32\2\u012b\u012c\5\60\31\2\u012c/\3\2\2\2\u012d\u012e"+
		"\7\"\2\2\u012e\u012f\7\"\2\2\u012f\u0130\7\"\2\2\u0130\61\3\2\2\2\u0131"+
		"\u0134\7)\2\2\u0132\u0134\5\f\7\2\u0133\u0131\3\2\2\2\u0133\u0132\3\2"+
		"\2\2\u0134\u0137\3\2\2\2\u0135\u0133\3\2\2\2\u0135\u0136\3\2\2\2\u0136"+
		"\63\3\2\2\2\u0137\u0135\3\2\2\2\u0138\u013d\5> \2\u0139\u013d\5B\"\2\u013a"+
		"\u013d\5D#\2\u013b\u013d\5F$\2\u013c\u0138\3\2\2\2\u013c\u0139\3\2\2\2"+
		"\u013c\u013a\3\2\2\2\u013c\u013b\3\2\2\2\u013d\65\3\2\2\2\u013e\u013f"+
		"\7\33\2\2\u013f\67\3\2\2\2\u0140\u0141\7\20\2\2\u01419\3\2\2\2\u0142\u0143"+
		"\7\21\2\2\u0143;\3\2\2\2\u0144\u0145\7\22\2\2\u0145=\3\2\2\2\u0146\u0149"+
		"\5\66\34\2\u0147\u0149\7*\2\2\u0148\u0146\3\2\2\2\u0148\u0147\3\2\2\2"+
		"\u0148\u0149\3\2\2\2\u0149\u014a\3\2\2\2\u014a\u014b\5@!\2\u014b?\3\2"+
		"\2\2\u014c\u0157\7\66\2\2\u014d\u0153\7+\2\2\u014e\u0152\7\66\2\2\u014f"+
		"\u0150\7\36\2\2\u0150\u0152\7\66\2\2\u0151\u014e\3\2\2\2\u0151\u014f\3"+
		"\2\2\2\u0152\u0155\3\2\2\2\u0153\u0151\3\2\2\2\u0153\u0154\3\2\2\2\u0154"+
		"\u0157\3\2\2\2\u0155\u0153\3\2\2\2\u0156\u014c\3\2\2\2\u0156\u014d\3\2"+
		"\2\2\u0157A\3\2\2\2\u0158\u015c\58\35\2\u0159\u015b\7\67\2\2\u015a\u0159"+
		"\3\2\2\2\u015b\u015e\3\2\2\2\u015c\u015a\3\2\2\2\u015c\u015d\3\2\2\2\u015d"+
		"\u0162\3\2\2\2\u015e\u015c\3\2\2\2\u015f\u0163\7\67\2\2\u0160\u0161\7"+
		"\36\2\2\u0161\u0163\7\67\2\2\u0162\u015f\3\2\2\2\u0162\u0160\3\2\2\2\u0163"+
		"C\3\2\2\2\u0164\u0168\5:\36\2\u0165\u0167\7,\2\2\u0166\u0165\3\2\2\2\u0167"+
		"\u016a\3\2\2\2\u0168\u0166\3\2\2\2\u0168\u0169\3\2\2\2\u0169\u016e\3\2"+
		"\2\2\u016a\u0168\3\2\2\2\u016b\u016f\7,\2\2\u016c\u016d\7\36\2\2\u016d"+
		"\u016f\7,\2\2\u016e\u016b\3\2\2\2\u016e\u016c\3\2\2\2\u016fE\3\2\2\2\u0170"+
		"\u0174\5<\37\2\u0171\u0173\7-\2\2\u0172\u0171\3\2\2\2\u0173\u0176\3\2"+
		"\2\2\u0174\u0172\3\2\2\2\u0174\u0175\3\2\2\2\u0175\u017a\3\2\2\2\u0176"+
		"\u0174\3\2\2\2\u0177\u017b\7-\2\2\u0178\u0179\7\36\2\2\u0179\u017b\7-"+
		"\2\2\u017a\u0177\3\2\2\2\u017a\u0178\3\2\2\2\u017bG\3\2\2\2\u017c\u0182"+
		"\5J&\2\u017d\u0183\5R*\2\u017e\u0180\5L\'\2\u017f\u0181\5R*\2\u0180\u017f"+
		"\3\2\2\2\u0180\u0181\3\2\2\2\u0181\u0183\3\2\2\2\u0182\u017d\3\2\2\2\u0182"+
		"\u017e\3\2\2\2\u0183\u0186\3\2\2\2\u0184\u0186\5T+\2\u0185\u017c\3\2\2"+
		"\2\u0185\u0184\3\2\2\2\u0186I\3\2\2\2\u0187\u0188\5> \2\u0188K\3\2\2\2"+
		"\u0189\u018a\5N(\2\u018a\u018b\5P)\2\u018bM\3\2\2\2\u018c\u018d\7\34\2"+
		"\2\u018dO\3\2\2\2\u018e\u0194\7\66\2\2\u018f\u0193\7\66\2\2\u0190\u0191"+
		"\7\36\2\2\u0191\u0193\7\66\2\2\u0192\u018f\3\2\2\2\u0192\u0190\3\2\2\2"+
		"\u0193\u0196\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0195\3\2\2\2\u0195Q\3"+
		"\2\2\2\u0196\u0194\3\2\2\2\u0197\u0198\7.\2\2\u0198\u0199\5J&\2\u0199"+
		"S\3\2\2\2\u019a\u01a0\5\66\34\2\u019b\u019d\7*\2\2\u019c\u019b\3\2\2\2"+
		"\u019c\u019d\3\2\2\2\u019d\u019e\3\2\2\2\u019e\u01a0\t\6\2\2\u019f\u019a"+
		"\3\2\2\2\u019f\u019c\3\2\2\2\u01a0U\3\2\2\2\u01a1\u01a2\t\7\2\2\u01a2"+
		"W\3\2\2\2\u01a3\u01a8\5t;\2\u01a4\u01a8\5v<\2\u01a5\u01a8\5x=\2\u01a6"+
		"\u01a8\5z>\2\u01a7\u01a3\3\2\2\2\u01a7\u01a4\3\2\2\2\u01a7\u01a5\3\2\2"+
		"\2\u01a7\u01a6\3\2\2\2\u01a8Y\3\2\2\2\u01a9\u01aa\7\66\2\2\u01aa\u01ab"+
		"\7\66\2\2\u01ab\u01ac\7\66\2\2\u01ac\u01ad\7\66\2\2\u01ad[\3\2\2\2\u01ae"+
		"\u01af\7\66\2\2\u01af\u01b0\7\66\2\2\u01b0]\3\2\2\2\u01b1\u01b2\7\66\2"+
		"\2\u01b2\u01b3\7\66\2\2\u01b3_\3\2\2\2\u01b4\u01b5\t\b\2\2\u01b5a\3\2"+
		"\2\2\u01b6\u01b7\7\66\2\2\u01b7\u01b8\7\66\2\2\u01b8c\3\2\2\2\u01b9\u01ba"+
		"\7\66\2\2\u01ba\u01bb\7\66\2\2\u01bbe\3\2\2\2\u01bc\u01bd\7\66\2\2\u01bd"+
		"\u01be\7\66\2\2\u01beg\3\2\2\2\u01bf\u01c0\7\34\2\2\u01c0\u01c1\7\66\2"+
		"\2\u01c1i\3\2\2\2\u01c2\u01c3\t\t\2\2\u01c3\u01c4\5b\62\2\u01c4\u01c5"+
		"\7\37\2\2\u01c5\u01c6\5d\63\2\u01c6k\3\2\2\2\u01c7\u01ca\7\65\2\2\u01c8"+
		"\u01ca\5j\66\2\u01c9\u01c7\3\2\2\2\u01c9\u01c8\3\2\2\2\u01cam\3\2\2\2"+
		"\u01cb\u01cc\5b\62\2\u01cc\u01cd\7\37\2\2\u01cd\u01ce\5d\63\2\u01ce\u01cf"+
		"\7\37\2\2\u01cf\u01d1\5f\64\2\u01d0\u01d2\5h\65\2\u01d1\u01d0\3\2\2\2"+
		"\u01d1\u01d2\3\2\2\2\u01d2o\3\2\2\2\u01d3\u01d4\5Z.\2\u01d4\u01d5\7\33"+
		"\2\2\u01d5\u01d6\5\\/\2\u01d6\u01d7\7\33\2\2\u01d7\u01d8\5^\60\2\u01d8"+
		"q\3\2\2\2\u01d9\u01da\5n8\2\u01da\u01db\5l\67\2\u01dbs\3\2\2\2\u01dc\u01dd"+
		"\5p9\2\u01dd\u01de\5`\61\2\u01de\u01df\5r:\2\u01dfu\3\2\2\2\u01e0\u01e1"+
		"\5p9\2\u01e1\u01e2\5`\61\2\u01e2\u01e3\5n8\2\u01e3w\3\2\2\2\u01e4\u01e5"+
		"\5p9\2\u01e5y\3\2\2\2\u01e6\u01e7\5n8\2\u01e7{\3\2\2\2\u01e8\u01ea\5~"+
		"@\2\u01e9\u01eb\5\u0082B\2\u01ea\u01e9\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb"+
		"\u01ec\3\2\2\2\u01ec\u01ed\5\b\5\2\u01ed\u01ee\5\u0080A\2\u01ee}\3\2\2"+
		"\2\u01ef\u01f0\7\23\2\2\u01f0\177\3\2\2\2\u01f1\u01f2\7\24\2\2\u01f2\u0081"+
		"\3\2\2\2\u01f3\u01f4\5\b\5\2\u01f4\u01fb\5\u0084C\2\u01f5\u01f6\5\u0086"+
		"D\2\u01f6\u01f7\5\b\5\2\u01f7\u01f8\5\u0084C\2\u01f8\u01fa\3\2\2\2\u01f9"+
		"\u01f5\3\2\2\2\u01fa\u01fd\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fb\u01fc\3\2"+
		"\2\2\u01fc\u0083\3\2\2\2\u01fd\u01fb\3\2\2\2\u01fe\u01ff\5\30\r\2\u01ff"+
		"\u0200\5\b\5\2\u0200\u0085\3\2\2\2\u0201\u0202\7 \2\2\u0202\u0087\3\2"+
		"\2\2\u0203\u0206\5\u008aF\2\u0204\u0206\5\u009eP\2\u0205\u0203\3\2\2\2"+
		"\u0205\u0204\3\2\2\2\u0206\u0089\3\2\2\2\u0207\u0208\5\u008cG\2\u0208"+
		"\u020e\5\20\t\2\u0209\u020a\5\u0090I\2\u020a\u020b\5\20\t\2\u020b\u020d"+
		"\3\2\2\2\u020c\u0209\3\2\2\2\u020d\u0210\3\2\2\2\u020e\u020c\3\2\2\2\u020e"+
		"\u020f\3\2\2\2\u020f\u0211\3\2\2\2\u0210\u020e\3\2\2\2\u0211\u0212\5\u008e"+
		"H\2\u0212\u008b\3\2\2\2\u0213\u0214\7\23\2\2\u0214\u008d\3\2\2\2\u0215"+
		"\u0216\7\24\2\2\u0216\u008f\3\2\2\2\u0217\u0218\7\34\2\2\u0218\u0091\3"+
		"\2\2\2\u0219\u021b\5\u0094K\2\u021a\u021c\5\u009aN\2\u021b\u021a\3\2\2"+
		"\2\u021b\u021c\3\2\2\2\u021c\u021d\3\2\2\2\u021d\u021e\5\b\5\2\u021e\u021f"+
		"\5\u0096L\2\u021f\u0093\3\2\2\2\u0220\u0221\7\25\2\2\u0221\u0095\3\2\2"+
		"\2\u0222\u0223\7\26\2\2\u0223\u0097\3\2\2\2\u0224\u0225\7 \2\2\u0225\u0099"+
		"\3\2\2\2\u0226\u0227\5\b\5\2\u0227\u022e\5\u009cO\2\u0228\u0229\5\u0098"+
		"M\2\u0229\u022a\5\b\5\2\u022a\u022b\5\u009cO\2\u022b\u022d\3\2\2\2\u022c"+
		"\u0228\3\2\2\2\u022d\u0230\3\2\2\2\u022e\u022c\3\2\2\2\u022e\u022f\3\2"+
		"\2\2\u022f\u009b\3\2\2\2\u0230\u022e\3\2\2\2\u0231\u0232\5\20\t\2\u0232"+
		"\u0233\5\26\f\2\u0233\u0234\5\30\r\2\u0234\u009d\3\2\2\2\u0235\u0236\5"+
		"\u00a0Q\2\u0236\u023c\5\20\t\2\u0237\u0238\5\u0090I\2\u0238\u0239\5\20"+
		"\t\2\u0239\u023b\3\2\2\2\u023a\u0237\3\2\2\2\u023b\u023e\3\2\2\2\u023c"+
		"\u023a\3\2\2\2\u023c\u023d\3\2\2\2\u023d\u023f\3\2\2\2\u023e\u023c\3\2"+
		"\2\2\u023f\u0240\5\u00a2R\2\u0240\u009f\3\2\2\2\u0241\u0242\7\27\2\2\u0242"+
		"\u00a1\3\2\2\2\u0243\u0244\7\30\2\2\u0244\u00a3\3\2\2\2/\u00aa\u00b8\u00bd"+
		"\u00ca\u00d0\u00d2\u00d7\u00e4\u00ea\u00f0\u0103\u0117\u0119\u011e\u0124"+
		"\u0133\u0135\u013c\u0148\u0151\u0153\u0156\u015c\u0162\u0168\u016e\u0174"+
		"\u017a\u0180\u0182\u0185\u0192\u0194\u019c\u019f\u01a7\u01c9\u01d1\u01ea"+
		"\u01fb\u0205\u020e\u021b\u022e\u023c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}