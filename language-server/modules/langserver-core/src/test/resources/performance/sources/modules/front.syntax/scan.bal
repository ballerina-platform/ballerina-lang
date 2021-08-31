// Bugs
// Spec bug float suffix after hex makes no sense
// _ can be followed by identifier

import wso2/nballerina.err;

//import ballerina/io;

// This should be byte
// But multiple runtime/compile-time JBUGs
type FragCode int;

type ScannedLine readonly & record {|
    // In the future, we will need some more fields here.
    // Does it contain code points > 0x7F?
    // If not, then code point index = UTF-16 index = byte index.
    // Does it contain code points > 0xFFFF?
    // If not, UTF-16 index is the same as code point index.
    // What is the "brace delta" i.e. difference between opening brace level and closing brace level?
    // (A line with one left brace would have a brace delta of 1.)
    // (This is counting brace tokens not brace characters.)
    // What is minimum brace delta in the line?
    // Consider a line `} {`? Brace delta is 0, but minimum delta is -1.
    // We will also need stuff for backquotes.
    // This scan is only valid if there are no open backquotes at the beginning of the line.
    // What is the number of open backquotes at the end of the line?
    // Does it contain backquotes at all?
    // Does it contain any dollar signs?
    // If no backquotes and no dollars, we can determine how it will scan when it's within a backquote string.
    FragCode[] fragCodes;
    string[] fragments;
|};

type Scanned record {|
    FragCode[] fragCodes;
    int[] endIndex;
|};

// These can all be associated with different strings
const FRAG_WHITESPACE = 0x00;
const FRAG_COMMENT = 0x01;
const FRAG_INVALID = 0x02;
const FRAG_IDENTIFIER = 0x03;
const FRAG_DECIMAL_NUMBER = 0x04;
const FRAG_HEX_NUMBER = 0x05; // 0xDEADBEEF
const FRAG_DECIMAL_FP_NUMBER = 0x06; // with `.` or exponent
const FRAG_DECIMAL_FP_NUMBER_F = 0x07; // with F or f suffix

const FRAG_STRING_CHARS = 0x08;
const FRAG_STRING_CONTROL_ESCAPE = 0x09; // \r \t \n
const FRAG_STRING_CHAR_ESCAPE = 0x0A; // \" \\
const FRAG_STRING_NUMERIC_ESCAPE = 0x0B; // \u{NNN}

const VAR_FRAG_MAX = 0x0B;

// each fragment codes >= always comes from the same string
const FRAG_FIXED = 0x1D; // >= this corre

// Comes from greater than
const FRAG_GREATER_THAN = 0x1D; 
// Comes from a double quote that starts a string
const FRAG_STRING_OPEN = 0x1E;
// Comes from a double quote that ends a string
const FRAG_STRING_CLOSE = 0x1F;

// fragment codes >= 0x21 correspond to fixed tokens

const FRAG_FIXED_TOKEN = 0x21;

// Multi char delims mapped into range used by upper case
const FRAG_LEFT_CURLY_VBAR = 0x41;
const FRAG_VBAR_RIGHT_CURLY = 0x42;
const FRAG_DOT_DOT_DOT = 0x43;
const FRAG_DOT_DOT_LESS_THAN = 0x44;
const FRAG_EQUAL_EQUAL = 0x45;
const FRAG_NOT_EQUAL = 0x46;
const FRAG_EQUAL_EQUAL_EQUAL = 0x47;
const FRAG_NOT_EQUAL_EQUAL = 0x48;
const FRAG_LESS_THAN_EQUAL = 0x49;
const FRAG_GREATER_THAN_EQUAL = 0x4A;
const FRAG_LESS_THAN_LESS_THAN = 0x4B;
const FRAG_EQUAL_GREATER_THAN = 0x4C;
const FRAG_PLUS_EQUAL = 0x4D;
const FRAG_MINUS_EQUAL = 0x4E;
const FRAG_SLASH_EQUAL = 0x4F;
const FRAG_ASTERISK_EQUAL = 0x50;
const FRAG_AMPERSAND_EQUAL = 0x51;
const FRAG_VBAR_EQUAL = 0x52;
const FRAG_CIRCUMFLEX_EQUAL = 0x53;
const FRAG_LESS_THAN_LESS_THAN_EQUAL = 0x54;
const FRAG_GREATER_THAN_GREATER_THAN_EQUAL = 0x55;
const FRAG_GREATER_THAN_GREATER_THAN_GREATER_THAN_EQUAL = 0x56;
 
const FRAG_KEYWORD = 0x80;

final readonly & Keyword[] keywords = [
    "any",
    "boolean",
    "break",
    "byte",
    "const",
    "continue",
    "decimal",
    "else",
    "error",
    "false",
    "final",
    "float",
    "foreach",
    "function",
    "handle",
    "if",
    "import",
    "in",
    "int",
    "is",
    "json",
    "map",
    "match",
    "never",
    "null",
    "public",
    "readonly",
    "record",
    "return",
    "returns",
    "string",
    "true",
    "type",
    "typedesc",
    "while",
    "xml"
];

// This maps a frag code to a string
// JBUG if this comes before keywords it gets a NPE at module init time
final readonly & FixedToken?[] fragTokens = createFragTokens();


function createFragTokens() returns readonly & FixedToken?[] {
    FixedToken?[] ft = [];
    foreach int i in 0 ..< keywords.length() {
        ft[FRAG_KEYWORD + i] = keywords[i];
    }
    // JBUG int casts needed
    // Use toFixedToken to avoid method too large error
    ft[<int>FRAG_LEFT_CURLY_VBAR] = toFixedToken("{|");
    ft[<int>FRAG_VBAR_RIGHT_CURLY] = toFixedToken("|}");
    ft[<int>FRAG_DOT_DOT_DOT] = toFixedToken("...");
    ft[<int>FRAG_DOT_DOT_LESS_THAN] = toFixedToken("..<");
    ft[<int>FRAG_EQUAL_EQUAL] = toFixedToken("==");
    ft[<int>FRAG_NOT_EQUAL] = toFixedToken("!=");
    ft[<int>FRAG_EQUAL_EQUAL_EQUAL] = toFixedToken("===");
    ft[<int>FRAG_NOT_EQUAL_EQUAL] = toFixedToken("!==");
    ft[<int>FRAG_LESS_THAN_EQUAL] = toFixedToken("<="); 
    ft[<int>FRAG_GREATER_THAN_EQUAL] = toFixedToken(">=");
    ft[<int>FRAG_LESS_THAN_LESS_THAN] = toFixedToken("<<");
    ft[<int>FRAG_EQUAL_GREATER_THAN] = toFixedToken("=>");
    ft[<int>FRAG_PLUS_EQUAL] = toFixedToken("+=");
    ft[<int>FRAG_MINUS_EQUAL] = toFixedToken("-=");
    ft[<int>FRAG_ASTERISK_EQUAL] = toFixedToken("*=");
    ft[<int>FRAG_SLASH_EQUAL] = toFixedToken("/=");
    ft[<int>FRAG_AMPERSAND_EQUAL] = toFixedToken("&=");
    ft[<int>FRAG_VBAR_EQUAL] = toFixedToken("|=");
    ft[<int>FRAG_CIRCUMFLEX_EQUAL] = toFixedToken("^=");
    ft[<int>FRAG_LESS_THAN_LESS_THAN_EQUAL] = toFixedToken("<<=");
    ft[<int>FRAG_GREATER_THAN_GREATER_THAN_EQUAL] = toFixedToken(">>=");
    ft[<int>FRAG_GREATER_THAN_GREATER_THAN_GREATER_THAN_EQUAL] = toFixedToken(">>>=");
    // JBUG error if hex used for 32 and 128
    foreach int cp in 32 ..< 128 {
        string s = checkpanic string:fromCodePointInt(cp);
        if s is SingleCharDelim {
            ft[cp] = s;
        }
    }
    return ft.cloneReadOnly();
}

function unicodeEscapeValue(string fragment) returns string|error {
    string hexDigits = fragment.substring(3, fragment.length() - 1);
    int codePoint = check int:fromHexString(hexDigits);
    // JBUG #31778 shouldn't need this check, fromCodePointInt should return an error
    if 0xD800 <= codePoint && codePoint <= 0xDFFF {
        return error("invalid codepoint");
    }
    string:Char ch = check string:fromCodePointInt(codePoint);
    return ch;
}

function scanLine(string line) returns ScannedLine {
    int[] codePoints = line.toCodePointInts();
    FragCode[] fragCodes = [];
    int[] endIndex = [];
    //io:println("nCodePoints: ", codePoints.length());
    scanNormal(codePoints, 0, { fragCodes, endIndex });
    //io:println("nFragCodes: ", fragCodes.length());
    //io:println(fragCodes);
    //io:println("nEndIndex: ", endIndex.length());

    string[] fragments;
    if endIndex.length() > 0 {
        endIndex.setLength(endIndex.length() - 1);
        fragments = splitString(line, endIndex);
    }
    else {
        fragments = [];
    }
    int nVarFragments = 0;
    foreach int i in 0 ..< fragCodes.length() {
        if fragCodes[i] <= VAR_FRAG_MAX {
            fragments[nVarFragments] = fragments[i];
            nVarFragments += 1;
        }
    }
    //io:println("nVarFragments: ", nVarFragments);

    fragments.setLength(nVarFragments);
    return { fragCodes: fragCodes.cloneReadOnly(), fragments: fragments.cloneReadOnly() };
}

// Might want to do this in native code
function splitString(string str, int[] endIndex) returns string[] {
    string[] fragments = [];
    int startIndex = 0;
    foreach int index in endIndex {
        fragments.push(str.substring(startIndex, index));
        startIndex = index;
    }
    fragments.push(str.substring(startIndex, str.length()));
    return fragments;
}

function splitIntoLines(string str) returns string[] {
    string[] lines = [];
    int i = 0;
    int lineStartIndex = 0;
    int? cr = ();
    foreach var ch in str {
        if ch == "\n" {
            if cr == () || cr + 1 != i {
                lines.push(str.substring(lineStartIndex, i));
            }
            lineStartIndex = i + 1;
        }
        else if ch == "\r"  {
            lines.push(str.substring(lineStartIndex, i));
            cr = i;
            lineStartIndex = i + 1;           
        }
        i += 1;
    }
    if lineStartIndex < str.length() {
        lines.push(str.substring(lineStartIndex));
    }
    return lines;
}

function scanNormal(int[] codePoints, int startIndex, Scanned result) {
    final int len = codePoints.length();
    int i = startIndex;
    while i < len {
        int cp = codePoints[i];
        i = i + 1;
        match cp {
            CP_LF|CP_CR => {
                panic err:impossible("line terminators must not occur in input to scanner");
            }
            CP_SPACE|CP_TAB => {
                while i < len && (codePoints[i] == CP_SPACE || codePoints[i] == CP_TAB) {
                    i += 1;
                }
                endFragment(FRAG_WHITESPACE, i, result);
            }
            CP_SLASH => {
                if i == len {
                    endFragment(CP_SLASH, i, result);
                    continue;
                }
                cp = codePoints[i];
                if cp == CP_EQUAL {
                    i += 1;
                   endFragment(FRAG_SLASH_EQUAL, i, result);
                   continue; 
                }
                if cp != CP_SLASH {
                    endFragment(CP_SLASH, i, result);
                    continue;
                }
                endFragment(FRAG_COMMENT, len, result);
                break;
            }
            CP_LEFT_CURLY => {
                if i < len && codePoints[i] == CP_VBAR {
                    i += 1;
                    endFragment(FRAG_LEFT_CURLY_VBAR, i, result);
                    continue;
                }
                endFragment(CP_LEFT_CURLY, i, result);
            }
            CP_VBAR => {
                if i < len && codePoints[i] == CP_RIGHT_CURLY {
                    i += 1;
                    endFragment(FRAG_VBAR_RIGHT_CURLY, i, result);
                    continue;
                }
                if i < len && codePoints[i] == CP_EQUAL {
                    i += 1;
                   endFragment(FRAG_VBAR_EQUAL, i, result);
                   continue; 
                }
                endFragment(CP_VBAR, i, result);
            }
            CP_DOT => {
                if i < len {
                    int cp2 = codePoints[i];
        
                    if cp2 == CP_DOT {
                        if i + 1 < len && codePoints[i + 1] == CP_DOT {
                            i += 2;
                            endFragment(FRAG_DOT_DOT_DOT, i, result);
                            continue;
                        }
                        if i + 1 < len && codePoints[i + 1] == CP_LESS_THAN {
                            i += 2;
                            endFragment(FRAG_DOT_DOT_LESS_THAN, i, result);
                            continue;
                        }
                    }
                    else if isCodePointAsciiDigit(cp2) {
                        int? endIndex = scanFractionExponent(codePoints, i);
                        if endIndex != () {
                            i = endDecimal(FRAG_DECIMAL_FP_NUMBER, codePoints, endIndex, result);
                            continue;
                        }
                    }
                }
                endFragment(CP_DOT, i, result);
            }
            CP_EQUAL => {
                if i < len {
                    int cp2 = codePoints[i];
                    if cp2 == CP_EQUAL {
                        if i + 1 < len && codePoints[i + 1] == CP_EQUAL {
                            i += 2;
                            endFragment(FRAG_EQUAL_EQUAL_EQUAL, i, result);
                            continue;
                        }
                        i += 1;
                        endFragment(FRAG_EQUAL_EQUAL, i, result);
                        continue;
                    }
                    if cp2 == CP_GREATER_THAN {
                        i += 1;
                        endFragment(FRAG_EQUAL_GREATER_THAN, i, result);
                        continue;
                    }
                }
                endFragment(CP_EQUAL, i, result);
            }
            CP_EXCLAM => {
                if i < len {
                    int cp2 = codePoints[i];
                    if cp2 == CP_EQUAL {
                        if i + 1 < len && codePoints[i + 1] == CP_EQUAL {
                            i += 2;
                            endFragment(FRAG_NOT_EQUAL_EQUAL, i, result);
                            continue;
                        }
                        i += 1;
                        endFragment(FRAG_NOT_EQUAL, i, result);
                        continue;
                    }
                }
                endFragment(CP_EXCLAM, i, result);
            }
            CP_LESS_THAN => {
                if i < len {
                    int cp2 = codePoints[i];
                    if cp2 == CP_LESS_THAN {
                        i += 1;
                        if codePoints[i] == CP_EQUAL {
                            i+=1;
                            endFragment(FRAG_LESS_THAN_LESS_THAN_EQUAL, i, result);
                            continue;
                        }
                        endFragment(FRAG_LESS_THAN_LESS_THAN, i, result);
                        continue;
                    }
                    if cp2 == CP_EQUAL {
                        i += 1;
                        endFragment(FRAG_LESS_THAN_EQUAL, i, result);
                        continue;
                    }
                }
                endFragment(CP_LESS_THAN, i, result);
            }
            CP_GREATER_THAN => {
                if i < len && codePoints[i] == CP_EQUAL {
                    i += 1;
                    endFragment(FRAG_GREATER_THAN_EQUAL, i, result);
                    continue;
                }
                if i < len && codePoints[i] == CP_GREATER_THAN {
                    if i+2 < len && codePoints[i+1] == CP_GREATER_THAN && codePoints[i+2] == CP_EQUAL{
                        i += 3;
                        endFragment(FRAG_GREATER_THAN_GREATER_THAN_GREATER_THAN_EQUAL, i, result);
                        continue;
                    }
                    else if i+1 < len && codePoints[i+1] == CP_EQUAL{
                        i += 2;
                        endFragment(FRAG_GREATER_THAN_GREATER_THAN_EQUAL, i, result);
                        continue;
                    }
                }
                // Tokenization of multiple `>`s depends on lexical mode
                // so do it later
                endFragment(FRAG_GREATER_THAN, i, result);
            }
            CP_DIGIT_0 => {
                if i < len {
                    int cp2 = codePoints[i];
                    if cp2 == CP_UPPER_X || cp2 == CP_LOWER_X {
                        int? endIndex = scanHexDigits(codePoints, i + 1);
                        if endIndex != () {
                            endFragment(FRAG_HEX_NUMBER, endIndex, result);
                            i = endIndex;
                            continue;
                        }
                    }
                    else if isCodePointAsciiDigit(cp2) {
                        // 01 is not a valid token
                        endFragment(FRAG_DECIMAL_NUMBER, i, result);
                        continue;
                    }
                }
                i = scanDecimal(codePoints, i, result);   
            }
            CP_DIGIT_1
            |CP_DIGIT_2
            |CP_DIGIT_3
            |CP_DIGIT_4
            |CP_DIGIT_5
            |CP_DIGIT_6
            |CP_DIGIT_7
            |CP_DIGIT_8
            |CP_DIGIT_9 => {
                i = scanDecimal(codePoints, i, result);
            }
            CP_PERCENT
            |CP_LEFT_PAREN 
            |CP_RIGHT_PAREN
            |CP_COMMA
            |CP_COLON
            |CP_SEMICOLON 
            |CP_QUESTION
            |CP_LEFT_SQUARE
            |CP_RIGHT_SQUARE
            |CP_RIGHT_CURLY
            |CP_TILDE => {
                // JBUG when FragCode is byte, error without cast
                endFragment(<FragCode>cp, i, result);
            }
            CP_CIRCUMFLEX => {
                i = endFragmentCompoundAssign(codePoints, i, CP_CIRCUMFLEX, FRAG_CIRCUMFLEX_EQUAL, result);
                
            }
            CP_PLUS => {
                i = endFragmentCompoundAssign(codePoints, i, CP_PLUS, FRAG_PLUS_EQUAL, result);
            }
            CP_MINUS => {
                i = endFragmentCompoundAssign(codePoints, i, CP_MINUS, FRAG_MINUS_EQUAL, result);
            }
            CP_ASTERISK => {
                i = endFragmentCompoundAssign(codePoints, i, CP_ASTERISK, FRAG_ASTERISK_EQUAL, result);
            }
            CP_AMPERSAND => {
                i = endFragmentCompoundAssign(codePoints, i, CP_AMPERSAND, FRAG_AMPERSAND_EQUAL, result);
            }
            CP_DOUBLE_QUOTE => {
                i = scanString(codePoints, i, result);
            }
            CP_LOWER_A
            |CP_LOWER_B
            |CP_LOWER_C
            |CP_LOWER_D
            |CP_LOWER_E
            |CP_LOWER_F
            |CP_LOWER_G
            |CP_LOWER_H
            |CP_LOWER_I
            |CP_LOWER_J
            |CP_LOWER_K
            |CP_LOWER_L
            |CP_LOWER_M
            |CP_LOWER_N
            |CP_LOWER_O
            |CP_LOWER_P
            |CP_LOWER_Q
            |CP_LOWER_R
            |CP_LOWER_S
            |CP_LOWER_T
            |CP_LOWER_U
            |CP_LOWER_V
            |CP_LOWER_W
            |CP_LOWER_X
            |CP_LOWER_Y
            |CP_LOWER_Z => {
                while true {
                    if i >= len {
                        endIdentifierOrKeyword(codePoints, i, result);
                        return;
                    }
                    cp = codePoints[i];
                    if cp < CP_LOWER_A || cp > CP_LOWER_Z {
                        break;
                    }
                    i += 1;
                }
                // cp is code point that is not a lower case ASCII letter
                if isCodePointIdentifierFollowing(cp) {
                    i = scanIdentifier(codePoints, i + 1, result);
                }
                else {
                    endIdentifierOrKeyword(codePoints, i, result);
                }
            }
            CP_UPPER_A
            |CP_UPPER_B
            |CP_UPPER_C
            |CP_UPPER_D
            |CP_UPPER_E
            |CP_UPPER_F
            |CP_UPPER_G
            |CP_UPPER_H
            |CP_UPPER_I
            |CP_UPPER_J
            |CP_UPPER_K
            |CP_UPPER_L
            |CP_UPPER_M
            |CP_UPPER_N
            |CP_UPPER_O
            |CP_UPPER_P
            |CP_UPPER_Q
            |CP_UPPER_R
            |CP_UPPER_S
            |CP_UPPER_T
            |CP_UPPER_U
            |CP_UPPER_V
            |CP_UPPER_W
            |CP_UPPER_X
            |CP_UPPER_Y
            |CP_UPPER_Z => {
                i = scanIdentifier(codePoints, i, result);
            }
            CP_UNDERSCORE => {
                if i < len && isCodePointIdentifierFollowing(codePoints[i]) {
                    i = scanIdentifier(codePoints, i + 1, result);
                }
                else {
                    endFragment(CP_UNDERSCORE, i, result);
                }
            }
            _ => {
                if isCodePointUnicodeIdentifier(cp) {
                    i = scanIdentifier(codePoints, i, result);
                }
                else {
                    endFragment(FRAG_INVALID, i, result);
                }
                
            }
        }

    }
}

function scanString(int[] codePoints, int startIndex, Scanned result) returns int {
    final int startResultLength = result.fragCodes.length();
    endFragment(FRAG_STRING_OPEN, startIndex, result);
    int i = startIndex;
    int len = codePoints.length();
    while i < len {
        int cp = codePoints[i];
        i += 1;
        if cp == CP_DOUBLE_QUOTE {
            endFragment(FRAG_STRING_CLOSE, i, result);
            return i;
        }
        else if cp == CP_BACKSLASH {
            if i < len {
                int cp2 = codePoints[i];
                if cp2 == CP_LOWER_R || cp2 == CP_LOWER_N || cp2 == CP_LOWER_T {
                    i += 1;
                    endFragment(FRAG_STRING_CONTROL_ESCAPE, i, result);
                    continue;
                }
                if cp2 == CP_BACKSLASH || cp2 == CP_DOUBLE_QUOTE {
                    i += 1;
                    endFragment(FRAG_STRING_CHAR_ESCAPE, i, result);
                    continue;
                }
                if cp2 == CP_LOWER_U {
                    int? endIndex = scanNumericEscape(codePoints, i + 1);
                    if endIndex != () {
                        endFragment(FRAG_STRING_NUMERIC_ESCAPE, endIndex, result);
                        i = endIndex;
                        continue;
                    }
                }
            }
            // mark the backslash as invalid
            endFragment(FRAG_INVALID, i, result);
        }
        else {
            endFragmentMerge(FRAG_STRING_CHARS, i, result);
        }
    }
    // no closing quote
    // mark the opening quote as invalid
    result.fragCodes.setLength(startResultLength);
    result.endIndex.setLength(startResultLength);
    endFragment(FRAG_INVALID, startIndex, result);
    return startIndex;
}

function scanNumericEscape(int[] codePoints, int startIndex) returns int? {
    int len = codePoints.length();
    int i = startIndex;
    if i >= len {
        return ();
    }
    if codePoints[i] != CP_LEFT_CURLY {
        return ();
    }
    int? endIndex = scanHexDigits(codePoints, i + 1);
    if endIndex == () || endIndex >= len || codePoints[endIndex] != CP_RIGHT_CURLY {
        return ();
    }
    else {
        return endIndex + 1;
    }
}

// Scan one or more hex digits
function scanHexDigits(int[] codePoints, int startIndex) returns int? {
    int len = codePoints.length();
    int i = startIndex;
    while i < len {
        match codePoints[i] {
            CP_DIGIT_0
            |CP_DIGIT_1
            |CP_DIGIT_2
            |CP_DIGIT_3
            |CP_DIGIT_4
            |CP_DIGIT_5
            |CP_DIGIT_6
            |CP_DIGIT_7
            |CP_DIGIT_8
            |CP_DIGIT_9
            |CP_UPPER_A
            |CP_UPPER_B
            |CP_UPPER_C
            |CP_UPPER_D
            |CP_UPPER_E
            |CP_UPPER_F
            |CP_LOWER_A
            |CP_LOWER_B
            |CP_LOWER_C
            |CP_LOWER_D
            |CP_LOWER_E
            |CP_LOWER_F => {
                i += 1;
                continue;
            }
        }
        break;
    }
    return i == startIndex ? () : i;
}

// `i` is following first digit
function scanDecimal(int[] codePoints, int startIndex, Scanned result) returns int {
    int i = scanOptDigits(codePoints, startIndex);
    int len = codePoints.length();
    if i < len {
        int cp = codePoints[i];
        if cp == CP_DOT {
            int? endIndex = scanFractionExponent(codePoints, i + 1);
            if endIndex != () {
                return endDecimal(FRAG_DECIMAL_FP_NUMBER, codePoints, endIndex, result);
            }
        }
        else if cp == CP_UPPER_E || cp == CP_LOWER_E {
            int? endIndex = scanExponent(codePoints, i + 1);
            if endIndex != () {
                return endDecimal(FRAG_DECIMAL_FP_NUMBER, codePoints, endIndex, result);
            }
        }
    }
    return endDecimal(FRAG_DECIMAL_NUMBER, codePoints, i, result);
}

function endDecimal(FragCode fragCodeIfNoSuffix, int[] codePoints, int i, Scanned result) returns int {
    if i < codePoints.length() {
        int cp = codePoints[i];
        if cp == CP_UPPER_F || cp == CP_LOWER_F {
            endFragment(FRAG_DECIMAL_FP_NUMBER_F, i + 1, result);
            return i + 1;
        }
    }
    endFragment(fragCodeIfNoSuffix, i, result);
    return i;
}

function scanOptDigits(int[] codePoints, int startIndex) returns int {
    int len = codePoints.length();
    int i = startIndex;
    while i < len && isCodePointAsciiDigit(codePoints[i]) {
        i += 1;
    }
    return i;
}

function scanFractionExponent(int[] codePoints, int startIndex) returns int? {
    int i = startIndex;
    int len = codePoints.length();
    if i >= len || !isCodePointAsciiDigit(codePoints[i]) {
        return ();
    }
    if !isCodePointAsciiDigit(codePoints[i]) {
        return ();
    }
    i = scanOptDigits(codePoints, i + 1);
    if i >= len {
        return i;
    }
    int cp = codePoints[i];
    if  cp == CP_UPPER_E || cp == CP_LOWER_E {
        int? endIndex = scanExponent(codePoints, i + 1);
        if endIndex != () {
            return endIndex;
        }
    }
    return i;
}

function scanExponent(int[] codePoints, int startIndex) returns int? {
    int i = startIndex;
    int len = codePoints.length();
    if i >= len {
        return ();
    }
    int cp = codePoints[i];
    if cp == CP_MINUS || cp == CP_PLUS {
        i += 1;
        if i >= len {
            return ();
        }
        cp = codePoints[i];
    }
    if isCodePointAsciiDigit(cp) {
        return scanOptDigits(codePoints, i + 1);
    }
    return ();
}

function scanIdentifier(int[] codePoints, int startIndex, Scanned result) returns int {
    int len = codePoints.length();
    int i = startIndex;
    while i < len && isCodePointIdentifierFollowing(codePoints[i]) {
        i += 1;
    }
    endFragment(FRAG_IDENTIFIER, i, result);
    return i;
}

function endIdentifierOrKeyword(int[] codePoints, int i, Scanned result) {
    int[] ei = result.endIndex;
    int len = ei.length();
    int startIndex = len > 0 ? ei[len - 1] : 0;
    int? ki = keywordIndex(codePoints, startIndex, i);
    endFragment(ki == () ? FRAG_IDENTIFIER : <FragCode>(FRAG_KEYWORD + ki), i, result);
}

// Need a more efficient implementation
function keywordIndex(int[] codePoints, int startIndex, int endIndex) returns int? {
    string kw = checkpanic string:fromCodePointInts(codePoints.slice(startIndex, endIndex));
    foreach int i in 0 ..< keywords.length() {
        if keywords[i] == kw {
            return i;
        }
    }
    return ();
}

function endFragment(FragCode fragCode, int endIndex, Scanned result) {
    result.fragCodes.push(fragCode);
    result.endIndex.push(endIndex);
}

function endFragmentMerge(FragCode fragCode, int endIndex, Scanned result) {
    FragCode[] fc = result.fragCodes;
    int len = fc.length();
    if len > 0 && fc[len - 1] == fragCode {
        result.endIndex[len - 1] = endIndex;
    }
    else {
        fc.push(fragCode);
        result.endIndex.push(endIndex);
    }
}

function endFragmentCompoundAssign(int[] codePoints, int i, int CP, int FCP, Scanned result) returns int {
    int len = codePoints.length();
    if i < len {
        int cp = codePoints[i];
        if cp == CP_EQUAL {
            endFragment(FCP, i + 1, result);
            return i + 1;
        }
    }
    endFragment(CP, i, result);
    return i;
}

function isCodePointIdentifierFollowing(int cp) returns boolean {
   return isCodePointAsciiUpper(cp)
          || isCodePointAsciiLower(cp)
          || isCodePointAsciiDigit(cp)
          || cp == CP_UNDERSCORE
          || isCodePointUnicodeIdentifier(cp);
}

function isCodePointAsciiDigit(int cp) returns boolean {
    return CP_DIGIT_0 <= cp && cp <= CP_DIGIT_9;
}

function isCodePointAsciiLower(int cp) returns boolean {
    return CP_LOWER_A <= cp && cp <= CP_LOWER_Z;
}

function isCodePointAsciiUpper(int cp) returns boolean {
    return CP_UPPER_A <= cp && cp <= CP_UPPER_Z;
}

function isCodePointUnicodeIdentifier(int cp) returns boolean {
    return false;
}

// JBUG this avoids bloat that causes `method is too large` errors
function toFixedToken(string t) returns FixedToken? {
    return <FixedToken?>t;
}
