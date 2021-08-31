// Parsing of type descriptors
import wso2/nballerina.err;

// This is for the subset we currently support in the compiler
function parseInlineTypeDesc(Tokenizer tok) returns InlineTypeDesc|err:Syntax {
    Token? t = tok.current();
    if t == "any" {
        check tok.advance();
        t = tok.current();
        if t == "[" {
            check tok.advance();
            check tok.expect("]");
            InlineArrayTypeDesc td = {};
            return td;
        }
        return "any";
    }
    else if t is InlineBasicTypeDesc {
        check tok.advance();
        return parseInlineUnionTypeDesc(tok, t);
    }
    else if t is "map" {
        check tok.advance();
        check tok.expect("<");
        check tok.expect("any");
        check tok.expect(">");
        InlineMapTypeDesc td = {};
        return td;
    }
    return parseError(tok, "expected type descriptor");    
}

function parseInlineUnionTypeDesc(Tokenizer tok, InlineBasicTypeDesc td) returns InlineTypeDesc|err:Syntax {
    InlineAltTypeDesc left = check parseInlineOptionalTypeDesc(tok, td);
    while true {
        Token? t = tok.current();
        if t != "|" {
            break;
        }
        check tok.advance();
        t = tok.current();
        if !(t is InlineBasicTypeDesc) {
            return parseError(tok, "expected basic type name after |");
        }
        else {
            check tok.advance();
            var right = check parseInlineOptionalTypeDesc(tok, t);
            InlineUnionTypeDesc u = { left, right };
            left = u;
        }
    }
    return left;
}

function parseInlineOptionalTypeDesc(Tokenizer tok, InlineBasicTypeDesc td) returns InlineAltTypeDesc|err:Syntax {
    Token? t = tok.current();
    if t == "?" {
        check tok.advance();
        return { left: td, right: "()" };
    }
    return td;
}

function parseTypeDesc(Tokenizer tok) returns TypeDesc|err:Syntax {
    if tok.current() == "function" {
        check tok.advance();
        return parseFunctionTypeDesc(tok);
    }
    return parseUnion(tok);
}

function parseUnion(Tokenizer tok) returns TypeDesc|err:Syntax {
    TypeDesc td = check parseIntersection(tok);
    while tok.current() == "|" {
        check tok.advance();
        TypeDesc right = check parseIntersection(tok);
        BinaryTypeDesc bin = { op: "|", left: td, right };
        td = bin;
    }
    return td;
}

function parseIntersection(Tokenizer tok) returns TypeDesc|err:Syntax {
    TypeDesc td = check parsePostfixTypeDesc(tok);
    while tok.current() == "&" {
        check tok.advance();
        TypeDesc right = check parsePostfixTypeDesc(tok);
        BinaryTypeDesc bin = { op: "&", left: td, right };
        td = bin;
    }
    return td;
}

function parsePostfixTypeDesc(Tokenizer tok) returns TypeDesc|err:Syntax {
    TypeDesc td = check parsePrimaryTypeDesc(tok);
    while true {
        if tok.current() == "?" {
            check tok.advance();
            BinaryTypeDesc bin =  { op: "|", left: td, right: "()" };
            td = bin;
        }
        else if tok.current() == "[" {
            check tok.advance();
            check tok.expect("]");
            ListTypeDesc list = { members: [], rest: td };
            td = list;
        }
        else {
            break;
        }
    }
    return td;
}

final map<BuiltinIntSubtypeDesc> BUILTIN_INT_SUBTYPES = {
    Signed8: "sint8",
    Signed16: "sint16",
    Signed32: "sint32",
    Unsigned8: "uint8",
    Unsigned16: "uint16",
    Unsigned32: "uint32"
};

// Tokenizer is on first token of the type descriptor
// Afterwards it is on the token immediately following the type descriptor
function parsePrimaryTypeDesc(Tokenizer tok) returns TypeDesc|err:Syntax {
    Token? cur = tok.current();
    match cur {
        "(" => {
            check tok.advance();
            if tok.current() == ")" {
                check tok.advance();
                return "()";
            }
            TypeDesc td = check parseTypeDesc(tok);
            check tok.expect(")");
            return td;
        }
        "boolean"
        | "decimal"
        | "float"
        | "string"
        | "xml"
        | "typedesc"
        | "handle"
        | "any"
        | "never"
        | "json"
        |  "readonly" => {
            check tok.advance();
            // JBUG should not need cast #30191
            return <LeafTypeDesc>cur;
        }
        "byte" => {
            check tok.advance();
            return "uint8";
        }
        "[" => {
            return parseTupleTypeDesc(tok);
        }
        "map" => {
            check tok.advance();
            return <MappingTypeDesc>{ rest: check parseTypeParam(tok), fields: [] };
        }
        "error" => {
            check tok.advance();
            if tok.current() != "<" {
                return "error";
            }
            return <ErrorTypeDesc>{ detail: check parseTypeParam(tok) };
        }        
        "record" => {
            return parseRecordTypeDesc(tok);         
        }
        "int" => {
            check tok.advance();
            if tok.current() != ":" {
                return "int";
            }
            check tok.advance();
            Token? t = tok.current();
            if t is [IDENTIFIER, string] {
                var name = t[1];
                BuiltinIntSubtypeDesc? desc = BUILTIN_INT_SUBTYPES[name];
                if !(desc is ()) {
                    check tok.advance();
                    return desc;
                }
                return tok.err("unrecognized integer subtype '" + name + "'");
            }
            // match falls through to parseError
        }
        [IDENTIFIER, var ref] => {
            TypeDescRef r = { ref, pos: tok.currentPos() };
            check tok.advance();
            return r;
        }
        [STRING_LITERAL, var str] => {
            check tok.advance();
            return <SingletonTypeDesc>{ value: str };
        }
        [DECIMAL_NUMBER, _]
        | [HEX_INT_LITERAL, _]
        | [DECIMAL_FP_NUMBER, _, _] => {
            return parseNumericLiteralTypeDesc(tok, ());
        }
        "-" => {
            Position signPos = tok.currentPos();
            check tok.advance();
            return parseNumericLiteralTypeDesc(tok, signPos);
        }
        "true" => {
            check tok.advance();
            return <SingletonTypeDesc>{ value: true };
        }
        "false" => {
            check tok.advance();
            return <SingletonTypeDesc>{ value: false };
        }
    }
    return parseError(tok);
}

// It would be cleaner to handle this using const folding, but there are obstacles:
// it treats conversion errors as semantic errors, but at this point we can only return syntax errors;
// we don't have what we need to create a context.
// Another approach would be to have a kind of TypeDesc that refers to an NumericLiteralExpr and then convert in resolveTypes.
// XXX Revisit when floats (and maybe decimals) are fully incorporated in the front-end.
function parseNumericLiteralTypeDesc(Tokenizer tok, Position? signPos = ()) returns SingletonTypeDesc|err:Syntax {
    NumericLiteralExpr expr = check parseNumericLiteralExpr(tok);
    if expr is FpLiteralExpr {
        var f = float:fromString(expr.untypedLiteral);
        if f is error {
            return tok.err(`invalid float literal ${expr.untypedLiteral}`); // don't think this should happen
        }
        else {
            float value = f;
            if signPos != () {
                value = -value;
            }
            return { value };
        }
    }
    else {
        var n = intFromIntLiteral(expr.base, expr.digits);
        if n is error {
            return tok.err(`invalid integer literal ${expr.digits}`);
        }
        else {
            int value;
            if signPos == () {
                value = n;
            }
            else if n == int:MIN_VALUE {
                return tok.err(`-${expr.digits} overflows`);
            }
            else {
                value = -n;
            }
            return { value };
        }
    }
}

function parseTypeParam(Tokenizer tok) returns TypeDesc|err:Syntax {
    check tok.expect("<");
    tok.setMode(MODE_TYPE_DESC);
    TypeDesc td = check parseTypeDesc(tok);
    check tok.expect(">");
    tok.setMode(MODE_NORMAL);
    return td;
}

// current token should be "("
function parseFunctionTypeDesc(Tokenizer tok, string[]? paramNames = ()) returns FunctionTypeDesc|err:Syntax {
    // skip "function"
    check tok.expect("(");
    TypeDesc[] args = [];
    while true {
        if tok.current() == ")" {
            break;
        }
        args.push(check parseTypeDesc(tok));
        match tok.current() {
            [IDENTIFIER, var paramName] => {
                if !(paramNames is ()) {
                    paramNames.push(paramName);
                }
                check tok.advance();
            }
            _ => {
                if !(paramNames is ()) {
                    return parseError(tok);
                }
            }
        }
        if tok.current() == "," {
            check tok.advance();
        }
    }
    // on ")"
    check tok.advance();
    TypeDesc ret;
    if tok.current() == "returns" {
        check tok.advance();
        ret = check parseTypeDesc(tok);
    }
    else {
        ret = "()";
    }
    return <FunctionTypeDesc>{ args, ret };
}

// current token is []
function parseTupleTypeDesc(Tokenizer tok) returns ListTypeDesc|err:Syntax {
    TypeDesc[] members = [];
    TypeDesc rest = "never";
    check tok.advance();
    if tok.current() != "]" {
        while true {
            TypeDesc td = check parseTypeDesc(tok);
            if tok.current() == "..." {
                rest = td;
                check tok.advance();
            }
            else {
                members.push(td);
                if tok.current() == "," {
                    check tok.advance();
                    continue;
                }
                if tok.current() == "]" {
                    break;
                }
            }
            return parseError(tok);
        }
    }
    check tok.advance();
    return {members, rest};
}

function parseRecordTypeDesc(Tokenizer tok) returns MappingTypeDesc|err:Syntax {
    check tok.advance();
    check tok.expect("{|");
    FieldDesc[] fields = [];
    TypeDesc? rest = ();
    while tok.current() != "|}" {
        if !(rest is ()) {
            return parseError(tok);
        }
        TypeDesc td = check parseTypeDesc(tok);
        match tok.current() {
            "..." => {
                rest = td;
                check tok.advance();
            }
            [IDENTIFIER, var name] => {
                check tok.advance();
                fields.push({name, typeDesc: td});
            }
            _ => {
                return parseError(tok);
            }
        }
        check tok.expect(";");
    }
    check tok.advance();
    return { fields, rest: rest ?: "never" };
}
