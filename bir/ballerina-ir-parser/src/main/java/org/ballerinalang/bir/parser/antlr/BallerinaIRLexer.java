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
            PACKAGE = 1, TYPE = 2, STRING = 3, INT = 4, FUNCTION = 5, BB = 6, LEFT_BRACE = 7, RIGHT_BRACE = 8,
            QUOTE = 9, LEFT_PARENTHESIS = 10, RIGHT_PARENTHESIS = 11, Identifier = 12, WS = 13,
            NEW_LINE = 14, LINE_COMMENT = 15;
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    public static final String[] ruleNames = {
            "PACKAGE", "TYPE", "STRING", "INT", "FUNCTION", "BB", "LEFT_BRACE", "RIGHT_BRACE",
            "QUOTE", "LEFT_PARENTHESIS", "RIGHT_PARENTHESIS", "Identifier", "IdentifierLiteralChar",
            "IdentifierLiteralEscapeSequence", "UnicodeEscape", "HexDigit", "Int",
            "WS", "NEW_LINE", "LINE_COMMENT"
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
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\21\u009c\b\1\4\2" +
            "\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4" +
            "\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22" +
            "\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3" +
            "\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6" +
            "\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3" +
            "\n\3\13\3\13\3\f\3\f\3\r\3\r\6\r^\n\r\r\r\16\r_\3\r\3\r\3\16\3\16\5\16" +
            "f\n\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17o\n\17\3\20\3\20\3\20\3" +
            "\20\3\20\3\20\3\20\3\21\3\21\3\22\3\22\7\22|\n\22\f\22\16\22\177\13\22" +
            "\3\22\5\22\u0082\n\22\3\23\6\23\u0085\n\23\r\23\16\23\u0086\3\23\3\23" +
            "\3\24\6\24\u008c\n\24\r\24\16\24\u008d\3\24\3\24\3\25\3\25\3\25\3\25\7" +
            "\25\u0096\n\25\f\25\16\25\u0099\13\25\3\25\3\25\2\2\26\3\3\5\4\7\5\t\6" +
            "\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\2\35\2\37\2!\2#\2%\17\'\20" +
            ")\21\3\2\13\7\2\n\f\16\17$$^^~~\6\2$$\61\61^^~~\7\2ddhhppttvv\5\2\62;" +
            "CHch\3\2\63;\3\2\62;\4\2\13\13\"\"\4\2\f\f\16\17\4\2\f\f\17\17\u009f\2" +
            "\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2" +
            "\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2" +
            "\31\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\3+\3\2\2\2\5\63\3\2\2\2" +
            "\78\3\2\2\2\t?\3\2\2\2\13C\3\2\2\2\rL\3\2\2\2\17Q\3\2\2\2\21S\3\2\2\2" +
            "\23U\3\2\2\2\25W\3\2\2\2\27Y\3\2\2\2\31[\3\2\2\2\33e\3\2\2\2\35n\3\2\2" +
            "\2\37p\3\2\2\2!w\3\2\2\2#\u0081\3\2\2\2%\u0084\3\2\2\2\'\u008b\3\2\2\2" +
            ")\u0091\3\2\2\2+,\7r\2\2,-\7c\2\2-.\7e\2\2./\7m\2\2/\60\7c\2\2\60\61\7" +
            "i\2\2\61\62\7g\2\2\62\4\3\2\2\2\63\64\7v\2\2\64\65\7{\2\2\65\66\7r\2\2" +
            "\66\67\7g\2\2\67\6\3\2\2\289\7u\2\29:\7v\2\2:;\7t\2\2;<\7k\2\2<=\7p\2" +
            "\2=>\7i\2\2>\b\3\2\2\2?@\7k\2\2@A\7p\2\2AB\7v\2\2B\n\3\2\2\2CD\7h\2\2" +
            "DE\7w\2\2EF\7p\2\2FG\7e\2\2GH\7v\2\2HI\7k\2\2IJ\7q\2\2JK\7p\2\2K\f\3\2" +
            "\2\2LM\7d\2\2MN\7d\2\2NO\3\2\2\2OP\5#\22\2P\16\3\2\2\2QR\7}\2\2R\20\3" +
            "\2\2\2ST\7\177\2\2T\22\3\2\2\2UV\7$\2\2V\24\3\2\2\2WX\7*\2\2X\26\3\2\2" +
            "\2YZ\7+\2\2Z\30\3\2\2\2[]\7$\2\2\\^\5\33\16\2]\\\3\2\2\2^_\3\2\2\2_]\3" +
            "\2\2\2_`\3\2\2\2`a\3\2\2\2ab\7$\2\2b\32\3\2\2\2cf\n\2\2\2df\5\35\17\2" +
            "ec\3\2\2\2ed\3\2\2\2f\34\3\2\2\2gh\7^\2\2ho\t\3\2\2ij\7^\2\2jk\7^\2\2" +
            "kl\3\2\2\2lo\t\4\2\2mo\5\37\20\2ng\3\2\2\2ni\3\2\2\2nm\3\2\2\2o\36\3\2" +
            "\2\2pq\7^\2\2qr\7w\2\2rs\5!\21\2st\5!\21\2tu\5!\21\2uv\5!\21\2v \3\2\2" +
            "\2wx\t\5\2\2x\"\3\2\2\2y}\t\6\2\2z|\t\7\2\2{z\3\2\2\2|\177\3\2\2\2}{\3" +
            "\2\2\2}~\3\2\2\2~\u0082\3\2\2\2\177}\3\2\2\2\u0080\u0082\7\62\2\2\u0081" +
            "y\3\2\2\2\u0081\u0080\3\2\2\2\u0082$\3\2\2\2\u0083\u0085\t\b\2\2\u0084" +
            "\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2" +
            "\2\2\u0087\u0088\3\2\2\2\u0088\u0089\b\23\2\2\u0089&\3\2\2\2\u008a\u008c" +
            "\t\t\2\2\u008b\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008b\3\2\2\2\u008d" +
            "\u008e\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\b\24\2\2\u0090(\3\2\2\2" +
            "\u0091\u0092\7\61\2\2\u0092\u0093\7\61\2\2\u0093\u0097\3\2\2\2\u0094\u0096" +
            "\n\n\2\2\u0095\u0094\3\2\2\2\u0096\u0099\3\2\2\2\u0097\u0095\3\2\2\2\u0097" +
            "\u0098\3\2\2\2\u0098\u009a\3\2\2\2\u0099\u0097\3\2\2\2\u009a\u009b\b\25" +
            "\2\2\u009b*\3\2\2\2\13\2_en}\u0081\u0086\u008d\u0097\3\2\3\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}