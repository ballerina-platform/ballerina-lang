import wso2/nballerina.err;

function parseExpr(Tokenizer tok) returns Expr|err:Syntax {
    Token? t = tok.current();
    if t == "[" {
        check tok.advance();
        Expr[] members = check parseExprList(tok, "]");
        ListConstructorExpr expr = { members };
        return expr;
    }
    else if t == "{" {
        check tok.advance();
        Field[] fields = check parseFields(tok);
        MappingConstructorExpr expr = { fields };
        return expr;
    }
    return parseInnerExpr(tok);
}

function parseInnerExpr(Tokenizer tok) returns Expr|err:Syntax {
    return parseBitwiseOrExpr(tok);
}

function parseBitwiseOrExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseBitwiseXorExpr(tok);
    while true {
        Token? t = tok.current();
        if t == "|" {
            check tok.advance();
            Expr right = check parseBitwiseXorExpr(tok);
            BinaryBitwiseExpr bin = { bitwiseOp: t, left: expr, right };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseBitwiseXorExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseBitwiseAndExpr(tok);
    while true {
        Token? t = tok.current();
        if t == "^" {
            check tok.advance();
            Expr right = check parseBitwiseAndExpr(tok);
            BinaryBitwiseExpr bin = { bitwiseOp: t, left: expr, right };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseBitwiseAndExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseEqualityExpr(tok);
    while true {
        Token? t = tok.current();
        if t == "&" {
            check tok.advance();
            Expr right = check parseEqualityExpr(tok);
            BinaryBitwiseExpr bin = { bitwiseOp: t, left: expr, right };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseEqualityExpr(Tokenizer tok)  returns Expr|err:Syntax {
    Expr expr = check parseRelationalExpr(tok);
    while true {
        Token? t = tok.current();
        if t is BinaryEqualityOp {
            check tok.advance();
            Expr right = check parseRelationalExpr(tok);
            BinaryEqualityExpr bin = { equalityOp: t, left: expr, right };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseRelationalExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseShiftExpr(tok);
    Token? t = tok.current();
    if t is BinaryRelationalOp {
        check tok.advance();
        Expr right = check parseShiftExpr(tok);
        BinaryRelationalExpr bin = { relationalOp: t, left: expr, right };
        return bin;
    }
    else if t == "is" {
        return finishTypeTestExpr(tok, expr, false);
    }
    if t == "!" {
        check tok.advance();
        Token? t2 = tok.current();
        if t2 is "is" {
            return finishTypeTestExpr(tok, expr, true);
        }
        return err:syntax("invalid operator");
    }
    else {
        return expr;
    }
}

function finishTypeTestExpr(Tokenizer tok, Expr expr, boolean negated) returns TypeTestExpr|err:Syntax {
    tok.setMode(MODE_TYPE_DESC);
    check tok.advance();
    InlineTypeDesc td = check parseInlineTypeDesc(tok);
    tok.setMode(MODE_NORMAL);
    return { td, left: expr, semType: resolveInlineTypeDesc(td), negated };
}

function parseRangeExpr(Tokenizer tok) returns RangeExpr|err:Syntax {
    Expr lower = check parseAdditiveExpr(tok);
    check tok.expect("..<");
    Expr upper = check parseAdditiveExpr(tok);
    return { lower, upper };
}

function parseShiftExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseAdditiveExpr(tok);
    while true {
        Token? t = tok.current();
        if t is ("<<"|">>>"|">>") {
            check tok.advance();
            Expr right = check parseAdditiveExpr(tok);
            BinaryBitwiseExpr shift = { bitwiseOp: t, left: expr, right };
            expr = shift;
        }
        else {
            break;
        }
    }
    return expr;
}

function parseAdditiveExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseMultiplicativeExpr(tok);
    while true {
        Token? t = tok.current();
        if t is ("+"|"-") {
            Position pos = tok.currentPos();
            check tok.advance();
            Expr right = check parseMultiplicativeExpr(tok);
            BinaryArithmeticExpr bin = { arithmeticOp: t, left: expr, right, pos };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseMultiplicativeExpr(Tokenizer tok) returns Expr|err:Syntax {
    Expr expr = check parseUnaryExpr(tok);
    while true {
        Token? t = tok.current();
        if t is ("*"|"/"|"%") {
            Position pos = tok.currentPos();
            check tok.advance();
            Expr right = check parseUnaryExpr(tok);
            BinaryArithmeticExpr bin = { arithmeticOp: t, left: expr, right, pos };
            expr = bin;
        } 
        else {
            break;
        }
    }
    return expr;
}

function parseUnaryExpr(Tokenizer tok) returns Expr|err:Syntax {
    Token? t = tok.current();
    if t is "-"|"!"|"~" {
        Position pos = tok.currentPos();
        check tok.advance();
        Expr operand = check parseUnaryExpr(tok);
        UnaryExpr expr = { op: t, operand, pos };
        return expr;
    }
    else if t is "<" {
        return parseTypeCastExpr(tok);
    }
    return parsePrimaryExpr(tok);
}

function parseTypeCastExpr(Tokenizer tok) returns Expr|err:Syntax {
    tok.setMode(MODE_TYPE_DESC);
    Position pos = tok.currentPos();
    check tok.advance();
    InlineTypeDesc td = check parseInlineTypeDesc(tok);
    check tok.expect(">");
    tok.setMode(MODE_NORMAL);
    Expr operand = check parseUnaryExpr(tok);
    TypeCastExpr expr = { pos, td, operand, semType: resolveInlineTypeDesc(td) };
    return expr;
}

function parsePrimaryExpr(Tokenizer tok) returns Expr|err:Syntax {
    return finishPrimaryExpr(tok, check startPrimaryExpr(tok));
}

function startPrimaryExpr(Tokenizer tok) returns Expr|err:Syntax {
    Token? t = tok.current();
    if t is [IDENTIFIER, string] {
        Position pos = tok.currentPos();
        string identifier = t[1];
        VarRefExpr expr = { varName: identifier };
        check tok.advance();
        t = tok.current();
        if t == "(" {
            check tok.advance();
            return finishFunctionCallExpr(tok, (), identifier, pos);
        }
        return expr;
    }
    else if t is [DECIMAL_NUMBER, string] {
        IntLiteralExpr expr = { base: 10, digits: t[1], pos: tok.currentPos() };
        check tok.advance();
        return expr;
    }
    else if t is [DECIMAL_FP_NUMBER, string, FLOAT_TYPE_SUFFIX|()] {
        FpLiteralExpr expr = { untypedLiteral: t[1], typeSuffix: t[2], pos: tok.currentPos() };
        check tok.advance();
        return expr;
    }
    else if t is [HEX_INT_LITERAL, string] {
        IntLiteralExpr expr = { base: 16, digits: t[1], pos: tok.currentPos() };
        check tok.advance();
        return expr;
    }
    else if t is [STRING_LITERAL, string] {
        ConstValueExpr expr = { value: t[1] };
        check tok.advance();
        return expr;
    }
    else if t == "(" {
        check tok.advance();
        if tok.current() == ")" {
            check tok.advance();
            ConstValueExpr expr = { value: () };
            return expr;
        }
        Expr expr = check parseInnerExpr(tok);
        check tok.expect(")");
        return expr;
    }
    else if t is "true"|"false" {
        check tok.advance();
        ConstValueExpr expr = { value: t == "true" };
        return expr;
    }
    else if t is "null" {
        check tok.advance();
        ConstValueExpr expr = { value: () };
        return expr;
    }
    else {
        return parseError(tok);
    }
}

function finishPrimaryExpr(Tokenizer tok, Expr expr) returns Expr|err:Syntax {
    Token? t = tok.current();
    if t == "[" {
        Position pos = tok.currentPos();
        check tok.advance();
        Expr index = check parseInnerExpr(tok);
        check tok.expect("]");
        MemberAccessExpr accessExpr = { container: expr, index, pos };
        return finishPrimaryExpr(tok, accessExpr);
    }
    else if t == "." {
        MethodCallExpr methodCallExpr = check finishMethodCallExpr(tok, expr);
        return finishPrimaryExpr(tok, methodCallExpr);
    }
    else {
        return expr;
    }
}

// Called with current token as "."
function finishMethodCallExpr(Tokenizer tok, Expr target) returns MethodCallExpr|err:Syntax {
    Position pos = tok.currentPos();
    check tok.advance();
    Token? t = tok.current();
    if t is [IDENTIFIER, string] {
        string name = t[1];
        check tok.advance();
        t = tok.current();
        if t == "(" {
            check tok.advance();
            Expr[] args = check parseExprList(tok, ")");
            MethodCallExpr methodCallExpr = { methodName: name, target, pos, args };
            return methodCallExpr;
        }
    }
    return parseError(tok, "expected method call after dot");
}

function finishFunctionCallExpr(Tokenizer tok, string? prefix, string funcName, Position pos) returns FunctionCallExpr|err:Syntax {
    Expr[] args = check parseExprList(tok, ")");
    return { funcName, pos, args, prefix };
}

function parseExprList(Tokenizer tok, "]"|")" terminator) returns Expr[]|err:Syntax {
    Expr[] exprs = [];
    if tok.current() != terminator {
        while true {
            Expr expr = check parseExpr(tok);
            exprs.push(expr);
            Token? t = tok.current();
            if t == "," {
                check tok.advance();
            }
            else if t == terminator {
                break;
            }
            else {
                return parseError(tok, "invalid expression list");
            }
        }
    }
    check tok.advance();
    return exprs;
}

function parseFields(Tokenizer tok) returns Field[]|err:Syntax {
    Field[] fields = [];
    if tok.current() != "}" {
        while true {
            Field f = check parseField(tok);
            fields.push(f);
            Token? t = tok.current();
            if t == "," {
                check tok.advance();
            }
            else if t == "}" {
                break;
            }
            else {
                return parseError(tok, "invalid field list");
            }
        }
    }
    check tok.advance();
    return fields;
}

function parseField(Tokenizer tok) returns Field|err:Syntax {
    Token? t = tok.current();
    match t {
        [IDENTIFIER, var name]
        | [STRING_LITERAL, var name] => {
            // Don't report an error for duplicates here
            // (it's not a syntax error)
            // Instead save the position and report during codeGen
            Position pos = tok.currentPos();
            check tok.advance();
            check tok.expect(":");
            Expr value = check parseExpr(tok);
            Field f = { pos, name, value };
            return f;
        }
    }
    return err:syntax("expected field name");
}

// This is simple-const-expr in the spec
// This is used for match patterns
// Will also be used for type descriptors
function parseSimpleConstExpr(Tokenizer tok) returns SimpleConstExpr|err:Syntax {
    Token? t = tok.current();
    if t == "-" {
        Position pos = tok.currentPos();
        check tok.advance();
        IntLiteralExpr operand = check parseIntLiteralExpr(tok);
        SimpleConstNegateExpr expr = { operand, pos };
        return expr;
    }
    match t {
        [IDENTIFIER, var varName] => {
            VarRefExpr expr = { varName };
            check tok.advance();
            return expr;
        }
        [STRING_LITERAL, var value] => {
            ConstValueExpr expr = { value };
            check tok.advance();
            return expr;
        }
        "("  => {
            check tok.advance();
            check tok.expect(")");
            ConstValueExpr expr = { value: () };
            return expr;
        }
        "true"|"false"  => {
            check tok.advance();
            ConstValueExpr expr = { value: t == "true" };
            return expr;
        }
        [DECIMAL_NUMBER, _]
        | [HEX_INT_LITERAL, _] => {
            return parseIntLiteralExpr(tok);
        }
    }
    return parseError(tok);
}

function parseNumericLiteralExpr(Tokenizer tok) returns NumericLiteralExpr|err:Syntax {
    Token? t = tok.current();
    Position pos = tok.currentPos();
    match t {
        [DECIMAL_NUMBER, _]
        | [HEX_INT_LITERAL, _] => {
            return parseIntLiteralExpr(tok);
        }
        [DECIMAL_FP_NUMBER, var untypedLiteral, var typeSuffix] => {
            check tok.advance();
            return { untypedLiteral, typeSuffix, pos };
        }
    }
    return parseError(tok, "expected numeric literal");
}

// XXX This can merged into parseNumericLiteralExpr when we add float support
// outside types
function parseIntLiteralExpr(Tokenizer tok) returns IntLiteralExpr|err:Syntax {
    Token? t = tok.current();
    Position pos = tok.currentPos();
    match t {
        [DECIMAL_NUMBER, var digits] => {
            check tok.advance();
            return { base: 10, digits, pos };
        }
        [HEX_INT_LITERAL, var digits] => {
            check tok.advance();
            return { base: 16, digits, pos };
        }
    }
    return parseError(tok, "expected integer literal");
}

public function intFromIntLiteral(IntLiteralBase base, string digits) returns int|error {
    return base == 10 ? int:fromString(digits) : int:fromHexString(digits);
}
