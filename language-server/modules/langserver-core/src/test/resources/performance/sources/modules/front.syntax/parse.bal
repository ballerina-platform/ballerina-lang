// Parse one file in a module

import wso2/nballerina.err;

public readonly class SourceFile {
    *err:File;
    private string fn;
    public function init(string fn) {
        self.fn = fn;
    }
    public function filename() returns string => self.fn;
}

public function parseModulePart(string[] lines, string filename) returns ModulePart|err:Syntax {
    SourceFile file = new(filename);
    Tokenizer tok = new (lines, file);
    check tok.advance();
    ModulePart part = {
        file,
        defns: [],
        importDecl: check parseImportDecl(tok)
    };
    while tok.current() != () {
        part.defns.push(check parseModuleDecl(tok));
    }
    return part;
}

public function parseExpression(string[] lines, string filename) returns Expr|err:Syntax {
    SourceFile file = new(filename);
    Tokenizer tok = new (lines, file);
    check tok.advance();
    Expr expr = check parseExpr(tok);
    if tok.current() != () {
        return parseError(tok, "unexpected input after expression");
    }
    return expr;
}

function parseImportDecl(Tokenizer tok) returns ImportDecl?|err:Syntax {
    Token? t = tok.current();
    if t != "import" {
        return;
    }
    check tok.advance();
    t = tok.current();
    if t is [IDENTIFIER, string] { 
        string org = t[1];
        check tok.advance();
        check tok.expect("/");
        t = tok.current();
        if t is [IDENTIFIER, string] {
            check tok.advance();
            check tok.expect(";");
            return { org, module: t[1] };
        }
    }
    return parseError(tok, "import declaration");
}

function parseModuleDecl(Tokenizer tok) returns ModuleLevelDefn|err:Syntax {
    Token? t = tok.current();
    Visibility vis;
    if t == "public" {
        vis = t;
        check tok.advance();
        t = tok.current();
    }
    else {
        vis = ();
    }
    match t {
        "type" => {
            return parseTypeDefinition(tok, vis);
        }
        "const" => {
            return parseConstDefinition(tok, vis);
        }
        "function" => {
            return parseFunctionDefinition(tok, vis);
        }
    }
    return parseError(tok);
}

function parseTypeDefinition(Tokenizer tok, Visibility vis) returns TypeDefn|err:Syntax {
    check tok.advance();
    Position pos = tok.currentPos();
    Token? t = tok.current();
    if t is [IDENTIFIER, string] {
        string name = t[1];
        check tok.advance();
        TypeDesc td = check parseTypeDesc(tok);
        check tok.expect(";");
        return { name, td, pos, vis, file: tok.file };
    }
    return parseError(tok);
}

function parseConstDefinition(Tokenizer tok, Visibility vis) returns ConstDefn|err:Syntax {
    check tok.advance();
    Position pos = tok.currentPos();
    Token? t = tok.current();
    InlineBasicTypeDesc? td = ();
    if t is InlineBasicTypeDesc {
        check tok.advance();
        td = t;
        t = tok.current();
    }
    if t is [IDENTIFIER, string] {
        string name = t[1];
        check tok.advance();
        check tok.expect("=");
        Expr expr = check parseInnerExpr(tok);
        check tok.expect(";");
        return { td, name, expr, pos, vis, file: tok.file };
    }
    return parseError(tok);
}

function parseFunctionDefinition(Tokenizer tok, Visibility vis) returns FunctionDefn|err:Syntax {
    check tok.advance();
    Position pos = tok.currentPos();
    Token? t = tok.current();
    if t is [IDENTIFIER, string] {
        string name = t[1];
        check tok.advance();
        string[] paramNames = [];
        FunctionTypeDesc typeDesc = check parseFunctionTypeDesc(tok, paramNames);
        Stmt[] body = check parseStmtBlock(tok);
        FunctionDefn defn = { name, vis, paramNames, typeDesc, pos, body, file: tok.file };
        return defn;
    }
    return parseError(tok);
}

function parseError(Tokenizer tok, string? detail = ()) returns err:Syntax {
    string message = "parse error";
    Token? t = tok.current();
    if t is string {
        // JBUG cast needed #30734
        // XXX should use err:Template here
        message += " at '" + t + "'";
    }
    if detail != () {
        message += ": " + detail;
    }
    return tok.err(message);
}

public function defnLocation(ModuleLevelDefn defn) returns err:Location {
    return err:location(defn.file, defn.pos);
}