// Generated from NEL.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('antlr4/index');
var NELListener = require('./NELListener').NELListener;
var grammarFileName = "NEL.g4";

var serializedATN = ["\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd",
    "\3\u008e\u029f\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t",
    "\b\4\t\t\t\4\n\t\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20",
    "\t\20\4\21\t\21\4\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4",
    "\27\t\27\4\30\t\30\4\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35",
    "\4\36\t\36\4\37\t\37\4 \t \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'",
    "\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61",
    "\t\61\4\62\t\62\4\63\t\63\4\64\t\64\4\65\t\65\4\66\t\66\4\67\t\67\4",
    "8\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t=\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C",
    "\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I\tI\3\2\3\2\5\2\u0095\n\2\3\2\5",
    "\2\u0098\n\2\3\2\3\2\3\2\3\3\5\3\u009e\n\3\3\3\3\3\5\3\u00a2\n\3\3\3",
    "\3\3\3\4\7\4\u00a7\n\4\f\4\16\4\u00aa\13\4\3\5\7\5\u00ad\n\5\f\5\16",
    "\5\u00b0\13\5\3\6\6\6\u00b3\n\6\r\6\16\6\u00b4\3\7\3\7\3\7\3\7\3\b\3",
    "\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3",
    "\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\5\f\u00d7\n\f\3\r\3\r\3",
    "\r\3\r\5\r\u00dd\n\r\3\16\3\16\3\16\3\16\5\16\u00e3\n\16\3\17\3\17\3",
    "\17\3\17\5\17\u00e9\n\17\3\20\3\20\3\20\3\20\5\20\u00ef\n\20\3\21\3",
    "\21\3\21\3\21\5\21\u00f5\n\21\3\21\5\21\u00f8\n\21\3\22\3\22\3\22\3",
    "\22\5\22\u00fe\n\22\3\22\5\22\u0101\n\22\3\23\3\23\3\23\3\23\3\23\5",
    "\23\u0108\n\23\3\23\5\23\u010b\n\23\3\24\3\24\3\24\3\24\5\24\u0111\n",
    "\24\3\24\3\24\3\25\3\25\3\25\7\25\u0118\n\25\f\25\16\25\u011b\13\25",
    "\3\26\3\26\3\26\3\26\3\26\5\26\u0122\n\26\3\26\3\26\3\27\3\27\3\27\7",
    "\27\u0129\n\27\f\27\16\27\u012c\13\27\3\30\3\30\3\30\3\30\5\30\u0132",
    "\n\30\3\30\3\30\5\30\u0136\n\30\5\30\u0138\n\30\3\31\3\31\3\31\3\31",
    "\3\32\3\32\3\32\5\32\u0141\n\32\3\32\3\32\3\32\5\32\u0146\n\32\3\32",
    "\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3",
    "\36\3\36\3\36\6\36\u0159\n\36\r\36\16\36\u015a\3\37\3\37\3\37\3\37\7",
    "\37\u0161\n\37\f\37\16\37\u0164\13\37\3\37\3\37\3 \3 \3 \3 \3!\3!\3",
    "!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"",
    "\u017f\n\"\3\"\3\"\3\"\5\"\u0184\n\"\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3",
    "#\5#\u0191\n#\3$\3$\3$\3$\3%\3%\3&\3&\5&\u019b\n&\3&\5&\u019e\n&\3&",
    "\5&\u01a1\n&\3&\5&\u01a4\n&\3&\3&\3&\3\'\3\'\3\'\3\'\3\'\7\'\u01ae\n",
    "\'\f\'\16\'\u01b1\13\'\3(\3(\3(\7(\u01b6\n(\f(\16(\u01b9\13(\3)\3)\3",
    ")\3)\3)\3)\3)\3)\3*\3*\3+\3+\7+\u01c7\n+\f+\16+\u01ca\13+\3+\3+\3,\3",
    ",\3,\3,\3,\3,\3,\3,\5,\u01d6\n,\3-\3-\6-\u01da\n-\r-\16-\u01db\3.\3",
    ".\3.\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\61\3\61\3\62\3\62\5\62\u01ee",
    "\n\62\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65\5",
    "\65\u01fc\n\65\3\65\3\65\3\65\5\65\u0201\n\65\3\66\3\66\3\66\3\66\3",
    "\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66",
    "\3\66\3\66\3\66\3\66\3\66\5\66\u021a\n\66\3\67\3\67\3\67\3\67\3\67\3",
    "\67\3\67\3\67\3\67\3\67\3\67\3\67\3\67\3\67\3\67\5\67\u022b\n\67\38",
    "\38\38\39\39\39\39\59\u0234\n9\39\39\3:\3:\3:\5:\u023b\n:\3:\3:\3;\5",
    ";\u0240\n;\3;\5;\u0243\n;\3;\3;\3;\3<\3<\3<\5<\u024b\n<\3<\3<\3=\3=",
    "\3=\5=\u0252\n=\3=\3=\3>\3>\3>\7>\u0259\n>\f>\16>\u025c\13>\3?\3?\5",
    "?\u0260\n?\3?\3?\3?\5?\u0265\n?\3@\3@\3@\3@\3@\3@\3@\3@\3@\3@\3A\3A",
    "\3A\5A\u0274\nA\3A\3A\3B\3B\3B\3B\5B\u027c\nB\3B\3B\3C\3C\5C\u0282\n",
    "C\3D\3D\3D\3D\3D\3D\3D\3D\3D\3D\3D\3E\3E\3F\3F\3G\3G\3H\3H\3I\3I\3I",
    "\7I\u029a\nI\fI\16I\u029d\13I\3I\2\2J\2\4\6\b\n\f\16\20\22\24\26\30",
    "\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~",
    "\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\2\t\4\2XX\\\\",
    "\3\2\30\33\4\2hins\3\2X]\3\2\"#\13\2$$\'\')),,\63\6399@@BBJJ\4\2\27",
    "\27\34\34\u02a1\2\u0092\3\2\2\2\4\u009d\3\2\2\2\6\u00a8\3\2\2\2\b\u00ae",
    "\3\2\2\2\n\u00b2\3\2\2\2\f\u00b6\3\2\2\2\16\u00ba\3\2\2\2\20\u00c0\3",
    "\2\2\2\22\u00c6\3\2\2\2\24\u00cc\3\2\2\2\26\u00d2\3\2\2\2\30\u00d8\3",
    "\2\2\2\32\u00de\3\2\2\2\34\u00e4\3\2\2\2\36\u00ea\3\2\2\2 \u00f0\3\2",
    "\2\2\"\u00f9\3\2\2\2$\u0102\3\2\2\2&\u010c\3\2\2\2(\u0114\3\2\2\2*\u011c",
    "\3\2\2\2,\u0125\3\2\2\2.\u0137\3\2\2\2\60\u0139\3\2\2\2\62\u0140\3\2",
    "\2\2\64\u0149\3\2\2\2\66\u014d\3\2\2\28\u0151\3\2\2\2:\u0155\3\2\2\2",
    "<\u015c\3\2\2\2>\u0167\3\2\2\2@\u016b\3\2\2\2B\u0183\3\2\2\2D\u0190",
    "\3\2\2\2F\u0192\3\2\2\2H\u0196\3\2\2\2J\u0198\3\2\2\2L\u01af\3\2\2\2",
    "N\u01b2\3\2\2\2P\u01ba\3\2\2\2R\u01c2\3\2\2\2T\u01c4\3\2\2\2V\u01d5",
    "\3\2\2\2X\u01d7\3\2\2\2Z\u01dd\3\2\2\2\\\u01e0\3\2\2\2^\u01e6\3\2\2",
    "\2`\u01e9\3\2\2\2b\u01eb\3\2\2\2d\u01ef\3\2\2\2f\u01f3\3\2\2\2h\u0200",
    "\3\2\2\2j\u0219\3\2\2\2l\u022a\3\2\2\2n\u022c\3\2\2\2p\u022f\3\2\2\2",
    "r\u0237\3\2\2\2t\u023f\3\2\2\2v\u0247\3\2\2\2x\u024e\3\2\2\2z\u0255",
    "\3\2\2\2|\u025f\3\2\2\2~\u0266\3\2\2\2\u0080\u0270\3\2\2\2\u0082\u0277",
    "\3\2\2\2\u0084\u0281\3\2\2\2\u0086\u0283\3\2\2\2\u0088\u028e\3\2\2\2",
    "\u008a\u0290\3\2\2\2\u008c\u0292\3\2\2\2\u008e\u0294\3\2\2\2\u0090\u0296",
    "\3\2\2\2\u0092\u0094\5\4\3\2\u0093\u0095\5\6\4\2\u0094\u0093\3\2\2\2",
    "\u0094\u0095\3\2\2\2\u0095\u0097\3\2\2\2\u0096\u0098\5\b\5\2\u0097\u0096",
    "\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009a\5\n\6\2",
    "\u009a\u009b\7\2\2\3\u009b\3\3\2\2\2\u009c\u009e\5\16\b\2\u009d\u009c",
    "\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u00a1\5\20\t",
    "\2\u00a0\u00a2\5\22\n\2\u00a1\u00a0\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2",
    "\u00a3\3\2\2\2\u00a3\u00a4\5\f\7\2\u00a4\5\3\2\2\2\u00a5\u00a7\5B\"",
    "\2\u00a6\u00a5\3\2\2\2\u00a7\u00aa\3\2\2\2\u00a8\u00a6\3\2\2\2\u00a8",
    "\u00a9\3\2\2\2\u00a9\7\3\2\2\2\u00aa\u00a8\3\2\2\2\u00ab\u00ad\5D#\2",
    "\u00ac\u00ab\3\2\2\2\u00ad\u00b0\3\2\2\2\u00ae\u00ac\3\2\2\2\u00ae\u00af",
    "\3\2\2\2\u00af\t\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b1\u00b3\5J&\2\u00b2",
    "\u00b1\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b4\u00b5\3",
    "\2\2\2\u00b5\13\3\2\2\2\u00b6\u00b7\7E\2\2\u00b7\u00b8\5N(\2\u00b8\u00b9",
    "\7d\2\2\u00b9\r\3\2\2\2\u00ba\u00bb\7\u008a\2\2\u00bb\u00bc\7\3\2\2",
    "\u00bc\u00bd\7^\2\2\u00bd\u00be\7\\\2\2\u00be\u00bf\7_\2\2\u00bf\17",
    "\3\2\2\2\u00c0\u00c1\7\u008a\2\2\u00c1\u00c2\7\4\2\2\u00c2\u00c3\7^",
    "\2\2\u00c3\u00c4\5.\30\2\u00c4\u00c5\7_\2\2\u00c5\21\3\2\2\2\u00c6\u00c7",
    "\7\u008a\2\2\u00c7\u00c8\7\5\2\2\u00c8\u00c9\7^\2\2\u00c9\u00ca\5\62",
    "\32\2\u00ca\u00cb\7_\2\2\u00cb\23\3\2\2\2\u00cc\u00cd\7\u008a\2\2\u00cd",
    "\u00ce\7\3\2\2\u00ce\u00cf\7^\2\2\u00cf\u00d0\7\\\2\2\u00d0\u00d1\7",
    "_\2\2\u00d1\25\3\2\2\2\u00d2\u00d3\7\u008a\2\2\u00d3\u00d6\7\6\2\2\u00d4",
    "\u00d5\7^\2\2\u00d5\u00d7\7_\2\2\u00d6\u00d4\3\2\2\2\u00d6\u00d7\3\2",
    "\2\2\u00d7\27\3\2\2\2\u00d8\u00d9\7\u008a\2\2\u00d9\u00dc\7\7\2\2\u00da",
    "\u00db\7^\2\2\u00db\u00dd\7_\2\2\u00dc\u00da\3\2\2\2\u00dc\u00dd\3\2",
    "\2\2\u00dd\31\3\2\2\2\u00de\u00df\7\u008a\2\2\u00df\u00e2\7\b\2\2\u00e0",
    "\u00e1\7^\2\2\u00e1\u00e3\7_\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2",
    "\2\2\u00e3\33\3\2\2\2\u00e4\u00e5\7\u008a\2\2\u00e5\u00e8\7\t\2\2\u00e6",
    "\u00e7\7^\2\2\u00e7\u00e9\7_\2\2\u00e8\u00e6\3\2\2\2\u00e8\u00e9\3\2",
    "\2\2\u00e9\35\3\2\2\2\u00ea\u00eb\7\u008a\2\2\u00eb\u00ee\7\n\2\2\u00ec",
    "\u00ed\7^\2\2\u00ed\u00ef\7_\2\2\u00ee\u00ec\3\2\2\2\u00ee\u00ef\3\2",
    "\2\2\u00ef\37\3\2\2\2\u00f0\u00f1\7\u008a\2\2\u00f1\u00f7\7\13\2\2\u00f2",
    "\u00f4\7^\2\2\u00f3\u00f5\5H%\2\u00f4\u00f3\3\2\2\2\u00f4\u00f5\3\2",
    "\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f8\7_\2\2\u00f7\u00f2\3\2\2\2\u00f7",
    "\u00f8\3\2\2\2\u00f8!\3\2\2\2\u00f9\u00fa\7\u008a\2\2\u00fa\u0100\7",
    "\f\2\2\u00fb\u00fd\7^\2\2\u00fc\u00fe\5H%\2\u00fd\u00fc\3\2\2\2\u00fd",
    "\u00fe\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0101\7_\2\2\u0100\u00fb\3",
    "\2\2\2\u0100\u0101\3\2\2\2\u0101#\3\2\2\2\u0102\u0103\7\u008a\2\2\u0103",
    "\u010a\7\r\2\2\u0104\u0107\7^\2\2\u0105\u0108\5,\27\2\u0106\u0108\5",
    "H%\2\u0107\u0105\3\2\2\2\u0107\u0106\3\2\2\2\u0107\u0108\3\2\2\2\u0108",
    "\u0109\3\2\2\2\u0109\u010b\7_\2\2\u010a\u0104\3\2\2\2\u010a\u010b\3",
    "\2\2\2\u010b%\3\2\2\2\u010c\u010d\7\u008a\2\2\u010d\u010e\7\16\2\2\u010e",
    "\u0110\7^\2\2\u010f\u0111\5(\25\2\u0110\u010f\3\2\2\2\u0110\u0111\3",
    "\2\2\2\u0111\u0112\3\2\2\2\u0112\u0113\7_\2\2\u0113\'\3\2\2\2\u0114",
    "\u0119\5*\26\2\u0115\u0116\7e\2\2\u0116\u0118\5*\26\2\u0117\u0115\3",
    "\2\2\2\u0118\u011b\3\2\2\2\u0119\u0117\3\2\2\2\u0119\u011a\3\2\2\2\u011a",
    ")\3\2\2\2\u011b\u0119\3\2\2\2\u011c\u011d\7\u008a\2\2\u011d\u011e\7",
    "\17\2\2\u011e\u0121\7^\2\2\u011f\u0122\5,\27\2\u0120\u0122\5H%\2\u0121",
    "\u011f\3\2\2\2\u0121\u0120\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123\3",
    "\2\2\2\u0123\u0124\7_\2\2\u0124+\3\2\2\2\u0125\u012a\5F$\2\u0126\u0127",
    "\7e\2\2\u0127\u0129\5F$\2\u0128\u0126\3\2\2\2\u0129\u012c\3\2\2\2\u012a",
    "\u0128\3\2\2\2\u012a\u012b\3\2\2\2\u012b-\3\2\2\2\u012c\u012a\3\2\2",
    "\2\u012d\u0138\5\60\31\2\u012e\u0131\5\64\33\2\u012f\u0130\7e\2\2\u0130",
    "\u0132\5\66\34\2\u0131\u012f\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0135",
    "\3\2\2\2\u0133\u0134\7e\2\2\u0134\u0136\58\35\2\u0135\u0133\3\2\2\2",
    "\u0135\u0136\3\2\2\2\u0136\u0138\3\2\2\2\u0137\u012d\3\2\2\2\u0137\u012e",
    "\3\2\2\2\u0138/\3\2\2\2\u0139\u013a\7A\2\2\u013a\u013b\7g\2\2\u013b",
    "\u013c\7\\\2\2\u013c\61\3\2\2\2\u013d\u013e\5:\36\2\u013e\u013f\7e\2",
    "\2\u013f\u0141\3\2\2\2\u0140\u013d\3\2\2\2\u0140\u0141\3\2\2\2\u0141",
    "\u0145\3\2\2\2\u0142\u0143\5> \2\u0143\u0144\7e\2\2\u0144\u0146\3\2",
    "\2\2\u0145\u0142\3\2\2\2\u0145\u0146\3\2\2\2\u0146\u0147\3\2\2\2\u0147",
    "\u0148\5@!\2\u0148\63\3\2\2\2\u0149\u014a\7\20\2\2\u014a\u014b\7g\2",
    "\2\u014b\u014c\7\\\2\2\u014c\65\3\2\2\2\u014d\u014e\7\21\2\2\u014e\u014f",
    "\7g\2\2\u014f\u0150\7\\\2\2\u0150\67\3\2\2\2\u0151\u0152\7\22\2\2\u0152",
    "\u0153\7g\2\2\u0153\u0154\7X\2\2\u01549\3\2\2\2\u0155\u0156\7\23\2\2",
    "\u0156\u0158\7g\2\2\u0157\u0159\5<\37\2\u0158\u0157\3\2\2\2\u0159\u015a",
    "\3\2\2\2\u015a\u0158\3\2\2\2\u015a\u015b\3\2\2\2\u015b;\3\2\2\2\u015c",
    "\u015d\7`\2\2\u015d\u0162\7\\\2\2\u015e\u015f\7e\2\2\u015f\u0161\7\\",
    "\2\2\u0160\u015e\3\2\2\2\u0161\u0164\3\2\2\2\u0162\u0160\3\2\2\2\u0162",
    "\u0163\3\2\2\2\u0163\u0165\3\2\2\2\u0164\u0162\3\2\2\2\u0165\u0166\7",
    "a\2\2\u0166=\3\2\2\2\u0167\u0168\7\24\2\2\u0168\u0169\7g\2\2\u0169\u016a",
    "\7\\\2\2\u016a?\3\2\2\2\u016b\u016c\7\25\2\2\u016c\u016d\7g\2\2\u016d",
    "\u016e\5\u008aF\2\u016eA\3\2\2\2\u016f\u0170\7\60\2\2\u0170\u0171\5",
    "\u008cG\2\u0171\u0172\7\u0089\2\2\u0172\u0173\7g\2\2\u0173\u0174\5\u0088",
    "E\2\u0174\u0175\7d\2\2\u0175\u0184\3\2\2\2\u0176\u0177\7\60\2\2\u0177",
    "\u0178\5\u008eH\2\u0178\u0179\7\u0089\2\2\u0179\u017a\7g\2\2\u017a\u017b",
    "\7D\2\2\u017b\u017c\7\u0089\2\2\u017c\u017e\7^\2\2\u017d\u017f\7\\\2",
    "\2\u017e\u017d\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u0180\3\2\2\2\u0180",
    "\u0181\7_\2\2\u0181\u0182\7d\2\2\u0182\u0184\3\2\2\2\u0183\u016f\3\2",
    "\2\2\u0183\u0176\3\2\2\2\u0184C\3\2\2\2\u0185\u0186\5\u008cG\2\u0186",
    "\u0187\7\u0089\2\2\u0187\u0188\7g\2\2\u0188\u0189\5\u0088E\2\u0189\u018a",
    "\7d\2\2\u018a\u0191\3\2\2\2\u018b\u018c\5t;\2\u018c\u018d\7g\2\2\u018d",
    "\u018e\5p9\2\u018e\u018f\7d\2\2\u018f\u0191\3\2\2\2\u0190\u0185\3\2",
    "\2\2\u0190\u018b\3\2\2\2\u0191E\3\2\2\2\u0192\u0193\7\u0089\2\2\u0193",
    "\u0194\7g\2\2\u0194\u0195\5H%\2\u0195G\3\2\2\2\u0196\u0197\t\2\2\2\u0197",
    "I\3\2\2\2\u0198\u019a\5L\'\2\u0199\u019b\5 \21\2\u019a\u0199\3\2\2\2",
    "\u019a\u019b\3\2\2\2\u019b\u019d\3\2\2\2\u019c\u019e\5\"\22\2\u019d",
    "\u019c\3\2\2\2\u019d\u019e\3\2\2\2\u019e\u01a0\3\2\2\2\u019f\u01a1\5",
    "$\23\2\u01a0\u019f\3\2\2\2\u01a0\u01a1\3\2\2\2\u01a1\u01a3\3\2\2\2\u01a2",
    "\u01a4\5&\24\2\u01a3\u01a2\3\2\2\2\u01a3\u01a4\3\2\2\2\u01a4\u01a5\3",
    "\2\2\2\u01a5\u01a6\5\24\13\2\u01a6\u01a7\5P)\2\u01a7K\3\2\2\2\u01a8",
    "\u01ae\5\26\f\2\u01a9\u01ae\5\30\r\2\u01aa\u01ae\5\32\16\2\u01ab\u01ae",
    "\5\34\17\2\u01ac\u01ae\5\36\20\2\u01ad\u01a8\3\2\2\2\u01ad\u01a9\3\2",
    "\2\2\u01ad\u01aa\3\2\2\2\u01ad\u01ab\3\2\2\2\u01ad\u01ac\3\2\2\2\u01ae",
    "\u01b1\3\2\2\2\u01af\u01ad\3\2\2\2\u01af\u01b0\3\2\2\2\u01b0M\3\2\2",
    "\2\u01b1\u01af\3\2\2\2\u01b2\u01b7\7\u0089\2\2\u01b3\u01b4\7f\2\2\u01b4",
    "\u01b6\7\u0089\2\2\u01b5\u01b3\3\2\2\2\u01b6\u01b9\3\2\2\2\u01b7\u01b5",
    "\3\2\2\2\u01b7\u01b8\3\2\2\2\u01b8O\3\2\2\2\u01b9\u01b7\3\2\2\2\u01ba",
    "\u01bb\7\26\2\2\u01bb\u01bc\5R*\2\u01bc\u01bd\7^\2\2\u01bd\u01be\7\27",
    "\2\2\u01be\u01bf\7\u0089\2\2\u01bf\u01c0\7_\2\2\u01c0\u01c1\5T+\2\u01c1",
    "Q\3\2\2\2\u01c2\u01c3\7\u0089\2\2\u01c3S\3\2\2\2\u01c4\u01c8\7`\2\2",
    "\u01c5\u01c7\5V,\2\u01c6\u01c5\3\2\2\2\u01c7\u01ca\3\2\2\2\u01c8\u01c6",
    "\3\2\2\2\u01c8\u01c9\3\2\2\2\u01c9\u01cb\3\2\2\2\u01ca\u01c8\3\2\2\2",
    "\u01cb\u01cc\7a\2\2\u01ccU\3\2\2\2\u01cd\u01d6\5h\65\2\u01ce\u01d6\5",
    "j\66\2\u01cf\u01d6\5l\67\2\u01d0\u01d6\5~@\2\u01d1\u01d6\5\u0080A\2",
    "\u01d2\u01d6\5n8\2\u01d3\u01d6\5X-\2\u01d4\u01d6\5b\62\2\u01d5\u01cd",
    "\3\2\2\2\u01d5\u01ce\3\2\2\2\u01d5\u01cf\3\2\2\2\u01d5\u01d0\3\2\2\2",
    "\u01d5\u01d1\3\2\2\2\u01d5\u01d2\3\2\2\2\u01d5\u01d3\3\2\2\2\u01d5\u01d4",
    "\3\2\2\2\u01d6W\3\2\2\2\u01d7\u01d9\5Z.\2\u01d8\u01da\5\\/\2\u01d9\u01d8",
    "\3\2\2\2\u01da\u01db\3\2\2\2\u01db\u01d9\3\2\2\2\u01db\u01dc\3\2\2\2",
    "\u01dcY\3\2\2\2\u01dd\u01de\7T\2\2\u01de\u01df\5T+\2\u01df[\3\2\2\2",
    "\u01e0\u01e1\7+\2\2\u01e1\u01e2\7^\2\2\u01e2\u01e3\5^\60\2\u01e3\u01e4",
    "\7_\2\2\u01e4\u01e5\5T+\2\u01e5]\3\2\2\2\u01e6\u01e7\5`\61\2\u01e7\u01e8",
    "\7\u0089\2\2\u01e8_\3\2\2\2\u01e9\u01ea\t\3\2\2\u01eaa\3\2\2\2\u01eb",
    "\u01ed\5d\63\2\u01ec\u01ee\5f\64\2\u01ed\u01ec\3\2\2\2\u01ed\u01ee\3",
    "\2\2\2\u01eec\3\2\2\2\u01ef\u01f0\7;\2\2\u01f0\u01f1\5\u0082B\2\u01f1",
    "\u01f2\5T+\2\u01f2e\3\2\2\2\u01f3\u01f4\7\64\2\2\u01f4\u01f5\5T+\2\u01f5",
    "g\3\2\2\2\u01f6\u01f7\5t;\2\u01f7\u01f8\7d\2\2\u01f8\u0201\3\2\2\2\u01f9",
    "\u01fc\5\u008cG\2\u01fa\u01fc\5\u008eH\2\u01fb\u01f9\3\2\2\2\u01fb\u01fa",
    "\3\2\2\2\u01fc\u01fd\3\2\2\2\u01fd\u01fe\7\u0089\2\2\u01fe\u01ff\7d",
    "\2\2\u01ff\u0201\3\2\2\2\u0200\u01f6\3\2\2\2\u0200\u01fb\3\2\2\2\u0201",
    "i\3\2\2\2\u0202\u0203\5\u008cG\2\u0203\u0204\7\u0089\2\2\u0204\u0205",
    "\7g\2\2\u0205\u0206\5\u0088E\2\u0206\u0207\7d\2\2\u0207\u021a\3\2\2",
    "\2\u0208\u0209\5t;\2\u0209\u020a\7g\2\2\u020a\u020b\5p9\2\u020b\u020c",
    "\7d\2\2\u020c\u021a\3\2\2\2\u020d\u020e\5\u008eH\2\u020e\u020f\7\u0089",
    "\2\2\u020f\u0210\7g\2\2\u0210\u0211\5p9\2\u0211\u0212\7d\2\2\u0212\u021a",
    "\3\2\2\2\u0213\u0214\5\u008eH\2\u0214\u0215\7\u0089\2\2\u0215\u0216",
    "\7g\2\2\u0216\u0217\5r:\2\u0217\u0218\7d\2\2\u0218\u021a\3\2\2\2\u0219",
    "\u0202\3\2\2\2\u0219\u0208\3\2\2\2\u0219\u020d\3\2\2\2\u0219\u0213\3",
    "\2\2\2\u021ak\3\2\2\2\u021b\u021c\7\u0089\2\2\u021c\u021d\7g\2\2\u021d",
    "\u021e\5\u0088E\2\u021e\u021f\7d\2\2\u021f\u022b\3\2\2\2\u0220\u0221",
    "\7\u0089\2\2\u0221\u0222\7g\2\2\u0222\u0223\5p9\2\u0223\u0224\7d\2\2",
    "\u0224\u022b\3\2\2\2\u0225\u0226\7\u0089\2\2\u0226\u0227\7g\2\2\u0227",
    "\u0228\5r:\2\u0228\u0229\7d\2\2\u0229\u022b\3\2\2\2\u022a\u021b\3\2",
    "\2\2\u022a\u0220\3\2\2\2\u022a\u0225\3\2\2\2\u022bm\3\2\2\2\u022c\u022d",
    "\5r:\2\u022d\u022e\7d\2\2\u022eo\3\2\2\2\u022f\u0230\7D\2\2\u0230\u0231",
    "\5\u008eH\2\u0231\u0233\7^\2\2\u0232\u0234\5\u0088E\2\u0233\u0232\3",
    "\2\2\2\u0233\u0234\3\2\2\2\u0234\u0235\3\2\2\2\u0235\u0236\7_\2\2\u0236",
    "q\3\2\2\2\u0237\u0238\7\u0089\2\2\u0238\u023a\7^\2\2\u0239\u023b\5z",
    ">\2\u023a\u0239\3\2\2\2\u023a\u023b\3\2\2\2\u023b\u023c\3\2\2\2\u023c",
    "\u023d\7_\2\2\u023ds\3\2\2\2\u023e\u0240\5v<\2\u023f\u023e\3\2\2\2\u023f",
    "\u0240\3\2\2\2\u0240\u0242\3\2\2\2\u0241\u0243\5x=\2\u0242\u0241\3\2",
    "\2\2\u0242\u0243\3\2\2\2\u0243\u0244\3\2\2\2\u0244\u0245\7\34\2\2\u0245",
    "\u0246\7\u0089\2\2\u0246u\3\2\2\2\u0247\u0248\7\35\2\2\u0248\u024a\7",
    "^\2\2\u0249\u024b\5z>\2\u024a\u0249\3\2\2\2\u024a\u024b\3\2\2\2\u024b",
    "\u024c\3\2\2\2\u024c\u024d\7_\2\2\u024dw\3\2\2\2\u024e\u024f\7\36\2",
    "\2\u024f\u0251\7^\2\2\u0250\u0252\5z>\2\u0251\u0250\3\2\2\2\u0251\u0252",
    "\3\2\2\2\u0252\u0253\3\2\2\2\u0253\u0254\7_\2\2\u0254y\3\2\2\2\u0255",
    "\u025a\5|?\2\u0256\u0257\7e\2\2\u0257\u0259\5|?\2\u0258\u0256\3\2\2",
    "\2\u0259\u025c\3\2\2\2\u025a\u0258\3\2\2\2\u025a\u025b\3\2\2\2\u025b",
    "{\3\2\2\2\u025c\u025a\3\2\2\2\u025d\u0260\7\u0089\2\2\u025e\u0260\5",
    "\u008eH\2\u025f\u025d\3\2\2\2\u025f\u025e\3\2\2\2\u0260\u0261\3\2\2",
    "\2\u0261\u0264\7g\2\2\u0262\u0265\5\u0088E\2\u0263\u0265\7\u0089\2\2",
    "\u0264\u0262\3\2\2\2\u0264\u0263\3\2\2\2\u0265}\3\2\2\2\u0266\u0267",
    "\7\u0089\2\2\u0267\u0268\7f\2\2\u0268\u0269\7\u0089\2\2\u0269\u026a",
    "\7^\2\2\u026a\u026b\5\u0090I\2\u026b\u026c\7e\2\2\u026c\u026d\5\u0088",
    "E\2\u026d\u026e\7_\2\2\u026e\u026f\7d\2\2\u026f\177\3\2\2\2\u0270\u0273",
    "\7\37\2\2\u0271\u0274\7\u0089\2\2\u0272\u0274\5r:\2\u0273\u0271\3\2",
    "\2\2\u0273\u0272\3\2\2\2\u0273\u0274\3\2\2\2\u0274\u0275\3\2\2\2\u0275",
    "\u0276\7d\2\2\u0276\u0081\3\2\2\2\u0277\u0278\7^\2\2\u0278\u027b\5\u0084",
    "C\2\u0279\u027a\t\4\2\2\u027a\u027c\5\u0084C\2\u027b\u0279\3\2\2\2\u027b",
    "\u027c\3\2\2\2\u027c\u027d\3\2\2\2\u027d\u027e\7_\2\2\u027e\u0083\3",
    "\2\2\2\u027f\u0282\5\u0086D\2\u0280\u0282\5\u0088E\2\u0281\u027f\3\2",
    "\2\2\u0281\u0280\3\2\2\2\u0282\u0085\3\2\2\2\u0283\u0284\7 \2\2\u0284",
    "\u0285\7^\2\2\u0285\u0286\7\u0089\2\2\u0286\u0287\7g\2\2\u0287\u0288",
    "\7\u0089\2\2\u0288\u0289\7e\2\2\u0289\u028a\7!\2\2\u028a\u028b\7g\2",
    "\2\u028b\u028c\7\\\2\2\u028c\u028d\7_\2\2\u028d\u0087\3\2\2\2\u028e",
    "\u028f\t\5\2\2\u028f\u0089\3\2\2\2\u0290\u0291\t\6\2\2\u0291\u008b\3",
    "\2\2\2\u0292\u0293\t\7\2\2\u0293\u008d\3\2\2\2\u0294\u0295\t\b\2\2\u0295",
    "\u008f\3\2\2\2\u0296\u029b\7\u0089\2\2\u0297\u0298\7f\2\2\u0298\u029a",
    "\7\u0089\2\2\u0299\u0297\3\2\2\2\u029a\u029d\3\2\2\2\u029b\u0299\3\2",
    "\2\2\u029b\u029c\3\2\2\2\u029c\u0091\3\2\2\2\u029d\u029b\3\2\2\2>\u0094",
    "\u0097\u009d\u00a1\u00a8\u00ae\u00b4\u00d6\u00dc\u00e2\u00e8\u00ee\u00f4",
    "\u00f7\u00fd\u0100\u0107\u010a\u0110\u0119\u0121\u012a\u0131\u0135\u0137",
    "\u0140\u0145\u015a\u0162\u017e\u0183\u0190\u019a\u019d\u01a0\u01a3\u01ad",
    "\u01af\u01b7\u01c8\u01d5\u01db\u01ed\u01fb\u0200\u0219\u022a\u0233\u023a",
    "\u023f\u0242\u024a\u0251\u025a\u025f\u0264\u0273\u027b\u0281\u029b"].join("");


var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

var decisionsToDFA = atn.decisionToState.map( function(ds, index) { return new antlr4.dfa.DFA(ds, index); });

var sharedContextCache = new antlr4.PredictionContextCache();

var literalNames = [ 'null', "'Path'", "'Source'", "'Service'", "'GET'", 
                     "'POST'", "'PUT'", "'DELETE'", "'HEAD'", "'Produces'", 
                     "'Consumes'", "'ApiOperation'", "'ApiResponses'", "'ApiResponse'", 
                     "'protocol'", "'host'", "'port'", "'tags'", "'description'", 
                     "'produces'", "'resource'", "'message'", "'ConnectionClosedException'", 
                     "'ConnectionFailedException'", "'ConnectionTimeoutException'", 
                     "'Exception'", "'endpoint'", "'@Parameters'", "'@CircuitBreaker'", 
                     "'reply'", "'eval'", "'path'", "'MediaType.APPLICATION_JSON'", 
                     "'MediaType.APPLICATION_XML'", "'string'", "'abstract'", 
                     "'assert'", "'boolean'", "'break'", "'byte'", "'case'", 
                     "'catch'", "'char'", "'class'", "'const'", "'continue'", 
                     "'constant'", "'default'", "'do'", "'double'", "'else'", 
                     "'enum'", "'extends'", "'final'", "'finally'", "'float'", 
                     "'for'", "'if'", "'goto'", "'implements'", "'import'", 
                     "'instanceof'", "'int'", "'interface'", "'long'", "'native'", 
                     "'new'", "'package'", "'private'", "'protected'", "'public'", 
                     "'return'", "'short'", "'static'", "'strictfp'", "'super'", 
                     "'switch'", "'synchronized'", "'this'", "'throw'", 
                     "'throws'", "'transient'", "'try'", "'void'", "'volatile'", 
                     "'while'", 'null', 'null', 'null', 'null', 'null', 
                     "'null'", "'('", "')'", "'{'", "'}'", "'['", "']'", 
                     "';'", "','", "'.'", "'='", "'>'", "'<'", "'!'", "'~'", 
                     "'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", 
                     "'||'", "'++'", "'--'", "'+'", "'-'", "'*'", "'/'", 
                     "'&'", "'|'", "'^'", "'%'", "'+='", "'-='", "'*='", 
                     "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", "'>>='", 
                     "'>>>='", 'null', "'@'", "'...'" ];

var symbolicNames = [ 'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      'null', 'null', 'null', 'null', 'null', 'null', 'null', 
                      "ABSTRACT", "ASSERT", "BOOLEAN", "BREAK", "BYTE", 
                      "CASE", "CATCH", "CHAR", "CLASS", "CONST", "CONTINUE", 
                      "CONSTANT", "DEFAULT", "DO", "DOUBLE", "ELSE", "ENUM", 
                      "EXTENDS", "FINAL", "FINALLY", "FLOAT", "FOR", "IF", 
                      "GOTO", "IMPLEMENTS", "IMPORT", "INSTANCEOF", "INT", 
                      "INTERFACE", "LONG", "NATIVE", "NEW", "PACKAGE", "PRIVATE", 
                      "PROTECTED", "PUBLIC", "RETURN", "SHORT", "STATIC", 
                      "STRICTFP", "SUPER", "SWITCH", "SYNCHRONIZED", "THIS", 
                      "THROW", "THROWS", "TRANSIENT", "TRY", "VOID", "VOLATILE", 
                      "WHILE", "IntegerLiteral", "FloatingPointLiteral", 
                      "BooleanLiteral", "CharacterLiteral", "StringLiteral", 
                      "NullLiteral", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
                      "LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", "ASSIGN", 
                      "GT", "LT", "BANG", "TILDE", "QUESTION", "COLON", 
                      "EQUAL", "LE", "GE", "NOTEQUAL", "AND", "OR", "INC", 
                      "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", 
                      "CARET", "MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", 
                      "DIV_ASSIGN", "AND_ASSIGN", "OR_ASSIGN", "XOR_ASSIGN", 
                      "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", "URSHIFT_ASSIGN", 
                      "Identifier", "AT", "ELLIPSIS", "WS", "COMMENT", "LINE_COMMENT" ];

var ruleNames =  [ "sourceFile", "definition", "constants", "globalVariables", 
                   "resources", "packageDef", "path", "source", "api", "resourcePath", 
                   "getMethod", "postMethod", "putMethod", "deleteMethod", 
                   "headMethod", "prodAnt", "conAnt", "antApiOperation", 
                   "antApiResponses", "antApiResponseSet", "antApiResponse", 
                   "elementValuePairs", "sourceElementValuePairs", "interfaceDeclaration", 
                   "apiElementValuePairs", "protocol", "host", "port", "tags", 
                   "tag", "descripton", "producer", "constant", "globalVariable", 
                   "elementValuePair", "elementValue", "resource", "httpMethods", 
                   "qualifiedName", "resourceDeclaration", "resourceName", 
                   "block", "blockStatement", "tryCatchBlock", "tryClause", 
                   "catchClause", "exceptionHandler", "exceptionType", "ifElseBlock", 
                   "ifBlock", "elseBlock", "localVariableDeclarationStatement", 
                   "localVariableInitializationStatement", "localVariableAssignmentStatement", 
                   "mediatorCallStatement", "newTypeObjectCreation", "mediatorCall", 
                   "endpointDeclaration", "parametersAnnotation", "circuitBreakerAnnotation", 
                   "keyValuePairs", "keyValuePair", "messageModificationStatement", 
                   "returnStatement", "parExpression", "expression", "evalExpression", 
                   "literal", "mediaType", "type", "classType", "messagePropertyName" ];

function NELParser (input) {
	antlr4.Parser.call(this, input);
    this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
    this.ruleNames = ruleNames;
    this.literalNames = literalNames;
    this.symbolicNames = symbolicNames;
    return this;
}

NELParser.prototype = Object.create(antlr4.Parser.prototype);
NELParser.prototype.constructor = NELParser;

Object.defineProperty(NELParser.prototype, "atn", {
	get : function() {
		return atn;
	}
});

NELParser.EOF = antlr4.Token.EOF;
NELParser.T__0 = 1;
NELParser.T__1 = 2;
NELParser.T__2 = 3;
NELParser.T__3 = 4;
NELParser.T__4 = 5;
NELParser.T__5 = 6;
NELParser.T__6 = 7;
NELParser.T__7 = 8;
NELParser.T__8 = 9;
NELParser.T__9 = 10;
NELParser.T__10 = 11;
NELParser.T__11 = 12;
NELParser.T__12 = 13;
NELParser.T__13 = 14;
NELParser.T__14 = 15;
NELParser.T__15 = 16;
NELParser.T__16 = 17;
NELParser.T__17 = 18;
NELParser.T__18 = 19;
NELParser.T__19 = 20;
NELParser.T__20 = 21;
NELParser.T__21 = 22;
NELParser.T__22 = 23;
NELParser.T__23 = 24;
NELParser.T__24 = 25;
NELParser.T__25 = 26;
NELParser.T__26 = 27;
NELParser.T__27 = 28;
NELParser.T__28 = 29;
NELParser.T__29 = 30;
NELParser.T__30 = 31;
NELParser.T__31 = 32;
NELParser.T__32 = 33;
NELParser.T__33 = 34;
NELParser.ABSTRACT = 35;
NELParser.ASSERT = 36;
NELParser.BOOLEAN = 37;
NELParser.BREAK = 38;
NELParser.BYTE = 39;
NELParser.CASE = 40;
NELParser.CATCH = 41;
NELParser.CHAR = 42;
NELParser.CLASS = 43;
NELParser.CONST = 44;
NELParser.CONTINUE = 45;
NELParser.CONSTANT = 46;
NELParser.DEFAULT = 47;
NELParser.DO = 48;
NELParser.DOUBLE = 49;
NELParser.ELSE = 50;
NELParser.ENUM = 51;
NELParser.EXTENDS = 52;
NELParser.FINAL = 53;
NELParser.FINALLY = 54;
NELParser.FLOAT = 55;
NELParser.FOR = 56;
NELParser.IF = 57;
NELParser.GOTO = 58;
NELParser.IMPLEMENTS = 59;
NELParser.IMPORT = 60;
NELParser.INSTANCEOF = 61;
NELParser.INT = 62;
NELParser.INTERFACE = 63;
NELParser.LONG = 64;
NELParser.NATIVE = 65;
NELParser.NEW = 66;
NELParser.PACKAGE = 67;
NELParser.PRIVATE = 68;
NELParser.PROTECTED = 69;
NELParser.PUBLIC = 70;
NELParser.RETURN = 71;
NELParser.SHORT = 72;
NELParser.STATIC = 73;
NELParser.STRICTFP = 74;
NELParser.SUPER = 75;
NELParser.SWITCH = 76;
NELParser.SYNCHRONIZED = 77;
NELParser.THIS = 78;
NELParser.THROW = 79;
NELParser.THROWS = 80;
NELParser.TRANSIENT = 81;
NELParser.TRY = 82;
NELParser.VOID = 83;
NELParser.VOLATILE = 84;
NELParser.WHILE = 85;
NELParser.IntegerLiteral = 86;
NELParser.FloatingPointLiteral = 87;
NELParser.BooleanLiteral = 88;
NELParser.CharacterLiteral = 89;
NELParser.StringLiteral = 90;
NELParser.NullLiteral = 91;
NELParser.LPAREN = 92;
NELParser.RPAREN = 93;
NELParser.LBRACE = 94;
NELParser.RBRACE = 95;
NELParser.LBRACK = 96;
NELParser.RBRACK = 97;
NELParser.SEMI = 98;
NELParser.COMMA = 99;
NELParser.DOT = 100;
NELParser.ASSIGN = 101;
NELParser.GT = 102;
NELParser.LT = 103;
NELParser.BANG = 104;
NELParser.TILDE = 105;
NELParser.QUESTION = 106;
NELParser.COLON = 107;
NELParser.EQUAL = 108;
NELParser.LE = 109;
NELParser.GE = 110;
NELParser.NOTEQUAL = 111;
NELParser.AND = 112;
NELParser.OR = 113;
NELParser.INC = 114;
NELParser.DEC = 115;
NELParser.ADD = 116;
NELParser.SUB = 117;
NELParser.MUL = 118;
NELParser.DIV = 119;
NELParser.BITAND = 120;
NELParser.BITOR = 121;
NELParser.CARET = 122;
NELParser.MOD = 123;
NELParser.ADD_ASSIGN = 124;
NELParser.SUB_ASSIGN = 125;
NELParser.MUL_ASSIGN = 126;
NELParser.DIV_ASSIGN = 127;
NELParser.AND_ASSIGN = 128;
NELParser.OR_ASSIGN = 129;
NELParser.XOR_ASSIGN = 130;
NELParser.MOD_ASSIGN = 131;
NELParser.LSHIFT_ASSIGN = 132;
NELParser.RSHIFT_ASSIGN = 133;
NELParser.URSHIFT_ASSIGN = 134;
NELParser.Identifier = 135;
NELParser.AT = 136;
NELParser.ELLIPSIS = 137;
NELParser.WS = 138;
NELParser.COMMENT = 139;
NELParser.LINE_COMMENT = 140;

NELParser.RULE_sourceFile = 0;
NELParser.RULE_definition = 1;
NELParser.RULE_constants = 2;
NELParser.RULE_globalVariables = 3;
NELParser.RULE_resources = 4;
NELParser.RULE_packageDef = 5;
NELParser.RULE_path = 6;
NELParser.RULE_source = 7;
NELParser.RULE_api = 8;
NELParser.RULE_resourcePath = 9;
NELParser.RULE_getMethod = 10;
NELParser.RULE_postMethod = 11;
NELParser.RULE_putMethod = 12;
NELParser.RULE_deleteMethod = 13;
NELParser.RULE_headMethod = 14;
NELParser.RULE_prodAnt = 15;
NELParser.RULE_conAnt = 16;
NELParser.RULE_antApiOperation = 17;
NELParser.RULE_antApiResponses = 18;
NELParser.RULE_antApiResponseSet = 19;
NELParser.RULE_antApiResponse = 20;
NELParser.RULE_elementValuePairs = 21;
NELParser.RULE_sourceElementValuePairs = 22;
NELParser.RULE_interfaceDeclaration = 23;
NELParser.RULE_apiElementValuePairs = 24;
NELParser.RULE_protocol = 25;
NELParser.RULE_host = 26;
NELParser.RULE_port = 27;
NELParser.RULE_tags = 28;
NELParser.RULE_tag = 29;
NELParser.RULE_descripton = 30;
NELParser.RULE_producer = 31;
NELParser.RULE_constant = 32;
NELParser.RULE_globalVariable = 33;
NELParser.RULE_elementValuePair = 34;
NELParser.RULE_elementValue = 35;
NELParser.RULE_resource = 36;
NELParser.RULE_httpMethods = 37;
NELParser.RULE_qualifiedName = 38;
NELParser.RULE_resourceDeclaration = 39;
NELParser.RULE_resourceName = 40;
NELParser.RULE_block = 41;
NELParser.RULE_blockStatement = 42;
NELParser.RULE_tryCatchBlock = 43;
NELParser.RULE_tryClause = 44;
NELParser.RULE_catchClause = 45;
NELParser.RULE_exceptionHandler = 46;
NELParser.RULE_exceptionType = 47;
NELParser.RULE_ifElseBlock = 48;
NELParser.RULE_ifBlock = 49;
NELParser.RULE_elseBlock = 50;
NELParser.RULE_localVariableDeclarationStatement = 51;
NELParser.RULE_localVariableInitializationStatement = 52;
NELParser.RULE_localVariableAssignmentStatement = 53;
NELParser.RULE_mediatorCallStatement = 54;
NELParser.RULE_newTypeObjectCreation = 55;
NELParser.RULE_mediatorCall = 56;
NELParser.RULE_endpointDeclaration = 57;
NELParser.RULE_parametersAnnotation = 58;
NELParser.RULE_circuitBreakerAnnotation = 59;
NELParser.RULE_keyValuePairs = 60;
NELParser.RULE_keyValuePair = 61;
NELParser.RULE_messageModificationStatement = 62;
NELParser.RULE_returnStatement = 63;
NELParser.RULE_parExpression = 64;
NELParser.RULE_expression = 65;
NELParser.RULE_evalExpression = 66;
NELParser.RULE_literal = 67;
NELParser.RULE_mediaType = 68;
NELParser.RULE_type = 69;
NELParser.RULE_classType = 70;
NELParser.RULE_messagePropertyName = 71;

function SourceFileContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_sourceFile;
    return this;
}

SourceFileContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
SourceFileContext.prototype.constructor = SourceFileContext;

SourceFileContext.prototype.definition = function() {
    return this.getTypedRuleContext(DefinitionContext,0);
};

SourceFileContext.prototype.resources = function() {
    return this.getTypedRuleContext(ResourcesContext,0);
};

SourceFileContext.prototype.EOF = function() {
    return this.getToken(NELParser.EOF, 0);
};

SourceFileContext.prototype.constants = function() {
    return this.getTypedRuleContext(ConstantsContext,0);
};

SourceFileContext.prototype.globalVariables = function() {
    return this.getTypedRuleContext(GlobalVariablesContext,0);
};

SourceFileContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterSourceFile(this);
	}
};

SourceFileContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitSourceFile(this);
	}
};

NELParser.SourceFileContext = SourceFileContext;

NELParser.prototype.sourceFile = function() {

    var localctx = new SourceFileContext(this, this._ctx, this.state);
    this.enterRule(localctx, 0, NELParser.RULE_sourceFile);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 144;
        this.definition();
        this.state = 146;
        var la_ = this._interp.adaptivePredict(this._input,0,this._ctx);
        if(la_===1) {
            this.state = 145;
            this.constants();

        }
        this.state = 149;
        var la_ = this._interp.adaptivePredict(this._input,1,this._ctx);
        if(la_===1) {
            this.state = 148;
            this.globalVariables();

        }
        this.state = 151;
        this.resources();
        this.state = 152;
        this.match(NELParser.EOF);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function DefinitionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_definition;
    return this;
}

DefinitionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
DefinitionContext.prototype.constructor = DefinitionContext;

DefinitionContext.prototype.source = function() {
    return this.getTypedRuleContext(SourceContext,0);
};

DefinitionContext.prototype.packageDef = function() {
    return this.getTypedRuleContext(PackageDefContext,0);
};

DefinitionContext.prototype.path = function() {
    return this.getTypedRuleContext(PathContext,0);
};

DefinitionContext.prototype.api = function() {
    return this.getTypedRuleContext(ApiContext,0);
};

DefinitionContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterDefinition(this);
	}
};

DefinitionContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitDefinition(this);
	}
};




NELParser.DefinitionContext = DefinitionContext;

NELParser.prototype.definition = function() {

    var localctx = new DefinitionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 2, NELParser.RULE_definition);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 155;
        var la_ = this._interp.adaptivePredict(this._input,2,this._ctx);
        if(la_===1) {
            this.state = 154;
            this.path();

        }
        this.state = 157;
        this.source();
        this.state = 159;
        _la = this._input.LA(1);
        if(_la===NELParser.AT) {
            this.state = 158;
            this.api();
        }

        this.state = 161;
        this.packageDef();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ConstantsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_constants;
    return this;
}

ConstantsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ConstantsContext.prototype.constructor = ConstantsContext;

ConstantsContext.prototype.constant = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ConstantContext);
    } else {
        return this.getTypedRuleContext(ConstantContext,i);
    }
};

ConstantsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterConstants(this);
	}
};

ConstantsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitConstants(this);
	}
};




NELParser.ConstantsContext = ConstantsContext;

NELParser.prototype.constants = function() {

    var localctx = new ConstantsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 4, NELParser.RULE_constants);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 166;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.CONSTANT) {
            this.state = 163;
            this.constant();
            this.state = 168;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function GlobalVariablesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_globalVariables;
    return this;
}

GlobalVariablesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
GlobalVariablesContext.prototype.constructor = GlobalVariablesContext;

GlobalVariablesContext.prototype.globalVariable = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(GlobalVariableContext);
    } else {
        return this.getTypedRuleContext(GlobalVariableContext,i);
    }
};

GlobalVariablesContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterGlobalVariables(this);
	}
};

GlobalVariablesContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitGlobalVariables(this);
	}
};




NELParser.GlobalVariablesContext = GlobalVariablesContext;

NELParser.prototype.globalVariables = function() {

    var localctx = new GlobalVariablesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 6, NELParser.RULE_globalVariables);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 172;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(((((_la - 26)) & ~0x1f) == 0 && ((1 << (_la - 26)) & ((1 << (NELParser.T__25 - 26)) | (1 << (NELParser.T__26 - 26)) | (1 << (NELParser.T__27 - 26)) | (1 << (NELParser.T__33 - 26)) | (1 << (NELParser.BOOLEAN - 26)) | (1 << (NELParser.BYTE - 26)) | (1 << (NELParser.CHAR - 26)) | (1 << (NELParser.DOUBLE - 26)) | (1 << (NELParser.FLOAT - 26)))) !== 0) || ((((_la - 62)) & ~0x1f) == 0 && ((1 << (_la - 62)) & ((1 << (NELParser.INT - 62)) | (1 << (NELParser.LONG - 62)) | (1 << (NELParser.SHORT - 62)))) !== 0)) {
            this.state = 169;
            this.globalVariable();
            this.state = 174;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ResourcesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_resources;
    return this;
}

ResourcesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourcesContext.prototype.constructor = ResourcesContext;

ResourcesContext.prototype.resource = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ResourceContext);
    } else {
        return this.getTypedRuleContext(ResourceContext,i);
    }
};

ResourcesContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterResources(this);
	}
};

ResourcesContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitResources(this);
	}
};




NELParser.ResourcesContext = ResourcesContext;

NELParser.prototype.resources = function() {

    var localctx = new ResourcesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 8, NELParser.RULE_resources);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 176; 
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        do {
            this.state = 175;
            this.resource();
            this.state = 178; 
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        } while(_la===NELParser.AT);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PackageDefContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_packageDef;
    return this;
}

PackageDefContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PackageDefContext.prototype.constructor = PackageDefContext;

PackageDefContext.prototype.PACKAGE = function() {
    return this.getToken(NELParser.PACKAGE, 0);
};

PackageDefContext.prototype.qualifiedName = function() {
    return this.getTypedRuleContext(QualifiedNameContext,0);
};

PackageDefContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterPackageDef(this);
	}
};

PackageDefContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitPackageDef(this);
	}
};




NELParser.PackageDefContext = PackageDefContext;

NELParser.prototype.packageDef = function() {

    var localctx = new PackageDefContext(this, this._ctx, this.state);
    this.enterRule(localctx, 10, NELParser.RULE_packageDef);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 180;
        this.match(NELParser.PACKAGE);
        this.state = 181;
        this.qualifiedName();
        this.state = 182;
        this.match(NELParser.SEMI);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PathContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_path;
    return this;
}

PathContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PathContext.prototype.constructor = PathContext;

PathContext.prototype.AT = function() {
    return this.getToken(NELParser.AT, 0);
};

PathContext.prototype.LPAREN = function() {
    return this.getToken(NELParser.LPAREN, 0);
};

PathContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

PathContext.prototype.RPAREN = function() {
    return this.getToken(NELParser.RPAREN, 0);
};

PathContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterPath(this);
	}
};

PathContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitPath(this);
	}
};




NELParser.PathContext = PathContext;

NELParser.prototype.path = function() {

    var localctx = new PathContext(this, this._ctx, this.state);
    this.enterRule(localctx, 12, NELParser.RULE_path);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 184;
        this.match(NELParser.AT);
        this.state = 185;
        this.match(NELParser.T__0);
        this.state = 186;
        this.match(NELParser.LPAREN);
        this.state = 187;
        this.match(NELParser.StringLiteral);
        this.state = 188;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function SourceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_source;
    return this;
}

SourceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
SourceContext.prototype.constructor = SourceContext;

SourceContext.prototype.AT = function() {
    return this.getToken(NELParser.AT, 0);
};

SourceContext.prototype.LPAREN = function() {
    return this.getToken(NELParser.LPAREN, 0);
};

SourceContext.prototype.sourceElementValuePairs = function() {
    return this.getTypedRuleContext(SourceElementValuePairsContext,0);
};

SourceContext.prototype.RPAREN = function() {
    return this.getToken(NELParser.RPAREN, 0);
};

SourceContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterSource(this);
	}
};

SourceContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitSource(this);
	}
};




NELParser.SourceContext = SourceContext;

NELParser.prototype.source = function() {

    var localctx = new SourceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 14, NELParser.RULE_source);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 190;
        this.match(NELParser.AT);
        this.state = 191;
        this.match(NELParser.T__1);
        this.state = 192;
        this.match(NELParser.LPAREN);
        this.state = 193;
        this.sourceElementValuePairs();
        this.state = 194;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ApiContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_api;
    return this;
}

ApiContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ApiContext.prototype.constructor = ApiContext;

ApiContext.prototype.apiElementValuePairs = function() {
    return this.getTypedRuleContext(ApiElementValuePairsContext,0);
};

ApiContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterApi(this);
	}
};

ApiContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitApi(this);
	}
};




NELParser.ApiContext = ApiContext;

NELParser.prototype.api = function() {

    var localctx = new ApiContext(this, this._ctx, this.state);
    this.enterRule(localctx, 16, NELParser.RULE_api);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 196;
        this.match(NELParser.AT);
        this.state = 197;
        this.match(NELParser.T__2);

        this.state = 198;
        this.match(NELParser.LPAREN);

        this.state = 199;
        this.apiElementValuePairs();
        this.state = 200;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ResourcePathContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_resourcePath;
    return this;
}

ResourcePathContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourcePathContext.prototype.constructor = ResourcePathContext;

ResourcePathContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

ResourcePathContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterResourcePath(this);
	}
};

ResourcePathContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitResourcePath(this);
	}
};




NELParser.ResourcePathContext = ResourcePathContext;

NELParser.prototype.resourcePath = function() {

    var localctx = new ResourcePathContext(this, this._ctx, this.state);
    this.enterRule(localctx, 18, NELParser.RULE_resourcePath);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 202;
        this.match(NELParser.AT);
        this.state = 203;
        this.match(NELParser.T__0);

        this.state = 204;
        this.match(NELParser.LPAREN);
        this.state = 205;
        this.match(NELParser.StringLiteral);
        this.state = 206;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function GetMethodContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_getMethod;
    return this;
}

GetMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
GetMethodContext.prototype.constructor = GetMethodContext;


GetMethodContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterGetMethod(this);
	}
};

GetMethodContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitGetMethod(this);
	}
};




NELParser.GetMethodContext = GetMethodContext;

NELParser.prototype.getMethod = function() {

    var localctx = new GetMethodContext(this, this._ctx, this.state);
    this.enterRule(localctx, 20, NELParser.RULE_getMethod);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 208;
        this.match(NELParser.AT);
        this.state = 209;
        this.match(NELParser.T__3);
        this.state = 212;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 210;
            this.match(NELParser.LPAREN);
            this.state = 211;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PostMethodContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_postMethod;
    return this;
}

PostMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PostMethodContext.prototype.constructor = PostMethodContext;


PostMethodContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterPostMethod(this);
	}
};

PostMethodContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitPostMethod(this);
	}
};




NELParser.PostMethodContext = PostMethodContext;

NELParser.prototype.postMethod = function() {

    var localctx = new PostMethodContext(this, this._ctx, this.state);
    this.enterRule(localctx, 22, NELParser.RULE_postMethod);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 214;
        this.match(NELParser.AT);
        this.state = 215;
        this.match(NELParser.T__4);
        this.state = 218;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 216;
            this.match(NELParser.LPAREN);
            this.state = 217;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PutMethodContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_putMethod;
    return this;
}

PutMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PutMethodContext.prototype.constructor = PutMethodContext;


PutMethodContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterPutMethod(this);
	}
};

PutMethodContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitPutMethod(this);
	}
};




NELParser.PutMethodContext = PutMethodContext;

NELParser.prototype.putMethod = function() {

    var localctx = new PutMethodContext(this, this._ctx, this.state);
    this.enterRule(localctx, 24, NELParser.RULE_putMethod);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 220;
        this.match(NELParser.AT);
        this.state = 221;
        this.match(NELParser.T__5);
        this.state = 224;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 222;
            this.match(NELParser.LPAREN);
            this.state = 223;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function DeleteMethodContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_deleteMethod;
    return this;
}

DeleteMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
DeleteMethodContext.prototype.constructor = DeleteMethodContext;


DeleteMethodContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterDeleteMethod(this);
	}
};

DeleteMethodContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitDeleteMethod(this);
	}
};




NELParser.DeleteMethodContext = DeleteMethodContext;

NELParser.prototype.deleteMethod = function() {

    var localctx = new DeleteMethodContext(this, this._ctx, this.state);
    this.enterRule(localctx, 26, NELParser.RULE_deleteMethod);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 226;
        this.match(NELParser.AT);
        this.state = 227;
        this.match(NELParser.T__6);
        this.state = 230;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 228;
            this.match(NELParser.LPAREN);
            this.state = 229;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function HeadMethodContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_headMethod;
    return this;
}

HeadMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
HeadMethodContext.prototype.constructor = HeadMethodContext;


HeadMethodContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterHeadMethod(this);
	}
};

HeadMethodContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitHeadMethod(this);
	}
};




NELParser.HeadMethodContext = HeadMethodContext;

NELParser.prototype.headMethod = function() {

    var localctx = new HeadMethodContext(this, this._ctx, this.state);
    this.enterRule(localctx, 28, NELParser.RULE_headMethod);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 232;
        this.match(NELParser.AT);
        this.state = 233;
        this.match(NELParser.T__7);
        this.state = 236;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 234;
            this.match(NELParser.LPAREN);
            this.state = 235;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ProdAntContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_prodAnt;
    return this;
}

ProdAntContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ProdAntContext.prototype.constructor = ProdAntContext;

ProdAntContext.prototype.elementValue = function() {
    return this.getTypedRuleContext(ElementValueContext,0);
};

ProdAntContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterProdAnt(this);
	}
};

ProdAntContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitProdAnt(this);
	}
};




NELParser.ProdAntContext = ProdAntContext;

NELParser.prototype.prodAnt = function() {

    var localctx = new ProdAntContext(this, this._ctx, this.state);
    this.enterRule(localctx, 30, NELParser.RULE_prodAnt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 238;
        this.match(NELParser.AT);
        this.state = 239;
        this.match(NELParser.T__8);
        this.state = 245;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 240;
            this.match(NELParser.LPAREN);
            this.state = 242;
            _la = this._input.LA(1);
            if(_la===NELParser.IntegerLiteral || _la===NELParser.StringLiteral) {
                this.state = 241;
                this.elementValue();
            }

            this.state = 244;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ConAntContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_conAnt;
    return this;
}

ConAntContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ConAntContext.prototype.constructor = ConAntContext;

ConAntContext.prototype.elementValue = function() {
    return this.getTypedRuleContext(ElementValueContext,0);
};

ConAntContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterConAnt(this);
	}
};

ConAntContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitConAnt(this);
	}
};




NELParser.ConAntContext = ConAntContext;

NELParser.prototype.conAnt = function() {

    var localctx = new ConAntContext(this, this._ctx, this.state);
    this.enterRule(localctx, 32, NELParser.RULE_conAnt);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 247;
        this.match(NELParser.AT);
        this.state = 248;
        this.match(NELParser.T__9);
        this.state = 254;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 249;
            this.match(NELParser.LPAREN);
            this.state = 251;
            _la = this._input.LA(1);
            if(_la===NELParser.IntegerLiteral || _la===NELParser.StringLiteral) {
                this.state = 250;
                this.elementValue();
            }

            this.state = 253;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function AntApiOperationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_antApiOperation;
    return this;
}

AntApiOperationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
AntApiOperationContext.prototype.constructor = AntApiOperationContext;

AntApiOperationContext.prototype.elementValuePairs = function() {
    return this.getTypedRuleContext(ElementValuePairsContext,0);
};

AntApiOperationContext.prototype.elementValue = function() {
    return this.getTypedRuleContext(ElementValueContext,0);
};

AntApiOperationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterAntApiOperation(this);
	}
};

AntApiOperationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitAntApiOperation(this);
	}
};




NELParser.AntApiOperationContext = AntApiOperationContext;

NELParser.prototype.antApiOperation = function() {

    var localctx = new AntApiOperationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 34, NELParser.RULE_antApiOperation);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 256;
        this.match(NELParser.AT);
        this.state = 257;
        this.match(NELParser.T__10);
        this.state = 264;
        _la = this._input.LA(1);
        if(_la===NELParser.LPAREN) {
            this.state = 258;
            this.match(NELParser.LPAREN);
            this.state = 261;
            switch (this._input.LA(1)) {
            case NELParser.Identifier:
            	this.state = 259;
            	this.elementValuePairs();
            	break;
            case NELParser.IntegerLiteral:
            case NELParser.StringLiteral:
            	this.state = 260;
            	this.elementValue();
            	break;
            case NELParser.RPAREN:
            	break;
            default:
            	throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 263;
            this.match(NELParser.RPAREN);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function AntApiResponsesContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_antApiResponses;
    return this;
}

AntApiResponsesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
AntApiResponsesContext.prototype.constructor = AntApiResponsesContext;

AntApiResponsesContext.prototype.antApiResponseSet = function() {
    return this.getTypedRuleContext(AntApiResponseSetContext,0);
};

AntApiResponsesContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterAntApiResponses(this);
	}
};

AntApiResponsesContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitAntApiResponses(this);
	}
};




NELParser.AntApiResponsesContext = AntApiResponsesContext;

NELParser.prototype.antApiResponses = function() {

    var localctx = new AntApiResponsesContext(this, this._ctx, this.state);
    this.enterRule(localctx, 36, NELParser.RULE_antApiResponses);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 266;
        this.match(NELParser.AT);
        this.state = 267;
        this.match(NELParser.T__11);
        this.state = 268;
        this.match(NELParser.LPAREN);
        this.state = 270;
        _la = this._input.LA(1);
        if(_la===NELParser.AT) {
            this.state = 269;
            this.antApiResponseSet();
        }

        this.state = 272;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function AntApiResponseSetContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_antApiResponseSet;
    return this;
}

AntApiResponseSetContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
AntApiResponseSetContext.prototype.constructor = AntApiResponseSetContext;

AntApiResponseSetContext.prototype.antApiResponse = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(AntApiResponseContext);
    } else {
        return this.getTypedRuleContext(AntApiResponseContext,i);
    }
};

AntApiResponseSetContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterAntApiResponseSet(this);
	}
};

AntApiResponseSetContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitAntApiResponseSet(this);
	}
};




NELParser.AntApiResponseSetContext = AntApiResponseSetContext;

NELParser.prototype.antApiResponseSet = function() {

    var localctx = new AntApiResponseSetContext(this, this._ctx, this.state);
    this.enterRule(localctx, 38, NELParser.RULE_antApiResponseSet);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 274;
        this.antApiResponse();
        this.state = 279;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.COMMA) {
            this.state = 275;
            this.match(NELParser.COMMA);
            this.state = 276;
            this.antApiResponse();
            this.state = 281;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function AntApiResponseContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_antApiResponse;
    return this;
}

AntApiResponseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
AntApiResponseContext.prototype.constructor = AntApiResponseContext;

AntApiResponseContext.prototype.elementValuePairs = function() {
    return this.getTypedRuleContext(ElementValuePairsContext,0);
};

AntApiResponseContext.prototype.elementValue = function() {
    return this.getTypedRuleContext(ElementValueContext,0);
};

AntApiResponseContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterAntApiResponse(this);
	}
};

AntApiResponseContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitAntApiResponse(this);
	}
};




NELParser.AntApiResponseContext = AntApiResponseContext;

NELParser.prototype.antApiResponse = function() {

    var localctx = new AntApiResponseContext(this, this._ctx, this.state);
    this.enterRule(localctx, 40, NELParser.RULE_antApiResponse);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 282;
        this.match(NELParser.AT);
        this.state = 283;
        this.match(NELParser.T__12);
        this.state = 284;
        this.match(NELParser.LPAREN);
        this.state = 287;
        switch (this._input.LA(1)) {
        case NELParser.Identifier:
        	this.state = 285;
        	this.elementValuePairs();
        	break;
        case NELParser.IntegerLiteral:
        case NELParser.StringLiteral:
        	this.state = 286;
        	this.elementValue();
        	break;
        case NELParser.RPAREN:
        	break;
        default:
        	throw new antlr4.error.NoViableAltException(this);
        }
        this.state = 289;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ElementValuePairsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_elementValuePairs;
    return this;
}

ElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ElementValuePairsContext.prototype.constructor = ElementValuePairsContext;

ElementValuePairsContext.prototype.elementValuePair = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ElementValuePairContext);
    } else {
        return this.getTypedRuleContext(ElementValuePairContext,i);
    }
};

ElementValuePairsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterElementValuePairs(this);
	}
};

ElementValuePairsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitElementValuePairs(this);
	}
};




NELParser.ElementValuePairsContext = ElementValuePairsContext;

NELParser.prototype.elementValuePairs = function() {

    var localctx = new ElementValuePairsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 42, NELParser.RULE_elementValuePairs);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 291;
        this.elementValuePair();
        this.state = 296;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.COMMA) {
            this.state = 292;
            this.match(NELParser.COMMA);
            this.state = 293;
            this.elementValuePair();
            this.state = 298;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function SourceElementValuePairsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_sourceElementValuePairs;
    return this;
}

SourceElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
SourceElementValuePairsContext.prototype.constructor = SourceElementValuePairsContext;

SourceElementValuePairsContext.prototype.interfaceDeclaration = function() {
    return this.getTypedRuleContext(InterfaceDeclarationContext,0);
};

SourceElementValuePairsContext.prototype.protocol = function() {
    return this.getTypedRuleContext(ProtocolContext,0);
};

SourceElementValuePairsContext.prototype.host = function() {
    return this.getTypedRuleContext(HostContext,0);
};

SourceElementValuePairsContext.prototype.port = function() {
    return this.getTypedRuleContext(PortContext,0);
};

SourceElementValuePairsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterSourceElementValuePairs(this);
	}
};

SourceElementValuePairsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitSourceElementValuePairs(this);
	}
};




NELParser.SourceElementValuePairsContext = SourceElementValuePairsContext;

NELParser.prototype.sourceElementValuePairs = function() {

    var localctx = new SourceElementValuePairsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 44, NELParser.RULE_sourceElementValuePairs);
    var _la = 0; // Token type
    try {
        this.state = 309;
        switch(this._input.LA(1)) {
        case NELParser.INTERFACE:
            this.enterOuterAlt(localctx, 1);
            this.state = 299;
            this.interfaceDeclaration();
            break;
        case NELParser.T__13:
            this.enterOuterAlt(localctx, 2);
            this.state = 300;
            this.protocol();
            this.state = 303;
            var la_ = this._interp.adaptivePredict(this._input,22,this._ctx);
            if(la_===1) {
                this.state = 301;
                this.match(NELParser.COMMA);
                this.state = 302;
                this.host();

            }
            this.state = 307;
            _la = this._input.LA(1);
            if(_la===NELParser.COMMA) {
                this.state = 305;
                this.match(NELParser.COMMA);
                this.state = 306;
                this.port();
            }

            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function InterfaceDeclarationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_interfaceDeclaration;
    return this;
}

InterfaceDeclarationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
InterfaceDeclarationContext.prototype.constructor = InterfaceDeclarationContext;

InterfaceDeclarationContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

InterfaceDeclarationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterInterfaceDeclaration(this);
	}
};

InterfaceDeclarationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitInterfaceDeclaration(this);
	}
};




NELParser.InterfaceDeclarationContext = InterfaceDeclarationContext;

NELParser.prototype.interfaceDeclaration = function() {

    var localctx = new InterfaceDeclarationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 46, NELParser.RULE_interfaceDeclaration);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 311;
        this.match(NELParser.INTERFACE);
        this.state = 312;
        this.match(NELParser.ASSIGN);
        this.state = 313;
        this.match(NELParser.StringLiteral);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ApiElementValuePairsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_apiElementValuePairs;
    return this;
}

ApiElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ApiElementValuePairsContext.prototype.constructor = ApiElementValuePairsContext;

ApiElementValuePairsContext.prototype.producer = function() {
    return this.getTypedRuleContext(ProducerContext,0);
};

ApiElementValuePairsContext.prototype.tags = function() {
    return this.getTypedRuleContext(TagsContext,0);
};

ApiElementValuePairsContext.prototype.descripton = function() {
    return this.getTypedRuleContext(DescriptonContext,0);
};

ApiElementValuePairsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterApiElementValuePairs(this);
	}
};

ApiElementValuePairsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitApiElementValuePairs(this);
	}
};




NELParser.ApiElementValuePairsContext = ApiElementValuePairsContext;

NELParser.prototype.apiElementValuePairs = function() {

    var localctx = new ApiElementValuePairsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 48, NELParser.RULE_apiElementValuePairs);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 318;
        _la = this._input.LA(1);
        if(_la===NELParser.T__16) {
            this.state = 315;
            this.tags();
            this.state = 316;
            this.match(NELParser.COMMA);
        }

        this.state = 323;
        _la = this._input.LA(1);
        if(_la===NELParser.T__17) {
            this.state = 320;
            this.descripton();
            this.state = 321;
            this.match(NELParser.COMMA);
        }

        this.state = 325;
        this.producer();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ProtocolContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_protocol;
    return this;
}

ProtocolContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ProtocolContext.prototype.constructor = ProtocolContext;

ProtocolContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

ProtocolContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterProtocol(this);
	}
};

ProtocolContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitProtocol(this);
	}
};




NELParser.ProtocolContext = ProtocolContext;

NELParser.prototype.protocol = function() {

    var localctx = new ProtocolContext(this, this._ctx, this.state);
    this.enterRule(localctx, 50, NELParser.RULE_protocol);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 327;
        this.match(NELParser.T__13);
        this.state = 328;
        this.match(NELParser.ASSIGN);
        this.state = 329;
        this.match(NELParser.StringLiteral);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function HostContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_host;
    return this;
}

HostContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
HostContext.prototype.constructor = HostContext;

HostContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

HostContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterHost(this);
	}
};

HostContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitHost(this);
	}
};




NELParser.HostContext = HostContext;

NELParser.prototype.host = function() {

    var localctx = new HostContext(this, this._ctx, this.state);
    this.enterRule(localctx, 52, NELParser.RULE_host);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 331;
        this.match(NELParser.T__14);
        this.state = 332;
        this.match(NELParser.ASSIGN);
        this.state = 333;
        this.match(NELParser.StringLiteral);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function PortContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_port;
    return this;
}

PortContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
PortContext.prototype.constructor = PortContext;

PortContext.prototype.IntegerLiteral = function() {
    return this.getToken(NELParser.IntegerLiteral, 0);
};

PortContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterPort(this);
	}
};

PortContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitPort(this);
	}
};




NELParser.PortContext = PortContext;

NELParser.prototype.port = function() {

    var localctx = new PortContext(this, this._ctx, this.state);
    this.enterRule(localctx, 54, NELParser.RULE_port);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 335;
        this.match(NELParser.T__15);
        this.state = 336;
        this.match(NELParser.ASSIGN);
        this.state = 337;
        this.match(NELParser.IntegerLiteral);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TagsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_tags;
    return this;
}

TagsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TagsContext.prototype.constructor = TagsContext;

TagsContext.prototype.tag = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(TagContext);
    } else {
        return this.getTypedRuleContext(TagContext,i);
    }
};

TagsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterTags(this);
	}
};

TagsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitTags(this);
	}
};




NELParser.TagsContext = TagsContext;

NELParser.prototype.tags = function() {

    var localctx = new TagsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 56, NELParser.RULE_tags);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 339;
        this.match(NELParser.T__16);
        this.state = 340;
        this.match(NELParser.ASSIGN);
        this.state = 342; 
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        do {
            this.state = 341;
            this.tag();
            this.state = 344; 
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        } while(_la===NELParser.LBRACE);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TagContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_tag;
    return this;
}

TagContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TagContext.prototype.constructor = TagContext;

TagContext.prototype.StringLiteral = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.StringLiteral);
    } else {
        return this.getToken(NELParser.StringLiteral, i);
    }
};


TagContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterTag(this);
	}
};

TagContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitTag(this);
	}
};




NELParser.TagContext = TagContext;

NELParser.prototype.tag = function() {

    var localctx = new TagContext(this, this._ctx, this.state);
    this.enterRule(localctx, 58, NELParser.RULE_tag);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 346;
        this.match(NELParser.LBRACE);
        this.state = 347;
        this.match(NELParser.StringLiteral);
        this.state = 352;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.COMMA) {
            this.state = 348;
            this.match(NELParser.COMMA);
            this.state = 349;
            this.match(NELParser.StringLiteral);
            this.state = 354;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 355;
        this.match(NELParser.RBRACE);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function DescriptonContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_descripton;
    return this;
}

DescriptonContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
DescriptonContext.prototype.constructor = DescriptonContext;

DescriptonContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

DescriptonContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterDescripton(this);
	}
};

DescriptonContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitDescripton(this);
	}
};




NELParser.DescriptonContext = DescriptonContext;

NELParser.prototype.descripton = function() {

    var localctx = new DescriptonContext(this, this._ctx, this.state);
    this.enterRule(localctx, 60, NELParser.RULE_descripton);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 357;
        this.match(NELParser.T__17);
        this.state = 358;
        this.match(NELParser.ASSIGN);
        this.state = 359;
        this.match(NELParser.StringLiteral);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ProducerContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_producer;
    return this;
}

ProducerContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ProducerContext.prototype.constructor = ProducerContext;

ProducerContext.prototype.mediaType = function() {
    return this.getTypedRuleContext(MediaTypeContext,0);
};

ProducerContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterProducer(this);
	}
};

ProducerContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitProducer(this);
	}
};




NELParser.ProducerContext = ProducerContext;

NELParser.prototype.producer = function() {

    var localctx = new ProducerContext(this, this._ctx, this.state);
    this.enterRule(localctx, 62, NELParser.RULE_producer);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 361;
        this.match(NELParser.T__18);
        this.state = 362;
        this.match(NELParser.ASSIGN);
        this.state = 363;
        this.mediaType();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ConstantContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_constant;
    return this;
}

ConstantContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ConstantContext.prototype.constructor = ConstantContext;

ConstantContext.prototype.CONSTANT = function() {
    return this.getToken(NELParser.CONSTANT, 0);
};

ConstantContext.prototype.type = function() {
    return this.getTypedRuleContext(TypeContext,0);
};

ConstantContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


ConstantContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

ConstantContext.prototype.classType = function() {
    return this.getTypedRuleContext(ClassTypeContext,0);
};

ConstantContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

ConstantContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterConstant(this);
	}
};

ConstantContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitConstant(this);
	}
};




NELParser.ConstantContext = ConstantContext;

NELParser.prototype.constant = function() {

    var localctx = new ConstantContext(this, this._ctx, this.state);
    this.enterRule(localctx, 64, NELParser.RULE_constant);
    var _la = 0; // Token type
    try {
        this.state = 385;
        var la_ = this._interp.adaptivePredict(this._input,30,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 365;
            this.match(NELParser.CONSTANT);
            this.state = 366;
            this.type();
            this.state = 367;
            this.match(NELParser.Identifier);
            this.state = 368;
            this.match(NELParser.ASSIGN);
            this.state = 369;
            this.literal();
            this.state = 370;
            this.match(NELParser.SEMI);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 372;
            this.match(NELParser.CONSTANT);
            this.state = 373;
            this.classType();
            this.state = 374;
            this.match(NELParser.Identifier);
            this.state = 375;
            this.match(NELParser.ASSIGN);
            this.state = 376;
            this.match(NELParser.NEW);
            this.state = 377;
            this.match(NELParser.Identifier);
            this.state = 378;
            this.match(NELParser.LPAREN);
            this.state = 380;
            _la = this._input.LA(1);
            if(_la===NELParser.StringLiteral) {
                this.state = 379;
                this.match(NELParser.StringLiteral);
            }

            this.state = 382;
            this.match(NELParser.RPAREN);
            this.state = 383;
            this.match(NELParser.SEMI);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function GlobalVariableContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_globalVariable;
    return this;
}

GlobalVariableContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
GlobalVariableContext.prototype.constructor = GlobalVariableContext;

GlobalVariableContext.prototype.type = function() {
    return this.getTypedRuleContext(TypeContext,0);
};

GlobalVariableContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

GlobalVariableContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

GlobalVariableContext.prototype.endpointDeclaration = function() {
    return this.getTypedRuleContext(EndpointDeclarationContext,0);
};

GlobalVariableContext.prototype.newTypeObjectCreation = function() {
    return this.getTypedRuleContext(NewTypeObjectCreationContext,0);
};

GlobalVariableContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterGlobalVariable(this);
	}
};

GlobalVariableContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitGlobalVariable(this);
	}
};




NELParser.GlobalVariableContext = GlobalVariableContext;

NELParser.prototype.globalVariable = function() {

    var localctx = new GlobalVariableContext(this, this._ctx, this.state);
    this.enterRule(localctx, 66, NELParser.RULE_globalVariable);
    try {
        this.state = 398;
        switch(this._input.LA(1)) {
        case NELParser.T__33:
        case NELParser.BOOLEAN:
        case NELParser.BYTE:
        case NELParser.CHAR:
        case NELParser.DOUBLE:
        case NELParser.FLOAT:
        case NELParser.INT:
        case NELParser.LONG:
        case NELParser.SHORT:
            this.enterOuterAlt(localctx, 1);
            this.state = 387;
            this.type();
            this.state = 388;
            this.match(NELParser.Identifier);
            this.state = 389;
            this.match(NELParser.ASSIGN);
            this.state = 390;
            this.literal();
            this.state = 391;
            this.match(NELParser.SEMI);
            break;
        case NELParser.T__25:
        case NELParser.T__26:
        case NELParser.T__27:
            this.enterOuterAlt(localctx, 2);
            this.state = 393;
            this.endpointDeclaration();
            this.state = 394;
            this.match(NELParser.ASSIGN);
            this.state = 395;
            this.newTypeObjectCreation();
            this.state = 396;
            this.match(NELParser.SEMI);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ElementValuePairContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_elementValuePair;
    return this;
}

ElementValuePairContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ElementValuePairContext.prototype.constructor = ElementValuePairContext;

ElementValuePairContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

ElementValuePairContext.prototype.elementValue = function() {
    return this.getTypedRuleContext(ElementValueContext,0);
};

ElementValuePairContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterElementValuePair(this);
	}
};

ElementValuePairContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitElementValuePair(this);
	}
};




NELParser.ElementValuePairContext = ElementValuePairContext;

NELParser.prototype.elementValuePair = function() {

    var localctx = new ElementValuePairContext(this, this._ctx, this.state);
    this.enterRule(localctx, 68, NELParser.RULE_elementValuePair);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 400;
        this.match(NELParser.Identifier);
        this.state = 401;
        this.match(NELParser.ASSIGN);
        this.state = 402;
        this.elementValue();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ElementValueContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_elementValue;
    return this;
}

ElementValueContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ElementValueContext.prototype.constructor = ElementValueContext;

ElementValueContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

ElementValueContext.prototype.IntegerLiteral = function() {
    return this.getToken(NELParser.IntegerLiteral, 0);
};

ElementValueContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterElementValue(this);
	}
};

ElementValueContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitElementValue(this);
	}
};




NELParser.ElementValueContext = ElementValueContext;

NELParser.prototype.elementValue = function() {

    var localctx = new ElementValueContext(this, this._ctx, this.state);
    this.enterRule(localctx, 70, NELParser.RULE_elementValue);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 404;
        _la = this._input.LA(1);
        if(!(_la===NELParser.IntegerLiteral || _la===NELParser.StringLiteral)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ResourceContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_resource;
    return this;
}

ResourceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourceContext.prototype.constructor = ResourceContext;

ResourceContext.prototype.httpMethods = function() {
    return this.getTypedRuleContext(HttpMethodsContext,0);
};

ResourceContext.prototype.resourcePath = function() {
    return this.getTypedRuleContext(ResourcePathContext,0);
};

ResourceContext.prototype.resourceDeclaration = function() {
    return this.getTypedRuleContext(ResourceDeclarationContext,0);
};

ResourceContext.prototype.prodAnt = function() {
    return this.getTypedRuleContext(ProdAntContext,0);
};

ResourceContext.prototype.conAnt = function() {
    return this.getTypedRuleContext(ConAntContext,0);
};

ResourceContext.prototype.antApiOperation = function() {
    return this.getTypedRuleContext(AntApiOperationContext,0);
};

ResourceContext.prototype.antApiResponses = function() {
    return this.getTypedRuleContext(AntApiResponsesContext,0);
};

ResourceContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterResource(this);
	}
};

ResourceContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitResource(this);
	}
};




NELParser.ResourceContext = ResourceContext;

NELParser.prototype.resource = function() {

    var localctx = new ResourceContext(this, this._ctx, this.state);
    this.enterRule(localctx, 72, NELParser.RULE_resource);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 406;
        this.httpMethods();
        this.state = 408;
        var la_ = this._interp.adaptivePredict(this._input,32,this._ctx);
        if(la_===1) {
            this.state = 407;
            this.prodAnt();

        }
        this.state = 411;
        var la_ = this._interp.adaptivePredict(this._input,33,this._ctx);
        if(la_===1) {
            this.state = 410;
            this.conAnt();

        }
        this.state = 414;
        var la_ = this._interp.adaptivePredict(this._input,34,this._ctx);
        if(la_===1) {
            this.state = 413;
            this.antApiOperation();

        }
        this.state = 417;
        var la_ = this._interp.adaptivePredict(this._input,35,this._ctx);
        if(la_===1) {
            this.state = 416;
            this.antApiResponses();

        }
        this.state = 419;
        this.resourcePath();
        this.state = 420;
        this.resourceDeclaration();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function HttpMethodsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_httpMethods;
    return this;
}

HttpMethodsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
HttpMethodsContext.prototype.constructor = HttpMethodsContext;

HttpMethodsContext.prototype.getMethod = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(GetMethodContext);
    } else {
        return this.getTypedRuleContext(GetMethodContext,i);
    }
};

HttpMethodsContext.prototype.postMethod = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(PostMethodContext);
    } else {
        return this.getTypedRuleContext(PostMethodContext,i);
    }
};

HttpMethodsContext.prototype.putMethod = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(PutMethodContext);
    } else {
        return this.getTypedRuleContext(PutMethodContext,i);
    }
};

HttpMethodsContext.prototype.deleteMethod = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(DeleteMethodContext);
    } else {
        return this.getTypedRuleContext(DeleteMethodContext,i);
    }
};

HttpMethodsContext.prototype.headMethod = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(HeadMethodContext);
    } else {
        return this.getTypedRuleContext(HeadMethodContext,i);
    }
};

HttpMethodsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterHttpMethods(this);
	}
};

HttpMethodsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitHttpMethods(this);
	}
};




NELParser.HttpMethodsContext = HttpMethodsContext;

NELParser.prototype.httpMethods = function() {

    var localctx = new HttpMethodsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 74, NELParser.RULE_httpMethods);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 429;
        this._errHandler.sync(this);
        var _alt = this._interp.adaptivePredict(this._input,37,this._ctx)
        while(_alt!=2 && _alt!=antlr4.atn.ATN.INVALID_ALT_NUMBER) {
            if(_alt===1) {
                this.state = 427;
                var la_ = this._interp.adaptivePredict(this._input,36,this._ctx);
                switch(la_) {
                case 1:
                    this.state = 422;
                    this.getMethod();
                    break;

                case 2:
                    this.state = 423;
                    this.postMethod();
                    break;

                case 3:
                    this.state = 424;
                    this.putMethod();
                    break;

                case 4:
                    this.state = 425;
                    this.deleteMethod();
                    break;

                case 5:
                    this.state = 426;
                    this.headMethod();
                    break;

                } 
            }
            this.state = 431;
            this._errHandler.sync(this);
            _alt = this._interp.adaptivePredict(this._input,37,this._ctx);
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function QualifiedNameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_qualifiedName;
    return this;
}

QualifiedNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
QualifiedNameContext.prototype.constructor = QualifiedNameContext;

QualifiedNameContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


QualifiedNameContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterQualifiedName(this);
	}
};

QualifiedNameContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitQualifiedName(this);
	}
};




NELParser.QualifiedNameContext = QualifiedNameContext;

NELParser.prototype.qualifiedName = function() {

    var localctx = new QualifiedNameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 76, NELParser.RULE_qualifiedName);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 432;
        this.match(NELParser.Identifier);
        this.state = 437;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.DOT) {
            this.state = 433;
            this.match(NELParser.DOT);
            this.state = 434;
            this.match(NELParser.Identifier);
            this.state = 439;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ResourceDeclarationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_resourceDeclaration;
    return this;
}

ResourceDeclarationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourceDeclarationContext.prototype.constructor = ResourceDeclarationContext;

ResourceDeclarationContext.prototype.resourceName = function() {
    return this.getTypedRuleContext(ResourceNameContext,0);
};

ResourceDeclarationContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

ResourceDeclarationContext.prototype.block = function() {
    return this.getTypedRuleContext(BlockContext,0);
};

ResourceDeclarationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterResourceDeclaration(this);
	}
};

ResourceDeclarationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitResourceDeclaration(this);
	}
};




NELParser.ResourceDeclarationContext = ResourceDeclarationContext;

NELParser.prototype.resourceDeclaration = function() {

    var localctx = new ResourceDeclarationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 78, NELParser.RULE_resourceDeclaration);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 440;
        this.match(NELParser.T__19);
        this.state = 441;
        this.resourceName();
        this.state = 442;
        this.match(NELParser.LPAREN);
        this.state = 443;
        this.match(NELParser.T__20);
        this.state = 444;
        this.match(NELParser.Identifier);
        this.state = 445;
        this.match(NELParser.RPAREN);
        this.state = 446;
        this.block();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ResourceNameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_resourceName;
    return this;
}

ResourceNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ResourceNameContext.prototype.constructor = ResourceNameContext;

ResourceNameContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

ResourceNameContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterResourceName(this);
	}
};

ResourceNameContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitResourceName(this);
	}
};




NELParser.ResourceNameContext = ResourceNameContext;

NELParser.prototype.resourceName = function() {

    var localctx = new ResourceNameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 80, NELParser.RULE_resourceName);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 448;
        this.match(NELParser.Identifier);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function BlockContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_block;
    return this;
}

BlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
BlockContext.prototype.constructor = BlockContext;

BlockContext.prototype.blockStatement = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(BlockStatementContext);
    } else {
        return this.getTypedRuleContext(BlockStatementContext,i);
    }
};

BlockContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterBlock(this);
	}
};

BlockContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitBlock(this);
	}
};




NELParser.BlockContext = BlockContext;

NELParser.prototype.block = function() {

    var localctx = new BlockContext(this, this._ctx, this.state);
    this.enterRule(localctx, 82, NELParser.RULE_block);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 450;
        this.match(NELParser.LBRACE);
        this.state = 454;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(((((_la - 21)) & ~0x1f) == 0 && ((1 << (_la - 21)) & ((1 << (NELParser.T__20 - 21)) | (1 << (NELParser.T__25 - 21)) | (1 << (NELParser.T__26 - 21)) | (1 << (NELParser.T__27 - 21)) | (1 << (NELParser.T__28 - 21)) | (1 << (NELParser.T__33 - 21)) | (1 << (NELParser.BOOLEAN - 21)) | (1 << (NELParser.BYTE - 21)) | (1 << (NELParser.CHAR - 21)) | (1 << (NELParser.DOUBLE - 21)))) !== 0) || ((((_la - 55)) & ~0x1f) == 0 && ((1 << (_la - 55)) & ((1 << (NELParser.FLOAT - 55)) | (1 << (NELParser.IF - 55)) | (1 << (NELParser.INT - 55)) | (1 << (NELParser.LONG - 55)) | (1 << (NELParser.SHORT - 55)) | (1 << (NELParser.TRY - 55)))) !== 0) || _la===NELParser.Identifier) {
            this.state = 451;
            this.blockStatement();
            this.state = 456;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
        this.state = 457;
        this.match(NELParser.RBRACE);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function BlockStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_blockStatement;
    return this;
}

BlockStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
BlockStatementContext.prototype.constructor = BlockStatementContext;

BlockStatementContext.prototype.localVariableDeclarationStatement = function() {
    return this.getTypedRuleContext(LocalVariableDeclarationStatementContext,0);
};

BlockStatementContext.prototype.localVariableInitializationStatement = function() {
    return this.getTypedRuleContext(LocalVariableInitializationStatementContext,0);
};

BlockStatementContext.prototype.localVariableAssignmentStatement = function() {
    return this.getTypedRuleContext(LocalVariableAssignmentStatementContext,0);
};

BlockStatementContext.prototype.messageModificationStatement = function() {
    return this.getTypedRuleContext(MessageModificationStatementContext,0);
};

BlockStatementContext.prototype.returnStatement = function() {
    return this.getTypedRuleContext(ReturnStatementContext,0);
};

BlockStatementContext.prototype.mediatorCallStatement = function() {
    return this.getTypedRuleContext(MediatorCallStatementContext,0);
};

BlockStatementContext.prototype.tryCatchBlock = function() {
    return this.getTypedRuleContext(TryCatchBlockContext,0);
};

BlockStatementContext.prototype.ifElseBlock = function() {
    return this.getTypedRuleContext(IfElseBlockContext,0);
};

BlockStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterBlockStatement(this);
	}
};

BlockStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitBlockStatement(this);
	}
};




NELParser.BlockStatementContext = BlockStatementContext;

NELParser.prototype.blockStatement = function() {

    var localctx = new BlockStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 84, NELParser.RULE_blockStatement);
    try {
        this.state = 467;
        var la_ = this._interp.adaptivePredict(this._input,40,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 459;
            this.localVariableDeclarationStatement();
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 460;
            this.localVariableInitializationStatement();
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 461;
            this.localVariableAssignmentStatement();
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 462;
            this.messageModificationStatement();
            break;

        case 5:
            this.enterOuterAlt(localctx, 5);
            this.state = 463;
            this.returnStatement();
            break;

        case 6:
            this.enterOuterAlt(localctx, 6);
            this.state = 464;
            this.mediatorCallStatement();
            break;

        case 7:
            this.enterOuterAlt(localctx, 7);
            this.state = 465;
            this.tryCatchBlock();
            break;

        case 8:
            this.enterOuterAlt(localctx, 8);
            this.state = 466;
            this.ifElseBlock();
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TryCatchBlockContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_tryCatchBlock;
    return this;
}

TryCatchBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TryCatchBlockContext.prototype.constructor = TryCatchBlockContext;

TryCatchBlockContext.prototype.tryClause = function() {
    return this.getTypedRuleContext(TryClauseContext,0);
};

TryCatchBlockContext.prototype.catchClause = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(CatchClauseContext);
    } else {
        return this.getTypedRuleContext(CatchClauseContext,i);
    }
};

TryCatchBlockContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterTryCatchBlock(this);
	}
};

TryCatchBlockContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitTryCatchBlock(this);
	}
};




NELParser.TryCatchBlockContext = TryCatchBlockContext;

NELParser.prototype.tryCatchBlock = function() {

    var localctx = new TryCatchBlockContext(this, this._ctx, this.state);
    this.enterRule(localctx, 86, NELParser.RULE_tryCatchBlock);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 469;
        this.tryClause();
        this.state = 471; 
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        do {
            this.state = 470;
            this.catchClause();
            this.state = 473; 
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        } while(_la===NELParser.CATCH);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TryClauseContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_tryClause;
    return this;
}

TryClauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TryClauseContext.prototype.constructor = TryClauseContext;

TryClauseContext.prototype.block = function() {
    return this.getTypedRuleContext(BlockContext,0);
};

TryClauseContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterTryClause(this);
	}
};

TryClauseContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitTryClause(this);
	}
};




NELParser.TryClauseContext = TryClauseContext;

NELParser.prototype.tryClause = function() {

    var localctx = new TryClauseContext(this, this._ctx, this.state);
    this.enterRule(localctx, 88, NELParser.RULE_tryClause);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 475;
        this.match(NELParser.TRY);
        this.state = 476;
        this.block();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function CatchClauseContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_catchClause;
    return this;
}

CatchClauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
CatchClauseContext.prototype.constructor = CatchClauseContext;

CatchClauseContext.prototype.exceptionHandler = function() {
    return this.getTypedRuleContext(ExceptionHandlerContext,0);
};

CatchClauseContext.prototype.block = function() {
    return this.getTypedRuleContext(BlockContext,0);
};

CatchClauseContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterCatchClause(this);
	}
};

CatchClauseContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitCatchClause(this);
	}
};




NELParser.CatchClauseContext = CatchClauseContext;

NELParser.prototype.catchClause = function() {

    var localctx = new CatchClauseContext(this, this._ctx, this.state);
    this.enterRule(localctx, 90, NELParser.RULE_catchClause);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 478;
        this.match(NELParser.CATCH);
        this.state = 479;
        this.match(NELParser.LPAREN);
        this.state = 480;
        this.exceptionHandler();
        this.state = 481;
        this.match(NELParser.RPAREN);
        this.state = 482;
        this.block();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ExceptionHandlerContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_exceptionHandler;
    return this;
}

ExceptionHandlerContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ExceptionHandlerContext.prototype.constructor = ExceptionHandlerContext;

ExceptionHandlerContext.prototype.exceptionType = function() {
    return this.getTypedRuleContext(ExceptionTypeContext,0);
};

ExceptionHandlerContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

ExceptionHandlerContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterExceptionHandler(this);
	}
};

ExceptionHandlerContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitExceptionHandler(this);
	}
};




NELParser.ExceptionHandlerContext = ExceptionHandlerContext;

NELParser.prototype.exceptionHandler = function() {

    var localctx = new ExceptionHandlerContext(this, this._ctx, this.state);
    this.enterRule(localctx, 92, NELParser.RULE_exceptionHandler);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 484;
        this.exceptionType();
        this.state = 485;
        this.match(NELParser.Identifier);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ExceptionTypeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_exceptionType;
    return this;
}

ExceptionTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ExceptionTypeContext.prototype.constructor = ExceptionTypeContext;


ExceptionTypeContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterExceptionType(this);
	}
};

ExceptionTypeContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitExceptionType(this);
	}
};




NELParser.ExceptionTypeContext = ExceptionTypeContext;

NELParser.prototype.exceptionType = function() {

    var localctx = new ExceptionTypeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 94, NELParser.RULE_exceptionType);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 487;
        _la = this._input.LA(1);
        if(!((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << NELParser.T__21) | (1 << NELParser.T__22) | (1 << NELParser.T__23) | (1 << NELParser.T__24))) !== 0))) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function IfElseBlockContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_ifElseBlock;
    return this;
}

IfElseBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
IfElseBlockContext.prototype.constructor = IfElseBlockContext;

IfElseBlockContext.prototype.ifBlock = function() {
    return this.getTypedRuleContext(IfBlockContext,0);
};

IfElseBlockContext.prototype.elseBlock = function() {
    return this.getTypedRuleContext(ElseBlockContext,0);
};

IfElseBlockContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterIfElseBlock(this);
	}
};

IfElseBlockContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitIfElseBlock(this);
	}
};




NELParser.IfElseBlockContext = IfElseBlockContext;

NELParser.prototype.ifElseBlock = function() {

    var localctx = new IfElseBlockContext(this, this._ctx, this.state);
    this.enterRule(localctx, 96, NELParser.RULE_ifElseBlock);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 489;
        this.ifBlock();
        this.state = 491;
        _la = this._input.LA(1);
        if(_la===NELParser.ELSE) {
            this.state = 490;
            this.elseBlock();
        }

    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function IfBlockContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_ifBlock;
    return this;
}

IfBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
IfBlockContext.prototype.constructor = IfBlockContext;

IfBlockContext.prototype.parExpression = function() {
    return this.getTypedRuleContext(ParExpressionContext,0);
};

IfBlockContext.prototype.block = function() {
    return this.getTypedRuleContext(BlockContext,0);
};

IfBlockContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterIfBlock(this);
	}
};

IfBlockContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitIfBlock(this);
	}
};




NELParser.IfBlockContext = IfBlockContext;

NELParser.prototype.ifBlock = function() {

    var localctx = new IfBlockContext(this, this._ctx, this.state);
    this.enterRule(localctx, 98, NELParser.RULE_ifBlock);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 493;
        this.match(NELParser.IF);
        this.state = 494;
        this.parExpression();
        this.state = 495;
        this.block();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ElseBlockContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_elseBlock;
    return this;
}

ElseBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ElseBlockContext.prototype.constructor = ElseBlockContext;

ElseBlockContext.prototype.block = function() {
    return this.getTypedRuleContext(BlockContext,0);
};

ElseBlockContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterElseBlock(this);
	}
};

ElseBlockContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitElseBlock(this);
	}
};




NELParser.ElseBlockContext = ElseBlockContext;

NELParser.prototype.elseBlock = function() {

    var localctx = new ElseBlockContext(this, this._ctx, this.state);
    this.enterRule(localctx, 100, NELParser.RULE_elseBlock);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 497;
        this.match(NELParser.ELSE);
        this.state = 498;
        this.block();
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function LocalVariableDeclarationStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_localVariableDeclarationStatement;
    return this;
}

LocalVariableDeclarationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
LocalVariableDeclarationStatementContext.prototype.constructor = LocalVariableDeclarationStatementContext;

LocalVariableDeclarationStatementContext.prototype.endpointDeclaration = function() {
    return this.getTypedRuleContext(EndpointDeclarationContext,0);
};

LocalVariableDeclarationStatementContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

LocalVariableDeclarationStatementContext.prototype.type = function() {
    return this.getTypedRuleContext(TypeContext,0);
};

LocalVariableDeclarationStatementContext.prototype.classType = function() {
    return this.getTypedRuleContext(ClassTypeContext,0);
};

LocalVariableDeclarationStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterLocalVariableDeclarationStatement(this);
	}
};

LocalVariableDeclarationStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitLocalVariableDeclarationStatement(this);
	}
};




NELParser.LocalVariableDeclarationStatementContext = LocalVariableDeclarationStatementContext;

NELParser.prototype.localVariableDeclarationStatement = function() {

    var localctx = new LocalVariableDeclarationStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 102, NELParser.RULE_localVariableDeclarationStatement);
    try {
        this.state = 510;
        var la_ = this._interp.adaptivePredict(this._input,44,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 500;
            this.endpointDeclaration();
            this.state = 501;
            this.match(NELParser.SEMI);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 505;
            switch(this._input.LA(1)) {
            case NELParser.T__33:
            case NELParser.BOOLEAN:
            case NELParser.BYTE:
            case NELParser.CHAR:
            case NELParser.DOUBLE:
            case NELParser.FLOAT:
            case NELParser.INT:
            case NELParser.LONG:
            case NELParser.SHORT:
                this.state = 503;
                this.type();
                break;
            case NELParser.T__20:
            case NELParser.T__25:
                this.state = 504;
                this.classType();
                break;
            default:
                throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 507;
            this.match(NELParser.Identifier);
            this.state = 508;
            this.match(NELParser.SEMI);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function LocalVariableInitializationStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_localVariableInitializationStatement;
    return this;
}

LocalVariableInitializationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
LocalVariableInitializationStatementContext.prototype.constructor = LocalVariableInitializationStatementContext;

LocalVariableInitializationStatementContext.prototype.type = function() {
    return this.getTypedRuleContext(TypeContext,0);
};

LocalVariableInitializationStatementContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

LocalVariableInitializationStatementContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

LocalVariableInitializationStatementContext.prototype.endpointDeclaration = function() {
    return this.getTypedRuleContext(EndpointDeclarationContext,0);
};

LocalVariableInitializationStatementContext.prototype.newTypeObjectCreation = function() {
    return this.getTypedRuleContext(NewTypeObjectCreationContext,0);
};

LocalVariableInitializationStatementContext.prototype.classType = function() {
    return this.getTypedRuleContext(ClassTypeContext,0);
};

LocalVariableInitializationStatementContext.prototype.mediatorCall = function() {
    return this.getTypedRuleContext(MediatorCallContext,0);
};

LocalVariableInitializationStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterLocalVariableInitializationStatement(this);
	}
};

LocalVariableInitializationStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitLocalVariableInitializationStatement(this);
	}
};




NELParser.LocalVariableInitializationStatementContext = LocalVariableInitializationStatementContext;

NELParser.prototype.localVariableInitializationStatement = function() {

    var localctx = new LocalVariableInitializationStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 104, NELParser.RULE_localVariableInitializationStatement);
    try {
        this.state = 535;
        var la_ = this._interp.adaptivePredict(this._input,45,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 512;
            this.type();
            this.state = 513;
            this.match(NELParser.Identifier);
            this.state = 514;
            this.match(NELParser.ASSIGN);
            this.state = 515;
            this.literal();
            this.state = 516;
            this.match(NELParser.SEMI);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 518;
            this.endpointDeclaration();
            this.state = 519;
            this.match(NELParser.ASSIGN);
            this.state = 520;
            this.newTypeObjectCreation();
            this.state = 521;
            this.match(NELParser.SEMI);
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 523;
            this.classType();
            this.state = 524;
            this.match(NELParser.Identifier);
            this.state = 525;
            this.match(NELParser.ASSIGN);
            this.state = 526;
            this.newTypeObjectCreation();
            this.state = 527;
            this.match(NELParser.SEMI);
            break;

        case 4:
            this.enterOuterAlt(localctx, 4);
            this.state = 529;
            this.classType();
            this.state = 530;
            this.match(NELParser.Identifier);
            this.state = 531;
            this.match(NELParser.ASSIGN);
            this.state = 532;
            this.mediatorCall();
            this.state = 533;
            this.match(NELParser.SEMI);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function LocalVariableAssignmentStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_localVariableAssignmentStatement;
    return this;
}

LocalVariableAssignmentStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
LocalVariableAssignmentStatementContext.prototype.constructor = LocalVariableAssignmentStatementContext;

LocalVariableAssignmentStatementContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

LocalVariableAssignmentStatementContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

LocalVariableAssignmentStatementContext.prototype.newTypeObjectCreation = function() {
    return this.getTypedRuleContext(NewTypeObjectCreationContext,0);
};

LocalVariableAssignmentStatementContext.prototype.mediatorCall = function() {
    return this.getTypedRuleContext(MediatorCallContext,0);
};

LocalVariableAssignmentStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterLocalVariableAssignmentStatement(this);
	}
};

LocalVariableAssignmentStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitLocalVariableAssignmentStatement(this);
	}
};




NELParser.LocalVariableAssignmentStatementContext = LocalVariableAssignmentStatementContext;

NELParser.prototype.localVariableAssignmentStatement = function() {

    var localctx = new LocalVariableAssignmentStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 106, NELParser.RULE_localVariableAssignmentStatement);
    try {
        this.state = 552;
        var la_ = this._interp.adaptivePredict(this._input,46,this._ctx);
        switch(la_) {
        case 1:
            this.enterOuterAlt(localctx, 1);
            this.state = 537;
            this.match(NELParser.Identifier);
            this.state = 538;
            this.match(NELParser.ASSIGN);
            this.state = 539;
            this.literal();
            this.state = 540;
            this.match(NELParser.SEMI);
            break;

        case 2:
            this.enterOuterAlt(localctx, 2);
            this.state = 542;
            this.match(NELParser.Identifier);
            this.state = 543;
            this.match(NELParser.ASSIGN);
            this.state = 544;
            this.newTypeObjectCreation();
            this.state = 545;
            this.match(NELParser.SEMI);
            break;

        case 3:
            this.enterOuterAlt(localctx, 3);
            this.state = 547;
            this.match(NELParser.Identifier);
            this.state = 548;
            this.match(NELParser.ASSIGN);
            this.state = 549;
            this.mediatorCall();
            this.state = 550;
            this.match(NELParser.SEMI);
            break;

        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MediatorCallStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_mediatorCallStatement;
    return this;
}

MediatorCallStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MediatorCallStatementContext.prototype.constructor = MediatorCallStatementContext;

MediatorCallStatementContext.prototype.mediatorCall = function() {
    return this.getTypedRuleContext(MediatorCallContext,0);
};

MediatorCallStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterMediatorCallStatement(this);
	}
};

MediatorCallStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitMediatorCallStatement(this);
	}
};




NELParser.MediatorCallStatementContext = MediatorCallStatementContext;

NELParser.prototype.mediatorCallStatement = function() {

    var localctx = new MediatorCallStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 108, NELParser.RULE_mediatorCallStatement);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 554;
        this.mediatorCall();
        this.state = 555;
        this.match(NELParser.SEMI);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function NewTypeObjectCreationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_newTypeObjectCreation;
    return this;
}

NewTypeObjectCreationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
NewTypeObjectCreationContext.prototype.constructor = NewTypeObjectCreationContext;

NewTypeObjectCreationContext.prototype.classType = function() {
    return this.getTypedRuleContext(ClassTypeContext,0);
};

NewTypeObjectCreationContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

NewTypeObjectCreationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterNewTypeObjectCreation(this);
	}
};

NewTypeObjectCreationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitNewTypeObjectCreation(this);
	}
};




NELParser.NewTypeObjectCreationContext = NewTypeObjectCreationContext;

NELParser.prototype.newTypeObjectCreation = function() {

    var localctx = new NewTypeObjectCreationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 110, NELParser.RULE_newTypeObjectCreation);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 557;
        this.match(NELParser.NEW);
        this.state = 558;
        this.classType();
        this.state = 559;
        this.match(NELParser.LPAREN);
        this.state = 561;
        _la = this._input.LA(1);
        if(((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (NELParser.IntegerLiteral - 86)) | (1 << (NELParser.FloatingPointLiteral - 86)) | (1 << (NELParser.BooleanLiteral - 86)) | (1 << (NELParser.CharacterLiteral - 86)) | (1 << (NELParser.StringLiteral - 86)) | (1 << (NELParser.NullLiteral - 86)))) !== 0)) {
            this.state = 560;
            this.literal();
        }

        this.state = 563;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MediatorCallContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_mediatorCall;
    return this;
}

MediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MediatorCallContext.prototype.constructor = MediatorCallContext;

MediatorCallContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

MediatorCallContext.prototype.keyValuePairs = function() {
    return this.getTypedRuleContext(KeyValuePairsContext,0);
};

MediatorCallContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterMediatorCall(this);
	}
};

MediatorCallContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitMediatorCall(this);
	}
};




NELParser.MediatorCallContext = MediatorCallContext;

NELParser.prototype.mediatorCall = function() {

    var localctx = new MediatorCallContext(this, this._ctx, this.state);
    this.enterRule(localctx, 112, NELParser.RULE_mediatorCall);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 565;
        this.match(NELParser.Identifier);
        this.state = 566;
        this.match(NELParser.LPAREN);
        this.state = 568;
        _la = this._input.LA(1);
        if(_la===NELParser.T__20 || _la===NELParser.T__25 || _la===NELParser.Identifier) {
            this.state = 567;
            this.keyValuePairs();
        }

        this.state = 570;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function EndpointDeclarationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_endpointDeclaration;
    return this;
}

EndpointDeclarationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
EndpointDeclarationContext.prototype.constructor = EndpointDeclarationContext;

EndpointDeclarationContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

EndpointDeclarationContext.prototype.parametersAnnotation = function() {
    return this.getTypedRuleContext(ParametersAnnotationContext,0);
};

EndpointDeclarationContext.prototype.circuitBreakerAnnotation = function() {
    return this.getTypedRuleContext(CircuitBreakerAnnotationContext,0);
};

EndpointDeclarationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterEndpointDeclaration(this);
	}
};

EndpointDeclarationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitEndpointDeclaration(this);
	}
};




NELParser.EndpointDeclarationContext = EndpointDeclarationContext;

NELParser.prototype.endpointDeclaration = function() {

    var localctx = new EndpointDeclarationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 114, NELParser.RULE_endpointDeclaration);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 573;
        _la = this._input.LA(1);
        if(_la===NELParser.T__26) {
            this.state = 572;
            this.parametersAnnotation();
        }

        this.state = 576;
        _la = this._input.LA(1);
        if(_la===NELParser.T__27) {
            this.state = 575;
            this.circuitBreakerAnnotation();
        }

        this.state = 578;
        this.match(NELParser.T__25);
        this.state = 579;
        this.match(NELParser.Identifier);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ParametersAnnotationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_parametersAnnotation;
    return this;
}

ParametersAnnotationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ParametersAnnotationContext.prototype.constructor = ParametersAnnotationContext;

ParametersAnnotationContext.prototype.keyValuePairs = function() {
    return this.getTypedRuleContext(KeyValuePairsContext,0);
};

ParametersAnnotationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterParametersAnnotation(this);
	}
};

ParametersAnnotationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitParametersAnnotation(this);
	}
};




NELParser.ParametersAnnotationContext = ParametersAnnotationContext;

NELParser.prototype.parametersAnnotation = function() {

    var localctx = new ParametersAnnotationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 116, NELParser.RULE_parametersAnnotation);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 581;
        this.match(NELParser.T__26);
        this.state = 582;
        this.match(NELParser.LPAREN);
        this.state = 584;
        _la = this._input.LA(1);
        if(_la===NELParser.T__20 || _la===NELParser.T__25 || _la===NELParser.Identifier) {
            this.state = 583;
            this.keyValuePairs();
        }

        this.state = 586;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function CircuitBreakerAnnotationContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_circuitBreakerAnnotation;
    return this;
}

CircuitBreakerAnnotationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
CircuitBreakerAnnotationContext.prototype.constructor = CircuitBreakerAnnotationContext;

CircuitBreakerAnnotationContext.prototype.keyValuePairs = function() {
    return this.getTypedRuleContext(KeyValuePairsContext,0);
};

CircuitBreakerAnnotationContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterCircuitBreakerAnnotation(this);
	}
};

CircuitBreakerAnnotationContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitCircuitBreakerAnnotation(this);
	}
};




NELParser.CircuitBreakerAnnotationContext = CircuitBreakerAnnotationContext;

NELParser.prototype.circuitBreakerAnnotation = function() {

    var localctx = new CircuitBreakerAnnotationContext(this, this._ctx, this.state);
    this.enterRule(localctx, 118, NELParser.RULE_circuitBreakerAnnotation);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 588;
        this.match(NELParser.T__27);
        this.state = 589;
        this.match(NELParser.LPAREN);
        this.state = 591;
        _la = this._input.LA(1);
        if(_la===NELParser.T__20 || _la===NELParser.T__25 || _la===NELParser.Identifier) {
            this.state = 590;
            this.keyValuePairs();
        }

        this.state = 593;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function KeyValuePairsContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_keyValuePairs;
    return this;
}

KeyValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
KeyValuePairsContext.prototype.constructor = KeyValuePairsContext;

KeyValuePairsContext.prototype.keyValuePair = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(KeyValuePairContext);
    } else {
        return this.getTypedRuleContext(KeyValuePairContext,i);
    }
};

KeyValuePairsContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterKeyValuePairs(this);
	}
};

KeyValuePairsContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitKeyValuePairs(this);
	}
};




NELParser.KeyValuePairsContext = KeyValuePairsContext;

NELParser.prototype.keyValuePairs = function() {

    var localctx = new KeyValuePairsContext(this, this._ctx, this.state);
    this.enterRule(localctx, 120, NELParser.RULE_keyValuePairs);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 595;
        this.keyValuePair();
        this.state = 600;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.COMMA) {
            this.state = 596;
            this.match(NELParser.COMMA);
            this.state = 597;
            this.keyValuePair();
            this.state = 602;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function KeyValuePairContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_keyValuePair;
    return this;
}

KeyValuePairContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
KeyValuePairContext.prototype.constructor = KeyValuePairContext;

KeyValuePairContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


KeyValuePairContext.prototype.classType = function() {
    return this.getTypedRuleContext(ClassTypeContext,0);
};

KeyValuePairContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

KeyValuePairContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterKeyValuePair(this);
	}
};

KeyValuePairContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitKeyValuePair(this);
	}
};




NELParser.KeyValuePairContext = KeyValuePairContext;

NELParser.prototype.keyValuePair = function() {

    var localctx = new KeyValuePairContext(this, this._ctx, this.state);
    this.enterRule(localctx, 122, NELParser.RULE_keyValuePair);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 605;
        switch(this._input.LA(1)) {
        case NELParser.Identifier:
            this.state = 603;
            this.match(NELParser.Identifier);
            break;
        case NELParser.T__20:
        case NELParser.T__25:
            this.state = 604;
            this.classType();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
        this.state = 607;
        this.match(NELParser.ASSIGN);
        this.state = 610;
        switch(this._input.LA(1)) {
        case NELParser.IntegerLiteral:
        case NELParser.FloatingPointLiteral:
        case NELParser.BooleanLiteral:
        case NELParser.CharacterLiteral:
        case NELParser.StringLiteral:
        case NELParser.NullLiteral:
            this.state = 608;
            this.literal();
            break;
        case NELParser.Identifier:
            this.state = 609;
            this.match(NELParser.Identifier);
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MessageModificationStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_messageModificationStatement;
    return this;
}

MessageModificationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MessageModificationStatementContext.prototype.constructor = MessageModificationStatementContext;

MessageModificationStatementContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


MessageModificationStatementContext.prototype.messagePropertyName = function() {
    return this.getTypedRuleContext(MessagePropertyNameContext,0);
};

MessageModificationStatementContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

MessageModificationStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterMessageModificationStatement(this);
	}
};

MessageModificationStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitMessageModificationStatement(this);
	}
};




NELParser.MessageModificationStatementContext = MessageModificationStatementContext;

NELParser.prototype.messageModificationStatement = function() {

    var localctx = new MessageModificationStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 124, NELParser.RULE_messageModificationStatement);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 612;
        this.match(NELParser.Identifier);
        this.state = 613;
        this.match(NELParser.DOT);
        this.state = 614;
        this.match(NELParser.Identifier);
        this.state = 615;
        this.match(NELParser.LPAREN);
        this.state = 616;
        this.messagePropertyName();
        this.state = 617;
        this.match(NELParser.COMMA);
        this.state = 618;
        this.literal();
        this.state = 619;
        this.match(NELParser.RPAREN);
        this.state = 620;
        this.match(NELParser.SEMI);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ReturnStatementContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_returnStatement;
    return this;
}

ReturnStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ReturnStatementContext.prototype.constructor = ReturnStatementContext;

ReturnStatementContext.prototype.Identifier = function() {
    return this.getToken(NELParser.Identifier, 0);
};

ReturnStatementContext.prototype.mediatorCall = function() {
    return this.getTypedRuleContext(MediatorCallContext,0);
};

ReturnStatementContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterReturnStatement(this);
	}
};

ReturnStatementContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitReturnStatement(this);
	}
};




NELParser.ReturnStatementContext = ReturnStatementContext;

NELParser.prototype.returnStatement = function() {

    var localctx = new ReturnStatementContext(this, this._ctx, this.state);
    this.enterRule(localctx, 126, NELParser.RULE_returnStatement);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 622;
        this.match(NELParser.T__28);
        this.state = 625;
        var la_ = this._interp.adaptivePredict(this._input,56,this._ctx);
        if(la_===1) {
            this.state = 623;
            this.match(NELParser.Identifier);

        } else if(la_===2) {
            this.state = 624;
            this.mediatorCall();

        }
        this.state = 627;
        this.match(NELParser.SEMI);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ParExpressionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_parExpression;
    return this;
}

ParExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ParExpressionContext.prototype.constructor = ParExpressionContext;

ParExpressionContext.prototype.expression = function(i) {
    if(i===undefined) {
        i = null;
    }
    if(i===null) {
        return this.getTypedRuleContexts(ExpressionContext);
    } else {
        return this.getTypedRuleContext(ExpressionContext,i);
    }
};

ParExpressionContext.prototype.GT = function() {
    return this.getToken(NELParser.GT, 0);
};

ParExpressionContext.prototype.LT = function() {
    return this.getToken(NELParser.LT, 0);
};

ParExpressionContext.prototype.EQUAL = function() {
    return this.getToken(NELParser.EQUAL, 0);
};

ParExpressionContext.prototype.LE = function() {
    return this.getToken(NELParser.LE, 0);
};

ParExpressionContext.prototype.GE = function() {
    return this.getToken(NELParser.GE, 0);
};

ParExpressionContext.prototype.NOTEQUAL = function() {
    return this.getToken(NELParser.NOTEQUAL, 0);
};

ParExpressionContext.prototype.AND = function() {
    return this.getToken(NELParser.AND, 0);
};

ParExpressionContext.prototype.OR = function() {
    return this.getToken(NELParser.OR, 0);
};

ParExpressionContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterParExpression(this);
	}
};

ParExpressionContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitParExpression(this);
	}
};




NELParser.ParExpressionContext = ParExpressionContext;

NELParser.prototype.parExpression = function() {

    var localctx = new ParExpressionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 128, NELParser.RULE_parExpression);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 629;
        this.match(NELParser.LPAREN);
        this.state = 630;
        this.expression();
        this.state = 633;
        _la = this._input.LA(1);
        if(((((_la - 102)) & ~0x1f) == 0 && ((1 << (_la - 102)) & ((1 << (NELParser.GT - 102)) | (1 << (NELParser.LT - 102)) | (1 << (NELParser.EQUAL - 102)) | (1 << (NELParser.LE - 102)) | (1 << (NELParser.GE - 102)) | (1 << (NELParser.NOTEQUAL - 102)) | (1 << (NELParser.AND - 102)) | (1 << (NELParser.OR - 102)))) !== 0)) {
            this.state = 631;
            _la = this._input.LA(1);
            if(!(((((_la - 102)) & ~0x1f) == 0 && ((1 << (_la - 102)) & ((1 << (NELParser.GT - 102)) | (1 << (NELParser.LT - 102)) | (1 << (NELParser.EQUAL - 102)) | (1 << (NELParser.LE - 102)) | (1 << (NELParser.GE - 102)) | (1 << (NELParser.NOTEQUAL - 102)) | (1 << (NELParser.AND - 102)) | (1 << (NELParser.OR - 102)))) !== 0))) {
            this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
            this.state = 632;
            this.expression();
        }

        this.state = 635;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ExpressionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_expression;
    return this;
}

ExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ExpressionContext.prototype.constructor = ExpressionContext;

ExpressionContext.prototype.evalExpression = function() {
    return this.getTypedRuleContext(EvalExpressionContext,0);
};

ExpressionContext.prototype.literal = function() {
    return this.getTypedRuleContext(LiteralContext,0);
};

ExpressionContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterExpression(this);
	}
};

ExpressionContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitExpression(this);
	}
};




NELParser.ExpressionContext = ExpressionContext;

NELParser.prototype.expression = function() {

    var localctx = new ExpressionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 130, NELParser.RULE_expression);
    try {
        this.state = 639;
        switch(this._input.LA(1)) {
        case NELParser.T__29:
            this.enterOuterAlt(localctx, 1);
            this.state = 637;
            this.evalExpression();
            break;
        case NELParser.IntegerLiteral:
        case NELParser.FloatingPointLiteral:
        case NELParser.BooleanLiteral:
        case NELParser.CharacterLiteral:
        case NELParser.StringLiteral:
        case NELParser.NullLiteral:
            this.enterOuterAlt(localctx, 2);
            this.state = 638;
            this.literal();
            break;
        default:
            throw new antlr4.error.NoViableAltException(this);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function EvalExpressionContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_evalExpression;
    return this;
}

EvalExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
EvalExpressionContext.prototype.constructor = EvalExpressionContext;

EvalExpressionContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


EvalExpressionContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

EvalExpressionContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterEvalExpression(this);
	}
};

EvalExpressionContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitEvalExpression(this);
	}
};




NELParser.EvalExpressionContext = EvalExpressionContext;

NELParser.prototype.evalExpression = function() {

    var localctx = new EvalExpressionContext(this, this._ctx, this.state);
    this.enterRule(localctx, 132, NELParser.RULE_evalExpression);
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 641;
        this.match(NELParser.T__29);
        this.state = 642;
        this.match(NELParser.LPAREN);
        this.state = 643;
        this.match(NELParser.Identifier);
        this.state = 644;
        this.match(NELParser.ASSIGN);
        this.state = 645;
        this.match(NELParser.Identifier);
        this.state = 646;
        this.match(NELParser.COMMA);
        this.state = 647;
        this.match(NELParser.T__30);
        this.state = 648;
        this.match(NELParser.ASSIGN);
        this.state = 649;
        this.match(NELParser.StringLiteral);
        this.state = 650;
        this.match(NELParser.RPAREN);
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function LiteralContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_literal;
    return this;
}

LiteralContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
LiteralContext.prototype.constructor = LiteralContext;

LiteralContext.prototype.IntegerLiteral = function() {
    return this.getToken(NELParser.IntegerLiteral, 0);
};

LiteralContext.prototype.FloatingPointLiteral = function() {
    return this.getToken(NELParser.FloatingPointLiteral, 0);
};

LiteralContext.prototype.CharacterLiteral = function() {
    return this.getToken(NELParser.CharacterLiteral, 0);
};

LiteralContext.prototype.StringLiteral = function() {
    return this.getToken(NELParser.StringLiteral, 0);
};

LiteralContext.prototype.BooleanLiteral = function() {
    return this.getToken(NELParser.BooleanLiteral, 0);
};

LiteralContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterLiteral(this);
	}
};

LiteralContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitLiteral(this);
	}
};




NELParser.LiteralContext = LiteralContext;

NELParser.prototype.literal = function() {

    var localctx = new LiteralContext(this, this._ctx, this.state);
    this.enterRule(localctx, 134, NELParser.RULE_literal);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 652;
        _la = this._input.LA(1);
        if(!(((((_la - 86)) & ~0x1f) == 0 && ((1 << (_la - 86)) & ((1 << (NELParser.IntegerLiteral - 86)) | (1 << (NELParser.FloatingPointLiteral - 86)) | (1 << (NELParser.BooleanLiteral - 86)) | (1 << (NELParser.CharacterLiteral - 86)) | (1 << (NELParser.StringLiteral - 86)) | (1 << (NELParser.NullLiteral - 86)))) !== 0))) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MediaTypeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_mediaType;
    return this;
}

MediaTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MediaTypeContext.prototype.constructor = MediaTypeContext;


MediaTypeContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterMediaType(this);
	}
};

MediaTypeContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitMediaType(this);
	}
};




NELParser.MediaTypeContext = MediaTypeContext;

NELParser.prototype.mediaType = function() {

    var localctx = new MediaTypeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 136, NELParser.RULE_mediaType);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 654;
        _la = this._input.LA(1);
        if(!(_la===NELParser.T__31 || _la===NELParser.T__32)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function TypeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_type;
    return this;
}

TypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
TypeContext.prototype.constructor = TypeContext;


TypeContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterType(this);
	}
};

TypeContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitType(this);
	}
};




NELParser.TypeContext = TypeContext;

NELParser.prototype.type = function() {

    var localctx = new TypeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 138, NELParser.RULE_type);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 656;
        _la = this._input.LA(1);
        if(!(((((_la - 34)) & ~0x1f) == 0 && ((1 << (_la - 34)) & ((1 << (NELParser.T__33 - 34)) | (1 << (NELParser.BOOLEAN - 34)) | (1 << (NELParser.BYTE - 34)) | (1 << (NELParser.CHAR - 34)) | (1 << (NELParser.DOUBLE - 34)) | (1 << (NELParser.FLOAT - 34)) | (1 << (NELParser.INT - 34)) | (1 << (NELParser.LONG - 34)))) !== 0) || _la===NELParser.SHORT)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function ClassTypeContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_classType;
    return this;
}

ClassTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
ClassTypeContext.prototype.constructor = ClassTypeContext;


ClassTypeContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterClassType(this);
	}
};

ClassTypeContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitClassType(this);
	}
};




NELParser.ClassTypeContext = ClassTypeContext;

NELParser.prototype.classType = function() {

    var localctx = new ClassTypeContext(this, this._ctx, this.state);
    this.enterRule(localctx, 140, NELParser.RULE_classType);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 658;
        _la = this._input.LA(1);
        if(!(_la===NELParser.T__20 || _la===NELParser.T__25)) {
        this._errHandler.recoverInline(this);
        }
        else {
            this.consume();
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};

function MessagePropertyNameContext(parser, parent, invokingState) {
	if(parent===undefined) {
	    parent = null;
	}
	if(invokingState===undefined || invokingState===null) {
		invokingState = -1;
	}
	antlr4.ParserRuleContext.call(this, parent, invokingState);
    this.parser = parser;
    this.ruleIndex = NELParser.RULE_messagePropertyName;
    return this;
}

MessagePropertyNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
MessagePropertyNameContext.prototype.constructor = MessagePropertyNameContext;

MessagePropertyNameContext.prototype.Identifier = function(i) {
	if(i===undefined) {
		i = null;
	}
    if(i===null) {
        return this.getTokens(NELParser.Identifier);
    } else {
        return this.getToken(NELParser.Identifier, i);
    }
};


MessagePropertyNameContext.prototype.enterRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.enterMessagePropertyName(this);
	}
};

MessagePropertyNameContext.prototype.exitRule = function(listener) {
    if(listener instanceof NELListener ) {
        listener.exitMessagePropertyName(this);
	}
};




NELParser.MessagePropertyNameContext = MessagePropertyNameContext;

NELParser.prototype.messagePropertyName = function() {

    var localctx = new MessagePropertyNameContext(this, this._ctx, this.state);
    this.enterRule(localctx, 142, NELParser.RULE_messagePropertyName);
    var _la = 0; // Token type
    try {
        this.enterOuterAlt(localctx, 1);
        this.state = 660;
        this.match(NELParser.Identifier);
        this.state = 665;
        this._errHandler.sync(this);
        _la = this._input.LA(1);
        while(_la===NELParser.DOT) {
            this.state = 661;
            this.match(NELParser.DOT);
            this.state = 662;
            this.match(NELParser.Identifier);
            this.state = 667;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
        }
    } catch (re) {
    	if(re instanceof antlr4.error.RecognitionException) {
	        localctx.exception = re;
	        this._errHandler.reportError(this, re);
	        this._errHandler.recover(this, re);
	    } else {
	    	throw re;
	    }
    } finally {
        this.exitRule();
    }
    return localctx;
};


exports.NELParser = NELParser;
