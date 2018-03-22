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
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		ALPHA=18, SPACE=19, HYPHEN=20, PERIOD=21, QUOTATION_MARK=22, UNDERSCORE=23, 
		COLON=24, COMMA=25, SLASH=26, APOSTROPHE=27, EQUALS=28, HASH=29, LEFT_BRACKET=30, 
		RIGHT_BRACKET=31, LEFT_BRACE=32, RIGHT_BRACE=33, COMMENT=34, DIGIT19=35, 
		BASICUNESCAPED=36, MLBASICUNESCAPED=37, LITERALCHAR=38, MLLITERALCHAR=39, 
		PLUS=40, DIGIT07=41, DIGIT01=42, E=43, INF=44, NAN=45, TRUE=46, FALSE=47, 
		UPPERCASE_T=48, LOWERCASE_T=49, UPPERCASE_Z=50;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
		"ALPHA", "SPACE", "HYPHEN", "PERIOD", "QUOTATION_MARK", "UNDERSCORE", 
		"COLON", "COMMA", "SLASH", "APOSTROPHE", "EQUALS", "HASH", "LEFT_BRACKET", 
		"RIGHT_BRACKET", "LEFT_BRACE", "RIGHT_BRACE", "COMMENT", "DIGIT19", "BASICUNESCAPED", 
		"MLBASICUNESCAPED", "LITERALCHAR", "MLLITERALCHAR", "PLUS", "DIGIT07", 
		"DIGIT01", "E", "INF", "NAN", "TRUE", "FALSE", "UPPERCASE_T", "LOWERCASE_T", 
		"UPPERCASE_Z"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'\t'", "'\r'", "'\n'", "'\r\n'", "'0'", "'\\\"'", "'\\\\'", "'\\/'", 
		"'\\b'", "'\\f'", "'\\n'", "'\\r'", "'\\t'", "'\\'", "'0x'", "'0o'", "'0b'", 
		null, "' '", "'-'", "'.'", "'\"'", "'_'", "':'", "','", "'/'", "'''", 
		"'='", "'#'", "'['", "']'", "'{'", "'}'", null, null, null, null, null, 
		null, "'+'", null, null, "'e'", "'inf'", "'nan'", "'true'", "'false'", 
		"'T'", "'t'", "'Z'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, "ALPHA", "SPACE", "HYPHEN", "PERIOD", 
		"QUOTATION_MARK", "UNDERSCORE", "COLON", "COMMA", "SLASH", "APOSTROPHE", 
		"EQUALS", "HASH", "LEFT_BRACKET", "RIGHT_BRACKET", "LEFT_BRACE", "RIGHT_BRACE", 
		"COMMENT", "DIGIT19", "BASICUNESCAPED", "MLBASICUNESCAPED", "LITERALCHAR", 
		"MLLITERALCHAR", "PLUS", "DIGIT07", "DIGIT01", "E", "INF", "NAN", "TRUE", 
		"FALSE", "UPPERCASE_T", "LOWERCASE_T", "UPPERCASE_Z"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\64\u00ec\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3"+
		"\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3"+
		"\16\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\22\3\23\5\23\u0097"+
		"\n\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32"+
		"\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3"+
		"\"\3\"\3#\3#\7#\u00b9\n#\f#\16#\u00bc\13#\3#\3#\3#\3#\3$\3$\3%\3%\3&\3"+
		"&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3-\3-\3.\3.\3.\3.\3/\3/"+
		"\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63"+
		"\3\u00ba\2\64\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34"+
		"\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\62c\63e\64\3"+
		"\2\13\4\2C\\c|\4\2\13\f\17\17\3\2\63;\6\2##%]_\u0080\u0082\u1101\4\2\""+
		"]_\u1101\5\2\13\13\"(*\u1101\4\2\13\13\"\u1101\3\2\629\3\2\62\63\u00ec"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3"+
		"\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2"+
		"\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2"+
		"U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3"+
		"\2\2\2\2c\3\2\2\2\2e\3\2\2\2\3g\3\2\2\2\5i\3\2\2\2\7k\3\2\2\2\tm\3\2\2"+
		"\2\13p\3\2\2\2\rr\3\2\2\2\17u\3\2\2\2\21x\3\2\2\2\23{\3\2\2\2\25~\3\2"+
		"\2\2\27\u0081\3\2\2\2\31\u0084\3\2\2\2\33\u0087\3\2\2\2\35\u008a\3\2\2"+
		"\2\37\u008c\3\2\2\2!\u008f\3\2\2\2#\u0092\3\2\2\2%\u0096\3\2\2\2\'\u0098"+
		"\3\2\2\2)\u009a\3\2\2\2+\u009c\3\2\2\2-\u009e\3\2\2\2/\u00a0\3\2\2\2\61"+
		"\u00a2\3\2\2\2\63\u00a4\3\2\2\2\65\u00a6\3\2\2\2\67\u00a8\3\2\2\29\u00aa"+
		"\3\2\2\2;\u00ac\3\2\2\2=\u00ae\3\2\2\2?\u00b0\3\2\2\2A\u00b2\3\2\2\2C"+
		"\u00b4\3\2\2\2E\u00b6\3\2\2\2G\u00c1\3\2\2\2I\u00c3\3\2\2\2K\u00c5\3\2"+
		"\2\2M\u00c7\3\2\2\2O\u00c9\3\2\2\2Q\u00cb\3\2\2\2S\u00cd\3\2\2\2U\u00cf"+
		"\3\2\2\2W\u00d1\3\2\2\2Y\u00d3\3\2\2\2[\u00d7\3\2\2\2]\u00db\3\2\2\2_"+
		"\u00e0\3\2\2\2a\u00e6\3\2\2\2c\u00e8\3\2\2\2e\u00ea\3\2\2\2gh\7\13\2\2"+
		"h\4\3\2\2\2ij\7\17\2\2j\6\3\2\2\2kl\7\f\2\2l\b\3\2\2\2mn\7\17\2\2no\7"+
		"\f\2\2o\n\3\2\2\2pq\7\62\2\2q\f\3\2\2\2rs\7^\2\2st\7$\2\2t\16\3\2\2\2"+
		"uv\7^\2\2vw\7^\2\2w\20\3\2\2\2xy\7^\2\2yz\7\61\2\2z\22\3\2\2\2{|\7^\2"+
		"\2|}\7d\2\2}\24\3\2\2\2~\177\7^\2\2\177\u0080\7h\2\2\u0080\26\3\2\2\2"+
		"\u0081\u0082\7^\2\2\u0082\u0083\7p\2\2\u0083\30\3\2\2\2\u0084\u0085\7"+
		"^\2\2\u0085\u0086\7t\2\2\u0086\32\3\2\2\2\u0087\u0088\7^\2\2\u0088\u0089"+
		"\7v\2\2\u0089\34\3\2\2\2\u008a\u008b\7^\2\2\u008b\36\3\2\2\2\u008c\u008d"+
		"\7\62\2\2\u008d\u008e\7z\2\2\u008e \3\2\2\2\u008f\u0090\7\62\2\2\u0090"+
		"\u0091\7q\2\2\u0091\"\3\2\2\2\u0092\u0093\7\62\2\2\u0093\u0094\7d\2\2"+
		"\u0094$\3\2\2\2\u0095\u0097\t\2\2\2\u0096\u0095\3\2\2\2\u0097&\3\2\2\2"+
		"\u0098\u0099\7\"\2\2\u0099(\3\2\2\2\u009a\u009b\7/\2\2\u009b*\3\2\2\2"+
		"\u009c\u009d\7\60\2\2\u009d,\3\2\2\2\u009e\u009f\7$\2\2\u009f.\3\2\2\2"+
		"\u00a0\u00a1\7a\2\2\u00a1\60\3\2\2\2\u00a2\u00a3\7<\2\2\u00a3\62\3\2\2"+
		"\2\u00a4\u00a5\7.\2\2\u00a5\64\3\2\2\2\u00a6\u00a7\7\61\2\2\u00a7\66\3"+
		"\2\2\2\u00a8\u00a9\7)\2\2\u00a98\3\2\2\2\u00aa\u00ab\7?\2\2\u00ab:\3\2"+
		"\2\2\u00ac\u00ad\7%\2\2\u00ad<\3\2\2\2\u00ae\u00af\7]\2\2\u00af>\3\2\2"+
		"\2\u00b0\u00b1\7_\2\2\u00b1@\3\2\2\2\u00b2\u00b3\7}\2\2\u00b3B\3\2\2\2"+
		"\u00b4\u00b5\7\177\2\2\u00b5D\3\2\2\2\u00b6\u00ba\7%\2\2\u00b7\u00b9\13"+
		"\2\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00bc\3\2\2\2\u00ba\u00bb\3\2\2\2\u00ba"+
		"\u00b8\3\2\2\2\u00bb\u00bd\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bd\u00be\t\3"+
		"\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c0\b#\2\2\u00c0F\3\2\2\2\u00c1\u00c2"+
		"\t\4\2\2\u00c2H\3\2\2\2\u00c3\u00c4\t\5\2\2\u00c4J\3\2\2\2\u00c5\u00c6"+
		"\t\6\2\2\u00c6L\3\2\2\2\u00c7\u00c8\t\7\2\2\u00c8N\3\2\2\2\u00c9\u00ca"+
		"\t\b\2\2\u00caP\3\2\2\2\u00cb\u00cc\7-\2\2\u00ccR\3\2\2\2\u00cd\u00ce"+
		"\t\t\2\2\u00ceT\3\2\2\2\u00cf\u00d0\t\n\2\2\u00d0V\3\2\2\2\u00d1\u00d2"+
		"\7g\2\2\u00d2X\3\2\2\2\u00d3\u00d4\7k\2\2\u00d4\u00d5\7p\2\2\u00d5\u00d6"+
		"\7h\2\2\u00d6Z\3\2\2\2\u00d7\u00d8\7p\2\2\u00d8\u00d9\7c\2\2\u00d9\u00da"+
		"\7p\2\2\u00da\\\3\2\2\2\u00db\u00dc\7v\2\2\u00dc\u00dd\7t\2\2\u00dd\u00de"+
		"\7w\2\2\u00de\u00df\7g\2\2\u00df^\3\2\2\2\u00e0\u00e1\7h\2\2\u00e1\u00e2"+
		"\7c\2\2\u00e2\u00e3\7n\2\2\u00e3\u00e4\7u\2\2\u00e4\u00e5\7g\2\2\u00e5"+
		"`\3\2\2\2\u00e6\u00e7\7V\2\2\u00e7b\3\2\2\2\u00e8\u00e9\7v\2\2\u00e9d"+
		"\3\2\2\2\u00ea\u00eb\7\\\2\2\u00ebf\3\2\2\2\5\2\u0096\u00ba\3\2\4\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}