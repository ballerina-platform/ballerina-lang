// Generated from Toml.g4 by ANTLR 4.5.3
package org.ballerinalang.toml.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TomlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, ALPHA=17, 
		SPACE=18, HYPHEN=19, PERIOD=20, QUOTATION_MARK=21, UNDERSCORE=22, COLON=23, 
		COMMA=24, SLASH=25, APOSTROPHE=26, EQUALS=27, HASH=28, LEFT_BRACKET=29, 
		RIGHT_BRACKET=30, LEFT_BRACE=31, RIGHT_BRACE=32, COMMENT=33, DIGIT19=34, 
		BASICUNESCAPED=35, MLBASICUNESCAPED=36, LITERALCHAR=37, MLLITERALCHAR=38, 
		PLUS=39, DIGIT07=40, DIGIT01=41, BIN_PREFIX=42, E=43, INF=44, NAN=45, 
		TRUE=46, FALSE=47, UPPERCASE_T=48, LOWERCASE_T=49, UPPERCASE_Z=50, HEXDIG=51;
	public static final int
		RULE_toml = 0, RULE_alpha = 1, RULE_expression = 2, RULE_ws = 3, RULE_wschar = 4, 
		RULE_newline = 5, RULE_keyVal = 6, RULE_key = 7, RULE_simpleKey = 8, RULE_unquotedKey = 9, 
		RULE_quotedKey = 10, RULE_dottedKey = 11, RULE_keyValSep = 12, RULE_dotSep = 13, 
		RULE_val = 14, RULE_string = 15, RULE_basicString = 16, RULE_basicStringValue = 17, 
		RULE_basicChar = 18, RULE_digit = 19, RULE_escaped = 20, RULE_escapeSeqChar = 21, 
		RULE_mlBasicString = 22, RULE_mlBasicStringDelim = 23, RULE_mlBasicBody = 24, 
		RULE_mlBasicChar = 25, RULE_literalString = 26, RULE_mlLiteralString = 27, 
		RULE_mlLiteralStringDelim = 28, RULE_mlLiteralBody = 29, RULE_integer = 30, 
		RULE_minus = 31, RULE_hexPrefix = 32, RULE_octPrefix = 33, RULE_decInt = 34, 
		RULE_unsignedDecInt = 35, RULE_hexInt = 36, RULE_octInt = 37, RULE_binInt = 38, 
		RULE_floatingPoint = 39, RULE_floatIntPart = 40, RULE_frac = 41, RULE_decimalPoint = 42, 
		RULE_zeroPrefixableInt = 43, RULE_exp = 44, RULE_specialFloat = 45, RULE_bool = 46, 
		RULE_dateTime = 47, RULE_dateFullyear = 48, RULE_dateMonth = 49, RULE_dateMday = 50, 
		RULE_timeDelim = 51, RULE_timeHour = 52, RULE_timeMinute = 53, RULE_timeSecond = 54, 
		RULE_timeSecfrac = 55, RULE_timeNumoffset = 56, RULE_timeOffset = 57, 
		RULE_partialTime = 58, RULE_fullDate = 59, RULE_fullTime = 60, RULE_offsetDateTime = 61, 
		RULE_localDateTime = 62, RULE_localDate = 63, RULE_localTime = 64, RULE_array = 65, 
		RULE_arrayOpen = 66, RULE_arrayClose = 67, RULE_arrayValues = 68, RULE_arrayvalsNonEmpty = 69, 
		RULE_arraySep = 70, RULE_inlineTable = 71, RULE_inlineTableOpen = 72, 
		RULE_inlineTableClose = 73, RULE_inlineTableSep = 74, RULE_inlineTableKeyvals = 75, 
		RULE_inlineTableKeyvalsNonEmpty = 76, RULE_table = 77, RULE_stdTable = 78, 
		RULE_stdTableOpen = 79, RULE_stdTableClose = 80;
	public static final String[] ruleNames = {
		"toml", "alpha", "expression", "ws", "wschar", "newline", "keyVal", "key", 
		"simpleKey", "unquotedKey", "quotedKey", "dottedKey", "keyValSep", "dotSep", 
		"val", "string", "basicString", "basicStringValue", "basicChar", "digit", 
		"escaped", "escapeSeqChar", "mlBasicString", "mlBasicStringDelim", "mlBasicBody", 
		"mlBasicChar", "literalString", "mlLiteralString", "mlLiteralStringDelim", 
		"mlLiteralBody", "integer", "minus", "hexPrefix", "octPrefix", "decInt", 
		"unsignedDecInt", "hexInt", "octInt", "binInt", "floatingPoint", "floatIntPart", 
		"frac", "decimalPoint", "zeroPrefixableInt", "exp", "specialFloat", "bool", 
		"dateTime", "dateFullyear", "dateMonth", "dateMday", "timeDelim", "timeHour", 
		"timeMinute", "timeSecond", "timeSecfrac", "timeNumoffset", "timeOffset", 
		"partialTime", "fullDate", "fullTime", "offsetDateTime", "localDateTime", 
		"localDate", "localTime", "array", "arrayOpen", "arrayClose", "arrayValues", 
		"arrayvalsNonEmpty", "arraySep", "inlineTable", "inlineTableOpen", "inlineTableClose", 
		"inlineTableSep", "inlineTableKeyvals", "inlineTableKeyvalsNonEmpty", 
		"table", "stdTable", "stdTableOpen", "stdTableClose"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\t'", "'\r'", "'\n'", "'\r\n'", "'0'", "'\\\"'", "'\\\\'", "'\\/'", 
		"'\\b'", "'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\'", "'0x'", "'0o'", null, 
		"' '", "'-'", "'.'", "'\"'", "'_'", "':'", "','", "'/'", "'''", "'='", 
		"'#'", "'['", "']'", "'{'", "'}'", null, null, null, null, null, null, 
		"'+'", null, null, "'0b'", "'e'", "'inf'", "'nan'", "'true'", "'false'", 
		"'T'", "'t'", "'Z'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, "ALPHA", "SPACE", "HYPHEN", "PERIOD", "QUOTATION_MARK", 
		"UNDERSCORE", "COLON", "COMMA", "SLASH", "APOSTROPHE", "EQUALS", "HASH", 
		"LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_BRACE", "RIGHT_BRACE", "COMMENT", 
		"DIGIT19", "BASICUNESCAPED", "MLBASICUNESCAPED", "LITERALCHAR", "MLLITERALCHAR", 
		"PLUS", "DIGIT07", "DIGIT01", "BIN_PREFIX", "E", "INF", "NAN", "TRUE", 
		"FALSE", "UPPERCASE_T", "LOWERCASE_T", "UPPERCASE_Z", "HEXDIG"
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
		public TerminalNode BIN_PREFIX() { return getToken(TomlParser.BIN_PREFIX, 0); }
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
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALPHA) | (1L << BIN_PREFIX) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) ) {
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
		public KeyValContext keyVal() {
			return getRuleContext(KeyValContext.class,0);
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
				keyVal();
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

	public static class KeyValContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public KeyValSepContext keyValSep() {
			return getRuleContext(KeyValSepContext.class,0);
		}
		public ValContext val() {
			return getRuleContext(ValContext.class,0);
		}
		public KeyValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterKeyVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitKeyVal(this);
		}
	}

	public final KeyValContext keyVal() throws RecognitionException {
		KeyValContext _localctx = new KeyValContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_keyVal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			key();
			setState(195);
			keyValSep();
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
		public DottedKeyContext dottedKey() {
			return getRuleContext(DottedKeyContext.class,0);
		}
		public SimpleKeyContext simpleKey() {
			return getRuleContext(SimpleKeyContext.class,0);
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
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(198);
				dottedKey();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(199);
				simpleKey();
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

	public static class SimpleKeyContext extends ParserRuleContext {
		public UnquotedKeyContext unquotedKey() {
			return getRuleContext(UnquotedKeyContext.class,0);
		}
		public QuotedKeyContext quotedKey() {
			return getRuleContext(QuotedKeyContext.class,0);
		}
		public SimpleKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterSimpleKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitSimpleKey(this);
		}
	}

	public final SimpleKeyContext simpleKey() throws RecognitionException {
		SimpleKeyContext _localctx = new SimpleKeyContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_simpleKey);
		try {
			setState(204);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case T__4:
			case ALPHA:
			case SPACE:
			case HYPHEN:
			case PERIOD:
			case UNDERSCORE:
			case EQUALS:
			case RIGHT_BRACKET:
			case DIGIT19:
			case BIN_PREFIX:
			case E:
			case INF:
			case NAN:
			case TRUE:
			case FALSE:
			case UPPERCASE_T:
			case LOWERCASE_T:
			case UPPERCASE_Z:
				enterOuterAlt(_localctx, 1);
				{
				setState(202);
				unquotedKey();
				}
				break;
			case QUOTATION_MARK:
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(203);
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 18, RULE_unquotedKey);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << ALPHA) | (1L << HYPHEN) | (1L << UNDERSCORE) | (1L << DIGIT19) | (1L << BIN_PREFIX) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) {
				{
				setState(210);
				switch (_input.LA(1)) {
				case ALPHA:
				case BIN_PREFIX:
				case E:
				case INF:
				case NAN:
				case TRUE:
				case FALSE:
				case UPPERCASE_T:
				case LOWERCASE_T:
				case UPPERCASE_Z:
					{
					setState(206);
					alpha();
					}
					break;
				case T__4:
				case DIGIT19:
					{
					setState(207);
					digit();
					}
					break;
				case HYPHEN:
					{
					setState(208);
					match(HYPHEN);
					}
					break;
				case UNDERSCORE:
					{
					setState(209);
					match(UNDERSCORE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(214);
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
		enterRule(_localctx, 20, RULE_quotedKey);
		try {
			setState(217);
			switch (_input.LA(1)) {
			case QUOTATION_MARK:
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				basicString();
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(216);
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

	public static class DottedKeyContext extends ParserRuleContext {
		public List<SimpleKeyContext> simpleKey() {
			return getRuleContexts(SimpleKeyContext.class);
		}
		public SimpleKeyContext simpleKey(int i) {
			return getRuleContext(SimpleKeyContext.class,i);
		}
		public List<DotSepContext> dotSep() {
			return getRuleContexts(DotSepContext.class);
		}
		public DotSepContext dotSep(int i) {
			return getRuleContext(DotSepContext.class,i);
		}
		public DottedKeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dottedKey; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDottedKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDottedKey(this);
		}
	}

	public final DottedKeyContext dottedKey() throws RecognitionException {
		DottedKeyContext _localctx = new DottedKeyContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_dottedKey);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			simpleKey();
			setState(225);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(220);
					dotSep();
					setState(221);
					simpleKey();
					}
					} 
				}
				setState(227);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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

	public static class KeyValSepContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode EQUALS() { return getToken(TomlParser.EQUALS, 0); }
		public KeyValSepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyValSep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterKeyValSep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitKeyValSep(this);
		}
	}

	public final KeyValSepContext keyValSep() throws RecognitionException {
		KeyValSepContext _localctx = new KeyValSepContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_keyValSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			ws();
			setState(229);
			match(EQUALS);
			setState(230);
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

	public static class DotSepContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode PERIOD() { return getToken(TomlParser.PERIOD, 0); }
		public DotSepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dotSep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDotSep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDotSep(this);
		}
	}

	public final DotSepContext dotSep() throws RecognitionException {
		DotSepContext _localctx = new DotSepContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_dotSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			ws();
			setState(233);
			match(PERIOD);
			setState(234);
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
		public DateTimeContext dateTime() {
			return getRuleContext(DateTimeContext.class,0);
		}
		public FloatingPointContext floatingPoint() {
			return getRuleContext(FloatingPointContext.class,0);
		}
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public InlineTableContext inlineTable() {
			return getRuleContext(InlineTableContext.class,0);
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
		enterRule(_localctx, 28, RULE_val);
		try {
			setState(243);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(236);
				string();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(237);
				bool();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(238);
				array();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(239);
				dateTime();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(240);
				floatingPoint();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(241);
				integer();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(242);
				inlineTable();
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
		enterRule(_localctx, 30, RULE_string);
		try {
			setState(249);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(245);
				mlBasicString();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(246);
				basicString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(247);
				mlLiteralString();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(248);
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
		public BasicStringValueContext basicStringValue() {
			return getRuleContext(BasicStringValueContext.class,0);
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
		enterRule(_localctx, 32, RULE_basicString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(251);
			match(QUOTATION_MARK);
			setState(252);
			basicStringValue();
			setState(253);
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

	public static class BasicStringValueContext extends ParserRuleContext {
		public List<BasicCharContext> basicChar() {
			return getRuleContexts(BasicCharContext.class);
		}
		public BasicCharContext basicChar(int i) {
			return getRuleContext(BasicCharContext.class,i);
		}
		public BasicStringValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basicStringValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterBasicStringValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitBasicStringValue(this);
		}
	}

	public final BasicStringValueContext basicStringValue() throws RecognitionException {
		BasicStringValueContext _localctx = new BasicStringValueContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_basicStringValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << ALPHA) | (1L << SPACE) | (1L << HYPHEN) | (1L << PERIOD) | (1L << UNDERSCORE) | (1L << COLON) | (1L << COMMA) | (1L << SLASH) | (1L << APOSTROPHE) | (1L << EQUALS) | (1L << HASH) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << LEFT_BRACE) | (1L << RIGHT_BRACE) | (1L << DIGIT19) | (1L << BASICUNESCAPED) | (1L << PLUS) | (1L << BIN_PREFIX) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) {
				{
				{
				setState(255);
				basicChar();
				}
				}
				setState(260);
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

	public static class BasicCharContext extends ParserRuleContext {
		public EscapedContext escaped() {
			return getRuleContext(EscapedContext.class,0);
		}
		public AlphaContext alpha() {
			return getRuleContext(AlphaContext.class,0);
		}
		public TerminalNode BASICUNESCAPED() { return getToken(TomlParser.BASICUNESCAPED, 0); }
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
		public TerminalNode LEFT_BRACKET() { return getToken(TomlParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(TomlParser.RIGHT_BRACKET, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(TomlParser.LEFT_BRACE, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(TomlParser.RIGHT_BRACE, 0); }
		public DigitContext digit() {
			return getRuleContext(DigitContext.class,0);
		}
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
		enterRule(_localctx, 36, RULE_basicChar);
		try {
			setState(280);
			switch (_input.LA(1)) {
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case T__12:
				enterOuterAlt(_localctx, 1);
				{
				setState(261);
				escaped();
				}
				break;
			case ALPHA:
			case BIN_PREFIX:
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
				setState(262);
				alpha();
				}
				break;
			case BASICUNESCAPED:
				enterOuterAlt(_localctx, 3);
				{
				setState(263);
				match(BASICUNESCAPED);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 4);
				{
				setState(264);
				match(SPACE);
				}
				break;
			case PLUS:
				enterOuterAlt(_localctx, 5);
				{
				setState(265);
				match(PLUS);
				}
				break;
			case HYPHEN:
				enterOuterAlt(_localctx, 6);
				{
				setState(266);
				match(HYPHEN);
				}
				break;
			case PERIOD:
				enterOuterAlt(_localctx, 7);
				{
				setState(267);
				match(PERIOD);
				}
				break;
			case UNDERSCORE:
				enterOuterAlt(_localctx, 8);
				{
				setState(268);
				match(UNDERSCORE);
				}
				break;
			case COLON:
				enterOuterAlt(_localctx, 9);
				{
				setState(269);
				match(COLON);
				}
				break;
			case COMMA:
				enterOuterAlt(_localctx, 10);
				{
				setState(270);
				match(COMMA);
				}
				break;
			case SLASH:
				enterOuterAlt(_localctx, 11);
				{
				setState(271);
				match(SLASH);
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 12);
				{
				setState(272);
				match(APOSTROPHE);
				}
				break;
			case EQUALS:
				enterOuterAlt(_localctx, 13);
				{
				setState(273);
				match(EQUALS);
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 14);
				{
				setState(274);
				match(HASH);
				}
				break;
			case LEFT_BRACKET:
				enterOuterAlt(_localctx, 15);
				{
				setState(275);
				match(LEFT_BRACKET);
				}
				break;
			case RIGHT_BRACKET:
				enterOuterAlt(_localctx, 16);
				{
				setState(276);
				match(RIGHT_BRACKET);
				}
				break;
			case LEFT_BRACE:
				enterOuterAlt(_localctx, 17);
				{
				setState(277);
				match(LEFT_BRACE);
				}
				break;
			case RIGHT_BRACE:
				enterOuterAlt(_localctx, 18);
				{
				setState(278);
				match(RIGHT_BRACE);
				}
				break;
			case T__4:
			case DIGIT19:
				enterOuterAlt(_localctx, 19);
				{
				setState(279);
				digit();
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

	public static class DigitContext extends ParserRuleContext {
		public TerminalNode DIGIT19() { return getToken(TomlParser.DIGIT19, 0); }
		public DigitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_digit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).enterDigit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TomlListener ) ((TomlListener)listener).exitDigit(this);
		}
	}

	public final DigitContext digit() throws RecognitionException {
		DigitContext _localctx = new DigitContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_digit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(282);
			_la = _input.LA(1);
			if ( !(_la==T__4 || _la==DIGIT19) ) {
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
		enterRule(_localctx, 40, RULE_escaped);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
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
		enterRule(_localctx, 42, RULE_escapeSeqChar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12))) != 0)) ) {
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
		enterRule(_localctx, 44, RULE_mlBasicString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			mlBasicStringDelim();
			setState(289);
			mlBasicBody();
			setState(290);
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
		enterRule(_localctx, 46, RULE_mlBasicStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			match(QUOTATION_MARK);
			setState(293);
			match(QUOTATION_MARK);
			setState(294);
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
		enterRule(_localctx, 48, RULE_mlBasicBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(304);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << MLBASICUNESCAPED))) != 0)) {
				{
				setState(302);
				switch (_input.LA(1)) {
				case T__5:
				case T__6:
				case T__7:
				case T__8:
				case T__9:
				case T__10:
				case T__11:
				case T__12:
				case MLBASICUNESCAPED:
					{
					setState(296);
					mlBasicChar();
					}
					break;
				case T__2:
				case T__3:
					{
					setState(297);
					newline();
					}
					break;
				case T__13:
					{
					{
					setState(298);
					match(T__13);
					setState(299);
					ws();
					setState(300);
					newline();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(306);
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
		enterRule(_localctx, 50, RULE_mlBasicChar);
		try {
			setState(309);
			switch (_input.LA(1)) {
			case MLBASICUNESCAPED:
				enterOuterAlt(_localctx, 1);
				{
				setState(307);
				match(MLBASICUNESCAPED);
				}
				break;
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case T__12:
				enterOuterAlt(_localctx, 2);
				{
				setState(308);
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
		enterRule(_localctx, 52, RULE_literalString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			match(APOSTROPHE);
			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LITERALCHAR) {
				{
				{
				setState(312);
				match(LITERALCHAR);
				}
				}
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(318);
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
		enterRule(_localctx, 54, RULE_mlLiteralString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(320);
			mlLiteralStringDelim();
			setState(321);
			mlLiteralBody();
			setState(322);
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
		enterRule(_localctx, 56, RULE_mlLiteralStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			match(APOSTROPHE);
			setState(325);
			match(APOSTROPHE);
			setState(326);
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
		enterRule(_localctx, 58, RULE_mlLiteralBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << MLLITERALCHAR))) != 0)) {
				{
				setState(330);
				switch (_input.LA(1)) {
				case MLLITERALCHAR:
					{
					setState(328);
					match(MLLITERALCHAR);
					}
					break;
				case T__2:
				case T__3:
					{
					setState(329);
					newline();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(334);
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
		enterRule(_localctx, 60, RULE_integer);
		try {
			setState(339);
			switch (_input.LA(1)) {
			case T__4:
			case HYPHEN:
			case DIGIT19:
			case PLUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(335);
				decInt();
				}
				break;
			case T__14:
				enterOuterAlt(_localctx, 2);
				{
				setState(336);
				hexInt();
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 3);
				{
				setState(337);
				octInt();
				}
				break;
			case BIN_PREFIX:
				enterOuterAlt(_localctx, 4);
				{
				setState(338);
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
		enterRule(_localctx, 62, RULE_minus);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(341);
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
		enterRule(_localctx, 64, RULE_hexPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
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
		enterRule(_localctx, 66, RULE_octPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(345);
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
		enterRule(_localctx, 68, RULE_decInt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			switch (_input.LA(1)) {
			case HYPHEN:
				{
				setState(347);
				minus();
				}
				break;
			case PLUS:
				{
				setState(348);
				match(PLUS);
				}
				break;
			case T__4:
			case DIGIT19:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(351);
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 70, RULE_unsignedDecInt);
		int _la;
		try {
			setState(363);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(353);
				digit();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
				match(DIGIT19);
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << UNDERSCORE) | (1L << DIGIT19))) != 0)) {
					{
					setState(358);
					switch (_input.LA(1)) {
					case T__4:
					case DIGIT19:
						{
						setState(355);
						digit();
						}
						break;
					case UNDERSCORE:
						{
						setState(356);
						match(UNDERSCORE);
						setState(357);
						digit();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(362);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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
		enterRule(_localctx, 72, RULE_hexInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			hexPrefix();
			setState(369);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(366);
					match(HEXDIG);
					}
					} 
				}
				setState(371);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			setState(375);
			switch (_input.LA(1)) {
			case HEXDIG:
				{
				setState(372);
				match(HEXDIG);
				}
				break;
			case UNDERSCORE:
				{
				setState(373);
				match(UNDERSCORE);
				setState(374);
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
		enterRule(_localctx, 74, RULE_octInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(377);
			octPrefix();
			setState(381);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(378);
					match(DIGIT07);
					}
					} 
				}
				setState(383);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			}
			setState(387);
			switch (_input.LA(1)) {
			case DIGIT07:
				{
				setState(384);
				match(DIGIT07);
				}
				break;
			case UNDERSCORE:
				{
				setState(385);
				match(UNDERSCORE);
				setState(386);
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
		public TerminalNode BIN_PREFIX() { return getToken(TomlParser.BIN_PREFIX, 0); }
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
		enterRule(_localctx, 76, RULE_binInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			match(BIN_PREFIX);
			setState(393);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(390);
					match(DIGIT01);
					}
					} 
				}
				setState(395);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			setState(399);
			switch (_input.LA(1)) {
			case DIGIT01:
				{
				setState(396);
				match(DIGIT01);
				}
				break;
			case UNDERSCORE:
				{
				setState(397);
				match(UNDERSCORE);
				setState(398);
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
		enterRule(_localctx, 78, RULE_floatingPoint);
		int _la;
		try {
			setState(410);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(401);
				floatIntPart();
				setState(407);
				switch (_input.LA(1)) {
				case E:
					{
					setState(402);
					exp();
					}
					break;
				case PERIOD:
					{
					setState(403);
					frac();
					setState(405);
					_la = _input.LA(1);
					if (_la==E) {
						{
						setState(404);
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
				setState(409);
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
		enterRule(_localctx, 80, RULE_floatIntPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412);
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
		enterRule(_localctx, 82, RULE_frac);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414);
			decimalPoint();
			setState(415);
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
		enterRule(_localctx, 84, RULE_decimalPoint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 86, RULE_zeroPrefixableInt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			digit();
			setState(425);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << UNDERSCORE) | (1L << DIGIT19))) != 0)) {
				{
				setState(423);
				switch (_input.LA(1)) {
				case T__4:
				case DIGIT19:
					{
					setState(420);
					digit();
					}
					break;
				case UNDERSCORE:
					{
					setState(421);
					match(UNDERSCORE);
					setState(422);
					digit();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(427);
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
		enterRule(_localctx, 88, RULE_exp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428);
			match(E);
			setState(429);
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
		enterRule(_localctx, 90, RULE_specialFloat);
		int _la;
		try {
			setState(436);
			switch (_input.LA(1)) {
			case HYPHEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(431);
				minus();
				}
				break;
			case PLUS:
			case INF:
			case NAN:
				enterOuterAlt(_localctx, 2);
				{
				setState(433);
				_la = _input.LA(1);
				if (_la==PLUS) {
					{
					setState(432);
					match(PLUS);
					}
				}

				setState(435);
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
		enterRule(_localctx, 92, RULE_bool);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
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
		enterRule(_localctx, 94, RULE_dateTime);
		try {
			setState(444);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(440);
				offsetDateTime();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(441);
				localDateTime();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(442);
				localDate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(443);
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 96, RULE_dateFullyear);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(446);
			digit();
			setState(447);
			digit();
			setState(448);
			digit();
			setState(449);
			digit();
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 98, RULE_dateMonth);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			digit();
			setState(452);
			digit();
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 100, RULE_dateMday);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(454);
			digit();
			setState(455);
			digit();
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
		enterRule(_localctx, 102, RULE_timeDelim);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(457);
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 104, RULE_timeHour);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(459);
			digit();
			setState(460);
			digit();
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 106, RULE_timeMinute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(462);
			digit();
			setState(463);
			digit();
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
		public List<DigitContext> digit() {
			return getRuleContexts(DigitContext.class);
		}
		public DigitContext digit(int i) {
			return getRuleContext(DigitContext.class,i);
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
		enterRule(_localctx, 108, RULE_timeSecond);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(465);
			digit();
			setState(466);
			digit();
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
		public DigitContext digit() {
			return getRuleContext(DigitContext.class,0);
		}
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
		enterRule(_localctx, 110, RULE_timeSecfrac);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(468);
			match(PERIOD);
			setState(469);
			digit();
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
		enterRule(_localctx, 112, RULE_timeNumoffset);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(471);
			_la = _input.LA(1);
			if ( !(_la==HYPHEN || _la==PLUS) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(472);
			timeHour();
			setState(473);
			match(COLON);
			setState(474);
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
		enterRule(_localctx, 114, RULE_timeOffset);
		try {
			setState(478);
			switch (_input.LA(1)) {
			case UPPERCASE_Z:
				enterOuterAlt(_localctx, 1);
				{
				setState(476);
				match(UPPERCASE_Z);
				}
				break;
			case HYPHEN:
			case PLUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(477);
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
		enterRule(_localctx, 116, RULE_partialTime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(480);
			timeHour();
			setState(481);
			match(COLON);
			setState(482);
			timeMinute();
			setState(483);
			match(COLON);
			setState(484);
			timeSecond();
			setState(486);
			_la = _input.LA(1);
			if (_la==PERIOD) {
				{
				setState(485);
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
		enterRule(_localctx, 118, RULE_fullDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			dateFullyear();
			setState(489);
			match(HYPHEN);
			setState(490);
			dateMonth();
			setState(491);
			match(HYPHEN);
			setState(492);
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
		enterRule(_localctx, 120, RULE_fullTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(494);
			partialTime();
			setState(495);
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
		enterRule(_localctx, 122, RULE_offsetDateTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			fullDate();
			setState(498);
			timeDelim();
			setState(499);
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
		enterRule(_localctx, 124, RULE_localDateTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(501);
			fullDate();
			setState(502);
			timeDelim();
			setState(503);
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
		enterRule(_localctx, 126, RULE_localDate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(505);
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
		enterRule(_localctx, 128, RULE_localTime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(507);
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
		enterRule(_localctx, 130, RULE_array);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(509);
			arrayOpen();
			setState(511);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				{
				setState(510);
				arrayValues();
				}
				break;
			}
			setState(513);
			ws();
			setState(514);
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
		enterRule(_localctx, 132, RULE_arrayOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(LEFT_BRACKET);
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
		enterRule(_localctx, 134, RULE_arrayClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(518);
			match(RIGHT_BRACKET);
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
		enterRule(_localctx, 136, RULE_arrayValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(520);
			ws();
			setState(521);
			arrayvalsNonEmpty();
			setState(528);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(522);
				arraySep();
				setState(523);
				ws();
				setState(524);
				arrayvalsNonEmpty();
				}
				}
				setState(530);
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
		enterRule(_localctx, 138, RULE_arrayvalsNonEmpty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(531);
			val();
			setState(532);
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
		enterRule(_localctx, 140, RULE_arraySep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
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
		enterRule(_localctx, 142, RULE_inlineTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			inlineTableOpen();
			setState(538);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(537);
				inlineTableKeyvals();
				}
				break;
			}
			setState(540);
			ws();
			setState(541);
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
		enterRule(_localctx, 144, RULE_inlineTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			match(LEFT_BRACE);
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
		enterRule(_localctx, 146, RULE_inlineTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(545);
			match(RIGHT_BRACE);
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
		enterRule(_localctx, 148, RULE_inlineTableSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
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
		enterRule(_localctx, 150, RULE_inlineTableKeyvals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549);
			ws();
			setState(550);
			inlineTableKeyvalsNonEmpty();
			setState(557);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(551);
				inlineTableSep();
				setState(552);
				ws();
				setState(553);
				inlineTableKeyvalsNonEmpty();
				}
				}
				setState(559);
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
		public KeyValSepContext keyValSep() {
			return getRuleContext(KeyValSepContext.class,0);
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
		enterRule(_localctx, 152, RULE_inlineTableKeyvalsNonEmpty);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(560);
			key();
			setState(561);
			keyValSep();
			setState(562);
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

	public static class TableContext extends ParserRuleContext {
		public StdTableContext stdTable() {
			return getRuleContext(StdTableContext.class,0);
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
		enterRule(_localctx, 154, RULE_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(564);
			stdTable();
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
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public StdTableCloseContext stdTableClose() {
			return getRuleContext(StdTableCloseContext.class,0);
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
		enterRule(_localctx, 156, RULE_stdTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
			stdTableOpen();
			setState(567);
			key();
			setState(568);
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
		enterRule(_localctx, 158, RULE_stdTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(570);
			match(LEFT_BRACKET);
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
		enterRule(_localctx, 160, RULE_stdTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(572);
			match(RIGHT_BRACKET);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\65\u0241\4\2\t\2"+
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
		"\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\5\t\u00cb\n\t\3\n\3\n\5\n\u00cf\n\n"+
		"\3\13\3\13\3\13\3\13\7\13\u00d5\n\13\f\13\16\13\u00d8\13\13\3\f\3\f\5"+
		"\f\u00dc\n\f\3\r\3\r\3\r\3\r\7\r\u00e2\n\r\f\r\16\r\u00e5\13\r\3\16\3"+
		"\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5"+
		"\20\u00f6\n\20\3\21\3\21\3\21\3\21\5\21\u00fc\n\21\3\22\3\22\3\22\3\22"+
		"\3\23\7\23\u0103\n\23\f\23\16\23\u0106\13\23\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\5\24\u011b\n\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\7\32\u0131\n\32\f\32\16"+
		"\32\u0134\13\32\3\33\3\33\5\33\u0138\n\33\3\34\3\34\7\34\u013c\n\34\f"+
		"\34\16\34\u013f\13\34\3\34\3\34\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3\36"+
		"\3\37\3\37\7\37\u014d\n\37\f\37\16\37\u0150\13\37\3 \3 \3 \3 \5 \u0156"+
		"\n \3!\3!\3\"\3\"\3#\3#\3$\3$\5$\u0160\n$\3$\3$\3%\3%\3%\3%\3%\7%\u0169"+
		"\n%\f%\16%\u016c\13%\5%\u016e\n%\3&\3&\7&\u0172\n&\f&\16&\u0175\13&\3"+
		"&\3&\3&\5&\u017a\n&\3\'\3\'\7\'\u017e\n\'\f\'\16\'\u0181\13\'\3\'\3\'"+
		"\3\'\5\'\u0186\n\'\3(\3(\7(\u018a\n(\f(\16(\u018d\13(\3(\3(\3(\5(\u0192"+
		"\n(\3)\3)\3)\3)\5)\u0198\n)\5)\u019a\n)\3)\5)\u019d\n)\3*\3*\3+\3+\3+"+
		"\3,\3,\3-\3-\3-\3-\7-\u01aa\n-\f-\16-\u01ad\13-\3.\3.\3.\3/\3/\5/\u01b4"+
		"\n/\3/\5/\u01b7\n/\3\60\3\60\3\61\3\61\3\61\3\61\5\61\u01bf\n\61\3\62"+
		"\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\66\3\66"+
		"\3\66\3\67\3\67\3\67\38\38\38\39\39\39\3:\3:\3:\3:\3:\3;\3;\5;\u01e1\n"+
		";\3<\3<\3<\3<\3<\3<\5<\u01e9\n<\3=\3=\3=\3=\3=\3=\3>\3>\3>\3?\3?\3?\3"+
		"?\3@\3@\3@\3@\3A\3A\3B\3B\3C\3C\5C\u0202\nC\3C\3C\3C\3D\3D\3E\3E\3F\3"+
		"F\3F\3F\3F\3F\7F\u0211\nF\fF\16F\u0214\13F\3G\3G\3G\3H\3H\3I\3I\5I\u021d"+
		"\nI\3I\3I\3I\3J\3J\3K\3K\3L\3L\3M\3M\3M\3M\3M\3M\7M\u022e\nM\fM\16M\u0231"+
		"\13M\3N\3N\3N\3N\3O\3O\3P\3P\3P\3P\3Q\3Q\3R\3R\3R\2\2S\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bd"+
		"fhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092"+
		"\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\2\13\4\2\23\23,\64\4"+
		"\2\3\5\24\24\3\2\5\6\4\2\7\7$$\3\2\b\17\3\2./\3\2\60\61\4\2\24\24\62\63"+
		"\4\2\25\25))\u023c\2\u00a4\3\2\2\2\4\u00ad\3\2\2\2\6\u00b8\3\2\2\2\b\u00bd"+
		"\3\2\2\2\n\u00c0\3\2\2\2\f\u00c2\3\2\2\2\16\u00c4\3\2\2\2\20\u00ca\3\2"+
		"\2\2\22\u00ce\3\2\2\2\24\u00d6\3\2\2\2\26\u00db\3\2\2\2\30\u00dd\3\2\2"+
		"\2\32\u00e6\3\2\2\2\34\u00ea\3\2\2\2\36\u00f5\3\2\2\2 \u00fb\3\2\2\2\""+
		"\u00fd\3\2\2\2$\u0104\3\2\2\2&\u011a\3\2\2\2(\u011c\3\2\2\2*\u011e\3\2"+
		"\2\2,\u0120\3\2\2\2.\u0122\3\2\2\2\60\u0126\3\2\2\2\62\u0132\3\2\2\2\64"+
		"\u0137\3\2\2\2\66\u0139\3\2\2\28\u0142\3\2\2\2:\u0146\3\2\2\2<\u014e\3"+
		"\2\2\2>\u0155\3\2\2\2@\u0157\3\2\2\2B\u0159\3\2\2\2D\u015b\3\2\2\2F\u015f"+
		"\3\2\2\2H\u016d\3\2\2\2J\u016f\3\2\2\2L\u017b\3\2\2\2N\u0187\3\2\2\2P"+
		"\u019c\3\2\2\2R\u019e\3\2\2\2T\u01a0\3\2\2\2V\u01a3\3\2\2\2X\u01a5\3\2"+
		"\2\2Z\u01ae\3\2\2\2\\\u01b6\3\2\2\2^\u01b8\3\2\2\2`\u01be\3\2\2\2b\u01c0"+
		"\3\2\2\2d\u01c5\3\2\2\2f\u01c8\3\2\2\2h\u01cb\3\2\2\2j\u01cd\3\2\2\2l"+
		"\u01d0\3\2\2\2n\u01d3\3\2\2\2p\u01d6\3\2\2\2r\u01d9\3\2\2\2t\u01e0\3\2"+
		"\2\2v\u01e2\3\2\2\2x\u01ea\3\2\2\2z\u01f0\3\2\2\2|\u01f3\3\2\2\2~\u01f7"+
		"\3\2\2\2\u0080\u01fb\3\2\2\2\u0082\u01fd\3\2\2\2\u0084\u01ff\3\2\2\2\u0086"+
		"\u0206\3\2\2\2\u0088\u0208\3\2\2\2\u008a\u020a\3\2\2\2\u008c\u0215\3\2"+
		"\2\2\u008e\u0218\3\2\2\2\u0090\u021a\3\2\2\2\u0092\u0221\3\2\2\2\u0094"+
		"\u0223\3\2\2\2\u0096\u0225\3\2\2\2\u0098\u0227\3\2\2\2\u009a\u0232\3\2"+
		"\2\2\u009c\u0236\3\2\2\2\u009e\u0238\3\2\2\2\u00a0\u023c\3\2\2\2\u00a2"+
		"\u023e\3\2\2\2\u00a4\u00aa\5\6\4\2\u00a5\u00a6\5\f\7\2\u00a6\u00a7\5\6"+
		"\4\2\u00a7\u00a9\3\2\2\2\u00a8\u00a5\3\2\2\2\u00a9\u00ac\3\2\2\2\u00aa"+
		"\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\3\3\2\2\2\u00ac\u00aa\3\2\2\2"+
		"\u00ad\u00ae\t\2\2\2\u00ae\5\3\2\2\2\u00af\u00b9\5\b\5\2\u00b0\u00b1\5"+
		"\b\5\2\u00b1\u00b2\5\16\b\2\u00b2\u00b3\5\b\5\2\u00b3\u00b9\3\2\2\2\u00b4"+
		"\u00b5\5\b\5\2\u00b5\u00b6\5\u009cO\2\u00b6\u00b7\5\b\5\2\u00b7\u00b9"+
		"\3\2\2\2\u00b8\u00af\3\2\2\2\u00b8\u00b0\3\2\2\2\u00b8\u00b4\3\2\2\2\u00b9"+
		"\7\3\2\2\2\u00ba\u00bc\5\n\6\2\u00bb\u00ba\3\2\2\2\u00bc\u00bf\3\2\2\2"+
		"\u00bd\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\t\3\2\2\2\u00bf\u00bd\3"+
		"\2\2\2\u00c0\u00c1\t\3\2\2\u00c1\13\3\2\2\2\u00c2\u00c3\t\4\2\2\u00c3"+
		"\r\3\2\2\2\u00c4\u00c5\5\20\t\2\u00c5\u00c6\5\32\16\2\u00c6\u00c7\5\36"+
		"\20\2\u00c7\17\3\2\2\2\u00c8\u00cb\5\30\r\2\u00c9\u00cb\5\22\n\2\u00ca"+
		"\u00c8\3\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\21\3\2\2\2\u00cc\u00cf\5\24\13"+
		"\2\u00cd\u00cf\5\26\f\2\u00ce\u00cc\3\2\2\2\u00ce\u00cd\3\2\2\2\u00cf"+
		"\23\3\2\2\2\u00d0\u00d5\5\4\3\2\u00d1\u00d5\5(\25\2\u00d2\u00d5\7\25\2"+
		"\2\u00d3\u00d5\7\30\2\2\u00d4\u00d0\3\2\2\2\u00d4\u00d1\3\2\2\2\u00d4"+
		"\u00d2\3\2\2\2\u00d4\u00d3\3\2\2\2\u00d5\u00d8\3\2\2\2\u00d6\u00d4\3\2"+
		"\2\2\u00d6\u00d7\3\2\2\2\u00d7\25\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d9\u00dc"+
		"\5\"\22\2\u00da\u00dc\5\66\34\2\u00db\u00d9\3\2\2\2\u00db\u00da\3\2\2"+
		"\2\u00dc\27\3\2\2\2\u00dd\u00e3\5\22\n\2\u00de\u00df\5\34\17\2\u00df\u00e0"+
		"\5\22\n\2\u00e0\u00e2\3\2\2\2\u00e1\u00de\3\2\2\2\u00e2\u00e5\3\2\2\2"+
		"\u00e3\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\31\3\2\2\2\u00e5\u00e3"+
		"\3\2\2\2\u00e6\u00e7\5\b\5\2\u00e7\u00e8\7\35\2\2\u00e8\u00e9\5\b\5\2"+
		"\u00e9\33\3\2\2\2\u00ea\u00eb\5\b\5\2\u00eb\u00ec\7\26\2\2\u00ec\u00ed"+
		"\5\b\5\2\u00ed\35\3\2\2\2\u00ee\u00f6\5 \21\2\u00ef\u00f6\5^\60\2\u00f0"+
		"\u00f6\5\u0084C\2\u00f1\u00f6\5`\61\2\u00f2\u00f6\5P)\2\u00f3\u00f6\5"+
		"> \2\u00f4\u00f6\5\u0090I\2\u00f5\u00ee\3\2\2\2\u00f5\u00ef\3\2\2\2\u00f5"+
		"\u00f0\3\2\2\2\u00f5\u00f1\3\2\2\2\u00f5\u00f2\3\2\2\2\u00f5\u00f3\3\2"+
		"\2\2\u00f5\u00f4\3\2\2\2\u00f6\37\3\2\2\2\u00f7\u00fc\5.\30\2\u00f8\u00fc"+
		"\5\"\22\2\u00f9\u00fc\58\35\2\u00fa\u00fc\5\66\34\2\u00fb\u00f7\3\2\2"+
		"\2\u00fb\u00f8\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fb\u00fa\3\2\2\2\u00fc!"+
		"\3\2\2\2\u00fd\u00fe\7\27\2\2\u00fe\u00ff\5$\23\2\u00ff\u0100\7\27\2\2"+
		"\u0100#\3\2\2\2\u0101\u0103\5&\24\2\u0102\u0101\3\2\2\2\u0103\u0106\3"+
		"\2\2\2\u0104\u0102\3\2\2\2\u0104\u0105\3\2\2\2\u0105%\3\2\2\2\u0106\u0104"+
		"\3\2\2\2\u0107\u011b\5*\26\2\u0108\u011b\5\4\3\2\u0109\u011b\7%\2\2\u010a"+
		"\u011b\7\24\2\2\u010b\u011b\7)\2\2\u010c\u011b\7\25\2\2\u010d\u011b\7"+
		"\26\2\2\u010e\u011b\7\30\2\2\u010f\u011b\7\31\2\2\u0110\u011b\7\32\2\2"+
		"\u0111\u011b\7\33\2\2\u0112\u011b\7\34\2\2\u0113\u011b\7\35\2\2\u0114"+
		"\u011b\7\36\2\2\u0115\u011b\7\37\2\2\u0116\u011b\7 \2\2\u0117\u011b\7"+
		"!\2\2\u0118\u011b\7\"\2\2\u0119\u011b\5(\25\2\u011a\u0107\3\2\2\2\u011a"+
		"\u0108\3\2\2\2\u011a\u0109\3\2\2\2\u011a\u010a\3\2\2\2\u011a\u010b\3\2"+
		"\2\2\u011a\u010c\3\2\2\2\u011a\u010d\3\2\2\2\u011a\u010e\3\2\2\2\u011a"+
		"\u010f\3\2\2\2\u011a\u0110\3\2\2\2\u011a\u0111\3\2\2\2\u011a\u0112\3\2"+
		"\2\2\u011a\u0113\3\2\2\2\u011a\u0114\3\2\2\2\u011a\u0115\3\2\2\2\u011a"+
		"\u0116\3\2\2\2\u011a\u0117\3\2\2\2\u011a\u0118\3\2\2\2\u011a\u0119\3\2"+
		"\2\2\u011b\'\3\2\2\2\u011c\u011d\t\5\2\2\u011d)\3\2\2\2\u011e\u011f\5"+
		",\27\2\u011f+\3\2\2\2\u0120\u0121\t\6\2\2\u0121-\3\2\2\2\u0122\u0123\5"+
		"\60\31\2\u0123\u0124\5\62\32\2\u0124\u0125\5\60\31\2\u0125/\3\2\2\2\u0126"+
		"\u0127\7\27\2\2\u0127\u0128\7\27\2\2\u0128\u0129\7\27\2\2\u0129\61\3\2"+
		"\2\2\u012a\u0131\5\64\33\2\u012b\u0131\5\f\7\2\u012c\u012d\7\20\2\2\u012d"+
		"\u012e\5\b\5\2\u012e\u012f\5\f\7\2\u012f\u0131\3\2\2\2\u0130\u012a\3\2"+
		"\2\2\u0130\u012b\3\2\2\2\u0130\u012c\3\2\2\2\u0131\u0134\3\2\2\2\u0132"+
		"\u0130\3\2\2\2\u0132\u0133\3\2\2\2\u0133\63\3\2\2\2\u0134\u0132\3\2\2"+
		"\2\u0135\u0138\7&\2\2\u0136\u0138\5*\26\2\u0137\u0135\3\2\2\2\u0137\u0136"+
		"\3\2\2\2\u0138\65\3\2\2\2\u0139\u013d\7\34\2\2\u013a\u013c\7\'\2\2\u013b"+
		"\u013a\3\2\2\2\u013c\u013f\3\2\2\2\u013d\u013b\3\2\2\2\u013d\u013e\3\2"+
		"\2\2\u013e\u0140\3\2\2\2\u013f\u013d\3\2\2\2\u0140\u0141\7\34\2\2\u0141"+
		"\67\3\2\2\2\u0142\u0143\5:\36\2\u0143\u0144\5<\37\2\u0144\u0145\5:\36"+
		"\2\u01459\3\2\2\2\u0146\u0147\7\34\2\2\u0147\u0148\7\34\2\2\u0148\u0149"+
		"\7\34\2\2\u0149;\3\2\2\2\u014a\u014d\7(\2\2\u014b\u014d\5\f\7\2\u014c"+
		"\u014a\3\2\2\2\u014c\u014b\3\2\2\2\u014d\u0150\3\2\2\2\u014e\u014c\3\2"+
		"\2\2\u014e\u014f\3\2\2\2\u014f=\3\2\2\2\u0150\u014e\3\2\2\2\u0151\u0156"+
		"\5F$\2\u0152\u0156\5J&\2\u0153\u0156\5L\'\2\u0154\u0156\5N(\2\u0155\u0151"+
		"\3\2\2\2\u0155\u0152\3\2\2\2\u0155\u0153\3\2\2\2\u0155\u0154\3\2\2\2\u0156"+
		"?\3\2\2\2\u0157\u0158\7\25\2\2\u0158A\3\2\2\2\u0159\u015a\7\21\2\2\u015a"+
		"C\3\2\2\2\u015b\u015c\7\22\2\2\u015cE\3\2\2\2\u015d\u0160\5@!\2\u015e"+
		"\u0160\7)\2\2\u015f\u015d\3\2\2\2\u015f\u015e\3\2\2\2\u015f\u0160\3\2"+
		"\2\2\u0160\u0161\3\2\2\2\u0161\u0162\5H%\2\u0162G\3\2\2\2\u0163\u016e"+
		"\5(\25\2\u0164\u016a\7$\2\2\u0165\u0169\5(\25\2\u0166\u0167\7\30\2\2\u0167"+
		"\u0169\5(\25\2\u0168\u0165\3\2\2\2\u0168\u0166\3\2\2\2\u0169\u016c\3\2"+
		"\2\2\u016a\u0168\3\2\2\2\u016a\u016b\3\2\2\2\u016b\u016e\3\2\2\2\u016c"+
		"\u016a\3\2\2\2\u016d\u0163\3\2\2\2\u016d\u0164\3\2\2\2\u016eI\3\2\2\2"+
		"\u016f\u0173\5B\"\2\u0170\u0172\7\65\2\2\u0171\u0170\3\2\2\2\u0172\u0175"+
		"\3\2\2\2\u0173\u0171\3\2\2\2\u0173\u0174\3\2\2\2\u0174\u0179\3\2\2\2\u0175"+
		"\u0173\3\2\2\2\u0176\u017a\7\65\2\2\u0177\u0178\7\30\2\2\u0178\u017a\7"+
		"\65\2\2\u0179\u0176\3\2\2\2\u0179\u0177\3\2\2\2\u017aK\3\2\2\2\u017b\u017f"+
		"\5D#\2\u017c\u017e\7*\2\2\u017d\u017c\3\2\2\2\u017e\u0181\3\2\2\2\u017f"+
		"\u017d\3\2\2\2\u017f\u0180\3\2\2\2\u0180\u0185\3\2\2\2\u0181\u017f\3\2"+
		"\2\2\u0182\u0186\7*\2\2\u0183\u0184\7\30\2\2\u0184\u0186\7*\2\2\u0185"+
		"\u0182\3\2\2\2\u0185\u0183\3\2\2\2\u0186M\3\2\2\2\u0187\u018b\7,\2\2\u0188"+
		"\u018a\7+\2\2\u0189\u0188\3\2\2\2\u018a\u018d\3\2\2\2\u018b\u0189\3\2"+
		"\2\2\u018b\u018c\3\2\2\2\u018c\u0191\3\2\2\2\u018d\u018b\3\2\2\2\u018e"+
		"\u0192\7+\2\2\u018f\u0190\7\30\2\2\u0190\u0192\7+\2\2\u0191\u018e\3\2"+
		"\2\2\u0191\u018f\3\2\2\2\u0192O\3\2\2\2\u0193\u0199\5R*\2\u0194\u019a"+
		"\5Z.\2\u0195\u0197\5T+\2\u0196\u0198\5Z.\2\u0197\u0196\3\2\2\2\u0197\u0198"+
		"\3\2\2\2\u0198\u019a\3\2\2\2\u0199\u0194\3\2\2\2\u0199\u0195\3\2\2\2\u019a"+
		"\u019d\3\2\2\2\u019b\u019d\5\\/\2\u019c\u0193\3\2\2\2\u019c\u019b\3\2"+
		"\2\2\u019dQ\3\2\2\2\u019e\u019f\5F$\2\u019fS\3\2\2\2\u01a0\u01a1\5V,\2"+
		"\u01a1\u01a2\5X-\2\u01a2U\3\2\2\2\u01a3\u01a4\7\26\2\2\u01a4W\3\2\2\2"+
		"\u01a5\u01ab\5(\25\2\u01a6\u01aa\5(\25\2\u01a7\u01a8\7\30\2\2\u01a8\u01aa"+
		"\5(\25\2\u01a9\u01a6\3\2\2\2\u01a9\u01a7\3\2\2\2\u01aa\u01ad\3\2\2\2\u01ab"+
		"\u01a9\3\2\2\2\u01ab\u01ac\3\2\2\2\u01acY\3\2\2\2\u01ad\u01ab\3\2\2\2"+
		"\u01ae\u01af\7-\2\2\u01af\u01b0\5R*\2\u01b0[\3\2\2\2\u01b1\u01b7\5@!\2"+
		"\u01b2\u01b4\7)\2\2\u01b3\u01b2\3\2\2\2\u01b3\u01b4\3\2\2\2\u01b4\u01b5"+
		"\3\2\2\2\u01b5\u01b7\t\7\2\2\u01b6\u01b1\3\2\2\2\u01b6\u01b3\3\2\2\2\u01b7"+
		"]\3\2\2\2\u01b8\u01b9\t\b\2\2\u01b9_\3\2\2\2\u01ba\u01bf\5|?\2\u01bb\u01bf"+
		"\5~@\2\u01bc\u01bf\5\u0080A\2\u01bd\u01bf\5\u0082B\2\u01be\u01ba\3\2\2"+
		"\2\u01be\u01bb\3\2\2\2\u01be\u01bc\3\2\2\2\u01be\u01bd\3\2\2\2\u01bfa"+
		"\3\2\2\2\u01c0\u01c1\5(\25\2\u01c1\u01c2\5(\25\2\u01c2\u01c3\5(\25\2\u01c3"+
		"\u01c4\5(\25\2\u01c4c\3\2\2\2\u01c5\u01c6\5(\25\2\u01c6\u01c7\5(\25\2"+
		"\u01c7e\3\2\2\2\u01c8\u01c9\5(\25\2\u01c9\u01ca\5(\25\2\u01cag\3\2\2\2"+
		"\u01cb\u01cc\t\t\2\2\u01cci\3\2\2\2\u01cd\u01ce\5(\25\2\u01ce\u01cf\5"+
		"(\25\2\u01cfk\3\2\2\2\u01d0\u01d1\5(\25\2\u01d1\u01d2\5(\25\2\u01d2m\3"+
		"\2\2\2\u01d3\u01d4\5(\25\2\u01d4\u01d5\5(\25\2\u01d5o\3\2\2\2\u01d6\u01d7"+
		"\7\26\2\2\u01d7\u01d8\5(\25\2\u01d8q\3\2\2\2\u01d9\u01da\t\n\2\2\u01da"+
		"\u01db\5j\66\2\u01db\u01dc\7\31\2\2\u01dc\u01dd\5l\67\2\u01dds\3\2\2\2"+
		"\u01de\u01e1\7\64\2\2\u01df\u01e1\5r:\2\u01e0\u01de\3\2\2\2\u01e0\u01df"+
		"\3\2\2\2\u01e1u\3\2\2\2\u01e2\u01e3\5j\66\2\u01e3\u01e4\7\31\2\2\u01e4"+
		"\u01e5\5l\67\2\u01e5\u01e6\7\31\2\2\u01e6\u01e8\5n8\2\u01e7\u01e9\5p9"+
		"\2\u01e8\u01e7\3\2\2\2\u01e8\u01e9\3\2\2\2\u01e9w\3\2\2\2\u01ea\u01eb"+
		"\5b\62\2\u01eb\u01ec\7\25\2\2\u01ec\u01ed\5d\63\2\u01ed\u01ee\7\25\2\2"+
		"\u01ee\u01ef\5f\64\2\u01efy\3\2\2\2\u01f0\u01f1\5v<\2\u01f1\u01f2\5t;"+
		"\2\u01f2{\3\2\2\2\u01f3\u01f4\5x=\2\u01f4\u01f5\5h\65\2\u01f5\u01f6\5"+
		"z>\2\u01f6}\3\2\2\2\u01f7\u01f8\5x=\2\u01f8\u01f9\5h\65\2\u01f9\u01fa"+
		"\5v<\2\u01fa\177\3\2\2\2\u01fb\u01fc\5x=\2\u01fc\u0081\3\2\2\2\u01fd\u01fe"+
		"\5v<\2\u01fe\u0083\3\2\2\2\u01ff\u0201\5\u0086D\2\u0200\u0202\5\u008a"+
		"F\2\u0201\u0200\3\2\2\2\u0201\u0202\3\2\2\2\u0202\u0203\3\2\2\2\u0203"+
		"\u0204\5\b\5\2\u0204\u0205\5\u0088E\2\u0205\u0085\3\2\2\2\u0206\u0207"+
		"\7\37\2\2\u0207\u0087\3\2\2\2\u0208\u0209\7 \2\2\u0209\u0089\3\2\2\2\u020a"+
		"\u020b\5\b\5\2\u020b\u0212\5\u008cG\2\u020c\u020d\5\u008eH\2\u020d\u020e"+
		"\5\b\5\2\u020e\u020f\5\u008cG\2\u020f\u0211\3\2\2\2\u0210\u020c\3\2\2"+
		"\2\u0211\u0214\3\2\2\2\u0212\u0210\3\2\2\2\u0212\u0213\3\2\2\2\u0213\u008b"+
		"\3\2\2\2\u0214\u0212\3\2\2\2\u0215\u0216\5\36\20\2\u0216\u0217\5\b\5\2"+
		"\u0217\u008d\3\2\2\2\u0218\u0219\7\32\2\2\u0219\u008f\3\2\2\2\u021a\u021c"+
		"\5\u0092J\2\u021b\u021d\5\u0098M\2\u021c\u021b\3\2\2\2\u021c\u021d\3\2"+
		"\2\2\u021d\u021e\3\2\2\2\u021e\u021f\5\b\5\2\u021f\u0220\5\u0094K\2\u0220"+
		"\u0091\3\2\2\2\u0221\u0222\7!\2\2\u0222\u0093\3\2\2\2\u0223\u0224\7\""+
		"\2\2\u0224\u0095\3\2\2\2\u0225\u0226\7\32\2\2\u0226\u0097\3\2\2\2\u0227"+
		"\u0228\5\b\5\2\u0228\u022f\5\u009aN\2\u0229\u022a\5\u0096L\2\u022a\u022b"+
		"\5\b\5\2\u022b\u022c\5\u009aN\2\u022c\u022e\3\2\2\2\u022d\u0229\3\2\2"+
		"\2\u022e\u0231\3\2\2\2\u022f\u022d\3\2\2\2\u022f\u0230\3\2\2\2\u0230\u0099"+
		"\3\2\2\2\u0231\u022f\3\2\2\2\u0232\u0233\5\20\t\2\u0233\u0234\5\32\16"+
		"\2\u0234\u0235\5\36\20\2\u0235\u009b\3\2\2\2\u0236\u0237\5\u009eP\2\u0237"+
		"\u009d\3\2\2\2\u0238\u0239\5\u00a0Q\2\u0239\u023a\5\20\t\2\u023a\u023b"+
		"\5\u00a2R\2\u023b\u009f\3\2\2\2\u023c\u023d\7\37\2\2\u023d\u00a1\3\2\2"+
		"\2\u023e\u023f\7 \2\2\u023f\u00a3\3\2\2\2.\u00aa\u00b8\u00bd\u00ca\u00ce"+
		"\u00d4\u00d6\u00db\u00e3\u00f5\u00fb\u0104\u011a\u0130\u0132\u0137\u013d"+
		"\u014c\u014e\u0155\u015f\u0168\u016a\u016d\u0173\u0179\u017f\u0185\u018b"+
		"\u0191\u0197\u0199\u019c\u01a9\u01ab\u01b3\u01b6\u01be\u01e0\u01e8\u0201"+
		"\u0212\u021c\u022f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}