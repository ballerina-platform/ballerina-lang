// Generated from BallerinaIRLexer.g4 by ANTLR 4.5.3
package org.ballerinalang.bir.parser.antlr;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class BallerinaIRLexer extends Lexer {
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
	public static String[] modeNames = {
			"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
			"PACKAGE", "TYPE", "FUNCTION", "STRING", "INT", "BB", "GOTO", "RETURN",
			"LEFT_BRACE", "RIGHT_BRACE", "QUOTE", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS",
			"SEMICOLON", "Identifier", "IdentifierLiteralChar", "IdentifierLiteralEscapeSequence",
			"UnicodeEscape", "HexDigit", "Int", "WS", "NEW_LINE", "LINE_COMMENT"
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


	public BallerinaIRLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
	}

	@Override
	public String getGrammarFileName() {
		return "BallerinaIRLexer.g4";
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
	public String[] getModeNames() {
		return modeNames;
	}

	@Override
	public ATN getATN() {
		return _ATN;
	}

	public static final String _serializedATN =
			"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\24\u00b0\b\1\4\2" +
			"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4" +
			"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22" +
			"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2" +
			"\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3" +
			"\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7" +
			"\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13" +
			"\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\6\20r\n\20\r\20\16" +
			"\20s\3\20\3\20\3\21\3\21\5\21z\n\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22" +
			"\5\22\u0083\n\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25" +
			"\7\25\u0090\n\25\f\25\16\25\u0093\13\25\3\25\5\25\u0096\n\25\3\26\6\26" +
			"\u0099\n\26\r\26\16\26\u009a\3\26\3\26\3\27\6\27\u00a0\n\27\r\27\16\27" +
			"\u00a1\3\27\3\27\3\30\3\30\3\30\3\30\7\30\u00aa\n\30\f\30\16\30\u00ad" +
			"\13\30\3\30\3\30\2\2\31\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f" +
			"\27\r\31\16\33\17\35\20\37\21!\2#\2%\2\'\2)\2+\22-\23/\24\3\2\13\7\2\n" +
			"\f\16\17$$^^~~\6\2$$\61\61^^~~\7\2ddhhppttvv\5\2\62;CHch\3\2\63;\3\2\62" +
			";\4\2\13\13\"\"\4\2\f\f\16\17\4\2\f\f\17\17\u00b3\2\3\3\2\2\2\2\5\3\2" +
			"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21" +
			"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2" +
			"\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\3\61\3" +
			"\2\2\2\59\3\2\2\2\7>\3\2\2\2\tG\3\2\2\2\13N\3\2\2\2\rR\3\2\2\2\17W\3\2" +
			"\2\2\21\\\3\2\2\2\23c\3\2\2\2\25e\3\2\2\2\27g\3\2\2\2\31i\3\2\2\2\33k" +
			"\3\2\2\2\35m\3\2\2\2\37o\3\2\2\2!y\3\2\2\2#\u0082\3\2\2\2%\u0084\3\2\2" +
			"\2\'\u008b\3\2\2\2)\u0095\3\2\2\2+\u0098\3\2\2\2-\u009f\3\2\2\2/\u00a5" +
			"\3\2\2\2\61\62\7r\2\2\62\63\7c\2\2\63\64\7e\2\2\64\65\7m\2\2\65\66\7c" +
			"\2\2\66\67\7i\2\2\678\7g\2\28\4\3\2\2\29:\7v\2\2:;\7{\2\2;<\7r\2\2<=\7" +
			"g\2\2=\6\3\2\2\2>?\7h\2\2?@\7w\2\2@A\7p\2\2AB\7e\2\2BC\7v\2\2CD\7k\2\2" +
			"DE\7q\2\2EF\7p\2\2F\b\3\2\2\2GH\7u\2\2HI\7v\2\2IJ\7t\2\2JK\7k\2\2KL\7" +
			"p\2\2LM\7i\2\2M\n\3\2\2\2NO\7k\2\2OP\7p\2\2PQ\7v\2\2Q\f\3\2\2\2RS\7d\2" +
			"\2ST\7d\2\2TU\3\2\2\2UV\5)\25\2V\16\3\2\2\2WX\7i\2\2XY\7q\2\2YZ\7v\2\2" +
			"Z[\7q\2\2[\20\3\2\2\2\\]\7t\2\2]^\7g\2\2^_\7v\2\2_`\7w\2\2`a\7t\2\2ab" +
			"\7p\2\2b\22\3\2\2\2cd\7}\2\2d\24\3\2\2\2ef\7\177\2\2f\26\3\2\2\2gh\7$" +
			"\2\2h\30\3\2\2\2ij\7*\2\2j\32\3\2\2\2kl\7+\2\2l\34\3\2\2\2mn\7=\2\2n\36" +
			"\3\2\2\2oq\7$\2\2pr\5!\21\2qp\3\2\2\2rs\3\2\2\2sq\3\2\2\2st\3\2\2\2tu" +
			"\3\2\2\2uv\7$\2\2v \3\2\2\2wz\n\2\2\2xz\5#\22\2yw\3\2\2\2yx\3\2\2\2z\"" +
			"\3\2\2\2{|\7^\2\2|\u0083\t\3\2\2}~\7^\2\2~\177\7^\2\2\177\u0080\3\2\2" +
			"\2\u0080\u0083\t\4\2\2\u0081\u0083\5%\23\2\u0082{\3\2\2\2\u0082}\3\2\2" +
			"\2\u0082\u0081\3\2\2\2\u0083$\3\2\2\2\u0084\u0085\7^\2\2\u0085\u0086\7" +
			"w\2\2\u0086\u0087\5\'\24\2\u0087\u0088\5\'\24\2\u0088\u0089\5\'\24\2\u0089" +
			"\u008a\5\'\24\2\u008a&\3\2\2\2\u008b\u008c\t\5\2\2\u008c(\3\2\2\2\u008d" +
			"\u0091\t\6\2\2\u008e\u0090\t\7\2\2\u008f\u008e\3\2\2\2\u0090\u0093\3\2" +
			"\2\2\u0091\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0096\3\2\2\2\u0093" +
			"\u0091\3\2\2\2\u0094\u0096\7\62\2\2\u0095\u008d\3\2\2\2\u0095\u0094\3" +
			"\2\2\2\u0096*\3\2\2\2\u0097\u0099\t\b\2\2\u0098\u0097\3\2\2\2\u0099\u009a" +
			"\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b\u009c\3\2\2\2\u009c" +
			"\u009d\b\26\2\2\u009d,\3\2\2\2\u009e\u00a0\t\t\2\2\u009f\u009e\3\2\2\2" +
			"\u00a0\u00a1\3\2\2\2\u00a1\u009f\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a3" +
			"\3\2\2\2\u00a3\u00a4\b\27\2\2\u00a4.\3\2\2\2\u00a5\u00a6\7\61\2\2\u00a6" +
			"\u00a7\7\61\2\2\u00a7\u00ab\3\2\2\2\u00a8\u00aa\n\n\2\2\u00a9\u00a8\3" +
			"\2\2\2\u00aa\u00ad\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac" +
			"\u00ae\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ae\u00af\b\30\2\2\u00af\60\3\2\2" +
			"\2\13\2sy\u0082\u0091\u0095\u009a\u00a1\u00ab\3\2\3\2";
	public static final ATN _ATN =
			new ATNDeserializer().deserialize(_serializedATN.toCharArray());

	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}