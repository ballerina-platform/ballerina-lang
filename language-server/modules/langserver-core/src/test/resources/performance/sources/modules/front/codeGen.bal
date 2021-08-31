import wso2/nballerina.bir;
import wso2/nballerina.types as t;
import wso2/nballerina.front.syntax as s;
import wso2/nballerina.err;

type Environment record {|
    Binding? bindings;
    // A list of registers that were narrowed but have been assigned to
    // Holds the number of the original, unnarrowed register
    int[] assignments = [];
|};

type Binding record {|
    string name;
    bir:Register reg;
    boolean isFinal;
    Binding? prev;
    // When this binding represents a narrowing, this refers to the
    // original binding that was not narrowed.
    // In the case of the nested narrowing, this points all the way
    // back to the original explicit binding, not to the previous narrowing.
    Binding? unnarrowed = ();
|};

type StmtEffect record {|
    bir:BasicBlock? block;
    Binding? bindings = ();
    int[] assignments = [];
|};

type ExprEffect record {|
    bir:BasicBlock block;
    bir:Operand result;
    // This is non-nil when the expression is a boolean expression that case causes the type
    // of a variable to be narrowed when the expression is used as a condition.
    // We do not have `&&` and `||` yet, so only one variable is narrowed.
    Narrowing? narrowing = ();
    // This is non-nil when the expression is a variable reference.
    Binding? binding = ();
|};

type BooleanExprEffect record {|
    *ExprEffect;
    bir:BooleanOperand result;
|};

type IntExprEffect record {|
    *ExprEffect;
    bir:IntOperand result;
|};

type RegExprEffect record {|
    *ExprEffect;
    bir:Register result;
|};

// This describes the narrowing of the type of a variable
// caused by the use of a boolean expression in a condition.
type Narrowing record {|
    Binding binding;
    t:SemType ifTrue;
    t:SemType ifFalse;
    bir:InsnRef testInsn;
    boolean negated = false;
|};

type CodeGenError err:Semantic|err:Unimplemented;

type LoopContext record {|
    // number of first register created in the loop
    int startRegister;
    bir:BasicBlock onBreak;
    bir:BasicBlock? onContinue;
    // JBUG #31311 does not allow `outer` here
    LoopContext? enclosing;
    // will use this with while true to determine whether
    // following block is reachable
    boolean breakUsed = false;
    int[] onBreakAssignments = [];
    int[] onContinueAssignments = [];
|};

class CodeGenContext {
    final Module mod;
    final s:SourceFile file;
    final bir:FunctionCode code;
    final string functionName;
    final t:SemType returnType;
    LoopContext? loopContext = ();
    private final string?[] registerVarNames = [];

    function init(Module mod, s:SourceFile file, string functionName, t:SemType returnType) {
        self.mod = mod;
        self.file = file;
        self.code = {};
        self.functionName = functionName;
        self.returnType = returnType;
    }

    function createRegister(bir:SemType t, string? varName = ()) returns bir:Register {
        bir:Register reg = bir:createRegister(self.code, t, varName);
        self.registerVarNames[reg.number] = varName;
        return reg;
    }

    function nextRegisterNumber() returns int {
        return self.code.registers.length();
    }
    function registerVarName(int registerNumber) returns string? {
        return self.registerVarNames[registerNumber];
    }
    
    function createBasicBlock(string? name = ()) returns bir:BasicBlock {
        return bir:createBasicBlock(self.code, name);
    }

    function semanticErr(err:Message msg, s:Position? pos = (), error? cause = ()) returns err:Semantic {
        return err:semantic(msg, loc=self.location(pos), cause=cause, functionName=self.functionName);
    }

    function unimplementedErr(err:Message msg, s:Position? pos = (), error? cause = ()) returns err:Unimplemented {
        return err:unimplemented(msg, loc=self.location(pos), cause=cause, functionName=self.functionName);
    }
    
    private function location(s:Position? pos) returns err:Location {
        return err:location(self.file, pos);
    }

    function pushLoopContext(bir:BasicBlock onBreak, bir:BasicBlock? onContinue) {
        LoopContext c = { onBreak, onContinue, enclosing: self.loopContext, startRegister: self.nextRegisterNumber()  };
        self.loopContext = c;
    }

    function loopUsedBreak() returns boolean {
        return (<LoopContext>self.loopContext).breakUsed;
    }

    function loopContinueBlock() returns bir:BasicBlock? {
        return (<LoopContext>self.loopContext).onContinue;
    }

    function loopStartRegister() returns int {
        return (<LoopContext>self.loopContext).startRegister;
    }

    function popLoopContext() {
        self.loopContext = (<LoopContext>self.loopContext).enclosing;
    }

    function onBreakLabel() returns bir:Label|err:Semantic {
        LoopContext? c = self.loopContext;
        if c is () {
            return self.semanticErr("break not in loop");
        }
        else {
            c.breakUsed = true;
            return c.onBreak.label;
        }
    }

    function onContinueLabel() returns bir:Label|err:Semantic {
        LoopContext? c = self.loopContext;
        if c is () {
            return self.semanticErr("continue not in loop");
        }
        else {
            bir:BasicBlock b = c.onContinue ?: self.createBasicBlock();
            c.onContinue = b;
            return b.label;
        }
    }

    function addOnBreakAssignments(int[] assignments) {
        if assignments.length() == 0 {
            return;
        }
        LoopContext c = <LoopContext>self.loopContext;
        addAssignments(c.onBreakAssignments, assignments, c.startRegister);
    }

    function addOnContinueAssignments(int[] assignments) {
        if assignments.length() == 0 {
            return;
        }
        LoopContext c = <LoopContext>self.loopContext;
        addAssignments(c.onContinueAssignments, assignments, c.startRegister);
    }

    function onBreakAssignments() returns int[] {
        return  (<LoopContext>self.loopContext).onBreakAssignments;
    }

    function onContinueAssignments() returns int[] {
        return  (<LoopContext>self.loopContext).onContinueAssignments;
    }

    function foldExpr(Environment env, s:Expr expr, t:SemType? expectedType) returns s:Expr|FoldError {
        return foldExpr(new CodeGenFoldContext(self, env), expectedType, expr);
    }

}

class CodeGenFoldContext {
    *FoldContext;
    final CodeGenContext cx;
    final Environment env;
    function init(CodeGenContext cx, Environment env) {
        self.cx = cx;
        self.env = env;
    }

    function lookupConst(string varName) returns s:FLOAT_ZERO|t:Value?|FoldError {
        t:Value|Binding v = check lookupVarRef(self.cx, varName, self.env);
        if v is Binding {
            t:Value? shape = t:singleShape(v.reg.semType);
            if !(shape is ()) && shape.value == s:FLOAT_ZERO {
                return s:FLOAT_ZERO;
            }
            return shape;
        }
        else {
            return v;
        }
    }

    function semanticErr(err:Message msg, s:Position? pos = (), error? cause = ()) returns err:Semantic {
        return self.cx.semanticErr(msg, pos=pos, cause=cause);
    }
}

function addAssignments(int[] dest, int[] src, int excludeStart) {
    foreach int r in src {
        if r < excludeStart {
            dest.push(r);
        }
    }
}

function codeGenFunction(Module mod, s:SourceFile file, string functionName, bir:FunctionSignature signature, string[] paramNames, s:Stmt[] body) returns bir:FunctionCode|CodeGenError {
    CodeGenContext cx = new(mod, file, functionName, signature.returnType);
    bir:BasicBlock startBlock = cx.createBasicBlock();
    Binding? bindings = ();
    foreach int i in 0 ..< paramNames.length() {
        bir:Register reg = cx.createRegister(signature.paramTypes[i], paramNames[i]);
        bindings = { name: paramNames[i], reg, prev: bindings, isFinal: true };
    }
    var { block: endBlock } = check codeGenStmts(cx, startBlock, { bindings }, body);
    if !(endBlock is ()) {
        bir:RetInsn ret = { operand: () };
        endBlock.insns.push(ret);
    }
    codeGenOnPanic(cx);
    return cx.code;
}

function codeGenOnPanic(CodeGenContext cx) {
    bir:BasicBlock[] blocks = cx.code.blocks;
    bir:BasicBlock? onPanicBlock = ();
    foreach var b in blocks {
        if bir:isBasicBlockPotentiallyPanicking(b) {
            bir:BasicBlock pb;
            if onPanicBlock is () {
                pb = cx.createBasicBlock();
                onPanicBlock = pb;
            }
            else {
                pb = onPanicBlock;
            }
            b.onPanic = pb.label;
        }
    }
    if !(onPanicBlock is ()) {
        bir:Register reg = cx.createRegister(t:ERROR);
        bir:CatchInsn catch = { result: reg };
        onPanicBlock.insns.push(catch);
        onPanicBlock.insns.push(<bir:AbnormalRetInsn>{operand: reg});
    }
}

function codeGenStmts(CodeGenContext cx, bir:BasicBlock bb, Environment initialEnv, s:Stmt[] stmts) returns CodeGenError|StmtEffect {
    bir:BasicBlock? curBlock = bb;
    Environment env = environmentCopy(initialEnv);
    final int startRegister = cx.nextRegisterNumber();
    foreach var stmt in stmts {
        StmtEffect effect;
        if curBlock is () {
            return cx.semanticErr("unreachable code");
        }
        else if stmt is s:IfElseStmt {
            effect = check codeGenIfElseStmt(cx, curBlock, env, stmt);
        }
        else if stmt is s:MatchStmt {
            effect = check codeGenMatchStmt(cx, curBlock, env, stmt);
        }
        else if stmt is s:WhileStmt {
            // JBUG #31327 cast
            effect = check codeGenWhileStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else if stmt is s:ForeachStmt {
            effect = check codeGenForeachStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else if stmt is s:BreakStmt {
            effect = check codeGenBreakStmt(cx, <bir:BasicBlock>curBlock, env);
        }
        else if stmt is s:ContinueStmt {
            effect = check codeGenContinueStmt(cx, <bir:BasicBlock>curBlock, env);
        }
        else if stmt is s:ReturnStmt {
            // JBUG #31327 cast
            effect = check codeGenReturnStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else if stmt is s:VarDeclStmt {
            effect = check codeGenVarDeclStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else if stmt is s:AssignStmt {
            effect = check codeGenAssignStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else if stmt is s:CompoundAssignStmt {
            effect = check codeGenCompoundAssignStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        else {
            effect = check codeGenCallStmt(cx, <bir:BasicBlock>curBlock, env, stmt);
        }
        curBlock = effect.block;
        Binding? bindings = effect.bindings;
        if !(bindings is ()) {
            env.bindings = bindings;
        }
        if curBlock is () {
            env.assignments.setLength(0);
        }
        else {
            env.assignments.push(...effect.assignments);
        }
    }                
    int[] assignments = [];
    addAssignments(assignments, env.assignments, startRegister);
    return { block: curBlock, assignments };
}

function environmentCopy(Environment env) returns Environment {
    return { bindings: env.bindings, assignments: env.assignments.clone() };
}

function codeGenForeachStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:ForeachStmt stmt) returns CodeGenError|StmtEffect {
    string varName = stmt.varName;
    if lookup(varName, env) !== () {
        return cx.semanticErr(`duplicate declaration of ${varName}`);
    }
    s:RangeExpr range = stmt.range;
    var { result: lower, block: evalUpper } = check codeGenExprForInt(cx, startBlock, env, check cx.foldExpr(env, range.lower, t:INT));
    var { result: upper, block: initLoopVar } = check codeGenExprForInt(cx, evalUpper, env, check cx.foldExpr(env, range.upper, t:INT));
    bir:Register loopVar = cx.createRegister(t:INT, varName);
    bir:AssignInsn init = { result: loopVar, operand: lower };
    initLoopVar.insns.push(init);
    bir:BasicBlock loopHead = cx.createBasicBlock();
    bir:BasicBlock exit = cx.createBasicBlock();
    bir:BranchInsn branchToLoopHead = { dest: loopHead.label };
    initLoopVar.insns.push(branchToLoopHead);
    bir:Register condition = cx.createRegister(t:BOOLEAN);
    bir:CompareInsn compare = { op: "<", orderType: t:UT_INT, operands: [loopVar, upper], result: condition };
    loopHead.insns.push(compare);
    bir:BasicBlock loopBody = cx.createBasicBlock();
    bir:CondBranchInsn branch = { operand: condition, ifFalse: exit.label, ifTrue: loopBody.label };
    loopHead.insns.push(branch);
    cx.pushLoopContext(exit, ());
    Binding loopBindings = { name: varName, reg: loopVar, prev: env.bindings, isFinal: true };
    var { block: loopEnd, assignments } = check codeGenStmts(cx, loopBody, { bindings: loopBindings }, stmt.body);
    
    bir:BasicBlock? loopStep = cx.loopContinueBlock();
    if !(loopEnd is ()) {
        loopStep = loopStep ?: cx.createBasicBlock();
        bir:BranchInsn branchToLoopStep = { dest: (<bir:BasicBlock>loopStep).label };
        loopEnd.insns.push(branchToLoopStep);
        check validLoopAssignments(cx, assignments);
    }
    check validLoopAssignments(cx, cx.onContinueAssignments());
    assignments.push(...cx.onContinueAssignments());
    assignments.push(...cx.onBreakAssignments());
    if !(loopStep is ()) {
        bir:IntNoPanicArithmeticBinaryInsn increment = { op: "+", operands: [loopVar, 1], result: loopVar };
        loopStep.insns.push(increment);
        loopStep.insns.push(branchToLoopHead);
    }
    cx.popLoopContext();
    // XXX shouldn't we be passing up assignments here
    return { block: exit };
}

function codeGenWhileStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:WhileStmt stmt) returns CodeGenError|StmtEffect {
    bir:BasicBlock loopHead = cx.createBasicBlock(); // where we go to on continue
    bir:BranchInsn branchToLoopHead = { dest: loopHead.label };
    startBlock.insns.push(branchToLoopHead);
    bir:BasicBlock loopBody = cx.createBasicBlock();
    bir:BasicBlock exit = cx.createBasicBlock();

    boolean exitReachable = false;
    var { result: condition, block: afterCondition } = check codeGenConditionalExpr(cx, loopHead, env, stmt.condition);
    bir:Insn branch;
    if condition is bir:Register {
        branch = <bir:CondBranchInsn>{ operand: condition, ifFalse: exit.label, ifTrue: loopBody.label };
        exitReachable = true;
    }
    else if condition is true {
        branch = <bir:BranchInsn>{ dest: loopBody.label };     
    }
    else if stmt.body.length() == 0 {
        // this is `while false { }`
        // need to put something in loopHead
        branch = <bir:BranchInsn> { dest: exit.label };
        exitReachable = true;
    }
    else {
        // condition is false and body is non-empty
        return cx.semanticErr("unreachable code");
    }
    afterCondition.insns.push(branch);
    cx.pushLoopContext(exit, loopHead);
    var { block: loopEnd, assignments } = check codeGenStmts(cx, loopBody, env, stmt.body);
    if !(loopEnd is ()) {
        loopEnd.insns.push(branchToLoopHead);
        check validLoopAssignments(cx, assignments);
    }
    check validLoopAssignments(cx, cx.onContinueAssignments());
    // We won't used these if the exit isn't reachable
    assignments.push(...cx.onContinueAssignments());
    assignments.push(...cx.onBreakAssignments());
    if cx.loopUsedBreak() {
        exitReachable = true;
    }
    cx.popLoopContext();
   
    if exitReachable {
        return { block: exit, assignments };
    }
    else {
        return { block: () };
    }
}

function validLoopAssignments(CodeGenContext cx, int[] assignments) returns CodeGenError? {
    foreach int r in assignments {
        if r < cx.loopStartRegister() {
            return cx.semanticErr(`assignment to narrowed variable ${<string>cx.registerVarName(r)} in loop`);
        }
    }
}

function codeGenBreakStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env) returns CodeGenError|StmtEffect {
    bir:Label dest = check cx.onBreakLabel();
    bir:BranchInsn branch = { dest };
    startBlock.insns.push(branch);
    cx.addOnBreakAssignments(env.assignments);
    return { block: () };
}

function codeGenContinueStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env) returns CodeGenError|StmtEffect {
    bir:Label dest = check cx.onContinueLabel();
    bir:BranchInsn branch = { dest };
    startBlock.insns.push(branch);
    cx.addOnContinueAssignments(env.assignments);
    return { block: () };
}

type ConstMatchValue record {|
    readonly SimpleConst value;
    readonly int clauseIndex;
|};

function codeGenMatchStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:MatchStmt stmt) returns CodeGenError|StmtEffect {
    final int startRegister = cx.nextRegisterNumber();
    int[] assignments = [];
    var { result: matched, block: testBlock, binding } = check codeGenExpr(cx, startBlock, env, check cx.foldExpr(env, stmt.expr, ()));
    // JBUG need parentheses
    t:SemType matchedType = matched is bir:Register ? (matched.semType) : t:singleton(matched);
    // we enforce that the wildcardClauseIndex is either () or the index of the last clause
    int? wildcardClauseIndex = ();
    table<ConstMatchValue> key(value) constMatchValues = table[];
    t:SemType[] clauseLooksLike = [];
    bir:InsnRef[][] clauseTestInsns = [];
    // union of all clause patterns preceding this one
    t:SemType precedingPatternsUnion = t:NEVER;
    bir:BasicBlock[] clauseBlocks = [];

    foreach int i in 0 ..< stmt.clauses.length() {
        var clause = stmt.clauses[i];
        clauseTestInsns[i] = [];
        clauseBlocks[i] = cx.createBasicBlock("clause." + i.toString());
        t:SemType clausePatternUnion = t:NEVER;
        foreach var pattern in clause.patterns {
            if pattern is s:ConstPattern {
                if wildcardClauseIndex != () && i > wildcardClauseIndex {
                    return cx.semanticErr("match pattern unmatchable because of previous wildcard match pattern", pos=pattern.pos);
                }
                s:Expr patternExpr = pattern.expr;
                // JBUG following line results in bad code
                // ConstValueExpr cv = <ConstValueExpr> check cx.foldExpr(env, patternExpr, matchedType);
                // Can see this on FVmatch03.bal
                // Should give TypeCastError panic but instead reports "duplicate const match pattern"
                // This longer former if a workaround
                var foldResult = cx.foldExpr(env, patternExpr, matchedType);
                s:ConstValueExpr cv;
                if foldResult is CodeGenError {
                    return foldResult;
                }
                else {
                    cv = <s:ConstValueExpr>foldResult;
                }
              
                ConstMatchValue mv = { value: cv.value, clauseIndex: i };
                if constMatchValues.hasKey(mv.value) {
                    return cx.semanticErr("duplicate const match pattern", pos=pattern.pos);
                }
                constMatchValues.add(mv);    
                if !t:containsConst(matchedType, cv.value) {
                    return cx.semanticErr("match pattern cannot match value of expression", pos=pattern.pos);
                }
                clausePatternUnion = t:union(clausePatternUnion, t:singleton(mv.value));
            }
            else {
                // `1|_ => {}` is pointless, but I'm not making it an error
                // because a later pattern that overlaps an earlier one is not in general an error
                if wildcardClauseIndex != () {
                    return cx.semanticErr("duplicate wildcard match pattern");
                }
                wildcardClauseIndex = i;
                clausePatternUnion = t:ANY;
            }
        }
        clauseLooksLike[i] = t:diff(clausePatternUnion, precedingPatternsUnion);
        precedingPatternsUnion = t:union(precedingPatternsUnion, clausePatternUnion);
    }
  
    int patternIndex = 0;
    foreach var mv in constMatchValues {
        int clauseIndex = mv.clauseIndex;
        if clauseIndex == wildcardClauseIndex {
            break;
        }
        bir:Register testResult = cx.createRegister(t:BOOLEAN);
        bir:EqualityInsn eq = { op: "==", result: testResult, operands: [matched, mv.value] };
        testBlock.insns.push(eq);
        clauseTestInsns[clauseIndex].push(bir:lastInsnRef(testBlock));
        bir:BasicBlock nextBlock = cx.createBasicBlock("pattern." + patternIndex.toString());
        patternIndex += 1;
        bir:CondBranchInsn condBranch = { operand: testResult, ifTrue: clauseBlocks[clauseIndex].label, ifFalse: nextBlock.label } ;
        testBlock.insns.push(condBranch);
        testBlock = nextBlock;
    }
    bir:BasicBlock? contBlock = ();
    foreach int clauseIndex in 0 ..< stmt.clauses.length() {
        s:MatchClause clause = stmt.clauses[clauseIndex];
        bir:BasicBlock stmtBlock = clauseBlocks[clauseIndex];
        Environment clauseEnv = env;
        // Do type narrowing
        if !(binding is ()) {
            bir:Result? basis = ();
            if clauseIndex == wildcardClauseIndex {
                bir:Result[] and = [];
                foreach int i in 0 ..< clauseIndex {
                    foreach var insn in clauseTestInsns[i] {
                        and.push({ result: false, insn });
                    }
                }
                // Degenerate case may have empty results
                if and.length() > 0 {
                    basis = { and: and.cloneReadOnly() };
                }
            }
            else {
                bir:Result[] or = [];
                foreach var insn in clauseTestInsns[clauseIndex] {
                    or.push({ result: true, insn });
                }
                basis = { or: or.cloneReadOnly() };
            }
            if basis != () {
                // Will need readOnlyIntersect when we have proper match patterns
                t:SemType narrowedType = t:intersect(matchedType, clauseLooksLike[clauseIndex]);
                clauseEnv = codeGenNarrowing(cx, stmtBlock, env, binding, narrowedType, basis);
            }
        } 
        var { block: stmtBlockEnd, assignments: blockAssignments } = check codeGenStmts(cx, stmtBlock, clauseEnv, clause.block);
        if stmtBlockEnd is () {
            continue;
        }
        else {
            bir:BasicBlock b = maybeCreateBasicBlock(cx, contBlock);
            contBlock = b;
            bir:BranchInsn branchToCont = { dest: b.label };
            stmtBlockEnd.insns.push(branchToCont);
            assignments.push(...blockAssignments);
        }
    }
    if wildcardClauseIndex != () {
        bir:BranchInsn branch = { dest: clauseBlocks[wildcardClauseIndex].label };
        testBlock.insns.push(branch);
        if contBlock is () {
            // all the clauses have a return or similar
            return { block: () };
        }
    }
    else {
        bir:BasicBlock b = maybeCreateBasicBlock(cx, contBlock);
        contBlock = b;
        bir:BranchInsn branch = { dest: b.label };
        testBlock.insns.push(branch);
    }            
    return { block: contBlock, assignments };
}

function maybeCreateBasicBlock(CodeGenContext cx, bir:BasicBlock? block) returns bir:BasicBlock {
    if block is () {
        return cx.createBasicBlock();
    }
    else {
        return block;
    }
}

function codeGenIfElseStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:IfElseStmt stmt) returns CodeGenError|StmtEffect {
    var { condition, ifTrue, ifFalse } = stmt;
    var { result: operand, block: branchBlock, narrowing } = check codeGenConditionalExpr(cx, startBlock, env, condition);
    if operand is boolean {
        s:Stmt[] taken;
        s:Stmt[] notTaken;
        if operand {
            taken = ifTrue;
            notTaken = ifFalse;
        }
        else {
            taken = ifFalse;
            notTaken = ifTrue;
        }
        if notTaken.length() > 0 {
            // XXX position should come from first member of notTaken
            return cx.semanticErr("unreachable code");
        }
        return codeGenStmts(cx, branchBlock, env, taken);
    }
    else {
        bir:BasicBlock ifBlock = cx.createBasicBlock();
        Environment ifEnv = narrowing is () ? env : codeGenIfElseNarrowing(cx, ifBlock, env, narrowing, true);
        var { block: ifContBlock, assignments } = check codeGenStmts(cx, ifBlock, ifEnv, ifTrue);
        bir:BasicBlock contBlock;
        if ifFalse.length() == 0 {
            // just an if branch
            contBlock = cx.createBasicBlock();
            bir:CondBranchInsn condBranch = { operand, ifTrue: ifBlock.label, ifFalse: contBlock.label };
            branchBlock.insns.push(condBranch);
            if !(ifContBlock is ()) {
                bir:BranchInsn branch = { dest: contBlock.label };
                ifContBlock.insns.push(branch);
            }
            return { block: contBlock, assignments };    
        }
        else {
            // an if and an else
            bir:BasicBlock elseBlock = cx.createBasicBlock();
            Environment elseEnv = narrowing is () ? env : codeGenIfElseNarrowing(cx, elseBlock, env, narrowing, false);
            var { block: elseContBlock, assignments: elseAssignments } = check codeGenStmts(cx, elseBlock, elseEnv, ifFalse);
            bir:CondBranchInsn condBranch = { operand, ifTrue: ifBlock.label, ifFalse: elseBlock.label };
            branchBlock.insns.push(condBranch);
            if ifContBlock is () && elseContBlock is () {
                // e.g. both arms have a return
                return { block: () };
            }
            contBlock = cx.createBasicBlock();
            bir:BranchInsn branch = { dest: contBlock.label };
            if !(ifContBlock is ()) {
                ifContBlock.insns.push(branch);
            }
            if !(elseContBlock is ()) {
                elseContBlock.insns.push(branch);
                assignments.push(...elseAssignments);
            }
            return { block: contBlock, assignments };
        }
    }
}

// JBUG changing `returns` to `return` makes the parse get a NPE during error recovery
function codeGenIfElseNarrowing(CodeGenContext cx, bir:BasicBlock bb, Environment env, Narrowing narrowing, boolean condition) returns Environment {
    boolean insnResult = condition == !narrowing.negated;
    // JBUG without parentheses this gets a parse error
    t:SemType narrowedType = insnResult ? (narrowing.ifTrue) : narrowing.ifFalse;
    if narrowedType === t:NEVER {
        panic err:impossible("narrowed to never type");
    }
    return codeGenNarrowing(cx, bb, env, narrowing.binding, narrowedType, { insn: narrowing.testInsn, result: insnResult });
}

function codeGenNarrowing(CodeGenContext cx, bir:BasicBlock bb, Environment env, Binding binding, t:SemType narrowedType, bir:Result basis) returns Environment {
    bir:Register narrowed = cx.createRegister(narrowedType, binding.name);
    bir:CondNarrowInsn insn = {
        result: narrowed,
        operand: binding.reg,
        basis
    };
    bb.insns.push(insn);
    Binding bindings = {
        name: binding.name,
        reg: narrowed,
        isFinal: binding.isFinal,
        prev: env.bindings,
        unnarrowed: unnarrowBinding(binding)
    };
    return { bindings, assignments: env.assignments };
}

function unnarrowBinding(Binding binding) returns Binding {
    Binding? unnarrowed = binding.unnarrowed;
    return  unnarrowed is () ? binding : unnarrowed;
}

function codeGenReturnStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:ReturnStmt stmt) returns CodeGenError|StmtEffect {
    var { returnExpr } = stmt;
    var { result: operand, block: nextBlock } = check codeGenExpr(cx, startBlock, env, check cx.foldExpr(env, returnExpr, cx.returnType));
    bir:RetInsn insn = { operand };
    nextBlock.insns.push(insn);
    return { block: () };
}

function codeGenVarDeclStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:VarDeclStmt stmt) returns CodeGenError|StmtEffect {
    var { varName, initExpr, td, isFinal } = stmt;
    if lookup(varName, env) !== () {
        return cx.semanticErr(`duplicate declaration of ${varName}`);
    }
    t:SemType semType = s:resolveInlineTypeDesc(td);
    initExpr = check cx.foldExpr(env, initExpr, semType);
    var { result: operand, block: nextBlock } = check codeGenExpr(cx, startBlock, env, initExpr);
    bir:Register result = cx.createRegister(semType, varName);
    bir:AssignInsn insn = { result, operand };
    nextBlock.insns.push(insn);
    return { block: nextBlock, bindings: { name: varName, reg: result, prev: env.bindings, isFinal } };  
}

function codeGenAssignStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:AssignStmt stmt) returns CodeGenError|StmtEffect {
    var { lValue, expr } = stmt;
    if lValue is s:VarRefExpr {
        return codeGenAssignToVar(cx, startBlock, env, lValue.varName, expr);
    }
    else {
        return codeGenAssignToMember(cx, startBlock, env, lValue, expr);
    }
}

function codeGenCompoundAssignStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:CompoundAssignStmt stmt) returns CodeGenError|StmtEffect {
    var { lValue, expr , op, pos } = stmt;
    s:Expr binExpr;
    if op is s:BinaryArithmeticOp {
        binExpr = { arithmeticOp: op, left: lValue, right: expr, pos: pos };
    }
    else {
        binExpr = { bitwiseOp: op, left: lValue, right: expr };
    }
    return codeGenAssignToVar(cx, startBlock, env, lValue.varName, binExpr);
}

function codeGenAssignToVar(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, string varName, s:Expr expr) returns CodeGenError|StmtEffect {
    Binding binding = check lookupVarRefBinding(cx, varName, env);
    if binding.isFinal {
        return cx.semanticErr(`cannot assign to ${varName}`);
    }
    bir:Register unnarrowedReg;
    int[] assignments;
    Binding? unnarrowedBinding = binding.unnarrowed;
    if unnarrowedBinding is () {
        // no narrowed binding in effect
        unnarrowedReg = binding.reg;
        assignments = [];
    }
    else {
        // invalidate the narrowed binding
        // and use the unnarrowed binding
        unnarrowedReg = unnarrowedBinding.reg;
        assignments = [ unnarrowedReg.number ];
    }
    s:Expr foldedExpr = check cx.foldExpr(env, expr, unnarrowedReg.semType);
    var { result: operand, block: nextBlock } = check codeGenExpr(cx, startBlock, env, foldedExpr);

    bir:AssignInsn assign = { result: unnarrowedReg, operand };
    nextBlock.insns.push(assign);
    return { block: nextBlock, assignments };
}

function codeGenAssignToMember(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:MemberAccessLExpr lValue, s:Expr expr) returns CodeGenError|StmtEffect {
    bir:Register reg = (check lookupVarRefBinding(cx, lValue.container.varName, env)).reg;
    s:Expr foldedIndexExpr = check cx.foldExpr(env, lValue.index, t:union(t:INT, t:STRING));
    var { result: index, block: nextBlock } = check codeGenExpr(cx, startBlock, env, foldedIndexExpr);
    s:Expr foldedExpr = check cx.foldExpr(env, expr, t:ANY); // XXX need to change when we have typed arrays
    bir:Operand operand;
    { result: operand, block: nextBlock } = check codeGenExpr(cx, nextBlock, env, foldedExpr);
    TypedOperand? t = typedOperand(index);
    bir:Insn insn;
    if t is ["int", bir:IntOperand] {
        insn = <bir:ListSetInsn>{ list: reg, index: t[1], operand, position: lValue.pos };
    }
    else if t is ["string", bir:StringOperand] {
        insn = <bir:MappingSetInsn> { operands: [ reg, t[1], operand], position: lValue.pos };
    }
    else {
        return cx.semanticErr("key in assignment to member must be int or string");
    }
    nextBlock.insns.push(insn);
    return { block: nextBlock };
}

function codeGenCallStmt(CodeGenContext cx, bir:BasicBlock startBlock, Environment env, s:CallStmt stmt) returns CodeGenError|StmtEffect {
    // stmt is FunctionCallExpr or s:MethodCallExpr
    bir:Register reg;
    bir:BasicBlock nextBlock;
    if stmt is s:FunctionCallExpr {
        { result: reg, block: nextBlock } = check codeGenFunctionCall(cx, <bir:BasicBlock>startBlock, env, stmt);
    }
    else {
        { result: reg, block: nextBlock } = check codeGenMethodCall(cx, <bir:BasicBlock>startBlock, env, stmt);
    }
    if reg.semType !== t:NIL {
        return cx.semanticErr("return type of function or method in call statement must be nil");
    }
    return { block: nextBlock };
}

function codeGenConditionalExpr(CodeGenContext cx, bir:BasicBlock block, Environment env, s:Expr expr) returns CodeGenError|BooleanExprEffect {
    return codeGenExprForBoolean(cx, block, env,  check cx.foldExpr(env, expr, t:BOOLEAN));
}

function codeGenExprForBoolean(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:Expr expr) returns CodeGenError|BooleanExprEffect {
    var { result, block, narrowing } = check codeGenExpr(cx, bb, env, expr);
    if result is bir:BooleanOperand {
        // rest of the type checking is in the verifier
        return { result, block, narrowing };
    }
    return cx.semanticErr("expected boolean operand");
}

function codeGenExprForInt(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:Expr expr) returns CodeGenError|IntExprEffect {
    var { result, block } = check codeGenExpr(cx, bb, env, expr);
    if result is bir:IntOperand {
        // rest of the type checking is in the verifier
        return { result, block };
    }
    return cx.semanticErr("expected integer operand");
}

function codeGenExpr(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:Expr expr) returns CodeGenError|ExprEffect {
    match expr {
        var { arithmeticOp: op, left, right, pos } => {
            var { result: l, block: block1 } = check codeGenExpr(cx, bb, env, left);
            var { result: r, block: nextBlock } = check codeGenExpr(cx, block1, env, right);
            TypedOperandPair? pair = typedOperandPair(l, r);
            bir:Register result;
            if pair is IntOperandPair {
                result = cx.createRegister(t:INT);
                bir:IntArithmeticBinaryInsn insn = { op, operands: pair[1], result, position: pos };
                bb.insns.push(insn);
            }
            else if pair is FloatOperandPair {
                result = cx.createRegister(t:FLOAT);
                bir:FloatArithmeticBinaryInsn insn = { op, operands: pair[1], result, position: pos };
                bb.insns.push(insn);
            }
            else if pair is StringOperandPair { // XXX a bug here
                result = cx.createRegister(t:STRING);
                bir:StringConcatInsn insn = { operands: pair[1], result };
                bb.insns.push(insn);
            }
            else {
                return cx.semanticErr(`${op} not supported for operand types`);
            }               
            return { result, block: nextBlock };
        }
        // Negation
        { op: "-",  operand: var o, pos: var pos } => {
            var { result: operand, block: nextBlock } = check codeGenExpr(cx, bb, env, o);
            TypedOperand? typed = typedOperand(operand);
            bir:Register result;
            bir:Insn insn;
            if typed is ["int", bir:IntOperand] {
                result = cx.createRegister(t:INT);
                insn = <bir:IntArithmeticBinaryInsn> { op: "-", operands: [0, typed[1]], result, position: pos };
            }
            else if typed is ["float", bir:FloatOperand] {
                result = cx.createRegister(t:FLOAT);
                insn = <bir:FloatNegateInsn> { operand: <bir:Register>typed[1], result };
            }
            else {
                return cx.semanticErr(`operand of ${"-"} must be int or float`);
            }
            bb.insns.push(insn);
            return { result, block: nextBlock };
        }
        // Bitwise complement 
        { op: "~",  operand: var o } => {
            var { result: operand, block: nextBlock } = check codeGenExprForInt(cx, bb, env, o);
            bir:Register result = cx.createRegister(t:INT);
            bir:IntBitwiseBinaryInsn insn = { op: "^", operands: [-1, operand], result };
            bb.insns.push(insn);
            return { result, block: nextBlock };
        }
        { op: "!",  operand: var o } => {
            var { result: operand, block: nextBlock, narrowing } = check codeGenExprForBoolean(cx, bb, env, o);
            // Should have been resolved during constant folding
            bir:Register reg = <bir:Register>operand;
            bir:Register result = cx.createRegister(t:BOOLEAN);
            bir:BooleanNotInsn insn = { operand: reg, result };
            bb.insns.push(insn);
            if !(narrowing is ()) {
                narrowing.negated = !narrowing.negated;
            }
            return { result, block: nextBlock, narrowing };
        }
        var { bitwiseOp: op, left, right } => {
            var { result: l, block: block1} = check codeGenExprForInt(cx, bb, env, left);
            var { result: r, block: nextBlock } = check codeGenExprForInt(cx, block1, env, right);
            t:SemType lt = bitwiseOperandType(l);
            t:SemType rt = bitwiseOperandType(l);
            t:SemType resultType = op == "&" ? t:intersect(lt, rt) : t:union(lt, rt);
            bir:Register result = cx.createRegister(resultType);
            bir:IntBitwiseBinaryInsn insn = { op, operands: [l, r], result };
            bb.insns.push(insn);
            return { result, block: nextBlock };
        }
        var { equalityOp: op, left, right } => {
            return codeGenEquality(cx, bb, env, op, left, right);
        }
        var { relationalOp: op, left, right } => {
            bir:Register result = cx.createRegister(t:BOOLEAN);
            var { result: l, block: block1 } = check codeGenExpr(cx, bb, env, left);
            var { result: r, block: nextBlock } = check codeGenExpr(cx, block1, env, right);
            bir:OrderType? ot = operandPairOrderType(l, r);
            if ot != () {
                bir:CompareInsn insn = { op, orderType: ot, operands: [l, r], result };
                bb.insns.push(insn);
                return { result, block: nextBlock };  
            }
            else {
                return cx.semanticErr("operands of relational operator do not belong to an ordered type");
            }               
        }
        var { td, operand: _ } => {
            // JBUG #31782 cast needed
            return codeGenTypeCast(cx, bb, env, <s:TypeCastExpr>expr);
        }
        // Type test
        var { td, left, semType, negated} => {
            return codeGenTypeTest(cx, bb, env, td, left, semType, negated);
        }
        // Variable reference
        var { varName } => {
            return codeGenVarRefExpr(cx, varName, env, bb);
        }
        // Constant
        // JBUG does not work as match pattern `var { value, multiSemType }`
        var cvExpr if cvExpr is s:ConstValueExpr => {
            return codeGenConstValue(cx, bb, env, cvExpr);
        }
        var floatZeroExpr if floatZeroExpr is s:FloatZeroExpr => {
            return codeGenExpr(cx, bb, env, floatZeroExpr.expr);
        }
        // Function/method call
        var callExpr if callExpr is (s:FunctionCallExpr|s:MethodCallExpr) => {
            if callExpr is s:FunctionCallExpr {
                return codeGenFunctionCall(cx, bb, env, callExpr);
            }
            else {
                return codeGenMethodCall(cx, bb, env, callExpr);
            }
        }
        // Member access E[i]
        var { container, index, pos } => {
            // Do constant folding here since these expressions are not allowed in const definitions
            var { result: l, block: block1 } = check codeGenExpr(cx, bb, env, check cx.foldExpr(env, container, ()));
            var { result: r, block: nextBlock } = check codeGenExpr(cx, block1, env, check cx.foldExpr(env, index, t:union(t:INT, t:STRING)));
            if l is bir:Register {
                bir:Register result = cx.createRegister(t:ANY);
                TypedOperand? k = typedOperand(r);
                bir:Insn insn;
                if k is ["int", bir:IntOperand] {
                    insn = <bir:ListGetInsn>{ result, list: l, operand: k[1], position: pos };
                }
                else if k is ["string", bir:StringOperand] {
                    insn = <bir:MappingGetInsn>{ result, operands: [l, k[1]] };
                }
                else {
                    return cx.semanticErr("member access key must be a string or an int");
                }
                bb.insns.push(insn);
                return { result, block: nextBlock };
            }
            else {
                return cx.semanticErr("cannot apply member access to constant of simple type");
            }
        }
        // List construct
        var { members } => {
            return codeGenListConstructor(cx, bb, env, members);  
        }
        // Mapping construct
        var { fields } => {
            return codeGenMappingConstructor(cx, bb, env, fields);  
        }
        var { digits } => {
            panic err:impossible(`failed to fold int literal ${digits}`);
        }
    }
    panic err:impossible("unrecognized expression type in code gen: " +  s:exprToString(expr));
}

function codeGenListConstructor(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:Expr[] members) returns CodeGenError|ExprEffect {
    bir:BasicBlock nextBlock = bb;
    bir:Operand[] operands = [];
    foreach var member in members {
        bir:Operand operand;
        { result: operand, block: nextBlock } = check codeGenExpr(cx, nextBlock, env, member);
        operands.push(operand);
    }
    // In subset 3, we have only mutable lists of any
    // We will have to do more work in future subsets to determine the types here
    bir:Register result = cx.createRegister(t:LIST_RW);
    bir:ListConstructInsn insn = { operands: operands.cloneReadOnly(), result };
    nextBlock.insns.push(insn);
    return { result, block: nextBlock };
}

function codeGenMappingConstructor(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:Field[] fields) returns CodeGenError|ExprEffect {
    bir:BasicBlock nextBlock = bb;
    bir:Operand[] operands = [];
    string[] fieldNames= [];
    map<s:Position> fieldPos = {};
    foreach var { pos, name, value } in fields {
        s:Position? prevPos = fieldPos[name];
        if prevPos == () {
            fieldPos[name] = pos;
        }
        else {
            return cx.semanticErr(`duplicate field ${name}`, pos=pos);
        }
        bir:Operand operand;
        { result: operand, block: nextBlock } = check codeGenExpr(cx, nextBlock, env, value);
        operands.push(operand);
        fieldNames.push(name);
    }
    bir:Register result = cx.createRegister(t:MAPPING_RW);
    bir:MappingConstructInsn insn = { fieldNames: fieldNames.cloneReadOnly(), operands: operands.cloneReadOnly(), result };
    nextBlock.insns.push(insn);
    return { result, block: nextBlock };
}

function codeGenConstValue(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:ConstValueExpr cvExpr) returns CodeGenError|ExprEffect {
    t:SemType? multiSemType = cvExpr.multiSemType;
    SimpleConst value = cvExpr.value;
    if multiSemType is () {
        return { result: value, block: bb };
    }
    else {
        bir:Register reg = cx.createRegister(multiSemType);
        bir:AssignInsn insn = { operand: value, result: reg };
        bb.insns.push(insn);
        return { result: reg, block: bb };
    }
}

function codeGenEquality(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:BinaryEqualityOp op, s:Expr left, s:Expr right) returns CodeGenError|ExprEffect {
    bir:Register result = cx.createRegister(t:BOOLEAN);
    var { result: l, block: block1, binding: lBinding } = check codeGenExpr(cx, bb, env, left);
    var { result: r, block: nextBlock, binding: rBinding } = check codeGenExpr(cx, block1, env, right);
    // Type checking is done in the verifier
    bir:EqualityInsn insn = { op, operands: [l, r], result };
    bb.insns.push(insn);
    [Binding, SimpleConst]? narrowingCompare = ();
    if op is ("=="|"!=") {
        if lBinding is Binding && r is SimpleConst {
            narrowingCompare = [lBinding, r];
        }
        else if rBinding is Binding && l is SimpleConst {
            narrowingCompare = [rBinding, l];
        }
    }
    if narrowingCompare is () {
        return { result, block: nextBlock };
    }
    else {
        var [binding, value] = narrowingCompare;
        t:SemType ifTrue = t:singleton(value);
        t:SemType ifFalse = t:diff(binding.reg.semType, ifTrue);
        if (<string>op).startsWith("!") {
            [ifTrue, ifFalse] = [ifFalse, ifTrue];
        }
        Narrowing narrowing = {
            binding,
            ifTrue,
            ifFalse,
            testInsn: bir:lastInsnRef(bb),
            negated: false
        };
        return { result, block: nextBlock, narrowing };
    }
}

function codeGenVarRefExpr(CodeGenContext cx, string name, Environment env, bir:BasicBlock bb) returns CodeGenError|ExprEffect{
    var v = check lookupVarRef(cx, name, env);
    bir:Operand result;
    Binding? binding;
    if v is t:Value {
        result = v.value;
        binding = ();
    }
    else {
        result = v.reg;
        binding = v;
    }
    return { result, block: bb, binding };
}

function codeGenTypeCast(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:TypeCastExpr tcExpr) returns CodeGenError|ExprEffect {
    var { result: operand, block: nextBlock } = check codeGenExpr(cx, bb, env, tcExpr.operand);
    var reg = <bir:Register>operand; // const folding should have got rid of the const
    t:SemType toType = tcExpr.semType;
    t:UniformTypeBitSet? toNumType = t:singleNumericType(toType);
    if toNumType != () && !t:isSubtypeSimple(t:intersect(reg.semType, t:NUMBER), toNumType) {
        toType = t:diff(toType, t:diff(t:NUMBER, toNumType));

        bir:Register result = cx.createRegister(t:union(t:diff(reg.semType, t:NUMBER), toNumType));
        if toNumType == t:INT {
            bir:ConvertToIntInsn insn = { operand: reg, result, position: tcExpr.pos };
            nextBlock.insns.push(insn);
        }
        else if toNumType == t:FLOAT {
            bir:ConvertToFloatInsn insn = { operand: reg, result };
            nextBlock.insns.push(insn);
        }
        else {
            panic err:impossible("convert to decimal");
        }
        reg = result;
    }
    if t:isSubtype(cx.mod.tc, reg.semType, toType) {
        // it's redundant, so we can remove it
        return { result: reg, block: nextBlock };
    }
    t:SemType resultType = t:intersect(reg.semType, toType);
    bir:Register result = cx.createRegister(resultType);
    bir:TypeCastInsn insn = { operand: reg, semType: toType, position: tcExpr.pos, result };
    nextBlock.insns.push(insn);
    return { result, block: nextBlock };
}

function codeGenTypeTest(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:TypeDesc td, s:Expr left, t:SemType semType, boolean negated) returns CodeGenError|ExprEffect {
    var { result: operand, block: nextBlock, binding } = check codeGenExpr(cx, bb, env, left);
    // Constants should be resolved during constant folding
    bir:Register reg = <bir:Register>operand;        
    t:SemType diff = t:diff(reg.semType, semType);
    if t:isEmpty(cx.mod.tc, diff) {
        return { result: !negated, block: bb };
    }
    t:SemType intersect = t:intersect(reg.semType, semType);
    if t:isEmpty(cx.mod.tc, intersect) {
        return { result: negated, block: bb };
    }
    bir:Register result = cx.createRegister(t:BOOLEAN);
    bir:TypeTestInsn insn = { operand: reg, semType, result, negated};
    bb.insns.push(insn);
    if negated {
        [intersect, diff] = [diff, intersect];
    }
    Narrowing? narrowing = ();
    if !(binding is ()) {
        narrowing = {
            binding,
            ifTrue: intersect,
            ifFalse: diff,
            testInsn: bir:lastInsnRef(bb)
        };
    }
    return { result, block: nextBlock, narrowing };   
}

function codeGenFunctionCall(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:FunctionCallExpr expr) returns CodeGenError|RegExprEffect {
    string? prefix = expr.prefix;
    bir:FunctionRef func;
    if prefix is () {
        func =  check genLocalFunctionRef(cx, env, expr.funcName);
    }
    else {
        func = check genImportedFunctionRef(cx, env, prefix, expr.funcName);
    }
    check validArgumentCount(cx, func, expr.args.length(), expr.pos);
    t:SemType[] paramTypes = func.signature.paramTypes;
    bir:BasicBlock curBlock = bb;
    bir:Operand[] args = [];
    foreach int i in 0 ..< expr.args.length() {
        s:Expr argExpr = check cx.foldExpr(env, expr.args[i], paramTypes[i]);
        var { result: arg, block: nextBlock } = check codeGenExpr(cx, curBlock, env, argExpr);
        curBlock = nextBlock;
        args.push(arg);
    }
    bir:Register result = cx.createRegister(func.signature.returnType);
    bir:CallInsn call = {
        func,
        result,
        args: args.cloneReadOnly(),
        position: expr.pos
    };
    curBlock.insns.push(call);
    return { result, block: curBlock };
}

function codeGenMethodCall(CodeGenContext cx, bir:BasicBlock bb, Environment env, s:MethodCallExpr expr) returns CodeGenError|RegExprEffect {
    var { result: target, block: curBlock } = check codeGenExpr(cx, bb, env, check cx.foldExpr(env, expr.target, ()));
    bir:FunctionRef func = check getLangLibFunctionRef(cx, target, expr.methodName);
    check validArgumentCount(cx, func, expr.args.length() + 1, expr.pos);

    t:SemType[] paramTypes = func.signature.paramTypes;
    bir:Operand[] args = [target];
    foreach int i in 0 ..< expr.args.length() {
        s:Expr argExpr = check cx.foldExpr(env, expr.args[i], paramTypes[i + 1]);
        var { result: arg, block: nextBlock } = check codeGenExpr(cx, curBlock, env, argExpr);
        curBlock = nextBlock;
        args.push(arg);
    }
    bir:Register result = cx.createRegister(func.signature.returnType);
    bir:CallInsn call = {
        func,
        result,
        args: args.cloneReadOnly(),
        position: expr.pos
    };
    curBlock.insns.push(call);
    return { result, block: curBlock };
}

function validArgumentCount(CodeGenContext cx, bir:FunctionRef func, int nSuppliedArgs, s:Position pos) returns CodeGenError? {
    int nExpectedArgs = func.signature.paramTypes.length();
    if nSuppliedArgs == nExpectedArgs {
        return ();
    }
    string name = bir:symbolToString(cx.mod, func.symbol);
    if nSuppliedArgs < nExpectedArgs {
        return cx.semanticErr(`too few arguments for call to function ${name}`);
    }
    else {
        return cx.semanticErr(`too many arguments for call to function ${name}`);
    }
}

function genLocalFunctionRef(CodeGenContext cx, Environment env, string identifier) returns bir:FunctionRef|CodeGenError {
    if !(lookup(identifier, env) is ()) {
        return err:unimplemented("local variables cannot yet have function type");
    }
    bir:FunctionSignature signature;
    s:ModuleLevelDefn? defn = cx.mod.defns[identifier];
    if defn is s:FunctionDefn {
        signature = <bir:FunctionSignature>defn.signature;
        boolean isPublic = defn.vis == "public";
        bir:InternalSymbol symbol = { identifier, isPublic };
        return { symbol, signature };
    }
    else {
        err:Message msg;
        if defn is () {
            msg = `${identifier} is not defined`;
        }
        else {
            msg = `${identifier} is not a function`;
        }
        return cx.semanticErr(msg);
    }  
}

function genImportedFunctionRef(CodeGenContext cx, Environment env, string prefix, string identifier) returns bir:FunctionRef|CodeGenError {
    bir:ModuleId? moduleId = cx.mod.imports[prefix];
    if moduleId is () {
        return cx.semanticErr(`no import declaration for prefix ${prefix}`);
    }
    else {
        bir:FunctionSignature? signature = getLibFunction(moduleId, identifier);
        if signature is () {
            return err:unimplemented(`unsupported library function ${prefix}:${identifier}`);
        }
        else {
            bir:ExternalSymbol symbol = { module: moduleId, identifier };
            return { symbol, signature };
        }
    }
}

type LangLibModuleName "int"|"boolean"|"string"|"array"|"map";

function getLangLibFunctionRef(CodeGenContext cx, bir:Operand target, string methodName) returns bir:FunctionRef|CodeGenError {
    TypedOperand? t = typedOperand(target);
    if !(t is ()) && t[0] is LangLibModuleName {
        bir:ModuleId moduleId = { organization: "ballerina", names: ["lang", t[0]] };
        bir:FunctionSignature? signature = getLibFunction(moduleId, methodName);
        if signature is () {
            return err:unimplemented(`unrecognized lang library function ${t[0] + ":" + methodName}`);
        }
        else {
            bir:ExternalSymbol symbol = { module: moduleId, identifier: methodName };
            return { symbol, signature }; 
        }
    }
    return err:unimplemented(`cannot resolve ${methodName} to lang lib function`);
}

function lookupVarRefBinding(CodeGenContext cx, string name, Environment env) returns Binding|CodeGenError {
    var b = check lookupVarRef(cx, name, env);
    if b is Binding {
        return b;
    }
    else {
        return cx.semanticErr(`cannot refer to a const definition in an lvalue`);
    }
}

function lookupVarRef(CodeGenContext cx, string name, Environment env) returns t:Value|Binding|CodeGenError {
    Binding? binding = lookupLocalVarRef(cx, name, env);
    if binding is () {
        s:ModuleLevelDefn? defn = cx.mod.defns[name];
        if defn is () {
            return cx.semanticErr(`variable ${name} not defined`);
        }
        else if defn is s:ConstDefn {
            return (<s:ResolvedConst>defn.resolved)[1];
        }
        else {
            return cx.unimplementedErr(`values of function type not yet implemented`);
        }
    }
    else {
        return binding;
    }
}

function lookupLocalVarRef(CodeGenContext cx, string name, Environment env) returns Binding? {
    Binding? binding = lookup(name, env);
    if !(binding is ()) {
        Binding? unnarrowed = binding.unnarrowed;
        if !(unnarrowed is ()) {
            // This is a narrowed binding
            int num = unnarrowed.reg.number;
            if env.assignments.indexOf(num) != () {
                // This binding has been invalidated by an assignment
                return unnarrowed;
            }
        }
    }
    return binding;
}

function lookup(string name, Environment env) returns Binding? {
    Binding? tem = env.bindings;
    while true {
        if tem is () {
            break;
        }
        else if tem.name == name {
            return tem;
        }
        else {
            tem = tem.prev;
        }
    }
    return ();
}

function bitwiseOperandType(bir:IntOperand operand) returns t:SemType {
    t:SemType t;
    if operand is int {
        t = t:intConst(operand);
    }
    else {
        t = operand.semType;
    }
    return t:widenUnsigned(t);
}

type NilOperand ()|bir:Register;
type BooleanOperandPair readonly & ["boolean", [bir:BooleanOperand, bir:BooleanOperand]];
type IntOperandPair readonly & ["int", [bir:IntOperand, bir:IntOperand]];
type FloatOperandPair readonly & ["float", [bir:FloatOperand, bir:FloatOperand]];
type StringOperandPair readonly & ["string", [bir:StringOperand, bir:StringOperand]];
type NilOperandPair readonly & ["nil", [NilOperand, NilOperand]];

type TypedOperandPair BooleanOperandPair|IntOperandPair|FloatOperandPair|StringOperandPair|NilOperandPair;

// XXX should use t:UT_* instead of strings here (like the ordering stuff)
type TypedOperand readonly & (["array", bir:Register]
                              |["map", bir:Register]
                              |["string", bir:StringOperand]
                              |["float", bir:FloatOperand]
                              |["int", bir:IntOperand]
                              |["boolean", bir:BooleanOperand]
                              |["nil", NilOperand]);

function typedOperandPair(bir:Operand lhs, bir:Operand rhs) returns TypedOperandPair? {
    TypedOperand? l = typedOperand(lhs);
    TypedOperand? r = typedOperand(rhs);
    if l is ["int", bir:IntOperand] && r is ["int", bir:IntOperand] {
        return ["int", [l[1], r[1]]];
    }
    if l is ["float", bir:FloatOperand] && r is ["float", bir:FloatOperand] {
        return ["float", [l[1], r[1]]];
    }
    if l is ["string", bir:StringOperand] && r is ["string", bir:StringOperand] {
        return ["string", [l[1], r[1]]];
    }
    if l is ["boolean", bir:BooleanOperand] && r is ["boolean", bir:BooleanOperand] {
        return ["boolean", [l[1], r[1]]];
    }
    if l is ["nil", NilOperand] && r is ["int", NilOperand] {
        return ["nil", [l[1], r[1]]];
    }
    return ();
}

function typedOperand(bir:Operand operand) returns TypedOperand? {
    if operand is bir:Register {
        if operand.semType === t:NIL {
            return ["nil", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:BOOLEAN) {
            return ["boolean", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:INT) {
            return ["int", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:FLOAT) {
            return ["float", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:STRING) {
            return ["string", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:LIST) {
            return ["array", operand];
        }
        else if t:isSubtypeSimple(operand.semType, t:MAPPING) {
            return ["map", operand];
        }
    }
    else if operand is string {
        return ["string", operand];
    }
    else if operand is int {
        return ["int", operand];
    }
    else if operand is float {
        return ["float", operand];
    }
    else if operand is boolean {
        return ["boolean", operand];
    }
    else {
        return ["nil", operand];
    }
    return ();
}

function operandPairOrderType(bir:Operand left, bir:Operand right) returns bir:OrderType? {
    if operandIsNil(left) {
        return promoteToOptOrderType(operandOrderType(right));
    }
    if operandIsNil(right) {
        return promoteToOptOrderType(operandOrderType(left));
    }
    bir:OrderType? lot = operandOrderType(left);
    bir:OrderType? rot = operandOrderType(right);
    if lot == rot {
        return lot;
    }
    lot = promoteToOptOrderType(lot);
    rot = promoteToOptOrderType(rot);
    if lot == rot {
        return lot;
    }
    return ();
}

function promoteToOptOrderType(bir:OrderType? ot) returns bir:OrderType? {
    if ot == () || ot is bir:OptOrderType {
        return ot;
    }
    else {
        return { opt: ot };
    }
}

function operandIsNil(bir:Operand operand) returns boolean {
    if operand is bir:Register {
        return operand.semType === t:NIL;
    }
    else {
        return operand is ();
    }
}

final readonly & bir:UniformOrderType[] UNIFORM_ORDER_TYPES = [t:UT_BOOLEAN, t:UT_INT, t:UT_FLOAT, t:UT_STRING];

function operandOrderType(bir:Operand operand) returns bir:OrderType? {
    if operand is bir:Register {
        t:SemType operandTy = operand.semType;
        if operandTy === t:NIL {
            return ();
        }
        foreach bir:UniformOrderType tc in UNIFORM_ORDER_TYPES {
            t:UniformTypeBitSet optBasicType = t:uniformTypeUnion((1 << tc) | (1 << t:UT_NIL));
            if t:isSubtypeSimple(operandTy, optBasicType) {
                t:UniformTypeBitSet basicType = t:uniformType(tc);
                if t:isSubtypeSimple(operandTy, basicType) {
                    return tc;
                }
                return <bir:OptOrderType> { opt: tc };
            }
        }
    }
    else {
        var tc = t:constUniformTypeCode(operand);
        if tc is bir:UniformOrderType {
            return tc;
        }
    }
    return ();
}
