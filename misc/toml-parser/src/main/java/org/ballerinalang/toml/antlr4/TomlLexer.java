// Generated from Toml.g4 by ANTLR 4.5.3
package org.ballerinalang.toml.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TomlLexer extends Lexer {
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
		PLUS=39, DIGIT07=40, DIGIT01=41, HEX_PREFIX=42, OCT_PREFIX=43, BIN_PREFIX=44, 
		E=45, INF=46, NAN=47, TRUE=48, FALSE=49, UPPERCASE_T=50, LOWERCASE_T=51, 
		UPPERCASE_Z=52;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "ALPHA", 
		"SPACE", "HYPHEN", "PERIOD", "QUOTATION_MARK", "UNDERSCORE", "COLON", 
		"COMMA", "SLASH", "APOSTROPHE", "EQUALS", "HASH", "LEFT_BRACKET", "RIGHT_BRACKET", 
		"LEFT_BRACE", "RIGHT_BRACE", "COMMENT", "DIGIT19", "BASICUNESCAPED", "MLBASICUNESCAPED", 
		"LITERALCHAR", "MLLITERALCHAR", "PLUS", "DIGIT07", "DIGIT01", "HEX_PREFIX", 
		"OCT_PREFIX", "BIN_PREFIX", "E", "INF", "NAN", "TRUE", "FALSE", "UPPERCASE_T", 
		"LOWERCASE_T", "UPPERCASE_Z"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\t'", "'\r'", "'\n'", "'\r\n'", "'0'", "'\\\"'", "'\\\\'", "'\\/'", 
		"'\\b'", "'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\'", "'[['", "']]'", null, 
		"' '", "'-'", "'.'", "'\"'", "'_'", "':'", "','", "'/'", "'''", "'='", 
		"'#'", "'['", "']'", "'{'", "'}'", null, null, null, null, null, null, 
		"'+'", null, null, "'0x'", "'0o'", "'0b'", "'e'", "'inf'", "'nan'", "'true'", 
		"'false'", "'T'", "'t'", "'Z'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, "ALPHA", "SPACE", "HYPHEN", "PERIOD", "QUOTATION_MARK", 
		"UNDERSCORE", "COLON", "COMMA", "SLASH", "APOSTROPHE", "EQUALS", "HASH", 
		"LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_BRACE", "RIGHT_BRACE", "COMMENT", 
		"DIGIT19", "BASICUNESCAPED", "MLBASICUNESCAPED", "LITERALCHAR", "MLLITERALCHAR", 
		"PLUS", "DIGIT07", "DIGIT01", "HEX_PREFIX", "OCT_PREFIX", "BIN_PREFIX", 
		"E", "INF", "NAN", "TRUE", "FALSE", "UPPERCASE_T", "LOWERCASE_T", "UPPERCASE_Z"
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


	public TomlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Toml.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\66\u00f6\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3"+
		"\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\r"+
		"\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\3\22\5"+
		"\22\u0098\n\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30"+
		"\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37"+
		"\3\37\3 \3 \3!\3!\3\"\3\"\7\"\u00ba\n\"\f\"\16\"\u00bd\13\"\3\"\3\"\3"+
		"\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3+\3,\3"+
		",\3,\3-\3-\3-\3.\3.\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\61"+
		"\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\u00bb"+
		"\2\66\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64g\65i\66\3\2"+
		"\13\4\2C\\c|\4\2\13\f\17\17\3\2\63;\6\2##%]_\u0080\u0082\u1101\4\2\"]"+
		"_\u1101\5\2\13\13\"(*\u1101\4\2\13\13\"\u1101\3\2\629\3\2\62\63\u00f6"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3"+
		"\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2"+
		"\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2"+
		"U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3"+
		"\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\3k\3\2\2\2\5m\3\2\2"+
		"\2\7o\3\2\2\2\tq\3\2\2\2\13t\3\2\2\2\rv\3\2\2\2\17y\3\2\2\2\21|\3\2\2"+
		"\2\23\177\3\2\2\2\25\u0082\3\2\2\2\27\u0085\3\2\2\2\31\u0088\3\2\2\2\33"+
		"\u008b\3\2\2\2\35\u008e\3\2\2\2\37\u0090\3\2\2\2!\u0093\3\2\2\2#\u0097"+
		"\3\2\2\2%\u0099\3\2\2\2\'\u009b\3\2\2\2)\u009d\3\2\2\2+\u009f\3\2\2\2"+
		"-\u00a1\3\2\2\2/\u00a3\3\2\2\2\61\u00a5\3\2\2\2\63\u00a7\3\2\2\2\65\u00a9"+
		"\3\2\2\2\67\u00ab\3\2\2\29\u00ad\3\2\2\2;\u00af\3\2\2\2=\u00b1\3\2\2\2"+
		"?\u00b3\3\2\2\2A\u00b5\3\2\2\2C\u00b7\3\2\2\2E\u00c2\3\2\2\2G\u00c4\3"+
		"\2\2\2I\u00c6\3\2\2\2K\u00c8\3\2\2\2M\u00ca\3\2\2\2O\u00cc\3\2\2\2Q\u00ce"+
		"\3\2\2\2S\u00d0\3\2\2\2U\u00d2\3\2\2\2W\u00d5\3\2\2\2Y\u00d8\3\2\2\2["+
		"\u00db\3\2\2\2]\u00dd\3\2\2\2_\u00e1\3\2\2\2a\u00e5\3\2\2\2c\u00ea\3\2"+
		"\2\2e\u00f0\3\2\2\2g\u00f2\3\2\2\2i\u00f4\3\2\2\2kl\7\13\2\2l\4\3\2\2"+
		"\2mn\7\17\2\2n\6\3\2\2\2op\7\f\2\2p\b\3\2\2\2qr\7\17\2\2rs\7\f\2\2s\n"+
		"\3\2\2\2tu\7\62\2\2u\f\3\2\2\2vw\7^\2\2wx\7$\2\2x\16\3\2\2\2yz\7^\2\2"+
		"z{\7^\2\2{\20\3\2\2\2|}\7^\2\2}~\7\61\2\2~\22\3\2\2\2\177\u0080\7^\2\2"+
		"\u0080\u0081\7d\2\2\u0081\24\3\2\2\2\u0082\u0083\7^\2\2\u0083\u0084\7"+
		"h\2\2\u0084\26\3\2\2\2\u0085\u0086\7^\2\2\u0086\u0087\7p\2\2\u0087\30"+
		"\3\2\2\2\u0088\u0089\7^\2\2\u0089\u008a\7t\2\2\u008a\32\3\2\2\2\u008b"+
		"\u008c\7^\2\2\u008c\u008d\7v\2\2\u008d\34\3\2\2\2\u008e\u008f\7^\2\2\u008f"+
		"\36\3\2\2\2\u0090\u0091\7]\2\2\u0091\u0092\7]\2\2\u0092 \3\2\2\2\u0093"+
		"\u0094\7_\2\2\u0094\u0095\7_\2\2\u0095\"\3\2\2\2\u0096\u0098\t\2\2\2\u0097"+
		"\u0096\3\2\2\2\u0098$\3\2\2\2\u0099\u009a\7\"\2\2\u009a&\3\2\2\2\u009b"+
		"\u009c\7/\2\2\u009c(\3\2\2\2\u009d\u009e\7\60\2\2\u009e*\3\2\2\2\u009f"+
		"\u00a0\7$\2\2\u00a0,\3\2\2\2\u00a1\u00a2\7a\2\2\u00a2.\3\2\2\2\u00a3\u00a4"+
		"\7<\2\2\u00a4\60\3\2\2\2\u00a5\u00a6\7.\2\2\u00a6\62\3\2\2\2\u00a7\u00a8"+
		"\7\61\2\2\u00a8\64\3\2\2\2\u00a9\u00aa\7)\2\2\u00aa\66\3\2\2\2\u00ab\u00ac"+
		"\7?\2\2\u00ac8\3\2\2\2\u00ad\u00ae\7%\2\2\u00ae:\3\2\2\2\u00af\u00b0\7"+
		"]\2\2\u00b0<\3\2\2\2\u00b1\u00b2\7_\2\2\u00b2>\3\2\2\2\u00b3\u00b4\7}"+
		"\2\2\u00b4@\3\2\2\2\u00b5\u00b6\7\177\2\2\u00b6B\3\2\2\2\u00b7\u00bb\7"+
		"%\2\2\u00b8\u00ba\13\2\2\2\u00b9\u00b8\3\2\2\2\u00ba\u00bd\3\2\2\2\u00bb"+
		"\u00bc\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bc\u00be\3\2\2\2\u00bd\u00bb\3\2"+
		"\2\2\u00be\u00bf\t\3\2\2\u00bf\u00c0\3\2\2\2\u00c0\u00c1\b\"\2\2\u00c1"+
		"D\3\2\2\2\u00c2\u00c3\t\4\2\2\u00c3F\3\2\2\2\u00c4\u00c5\t\5\2\2\u00c5"+
		"H\3\2\2\2\u00c6\u00c7\t\6\2\2\u00c7J\3\2\2\2\u00c8\u00c9\t\7\2\2\u00c9"+
		"L\3\2\2\2\u00ca\u00cb\t\b\2\2\u00cbN\3\2\2\2\u00cc\u00cd\7-\2\2\u00cd"+
		"P\3\2\2\2\u00ce\u00cf\t\t\2\2\u00cfR\3\2\2\2\u00d0\u00d1\t\n\2\2\u00d1"+
		"T\3\2\2\2\u00d2\u00d3\7\62\2\2\u00d3\u00d4\7z\2\2\u00d4V\3\2\2\2\u00d5"+
		"\u00d6\7\62\2\2\u00d6\u00d7\7q\2\2\u00d7X\3\2\2\2\u00d8\u00d9\7\62\2\2"+
		"\u00d9\u00da\7d\2\2\u00daZ\3\2\2\2\u00db\u00dc\7g\2\2\u00dc\\\3\2\2\2"+
		"\u00dd\u00de\7k\2\2\u00de\u00df\7p\2\2\u00df\u00e0\7h\2\2\u00e0^\3\2\2"+
		"\2\u00e1\u00e2\7p\2\2\u00e2\u00e3\7c\2\2\u00e3\u00e4\7p\2\2\u00e4`\3\2"+
		"\2\2\u00e5\u00e6\7v\2\2\u00e6\u00e7\7t\2\2\u00e7\u00e8\7w\2\2\u00e8\u00e9"+
		"\7g\2\2\u00e9b\3\2\2\2\u00ea\u00eb\7h\2\2\u00eb\u00ec\7c\2\2\u00ec\u00ed"+
		"\7n\2\2\u00ed\u00ee\7u\2\2\u00ee\u00ef\7g\2\2\u00efd\3\2\2\2\u00f0\u00f1"+
		"\7V\2\2\u00f1f\3\2\2\2\u00f2\u00f3\7v\2\2\u00f3h\3\2\2\2\u00f4\u00f5\7"+
		"\\\2\2\u00f5j\3\2\2\2\5\2\u0097\u00bb\3\2\4\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}