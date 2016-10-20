// Generated from WUML.g4 by ANTLR 4.5.3
// jshint ignore: start
define(function(require, exports, module) {
    var antlr4 = require('../antlr4/index');
    var WUMLListener = require('./WUMLListener').WUMLListener;
    var grammarFileName = "WUML.g4";

    var serializedATN = ["\u0003\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd",
        "\u0003\u0090\u0273\u0004\u0002\t\u0002\u0004\u0003\t\u0003\u0004\u0004",
        "\t\u0004\u0004\u0005\t\u0005\u0004\u0006\t\u0006\u0004\u0007\t\u0007",
        "\u0004\b\t\b\u0004\t\t\t\u0004\n\t\n\u0004\u000b\t\u000b\u0004\f\t\f",
        "\u0004\r\t\r\u0004\u000e\t\u000e\u0004\u000f\t\u000f\u0004\u0010\t\u0010",
        "\u0004\u0011\t\u0011\u0004\u0012\t\u0012\u0004\u0013\t\u0013\u0004\u0014",
        "\t\u0014\u0004\u0015\t\u0015\u0004\u0016\t\u0016\u0004\u0017\t\u0017",
        "\u0004\u0018\t\u0018\u0004\u0019\t\u0019\u0004\u001a\t\u001a\u0004\u001b",
        "\t\u001b\u0004\u001c\t\u001c\u0004\u001d\t\u001d\u0004\u001e\t\u001e",
        "\u0004\u001f\t\u001f\u0004 \t \u0004!\t!\u0004\"\t\"\u0004#\t#\u0004",
        "$\t$\u0004%\t%\u0004&\t&\u0004\'\t\'\u0004(\t(\u0004)\t)\u0004*\t*\u0004",
        "+\t+\u0004,\t,\u0004-\t-\u0004.\t.\u0004/\t/\u00040\t0\u00041\t1\u0004",
        "2\t2\u00043\t3\u00044\t4\u00045\t5\u00046\t6\u00047\t7\u00048\t8\u0004",
        "9\t9\u0004:\t:\u0004;\t;\u0004<\t<\u0004=\t=\u0004>\t>\u0004?\t?\u0004",
        "@\t@\u0004A\tA\u0004B\tB\u0004C\tC\u0004D\tD\u0004E\tE\u0004F\tF\u0004",
        "G\tG\u0003\u0002\u0003\u0002\u0005\u0002\u0091\n\u0002\u0003\u0002\u0003",
        "\u0002\u0003\u0002\u0003\u0003\u0005\u0003\u0097\n\u0003\u0003\u0003",
        "\u0003\u0003\u0005\u0003\u009b\n\u0003\u0003\u0003\u0003\u0003\u0003",
        "\u0004\u0007\u0004\u00a0\n\u0004\f\u0004\u000e\u0004\u00a3\u000b\u0004",
        "\u0003\u0005\u0006\u0005\u00a6\n\u0005\r\u0005\u000e\u0005\u00a7\u0003",
        "\u0006\u0003\u0006\u0003\u0006\u0003\u0006\u0003\u0007\u0003\u0007\u0003",
        "\u0007\u0003\u0007\u0003\u0007\u0003\u0007\u0003\b\u0003\b\u0003\b\u0003",
        "\b\u0003\b\u0003\b\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003\t\u0003",
        "\n\u0003\n\u0003\n\u0003\n\u0003\n\u0003\n\u0003\u000b\u0003\u000b\u0003",
        "\u000b\u0003\u000b\u0005\u000b\u00ca\n\u000b\u0003\f\u0003\f\u0003\f",
        "\u0003\f\u0005\f\u00d0\n\f\u0003\r\u0003\r\u0003\r\u0003\r\u0005\r\u00d6",
        "\n\r\u0003\u000e\u0003\u000e\u0003\u000e\u0003\u000e\u0005\u000e\u00dc",
        "\n\u000e\u0003\u000f\u0003\u000f\u0003\u000f\u0003\u000f\u0005\u000f",
        "\u00e2\n\u000f\u0003\u0010\u0003\u0010\u0003\u0010\u0003\u0010\u0005",
        "\u0010\u00e8\n\u0010\u0003\u0010\u0005\u0010\u00eb\n\u0010\u0003\u0011",
        "\u0003\u0011\u0003\u0011\u0003\u0011\u0005\u0011\u00f1\n\u0011\u0003",
        "\u0011\u0005\u0011\u00f4\n\u0011\u0003\u0012\u0003\u0012\u0003\u0012",
        "\u0003\u0012\u0003\u0012\u0005\u0012\u00fb\n\u0012\u0003\u0012\u0005",
        "\u0012\u00fe\n\u0012\u0003\u0013\u0003\u0013\u0003\u0013\u0003\u0013",
        "\u0005\u0013\u0104\n\u0013\u0003\u0013\u0003\u0013\u0003\u0014\u0003",
        "\u0014\u0003\u0014\u0007\u0014\u010b\n\u0014\f\u0014\u000e\u0014\u010e",
        "\u000b\u0014\u0003\u0015\u0003\u0015\u0003\u0015\u0003\u0015\u0003\u0015",
        "\u0005\u0015\u0115\n\u0015\u0003\u0015\u0003\u0015\u0003\u0016\u0003",
        "\u0016\u0003\u0016\u0007\u0016\u011c\n\u0016\f\u0016\u000e\u0016\u011f",
        "\u000b\u0016\u0003\u0017\u0003\u0017\u0003\u0017\u0005\u0017\u0124\n",
        "\u0017\u0003\u0017\u0003\u0017\u0005\u0017\u0128\n\u0017\u0003\u0018",
        "\u0003\u0018\u0003\u0018\u0005\u0018\u012d\n\u0018\u0003\u0018\u0003",
        "\u0018\u0003\u0018\u0005\u0018\u0132\n\u0018\u0003\u0018\u0003\u0018",
        "\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u0019\u0003\u001a\u0003\u001a",
        "\u0003\u001a\u0003\u001a\u0003\u001b\u0003\u001b\u0003\u001b\u0003\u001b",
        "\u0003\u001c\u0003\u001c\u0003\u001c\u0006\u001c\u0145\n\u001c\r\u001c",
        "\u000e\u001c\u0146\u0003\u001d\u0003\u001d\u0003\u001d\u0003\u001d\u0007",
        "\u001d\u014d\n\u001d\f\u001d\u000e\u001d\u0150\u000b\u001d\u0003\u001d",
        "\u0003\u001d\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001e\u0003\u001f",
        "\u0003\u001f\u0003\u001f\u0003\u001f\u0003 \u0003 \u0003 \u0003 \u0003",
        " \u0003 \u0003 \u0003 \u0003 \u0003 \u0003 \u0003 \u0003 \u0003 \u0003",
        " \u0005 \u016b\n \u0003 \u0003 \u0003 \u0005 \u0170\n \u0003!\u0003",
        "!\u0003!\u0003!\u0003\"\u0003\"\u0003#\u0003#\u0005#\u017a\n#\u0003",
        "#\u0005#\u017d\n#\u0003#\u0005#\u0180\n#\u0003#\u0005#\u0183\n#\u0003",
        "#\u0003#\u0003#\u0003$\u0003$\u0003$\u0003$\u0003$\u0007$\u018d\n$\f",
        "$\u000e$\u0190\u000b$\u0003%\u0003%\u0003%\u0007%\u0195\n%\f%\u000e",
        "%\u0198\u000b%\u0003&\u0003&\u0003&\u0003&\u0003&\u0003&\u0003&\u0003",
        "&\u0003\'\u0003\'\u0003(\u0003(\u0007(\u01a6\n(\f(\u000e(\u01a9\u000b",
        "(\u0003(\u0003(\u0003)\u0003)\u0003)\u0003)\u0003)\u0003)\u0003)\u0003",
        ")\u0005)\u01b5\n)\u0003*\u0003*\u0006*\u01b9\n*\r*\u000e*\u01ba\u0003",
        "+\u0003+\u0003+\u0003,\u0003,\u0003,\u0003,\u0003,\u0003,\u0003-\u0003",
        "-\u0003-\u0003.\u0003.\u0003/\u0003/\u0005/\u01cd\n/\u00030\u00030\u0003",
        "0\u00030\u00031\u00031\u00031\u00032\u00032\u00052\u01d8\n2\u00032\u0003",
        "2\u00032\u00033\u00033\u00033\u00033\u00033\u00033\u00033\u00033\u0003",
        "3\u00033\u00033\u00053\u01e8\n3\u00034\u00034\u00034\u00034\u00034\u0003",
        "4\u00034\u00034\u00034\u00034\u00034\u00054\u01f5\n4\u00035\u00035\u0003",
        "5\u00036\u00056\u01fb\n6\u00036\u00036\u00036\u00036\u00036\u00036\u0003",
        "6\u00036\u00037\u00037\u00037\u00037\u00037\u00037\u00037\u00057\u020c",
        "\n7\u00038\u00038\u00038\u00038\u00038\u00038\u00038\u00039\u00039\u0003",
        "9\u00039\u00039\u00039\u00039\u0003:\u0003:\u0003:\u0003:\u0003:\u0003",
        ":\u0003:\u0003;\u0003;\u0003;\u0003;\u0003;\u0003;\u0003;\u0003<\u0003",
        "<\u0003<\u0003<\u0003<\u0003<\u0005<\u0230\n<\u0003<\u0003<\u0003=\u0003",
        "=\u0003=\u0003=\u0003=\u0003=\u0003=\u0003=\u0003=\u0005=\u023d\n=\u0003",
        ">\u0003>\u0003>\u0003>\u0003>\u0003>\u0003>\u0003>\u0003>\u0003>\u0003",
        "?\u0003?\u0003?\u0005?\u024c\n?\u0003?\u0003?\u0003@\u0003@\u0003@\u0003",
        "@\u0005@\u0254\n@\u0003@\u0003@\u0003A\u0003A\u0005A\u025a\nA\u0003",
        "B\u0003B\u0003B\u0003B\u0003B\u0003B\u0003B\u0003C\u0003C\u0003D\u0003",
        "D\u0003E\u0003E\u0003F\u0003F\u0003G\u0003G\u0003G\u0007G\u026e\nG\f",
        "G\u000eG\u0271\u000bG\u0003G\u0002\u0002H\u0002\u0004\u0006\b\n\f\u000e",
        "\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B",
        "DFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a",
        "\u008c\u0002\t\u0004\u0002ZZ^^\u0003\u0002\u0018\u001b\u0004\u0002j",
        "kpu\u0003\u0002Z_\u0003\u0002$%\n\u0002))++..55;;BBDDLL\u0004\u0002",
        "\u0017\u0017&&\u026f\u0002\u008e\u0003\u0002\u0002\u0002\u0004\u0096",
        "\u0003\u0002\u0002\u0002\u0006\u00a1\u0003\u0002\u0002\u0002\b\u00a5",
        "\u0003\u0002\u0002\u0002\n\u00a9\u0003\u0002\u0002\u0002\f\u00ad\u0003",
        "\u0002\u0002\u0002\u000e\u00b3\u0003\u0002\u0002\u0002\u0010\u00b9\u0003",
        "\u0002\u0002\u0002\u0012\u00bf\u0003\u0002\u0002\u0002\u0014\u00c5\u0003",
        "\u0002\u0002\u0002\u0016\u00cb\u0003\u0002\u0002\u0002\u0018\u00d1\u0003",
        "\u0002\u0002\u0002\u001a\u00d7\u0003\u0002\u0002\u0002\u001c\u00dd\u0003",
        "\u0002\u0002\u0002\u001e\u00e3\u0003\u0002\u0002\u0002 \u00ec\u0003",
        "\u0002\u0002\u0002\"\u00f5\u0003\u0002\u0002\u0002$\u00ff\u0003\u0002",
        "\u0002\u0002&\u0107\u0003\u0002\u0002\u0002(\u010f\u0003\u0002\u0002",
        "\u0002*\u0118\u0003\u0002\u0002\u0002,\u0120\u0003\u0002\u0002\u0002",
        ".\u012c\u0003\u0002\u0002\u00020\u0135\u0003\u0002\u0002\u00022\u0139",
        "\u0003\u0002\u0002\u00024\u013d\u0003\u0002\u0002\u00026\u0141\u0003",
        "\u0002\u0002\u00028\u0148\u0003\u0002\u0002\u0002:\u0153\u0003\u0002",
        "\u0002\u0002<\u0157\u0003\u0002\u0002\u0002>\u016f\u0003\u0002\u0002",
        "\u0002@\u0171\u0003\u0002\u0002\u0002B\u0175\u0003\u0002\u0002\u0002",
        "D\u0177\u0003\u0002\u0002\u0002F\u018e\u0003\u0002\u0002\u0002H\u0191",
        "\u0003\u0002\u0002\u0002J\u0199\u0003\u0002\u0002\u0002L\u01a1\u0003",
        "\u0002\u0002\u0002N\u01a3\u0003\u0002\u0002\u0002P\u01b4\u0003\u0002",
        "\u0002\u0002R\u01b6\u0003\u0002\u0002\u0002T\u01bc\u0003\u0002\u0002",
        "\u0002V\u01bf\u0003\u0002\u0002\u0002X\u01c5\u0003\u0002\u0002\u0002",
        "Z\u01c8\u0003\u0002\u0002\u0002\\\u01ca\u0003\u0002\u0002\u0002^\u01ce",
        "\u0003\u0002\u0002\u0002`\u01d2\u0003\u0002\u0002\u0002b\u01d7\u0003",
        "\u0002\u0002\u0002d\u01e7\u0003\u0002\u0002\u0002f\u01f4\u0003\u0002",
        "\u0002\u0002h\u01f6\u0003\u0002\u0002\u0002j\u01fa\u0003\u0002\u0002",
        "\u0002l\u0204\u0003\u0002\u0002\u0002n\u020d\u0003\u0002\u0002\u0002",
        "p\u0214\u0003\u0002\u0002\u0002r\u021b\u0003\u0002\u0002\u0002t\u0222",
        "\u0003\u0002\u0002\u0002v\u0229\u0003\u0002\u0002\u0002x\u023c\u0003",
        "\u0002\u0002\u0002z\u023e\u0003\u0002\u0002\u0002|\u0248\u0003\u0002",
        "\u0002\u0002~\u024f\u0003\u0002\u0002\u0002\u0080\u0259\u0003\u0002",
        "\u0002\u0002\u0082\u025b\u0003\u0002\u0002\u0002\u0084\u0262\u0003\u0002",
        "\u0002\u0002\u0086\u0264\u0003\u0002\u0002\u0002\u0088\u0266\u0003\u0002",
        "\u0002\u0002\u008a\u0268\u0003\u0002\u0002\u0002\u008c\u026a\u0003\u0002",
        "\u0002\u0002\u008e\u0090\u0005\u0004\u0003\u0002\u008f\u0091\u0005\u0006",
        "\u0004\u0002\u0090\u008f\u0003\u0002\u0002\u0002\u0090\u0091\u0003\u0002",
        "\u0002\u0002\u0091\u0092\u0003\u0002\u0002\u0002\u0092\u0093\u0005\b",
        "\u0005\u0002\u0093\u0094\u0007\u0002\u0002\u0003\u0094\u0003\u0003\u0002",
        "\u0002\u0002\u0095\u0097\u0005\f\u0007\u0002\u0096\u0095\u0003\u0002",
        "\u0002\u0002\u0096\u0097\u0003\u0002\u0002\u0002\u0097\u0098\u0003\u0002",
        "\u0002\u0002\u0098\u009a\u0005\u000e\b\u0002\u0099\u009b\u0005\u0010",
        "\t\u0002\u009a\u0099\u0003\u0002\u0002\u0002\u009a\u009b\u0003\u0002",
        "\u0002\u0002\u009b\u009c\u0003\u0002\u0002\u0002\u009c\u009d\u0005\n",
        "\u0006\u0002\u009d\u0005\u0003\u0002\u0002\u0002\u009e\u00a0\u0005>",
        " \u0002\u009f\u009e\u0003\u0002\u0002\u0002\u00a0\u00a3\u0003\u0002",
        "\u0002\u0002\u00a1\u009f\u0003\u0002\u0002\u0002\u00a1\u00a2\u0003\u0002",
        "\u0002\u0002\u00a2\u0007\u0003\u0002\u0002\u0002\u00a3\u00a1\u0003\u0002",
        "\u0002\u0002\u00a4\u00a6\u0005D#\u0002\u00a5\u00a4\u0003\u0002\u0002",
        "\u0002\u00a6\u00a7\u0003\u0002\u0002\u0002\u00a7\u00a5\u0003\u0002\u0002",
        "\u0002\u00a7\u00a8\u0003\u0002\u0002\u0002\u00a8\t\u0003\u0002\u0002",
        "\u0002\u00a9\u00aa\u0007G\u0002\u0002\u00aa\u00ab\u0005H%\u0002\u00ab",
        "\u00ac\u0007f\u0002\u0002\u00ac\u000b\u0003\u0002\u0002\u0002\u00ad",
        "\u00ae\u0007\u008c\u0002\u0002\u00ae\u00af\u0007\u0003\u0002\u0002\u00af",
        "\u00b0\u0007`\u0002\u0002\u00b0\u00b1\u0007^\u0002\u0002\u00b1\u00b2",
        "\u0007a\u0002\u0002\u00b2\r\u0003\u0002\u0002\u0002\u00b3\u00b4\u0007",
        "\u008c\u0002\u0002\u00b4\u00b5\u0007\u0004\u0002\u0002\u00b5\u00b6\u0007",
        "`\u0002\u0002\u00b6\u00b7\u0005,\u0017\u0002\u00b7\u00b8\u0007a\u0002",
        "\u0002\u00b8\u000f\u0003\u0002\u0002\u0002\u00b9\u00ba\u0007\u008c\u0002",
        "\u0002\u00ba\u00bb\u0007\u0005\u0002\u0002\u00bb\u00bc\u0007`\u0002",
        "\u0002\u00bc\u00bd\u0005.\u0018\u0002\u00bd\u00be\u0007a\u0002\u0002",
        "\u00be\u0011\u0003\u0002\u0002\u0002\u00bf\u00c0\u0007\u008c\u0002\u0002",
        "\u00c0\u00c1\u0007\u0003\u0002\u0002\u00c1\u00c2\u0007`\u0002\u0002",
        "\u00c2\u00c3\u0007^\u0002\u0002\u00c3\u00c4\u0007a\u0002\u0002\u00c4",
        "\u0013\u0003\u0002\u0002\u0002\u00c5\u00c6\u0007\u008c\u0002\u0002\u00c6",
        "\u00c9\u0007\u0006\u0002\u0002\u00c7\u00c8\u0007`\u0002\u0002\u00c8",
        "\u00ca\u0007a\u0002\u0002\u00c9\u00c7\u0003\u0002\u0002\u0002\u00c9",
        "\u00ca\u0003\u0002\u0002\u0002\u00ca\u0015\u0003\u0002\u0002\u0002\u00cb",
        "\u00cc\u0007\u008c\u0002\u0002\u00cc\u00cf\u0007\u0007\u0002\u0002\u00cd",
        "\u00ce\u0007`\u0002\u0002\u00ce\u00d0\u0007a\u0002\u0002\u00cf\u00cd",
        "\u0003\u0002\u0002\u0002\u00cf\u00d0\u0003\u0002\u0002\u0002\u00d0\u0017",
        "\u0003\u0002\u0002\u0002\u00d1\u00d2\u0007\u008c\u0002\u0002\u00d2\u00d5",
        "\u0007\b\u0002\u0002\u00d3\u00d4\u0007`\u0002\u0002\u00d4\u00d6\u0007",
        "a\u0002\u0002\u00d5\u00d3\u0003\u0002\u0002\u0002\u00d5\u00d6\u0003",
        "\u0002\u0002\u0002\u00d6\u0019\u0003\u0002\u0002\u0002\u00d7\u00d8\u0007",
        "\u008c\u0002\u0002\u00d8\u00db\u0007\t\u0002\u0002\u00d9\u00da\u0007",
        "`\u0002\u0002\u00da\u00dc\u0007a\u0002\u0002\u00db\u00d9\u0003\u0002",
        "\u0002\u0002\u00db\u00dc\u0003\u0002\u0002\u0002\u00dc\u001b\u0003\u0002",
        "\u0002\u0002\u00dd\u00de\u0007\u008c\u0002\u0002\u00de\u00e1\u0007\n",
        "\u0002\u0002\u00df\u00e0\u0007`\u0002\u0002\u00e0\u00e2\u0007a\u0002",
        "\u0002\u00e1\u00df\u0003\u0002\u0002\u0002\u00e1\u00e2\u0003\u0002\u0002",
        "\u0002\u00e2\u001d\u0003\u0002\u0002\u0002\u00e3\u00e4\u0007\u008c\u0002",
        "\u0002\u00e4\u00ea\u0007\u000b\u0002\u0002\u00e5\u00e7\u0007`\u0002",
        "\u0002\u00e6\u00e8\u0005B\"\u0002\u00e7\u00e6\u0003\u0002\u0002\u0002",
        "\u00e7\u00e8\u0003\u0002\u0002\u0002\u00e8\u00e9\u0003\u0002\u0002\u0002",
        "\u00e9\u00eb\u0007a\u0002\u0002\u00ea\u00e5\u0003\u0002\u0002\u0002",
        "\u00ea\u00eb\u0003\u0002\u0002\u0002\u00eb\u001f\u0003\u0002\u0002\u0002",
        "\u00ec\u00ed\u0007\u008c\u0002\u0002\u00ed\u00f3\u0007\f\u0002\u0002",
        "\u00ee\u00f0\u0007`\u0002\u0002\u00ef\u00f1\u0005B\"\u0002\u00f0\u00ef",
        "\u0003\u0002\u0002\u0002\u00f0\u00f1\u0003\u0002\u0002\u0002\u00f1\u00f2",
        "\u0003\u0002\u0002\u0002\u00f2\u00f4\u0007a\u0002\u0002\u00f3\u00ee",
        "\u0003\u0002\u0002\u0002\u00f3\u00f4\u0003\u0002\u0002\u0002\u00f4!",
        "\u0003\u0002\u0002\u0002\u00f5\u00f6\u0007\u008c\u0002\u0002\u00f6\u00fd",
        "\u0007\r\u0002\u0002\u00f7\u00fa\u0007`\u0002\u0002\u00f8\u00fb\u0005",
        "*\u0016\u0002\u00f9\u00fb\u0005B\"\u0002\u00fa\u00f8\u0003\u0002\u0002",
        "\u0002\u00fa\u00f9\u0003\u0002\u0002\u0002\u00fa\u00fb\u0003\u0002\u0002",
        "\u0002\u00fb\u00fc\u0003\u0002\u0002\u0002\u00fc\u00fe\u0007a\u0002",
        "\u0002\u00fd\u00f7\u0003\u0002\u0002\u0002\u00fd\u00fe\u0003\u0002\u0002",
        "\u0002\u00fe#\u0003\u0002\u0002\u0002\u00ff\u0100\u0007\u008c\u0002",
        "\u0002\u0100\u0101\u0007\u000e\u0002\u0002\u0101\u0103\u0007`\u0002",
        "\u0002\u0102\u0104\u0005&\u0014\u0002\u0103\u0102\u0003\u0002\u0002",
        "\u0002\u0103\u0104\u0003\u0002\u0002\u0002\u0104\u0105\u0003\u0002\u0002",
        "\u0002\u0105\u0106\u0007a\u0002\u0002\u0106%\u0003\u0002\u0002\u0002",
        "\u0107\u010c\u0005(\u0015\u0002\u0108\u0109\u0007g\u0002\u0002\u0109",
        "\u010b\u0005(\u0015\u0002\u010a\u0108\u0003\u0002\u0002\u0002\u010b",
        "\u010e\u0003\u0002\u0002\u0002\u010c\u010a\u0003\u0002\u0002\u0002\u010c",
        "\u010d\u0003\u0002\u0002\u0002\u010d\'\u0003\u0002\u0002\u0002\u010e",
        "\u010c\u0003\u0002\u0002\u0002\u010f\u0110\u0007\u008c\u0002\u0002\u0110",
        "\u0111\u0007\u000f\u0002\u0002\u0111\u0114\u0007`\u0002\u0002\u0112",
        "\u0115\u0005*\u0016\u0002\u0113\u0115\u0005B\"\u0002\u0114\u0112\u0003",
        "\u0002\u0002\u0002\u0114\u0113\u0003\u0002\u0002\u0002\u0114\u0115\u0003",
        "\u0002\u0002\u0002\u0115\u0116\u0003\u0002\u0002\u0002\u0116\u0117\u0007",
        "a\u0002\u0002\u0117)\u0003\u0002\u0002\u0002\u0118\u011d\u0005@!\u0002",
        "\u0119\u011a\u0007g\u0002\u0002\u011a\u011c\u0005@!\u0002\u011b\u0119",
        "\u0003\u0002\u0002\u0002\u011c\u011f\u0003\u0002\u0002\u0002\u011d\u011b",
        "\u0003\u0002\u0002\u0002\u011d\u011e\u0003\u0002\u0002\u0002\u011e+",
        "\u0003\u0002\u0002\u0002\u011f\u011d\u0003\u0002\u0002\u0002\u0120\u0123",
        "\u00050\u0019\u0002\u0121\u0122\u0007g\u0002\u0002\u0122\u0124\u0005",
        "2\u001a\u0002\u0123\u0121\u0003\u0002\u0002\u0002\u0123\u0124\u0003",
        "\u0002\u0002\u0002\u0124\u0127\u0003\u0002\u0002\u0002\u0125\u0126\u0007",
        "g\u0002\u0002\u0126\u0128\u00054\u001b\u0002\u0127\u0125\u0003\u0002",
        "\u0002\u0002\u0127\u0128\u0003\u0002\u0002\u0002\u0128-\u0003\u0002",
        "\u0002\u0002\u0129\u012a\u00056\u001c\u0002\u012a\u012b\u0007g\u0002",
        "\u0002\u012b\u012d\u0003\u0002\u0002\u0002\u012c\u0129\u0003\u0002\u0002",
        "\u0002\u012c\u012d\u0003\u0002\u0002\u0002\u012d\u0131\u0003\u0002\u0002",
        "\u0002\u012e\u012f\u0005:\u001e\u0002\u012f\u0130\u0007g\u0002\u0002",
        "\u0130\u0132\u0003\u0002\u0002\u0002\u0131\u012e\u0003\u0002\u0002\u0002",
        "\u0131\u0132\u0003\u0002\u0002\u0002\u0132\u0133\u0003\u0002\u0002\u0002",
        "\u0133\u0134\u0005<\u001f\u0002\u0134/\u0003\u0002\u0002\u0002\u0135",
        "\u0136\u0007\u0010\u0002\u0002\u0136\u0137\u0007i\u0002\u0002\u0137",
        "\u0138\u0007^\u0002\u0002\u01381\u0003\u0002\u0002\u0002\u0139\u013a",
        "\u0007\u0011\u0002\u0002\u013a\u013b\u0007i\u0002\u0002\u013b\u013c",
        "\u0007^\u0002\u0002\u013c3\u0003\u0002\u0002\u0002\u013d\u013e\u0007",
        "\u0012\u0002\u0002\u013e\u013f\u0007i\u0002\u0002\u013f\u0140\u0007",
        "Z\u0002\u0002\u01405\u0003\u0002\u0002\u0002\u0141\u0142\u0007\u0013",
        "\u0002\u0002\u0142\u0144\u0007i\u0002\u0002\u0143\u0145\u00058\u001d",
        "\u0002\u0144\u0143\u0003\u0002\u0002\u0002\u0145\u0146\u0003\u0002\u0002",
        "\u0002\u0146\u0144\u0003\u0002\u0002\u0002\u0146\u0147\u0003\u0002\u0002",
        "\u0002\u01477\u0003\u0002\u0002\u0002\u0148\u0149\u0007b\u0002\u0002",
        "\u0149\u014e\u0007^\u0002\u0002\u014a\u014b\u0007g\u0002\u0002\u014b",
        "\u014d\u0007^\u0002\u0002\u014c\u014a\u0003\u0002\u0002\u0002\u014d",
        "\u0150\u0003\u0002\u0002\u0002\u014e\u014c\u0003\u0002\u0002\u0002\u014e",
        "\u014f\u0003\u0002\u0002\u0002\u014f\u0151\u0003\u0002\u0002\u0002\u0150",
        "\u014e\u0003\u0002\u0002\u0002\u0151\u0152\u0007c\u0002\u0002\u0152",
        "9\u0003\u0002\u0002\u0002\u0153\u0154\u0007\u0014\u0002\u0002\u0154",
        "\u0155\u0007i\u0002\u0002\u0155\u0156\u0007^\u0002\u0002\u0156;\u0003",
        "\u0002\u0002\u0002\u0157\u0158\u0007\u0015\u0002\u0002\u0158\u0159\u0007",
        "i\u0002\u0002\u0159\u015a\u0005\u0086D\u0002\u015a=\u0003\u0002\u0002",
        "\u0002\u015b\u015c\u00072\u0002\u0002\u015c\u015d\u0005\u0088E\u0002",
        "\u015d\u015e\u0007\u008b\u0002\u0002\u015e\u015f\u0007i\u0002\u0002",
        "\u015f\u0160\u0005\u0084C\u0002\u0160\u0161\u0007f\u0002\u0002\u0161",
        "\u0170\u0003\u0002\u0002\u0002\u0162\u0163\u00072\u0002\u0002\u0163",
        "\u0164\u0005\u008aF\u0002\u0164\u0165\u0007\u008b\u0002\u0002\u0165",
        "\u0166\u0007i\u0002\u0002\u0166\u0167\u0007F\u0002\u0002\u0167\u0168",
        "\u0007\u008b\u0002\u0002\u0168\u016a\u0007`\u0002\u0002\u0169\u016b",
        "\u0007^\u0002\u0002\u016a\u0169\u0003\u0002\u0002\u0002\u016a\u016b",
        "\u0003\u0002\u0002\u0002\u016b\u016c\u0003\u0002\u0002\u0002\u016c\u016d",
        "\u0007a\u0002\u0002\u016d\u016e\u0007f\u0002\u0002\u016e\u0170\u0003",
        "\u0002\u0002\u0002\u016f\u015b\u0003\u0002\u0002\u0002\u016f\u0162\u0003",
        "\u0002\u0002\u0002\u0170?\u0003\u0002\u0002\u0002\u0171\u0172\u0007",
        "\u008b\u0002\u0002\u0172\u0173\u0007i\u0002\u0002\u0173\u0174\u0005",
        "B\"\u0002\u0174A\u0003\u0002\u0002\u0002\u0175\u0176\t\u0002\u0002\u0002",
        "\u0176C\u0003\u0002\u0002\u0002\u0177\u0179\u0005F$\u0002\u0178\u017a",
        "\u0005\u001e\u0010\u0002\u0179\u0178\u0003\u0002\u0002\u0002\u0179\u017a",
        "\u0003\u0002\u0002\u0002\u017a\u017c\u0003\u0002\u0002\u0002\u017b\u017d",
        "\u0005 \u0011\u0002\u017c\u017b\u0003\u0002\u0002\u0002\u017c\u017d",
        "\u0003\u0002\u0002\u0002\u017d\u017f\u0003\u0002\u0002\u0002\u017e\u0180",
        "\u0005\"\u0012\u0002\u017f\u017e\u0003\u0002\u0002\u0002\u017f\u0180",
        "\u0003\u0002\u0002\u0002\u0180\u0182\u0003\u0002\u0002\u0002\u0181\u0183",
        "\u0005$\u0013\u0002\u0182\u0181\u0003\u0002\u0002\u0002\u0182\u0183",
        "\u0003\u0002\u0002\u0002\u0183\u0184\u0003\u0002\u0002\u0002\u0184\u0185",
        "\u0005\u0012\n\u0002\u0185\u0186\u0005J&\u0002\u0186E\u0003\u0002\u0002",
        "\u0002\u0187\u018d\u0005\u0014\u000b\u0002\u0188\u018d\u0005\u0016\f",
        "\u0002\u0189\u018d\u0005\u0018\r\u0002\u018a\u018d\u0005\u001a\u000e",
        "\u0002\u018b\u018d\u0005\u001c\u000f\u0002\u018c\u0187\u0003\u0002\u0002",
        "\u0002\u018c\u0188\u0003\u0002\u0002\u0002\u018c\u0189\u0003\u0002\u0002",
        "\u0002\u018c\u018a\u0003\u0002\u0002\u0002\u018c\u018b\u0003\u0002\u0002",
        "\u0002\u018d\u0190\u0003\u0002\u0002\u0002\u018e\u018c\u0003\u0002\u0002",
        "\u0002\u018e\u018f\u0003\u0002\u0002\u0002\u018fG\u0003\u0002\u0002",
        "\u0002\u0190\u018e\u0003\u0002\u0002\u0002\u0191\u0196\u0007\u008b\u0002",
        "\u0002\u0192\u0193\u0007h\u0002\u0002\u0193\u0195\u0007\u008b\u0002",
        "\u0002\u0194\u0192\u0003\u0002\u0002\u0002\u0195\u0198\u0003\u0002\u0002",
        "\u0002\u0196\u0194\u0003\u0002\u0002\u0002\u0196\u0197\u0003\u0002\u0002",
        "\u0002\u0197I\u0003\u0002\u0002\u0002\u0198\u0196\u0003\u0002\u0002",
        "\u0002\u0199\u019a\u0007\u0016\u0002\u0002\u019a\u019b\u0005L\'\u0002",
        "\u019b\u019c\u0007`\u0002\u0002\u019c\u019d\u0007\u0017\u0002\u0002",
        "\u019d\u019e\u0007\u008b\u0002\u0002\u019e\u019f\u0007a\u0002\u0002",
        "\u019f\u01a0\u0005N(\u0002\u01a0K\u0003\u0002\u0002\u0002\u01a1\u01a2",
        "\u0007\u008b\u0002\u0002\u01a2M\u0003\u0002\u0002\u0002\u01a3\u01a7",
        "\u0007b\u0002\u0002\u01a4\u01a6\u0005P)\u0002\u01a5\u01a4\u0003\u0002",
        "\u0002\u0002\u01a6\u01a9\u0003\u0002\u0002\u0002\u01a7\u01a5\u0003\u0002",
        "\u0002\u0002\u01a7\u01a8\u0003\u0002\u0002\u0002\u01a8\u01aa\u0003\u0002",
        "\u0002\u0002\u01a9\u01a7\u0003\u0002\u0002\u0002\u01aa\u01ab\u0007c",
        "\u0002\u0002\u01abO\u0003\u0002\u0002\u0002\u01ac\u01b5\u0005b2\u0002",
        "\u01ad\u01b5\u0005d3\u0002\u01ae\u01b5\u0005f4\u0002\u01af\u01b5\u0005",
        "z>\u0002\u01b0\u01b5\u0005|?\u0002\u01b1\u01b5\u0005h5\u0002\u01b2\u01b5",
        "\u0005R*\u0002\u01b3\u01b5\u0005\\/\u0002\u01b4\u01ac\u0003\u0002\u0002",
        "\u0002\u01b4\u01ad\u0003\u0002\u0002\u0002\u01b4\u01ae\u0003\u0002\u0002",
        "\u0002\u01b4\u01af\u0003\u0002\u0002\u0002\u01b4\u01b0\u0003\u0002\u0002",
        "\u0002\u01b4\u01b1\u0003\u0002\u0002\u0002\u01b4\u01b2\u0003\u0002\u0002",
        "\u0002\u01b4\u01b3\u0003\u0002\u0002\u0002\u01b5Q\u0003\u0002\u0002",
        "\u0002\u01b6\u01b8\u0005T+\u0002\u01b7\u01b9\u0005V,\u0002\u01b8\u01b7",
        "\u0003\u0002\u0002\u0002\u01b9\u01ba\u0003\u0002\u0002\u0002\u01ba\u01b8",
        "\u0003\u0002\u0002\u0002\u01ba\u01bb\u0003\u0002\u0002\u0002\u01bbS",
        "\u0003\u0002\u0002\u0002\u01bc\u01bd\u0007V\u0002\u0002\u01bd\u01be",
        "\u0005N(\u0002\u01beU\u0003\u0002\u0002\u0002\u01bf\u01c0\u0007-\u0002",
        "\u0002\u01c0\u01c1\u0007`\u0002\u0002\u01c1\u01c2\u0005X-\u0002\u01c2",
        "\u01c3\u0007a\u0002\u0002\u01c3\u01c4\u0005N(\u0002\u01c4W\u0003\u0002",
        "\u0002\u0002\u01c5\u01c6\u0005Z.\u0002\u01c6\u01c7\u0007\u008b\u0002",
        "\u0002\u01c7Y\u0003\u0002\u0002\u0002\u01c8\u01c9\t\u0003\u0002\u0002",
        "\u01c9[\u0003\u0002\u0002\u0002\u01ca\u01cc\u0005^0\u0002\u01cb\u01cd",
        "\u0005`1\u0002\u01cc\u01cb\u0003\u0002\u0002\u0002\u01cc\u01cd\u0003",
        "\u0002\u0002\u0002\u01cd]\u0003\u0002\u0002\u0002\u01ce\u01cf\u0007",
        "=\u0002\u0002\u01cf\u01d0\u0005~@\u0002\u01d0\u01d1\u0005N(\u0002\u01d1",
        "_\u0003\u0002\u0002\u0002\u01d2\u01d3\u00076\u0002\u0002\u01d3\u01d4",
        "\u0005N(\u0002\u01d4a\u0003\u0002\u0002\u0002\u01d5\u01d8\u0005\u0088",
        "E\u0002\u01d6\u01d8\u0005\u008aF\u0002\u01d7\u01d5\u0003\u0002\u0002",
        "\u0002\u01d7\u01d6\u0003\u0002\u0002\u0002\u01d8\u01d9\u0003\u0002\u0002",
        "\u0002\u01d9\u01da\u0007\u008b\u0002\u0002\u01da\u01db\u0007f\u0002",
        "\u0002\u01dbc\u0003\u0002\u0002\u0002\u01dc\u01dd\u0005\u0088E\u0002",
        "\u01dd\u01de\u0007\u008b\u0002\u0002\u01de\u01df\u0007i\u0002\u0002",
        "\u01df\u01e0\u0005\u0084C\u0002\u01e0\u01e1\u0007f\u0002\u0002\u01e1",
        "\u01e8\u0003\u0002\u0002\u0002\u01e2\u01e8\u0005j6\u0002\u01e3\u01e4",
        "\u0005\u008aF\u0002\u01e4\u01e5\u0005l7\u0002\u01e5\u01e6\u0007f\u0002",
        "\u0002\u01e6\u01e8\u0003\u0002\u0002\u0002\u01e7\u01dc\u0003\u0002\u0002",
        "\u0002\u01e7\u01e2\u0003\u0002\u0002\u0002\u01e7\u01e3\u0003\u0002\u0002",
        "\u0002\u01e8e\u0003\u0002\u0002\u0002\u01e9\u01ea\u0007\u008b\u0002",
        "\u0002\u01ea\u01eb\u0007i\u0002\u0002\u01eb\u01ec\u0005\u0084C\u0002",
        "\u01ec\u01ed\u0007f\u0002\u0002\u01ed\u01f5\u0003\u0002\u0002\u0002",
        "\u01ee\u01ef\u0005j6\u0002\u01ef\u01f0\u0007f\u0002\u0002\u01f0\u01f5",
        "\u0003\u0002\u0002\u0002\u01f1\u01f2\u0005l7\u0002\u01f2\u01f3\u0007",
        "f\u0002\u0002\u01f3\u01f5\u0003\u0002\u0002\u0002\u01f4\u01e9\u0003",
        "\u0002\u0002\u0002\u01f4\u01ee\u0003\u0002\u0002\u0002\u01f4\u01f1\u0003",
        "\u0002\u0002\u0002\u01f5g\u0003\u0002\u0002\u0002\u01f6\u01f7\u0005",
        "x=\u0002\u01f7\u01f8\u0007f\u0002\u0002\u01f8i\u0003\u0002\u0002\u0002",
        "\u01f9\u01fb\u0005\u008aF\u0002\u01fa\u01f9\u0003\u0002\u0002\u0002",
        "\u01fa\u01fb\u0003\u0002\u0002\u0002\u01fb\u01fc\u0003\u0002\u0002\u0002",
        "\u01fc\u01fd\u0007\u008b\u0002\u0002\u01fd\u01fe\u0007i\u0002\u0002",
        "\u01fe\u01ff\u0007F\u0002\u0002\u01ff\u0200\u0005\u008aF\u0002\u0200",
        "\u0201\u0007`\u0002\u0002\u0201\u0202\u0007a\u0002\u0002\u0202\u0203",
        "\u0007f\u0002\u0002\u0203k\u0003\u0002\u0002\u0002\u0204\u0205\u0007",
        "\u008b\u0002\u0002\u0205\u020b\u0007i\u0002\u0002\u0206\u020c\u0005",
        "n8\u0002\u0207\u020c\u0005p9\u0002\u0208\u020c\u0005r:\u0002\u0209\u020c",
        "\u0005t;\u0002\u020a\u020c\u0005v<\u0002\u020b\u0206\u0003\u0002\u0002",
        "\u0002\u020b\u0207\u0003\u0002\u0002\u0002\u020b\u0208\u0003\u0002\u0002",
        "\u0002\u020b\u0209\u0003\u0002\u0002\u0002\u020b\u020a\u0003\u0002\u0002",
        "\u0002\u020cm\u0003\u0002\u0002\u0002\u020d\u020e\u0007\u001c\u0002",
        "\u0002\u020e\u020f\u0007`\u0002\u0002\u020f\u0210\u0007\u008b\u0002",
        "\u0002\u0210\u0211\u0007g\u0002\u0002\u0211\u0212\u0007\u008b\u0002",
        "\u0002\u0212\u0213\u0007a\u0002\u0002\u0213o\u0003\u0002\u0002\u0002",
        "\u0214\u0215\u0007\u001d\u0002\u0002\u0215\u0216\u0007`\u0002\u0002",
        "\u0216\u0217\u0007\u008b\u0002\u0002\u0217\u0218\u0007g\u0002\u0002",
        "\u0218\u0219\u0007\u008b\u0002\u0002\u0219\u021a\u0007a\u0002\u0002",
        "\u021aq\u0003\u0002\u0002\u0002\u021b\u021c\u0007\u001e\u0002\u0002",
        "\u021c\u021d\u0007`\u0002\u0002\u021d\u021e\u0005\u0084C\u0002\u021e",
        "\u021f\u0007g\u0002\u0002\u021f\u0220\u0007\u008b\u0002\u0002\u0220",
        "\u0221\u0007a\u0002\u0002\u0221s\u0003\u0002\u0002\u0002\u0222\u0223",
        "\u0007\u001f\u0002\u0002\u0223\u0224\u0007`\u0002\u0002\u0224\u0225",
        "\u0007\u008b\u0002\u0002\u0225\u0226\u0007g\u0002\u0002\u0226\u0227",
        "\u0007\u008b\u0002\u0002\u0227\u0228\u0007a\u0002\u0002\u0228u\u0003",
        "\u0002\u0002\u0002\u0229\u022a\u0007\u008b\u0002\u0002\u022a\u022b\u0007",
        " \u0002\u0002\u022b\u022c\u0007`\u0002\u0002\u022c\u022f\u0007\u008b",
        "\u0002\u0002\u022d\u022e\u0007g\u0002\u0002\u022e\u0230\u0007^\u0002",
        "\u0002\u022f\u022d\u0003\u0002\u0002\u0002\u022f\u0230\u0003\u0002\u0002",
        "\u0002\u0230\u0231\u0003\u0002\u0002\u0002\u0231\u0232\u0007a\u0002",
        "\u0002\u0232w\u0003\u0002\u0002\u0002\u0233\u0234\u0007!\u0002\u0002",
        "\u0234\u0235\u0007`\u0002\u0002\u0235\u0236\u0007\u008b\u0002\u0002",
        "\u0236\u023d\u0007a\u0002\u0002\u0237\u0238\u0007!\u0002\u0002\u0238",
        "\u0239\u0007`\u0002\u0002\u0239\u023a\u0005\u0084C\u0002\u023a\u023b",
        "\u0007a\u0002\u0002\u023b\u023d\u0003\u0002\u0002\u0002\u023c\u0233",
        "\u0003\u0002\u0002\u0002\u023c\u0237\u0003\u0002\u0002\u0002\u023dy",
        "\u0003\u0002\u0002\u0002\u023e\u023f\u0007\u008b\u0002\u0002\u023f\u0240",
        "\u0007h\u0002\u0002\u0240\u0241\u0007\u008b\u0002\u0002\u0241\u0242",
        "\u0007`\u0002\u0002\u0242\u0243\u0005\u008cG\u0002\u0243\u0244\u0007",
        "g\u0002\u0002\u0244\u0245\u0005\u0084C\u0002\u0245\u0246\u0007a\u0002",
        "\u0002\u0246\u0247\u0007f\u0002\u0002\u0247{\u0003\u0002\u0002\u0002",
        "\u0248\u024b\u0007\"\u0002\u0002\u0249\u024c\u0007\u008b\u0002\u0002",
        "\u024a\u024c\u0005n8\u0002\u024b\u0249\u0003\u0002\u0002\u0002\u024b",
        "\u024a\u0003\u0002\u0002\u0002\u024b\u024c\u0003\u0002\u0002\u0002\u024c",
        "\u024d\u0003\u0002\u0002\u0002\u024d\u024e\u0007f\u0002\u0002\u024e",
        "}\u0003\u0002\u0002\u0002\u024f\u0250\u0007`\u0002\u0002\u0250\u0253",
        "\u0005\u0080A\u0002\u0251\u0252\t\u0004\u0002\u0002\u0252\u0254\u0005",
        "\u0080A\u0002\u0253\u0251\u0003\u0002\u0002\u0002\u0253\u0254\u0003",
        "\u0002\u0002\u0002\u0254\u0255\u0003\u0002\u0002\u0002\u0255\u0256\u0007",
        "a\u0002\u0002\u0256\u007f\u0003\u0002\u0002\u0002\u0257\u025a\u0005",
        "\u0082B\u0002\u0258\u025a\u0005\u0084C\u0002\u0259\u0257\u0003\u0002",
        "\u0002\u0002\u0259\u0258\u0003\u0002\u0002\u0002\u025a\u0081\u0003\u0002",
        "\u0002\u0002\u025b\u025c\u0007#\u0002\u0002\u025c\u025d\u0007`\u0002",
        "\u0002\u025d\u025e\u0007^\u0002\u0002\u025e\u025f\u0007g\u0002\u0002",
        "\u025f\u0260\u0007\u008b\u0002\u0002\u0260\u0261\u0007a\u0002\u0002",
        "\u0261\u0083\u0003\u0002\u0002\u0002\u0262\u0263\t\u0005\u0002\u0002",
        "\u0263\u0085\u0003\u0002\u0002\u0002\u0264\u0265\t\u0006\u0002\u0002",
        "\u0265\u0087\u0003\u0002\u0002\u0002\u0266\u0267\t\u0007\u0002\u0002",
        "\u0267\u0089\u0003\u0002\u0002\u0002\u0268\u0269\t\b\u0002\u0002\u0269",
        "\u008b\u0003\u0002\u0002\u0002\u026a\u026f\u0007\u008b\u0002\u0002\u026b",
        "\u026c\u0007h\u0002\u0002\u026c\u026e\u0007\u008b\u0002\u0002\u026d",
        "\u026b\u0003\u0002\u0002\u0002\u026e\u0271\u0003\u0002\u0002\u0002\u026f",
        "\u026d\u0003\u0002\u0002\u0002\u026f\u0270\u0003\u0002\u0002\u0002\u0270",
        "\u008d\u0003\u0002\u0002\u0002\u0271\u026f\u0003\u0002\u0002\u00024",
        "\u0090\u0096\u009a\u00a1\u00a7\u00c9\u00cf\u00d5\u00db\u00e1\u00e7\u00ea",
        "\u00f0\u00f3\u00fa\u00fd\u0103\u010c\u0114\u011d\u0123\u0127\u012c\u0131",
        "\u0146\u014e\u016a\u016f\u0179\u017c\u017f\u0182\u018c\u018e\u0196\u01a7",
        "\u01b4\u01ba\u01cc\u01d7\u01e7\u01f4\u01fa\u020b\u022f\u023c\u024b\u0253",
        "\u0259\u026f"].join("");


    var atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

    var decisionsToDFA = atn.decisionToState.map(function (ds, index) {
        return new antlr4.dfa.DFA(ds, index);
    });

    var sharedContextCache = new antlr4.PredictionContextCache();

    var literalNames = [null, "'Path'", "'Source'", "'Api'", "'GET'", "'POST'",
        "'PUT'", "'DELETE'", "'HEAD'", "'Produces'", "'Consumes'",
        "'ApiOperation'", "'ApiResponses'", "'ApiResponse'",
        "'protocol'", "'host'", "'port'", "'tags'", "'description'",
        "'produces'", "'resource'", "'message'", "'ConnectionClosedException'",
        "'ConnectionFailedException'", "'ConnectionTimeoutException'",
        "'Exception'", "'invoke'", "'sendTo'", "'datamap'",
        "'receiveFrom'", "'.mediator'", "'log'", "'reply'",
        "'eval'", "'MediaType.APPLICATION_JSON'", "'MediaType.APPLICATION_XML'",
        "'endpoint'", "'abstract'", "'assert'", "'boolean'",
        "'break'", "'byte'", "'case'", "'catch'", "'char'",
        "'class'", "'const'", "'continue'", "'constant'", "'default'",
        "'do'", "'double'", "'else'", "'enum'", "'extends'",
        "'final'", "'finally'", "'float'", "'for'", "'if'",
        "'goto'", "'implements'", "'import'", "'instanceof'",
        "'int'", "'interface'", "'long'", "'native'", "'new'",
        "'package'", "'private'", "'protected'", "'public'",
        "'return'", "'short'", "'static'", "'strictfp'", "'super'",
        "'switch'", "'synchronized'", "'this'", "'throw'",
        "'throws'", "'transient'", "'try'", "'void'", "'volatile'",
        "'while'", null, null, null, null, null, "'null'",
        "'('", "')'", "'{'", "'}'", "'['", "']'", "';'", "','",
        "'.'", "'='", "'>'", "'<'", "'!'", "'~'", "'?'", "':'",
        "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", "'++'",
        "'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", "'^'",
        "'%'", "'+='", "'-='", "'*='", "'/='", "'&='", "'|='",
        "'^='", "'%='", "'<<='", "'>>='", "'>>>='", null, "'@'",
        "'...'"];

    var symbolicNames = [null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null,
        null, "ABSTRACT", "ASSERT", "BOOLEAN", "BREAK", "BYTE",
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
        "Identifier", "AT", "ELLIPSIS", "WS", "COMMENT", "LINE_COMMENT"];

    var ruleNames = ["sourceFile", "definition", "constants", "resources",
        "packageDef", "path", "source", "api", "resourcePath",
        "getMethod", "postMethod", "putMethod", "deleteMethod",
        "headMethod", "prodAnt", "conAnt", "antApiOperation",
        "antApiResponses", "antApiResponseSet", "antApiResponse",
        "elementValuePairs", "sourceElementValuePairs", "apiElementValuePairs",
        "protocol", "host", "port", "tags", "tag", "descripton",
        "producer", "constant", "elementValuePair", "elementValue",
        "resource", "httpMethods", "qualifiedName", "resourceDeclaration",
        "resourceName", "block", "blockStatement", "tryCatchBlock",
        "tryClause", "catchClause", "exceptionHandler", "exceptionType",
        "ifElseBlock", "ifBlock", "elseBlock", "localVariableDeclarationStatement",
        "localVaribaleInitializationStatement", "localVaribaleAssignmentStatement",
        "logMediatorStatement", "newTypeObjectCreation", "mediatorCall",
        "invokeMediatorCall", "sendToMediatorCall", "dataMapMediatorCall",
        "receiveFromMediatorCall", "customMediatorCall", "logMediatorCall",
        "messageModificationStatement", "returnStatement", "parExpression",
        "expression", "evalExpression", "literal", "mediaType",
        "type", "classType", "messagePropertyName"];

    function WUMLParser(input) {
        antlr4.Parser.call(this, input);
        this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
        this.ruleNames = ruleNames;
        this.literalNames = literalNames;
        this.symbolicNames = symbolicNames;
        return this;
    }

    WUMLParser.prototype = Object.create(antlr4.Parser.prototype);
    WUMLParser.prototype.constructor = WUMLParser;

    Object.defineProperty(WUMLParser.prototype, "atn", {
        get: function () {
            return atn;
        }
    });

    WUMLParser.EOF = antlr4.Token.EOF;
    WUMLParser.T__0 = 1;
    WUMLParser.T__1 = 2;
    WUMLParser.T__2 = 3;
    WUMLParser.T__3 = 4;
    WUMLParser.T__4 = 5;
    WUMLParser.T__5 = 6;
    WUMLParser.T__6 = 7;
    WUMLParser.T__7 = 8;
    WUMLParser.T__8 = 9;
    WUMLParser.T__9 = 10;
    WUMLParser.T__10 = 11;
    WUMLParser.T__11 = 12;
    WUMLParser.T__12 = 13;
    WUMLParser.T__13 = 14;
    WUMLParser.T__14 = 15;
    WUMLParser.T__15 = 16;
    WUMLParser.T__16 = 17;
    WUMLParser.T__17 = 18;
    WUMLParser.T__18 = 19;
    WUMLParser.T__19 = 20;
    WUMLParser.T__20 = 21;
    WUMLParser.T__21 = 22;
    WUMLParser.T__22 = 23;
    WUMLParser.T__23 = 24;
    WUMLParser.T__24 = 25;
    WUMLParser.T__25 = 26;
    WUMLParser.T__26 = 27;
    WUMLParser.T__27 = 28;
    WUMLParser.T__28 = 29;
    WUMLParser.T__29 = 30;
    WUMLParser.T__30 = 31;
    WUMLParser.T__31 = 32;
    WUMLParser.T__32 = 33;
    WUMLParser.T__33 = 34;
    WUMLParser.T__34 = 35;
    WUMLParser.T__35 = 36;
    WUMLParser.ABSTRACT = 37;
    WUMLParser.ASSERT = 38;
    WUMLParser.BOOLEAN = 39;
    WUMLParser.BREAK = 40;
    WUMLParser.BYTE = 41;
    WUMLParser.CASE = 42;
    WUMLParser.CATCH = 43;
    WUMLParser.CHAR = 44;
    WUMLParser.CLASS = 45;
    WUMLParser.CONST = 46;
    WUMLParser.CONTINUE = 47;
    WUMLParser.CONSTANT = 48;
    WUMLParser.DEFAULT = 49;
    WUMLParser.DO = 50;
    WUMLParser.DOUBLE = 51;
    WUMLParser.ELSE = 52;
    WUMLParser.ENUM = 53;
    WUMLParser.EXTENDS = 54;
    WUMLParser.FINAL = 55;
    WUMLParser.FINALLY = 56;
    WUMLParser.FLOAT = 57;
    WUMLParser.FOR = 58;
    WUMLParser.IF = 59;
    WUMLParser.GOTO = 60;
    WUMLParser.IMPLEMENTS = 61;
    WUMLParser.IMPORT = 62;
    WUMLParser.INSTANCEOF = 63;
    WUMLParser.INT = 64;
    WUMLParser.INTERFACE = 65;
    WUMLParser.LONG = 66;
    WUMLParser.NATIVE = 67;
    WUMLParser.NEW = 68;
    WUMLParser.PACKAGE = 69;
    WUMLParser.PRIVATE = 70;
    WUMLParser.PROTECTED = 71;
    WUMLParser.PUBLIC = 72;
    WUMLParser.RETURN = 73;
    WUMLParser.SHORT = 74;
    WUMLParser.STATIC = 75;
    WUMLParser.STRICTFP = 76;
    WUMLParser.SUPER = 77;
    WUMLParser.SWITCH = 78;
    WUMLParser.SYNCHRONIZED = 79;
    WUMLParser.THIS = 80;
    WUMLParser.THROW = 81;
    WUMLParser.THROWS = 82;
    WUMLParser.TRANSIENT = 83;
    WUMLParser.TRY = 84;
    WUMLParser.VOID = 85;
    WUMLParser.VOLATILE = 86;
    WUMLParser.WHILE = 87;
    WUMLParser.IntegerLiteral = 88;
    WUMLParser.FloatingPointLiteral = 89;
    WUMLParser.BooleanLiteral = 90;
    WUMLParser.CharacterLiteral = 91;
    WUMLParser.StringLiteral = 92;
    WUMLParser.NullLiteral = 93;
    WUMLParser.LPAREN = 94;
    WUMLParser.RPAREN = 95;
    WUMLParser.LBRACE = 96;
    WUMLParser.RBRACE = 97;
    WUMLParser.LBRACK = 98;
    WUMLParser.RBRACK = 99;
    WUMLParser.SEMI = 100;
    WUMLParser.COMMA = 101;
    WUMLParser.DOT = 102;
    WUMLParser.ASSIGN = 103;
    WUMLParser.GT = 104;
    WUMLParser.LT = 105;
    WUMLParser.BANG = 106;
    WUMLParser.TILDE = 107;
    WUMLParser.QUESTION = 108;
    WUMLParser.COLON = 109;
    WUMLParser.EQUAL = 110;
    WUMLParser.LE = 111;
    WUMLParser.GE = 112;
    WUMLParser.NOTEQUAL = 113;
    WUMLParser.AND = 114;
    WUMLParser.OR = 115;
    WUMLParser.INC = 116;
    WUMLParser.DEC = 117;
    WUMLParser.ADD = 118;
    WUMLParser.SUB = 119;
    WUMLParser.MUL = 120;
    WUMLParser.DIV = 121;
    WUMLParser.BITAND = 122;
    WUMLParser.BITOR = 123;
    WUMLParser.CARET = 124;
    WUMLParser.MOD = 125;
    WUMLParser.ADD_ASSIGN = 126;
    WUMLParser.SUB_ASSIGN = 127;
    WUMLParser.MUL_ASSIGN = 128;
    WUMLParser.DIV_ASSIGN = 129;
    WUMLParser.AND_ASSIGN = 130;
    WUMLParser.OR_ASSIGN = 131;
    WUMLParser.XOR_ASSIGN = 132;
    WUMLParser.MOD_ASSIGN = 133;
    WUMLParser.LSHIFT_ASSIGN = 134;
    WUMLParser.RSHIFT_ASSIGN = 135;
    WUMLParser.URSHIFT_ASSIGN = 136;
    WUMLParser.Identifier = 137;
    WUMLParser.AT = 138;
    WUMLParser.ELLIPSIS = 139;
    WUMLParser.WS = 140;
    WUMLParser.COMMENT = 141;
    WUMLParser.LINE_COMMENT = 142;

    WUMLParser.RULE_sourceFile = 0;
    WUMLParser.RULE_definition = 1;
    WUMLParser.RULE_constants = 2;
    WUMLParser.RULE_resources = 3;
    WUMLParser.RULE_packageDef = 4;
    WUMLParser.RULE_path = 5;
    WUMLParser.RULE_source = 6;
    WUMLParser.RULE_api = 7;
    WUMLParser.RULE_resourcePath = 8;
    WUMLParser.RULE_getMethod = 9;
    WUMLParser.RULE_postMethod = 10;
    WUMLParser.RULE_putMethod = 11;
    WUMLParser.RULE_deleteMethod = 12;
    WUMLParser.RULE_headMethod = 13;
    WUMLParser.RULE_prodAnt = 14;
    WUMLParser.RULE_conAnt = 15;
    WUMLParser.RULE_antApiOperation = 16;
    WUMLParser.RULE_antApiResponses = 17;
    WUMLParser.RULE_antApiResponseSet = 18;
    WUMLParser.RULE_antApiResponse = 19;
    WUMLParser.RULE_elementValuePairs = 20;
    WUMLParser.RULE_sourceElementValuePairs = 21;
    WUMLParser.RULE_apiElementValuePairs = 22;
    WUMLParser.RULE_protocol = 23;
    WUMLParser.RULE_host = 24;
    WUMLParser.RULE_port = 25;
    WUMLParser.RULE_tags = 26;
    WUMLParser.RULE_tag = 27;
    WUMLParser.RULE_descripton = 28;
    WUMLParser.RULE_producer = 29;
    WUMLParser.RULE_constant = 30;
    WUMLParser.RULE_elementValuePair = 31;
    WUMLParser.RULE_elementValue = 32;
    WUMLParser.RULE_resource = 33;
    WUMLParser.RULE_httpMethods = 34;
    WUMLParser.RULE_qualifiedName = 35;
    WUMLParser.RULE_resourceDeclaration = 36;
    WUMLParser.RULE_resourceName = 37;
    WUMLParser.RULE_block = 38;
    WUMLParser.RULE_blockStatement = 39;
    WUMLParser.RULE_tryCatchBlock = 40;
    WUMLParser.RULE_tryClause = 41;
    WUMLParser.RULE_catchClause = 42;
    WUMLParser.RULE_exceptionHandler = 43;
    WUMLParser.RULE_exceptionType = 44;
    WUMLParser.RULE_ifElseBlock = 45;
    WUMLParser.RULE_ifBlock = 46;
    WUMLParser.RULE_elseBlock = 47;
    WUMLParser.RULE_localVariableDeclarationStatement = 48;
    WUMLParser.RULE_localVaribaleInitializationStatement = 49;
    WUMLParser.RULE_localVaribaleAssignmentStatement = 50;
    WUMLParser.RULE_logMediatorStatement = 51;
    WUMLParser.RULE_newTypeObjectCreation = 52;
    WUMLParser.RULE_mediatorCall = 53;
    WUMLParser.RULE_invokeMediatorCall = 54;
    WUMLParser.RULE_sendToMediatorCall = 55;
    WUMLParser.RULE_dataMapMediatorCall = 56;
    WUMLParser.RULE_receiveFromMediatorCall = 57;
    WUMLParser.RULE_customMediatorCall = 58;
    WUMLParser.RULE_logMediatorCall = 59;
    WUMLParser.RULE_messageModificationStatement = 60;
    WUMLParser.RULE_returnStatement = 61;
    WUMLParser.RULE_parExpression = 62;
    WUMLParser.RULE_expression = 63;
    WUMLParser.RULE_evalExpression = 64;
    WUMLParser.RULE_literal = 65;
    WUMLParser.RULE_mediaType = 66;
    WUMLParser.RULE_type = 67;
    WUMLParser.RULE_classType = 68;
    WUMLParser.RULE_messagePropertyName = 69;

    function SourceFileContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_sourceFile;
        return this;
    }

    SourceFileContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    SourceFileContext.prototype.constructor = SourceFileContext;

    SourceFileContext.prototype.definition = function () {
        return this.getTypedRuleContext(DefinitionContext, 0);
    };

    SourceFileContext.prototype.resources = function () {
        return this.getTypedRuleContext(ResourcesContext, 0);
    };

    SourceFileContext.prototype.EOF = function () {
        return this.getToken(WUMLParser.EOF, 0);
    };

    SourceFileContext.prototype.constants = function () {
        return this.getTypedRuleContext(ConstantsContext, 0);
    };

    SourceFileContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterSourceFile(this);
        }
    };

    SourceFileContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitSourceFile(this);
        }
    };


    WUMLParser.SourceFileContext = SourceFileContext;

    WUMLParser.prototype.sourceFile = function () {

        var localctx = new SourceFileContext(this, this._ctx, this.state);
        this.enterRule(localctx, 0, WUMLParser.RULE_sourceFile);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 140;
            this.definition();
            this.state = 142;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 0, this._ctx);
            if (la_ === 1) {
                this.state = 141;
                this.constants();

            }
            this.state = 144;
            this.resources();
            this.state = 145;
            this.match(WUMLParser.EOF);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_definition;
        return this;
    }

    DefinitionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    DefinitionContext.prototype.constructor = DefinitionContext;

    DefinitionContext.prototype.source = function () {
        return this.getTypedRuleContext(SourceContext, 0);
    };

    DefinitionContext.prototype.packageDef = function () {
        return this.getTypedRuleContext(PackageDefContext, 0);
    };

    DefinitionContext.prototype.path = function () {
        return this.getTypedRuleContext(PathContext, 0);
    };

    DefinitionContext.prototype.api = function () {
        return this.getTypedRuleContext(ApiContext, 0);
    };

    DefinitionContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterDefinition(this);
        }
    };

    DefinitionContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitDefinition(this);
        }
    };


    WUMLParser.DefinitionContext = DefinitionContext;

    WUMLParser.prototype.definition = function () {

        var localctx = new DefinitionContext(this, this._ctx, this.state);
        this.enterRule(localctx, 2, WUMLParser.RULE_definition);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 148;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 1, this._ctx);
            if (la_ === 1) {
                this.state = 147;
                this.path();

            }
            this.state = 150;
            this.source();
            this.state = 152;
            _la = this._input.LA(1);
            if (_la === WUMLParser.AT) {
                this.state = 151;
                this.api();
            }

            this.state = 154;
            this.packageDef();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_constants;
        return this;
    }

    ConstantsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ConstantsContext.prototype.constructor = ConstantsContext;

    ConstantsContext.prototype.constant = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(ConstantContext);
        } else {
            return this.getTypedRuleContext(ConstantContext, i);
        }
    };

    ConstantsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterConstants(this);
        }
    };

    ConstantsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitConstants(this);
        }
    };


    WUMLParser.ConstantsContext = ConstantsContext;

    WUMLParser.prototype.constants = function () {

        var localctx = new ConstantsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 4, WUMLParser.RULE_constants);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 159;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.CONSTANT) {
                this.state = 156;
                this.constant();
                this.state = 161;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_resources;
        return this;
    }

    ResourcesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ResourcesContext.prototype.constructor = ResourcesContext;

    ResourcesContext.prototype.resource = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(ResourceContext);
        } else {
            return this.getTypedRuleContext(ResourceContext, i);
        }
    };

    ResourcesContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterResources(this);
        }
    };

    ResourcesContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitResources(this);
        }
    };


    WUMLParser.ResourcesContext = ResourcesContext;

    WUMLParser.prototype.resources = function () {

        var localctx = new ResourcesContext(this, this._ctx, this.state);
        this.enterRule(localctx, 6, WUMLParser.RULE_resources);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 163;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            do {
                this.state = 162;
                this.resource();
                this.state = 165;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            } while (_la === WUMLParser.AT);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_packageDef;
        return this;
    }

    PackageDefContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    PackageDefContext.prototype.constructor = PackageDefContext;

    PackageDefContext.prototype.PACKAGE = function () {
        return this.getToken(WUMLParser.PACKAGE, 0);
    };

    PackageDefContext.prototype.qualifiedName = function () {
        return this.getTypedRuleContext(QualifiedNameContext, 0);
    };

    PackageDefContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterPackageDef(this);
        }
    };

    PackageDefContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitPackageDef(this);
        }
    };


    WUMLParser.PackageDefContext = PackageDefContext;

    WUMLParser.prototype.packageDef = function () {

        var localctx = new PackageDefContext(this, this._ctx, this.state);
        this.enterRule(localctx, 8, WUMLParser.RULE_packageDef);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 167;
            this.match(WUMLParser.PACKAGE);
            this.state = 168;
            this.qualifiedName();
            this.state = 169;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_path;
        return this;
    }

    PathContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    PathContext.prototype.constructor = PathContext;

    PathContext.prototype.AT = function () {
        return this.getToken(WUMLParser.AT, 0);
    };

    PathContext.prototype.LPAREN = function () {
        return this.getToken(WUMLParser.LPAREN, 0);
    };

    PathContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    PathContext.prototype.RPAREN = function () {
        return this.getToken(WUMLParser.RPAREN, 0);
    };

    PathContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterPath(this);
        }
    };

    PathContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitPath(this);
        }
    };


    WUMLParser.PathContext = PathContext;

    WUMLParser.prototype.path = function () {

        var localctx = new PathContext(this, this._ctx, this.state);
        this.enterRule(localctx, 10, WUMLParser.RULE_path);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 171;
            this.match(WUMLParser.AT);
            this.state = 172;
            this.match(WUMLParser.T__0);
            this.state = 173;
            this.match(WUMLParser.LPAREN);
            this.state = 174;
            this.match(WUMLParser.StringLiteral);
            this.state = 175;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_source;
        return this;
    }

    SourceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    SourceContext.prototype.constructor = SourceContext;

    SourceContext.prototype.AT = function () {
        return this.getToken(WUMLParser.AT, 0);
    };

    SourceContext.prototype.LPAREN = function () {
        return this.getToken(WUMLParser.LPAREN, 0);
    };

    SourceContext.prototype.sourceElementValuePairs = function () {
        return this.getTypedRuleContext(SourceElementValuePairsContext, 0);
    };

    SourceContext.prototype.RPAREN = function () {
        return this.getToken(WUMLParser.RPAREN, 0);
    };

    SourceContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterSource(this);
        }
    };

    SourceContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitSource(this);
        }
    };


    WUMLParser.SourceContext = SourceContext;

    WUMLParser.prototype.source = function () {

        var localctx = new SourceContext(this, this._ctx, this.state);
        this.enterRule(localctx, 12, WUMLParser.RULE_source);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 177;
            this.match(WUMLParser.AT);
            this.state = 178;
            this.match(WUMLParser.T__1);
            this.state = 179;
            this.match(WUMLParser.LPAREN);
            this.state = 180;
            this.sourceElementValuePairs();
            this.state = 181;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_api;
        return this;
    }

    ApiContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ApiContext.prototype.constructor = ApiContext;

    ApiContext.prototype.apiElementValuePairs = function () {
        return this.getTypedRuleContext(ApiElementValuePairsContext, 0);
    };

    ApiContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterApi(this);
        }
    };

    ApiContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitApi(this);
        }
    };


    WUMLParser.ApiContext = ApiContext;

    WUMLParser.prototype.api = function () {

        var localctx = new ApiContext(this, this._ctx, this.state);
        this.enterRule(localctx, 14, WUMLParser.RULE_api);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 183;
            this.match(WUMLParser.AT);
            this.state = 184;
            this.match(WUMLParser.T__2);

            this.state = 185;
            this.match(WUMLParser.LPAREN);

            this.state = 186;
            this.apiElementValuePairs();
            this.state = 187;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_resourcePath;
        return this;
    }

    ResourcePathContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ResourcePathContext.prototype.constructor = ResourcePathContext;

    ResourcePathContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    ResourcePathContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterResourcePath(this);
        }
    };

    ResourcePathContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitResourcePath(this);
        }
    };


    WUMLParser.ResourcePathContext = ResourcePathContext;

    WUMLParser.prototype.resourcePath = function () {

        var localctx = new ResourcePathContext(this, this._ctx, this.state);
        this.enterRule(localctx, 16, WUMLParser.RULE_resourcePath);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 189;
            this.match(WUMLParser.AT);
            this.state = 190;
            this.match(WUMLParser.T__0);

            this.state = 191;
            this.match(WUMLParser.LPAREN);
            this.state = 192;
            this.match(WUMLParser.StringLiteral);
            this.state = 193;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_getMethod;
        return this;
    }

    GetMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    GetMethodContext.prototype.constructor = GetMethodContext;


    GetMethodContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterGetMethod(this);
        }
    };

    GetMethodContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitGetMethod(this);
        }
    };


    WUMLParser.GetMethodContext = GetMethodContext;

    WUMLParser.prototype.getMethod = function () {

        var localctx = new GetMethodContext(this, this._ctx, this.state);
        this.enterRule(localctx, 18, WUMLParser.RULE_getMethod);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 195;
            this.match(WUMLParser.AT);
            this.state = 196;
            this.match(WUMLParser.T__3);
            this.state = 199;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 197;
                this.match(WUMLParser.LPAREN);
                this.state = 198;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_postMethod;
        return this;
    }

    PostMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    PostMethodContext.prototype.constructor = PostMethodContext;


    PostMethodContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterPostMethod(this);
        }
    };

    PostMethodContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitPostMethod(this);
        }
    };


    WUMLParser.PostMethodContext = PostMethodContext;

    WUMLParser.prototype.postMethod = function () {

        var localctx = new PostMethodContext(this, this._ctx, this.state);
        this.enterRule(localctx, 20, WUMLParser.RULE_postMethod);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 201;
            this.match(WUMLParser.AT);
            this.state = 202;
            this.match(WUMLParser.T__4);
            this.state = 205;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 203;
                this.match(WUMLParser.LPAREN);
                this.state = 204;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_putMethod;
        return this;
    }

    PutMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    PutMethodContext.prototype.constructor = PutMethodContext;


    PutMethodContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterPutMethod(this);
        }
    };

    PutMethodContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitPutMethod(this);
        }
    };


    WUMLParser.PutMethodContext = PutMethodContext;

    WUMLParser.prototype.putMethod = function () {

        var localctx = new PutMethodContext(this, this._ctx, this.state);
        this.enterRule(localctx, 22, WUMLParser.RULE_putMethod);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 207;
            this.match(WUMLParser.AT);
            this.state = 208;
            this.match(WUMLParser.T__5);
            this.state = 211;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 209;
                this.match(WUMLParser.LPAREN);
                this.state = 210;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_deleteMethod;
        return this;
    }

    DeleteMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    DeleteMethodContext.prototype.constructor = DeleteMethodContext;


    DeleteMethodContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterDeleteMethod(this);
        }
    };

    DeleteMethodContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitDeleteMethod(this);
        }
    };


    WUMLParser.DeleteMethodContext = DeleteMethodContext;

    WUMLParser.prototype.deleteMethod = function () {

        var localctx = new DeleteMethodContext(this, this._ctx, this.state);
        this.enterRule(localctx, 24, WUMLParser.RULE_deleteMethod);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 213;
            this.match(WUMLParser.AT);
            this.state = 214;
            this.match(WUMLParser.T__6);
            this.state = 217;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 215;
                this.match(WUMLParser.LPAREN);
                this.state = 216;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_headMethod;
        return this;
    }

    HeadMethodContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    HeadMethodContext.prototype.constructor = HeadMethodContext;


    HeadMethodContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterHeadMethod(this);
        }
    };

    HeadMethodContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitHeadMethod(this);
        }
    };


    WUMLParser.HeadMethodContext = HeadMethodContext;

    WUMLParser.prototype.headMethod = function () {

        var localctx = new HeadMethodContext(this, this._ctx, this.state);
        this.enterRule(localctx, 26, WUMLParser.RULE_headMethod);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 219;
            this.match(WUMLParser.AT);
            this.state = 220;
            this.match(WUMLParser.T__7);
            this.state = 223;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 221;
                this.match(WUMLParser.LPAREN);
                this.state = 222;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_prodAnt;
        return this;
    }

    ProdAntContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ProdAntContext.prototype.constructor = ProdAntContext;

    ProdAntContext.prototype.elementValue = function () {
        return this.getTypedRuleContext(ElementValueContext, 0);
    };

    ProdAntContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterProdAnt(this);
        }
    };

    ProdAntContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitProdAnt(this);
        }
    };


    WUMLParser.ProdAntContext = ProdAntContext;

    WUMLParser.prototype.prodAnt = function () {

        var localctx = new ProdAntContext(this, this._ctx, this.state);
        this.enterRule(localctx, 28, WUMLParser.RULE_prodAnt);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 225;
            this.match(WUMLParser.AT);
            this.state = 226;
            this.match(WUMLParser.T__8);
            this.state = 232;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 227;
                this.match(WUMLParser.LPAREN);
                this.state = 229;
                _la = this._input.LA(1);
                if (_la === WUMLParser.IntegerLiteral || _la === WUMLParser.StringLiteral) {
                    this.state = 228;
                    this.elementValue();
                }

                this.state = 231;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_conAnt;
        return this;
    }

    ConAntContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ConAntContext.prototype.constructor = ConAntContext;

    ConAntContext.prototype.elementValue = function () {
        return this.getTypedRuleContext(ElementValueContext, 0);
    };

    ConAntContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterConAnt(this);
        }
    };

    ConAntContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitConAnt(this);
        }
    };


    WUMLParser.ConAntContext = ConAntContext;

    WUMLParser.prototype.conAnt = function () {

        var localctx = new ConAntContext(this, this._ctx, this.state);
        this.enterRule(localctx, 30, WUMLParser.RULE_conAnt);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 234;
            this.match(WUMLParser.AT);
            this.state = 235;
            this.match(WUMLParser.T__9);
            this.state = 241;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 236;
                this.match(WUMLParser.LPAREN);
                this.state = 238;
                _la = this._input.LA(1);
                if (_la === WUMLParser.IntegerLiteral || _la === WUMLParser.StringLiteral) {
                    this.state = 237;
                    this.elementValue();
                }

                this.state = 240;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_antApiOperation;
        return this;
    }

    AntApiOperationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    AntApiOperationContext.prototype.constructor = AntApiOperationContext;

    AntApiOperationContext.prototype.elementValuePairs = function () {
        return this.getTypedRuleContext(ElementValuePairsContext, 0);
    };

    AntApiOperationContext.prototype.elementValue = function () {
        return this.getTypedRuleContext(ElementValueContext, 0);
    };

    AntApiOperationContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterAntApiOperation(this);
        }
    };

    AntApiOperationContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitAntApiOperation(this);
        }
    };


    WUMLParser.AntApiOperationContext = AntApiOperationContext;

    WUMLParser.prototype.antApiOperation = function () {

        var localctx = new AntApiOperationContext(this, this._ctx, this.state);
        this.enterRule(localctx, 32, WUMLParser.RULE_antApiOperation);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 243;
            this.match(WUMLParser.AT);
            this.state = 244;
            this.match(WUMLParser.T__10);
            this.state = 251;
            _la = this._input.LA(1);
            if (_la === WUMLParser.LPAREN) {
                this.state = 245;
                this.match(WUMLParser.LPAREN);
                this.state = 248;
                switch (this._input.LA(1)) {
                    case WUMLParser.Identifier:
                        this.state = 246;
                        this.elementValuePairs();
                        break;
                    case WUMLParser.IntegerLiteral:
                    case WUMLParser.StringLiteral:
                        this.state = 247;
                        this.elementValue();
                        break;
                    case WUMLParser.RPAREN:
                        break;
                    default:
                        throw new antlr4.error.NoViableAltException(this);
                }
                this.state = 250;
                this.match(WUMLParser.RPAREN);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_antApiResponses;
        return this;
    }

    AntApiResponsesContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    AntApiResponsesContext.prototype.constructor = AntApiResponsesContext;

    AntApiResponsesContext.prototype.antApiResponseSet = function () {
        return this.getTypedRuleContext(AntApiResponseSetContext, 0);
    };

    AntApiResponsesContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterAntApiResponses(this);
        }
    };

    AntApiResponsesContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitAntApiResponses(this);
        }
    };


    WUMLParser.AntApiResponsesContext = AntApiResponsesContext;

    WUMLParser.prototype.antApiResponses = function () {

        var localctx = new AntApiResponsesContext(this, this._ctx, this.state);
        this.enterRule(localctx, 34, WUMLParser.RULE_antApiResponses);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 253;
            this.match(WUMLParser.AT);
            this.state = 254;
            this.match(WUMLParser.T__11);
            this.state = 255;
            this.match(WUMLParser.LPAREN);
            this.state = 257;
            _la = this._input.LA(1);
            if (_la === WUMLParser.AT) {
                this.state = 256;
                this.antApiResponseSet();
            }

            this.state = 259;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_antApiResponseSet;
        return this;
    }

    AntApiResponseSetContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    AntApiResponseSetContext.prototype.constructor = AntApiResponseSetContext;

    AntApiResponseSetContext.prototype.antApiResponse = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(AntApiResponseContext);
        } else {
            return this.getTypedRuleContext(AntApiResponseContext, i);
        }
    };

    AntApiResponseSetContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterAntApiResponseSet(this);
        }
    };

    AntApiResponseSetContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitAntApiResponseSet(this);
        }
    };


    WUMLParser.AntApiResponseSetContext = AntApiResponseSetContext;

    WUMLParser.prototype.antApiResponseSet = function () {

        var localctx = new AntApiResponseSetContext(this, this._ctx, this.state);
        this.enterRule(localctx, 36, WUMLParser.RULE_antApiResponseSet);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 261;
            this.antApiResponse();
            this.state = 266;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.COMMA) {
                this.state = 262;
                this.match(WUMLParser.COMMA);
                this.state = 263;
                this.antApiResponse();
                this.state = 268;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_antApiResponse;
        return this;
    }

    AntApiResponseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    AntApiResponseContext.prototype.constructor = AntApiResponseContext;

    AntApiResponseContext.prototype.elementValuePairs = function () {
        return this.getTypedRuleContext(ElementValuePairsContext, 0);
    };

    AntApiResponseContext.prototype.elementValue = function () {
        return this.getTypedRuleContext(ElementValueContext, 0);
    };

    AntApiResponseContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterAntApiResponse(this);
        }
    };

    AntApiResponseContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitAntApiResponse(this);
        }
    };


    WUMLParser.AntApiResponseContext = AntApiResponseContext;

    WUMLParser.prototype.antApiResponse = function () {

        var localctx = new AntApiResponseContext(this, this._ctx, this.state);
        this.enterRule(localctx, 38, WUMLParser.RULE_antApiResponse);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 269;
            this.match(WUMLParser.AT);
            this.state = 270;
            this.match(WUMLParser.T__12);
            this.state = 271;
            this.match(WUMLParser.LPAREN);
            this.state = 274;
            switch (this._input.LA(1)) {
                case WUMLParser.Identifier:
                    this.state = 272;
                    this.elementValuePairs();
                    break;
                case WUMLParser.IntegerLiteral:
                case WUMLParser.StringLiteral:
                    this.state = 273;
                    this.elementValue();
                    break;
                case WUMLParser.RPAREN:
                    break;
                default:
                    throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 276;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_elementValuePairs;
        return this;
    }

    ElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ElementValuePairsContext.prototype.constructor = ElementValuePairsContext;

    ElementValuePairsContext.prototype.elementValuePair = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(ElementValuePairContext);
        } else {
            return this.getTypedRuleContext(ElementValuePairContext, i);
        }
    };

    ElementValuePairsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterElementValuePairs(this);
        }
    };

    ElementValuePairsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitElementValuePairs(this);
        }
    };


    WUMLParser.ElementValuePairsContext = ElementValuePairsContext;

    WUMLParser.prototype.elementValuePairs = function () {

        var localctx = new ElementValuePairsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 40, WUMLParser.RULE_elementValuePairs);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 278;
            this.elementValuePair();
            this.state = 283;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.COMMA) {
                this.state = 279;
                this.match(WUMLParser.COMMA);
                this.state = 280;
                this.elementValuePair();
                this.state = 285;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_sourceElementValuePairs;
        return this;
    }

    SourceElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    SourceElementValuePairsContext.prototype.constructor = SourceElementValuePairsContext;

    SourceElementValuePairsContext.prototype.protocol = function () {
        return this.getTypedRuleContext(ProtocolContext, 0);
    };

    SourceElementValuePairsContext.prototype.host = function () {
        return this.getTypedRuleContext(HostContext, 0);
    };

    SourceElementValuePairsContext.prototype.port = function () {
        return this.getTypedRuleContext(PortContext, 0);
    };

    SourceElementValuePairsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterSourceElementValuePairs(this);
        }
    };

    SourceElementValuePairsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitSourceElementValuePairs(this);
        }
    };


    WUMLParser.SourceElementValuePairsContext = SourceElementValuePairsContext;

    WUMLParser.prototype.sourceElementValuePairs = function () {

        var localctx = new SourceElementValuePairsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 42, WUMLParser.RULE_sourceElementValuePairs);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 286;
            this.protocol();
            this.state = 289;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 20, this._ctx);
            if (la_ === 1) {
                this.state = 287;
                this.match(WUMLParser.COMMA);
                this.state = 288;
                this.host();

            }
            this.state = 293;
            _la = this._input.LA(1);
            if (_la === WUMLParser.COMMA) {
                this.state = 291;
                this.match(WUMLParser.COMMA);
                this.state = 292;
                this.port();
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_apiElementValuePairs;
        return this;
    }

    ApiElementValuePairsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ApiElementValuePairsContext.prototype.constructor = ApiElementValuePairsContext;

    ApiElementValuePairsContext.prototype.producer = function () {
        return this.getTypedRuleContext(ProducerContext, 0);
    };

    ApiElementValuePairsContext.prototype.tags = function () {
        return this.getTypedRuleContext(TagsContext, 0);
    };

    ApiElementValuePairsContext.prototype.descripton = function () {
        return this.getTypedRuleContext(DescriptonContext, 0);
    };

    ApiElementValuePairsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterApiElementValuePairs(this);
        }
    };

    ApiElementValuePairsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitApiElementValuePairs(this);
        }
    };


    WUMLParser.ApiElementValuePairsContext = ApiElementValuePairsContext;

    WUMLParser.prototype.apiElementValuePairs = function () {

        var localctx = new ApiElementValuePairsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 44, WUMLParser.RULE_apiElementValuePairs);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 298;
            _la = this._input.LA(1);
            if (_la === WUMLParser.T__16) {
                this.state = 295;
                this.tags();
                this.state = 296;
                this.match(WUMLParser.COMMA);
            }

            this.state = 303;
            _la = this._input.LA(1);
            if (_la === WUMLParser.T__17) {
                this.state = 300;
                this.descripton();
                this.state = 301;
                this.match(WUMLParser.COMMA);
            }

            this.state = 305;
            this.producer();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_protocol;
        return this;
    }

    ProtocolContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ProtocolContext.prototype.constructor = ProtocolContext;

    ProtocolContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    ProtocolContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterProtocol(this);
        }
    };

    ProtocolContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitProtocol(this);
        }
    };


    WUMLParser.ProtocolContext = ProtocolContext;

    WUMLParser.prototype.protocol = function () {

        var localctx = new ProtocolContext(this, this._ctx, this.state);
        this.enterRule(localctx, 46, WUMLParser.RULE_protocol);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 307;
            this.match(WUMLParser.T__13);
            this.state = 308;
            this.match(WUMLParser.ASSIGN);
            this.state = 309;
            this.match(WUMLParser.StringLiteral);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_host;
        return this;
    }

    HostContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    HostContext.prototype.constructor = HostContext;

    HostContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    HostContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterHost(this);
        }
    };

    HostContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitHost(this);
        }
    };


    WUMLParser.HostContext = HostContext;

    WUMLParser.prototype.host = function () {

        var localctx = new HostContext(this, this._ctx, this.state);
        this.enterRule(localctx, 48, WUMLParser.RULE_host);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 311;
            this.match(WUMLParser.T__14);
            this.state = 312;
            this.match(WUMLParser.ASSIGN);
            this.state = 313;
            this.match(WUMLParser.StringLiteral);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_port;
        return this;
    }

    PortContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    PortContext.prototype.constructor = PortContext;

    PortContext.prototype.IntegerLiteral = function () {
        return this.getToken(WUMLParser.IntegerLiteral, 0);
    };

    PortContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterPort(this);
        }
    };

    PortContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitPort(this);
        }
    };


    WUMLParser.PortContext = PortContext;

    WUMLParser.prototype.port = function () {

        var localctx = new PortContext(this, this._ctx, this.state);
        this.enterRule(localctx, 50, WUMLParser.RULE_port);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 315;
            this.match(WUMLParser.T__15);
            this.state = 316;
            this.match(WUMLParser.ASSIGN);
            this.state = 317;
            this.match(WUMLParser.IntegerLiteral);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_tags;
        return this;
    }

    TagsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    TagsContext.prototype.constructor = TagsContext;

    TagsContext.prototype.tag = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(TagContext);
        } else {
            return this.getTypedRuleContext(TagContext, i);
        }
    };

    TagsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterTags(this);
        }
    };

    TagsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitTags(this);
        }
    };


    WUMLParser.TagsContext = TagsContext;

    WUMLParser.prototype.tags = function () {

        var localctx = new TagsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 52, WUMLParser.RULE_tags);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 319;
            this.match(WUMLParser.T__16);
            this.state = 320;
            this.match(WUMLParser.ASSIGN);
            this.state = 322;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            do {
                this.state = 321;
                this.tag();
                this.state = 324;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            } while (_la === WUMLParser.LBRACE);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_tag;
        return this;
    }

    TagContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    TagContext.prototype.constructor = TagContext;

    TagContext.prototype.StringLiteral = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.StringLiteral);
        } else {
            return this.getToken(WUMLParser.StringLiteral, i);
        }
    };


    TagContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterTag(this);
        }
    };

    TagContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitTag(this);
        }
    };


    WUMLParser.TagContext = TagContext;

    WUMLParser.prototype.tag = function () {

        var localctx = new TagContext(this, this._ctx, this.state);
        this.enterRule(localctx, 54, WUMLParser.RULE_tag);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 326;
            this.match(WUMLParser.LBRACE);
            this.state = 327;
            this.match(WUMLParser.StringLiteral);
            this.state = 332;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.COMMA) {
                this.state = 328;
                this.match(WUMLParser.COMMA);
                this.state = 329;
                this.match(WUMLParser.StringLiteral);
                this.state = 334;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
            this.state = 335;
            this.match(WUMLParser.RBRACE);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_descripton;
        return this;
    }

    DescriptonContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    DescriptonContext.prototype.constructor = DescriptonContext;

    DescriptonContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    DescriptonContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterDescripton(this);
        }
    };

    DescriptonContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitDescripton(this);
        }
    };


    WUMLParser.DescriptonContext = DescriptonContext;

    WUMLParser.prototype.descripton = function () {

        var localctx = new DescriptonContext(this, this._ctx, this.state);
        this.enterRule(localctx, 56, WUMLParser.RULE_descripton);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 337;
            this.match(WUMLParser.T__17);
            this.state = 338;
            this.match(WUMLParser.ASSIGN);
            this.state = 339;
            this.match(WUMLParser.StringLiteral);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_producer;
        return this;
    }

    ProducerContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ProducerContext.prototype.constructor = ProducerContext;

    ProducerContext.prototype.mediaType = function () {
        return this.getTypedRuleContext(MediaTypeContext, 0);
    };

    ProducerContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterProducer(this);
        }
    };

    ProducerContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitProducer(this);
        }
    };


    WUMLParser.ProducerContext = ProducerContext;

    WUMLParser.prototype.producer = function () {

        var localctx = new ProducerContext(this, this._ctx, this.state);
        this.enterRule(localctx, 58, WUMLParser.RULE_producer);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 341;
            this.match(WUMLParser.T__18);
            this.state = 342;
            this.match(WUMLParser.ASSIGN);
            this.state = 343;
            this.mediaType();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_constant;
        return this;
    }

    ConstantContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ConstantContext.prototype.constructor = ConstantContext;

    ConstantContext.prototype.CONSTANT = function () {
        return this.getToken(WUMLParser.CONSTANT, 0);
    };

    ConstantContext.prototype.type = function () {
        return this.getTypedRuleContext(TypeContext, 0);
    };

    ConstantContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    ConstantContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    ConstantContext.prototype.classType = function () {
        return this.getTypedRuleContext(ClassTypeContext, 0);
    };

    ConstantContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    ConstantContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterConstant(this);
        }
    };

    ConstantContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitConstant(this);
        }
    };


    WUMLParser.ConstantContext = ConstantContext;

    WUMLParser.prototype.constant = function () {

        var localctx = new ConstantContext(this, this._ctx, this.state);
        this.enterRule(localctx, 60, WUMLParser.RULE_constant);
        var _la = 0; // Token type
        try {
            this.state = 365;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 27, this._ctx);
            switch (la_) {
                case 1:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 345;
                    this.match(WUMLParser.CONSTANT);
                    this.state = 346;
                    this.type();
                    this.state = 347;
                    this.match(WUMLParser.Identifier);
                    this.state = 348;
                    this.match(WUMLParser.ASSIGN);
                    this.state = 349;
                    this.literal();
                    this.state = 350;
                    this.match(WUMLParser.SEMI);
                    break;

                case 2:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 352;
                    this.match(WUMLParser.CONSTANT);
                    this.state = 353;
                    this.classType();
                    this.state = 354;
                    this.match(WUMLParser.Identifier);
                    this.state = 355;
                    this.match(WUMLParser.ASSIGN);
                    this.state = 356;
                    this.match(WUMLParser.NEW);
                    this.state = 357;
                    this.match(WUMLParser.Identifier);
                    this.state = 358;
                    this.match(WUMLParser.LPAREN);
                    this.state = 360;
                    _la = this._input.LA(1);
                    if (_la === WUMLParser.StringLiteral) {
                        this.state = 359;
                        this.match(WUMLParser.StringLiteral);
                    }

                    this.state = 362;
                    this.match(WUMLParser.RPAREN);
                    this.state = 363;
                    this.match(WUMLParser.SEMI);
                    break;

            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_elementValuePair;
        return this;
    }

    ElementValuePairContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ElementValuePairContext.prototype.constructor = ElementValuePairContext;

    ElementValuePairContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    ElementValuePairContext.prototype.elementValue = function () {
        return this.getTypedRuleContext(ElementValueContext, 0);
    };

    ElementValuePairContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterElementValuePair(this);
        }
    };

    ElementValuePairContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitElementValuePair(this);
        }
    };


    WUMLParser.ElementValuePairContext = ElementValuePairContext;

    WUMLParser.prototype.elementValuePair = function () {

        var localctx = new ElementValuePairContext(this, this._ctx, this.state);
        this.enterRule(localctx, 62, WUMLParser.RULE_elementValuePair);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 367;
            this.match(WUMLParser.Identifier);
            this.state = 368;
            this.match(WUMLParser.ASSIGN);
            this.state = 369;
            this.elementValue();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_elementValue;
        return this;
    }

    ElementValueContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ElementValueContext.prototype.constructor = ElementValueContext;

    ElementValueContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    ElementValueContext.prototype.IntegerLiteral = function () {
        return this.getToken(WUMLParser.IntegerLiteral, 0);
    };

    ElementValueContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterElementValue(this);
        }
    };

    ElementValueContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitElementValue(this);
        }
    };


    WUMLParser.ElementValueContext = ElementValueContext;

    WUMLParser.prototype.elementValue = function () {

        var localctx = new ElementValueContext(this, this._ctx, this.state);
        this.enterRule(localctx, 64, WUMLParser.RULE_elementValue);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 371;
            _la = this._input.LA(1);
            if (!(_la === WUMLParser.IntegerLiteral || _la === WUMLParser.StringLiteral)) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_resource;
        return this;
    }

    ResourceContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ResourceContext.prototype.constructor = ResourceContext;

    ResourceContext.prototype.httpMethods = function () {
        return this.getTypedRuleContext(HttpMethodsContext, 0);
    };

    ResourceContext.prototype.resourcePath = function () {
        return this.getTypedRuleContext(ResourcePathContext, 0);
    };

    ResourceContext.prototype.resourceDeclaration = function () {
        return this.getTypedRuleContext(ResourceDeclarationContext, 0);
    };

    ResourceContext.prototype.prodAnt = function () {
        return this.getTypedRuleContext(ProdAntContext, 0);
    };

    ResourceContext.prototype.conAnt = function () {
        return this.getTypedRuleContext(ConAntContext, 0);
    };

    ResourceContext.prototype.antApiOperation = function () {
        return this.getTypedRuleContext(AntApiOperationContext, 0);
    };

    ResourceContext.prototype.antApiResponses = function () {
        return this.getTypedRuleContext(AntApiResponsesContext, 0);
    };

    ResourceContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterResource(this);
        }
    };

    ResourceContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitResource(this);
        }
    };


    WUMLParser.ResourceContext = ResourceContext;

    WUMLParser.prototype.resource = function () {

        var localctx = new ResourceContext(this, this._ctx, this.state);
        this.enterRule(localctx, 66, WUMLParser.RULE_resource);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 373;
            this.httpMethods();
            this.state = 375;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 28, this._ctx);
            if (la_ === 1) {
                this.state = 374;
                this.prodAnt();

            }
            this.state = 378;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 29, this._ctx);
            if (la_ === 1) {
                this.state = 377;
                this.conAnt();

            }
            this.state = 381;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 30, this._ctx);
            if (la_ === 1) {
                this.state = 380;
                this.antApiOperation();

            }
            this.state = 384;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 31, this._ctx);
            if (la_ === 1) {
                this.state = 383;
                this.antApiResponses();

            }
            this.state = 386;
            this.resourcePath();
            this.state = 387;
            this.resourceDeclaration();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_httpMethods;
        return this;
    }

    HttpMethodsContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    HttpMethodsContext.prototype.constructor = HttpMethodsContext;

    HttpMethodsContext.prototype.getMethod = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(GetMethodContext);
        } else {
            return this.getTypedRuleContext(GetMethodContext, i);
        }
    };

    HttpMethodsContext.prototype.postMethod = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(PostMethodContext);
        } else {
            return this.getTypedRuleContext(PostMethodContext, i);
        }
    };

    HttpMethodsContext.prototype.putMethod = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(PutMethodContext);
        } else {
            return this.getTypedRuleContext(PutMethodContext, i);
        }
    };

    HttpMethodsContext.prototype.deleteMethod = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(DeleteMethodContext);
        } else {
            return this.getTypedRuleContext(DeleteMethodContext, i);
        }
    };

    HttpMethodsContext.prototype.headMethod = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(HeadMethodContext);
        } else {
            return this.getTypedRuleContext(HeadMethodContext, i);
        }
    };

    HttpMethodsContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterHttpMethods(this);
        }
    };

    HttpMethodsContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitHttpMethods(this);
        }
    };


    WUMLParser.HttpMethodsContext = HttpMethodsContext;

    WUMLParser.prototype.httpMethods = function () {

        var localctx = new HttpMethodsContext(this, this._ctx, this.state);
        this.enterRule(localctx, 68, WUMLParser.RULE_httpMethods);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 396;
            this._errHandler.sync(this);
            var _alt = this._interp.adaptivePredict(this._input, 33, this._ctx)
            while (_alt != 2 && _alt != antlr4.atn.ATN.INVALID_ALT_NUMBER) {
                if (_alt === 1) {
                    this.state = 394;
                    this._errHandler.sync(this);
                    var la_ = this._interp.adaptivePredict(this._input, 32, this._ctx);
                    switch (la_) {
                        case 1:
                            this.state = 389;
                            this.getMethod();
                            break;

                        case 2:
                            this.state = 390;
                            this.postMethod();
                            break;

                        case 3:
                            this.state = 391;
                            this.putMethod();
                            break;

                        case 4:
                            this.state = 392;
                            this.deleteMethod();
                            break;

                        case 5:
                            this.state = 393;
                            this.headMethod();
                            break;

                    }
                }
                this.state = 398;
                this._errHandler.sync(this);
                _alt = this._interp.adaptivePredict(this._input, 33, this._ctx);
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_qualifiedName;
        return this;
    }

    QualifiedNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    QualifiedNameContext.prototype.constructor = QualifiedNameContext;

    QualifiedNameContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    QualifiedNameContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterQualifiedName(this);
        }
    };

    QualifiedNameContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitQualifiedName(this);
        }
    };


    WUMLParser.QualifiedNameContext = QualifiedNameContext;

    WUMLParser.prototype.qualifiedName = function () {

        var localctx = new QualifiedNameContext(this, this._ctx, this.state);
        this.enterRule(localctx, 70, WUMLParser.RULE_qualifiedName);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 399;
            this.match(WUMLParser.Identifier);
            this.state = 404;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.DOT) {
                this.state = 400;
                this.match(WUMLParser.DOT);
                this.state = 401;
                this.match(WUMLParser.Identifier);
                this.state = 406;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_resourceDeclaration;
        return this;
    }

    ResourceDeclarationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ResourceDeclarationContext.prototype.constructor = ResourceDeclarationContext;

    ResourceDeclarationContext.prototype.resourceName = function () {
        return this.getTypedRuleContext(ResourceNameContext, 0);
    };

    ResourceDeclarationContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    ResourceDeclarationContext.prototype.block = function () {
        return this.getTypedRuleContext(BlockContext, 0);
    };

    ResourceDeclarationContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterResourceDeclaration(this);
        }
    };

    ResourceDeclarationContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitResourceDeclaration(this);
        }
    };


    WUMLParser.ResourceDeclarationContext = ResourceDeclarationContext;

    WUMLParser.prototype.resourceDeclaration = function () {

        var localctx = new ResourceDeclarationContext(this, this._ctx, this.state);
        this.enterRule(localctx, 72, WUMLParser.RULE_resourceDeclaration);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 407;
            this.match(WUMLParser.T__19);
            this.state = 408;
            this.resourceName();
            this.state = 409;
            this.match(WUMLParser.LPAREN);
            this.state = 410;
            this.match(WUMLParser.T__20);
            this.state = 411;
            this.match(WUMLParser.Identifier);
            this.state = 412;
            this.match(WUMLParser.RPAREN);
            this.state = 413;
            this.block();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_resourceName;
        return this;
    }

    ResourceNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ResourceNameContext.prototype.constructor = ResourceNameContext;

    ResourceNameContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    ResourceNameContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterResourceName(this);
        }
    };

    ResourceNameContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitResourceName(this);
        }
    };


    WUMLParser.ResourceNameContext = ResourceNameContext;

    WUMLParser.prototype.resourceName = function () {

        var localctx = new ResourceNameContext(this, this._ctx, this.state);
        this.enterRule(localctx, 74, WUMLParser.RULE_resourceName);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 415;
            this.match(WUMLParser.Identifier);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_block;
        return this;
    }

    BlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    BlockContext.prototype.constructor = BlockContext;

    BlockContext.prototype.blockStatement = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(BlockStatementContext);
        } else {
            return this.getTypedRuleContext(BlockStatementContext, i);
        }
    };

    BlockContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterBlock(this);
        }
    };

    BlockContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitBlock(this);
        }
    };


    WUMLParser.BlockContext = BlockContext;

    WUMLParser.prototype.block = function () {

        var localctx = new BlockContext(this, this._ctx, this.state);
        this.enterRule(localctx, 76, WUMLParser.RULE_block);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 417;
            this.match(WUMLParser.LBRACE);
            this.state = 421;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (((((_la - 21)) & ~0x1f) == 0 && ((1 << (_la - 21)) & ((1 << (WUMLParser.T__20 - 21)) | (1 << (WUMLParser.T__30 - 21)) | (1 << (WUMLParser.T__31 - 21)) | (1 << (WUMLParser.T__35 - 21)) | (1 << (WUMLParser.BOOLEAN - 21)) | (1 << (WUMLParser.BYTE - 21)) | (1 << (WUMLParser.CHAR - 21)) | (1 << (WUMLParser.DOUBLE - 21)))) !== 0) || ((((_la - 57)) & ~0x1f) == 0 && ((1 << (_la - 57)) & ((1 << (WUMLParser.FLOAT - 57)) | (1 << (WUMLParser.IF - 57)) | (1 << (WUMLParser.INT - 57)) | (1 << (WUMLParser.LONG - 57)) | (1 << (WUMLParser.SHORT - 57)) | (1 << (WUMLParser.TRY - 57)))) !== 0) || _la === WUMLParser.Identifier) {
                this.state = 418;
                this.blockStatement();
                this.state = 423;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
            this.state = 424;
            this.match(WUMLParser.RBRACE);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_blockStatement;
        return this;
    }

    BlockStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    BlockStatementContext.prototype.constructor = BlockStatementContext;

    BlockStatementContext.prototype.localVariableDeclarationStatement = function () {
        return this.getTypedRuleContext(LocalVariableDeclarationStatementContext, 0);
    };

    BlockStatementContext.prototype.localVaribaleInitializationStatement = function () {
        return this.getTypedRuleContext(LocalVaribaleInitializationStatementContext, 0);
    };

    BlockStatementContext.prototype.localVaribaleAssignmentStatement = function () {
        return this.getTypedRuleContext(LocalVaribaleAssignmentStatementContext, 0);
    };

    BlockStatementContext.prototype.messageModificationStatement = function () {
        return this.getTypedRuleContext(MessageModificationStatementContext, 0);
    };

    BlockStatementContext.prototype.returnStatement = function () {
        return this.getTypedRuleContext(ReturnStatementContext, 0);
    };

    BlockStatementContext.prototype.logMediatorStatement = function () {
        return this.getTypedRuleContext(LogMediatorStatementContext, 0);
    };

    BlockStatementContext.prototype.tryCatchBlock = function () {
        return this.getTypedRuleContext(TryCatchBlockContext, 0);
    };

    BlockStatementContext.prototype.ifElseBlock = function () {
        return this.getTypedRuleContext(IfElseBlockContext, 0);
    };

    BlockStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterBlockStatement(this);
        }
    };

    BlockStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitBlockStatement(this);
        }
    };


    WUMLParser.BlockStatementContext = BlockStatementContext;

    WUMLParser.prototype.blockStatement = function () {

        var localctx = new BlockStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 78, WUMLParser.RULE_blockStatement);
        try {
            this.state = 434;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 36, this._ctx);
            switch (la_) {
                case 1:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 426;
                    this.localVariableDeclarationStatement();
                    break;

                case 2:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 427;
                    this.localVaribaleInitializationStatement();
                    break;

                case 3:
                    this.enterOuterAlt(localctx, 3);
                    this.state = 428;
                    this.localVaribaleAssignmentStatement();
                    break;

                case 4:
                    this.enterOuterAlt(localctx, 4);
                    this.state = 429;
                    this.messageModificationStatement();
                    break;

                case 5:
                    this.enterOuterAlt(localctx, 5);
                    this.state = 430;
                    this.returnStatement();
                    break;

                case 6:
                    this.enterOuterAlt(localctx, 6);
                    this.state = 431;
                    this.logMediatorStatement();
                    break;

                case 7:
                    this.enterOuterAlt(localctx, 7);
                    this.state = 432;
                    this.tryCatchBlock();
                    break;

                case 8:
                    this.enterOuterAlt(localctx, 8);
                    this.state = 433;
                    this.ifElseBlock();
                    break;

            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_tryCatchBlock;
        return this;
    }

    TryCatchBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    TryCatchBlockContext.prototype.constructor = TryCatchBlockContext;

    TryCatchBlockContext.prototype.tryClause = function () {
        return this.getTypedRuleContext(TryClauseContext, 0);
    };

    TryCatchBlockContext.prototype.catchClause = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(CatchClauseContext);
        } else {
            return this.getTypedRuleContext(CatchClauseContext, i);
        }
    };

    TryCatchBlockContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterTryCatchBlock(this);
        }
    };

    TryCatchBlockContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitTryCatchBlock(this);
        }
    };


    WUMLParser.TryCatchBlockContext = TryCatchBlockContext;

    WUMLParser.prototype.tryCatchBlock = function () {

        var localctx = new TryCatchBlockContext(this, this._ctx, this.state);
        this.enterRule(localctx, 80, WUMLParser.RULE_tryCatchBlock);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 436;
            this.tryClause();
            this.state = 438;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            do {
                this.state = 437;
                this.catchClause();
                this.state = 440;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            } while (_la === WUMLParser.CATCH);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_tryClause;
        return this;
    }

    TryClauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    TryClauseContext.prototype.constructor = TryClauseContext;

    TryClauseContext.prototype.block = function () {
        return this.getTypedRuleContext(BlockContext, 0);
    };

    TryClauseContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterTryClause(this);
        }
    };

    TryClauseContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitTryClause(this);
        }
    };


    WUMLParser.TryClauseContext = TryClauseContext;

    WUMLParser.prototype.tryClause = function () {

        var localctx = new TryClauseContext(this, this._ctx, this.state);
        this.enterRule(localctx, 82, WUMLParser.RULE_tryClause);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 442;
            this.match(WUMLParser.TRY);
            this.state = 443;
            this.block();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_catchClause;
        return this;
    }

    CatchClauseContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    CatchClauseContext.prototype.constructor = CatchClauseContext;

    CatchClauseContext.prototype.exceptionHandler = function () {
        return this.getTypedRuleContext(ExceptionHandlerContext, 0);
    };

    CatchClauseContext.prototype.block = function () {
        return this.getTypedRuleContext(BlockContext, 0);
    };

    CatchClauseContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterCatchClause(this);
        }
    };

    CatchClauseContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitCatchClause(this);
        }
    };


    WUMLParser.CatchClauseContext = CatchClauseContext;

    WUMLParser.prototype.catchClause = function () {

        var localctx = new CatchClauseContext(this, this._ctx, this.state);
        this.enterRule(localctx, 84, WUMLParser.RULE_catchClause);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 445;
            this.match(WUMLParser.CATCH);
            this.state = 446;
            this.match(WUMLParser.LPAREN);
            this.state = 447;
            this.exceptionHandler();
            this.state = 448;
            this.match(WUMLParser.RPAREN);
            this.state = 449;
            this.block();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_exceptionHandler;
        return this;
    }

    ExceptionHandlerContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ExceptionHandlerContext.prototype.constructor = ExceptionHandlerContext;

    ExceptionHandlerContext.prototype.exceptionType = function () {
        return this.getTypedRuleContext(ExceptionTypeContext, 0);
    };

    ExceptionHandlerContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    ExceptionHandlerContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterExceptionHandler(this);
        }
    };

    ExceptionHandlerContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitExceptionHandler(this);
        }
    };


    WUMLParser.ExceptionHandlerContext = ExceptionHandlerContext;

    WUMLParser.prototype.exceptionHandler = function () {

        var localctx = new ExceptionHandlerContext(this, this._ctx, this.state);
        this.enterRule(localctx, 86, WUMLParser.RULE_exceptionHandler);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 451;
            this.exceptionType();
            this.state = 452;
            this.match(WUMLParser.Identifier);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_exceptionType;
        return this;
    }

    ExceptionTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ExceptionTypeContext.prototype.constructor = ExceptionTypeContext;


    ExceptionTypeContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterExceptionType(this);
        }
    };

    ExceptionTypeContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitExceptionType(this);
        }
    };


    WUMLParser.ExceptionTypeContext = ExceptionTypeContext;

    WUMLParser.prototype.exceptionType = function () {

        var localctx = new ExceptionTypeContext(this, this._ctx, this.state);
        this.enterRule(localctx, 88, WUMLParser.RULE_exceptionType);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 454;
            _la = this._input.LA(1);
            if (!((((_la) & ~0x1f) == 0 && ((1 << _la) & ((1 << WUMLParser.T__21) | (1 << WUMLParser.T__22) | (1 << WUMLParser.T__23) | (1 << WUMLParser.T__24))) !== 0))) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_ifElseBlock;
        return this;
    }

    IfElseBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    IfElseBlockContext.prototype.constructor = IfElseBlockContext;

    IfElseBlockContext.prototype.ifBlock = function () {
        return this.getTypedRuleContext(IfBlockContext, 0);
    };

    IfElseBlockContext.prototype.elseBlock = function () {
        return this.getTypedRuleContext(ElseBlockContext, 0);
    };

    IfElseBlockContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterIfElseBlock(this);
        }
    };

    IfElseBlockContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitIfElseBlock(this);
        }
    };


    WUMLParser.IfElseBlockContext = IfElseBlockContext;

    WUMLParser.prototype.ifElseBlock = function () {

        var localctx = new IfElseBlockContext(this, this._ctx, this.state);
        this.enterRule(localctx, 90, WUMLParser.RULE_ifElseBlock);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 456;
            this.ifBlock();
            this.state = 458;
            _la = this._input.LA(1);
            if (_la === WUMLParser.ELSE) {
                this.state = 457;
                this.elseBlock();
            }

        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_ifBlock;
        return this;
    }

    IfBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    IfBlockContext.prototype.constructor = IfBlockContext;

    IfBlockContext.prototype.parExpression = function () {
        return this.getTypedRuleContext(ParExpressionContext, 0);
    };

    IfBlockContext.prototype.block = function () {
        return this.getTypedRuleContext(BlockContext, 0);
    };

    IfBlockContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterIfBlock(this);
        }
    };

    IfBlockContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitIfBlock(this);
        }
    };


    WUMLParser.IfBlockContext = IfBlockContext;

    WUMLParser.prototype.ifBlock = function () {

        var localctx = new IfBlockContext(this, this._ctx, this.state);
        this.enterRule(localctx, 92, WUMLParser.RULE_ifBlock);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 460;
            this.match(WUMLParser.IF);
            this.state = 461;
            this.parExpression();
            this.state = 462;
            this.block();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_elseBlock;
        return this;
    }

    ElseBlockContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ElseBlockContext.prototype.constructor = ElseBlockContext;

    ElseBlockContext.prototype.block = function () {
        return this.getTypedRuleContext(BlockContext, 0);
    };

    ElseBlockContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterElseBlock(this);
        }
    };

    ElseBlockContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitElseBlock(this);
        }
    };


    WUMLParser.ElseBlockContext = ElseBlockContext;

    WUMLParser.prototype.elseBlock = function () {

        var localctx = new ElseBlockContext(this, this._ctx, this.state);
        this.enterRule(localctx, 94, WUMLParser.RULE_elseBlock);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 464;
            this.match(WUMLParser.ELSE);
            this.state = 465;
            this.block();
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_localVariableDeclarationStatement;
        return this;
    }

    LocalVariableDeclarationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LocalVariableDeclarationStatementContext.prototype.constructor = LocalVariableDeclarationStatementContext;

    LocalVariableDeclarationStatementContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    LocalVariableDeclarationStatementContext.prototype.type = function () {
        return this.getTypedRuleContext(TypeContext, 0);
    };

    LocalVariableDeclarationStatementContext.prototype.classType = function () {
        return this.getTypedRuleContext(ClassTypeContext, 0);
    };

    LocalVariableDeclarationStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLocalVariableDeclarationStatement(this);
        }
    };

    LocalVariableDeclarationStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLocalVariableDeclarationStatement(this);
        }
    };


    WUMLParser.LocalVariableDeclarationStatementContext = LocalVariableDeclarationStatementContext;

    WUMLParser.prototype.localVariableDeclarationStatement = function () {

        var localctx = new LocalVariableDeclarationStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 96, WUMLParser.RULE_localVariableDeclarationStatement);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 469;
            switch (this._input.LA(1)) {
                case WUMLParser.BOOLEAN:
                case WUMLParser.BYTE:
                case WUMLParser.CHAR:
                case WUMLParser.DOUBLE:
                case WUMLParser.FLOAT:
                case WUMLParser.INT:
                case WUMLParser.LONG:
                case WUMLParser.SHORT:
                    this.state = 467;
                    this.type();
                    break;
                case WUMLParser.T__20:
                case WUMLParser.T__35:
                    this.state = 468;
                    this.classType();
                    break;
                default:
                    throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 471;
            this.match(WUMLParser.Identifier);
            this.state = 472;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function LocalVaribaleInitializationStatementContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_localVaribaleInitializationStatement;
        return this;
    }

    LocalVaribaleInitializationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LocalVaribaleInitializationStatementContext.prototype.constructor = LocalVaribaleInitializationStatementContext;

    LocalVaribaleInitializationStatementContext.prototype.type = function () {
        return this.getTypedRuleContext(TypeContext, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.newTypeObjectCreation = function () {
        return this.getTypedRuleContext(NewTypeObjectCreationContext, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.classType = function () {
        return this.getTypedRuleContext(ClassTypeContext, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.mediatorCall = function () {
        return this.getTypedRuleContext(MediatorCallContext, 0);
    };

    LocalVaribaleInitializationStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLocalVaribaleInitializationStatement(this);
        }
    };

    LocalVaribaleInitializationStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLocalVaribaleInitializationStatement(this);
        }
    };


    WUMLParser.LocalVaribaleInitializationStatementContext = LocalVaribaleInitializationStatementContext;

    WUMLParser.prototype.localVaribaleInitializationStatement = function () {

        var localctx = new LocalVaribaleInitializationStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 98, WUMLParser.RULE_localVaribaleInitializationStatement);
        try {
            this.state = 485;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 40, this._ctx);
            switch (la_) {
                case 1:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 474;
                    this.type();
                    this.state = 475;
                    this.match(WUMLParser.Identifier);
                    this.state = 476;
                    this.match(WUMLParser.ASSIGN);
                    this.state = 477;
                    this.literal();
                    this.state = 478;
                    this.match(WUMLParser.SEMI);
                    break;

                case 2:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 480;
                    this.newTypeObjectCreation();
                    break;

                case 3:
                    this.enterOuterAlt(localctx, 3);
                    this.state = 481;
                    this.classType();
                    this.state = 482;
                    this.mediatorCall();
                    this.state = 483;
                    this.match(WUMLParser.SEMI);
                    break;

            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function LocalVaribaleAssignmentStatementContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_localVaribaleAssignmentStatement;
        return this;
    }

    LocalVaribaleAssignmentStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LocalVaribaleAssignmentStatementContext.prototype.constructor = LocalVaribaleAssignmentStatementContext;

    LocalVaribaleAssignmentStatementContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    LocalVaribaleAssignmentStatementContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    LocalVaribaleAssignmentStatementContext.prototype.newTypeObjectCreation = function () {
        return this.getTypedRuleContext(NewTypeObjectCreationContext, 0);
    };

    LocalVaribaleAssignmentStatementContext.prototype.mediatorCall = function () {
        return this.getTypedRuleContext(MediatorCallContext, 0);
    };

    LocalVaribaleAssignmentStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLocalVaribaleAssignmentStatement(this);
        }
    };

    LocalVaribaleAssignmentStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLocalVaribaleAssignmentStatement(this);
        }
    };


    WUMLParser.LocalVaribaleAssignmentStatementContext = LocalVaribaleAssignmentStatementContext;

    WUMLParser.prototype.localVaribaleAssignmentStatement = function () {

        var localctx = new LocalVaribaleAssignmentStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 100, WUMLParser.RULE_localVaribaleAssignmentStatement);
        try {
            this.state = 498;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 41, this._ctx);
            switch (la_) {
                case 1:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 487;
                    this.match(WUMLParser.Identifier);
                    this.state = 488;
                    this.match(WUMLParser.ASSIGN);
                    this.state = 489;
                    this.literal();
                    this.state = 490;
                    this.match(WUMLParser.SEMI);
                    break;

                case 2:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 492;
                    this.newTypeObjectCreation();
                    this.state = 493;
                    this.match(WUMLParser.SEMI);
                    break;

                case 3:
                    this.enterOuterAlt(localctx, 3);
                    this.state = 495;
                    this.mediatorCall();
                    this.state = 496;
                    this.match(WUMLParser.SEMI);
                    break;

            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function LogMediatorStatementContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_logMediatorStatement;
        return this;
    }

    LogMediatorStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LogMediatorStatementContext.prototype.constructor = LogMediatorStatementContext;

    LogMediatorStatementContext.prototype.logMediatorCall = function () {
        return this.getTypedRuleContext(LogMediatorCallContext, 0);
    };

    LogMediatorStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLogMediatorStatement(this);
        }
    };

    LogMediatorStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLogMediatorStatement(this);
        }
    };


    WUMLParser.LogMediatorStatementContext = LogMediatorStatementContext;

    WUMLParser.prototype.logMediatorStatement = function () {

        var localctx = new LogMediatorStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 102, WUMLParser.RULE_logMediatorStatement);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 500;
            this.logMediatorCall();
            this.state = 501;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_newTypeObjectCreation;
        return this;
    }

    NewTypeObjectCreationContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    NewTypeObjectCreationContext.prototype.constructor = NewTypeObjectCreationContext;

    NewTypeObjectCreationContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    NewTypeObjectCreationContext.prototype.classType = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(ClassTypeContext);
        } else {
            return this.getTypedRuleContext(ClassTypeContext, i);
        }
    };

    NewTypeObjectCreationContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterNewTypeObjectCreation(this);
        }
    };

    NewTypeObjectCreationContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitNewTypeObjectCreation(this);
        }
    };


    WUMLParser.NewTypeObjectCreationContext = NewTypeObjectCreationContext;

    WUMLParser.prototype.newTypeObjectCreation = function () {

        var localctx = new NewTypeObjectCreationContext(this, this._ctx, this.state);
        this.enterRule(localctx, 104, WUMLParser.RULE_newTypeObjectCreation);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 504;
            _la = this._input.LA(1);
            if (_la === WUMLParser.T__20 || _la === WUMLParser.T__35) {
                this.state = 503;
                this.classType();
            }

            this.state = 506;
            this.match(WUMLParser.Identifier);
            this.state = 507;
            this.match(WUMLParser.ASSIGN);
            this.state = 508;
            this.match(WUMLParser.NEW);
            this.state = 509;
            this.classType();
            this.state = 510;
            this.match(WUMLParser.LPAREN);
            this.state = 511;
            this.match(WUMLParser.RPAREN);
            this.state = 512;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_mediatorCall;
        return this;
    }

    MediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    MediatorCallContext.prototype.constructor = MediatorCallContext;

    MediatorCallContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    MediatorCallContext.prototype.invokeMediatorCall = function () {
        return this.getTypedRuleContext(InvokeMediatorCallContext, 0);
    };

    MediatorCallContext.prototype.sendToMediatorCall = function () {
        return this.getTypedRuleContext(SendToMediatorCallContext, 0);
    };

    MediatorCallContext.prototype.dataMapMediatorCall = function () {
        return this.getTypedRuleContext(DataMapMediatorCallContext, 0);
    };

    MediatorCallContext.prototype.receiveFromMediatorCall = function () {
        return this.getTypedRuleContext(ReceiveFromMediatorCallContext, 0);
    };

    MediatorCallContext.prototype.customMediatorCall = function () {
        return this.getTypedRuleContext(CustomMediatorCallContext, 0);
    };

    MediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterMediatorCall(this);
        }
    };

    MediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitMediatorCall(this);
        }
    };


    WUMLParser.MediatorCallContext = MediatorCallContext;

    WUMLParser.prototype.mediatorCall = function () {

        var localctx = new MediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 106, WUMLParser.RULE_mediatorCall);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 514;
            this.match(WUMLParser.Identifier);
            this.state = 515;
            this.match(WUMLParser.ASSIGN);
            this.state = 521;
            switch (this._input.LA(1)) {
                case WUMLParser.T__25:
                    this.state = 516;
                    this.invokeMediatorCall();
                    break;
                case WUMLParser.T__26:
                    this.state = 517;
                    this.sendToMediatorCall();
                    break;
                case WUMLParser.T__27:
                    this.state = 518;
                    this.dataMapMediatorCall();
                    break;
                case WUMLParser.T__28:
                    this.state = 519;
                    this.receiveFromMediatorCall();
                    break;
                case WUMLParser.Identifier:
                    this.state = 520;
                    this.customMediatorCall();
                    break;
                default:
                    throw new antlr4.error.NoViableAltException(this);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function InvokeMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_invokeMediatorCall;
        return this;
    }

    InvokeMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    InvokeMediatorCallContext.prototype.constructor = InvokeMediatorCallContext;

    InvokeMediatorCallContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    InvokeMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterInvokeMediatorCall(this);
        }
    };

    InvokeMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitInvokeMediatorCall(this);
        }
    };


    WUMLParser.InvokeMediatorCallContext = InvokeMediatorCallContext;

    WUMLParser.prototype.invokeMediatorCall = function () {

        var localctx = new InvokeMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 108, WUMLParser.RULE_invokeMediatorCall);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 523;
            this.match(WUMLParser.T__25);
            this.state = 524;
            this.match(WUMLParser.LPAREN);
            this.state = 525;
            this.match(WUMLParser.Identifier);
            this.state = 526;
            this.match(WUMLParser.COMMA);
            this.state = 527;
            this.match(WUMLParser.Identifier);
            this.state = 528;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function SendToMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_sendToMediatorCall;
        return this;
    }

    SendToMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    SendToMediatorCallContext.prototype.constructor = SendToMediatorCallContext;

    SendToMediatorCallContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    SendToMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterSendToMediatorCall(this);
        }
    };

    SendToMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitSendToMediatorCall(this);
        }
    };


    WUMLParser.SendToMediatorCallContext = SendToMediatorCallContext;

    WUMLParser.prototype.sendToMediatorCall = function () {

        var localctx = new SendToMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 110, WUMLParser.RULE_sendToMediatorCall);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 530;
            this.match(WUMLParser.T__26);
            this.state = 531;
            this.match(WUMLParser.LPAREN);
            this.state = 532;
            this.match(WUMLParser.Identifier);
            this.state = 533;
            this.match(WUMLParser.COMMA);
            this.state = 534;
            this.match(WUMLParser.Identifier);
            this.state = 535;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function DataMapMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_dataMapMediatorCall;
        return this;
    }

    DataMapMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    DataMapMediatorCallContext.prototype.constructor = DataMapMediatorCallContext;

    DataMapMediatorCallContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    DataMapMediatorCallContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    DataMapMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterDataMapMediatorCall(this);
        }
    };

    DataMapMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitDataMapMediatorCall(this);
        }
    };


    WUMLParser.DataMapMediatorCallContext = DataMapMediatorCallContext;

    WUMLParser.prototype.dataMapMediatorCall = function () {

        var localctx = new DataMapMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 112, WUMLParser.RULE_dataMapMediatorCall);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 537;
            this.match(WUMLParser.T__27);
            this.state = 538;
            this.match(WUMLParser.LPAREN);
            this.state = 539;
            this.literal();
            this.state = 540;
            this.match(WUMLParser.COMMA);
            this.state = 541;
            this.match(WUMLParser.Identifier);
            this.state = 542;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function ReceiveFromMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_receiveFromMediatorCall;
        return this;
    }

    ReceiveFromMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ReceiveFromMediatorCallContext.prototype.constructor = ReceiveFromMediatorCallContext;

    ReceiveFromMediatorCallContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    ReceiveFromMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterReceiveFromMediatorCall(this);
        }
    };

    ReceiveFromMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitReceiveFromMediatorCall(this);
        }
    };


    WUMLParser.ReceiveFromMediatorCallContext = ReceiveFromMediatorCallContext;

    WUMLParser.prototype.receiveFromMediatorCall = function () {

        var localctx = new ReceiveFromMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 114, WUMLParser.RULE_receiveFromMediatorCall);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 544;
            this.match(WUMLParser.T__28);
            this.state = 545;
            this.match(WUMLParser.LPAREN);
            this.state = 546;
            this.match(WUMLParser.Identifier);
            this.state = 547;
            this.match(WUMLParser.COMMA);
            this.state = 548;
            this.match(WUMLParser.Identifier);
            this.state = 549;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function CustomMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_customMediatorCall;
        return this;
    }

    CustomMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    CustomMediatorCallContext.prototype.constructor = CustomMediatorCallContext;

    CustomMediatorCallContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    CustomMediatorCallContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    CustomMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterCustomMediatorCall(this);
        }
    };

    CustomMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitCustomMediatorCall(this);
        }
    };


    WUMLParser.CustomMediatorCallContext = CustomMediatorCallContext;

    WUMLParser.prototype.customMediatorCall = function () {

        var localctx = new CustomMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 116, WUMLParser.RULE_customMediatorCall);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 551;
            this.match(WUMLParser.Identifier);
            this.state = 552;
            this.match(WUMLParser.T__29);
            this.state = 553;
            this.match(WUMLParser.LPAREN);
            this.state = 554;
            this.match(WUMLParser.Identifier);
            this.state = 557;
            _la = this._input.LA(1);
            if (_la === WUMLParser.COMMA) {
                this.state = 555;
                this.match(WUMLParser.COMMA);
                this.state = 556;
                this.match(WUMLParser.StringLiteral);
            }

            this.state = 559;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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

    function LogMediatorCallContext(parser, parent, invokingState) {
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_logMediatorCall;
        return this;
    }

    LogMediatorCallContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LogMediatorCallContext.prototype.constructor = LogMediatorCallContext;

    LogMediatorCallContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    LogMediatorCallContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    LogMediatorCallContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLogMediatorCall(this);
        }
    };

    LogMediatorCallContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLogMediatorCall(this);
        }
    };


    WUMLParser.LogMediatorCallContext = LogMediatorCallContext;

    WUMLParser.prototype.logMediatorCall = function () {

        var localctx = new LogMediatorCallContext(this, this._ctx, this.state);
        this.enterRule(localctx, 118, WUMLParser.RULE_logMediatorCall);
        try {
            this.state = 570;
            this._errHandler.sync(this);
            var la_ = this._interp.adaptivePredict(this._input, 45, this._ctx);
            switch (la_) {
                case 1:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 561;
                    this.match(WUMLParser.T__30);
                    this.state = 562;
                    this.match(WUMLParser.LPAREN);
                    this.state = 563;
                    this.match(WUMLParser.Identifier);
                    this.state = 564;
                    this.match(WUMLParser.RPAREN);
                    break;

                case 2:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 565;
                    this.match(WUMLParser.T__30);
                    this.state = 566;
                    this.match(WUMLParser.LPAREN);
                    this.state = 567;
                    this.literal();
                    this.state = 568;
                    this.match(WUMLParser.RPAREN);
                    break;

            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_messageModificationStatement;
        return this;
    }

    MessageModificationStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    MessageModificationStatementContext.prototype.constructor = MessageModificationStatementContext;

    MessageModificationStatementContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    MessageModificationStatementContext.prototype.messagePropertyName = function () {
        return this.getTypedRuleContext(MessagePropertyNameContext, 0);
    };

    MessageModificationStatementContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    MessageModificationStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterMessageModificationStatement(this);
        }
    };

    MessageModificationStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitMessageModificationStatement(this);
        }
    };


    WUMLParser.MessageModificationStatementContext = MessageModificationStatementContext;

    WUMLParser.prototype.messageModificationStatement = function () {

        var localctx = new MessageModificationStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 120, WUMLParser.RULE_messageModificationStatement);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 572;
            this.match(WUMLParser.Identifier);
            this.state = 573;
            this.match(WUMLParser.DOT);
            this.state = 574;
            this.match(WUMLParser.Identifier);
            this.state = 575;
            this.match(WUMLParser.LPAREN);
            this.state = 576;
            this.messagePropertyName();
            this.state = 577;
            this.match(WUMLParser.COMMA);
            this.state = 578;
            this.literal();
            this.state = 579;
            this.match(WUMLParser.RPAREN);
            this.state = 580;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_returnStatement;
        return this;
    }

    ReturnStatementContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ReturnStatementContext.prototype.constructor = ReturnStatementContext;

    ReturnStatementContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    ReturnStatementContext.prototype.invokeMediatorCall = function () {
        return this.getTypedRuleContext(InvokeMediatorCallContext, 0);
    };

    ReturnStatementContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterReturnStatement(this);
        }
    };

    ReturnStatementContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitReturnStatement(this);
        }
    };


    WUMLParser.ReturnStatementContext = ReturnStatementContext;

    WUMLParser.prototype.returnStatement = function () {

        var localctx = new ReturnStatementContext(this, this._ctx, this.state);
        this.enterRule(localctx, 122, WUMLParser.RULE_returnStatement);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 582;
            this.match(WUMLParser.T__31);
            this.state = 585;
            switch (this._input.LA(1)) {
                case WUMLParser.Identifier:
                    this.state = 583;
                    this.match(WUMLParser.Identifier);
                    break;
                case WUMLParser.T__25:
                    this.state = 584;
                    this.invokeMediatorCall();
                    break;
                case WUMLParser.SEMI:
                    break;
                default:
                    throw new antlr4.error.NoViableAltException(this);
            }
            this.state = 587;
            this.match(WUMLParser.SEMI);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_parExpression;
        return this;
    }

    ParExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ParExpressionContext.prototype.constructor = ParExpressionContext;

    ParExpressionContext.prototype.expression = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTypedRuleContexts(ExpressionContext);
        } else {
            return this.getTypedRuleContext(ExpressionContext, i);
        }
    };

    ParExpressionContext.prototype.GT = function () {
        return this.getToken(WUMLParser.GT, 0);
    };

    ParExpressionContext.prototype.LT = function () {
        return this.getToken(WUMLParser.LT, 0);
    };

    ParExpressionContext.prototype.EQUAL = function () {
        return this.getToken(WUMLParser.EQUAL, 0);
    };

    ParExpressionContext.prototype.LE = function () {
        return this.getToken(WUMLParser.LE, 0);
    };

    ParExpressionContext.prototype.GE = function () {
        return this.getToken(WUMLParser.GE, 0);
    };

    ParExpressionContext.prototype.NOTEQUAL = function () {
        return this.getToken(WUMLParser.NOTEQUAL, 0);
    };

    ParExpressionContext.prototype.AND = function () {
        return this.getToken(WUMLParser.AND, 0);
    };

    ParExpressionContext.prototype.OR = function () {
        return this.getToken(WUMLParser.OR, 0);
    };

    ParExpressionContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterParExpression(this);
        }
    };

    ParExpressionContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitParExpression(this);
        }
    };


    WUMLParser.ParExpressionContext = ParExpressionContext;

    WUMLParser.prototype.parExpression = function () {

        var localctx = new ParExpressionContext(this, this._ctx, this.state);
        this.enterRule(localctx, 124, WUMLParser.RULE_parExpression);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 589;
            this.match(WUMLParser.LPAREN);
            this.state = 590;
            this.expression();
            this.state = 593;
            _la = this._input.LA(1);
            if (((((_la - 104)) & ~0x1f) == 0 && ((1 << (_la - 104)) & ((1 << (WUMLParser.GT - 104)) | (1 << (WUMLParser.LT - 104)) | (1 << (WUMLParser.EQUAL - 104)) | (1 << (WUMLParser.LE - 104)) | (1 << (WUMLParser.GE - 104)) | (1 << (WUMLParser.NOTEQUAL - 104)) | (1 << (WUMLParser.AND - 104)) | (1 << (WUMLParser.OR - 104)))) !== 0)) {
                this.state = 591;
                _la = this._input.LA(1);
                if (!(((((_la - 104)) & ~0x1f) == 0 && ((1 << (_la - 104)) & ((1 << (WUMLParser.GT - 104)) | (1 << (WUMLParser.LT - 104)) | (1 << (WUMLParser.EQUAL - 104)) | (1 << (WUMLParser.LE - 104)) | (1 << (WUMLParser.GE - 104)) | (1 << (WUMLParser.NOTEQUAL - 104)) | (1 << (WUMLParser.AND - 104)) | (1 << (WUMLParser.OR - 104)))) !== 0))) {
                    this._errHandler.recoverInline(this);
                }
                else {
                    this.consume();
                }
                this.state = 592;
                this.expression();
            }

            this.state = 595;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_expression;
        return this;
    }

    ExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ExpressionContext.prototype.constructor = ExpressionContext;

    ExpressionContext.prototype.evalExpression = function () {
        return this.getTypedRuleContext(EvalExpressionContext, 0);
    };

    ExpressionContext.prototype.literal = function () {
        return this.getTypedRuleContext(LiteralContext, 0);
    };

    ExpressionContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterExpression(this);
        }
    };

    ExpressionContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitExpression(this);
        }
    };


    WUMLParser.ExpressionContext = ExpressionContext;

    WUMLParser.prototype.expression = function () {

        var localctx = new ExpressionContext(this, this._ctx, this.state);
        this.enterRule(localctx, 126, WUMLParser.RULE_expression);
        try {
            this.state = 599;
            switch (this._input.LA(1)) {
                case WUMLParser.T__32:
                    this.enterOuterAlt(localctx, 1);
                    this.state = 597;
                    this.evalExpression();
                    break;
                case WUMLParser.IntegerLiteral:
                case WUMLParser.FloatingPointLiteral:
                case WUMLParser.BooleanLiteral:
                case WUMLParser.CharacterLiteral:
                case WUMLParser.StringLiteral:
                case WUMLParser.NullLiteral:
                    this.enterOuterAlt(localctx, 2);
                    this.state = 598;
                    this.literal();
                    break;
                default:
                    throw new antlr4.error.NoViableAltException(this);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_evalExpression;
        return this;
    }

    EvalExpressionContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    EvalExpressionContext.prototype.constructor = EvalExpressionContext;

    EvalExpressionContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    EvalExpressionContext.prototype.Identifier = function () {
        return this.getToken(WUMLParser.Identifier, 0);
    };

    EvalExpressionContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterEvalExpression(this);
        }
    };

    EvalExpressionContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitEvalExpression(this);
        }
    };


    WUMLParser.EvalExpressionContext = EvalExpressionContext;

    WUMLParser.prototype.evalExpression = function () {

        var localctx = new EvalExpressionContext(this, this._ctx, this.state);
        this.enterRule(localctx, 128, WUMLParser.RULE_evalExpression);
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 601;
            this.match(WUMLParser.T__32);
            this.state = 602;
            this.match(WUMLParser.LPAREN);
            this.state = 603;
            this.match(WUMLParser.StringLiteral);
            this.state = 604;
            this.match(WUMLParser.COMMA);
            this.state = 605;
            this.match(WUMLParser.Identifier);
            this.state = 606;
            this.match(WUMLParser.RPAREN);
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_literal;
        return this;
    }

    LiteralContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    LiteralContext.prototype.constructor = LiteralContext;

    LiteralContext.prototype.IntegerLiteral = function () {
        return this.getToken(WUMLParser.IntegerLiteral, 0);
    };

    LiteralContext.prototype.FloatingPointLiteral = function () {
        return this.getToken(WUMLParser.FloatingPointLiteral, 0);
    };

    LiteralContext.prototype.CharacterLiteral = function () {
        return this.getToken(WUMLParser.CharacterLiteral, 0);
    };

    LiteralContext.prototype.StringLiteral = function () {
        return this.getToken(WUMLParser.StringLiteral, 0);
    };

    LiteralContext.prototype.BooleanLiteral = function () {
        return this.getToken(WUMLParser.BooleanLiteral, 0);
    };

    LiteralContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterLiteral(this);
        }
    };

    LiteralContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitLiteral(this);
        }
    };


    WUMLParser.LiteralContext = LiteralContext;

    WUMLParser.prototype.literal = function () {

        var localctx = new LiteralContext(this, this._ctx, this.state);
        this.enterRule(localctx, 130, WUMLParser.RULE_literal);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 608;
            _la = this._input.LA(1);
            if (!(((((_la - 88)) & ~0x1f) == 0 && ((1 << (_la - 88)) & ((1 << (WUMLParser.IntegerLiteral - 88)) | (1 << (WUMLParser.FloatingPointLiteral - 88)) | (1 << (WUMLParser.BooleanLiteral - 88)) | (1 << (WUMLParser.CharacterLiteral - 88)) | (1 << (WUMLParser.StringLiteral - 88)) | (1 << (WUMLParser.NullLiteral - 88)))) !== 0))) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_mediaType;
        return this;
    }

    MediaTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    MediaTypeContext.prototype.constructor = MediaTypeContext;


    MediaTypeContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterMediaType(this);
        }
    };

    MediaTypeContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitMediaType(this);
        }
    };


    WUMLParser.MediaTypeContext = MediaTypeContext;

    WUMLParser.prototype.mediaType = function () {

        var localctx = new MediaTypeContext(this, this._ctx, this.state);
        this.enterRule(localctx, 132, WUMLParser.RULE_mediaType);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 610;
            _la = this._input.LA(1);
            if (!(_la === WUMLParser.T__33 || _la === WUMLParser.T__34)) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_type;
        return this;
    }

    TypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    TypeContext.prototype.constructor = TypeContext;


    TypeContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterType(this);
        }
    };

    TypeContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitType(this);
        }
    };


    WUMLParser.TypeContext = TypeContext;

    WUMLParser.prototype.type = function () {

        var localctx = new TypeContext(this, this._ctx, this.state);
        this.enterRule(localctx, 134, WUMLParser.RULE_type);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 612;
            _la = this._input.LA(1);
            if (!(((((_la - 39)) & ~0x1f) == 0 && ((1 << (_la - 39)) & ((1 << (WUMLParser.BOOLEAN - 39)) | (1 << (WUMLParser.BYTE - 39)) | (1 << (WUMLParser.CHAR - 39)) | (1 << (WUMLParser.DOUBLE - 39)) | (1 << (WUMLParser.FLOAT - 39)) | (1 << (WUMLParser.INT - 39)) | (1 << (WUMLParser.LONG - 39)))) !== 0) || _la === WUMLParser.SHORT)) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_classType;
        return this;
    }

    ClassTypeContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    ClassTypeContext.prototype.constructor = ClassTypeContext;


    ClassTypeContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterClassType(this);
        }
    };

    ClassTypeContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitClassType(this);
        }
    };


    WUMLParser.ClassTypeContext = ClassTypeContext;

    WUMLParser.prototype.classType = function () {

        var localctx = new ClassTypeContext(this, this._ctx, this.state);
        this.enterRule(localctx, 136, WUMLParser.RULE_classType);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 614;
            _la = this._input.LA(1);
            if (!(_la === WUMLParser.T__20 || _la === WUMLParser.T__35)) {
                this._errHandler.recoverInline(this);
            }
            else {
                this.consume();
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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
        if (parent === undefined) {
            parent = null;
        }
        if (invokingState === undefined || invokingState === null) {
            invokingState = -1;
        }
        antlr4.ParserRuleContext.call(this, parent, invokingState);
        this.parser = parser;
        this.ruleIndex = WUMLParser.RULE_messagePropertyName;
        return this;
    }

    MessagePropertyNameContext.prototype = Object.create(antlr4.ParserRuleContext.prototype);
    MessagePropertyNameContext.prototype.constructor = MessagePropertyNameContext;

    MessagePropertyNameContext.prototype.Identifier = function (i) {
        if (i === undefined) {
            i = null;
        }
        if (i === null) {
            return this.getTokens(WUMLParser.Identifier);
        } else {
            return this.getToken(WUMLParser.Identifier, i);
        }
    };


    MessagePropertyNameContext.prototype.enterRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.enterMessagePropertyName(this);
        }
    };

    MessagePropertyNameContext.prototype.exitRule = function (listener) {
        if (listener instanceof WUMLListener) {
            listener.exitMessagePropertyName(this);
        }
    };


    WUMLParser.MessagePropertyNameContext = MessagePropertyNameContext;

    WUMLParser.prototype.messagePropertyName = function () {

        var localctx = new MessagePropertyNameContext(this, this._ctx, this.state);
        this.enterRule(localctx, 138, WUMLParser.RULE_messagePropertyName);
        var _la = 0; // Token type
        try {
            this.enterOuterAlt(localctx, 1);
            this.state = 616;
            this.match(WUMLParser.Identifier);
            this.state = 621;
            this._errHandler.sync(this);
            _la = this._input.LA(1);
            while (_la === WUMLParser.DOT) {
                this.state = 617;
                this.match(WUMLParser.DOT);
                this.state = 618;
                this.match(WUMLParser.Identifier);
                this.state = 623;
                this._errHandler.sync(this);
                _la = this._input.LA(1);
            }
        } catch (re) {
            if (re instanceof antlr4.error.RecognitionException) {
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


    module.exports.WUMLParser = WUMLParser;
});
