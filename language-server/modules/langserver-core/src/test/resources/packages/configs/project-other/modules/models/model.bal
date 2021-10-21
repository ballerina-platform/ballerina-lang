const LOWER = "abcdefghijklmnopqrstuvwxyz";
const UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const DIGIT = "0123456789";
const string ALPHA = LOWER + UPPER;
const string IDENT = ALPHA + DIGIT + "_";

type Char string;

type StringIterator object {
    public isolated function next() returns record {|
        Char value;
    |}?;
};

final readonly & map<Char> ESCAPES = {
    "\\": "\\",
    "\"": "\"",
    "n": "\n",
    "r": "\r",
    "t": "\t"
};

public type Position readonly & record {|
    // 1-based
    int lineNumber;
    // 0-based index (in code points) in the line
    int indexInLine;
|};

public type ParseErrorDetail record {
    Position pos;
};

public type ParseError distinct error<ParseErrorDetail>;

class Tokenizer {
    Token? cur = ();
    // The index in `str` of the first character of `cur`
    private int startIndex = 0;
    // Index of character starting line on which startPos occurs
    private int lineStartIndex = 0;
    // Line number of line starting at lineStartIndex
    private int lineNumber = 1;
    private final string str;

    private final StringIterator iter;
    private Char? ungot = ();
    // Number of characters returned by `iter`
    private int nextCount = 0;


    function init(string str) {
        self.iter = str.iterator();
        self.str = str;
    }

    // Moves to next token.record
    // Current token is () if there is no next token
    function advance() returns ParseError? {
        self.cur = check self.next();
    }

    function current() returns Token? {
        return self.cur;
    }

    function currentPos() returns Position {
        return {
            lineNumber: self.lineNumber,
            indexInLine: self.startIndex - self.lineStartIndex
        };
    }
}

enum Language {
    // An enum member can explicitly specify an associated expression.
    EN = "English"
}

final var expected = {
   proper_subtype: [true, false],
   equivalent: [true, true],
   incomparable: [false, false]
};
