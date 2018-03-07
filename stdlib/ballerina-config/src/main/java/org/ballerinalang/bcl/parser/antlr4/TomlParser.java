// Generated from Toml.g4 by ANTLR 4.5.3
package org.ballerinalang.bcl.parser.antlr4;
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
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		ALPHA=18, SPACE=19, HYPHEN=20, PERIOD=21, QUOTATION_MARK=22, UNDERSCORE=23, 
		COLON=24, COMMA=25, SLASH=26, APOSTROPHE=27, EQUALS=28, HASH=29, LEFT_BRACKET=30, 
		RIGHT_BRACKET=31, COMMENT=32, DIGIT19=33, BASICUNESCAPED=34, MLBASICUNESCAPED=35, 
		LITERALCHAR=36, MLLITERALCHAR=37, PLUS=38, DIGIT07=39, DIGIT01=40, E=41, 
		INF=42, NAN=43, TRUE=44, FALSE=45, UPPERCASE_T=46, LOWERCASE_T=47, UPPERCASE_Z=48, 
		HEXDIG=49;
	public static final int
		RULE_toml = 0, RULE_alpha = 1, RULE_expression = 2, RULE_ws = 3, RULE_wschar = 4, 
		RULE_newline = 5, RULE_keyval = 6, RULE_key = 7, RULE_simpleKey = 8, RULE_unquotedKey = 9, 
		RULE_quotedKey = 10, RULE_dottedKey = 11, RULE_keyvalSep = 12, RULE_dotSep = 13, 
		RULE_val = 14, RULE_string = 15, RULE_basicString = 16, RULE_basicChar = 17, 
		RULE_digit = 18, RULE_escaped = 19, RULE_escapeSeqChar = 20, RULE_mlBasicString = 21, 
		RULE_mlBasicStringDelim = 22, RULE_mlBasicBody = 23, RULE_mlBasicChar = 24, 
		RULE_literalString = 25, RULE_mlLiteralString = 26, RULE_mlLiteralStringDelim = 27, 
		RULE_mlLiteralBody = 28, RULE_integer = 29, RULE_minus = 30, RULE_hexPrefix = 31, 
		RULE_octPrefix = 32, RULE_binPrefix = 33, RULE_decInt = 34, RULE_unsignedDecInt = 35, 
		RULE_hexInt = 36, RULE_octInt = 37, RULE_binInt = 38, RULE_floatingPoint = 39, 
		RULE_floatIntPart = 40, RULE_frac = 41, RULE_decimalPoint = 42, RULE_zeroPrefixableInt = 43, 
		RULE_exp = 44, RULE_specialFloat = 45, RULE_bool = 46, RULE_dateTime = 47, 
		RULE_dateFullyear = 48, RULE_dateMonth = 49, RULE_dateMday = 50, RULE_timeDelim = 51, 
		RULE_timeHour = 52, RULE_timeMinute = 53, RULE_timeSecond = 54, RULE_timeSecfrac = 55, 
		RULE_timeNumoffset = 56, RULE_timeOffset = 57, RULE_partialTime = 58, 
		RULE_fullDate = 59, RULE_fullTime = 60, RULE_offsetDateTime = 61, RULE_localDateTime = 62, 
		RULE_localDate = 63, RULE_localTime = 64, RULE_array = 65, RULE_arrayOpen = 66, 
		RULE_arrayClose = 67, RULE_arrayValues = 68, RULE_arrayvalsNonEmpty = 69, 
		RULE_arraySep = 70, RULE_table = 71, RULE_stdTable = 72, RULE_stdTableOpen = 73, 
		RULE_stdTableClose = 74;
	public static final String[] ruleNames = {
		"toml", "alpha", "expression", "ws", "wschar", "newline", "keyval", "key", 
		"simpleKey", "unquotedKey", "quotedKey", "dottedKey", "keyvalSep", "dotSep", 
		"val", "string", "basicString", "basicChar", "digit", "escaped", "escapeSeqChar", 
		"mlBasicString", "mlBasicStringDelim", "mlBasicBody", "mlBasicChar", "literalString", 
		"mlLiteralString", "mlLiteralStringDelim", "mlLiteralBody", "integer", 
		"minus", "hexPrefix", "octPrefix", "binPrefix", "decInt", "unsignedDecInt", 
		"hexInt", "octInt", "binInt", "floatingPoint", "floatIntPart", "frac", 
		"decimalPoint", "zeroPrefixableInt", "exp", "specialFloat", "bool", "dateTime", 
		"dateFullyear", "dateMonth", "dateMday", "timeDelim", "timeHour", "timeMinute", 
		"timeSecond", "timeSecfrac", "timeNumoffset", "timeOffset", "partialTime", 
		"fullDate", "fullTime", "offsetDateTime", "localDateTime", "localDate", 
		"localTime", "array", "arrayOpen", "arrayClose", "arrayValues", "arrayvalsNonEmpty", 
		"arraySep", "table", "stdTable", "stdTableOpen", "stdTableClose"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\t'", "'\r'", "'\n'", "'\r\n'", "'0'", "'\\\"'", "'\\\\'", "'\\/'", 
		"'\\b'", "'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\'", "'0x'", "'0o'", "'0b'", 
		null, "' '", "'-'", "'.'", "'\"'", "'_'", "':'", "','", "'/'", "'''", 
		"'='", "'#'", "'['", "']'", null, null, null, null, null, null, "'+'", 
		null, null, "'e'", "'inf'", "'nan'", "'true'", "'false'", "'T'", "'t'", 
		"'Z'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, "ALPHA", "SPACE", "HYPHEN", "PERIOD", 
		"QUOTATION_MARK", "UNDERSCORE", "COLON", "COMMA", "SLASH", "APOSTROPHE", 
		"EQUALS", "HASH", "LEFT_BRACKET", "RIGHT_BRACKET", "COMMENT", "DIGIT19", 
		"BASICUNESCAPED", "MLBASICUNESCAPED", "LITERALCHAR", "MLLITERALCHAR", 
		"PLUS", "DIGIT07", "DIGIT01", "E", "INF", "NAN", "TRUE", "FALSE", "UPPERCASE_T", 
		"LOWERCASE_T", "UPPERCASE_Z", "HEXDIG"
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
			setState(150);
			expression();
			setState(156);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2 || _la==T__3) {
				{
				{
				setState(151);
				newline();
				setState(152);
				expression();
				}
				}
				setState(158);
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
			setState(159);
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
			setState(170);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(161);
				ws();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(162);
				ws();
				setState(163);
				keyval();
				setState(164);
				ws();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(166);
				ws();
				setState(167);
				table();
				setState(168);
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
			setState(175);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(172);
					wschar();
					}
					} 
				}
				setState(177);
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
			setState(178);
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
			setState(180);
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
			setState(182);
			key();
			setState(183);
			keyvalSep();
			setState(184);
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
			setState(188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(186);
				dottedKey();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(187);
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
			setState(192);
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
				setState(190);
				unquotedKey();
				}
				break;
			case QUOTATION_MARK:
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
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
			setState(200);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << ALPHA) | (1L << HYPHEN) | (1L << UNDERSCORE) | (1L << DIGIT19) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) {
				{
				setState(198);
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
					setState(194);
					alpha();
					}
					break;
				case T__4:
				case DIGIT19:
					{
					setState(195);
					digit();
					}
					break;
				case HYPHEN:
					{
					setState(196);
					match(HYPHEN);
					}
					break;
				case UNDERSCORE:
					{
					setState(197);
					match(UNDERSCORE);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(202);
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
			setState(205);
			switch (_input.LA(1)) {
			case QUOTATION_MARK:
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				basicString();
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 2);
				{
				setState(204);
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
			setState(207);
			simpleKey();
			setState(213);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(208);
					dotSep();
					setState(209);
					simpleKey();
					}
					} 
				}
				setState(215);
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
		enterRule(_localctx, 24, RULE_keyvalSep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
			ws();
			setState(217);
			match(EQUALS);
			setState(218);
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
			setState(220);
			ws();
			setState(221);
			match(PERIOD);
			setState(222);
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
			setState(230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(224);
				string();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(225);
				bool();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(226);
				array();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(227);
				dateTime();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(228);
				floatingPoint();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(229);
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
		enterRule(_localctx, 30, RULE_string);
		try {
			setState(236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(232);
				mlBasicString();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(233);
				basicString();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(234);
				mlLiteralString();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(235);
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
		enterRule(_localctx, 32, RULE_basicString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			match(QUOTATION_MARK);
			setState(242);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << ALPHA) | (1L << SPACE) | (1L << HYPHEN) | (1L << PERIOD) | (1L << UNDERSCORE) | (1L << COLON) | (1L << COMMA) | (1L << SLASH) | (1L << APOSTROPHE) | (1L << EQUALS) | (1L << HASH) | (1L << LEFT_BRACKET) | (1L << RIGHT_BRACKET) | (1L << DIGIT19) | (1L << BASICUNESCAPED) | (1L << PLUS) | (1L << E) | (1L << INF) | (1L << NAN) | (1L << TRUE) | (1L << FALSE) | (1L << UPPERCASE_T) | (1L << LOWERCASE_T) | (1L << UPPERCASE_Z))) != 0)) {
				{
				{
				setState(239);
				basicChar();
				}
				}
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(245);
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
		enterRule(_localctx, 34, RULE_basicChar);
		try {
			setState(264);
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
				setState(247);
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
				setState(248);
				alpha();
				}
				break;
			case BASICUNESCAPED:
				enterOuterAlt(_localctx, 3);
				{
				setState(249);
				match(BASICUNESCAPED);
				}
				break;
			case SPACE:
				enterOuterAlt(_localctx, 4);
				{
				setState(250);
				match(SPACE);
				}
				break;
			case PLUS:
				enterOuterAlt(_localctx, 5);
				{
				setState(251);
				match(PLUS);
				}
				break;
			case HYPHEN:
				enterOuterAlt(_localctx, 6);
				{
				setState(252);
				match(HYPHEN);
				}
				break;
			case PERIOD:
				enterOuterAlt(_localctx, 7);
				{
				setState(253);
				match(PERIOD);
				}
				break;
			case UNDERSCORE:
				enterOuterAlt(_localctx, 8);
				{
				setState(254);
				match(UNDERSCORE);
				}
				break;
			case COLON:
				enterOuterAlt(_localctx, 9);
				{
				setState(255);
				match(COLON);
				}
				break;
			case COMMA:
				enterOuterAlt(_localctx, 10);
				{
				setState(256);
				match(COMMA);
				}
				break;
			case SLASH:
				enterOuterAlt(_localctx, 11);
				{
				setState(257);
				match(SLASH);
				}
				break;
			case APOSTROPHE:
				enterOuterAlt(_localctx, 12);
				{
				setState(258);
				match(APOSTROPHE);
				}
				break;
			case EQUALS:
				enterOuterAlt(_localctx, 13);
				{
				setState(259);
				match(EQUALS);
				}
				break;
			case HASH:
				enterOuterAlt(_localctx, 14);
				{
				setState(260);
				match(HASH);
				}
				break;
			case LEFT_BRACKET:
				enterOuterAlt(_localctx, 15);
				{
				setState(261);
				match(LEFT_BRACKET);
				}
				break;
			case RIGHT_BRACKET:
				enterOuterAlt(_localctx, 16);
				{
				setState(262);
				match(RIGHT_BRACKET);
				}
				break;
			case T__4:
			case DIGIT19:
				enterOuterAlt(_localctx, 17);
				{
				setState(263);
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
		enterRule(_localctx, 36, RULE_digit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
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
		enterRule(_localctx, 38, RULE_escaped);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
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
		enterRule(_localctx, 40, RULE_escapeSeqChar);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(270);
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
		enterRule(_localctx, 42, RULE_mlBasicString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			mlBasicStringDelim();
			setState(273);
			mlBasicBody();
			setState(274);
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
		enterRule(_localctx, 44, RULE_mlBasicStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276);
			match(QUOTATION_MARK);
			setState(277);
			match(QUOTATION_MARK);
			setState(278);
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
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<NewlineContext> newline() {
			return getRuleContexts(NewlineContext.class);
		}
		public NewlineContext newline(int i) {
			return getRuleContext(NewlineContext.class,i);
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
		enterRule(_localctx, 46, RULE_mlBasicBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(287);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__13 || _la==MLBASICUNESCAPED) {
				{
				setState(285);
				switch (_input.LA(1)) {
				case MLBASICUNESCAPED:
					{
					setState(280);
					mlBasicChar();
					}
					break;
				case T__13:
					{
					{
					setState(281);
					match(T__13);
					setState(282);
					ws();
					setState(283);
					newline();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(289);
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
		enterRule(_localctx, 48, RULE_mlBasicChar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(290);
			match(MLBASICUNESCAPED);
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
		enterRule(_localctx, 50, RULE_literalString);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			match(APOSTROPHE);
			setState(296);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LITERALCHAR) {
				{
				{
				setState(293);
				match(LITERALCHAR);
				}
				}
				setState(298);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(299);
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
		enterRule(_localctx, 52, RULE_mlLiteralString);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
			mlLiteralStringDelim();
			setState(302);
			mlLiteralBody();
			setState(303);
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
		enterRule(_localctx, 54, RULE_mlLiteralStringDelim);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			match(APOSTROPHE);
			setState(306);
			match(APOSTROPHE);
			setState(307);
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
		enterRule(_localctx, 56, RULE_mlLiteralBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << MLLITERALCHAR))) != 0)) {
				{
				setState(311);
				switch (_input.LA(1)) {
				case MLLITERALCHAR:
					{
					setState(309);
					match(MLLITERALCHAR);
					}
					break;
				case T__2:
				case T__3:
					{
					setState(310);
					newline();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(315);
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
		enterRule(_localctx, 58, RULE_integer);
		try {
			setState(320);
			switch (_input.LA(1)) {
			case T__4:
			case HYPHEN:
			case DIGIT19:
			case PLUS:
				enterOuterAlt(_localctx, 1);
				{
				setState(316);
				decInt();
				}
				break;
			case T__14:
				enterOuterAlt(_localctx, 2);
				{
				setState(317);
				hexInt();
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 3);
				{
				setState(318);
				octInt();
				}
				break;
			case T__16:
				enterOuterAlt(_localctx, 4);
				{
				setState(319);
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
		enterRule(_localctx, 60, RULE_minus);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(322);
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
		enterRule(_localctx, 62, RULE_hexPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
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
		enterRule(_localctx, 64, RULE_octPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
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
		enterRule(_localctx, 66, RULE_binPrefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
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
			setState(332);
			switch (_input.LA(1)) {
			case HYPHEN:
				{
				setState(330);
				minus();
				}
				break;
			case PLUS:
				{
				setState(331);
				match(PLUS);
				}
				break;
			case T__4:
			case DIGIT19:
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(334);
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
			setState(346);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(336);
				digit();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(337);
				match(DIGIT19);
				setState(343);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << UNDERSCORE) | (1L << DIGIT19))) != 0)) {
					{
					setState(341);
					switch (_input.LA(1)) {
					case T__4:
					case DIGIT19:
						{
						setState(338);
						digit();
						}
						break;
					case UNDERSCORE:
						{
						setState(339);
						match(UNDERSCORE);
						setState(340);
						digit();
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(345);
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
			setState(348);
			hexPrefix();
			setState(352);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(349);
					match(HEXDIG);
					}
					} 
				}
				setState(354);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			setState(358);
			switch (_input.LA(1)) {
			case HEXDIG:
				{
				setState(355);
				match(HEXDIG);
				}
				break;
			case UNDERSCORE:
				{
				setState(356);
				match(UNDERSCORE);
				setState(357);
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
			setState(360);
			octPrefix();
			setState(364);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(361);
					match(DIGIT07);
					}
					} 
				}
				setState(366);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(370);
			switch (_input.LA(1)) {
			case DIGIT07:
				{
				setState(367);
				match(DIGIT07);
				}
				break;
			case UNDERSCORE:
				{
				setState(368);
				match(UNDERSCORE);
				setState(369);
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
		enterRule(_localctx, 76, RULE_binInt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
			binPrefix();
			setState(376);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(373);
					match(DIGIT01);
					}
					} 
				}
				setState(378);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			}
			setState(382);
			switch (_input.LA(1)) {
			case DIGIT01:
				{
				setState(379);
				match(DIGIT01);
				}
				break;
			case UNDERSCORE:
				{
				setState(380);
				match(UNDERSCORE);
				setState(381);
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
			setState(393);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(384);
				floatIntPart();
				setState(390);
				switch (_input.LA(1)) {
				case E:
					{
					setState(385);
					exp();
					}
					break;
				case PERIOD:
					{
					setState(386);
					frac();
					setState(388);
					_la = _input.LA(1);
					if (_la==E) {
						{
						setState(387);
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
				setState(392);
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
			setState(395);
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
			setState(397);
			decimalPoint();
			setState(398);
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
			setState(400);
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
			setState(402);
			digit();
			setState(408);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << UNDERSCORE) | (1L << DIGIT19))) != 0)) {
				{
				setState(406);
				switch (_input.LA(1)) {
				case T__4:
				case DIGIT19:
					{
					setState(403);
					digit();
					}
					break;
				case UNDERSCORE:
					{
					setState(404);
					match(UNDERSCORE);
					setState(405);
					digit();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(410);
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
			setState(411);
			match(E);
			setState(412);
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
			setState(419);
			switch (_input.LA(1)) {
			case HYPHEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(414);
				minus();
				}
				break;
			case PLUS:
			case INF:
			case NAN:
				enterOuterAlt(_localctx, 2);
				{
				setState(416);
				_la = _input.LA(1);
				if (_la==PLUS) {
					{
					setState(415);
					match(PLUS);
					}
				}

				setState(418);
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
			setState(421);
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
			setState(427);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(423);
				offsetDateTime();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(424);
				localDateTime();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(425);
				localDate();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(426);
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
			setState(429);
			digit();
			setState(430);
			digit();
			setState(431);
			digit();
			setState(432);
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
			setState(434);
			digit();
			setState(435);
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
			setState(437);
			digit();
			setState(438);
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
			setState(440);
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
			setState(442);
			digit();
			setState(443);
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
			setState(445);
			digit();
			setState(446);
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
			setState(451);
			match(PERIOD);
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
			setState(454);
			_la = _input.LA(1);
			if ( !(_la==HYPHEN || _la==PLUS) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			setState(455);
			timeHour();
			setState(456);
			match(COLON);
			setState(457);
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
			setState(461);
			switch (_input.LA(1)) {
			case UPPERCASE_Z:
				enterOuterAlt(_localctx, 1);
				{
				setState(459);
				match(UPPERCASE_Z);
				}
				break;
			case HYPHEN:
			case PLUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(460);
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
			setState(463);
			timeHour();
			setState(464);
			match(COLON);
			setState(465);
			timeMinute();
			setState(466);
			match(COLON);
			setState(467);
			timeSecond();
			setState(469);
			_la = _input.LA(1);
			if (_la==PERIOD) {
				{
				setState(468);
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
			setState(471);
			dateFullyear();
			setState(472);
			match(HYPHEN);
			setState(473);
			dateMonth();
			setState(474);
			match(HYPHEN);
			setState(475);
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
			setState(477);
			partialTime();
			setState(478);
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
			setState(480);
			fullDate();
			setState(481);
			timeDelim();
			setState(482);
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
			setState(484);
			fullDate();
			setState(485);
			timeDelim();
			setState(486);
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
			setState(488);
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
			setState(490);
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
			setState(492);
			arrayOpen();
			setState(494);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(493);
				arrayValues();
				}
				break;
			}
			setState(496);
			ws();
			setState(497);
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
			setState(499);
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
			setState(501);
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
			setState(503);
			ws();
			setState(504);
			arrayvalsNonEmpty();
			setState(511);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(505);
				arraySep();
				setState(506);
				ws();
				setState(507);
				arrayvalsNonEmpty();
				}
				}
				setState(513);
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
			setState(514);
			val();
			setState(515);
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
			setState(517);
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
		enterRule(_localctx, 142, RULE_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(519);
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
		enterRule(_localctx, 144, RULE_stdTable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(521);
			stdTableOpen();
			setState(522);
			key();
			setState(523);
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
		enterRule(_localctx, 146, RULE_stdTableOpen);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(525);
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
		enterRule(_localctx, 148, RULE_stdTableClose);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\63\u0214\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\3\2\3\2\3\2\3\2\7\2\u009d\n\2\f\2\16\2\u00a0\13"+
		"\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\u00ad\n\4\3\5\7\5\u00b0"+
		"\n\5\f\5\16\5\u00b3\13\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\5\t\u00bf"+
		"\n\t\3\n\3\n\5\n\u00c3\n\n\3\13\3\13\3\13\3\13\7\13\u00c9\n\13\f\13\16"+
		"\13\u00cc\13\13\3\f\3\f\5\f\u00d0\n\f\3\r\3\r\3\r\3\r\7\r\u00d6\n\r\f"+
		"\r\16\r\u00d9\13\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\5\20\u00e9\n\20\3\21\3\21\3\21\3\21\5\21\u00ef\n\21"+
		"\3\22\3\22\7\22\u00f3\n\22\f\22\16\22\u00f6\13\22\3\22\3\22\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\5\23\u010b\n\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\27\3\27"+
		"\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\7\31\u0120\n\31\f\31\16"+
		"\31\u0123\13\31\3\32\3\32\3\33\3\33\7\33\u0129\n\33\f\33\16\33\u012c\13"+
		"\33\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\36\3\36\7\36\u013a"+
		"\n\36\f\36\16\36\u013d\13\36\3\37\3\37\3\37\3\37\5\37\u0143\n\37\3 \3"+
		" \3!\3!\3\"\3\"\3#\3#\3$\3$\5$\u014f\n$\3$\3$\3%\3%\3%\3%\3%\7%\u0158"+
		"\n%\f%\16%\u015b\13%\5%\u015d\n%\3&\3&\7&\u0161\n&\f&\16&\u0164\13&\3"+
		"&\3&\3&\5&\u0169\n&\3\'\3\'\7\'\u016d\n\'\f\'\16\'\u0170\13\'\3\'\3\'"+
		"\3\'\5\'\u0175\n\'\3(\3(\7(\u0179\n(\f(\16(\u017c\13(\3(\3(\3(\5(\u0181"+
		"\n(\3)\3)\3)\3)\5)\u0187\n)\5)\u0189\n)\3)\5)\u018c\n)\3*\3*\3+\3+\3+"+
		"\3,\3,\3-\3-\3-\3-\7-\u0199\n-\f-\16-\u019c\13-\3.\3.\3.\3/\3/\5/\u01a3"+
		"\n/\3/\5/\u01a6\n/\3\60\3\60\3\61\3\61\3\61\3\61\5\61\u01ae\n\61\3\62"+
		"\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\66\3\66"+
		"\3\66\3\67\3\67\3\67\38\38\38\39\39\39\3:\3:\3:\3:\3:\3;\3;\5;\u01d0\n"+
		";\3<\3<\3<\3<\3<\3<\5<\u01d8\n<\3=\3=\3=\3=\3=\3=\3>\3>\3>\3?\3?\3?\3"+
		"?\3@\3@\3@\3@\3A\3A\3B\3B\3C\3C\5C\u01f1\nC\3C\3C\3C\3D\3D\3E\3E\3F\3"+
		"F\3F\3F\3F\3F\7F\u0200\nF\fF\16F\u0203\13F\3G\3G\3G\3H\3H\3I\3I\3J\3J"+
		"\3J\3J\3K\3K\3L\3L\3L\2\2M\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$"+
		"&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084"+
		"\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\2\13\4\2\24\24"+
		"+\62\4\2\3\5\25\25\3\2\5\6\4\2\7\7##\3\2\b\17\3\2,-\3\2./\4\2\25\25\60"+
		"\61\4\2\26\26((\u020e\2\u0098\3\2\2\2\4\u00a1\3\2\2\2\6\u00ac\3\2\2\2"+
		"\b\u00b1\3\2\2\2\n\u00b4\3\2\2\2\f\u00b6\3\2\2\2\16\u00b8\3\2\2\2\20\u00be"+
		"\3\2\2\2\22\u00c2\3\2\2\2\24\u00ca\3\2\2\2\26\u00cf\3\2\2\2\30\u00d1\3"+
		"\2\2\2\32\u00da\3\2\2\2\34\u00de\3\2\2\2\36\u00e8\3\2\2\2 \u00ee\3\2\2"+
		"\2\"\u00f0\3\2\2\2$\u010a\3\2\2\2&\u010c\3\2\2\2(\u010e\3\2\2\2*\u0110"+
		"\3\2\2\2,\u0112\3\2\2\2.\u0116\3\2\2\2\60\u0121\3\2\2\2\62\u0124\3\2\2"+
		"\2\64\u0126\3\2\2\2\66\u012f\3\2\2\28\u0133\3\2\2\2:\u013b\3\2\2\2<\u0142"+
		"\3\2\2\2>\u0144\3\2\2\2@\u0146\3\2\2\2B\u0148\3\2\2\2D\u014a\3\2\2\2F"+
		"\u014e\3\2\2\2H\u015c\3\2\2\2J\u015e\3\2\2\2L\u016a\3\2\2\2N\u0176\3\2"+
		"\2\2P\u018b\3\2\2\2R\u018d\3\2\2\2T\u018f\3\2\2\2V\u0192\3\2\2\2X\u0194"+
		"\3\2\2\2Z\u019d\3\2\2\2\\\u01a5\3\2\2\2^\u01a7\3\2\2\2`\u01ad\3\2\2\2"+
		"b\u01af\3\2\2\2d\u01b4\3\2\2\2f\u01b7\3\2\2\2h\u01ba\3\2\2\2j\u01bc\3"+
		"\2\2\2l\u01bf\3\2\2\2n\u01c2\3\2\2\2p\u01c5\3\2\2\2r\u01c8\3\2\2\2t\u01cf"+
		"\3\2\2\2v\u01d1\3\2\2\2x\u01d9\3\2\2\2z\u01df\3\2\2\2|\u01e2\3\2\2\2~"+
		"\u01e6\3\2\2\2\u0080\u01ea\3\2\2\2\u0082\u01ec\3\2\2\2\u0084\u01ee\3\2"+
		"\2\2\u0086\u01f5\3\2\2\2\u0088\u01f7\3\2\2\2\u008a\u01f9\3\2\2\2\u008c"+
		"\u0204\3\2\2\2\u008e\u0207\3\2\2\2\u0090\u0209\3\2\2\2\u0092\u020b\3\2"+
		"\2\2\u0094\u020f\3\2\2\2\u0096\u0211\3\2\2\2\u0098\u009e\5\6\4\2\u0099"+
		"\u009a\5\f\7\2\u009a\u009b\5\6\4\2\u009b\u009d\3\2\2\2\u009c\u0099\3\2"+
		"\2\2\u009d\u00a0\3\2\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2\2\2\u009f"+
		"\3\3\2\2\2\u00a0\u009e\3\2\2\2\u00a1\u00a2\t\2\2\2\u00a2\5\3\2\2\2\u00a3"+
		"\u00ad\5\b\5\2\u00a4\u00a5\5\b\5\2\u00a5\u00a6\5\16\b\2\u00a6\u00a7\5"+
		"\b\5\2\u00a7\u00ad\3\2\2\2\u00a8\u00a9\5\b\5\2\u00a9\u00aa\5\u0090I\2"+
		"\u00aa\u00ab\5\b\5\2\u00ab\u00ad\3\2\2\2\u00ac\u00a3\3\2\2\2\u00ac\u00a4"+
		"\3\2\2\2\u00ac\u00a8\3\2\2\2\u00ad\7\3\2\2\2\u00ae\u00b0\5\n\6\2\u00af"+
		"\u00ae\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2"+
		"\2\2\u00b2\t\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b5\t\3\2\2\u00b5\13"+
		"\3\2\2\2\u00b6\u00b7\t\4\2\2\u00b7\r\3\2\2\2\u00b8\u00b9\5\20\t\2\u00b9"+
		"\u00ba\5\32\16\2\u00ba\u00bb\5\36\20\2\u00bb\17\3\2\2\2\u00bc\u00bf\5"+
		"\30\r\2\u00bd\u00bf\5\22\n\2\u00be\u00bc\3\2\2\2\u00be\u00bd\3\2\2\2\u00bf"+
		"\21\3\2\2\2\u00c0\u00c3\5\24\13\2\u00c1\u00c3\5\26\f\2\u00c2\u00c0\3\2"+
		"\2\2\u00c2\u00c1\3\2\2\2\u00c3\23\3\2\2\2\u00c4\u00c9\5\4\3\2\u00c5\u00c9"+
		"\5&\24\2\u00c6\u00c9\7\26\2\2\u00c7\u00c9\7\31\2\2\u00c8\u00c4\3\2\2\2"+
		"\u00c8\u00c5\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c7\3\2\2\2\u00c9\u00cc"+
		"\3\2\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\25\3\2\2\2\u00cc"+
		"\u00ca\3\2\2\2\u00cd\u00d0\5\"\22\2\u00ce\u00d0\5\64\33\2\u00cf\u00cd"+
		"\3\2\2\2\u00cf\u00ce\3\2\2\2\u00d0\27\3\2\2\2\u00d1\u00d7\5\22\n\2\u00d2"+
		"\u00d3\5\34\17\2\u00d3\u00d4\5\22\n\2\u00d4\u00d6\3\2\2\2\u00d5\u00d2"+
		"\3\2\2\2\u00d6\u00d9\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8"+
		"\31\3\2\2\2\u00d9\u00d7\3\2\2\2\u00da\u00db\5\b\5\2\u00db\u00dc\7\36\2"+
		"\2\u00dc\u00dd\5\b\5\2\u00dd\33\3\2\2\2\u00de\u00df\5\b\5\2\u00df\u00e0"+
		"\7\27\2\2\u00e0\u00e1\5\b\5\2\u00e1\35\3\2\2\2\u00e2\u00e9\5 \21\2\u00e3"+
		"\u00e9\5^\60\2\u00e4\u00e9\5\u0084C\2\u00e5\u00e9\5`\61\2\u00e6\u00e9"+
		"\5P)\2\u00e7\u00e9\5<\37\2\u00e8\u00e2\3\2\2\2\u00e8\u00e3\3\2\2\2\u00e8"+
		"\u00e4\3\2\2\2\u00e8\u00e5\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e8\u00e7\3\2"+
		"\2\2\u00e9\37\3\2\2\2\u00ea\u00ef\5,\27\2\u00eb\u00ef\5\"\22\2\u00ec\u00ef"+
		"\5\66\34\2\u00ed\u00ef\5\64\33\2\u00ee\u00ea\3\2\2\2\u00ee\u00eb\3\2\2"+
		"\2\u00ee\u00ec\3\2\2\2\u00ee\u00ed\3\2\2\2\u00ef!\3\2\2\2\u00f0\u00f4"+
		"\7\30\2\2\u00f1\u00f3\5$\23\2\u00f2\u00f1\3\2\2\2\u00f3\u00f6\3\2\2\2"+
		"\u00f4\u00f2\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f7\3\2\2\2\u00f6\u00f4"+
		"\3\2\2\2\u00f7\u00f8\7\30\2\2\u00f8#\3\2\2\2\u00f9\u010b\5(\25\2\u00fa"+
		"\u010b\5\4\3\2\u00fb\u010b\7$\2\2\u00fc\u010b\7\25\2\2\u00fd\u010b\7("+
		"\2\2\u00fe\u010b\7\26\2\2\u00ff\u010b\7\27\2\2\u0100\u010b\7\31\2\2\u0101"+
		"\u010b\7\32\2\2\u0102\u010b\7\33\2\2\u0103\u010b\7\34\2\2\u0104\u010b"+
		"\7\35\2\2\u0105\u010b\7\36\2\2\u0106\u010b\7\37\2\2\u0107\u010b\7 \2\2"+
		"\u0108\u010b\7!\2\2\u0109\u010b\5&\24\2\u010a\u00f9\3\2\2\2\u010a\u00fa"+
		"\3\2\2\2\u010a\u00fb\3\2\2\2\u010a\u00fc\3\2\2\2\u010a\u00fd\3\2\2\2\u010a"+
		"\u00fe\3\2\2\2\u010a\u00ff\3\2\2\2\u010a\u0100\3\2\2\2\u010a\u0101\3\2"+
		"\2\2\u010a\u0102\3\2\2\2\u010a\u0103\3\2\2\2\u010a\u0104\3\2\2\2\u010a"+
		"\u0105\3\2\2\2\u010a\u0106\3\2\2\2\u010a\u0107\3\2\2\2\u010a\u0108\3\2"+
		"\2\2\u010a\u0109\3\2\2\2\u010b%\3\2\2\2\u010c\u010d\t\5\2\2\u010d\'\3"+
		"\2\2\2\u010e\u010f\5*\26\2\u010f)\3\2\2\2\u0110\u0111\t\6\2\2\u0111+\3"+
		"\2\2\2\u0112\u0113\5.\30\2\u0113\u0114\5\60\31\2\u0114\u0115\5.\30\2\u0115"+
		"-\3\2\2\2\u0116\u0117\7\30\2\2\u0117\u0118\7\30\2\2\u0118\u0119\7\30\2"+
		"\2\u0119/\3\2\2\2\u011a\u0120\5\62\32\2\u011b\u011c\7\20\2\2\u011c\u011d"+
		"\5\b\5\2\u011d\u011e\5\f\7\2\u011e\u0120\3\2\2\2\u011f\u011a\3\2\2\2\u011f"+
		"\u011b\3\2\2\2\u0120\u0123\3\2\2\2\u0121\u011f\3\2\2\2\u0121\u0122\3\2"+
		"\2\2\u0122\61\3\2\2\2\u0123\u0121\3\2\2\2\u0124\u0125\7%\2\2\u0125\63"+
		"\3\2\2\2\u0126\u012a\7\35\2\2\u0127\u0129\7&\2\2\u0128\u0127\3\2\2\2\u0129"+
		"\u012c\3\2\2\2\u012a\u0128\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012d\3\2"+
		"\2\2\u012c\u012a\3\2\2\2\u012d\u012e\7\35\2\2\u012e\65\3\2\2\2\u012f\u0130"+
		"\58\35\2\u0130\u0131\5:\36\2\u0131\u0132\58\35\2\u0132\67\3\2\2\2\u0133"+
		"\u0134\7\35\2\2\u0134\u0135\7\35\2\2\u0135\u0136\7\35\2\2\u01369\3\2\2"+
		"\2\u0137\u013a\7\'\2\2\u0138\u013a\5\f\7\2\u0139\u0137\3\2\2\2\u0139\u0138"+
		"\3\2\2\2\u013a\u013d\3\2\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013c"+
		";\3\2\2\2\u013d\u013b\3\2\2\2\u013e\u0143\5F$\2\u013f\u0143\5J&\2\u0140"+
		"\u0143\5L\'\2\u0141\u0143\5N(\2\u0142\u013e\3\2\2\2\u0142\u013f\3\2\2"+
		"\2\u0142\u0140\3\2\2\2\u0142\u0141\3\2\2\2\u0143=\3\2\2\2\u0144\u0145"+
		"\7\26\2\2\u0145?\3\2\2\2\u0146\u0147\7\21\2\2\u0147A\3\2\2\2\u0148\u0149"+
		"\7\22\2\2\u0149C\3\2\2\2\u014a\u014b\7\23\2\2\u014bE\3\2\2\2\u014c\u014f"+
		"\5> \2\u014d\u014f\7(\2\2\u014e\u014c\3\2\2\2\u014e\u014d\3\2\2\2\u014e"+
		"\u014f\3\2\2\2\u014f\u0150\3\2\2\2\u0150\u0151\5H%\2\u0151G\3\2\2\2\u0152"+
		"\u015d\5&\24\2\u0153\u0159\7#\2\2\u0154\u0158\5&\24\2\u0155\u0156\7\31"+
		"\2\2\u0156\u0158\5&\24\2\u0157\u0154\3\2\2\2\u0157\u0155\3\2\2\2\u0158"+
		"\u015b\3\2\2\2\u0159\u0157\3\2\2\2\u0159\u015a\3\2\2\2\u015a\u015d\3\2"+
		"\2\2\u015b\u0159\3\2\2\2\u015c\u0152\3\2\2\2\u015c\u0153\3\2\2\2\u015d"+
		"I\3\2\2\2\u015e\u0162\5@!\2\u015f\u0161\7\63\2\2\u0160\u015f\3\2\2\2\u0161"+
		"\u0164\3\2\2\2\u0162\u0160\3\2\2\2\u0162\u0163\3\2\2\2\u0163\u0168\3\2"+
		"\2\2\u0164\u0162\3\2\2\2\u0165\u0169\7\63\2\2\u0166\u0167\7\31\2\2\u0167"+
		"\u0169\7\63\2\2\u0168\u0165\3\2\2\2\u0168\u0166\3\2\2\2\u0169K\3\2\2\2"+
		"\u016a\u016e\5B\"\2\u016b\u016d\7)\2\2\u016c\u016b\3\2\2\2\u016d\u0170"+
		"\3\2\2\2\u016e\u016c\3\2\2\2\u016e\u016f\3\2\2\2\u016f\u0174\3\2\2\2\u0170"+
		"\u016e\3\2\2\2\u0171\u0175\7)\2\2\u0172\u0173\7\31\2\2\u0173\u0175\7)"+
		"\2\2\u0174\u0171\3\2\2\2\u0174\u0172\3\2\2\2\u0175M\3\2\2\2\u0176\u017a"+
		"\5D#\2\u0177\u0179\7*\2\2\u0178\u0177\3\2\2\2\u0179\u017c\3\2\2\2\u017a"+
		"\u0178\3\2\2\2\u017a\u017b\3\2\2\2\u017b\u0180\3\2\2\2\u017c\u017a\3\2"+
		"\2\2\u017d\u0181\7*\2\2\u017e\u017f\7\31\2\2\u017f\u0181\7*\2\2\u0180"+
		"\u017d\3\2\2\2\u0180\u017e\3\2\2\2\u0181O\3\2\2\2\u0182\u0188\5R*\2\u0183"+
		"\u0189\5Z.\2\u0184\u0186\5T+\2\u0185\u0187\5Z.\2\u0186\u0185\3\2\2\2\u0186"+
		"\u0187\3\2\2\2\u0187\u0189\3\2\2\2\u0188\u0183\3\2\2\2\u0188\u0184\3\2"+
		"\2\2\u0189\u018c\3\2\2\2\u018a\u018c\5\\/\2\u018b\u0182\3\2\2\2\u018b"+
		"\u018a\3\2\2\2\u018cQ\3\2\2\2\u018d\u018e\5F$\2\u018eS\3\2\2\2\u018f\u0190"+
		"\5V,\2\u0190\u0191\5X-\2\u0191U\3\2\2\2\u0192\u0193\7\27\2\2\u0193W\3"+
		"\2\2\2\u0194\u019a\5&\24\2\u0195\u0199\5&\24\2\u0196\u0197\7\31\2\2\u0197"+
		"\u0199\5&\24\2\u0198\u0195\3\2\2\2\u0198\u0196\3\2\2\2\u0199\u019c\3\2"+
		"\2\2\u019a\u0198\3\2\2\2\u019a\u019b\3\2\2\2\u019bY\3\2\2\2\u019c\u019a"+
		"\3\2\2\2\u019d\u019e\7+\2\2\u019e\u019f\5R*\2\u019f[\3\2\2\2\u01a0\u01a6"+
		"\5> \2\u01a1\u01a3\7(\2\2\u01a2\u01a1\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3"+
		"\u01a4\3\2\2\2\u01a4\u01a6\t\7\2\2\u01a5\u01a0\3\2\2\2\u01a5\u01a2\3\2"+
		"\2\2\u01a6]\3\2\2\2\u01a7\u01a8\t\b\2\2\u01a8_\3\2\2\2\u01a9\u01ae\5|"+
		"?\2\u01aa\u01ae\5~@\2\u01ab\u01ae\5\u0080A\2\u01ac\u01ae\5\u0082B\2\u01ad"+
		"\u01a9\3\2\2\2\u01ad\u01aa\3\2\2\2\u01ad\u01ab\3\2\2\2\u01ad\u01ac\3\2"+
		"\2\2\u01aea\3\2\2\2\u01af\u01b0\5&\24\2\u01b0\u01b1\5&\24\2\u01b1\u01b2"+
		"\5&\24\2\u01b2\u01b3\5&\24\2\u01b3c\3\2\2\2\u01b4\u01b5\5&\24\2\u01b5"+
		"\u01b6\5&\24\2\u01b6e\3\2\2\2\u01b7\u01b8\5&\24\2\u01b8\u01b9\5&\24\2"+
		"\u01b9g\3\2\2\2\u01ba\u01bb\t\t\2\2\u01bbi\3\2\2\2\u01bc\u01bd\5&\24\2"+
		"\u01bd\u01be\5&\24\2\u01bek\3\2\2\2\u01bf\u01c0\5&\24\2\u01c0\u01c1\5"+
		"&\24\2\u01c1m\3\2\2\2\u01c2\u01c3\5&\24\2\u01c3\u01c4\5&\24\2\u01c4o\3"+
		"\2\2\2\u01c5\u01c6\7\27\2\2\u01c6\u01c7\5&\24\2\u01c7q\3\2\2\2\u01c8\u01c9"+
		"\t\n\2\2\u01c9\u01ca\5j\66\2\u01ca\u01cb\7\32\2\2\u01cb\u01cc\5l\67\2"+
		"\u01ccs\3\2\2\2\u01cd\u01d0\7\62\2\2\u01ce\u01d0\5r:\2\u01cf\u01cd\3\2"+
		"\2\2\u01cf\u01ce\3\2\2\2\u01d0u\3\2\2\2\u01d1\u01d2\5j\66\2\u01d2\u01d3"+
		"\7\32\2\2\u01d3\u01d4\5l\67\2\u01d4\u01d5\7\32\2\2\u01d5\u01d7\5n8\2\u01d6"+
		"\u01d8\5p9\2\u01d7\u01d6\3\2\2\2\u01d7\u01d8\3\2\2\2\u01d8w\3\2\2\2\u01d9"+
		"\u01da\5b\62\2\u01da\u01db\7\26\2\2\u01db\u01dc\5d\63\2\u01dc\u01dd\7"+
		"\26\2\2\u01dd\u01de\5f\64\2\u01dey\3\2\2\2\u01df\u01e0\5v<\2\u01e0\u01e1"+
		"\5t;\2\u01e1{\3\2\2\2\u01e2\u01e3\5x=\2\u01e3\u01e4\5h\65\2\u01e4\u01e5"+
		"\5z>\2\u01e5}\3\2\2\2\u01e6\u01e7\5x=\2\u01e7\u01e8\5h\65\2\u01e8\u01e9"+
		"\5v<\2\u01e9\177\3\2\2\2\u01ea\u01eb\5x=\2\u01eb\u0081\3\2\2\2\u01ec\u01ed"+
		"\5v<\2\u01ed\u0083\3\2\2\2\u01ee\u01f0\5\u0086D\2\u01ef\u01f1\5\u008a"+
		"F\2\u01f0\u01ef\3\2\2\2\u01f0\u01f1\3\2\2\2\u01f1\u01f2\3\2\2\2\u01f2"+
		"\u01f3\5\b\5\2\u01f3\u01f4\5\u0088E\2\u01f4\u0085\3\2\2\2\u01f5\u01f6"+
		"\7 \2\2\u01f6\u0087\3\2\2\2\u01f7\u01f8\7!\2\2\u01f8\u0089\3\2\2\2\u01f9"+
		"\u01fa\5\b\5\2\u01fa\u0201\5\u008cG\2\u01fb\u01fc\5\u008eH\2\u01fc\u01fd"+
		"\5\b\5\2\u01fd\u01fe\5\u008cG\2\u01fe\u0200\3\2\2\2\u01ff\u01fb\3\2\2"+
		"\2\u0200\u0203\3\2\2\2\u0201\u01ff\3\2\2\2\u0201\u0202\3\2\2\2\u0202\u008b"+
		"\3\2\2\2\u0203\u0201\3\2\2\2\u0204\u0205\5\36\20\2\u0205\u0206\5\b\5\2"+
		"\u0206\u008d\3\2\2\2\u0207\u0208\7\33\2\2\u0208\u008f\3\2\2\2\u0209\u020a"+
		"\5\u0092J\2\u020a\u0091\3\2\2\2\u020b\u020c\5\u0094K\2\u020c\u020d\5\20"+
		"\t\2\u020d\u020e\5\u0096L\2\u020e\u0093\3\2\2\2\u020f\u0210\7 \2\2\u0210"+
		"\u0095\3\2\2\2\u0211\u0212\7!\2\2\u0212\u0097\3\2\2\2+\u009e\u00ac\u00b1"+
		"\u00be\u00c2\u00c8\u00ca\u00cf\u00d7\u00e8\u00ee\u00f4\u010a\u011f\u0121"+
		"\u012a\u0139\u013b\u0142\u014e\u0157\u0159\u015c\u0162\u0168\u016e\u0174"+
		"\u017a\u0180\u0186\u0188\u018b\u0198\u019a\u01a2\u01a5\u01ad\u01cf\u01d7"+
		"\u01f0\u0201";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}