/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.model.builder;


/**
 * {@link BLangExecutionFlowBuilder} builds a linked Node list for non-blocking execution.
 *
 */
public class BLangExecutionFlowBuilder { //implements NodeVisitor {

//    private boolean nonblockingEnabled = false;
//
//    // Iterate/While Statement stack related to break.
//    private Stack<WhileStmt> loopingStack;
//    // Function/Action Statement stack related to break.
//    private Stack<BlockStmt> returningBlockStmtStack;
//    private Stack<OffSetCounter> offSetCounterStack;
//    private Stack<Statement> abortTransactionStack;
//    private Resource currentResource;

    public BLangExecutionFlowBuilder() {
//        loopingStack = new Stack<>();
//        returningBlockStmtStack = new Stack<>();
//        offSetCounterStack = new Stack<>();
//        offSetCounterStack.push(new OffSetCounter());
//        abortTransactionStack =  new Stack<>();
//        nonblockingEnabled = ModeResolver.getInstance().isNonblockingEnabled();
    }
//
//    @Override
//    public void visit(BLangProgram bLangProgram) {
//    }
//
//    @Override
//    public void visit(BLangPackage bLangPackage) {
//    }
//
//    @Override
//    public void visit(BallerinaFile bFile) {
//    }
//
//    @Override
//    public void visit(ImportPackage importPkg) {
//    }
//
//    @Override
//    public void visit(ConstDef constant) {
//    }
//
//    @Override
//    public void visit(GlobalVariableDef globalVar) {
//
//    }
//
//    /**
//     * Visit Service.
//     *
//     * @param service Service Instance.
//     */
//    @Override
//    public void visit(Service service) {
//        if (!nonblockingEnabled) {
//            return;
//        }
//        // Visit all of resources in a service
//        for (Resource resource : service.getResources()) {
//            resource.accept(this);
//        }
//    }
//
//    @Override
//    public void visit(BallerinaConnectorDef connector) {
//    }
//
//    /**
//     * Visit Resource and Starting building execution flow.
//     *
//     * @param resource Resource instance to parse
//     */
//    @Override
//    public void visit(Resource resource) {
//        // Resource is a Staring point. Calculate parent from here.
//        clearBranchingStacks();
//        // Add offset counter for current Stack Frame.
//        offSetCounterStack.push(new OffSetCounter());
//        currentResource = resource;
//        // This is a execution Start point. Hence link Block Statement with StartNode
//        BlockStmt blockStmt = resource.getResourceBody();
//        blockStmt.setParent(new StartNode(StartNode.Originator.RESOURCE));
//        // Visit Block Statement and ask it to handle its children.
//        blockStmt.accept(this);
//        resource.setTempStackFrameSize(offSetCounterStack.pop().getCount());
//    }
//
//    @Override
//    public void visit(BallerinaFunction function) {
//        if (!nonblockingEnabled || function.isFlowBuilderVisited()) {
//            return;
//        }
//        clearBranchingStacks();
//        BlockStmt blockStmt = function.getCallableUnitBody();
//        // This is a execution Start point. Hence link Block Statement with StartNode
//        blockStmt.setParent(new StartNode(StartNode.Originator.MAIN_FUNCTION));
//        // Visit Block Statement and ask it to handle its children.
//        returningBlockStmtStack.push(blockStmt);
//        offSetCounterStack.push(new OffSetCounter());
//        function.setFlowBuilderVisited(true);
//        blockStmt.accept(this);
//        function.setTempStackFrameSize(offSetCounterStack.pop().getCount());
//        returningBlockStmtStack.pop();
//    }
//
//    @Override
//    public void visit(BTypeMapper typeMapper) {
//    }
//
//    @Override
//    public void visit(BallerinaAction action) {
//    }
//
//    @Override
//    public void visit(Worker worker) {
//    }
//
//    @Override
//    public void visit(AnnotationAttachment annotation) {
//    }
//
//    @Override
//    public void visit(ParameterDef parameterDef) {
//    }
//
//    @Override
//    public void visit(VariableDef variableDef) {
//    }
//
//    @Override
//    public void visit(StructDef structDef) {
//    }
//
//    @Override
//    public void visit(AnnotationAttributeDef annotationAttributeDef) {
//
//    }
//
//    @Override
//    public void visit(AnnotationDef annotationDef) {
//
//    }
//
//
//    @Override
//    public void visit(VariableDefStmt varDefStmt) {
//        // Flow : AssignStmt -> (RHS Expr) -> LHS Expr(s) -> AssignStmtEndNode -> AssignStmt.nextSibling. -> ...
//        Expression rExpr = varDefStmt.getRExpr();
//        Expression lExpr = varDefStmt.getLExpr();
//        if (rExpr != null) {
//            varDefStmt.setNext(rExpr);
//            rExpr.setParent(varDefStmt);
//        }
//        // Link Last expression with VariableDefStmtEndNode to perform the variable assignment. .
//        VariableDefStmtEndNode endNode = new VariableDefStmtEndNode(varDefStmt);
//        // Array,Map,Struct Field Access Expressions can have sub expressions to calculate index.
//        if (lExpr instanceof ArrayMapAccessExpr || lExpr instanceof FieldAccessExpr) {
//            if (rExpr == null) {
//                varDefStmt.setNext(lExpr);
//            } else {
//                rExpr.setNextSibling(lExpr);
//            }
//            lExpr.setParent(varDefStmt);
//            lExpr.setNext(endNode);
//        } else {
//            // Else is variableRef expression, we don't need to visit them.
//            if (rExpr == null) {
//                varDefStmt.setNext(endNode);
//            } else {
//                rExpr.setNextSibling(endNode);
//            }
//        }
//        // Ask children to find their next by visiting them-self.
//        if (rExpr != null) {
//            rExpr.accept(this);
//        }
//        // LHS variableRef expressions are not required to visit.
//        if (lExpr instanceof ArrayMapAccessExpr || lExpr instanceof FieldAccessExpr) {
//            lExpr.accept(this);
//        }
//        endNode.setNext(findNext(varDefStmt));
//    }
//
//    @Override
//    public void visit(AssignStmt assignStmt) {
//        // Flow : AssignStmt -> RHS Expr -> LHS Expr(s) -> AssignStmtEndNode -> AssignStmt.nextSibling. -> ...
//        Expression rExpr = assignStmt.getRExpr();
//        assignStmt.setNext(rExpr);
//        rExpr.setParent(assignStmt);
//
//        // Linking Siblings
//        LinkedNode previous = rExpr;
//        Expression[] lExprs = assignStmt.getLExprs();
//        for (Expression expression : lExprs) {
//            // Array,Map,Struct Field Access Expressions can have sub expressions to calculate index.
//            if (expression instanceof ArrayMapAccessExpr || expression instanceof FieldAccessExpr) {
//                expression.setParent(assignStmt);
//                previous.setNextSibling(expression);
//                previous = expression;
//            }
//            // Else is variableRef expression, we don't need to visit them.
//        }
//        // Link Last expression with AssignmentEndNode to perform the assignment.
//        AssignStmtEndNode endNode = new AssignStmtEndNode(assignStmt);
//        previous.setNextSibling(endNode);
//
//        // Ask children to find their next by visiting them-self.
//        rExpr.accept(this);
//        // LHS variableRef expressions are not required to visit.
//        Arrays.stream(lExprs).filter(expression -> expression instanceof ArrayMapAccessExpr
//                || expression instanceof FieldAccessExpr).forEach(expression -> expression.accept(this));
//        // Finally find AssignmentStat end node's next.
//        endNode.setNext(findNext(assignStmt));
//    }
//
//    @Override
//    public void visit(BlockStmt blockStmt) {
//        if (blockStmt.next != null) {
//            // Already visited.
//            return;
//        }
//        // All Children of a Block Statement are Statements and they don't have temp values.
//        if (blockStmt.getStatements() == null || blockStmt.getStatements().length == 0
//                || (blockStmt.getStatements().length == 1 && blockStmt.getStatements()[0] instanceof CommentStmt)) {
//            // Flow :   BlockStmt -> BlockStmt's next sibling. -> ...
//            blockStmt.setNext(findNext(blockStmt));
//            return;
//        } else {
//            // Flow :   BlockStmt -> BlockStmt.childStmt[0] -> ...
//            blockStmt.setNext(blockStmt.getStatements()[0]);
//        }
//        // Link Parent and Next Sibling.
//        Statement statement;
//        for (int i = 0; i < blockStmt.getStatements().length; i++) {
//            statement = blockStmt.getStatements()[i];
//            statement.setParent(blockStmt);
//            if (i == blockStmt.getStatements().length - 1) {
//                // Tail child doesn't have a next Sibling.
//                break;
//            }
//            statement.setNextSibling(blockStmt.getStatements()[i + 1]);
//        }
//        // Visiting Block Statement children to ask them, link their next.
//        Arrays.stream(blockStmt.getStatements()).forEach(stmt -> stmt.accept(this));
//    }
//
//    @Override
//    public void visit(CommentStmt commentStmt) {
//        commentStmt.setNext(findNext(commentStmt));
//    }
//
//    @Override
//    public void visit(IfElseStmt ifElseStmt) {
//        // Check (Link) If condition first. (Conditions are validate at runtime)
//        // Flow : ifElseStmt -> IfCondition -> IfElseNode(true) -> ThenBlock -> ifElseStmt.nextSibling -> ...
//        Expression condition = ifElseStmt.getCondition();
//        ifElseStmt.setNext(condition);
//        condition.setParent(ifElseStmt);
//
//        // Conditional Branching, Handle IF Block
//        IfElseNode branch = new IfElseNode(condition);
//        condition.setNextSibling(branch);
//        branch.setParent(ifElseStmt);
//        branch.setNext(ifElseStmt.getThenBody());
//        ifElseStmt.getThenBody().setParent(ifElseStmt);
//
//        // Handle Else-IF Blocks
//        IfElseNode previous = branch;
//        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
//
//            // Link Else-If Conditions to previous's falseNext branch.
//            // Flow : ... -> previous: IfElseNode(false)-> ElseIfCondition-> IFElseNode(true)-> ElseIfBlock ->
//            //                                                                           ifElseStmt.nextSibling -> ...
//            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
//            previous.setNextIfFalse(elseIfCondition);
//
//            //  New Conditional Branch to handle Else-If Block.
//            IfElseNode newBranch = new IfElseNode(elseIfCondition);
//            elseIfCondition.setParent(ifElseStmt);
//            elseIfCondition.setNextSibling(newBranch);
//            newBranch.setParent(ifElseStmt);
//            newBranch.setNext(elseIfBlock.getElseIfBody());
//            elseIfBlock.getElseIfBody().setParent(ifElseStmt);
//
//            previous = newBranch;
//        }
//
//        // Handle Else Block. Set it to previous's falseNext.
//        // Flow : ... -> previous: IfElseNode(false)-> ElseBlock -> ifElseStmt.nextSibling -> ...
//        if (ifElseStmt.getElseBody() != null) {
//            previous.setNextIfFalse(ifElseStmt.getElseBody());
//            ifElseStmt.getElseBody().setParent(ifElseStmt);
//        } else {
//            // No Else Condition. So falseNext is ifElseStmt Next's.
//            previous.setNextIfFalse(findNext(ifElseStmt));
//        }
//
//        // Visiting child in order.
//        ifElseStmt.getCondition().accept(this);
//        ifElseStmt.getThenBody().accept(this);
//        Arrays.asList(ifElseStmt.getElseIfBlocks()).forEach(block -> block.getElseIfCondition().accept(this));
//        Arrays.stream(ifElseStmt.getElseIfBlocks()).forEach(block -> block.getElseIfBody().accept(this));
//        if (ifElseStmt.getElseBody() != null) {
//            ifElseStmt.getElseBody().accept(this);
//        }
//    }
//
//    @Override
//    public void visit(ReplyStmt replyStmt) {
//        // Reply Statement represent a end statement of a worker. This should always end with a EndNode.
//        // Flow: ReplyStmt -> (ReplyExpr) -> EndNode.
//        if (currentResource != null) {
//            // TODO : Fix this with Worker Concept.
//            Expression replyExpr = replyStmt.getReplyExpr();
//            if (replyExpr != null) {
//                // Reply with message.
//                ReplyStmtEndNode endNode = new ReplyStmtEndNode(replyStmt);
//                replyExpr.setParent(replyStmt);
//                replyExpr.setNextSibling(endNode);
//                replyStmt.setNext(replyExpr);
//                replyExpr.accept(this);
//                endNode.setNext(new EndNode());
//            } else {
//                // Reply without message.
//                replyStmt.setNext(new EndNode());
//            }
//        } else {
//            throw new FlowBuilderException(replyStmt.getNodeLocation().getFileName() + ":" +
//                    replyStmt.getNodeLocation().getLineNumber() + " reply is not allowed here.");
//        }
//    }
//
//    @Override
//    public void visit(ReturnStmt returnStmt) {
//        // Return Stmt's next is always jump out from the current callable unit.
//        // Hence we need to find who is my current callable unit's blockStamt.
//        // Flow : ReturnStmt -> FunctionInvocationExpr.nextSibling (or parent) -> ...
//        BlockStmt peek = returningBlockStmtStack.peek();
//        // Link Return statements expressions.
//        Expression[] exprs = returnStmt.getExprs();
//        if (exprs != null && exprs.length > 0) {
//            returnStmt.setNext(exprs[0]);
//            LinkedNode previous = null;
//            for (Expression expression : exprs) {
//                expression.setParent(returnStmt);
//                if (previous != null) {
//                    previous.setNextSibling(expression);
//                }
//                previous = expression;
//            }
//            ReturnStmtEndNode endNode = new ReturnStmtEndNode(returnStmt);
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            } else {
//                throw new IllegalStateException("Unreachable execution. previous can't be null.");
//            }
//            endNode.setNext(findNext(peek));
//            // Visiting each child.
//            Arrays.stream(exprs).forEach(expr -> expr.accept(this));
//        } else {
//            // No arguments in Return Stmt.
//            returnStmt.setNext(findNext(peek));
//        }
//    }
//
//    @Override
//    public void visit(WhileStmt whileStmt) {
//        // While Stmt is modeled using ifElseBranch, where next is pointing to it self.
//        // WhileStmt -> condition -> ifElseNode: IfElseNode (true) -> whileBlock -> ifElseNode: IfElseNode (goes loop)
//        //                                              (false)->  whileStmt.nextSibling. -> ...
//        Expression condition = whileStmt.getCondition();
//        whileStmt.setNext(condition);
//        IfElseNode ifElseNode = new IfElseNode(condition);
//        condition.setParent(whileStmt);
//        condition.setNextSibling(ifElseNode);
//
//        condition.accept(this);
//
//        BlockStmt blockStmt = whileStmt.getBody();
//
//        ifElseNode.setNext(blockStmt);
//        ifElseNode.setNextIfFalse(findNext(whileStmt));
//        ifElseNode.setParent(whileStmt);
//        // After Block statement, it will redirect back to the condition. Act as a loop.
//        ifElseNode.setNextSibling(condition);
//        blockStmt.setParent(ifElseNode);
//
//        loopingStack.push(whileStmt);
//        blockStmt.accept(this);
//        loopingStack.pop();
//    }
//
//    @Override
//    public void visit(BreakStmt breakStmt) {
//        // BreakStmt has to link with looping Statement's blockStmt.
//        // Flow : BreakStmt -> LoopingStmt(i.e while).nextSibling -> ...
//        breakStmt.setNext(findNext(loopingStack.peek()));
//    }
//
//    @Override
//    public void visit(TryCatchStmt tryCatchStmt) {
//        TryCatchStmtEndNode endNode = new TryCatchStmtEndNode(tryCatchStmt);
//        Statement tryBlock = tryCatchStmt.getTryBlock();
//        // Visit Try Catch block.
//        tryBlock.setParent(tryCatchStmt);
//        tryCatchStmt.setNext(tryBlock);
//        tryBlock.setNextSibling(endNode);
//        tryBlock.accept(this);
//        endNode.setNext(findNext(tryCatchStmt));
//
//        // Visit Catch Block.
//        for (TryCatchStmt.CatchBlock catchBlock : tryCatchStmt.getCatchBlocks()) {
//            catchBlock.getCatchBlockStmt().setParent(tryCatchStmt);
//            catchBlock.getCatchBlockStmt().accept(this);
//        }
//
//        tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().setParent(tryCatchStmt);
//        tryCatchStmt.getFinallyBlock().getFinallyBlockStmt().accept(this);
//    }
//
//    @Override
//    public void visit(ThrowStmt throwStmt) {
//        throwStmt.setNext(throwStmt.getExpr());
//        throwStmt.getExpr().setParent(throwStmt);
//        ThrowStmtEndNode throwStmtEndNode = new ThrowStmtEndNode(throwStmt);
//        throwStmt.getExpr().setNextSibling(throwStmtEndNode);
//        throwStmt.getExpr().accept(this);
//        // throwStmtEndNode's next will be calculated at runtime.
//    }
//
//    @Override
//    public void visit(FunctionInvocationStmt funcIStmt) {
//        // Flow : FunctionInvocationStmt -> FunctionInvocationExpr -> ... -> FunctionInvocationStmt.nextSibling -> ...
//        FunctionInvocationExpr expr = funcIStmt.getFunctionInvocationExpr();
//        funcIStmt.setNext(expr);
//        expr.setParent(funcIStmt);
//        expr.accept(this);
//    }
//
//    @Override
//    public void visit(ActionInvocationStmt actionInvocationStmt) {
//        // Flow : ActionInvocationStmt -> ActionInvocationExpr -> ... -> ActionInvocationStmt.nextSibling -> ...
//        ActionInvocationExpr expr = actionInvocationStmt.getActionInvocationExpr();
//        actionInvocationStmt.setNext(expr);
//        expr.setParent(actionInvocationStmt);
//        // visiting child.
//        expr.accept(this);
//    }
//
//    @Override
//    public void visit(ForkJoinStmt forkJoinStmt) {
//
//        ForkJoinStartNode forkJoinStartNode = new ForkJoinStartNode(forkJoinStmt);
//        Expression timeoutExpression = forkJoinStmt.getTimeout().getTimeoutExpression();
//
//        Statement joinBlock = forkJoinStmt.getJoin().getJoinBlock();
//        Statement timeoutBlock = forkJoinStmt.getTimeout().getTimeoutBlock();
//
//        forkJoinStmt.setNext(timeoutExpression);
//        timeoutExpression.setNextSibling(forkJoinStartNode);
//        timeoutExpression.setParent(forkJoinStmt);
//        timeoutExpression.accept(this);
//        // forkJoinStartNode next will be calculated at runtime.
//        timeoutBlock.setParent(forkJoinStmt);
//        joinBlock.setParent(forkJoinStmt);
//        timeoutBlock.accept(this);
//        joinBlock.accept(this);
//    }
//
//    @Override
//    public void visit(TransactionRollbackStmt transactionRollbackStmt) {
//        TransactionRollbackStmtEndNode endNode = new TransactionRollbackStmtEndNode(transactionRollbackStmt);
//        Statement transactionBlock = transactionRollbackStmt.getTransactionBlock();
//        Statement rollbackBlock = transactionRollbackStmt.getRollbackBlock().getRollbackBlockStmt();
//        // Visit Transaction block.
//        transactionBlock.setParent(transactionRollbackStmt);
//        transactionRollbackStmt.setNext(transactionBlock);
//        transactionBlock.setNextSibling(endNode);
//        abortTransactionStack.push(transactionRollbackStmt);
//        transactionBlock.accept(this);
//        abortTransactionStack.pop();
//        endNode.setNext(findNext(transactionRollbackStmt));
//        // Visit Rollback Block.
//        rollbackBlock.setParent(transactionRollbackStmt);
//        rollbackBlock.accept(this);
//    }
//
//    @Override
//    public void visit(AbortStmt abortStmt) {
//        abortStmt.setNext(findNext(abortTransactionStack.peek()));
//    }
//
//    @Override
//    public void visit(WorkerInvocationStmt workerInvocationStmt) {
//        workerInvocationStmt.setNext(findNext(workerInvocationStmt));
//    }
//
//    @Override
//    public void visit(WorkerReplyStmt workerReplyStmt) {
//        workerReplyStmt.setNext(findNext(workerReplyStmt));
//    }
//
//    @Override
//    public void visit(TransformStmt transformStmt) {
//
//    }
//
//    @Override
//    public void visit(AddExpression addExpr) {
//        handelBinaryExpression(addExpr);
//    }
//
//    @Override
//    public void visit(AndExpression andExpression) {
//        handelBinaryExpression(andExpression);
//    }
//
//    @Override
//    public void visit(BasicLiteral basicLiteral) {
//        calculateTempOffSet(basicLiteral);
//        basicLiteral.setNext(findNext(basicLiteral));
//    }
//
//    @Override
//    public void visit(DivideExpr divideExpr) {
//        handelBinaryExpression(divideExpr);
//    }
//
//    @Override
//    public void visit(ModExpression modExpression) {
//        handelBinaryExpression(modExpression);
//    }
//
//    @Override
//    public void visit(EqualExpression equalExpression) {
//        handelBinaryEqualityExpression(equalExpression);
//    }
//
//    @Override
//    public void visit(FunctionInvocationExpr funcInvExpr) {
//        if (funcInvExpr.next != null || !nonblockingEnabled) {
//            // Already Visited.
//            return;
//        }
//        calculateTempOffSet(funcInvExpr);
//        LinkedNode previous = null;
//        if (funcInvExpr.getCallableUnit() instanceof BallerinaFunction) {
//            BallerinaFunction bFunction = (BallerinaFunction) funcInvExpr.getCallableUnit();
//            ConnectorDcl[] connectorDcls = bFunction.getConnectorDcls();
//            if (connectorDcls != null) {
//                for (ConnectorDcl dcl : connectorDcls) {
//                    for (Expression exp : dcl.getArgExprs()) {
//                        exp.setParent(funcInvExpr);
//                        if (previous != null) {
//                            previous.setNextSibling(exp);
//                        } else {
//                            // This is the first.
//                            funcInvExpr.setNext(exp);
//                        }
//                        previous = exp;
//                    }
//                }
//            }
//        }
//        // Parsing Arguments.
//        Expression[] argExprs = funcInvExpr.getArgExprs();
//        if (argExprs != null && argExprs.length > 0) {
//            if (previous == null) {
//                funcInvExpr.setNext(argExprs[0]);
//            }
//            for (Expression expression : argExprs) {
//                expression.setParent(funcInvExpr);
//
//                if (previous != null) {
//                    previous.setNextSibling(expression);
//                }
//                previous = expression;
//            }
//        }
//        FunctionInvocationExprStartNode endLink = new FunctionInvocationExprStartNode(funcInvExpr);
//        if (previous == null) {
//            funcInvExpr.setNext(endLink);
//        } else {
//            previous.setNextSibling(endLink);
//        }
//        endLink.setParent(funcInvExpr);
//        // Parsing Function Body.
//        BlockStmt blockStmt = funcInvExpr.getCallableUnit().getCallableUnitBody();
//        CallableUnitEndNode callableUnitEndLink = new CallableUnitEndNode(funcInvExpr);
//        if (blockStmt != null) {
//            // Ballerina Function. This blockStatement can have multi parents. So we need to handle this especially.
//            endLink.setNext(blockStmt);
//            callableUnitEndLink.setNativeInvocation(false);
//            GotoNode gotoNode;
//            // Setup MultiLink Statement for this block Statement.
//            if (blockStmt.getGotoNode() != null) {
//                gotoNode = blockStmt.getGotoNode();
//            } else {
//                gotoNode = new GotoNode();
//                blockStmt.setGotoNode(gotoNode);
//                blockStmt.setNextSibling(gotoNode);
//            }
//            // Get Branching ID for above multi link.
//            int branchID = gotoNode.addNext(callableUnitEndLink);
//            funcInvExpr.setHasGotoBranchID(true);
//            funcInvExpr.setGotoBranchID(branchID);
//
//            BallerinaFunction bFunction = (BallerinaFunction) funcInvExpr.getCallableUnit();
//            // Avoid recursive Linking.
//            if (!bFunction.isFlowBuilderVisited()) {
//                // Visiting Block Statement.
//                returningBlockStmtStack.push(blockStmt);
//                offSetCounterStack.push(new OffSetCounter());
//                bFunction.setFlowBuilderVisited(true);
//                blockStmt.accept(this);
//                funcInvExpr.getCallableUnit().setTempStackFrameSize(offSetCounterStack.pop().getCount());
//                returningBlockStmtStack.pop();
//            }
//        } else {
//            // Native functions.
//            InvokeNativeFunctionNode nativeIStmt = new InvokeNativeFunctionNode(
//                    (AbstractNativeFunction) funcInvExpr.getCallableUnit());
//            callableUnitEndLink.setNativeInvocation(true);
//            endLink.setNext(nativeIStmt);
//            nativeIStmt.setParent(endLink);
//            nativeIStmt.setNext(callableUnitEndLink);
//        }
//        // Visiting sub expressions.
//        if (funcInvExpr.getCallableUnit() instanceof BallerinaFunction) {
//            BallerinaFunction bFunction = (BallerinaFunction) funcInvExpr.getCallableUnit();
//            ConnectorDcl[] connectorDcls = bFunction.getConnectorDcls();
//            if (connectorDcls != null) {
//                Arrays.stream(connectorDcls).forEach(dcl ->
//                        Arrays.stream(dcl.getArgExprs()).forEach(exp -> exp.accept(this)));
//            }
//        }
//        if (argExprs != null) {
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        }
//        callableUnitEndLink.setNext(findNext(funcInvExpr));
//    }
//
//    @Override
//    public void visit(ActionInvocationExpr actionInvExpr) {
//        if (actionInvExpr.next != null) {
//            // Already Visited.
//            return;
//        }
//        calculateTempOffSet(actionInvExpr);
//        // Parsing Arguments.
//        LinkedNode previous = null;
//        if (actionInvExpr.getCallableUnit() instanceof BallerinaAction) {
//            BallerinaAction bAction = (BallerinaAction) actionInvExpr.getCallableUnit();
//            ConnectorDcl[] connectorDcls = bAction.getConnectorDcls();
//            if (connectorDcls != null) {
//                for (ConnectorDcl dcl : connectorDcls) {
//                    for (Expression exp : dcl.getArgExprs()) {
//                        exp.setParent(actionInvExpr);
//                        if (previous != null) {
//                            previous.setNextSibling(exp);
//                        } else {
//                            // This the first.
//                            actionInvExpr.setNext(exp);
//                        }
//                        previous = exp;
//                    }
//                }
//            }
//        }
//        Expression[] argExprs = actionInvExpr.getArgExprs();
//        if (argExprs != null && argExprs.length > 0) {
//            if (previous == null) {
//                actionInvExpr.setNext(argExprs[0]);
//            }
//            for (Expression expression : argExprs) {
//                expression.setParent(actionInvExpr);
//
//                if (previous != null) {
//                    previous.setNextSibling(expression);
//                }
//                previous = expression;
//            }
//        }
//        ActionInvocationExprStartNode endLink = new ActionInvocationExprStartNode(actionInvExpr);
//        if (previous == null) {
//            actionInvExpr.setNext(endLink);
//        } else {
//            previous.setNextSibling(endLink);
//        }
//        endLink.setParent(actionInvExpr);
//        // Parsing Function Body.
//        BlockStmt blockStmt = actionInvExpr.getCallableUnit().getCallableUnitBody();
//        CallableUnitEndNode callableUnitEndLink = new CallableUnitEndNode(actionInvExpr);
//        if (blockStmt != null) {
//            // Ballerina Function. This blockStatement can have multi parents. So we need to handle this especially.
//            endLink.setNext(blockStmt);
//            callableUnitEndLink.setNativeInvocation(false);
//            GotoNode multiLinkStmt;
//            // Setup MultiLink Statement for this block Statement.
//            if (blockStmt.getGotoNode() != null) {
//                multiLinkStmt = blockStmt.getGotoNode();
//            } else {
//                multiLinkStmt = new GotoNode();
//                blockStmt.setGotoNode(multiLinkStmt);
//                blockStmt.setNextSibling(multiLinkStmt);
//            }
//            // Get Branching ID for above multi link.
//            int branchID = multiLinkStmt.addNext(callableUnitEndLink);
//            actionInvExpr.setHasGotoBranchID(true);
//            actionInvExpr.setGotoBranchID(branchID);
//
//            // Visiting Block Statement.
//            BallerinaAction bAction = (BallerinaAction) actionInvExpr.getCallableUnit();
//            if (!bAction.isFlowBuilderVisited()) {
//                returningBlockStmtStack.push(blockStmt);
//                offSetCounterStack.push(new OffSetCounter());
//                bAction.setFlowBuilderVisited(true);
//                blockStmt.accept(this);
//                actionInvExpr.getCallableUnit().setTempStackFrameSize(offSetCounterStack.pop().getCount());
//                returningBlockStmtStack.pop();
//            }
//        } else {
//            // Native Action.
//            InvokeNativeActionNode link = new InvokeNativeActionNode(
//                    (AbstractNativeAction) actionInvExpr.getCallableUnit());
//            callableUnitEndLink.setNativeInvocation(true);
//            endLink.setNext(link);
//            link.setParent(endLink);
//            link.setNext(callableUnitEndLink);
//        }
//        // Visiting sub expressions.
//        if (actionInvExpr.getCallableUnit() instanceof BallerinaAction) {
//            BallerinaAction bAction = (BallerinaAction) actionInvExpr.getCallableUnit();
//            ConnectorDcl[] connectorDcls = bAction.getConnectorDcls();
//            if (connectorDcls != null) {
//                Arrays.stream(connectorDcls).forEach(dcl ->
//                        Arrays.stream(dcl.getArgExprs()).forEach(exp -> exp.accept(this)));
//            }
//        }
//        if (argExprs != null) {
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        }
//        callableUnitEndLink.setNext(findNext(actionInvExpr));
//    }
//
//    @Override
//    public void visit(GreaterEqualExpression greaterEqualExpression) {
//        handelBinaryExpression(greaterEqualExpression);
//    }
//
//    @Override
//    public void visit(GreaterThanExpression greaterThanExpression) {
//        handelBinaryExpression(greaterThanExpression);
//    }
//
//    @Override
//    public void visit(LessEqualExpression lessEqualExpression) {
//        handelBinaryExpression(lessEqualExpression);
//    }
//
//    @Override
//    public void visit(LessThanExpression lessThanExpression) {
//        handelBinaryExpression(lessThanExpression);
//    }
//
//    @Override
//    public void visit(MultExpression multExpression) {
//        handelBinaryExpression(multExpression);
//    }
//
//    @Override
//    public void visit(InstanceCreationExpr instanceCreationExpr) {
//        calculateTempOffSet(instanceCreationExpr);
//        // This assume instanceCreationExpr.getRExpr() == null.
//        if (instanceCreationExpr.getRExpr() != null) {
//            throw new IllegalStateException("Linking Error. RExp is not expected.");
//        }
//        // Handle this as non-blocking manner.
//        instanceCreationExpr.setNext(findNext(instanceCreationExpr));
//    }
//
//    @Override
//    public void visit(NotEqualExpression notEqualExpression) {
//        handelBinaryEqualityExpression(notEqualExpression);
//    }
//
//    @Override
//    public void visit(OrExpression orExpression) {
//        handelBinaryExpression(orExpression);
//    }
//
//    @Override
//    public void visit(SubtractExpression subtractExpression) {
//        handelBinaryExpression(subtractExpression);
//    }
//
//    @Override
//    public void visit(UnaryExpression unaryExpression) {
//        calculateTempOffSet(unaryExpression);
//        // Handle this as non-blocking manner.
//        // Flow : UnaryExpression -> RHSExpr -> UnaryExpressionEndLink -> UnaryExpression.nextSibling -> ...
//        UnaryExpressionEndNode endNode = new UnaryExpressionEndNode(unaryExpression);
//        Expression rExp = unaryExpression.getRExpr();
//        unaryExpression.setNext(rExp);
//        rExp.setParent(unaryExpression);
//        endNode.setParent(unaryExpression);
//        rExp.setNextSibling(endNode);
//        rExp.accept(this);
//        endNode.setNext(findNext(endNode));
//    }
//
//    @Override
//    public void visit(TypeCastExpression castExpression) {
//        if (castExpression.next != null) {
//            // Already visited.
//            return;
//        }
//        calculateTempOffSet(castExpression);
//        Expression rExpr = castExpression.getRExpr();
//        TypeCastExpressionEndNode endLink = new TypeCastExpressionEndNode(castExpression);
//        if (castExpression.getEvalFunc() != null) {
//            castExpression.setNext(rExpr);
//            rExpr.setParent(castExpression);
//            rExpr.setNextSibling(endLink);
//            endLink.setParent(castExpression);
//            rExpr.accept(this);
//            endLink.setNext(findNext(castExpression));
//        } else {
//            Expression[] argExprs = castExpression.getArgExprs();
//            LinkedNode previous = null;
//            if (argExprs.length > 0) {
//                castExpression.setNext(argExprs[0]);
//                for (Expression expression : argExprs) {
//                    expression.setParent(castExpression);
//                    if (previous != null) {
//                        previous.setNextSibling(expression);
//                    }
//                    previous = expression;
//                }
//            }
//            if (previous == null) {
//                castExpression.setNext(endLink);
//            } else {
//                previous.setNextSibling(endLink);
//            }
//            endLink.setParent(castExpression);
//            // Parsing Function Body.
//            BlockStmt blockStmt = castExpression.getCallableUnit().getCallableUnitBody();
//            CallableUnitEndNode callableUnitEndLink = new CallableUnitEndNode(castExpression);
//            if (blockStmt != null) {
//                // Ballerina TypeCase. This blockStatement can have multi parents. So we need to handle this
// especially.
//                endLink.setNext(blockStmt);
//                callableUnitEndLink.setNativeInvocation(false);
//                GotoNode gotoNode;
//                // Setup MultiLink Statement for this block Statement.
//                if (blockStmt.getGotoNode() != null) {
//                    gotoNode = blockStmt.getGotoNode();
//                } else {
//                    gotoNode = new GotoNode();
//                    blockStmt.setGotoNode(gotoNode);
//                    blockStmt.setNextSibling(gotoNode);
//                }
//                // Get Branching ID for above multi link.
//                int branchID = gotoNode.addNext(callableUnitEndLink);
//                castExpression.setHasGotoBranchID(true);
//                castExpression.setGotoBranchID(branchID);
//
//                // Visiting Block Statement.
//                BTypeMapper bTypeMapper = (BTypeMapper) castExpression.getCallableUnit();
//                if (!bTypeMapper.isFlowBuilderVisited()) {
//                    returningBlockStmtStack.push(blockStmt);
//                    offSetCounterStack.push(new OffSetCounter());
//                    bTypeMapper.setFlowBuilderVisited(true);
//                    blockStmt.accept(this);
//                    castExpression.getCallableUnit().setTempStackFrameSize(offSetCounterStack.pop().getCount());
//                    returningBlockStmtStack.pop();
//                }
//            } else {
//                // Native functions.
//                InvokeNativeTypeMapperNode link = new InvokeNativeTypeMapperNode(
//                        (AbstractNativeTypeMapper) castExpression.getCallableUnit());
//                callableUnitEndLink.setNativeInvocation(true);
//                endLink.setNext(link);
//                link.setParent(endLink);
//                link.setNext(callableUnitEndLink);
//            }
//            // Visiting each Argument.
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//            callableUnitEndLink.setNext(findNext(castExpression));
//        }
//    }
//
//    @Override
//    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
//        calculateTempOffSet(arrayMapAccessExpr);
//        // Handle this as non-blocking manner.
//        ArrayMapAccessExprEndNode endNode = new ArrayMapAccessExprEndNode(arrayMapAccessExpr);
//        Expression rExp = arrayMapAccessExpr.getRExpr();
//        Expression[] indexExprs = arrayMapAccessExpr.getIndexExprs();
//        arrayMapAccessExpr.setNext(rExp);
//        rExp.setParent(arrayMapAccessExpr);
//        LinkedNode previous = rExp;
//        for (Expression indexExpr : indexExprs) {
//            previous.setNextSibling(indexExpr);
//            indexExpr.setParent(arrayMapAccessExpr);
//            previous = indexExpr;
//        }
//        previous.setNextSibling(endNode);
//        rExp.accept(this);
//        for (Expression indexExpr : indexExprs) {
//            indexExpr.accept(this);
//        }
//        endNode.setNext(findNext(endNode));
//    }
//
//    @Override
//    public void visit(FieldAccessExpr fieldAccessExpr) {
//        // TODO: Simplify this logic.
//        StructFieldAccessExprEndNode endNode = new StructFieldAccessExprEndNode(fieldAccessExpr);
//        endNode.setParent(fieldAccessExpr);
//        Expression rootVarRefExpr = fieldAccessExpr.getVarRef();
//        fieldAccessExpr.setNext(rootVarRefExpr);
//        rootVarRefExpr.setParent(fieldAccessExpr);
//        FieldAccessExpr fieldExpr = fieldAccessExpr.getFieldExpr();
//        rootVarRefExpr.setNextSibling(fieldExpr);
//        rootVarRefExpr.accept(this);
//        LinkedNode lastLinkedNode = fieldExpr;
//
//        while (fieldExpr != null) {
//            Expression varRefExpr = fieldExpr.getVarRef();
//            if (varRefExpr instanceof ArrayMapAccessExpr) {
//                Expression[] indexExprs = ((ArrayMapAccessExpr) varRefExpr).getIndexExprs();
//                lastLinkedNode.setNext(indexExprs[0]);
//                for (int i = 1; i < indexExprs.length; i++) {
//                    indexExprs[i - 1].setParent(fieldAccessExpr);
//                    indexExprs[i - 1].setNextSibling(indexExprs[i]);
//                }
//                // Last Index.
//                indexExprs[indexExprs.length - 1].setParent(fieldAccessExpr);
//                if (fieldExpr.getFieldExpr() != null) {
//                    indexExprs[indexExprs.length - 1].setNextSibling(fieldExpr.getFieldExpr());
//                    lastLinkedNode = fieldExpr.getFieldExpr();
//                } else {
//                    if (fieldAccessExpr.isLHSExpr()) {
//                        lastLinkedNode = null;
//                    } else {
//                        indexExprs[indexExprs.length - 1].setNextSibling(endNode);
//                        lastLinkedNode = endNode;
//                    }
//                }
//                for (Expression indexExpr : indexExprs) {
//                    indexExpr.accept(this);
//                }
//            } else {
//                FieldAccessExpr childFieldExpr = fieldExpr.getFieldExpr();
//                lastLinkedNode.setNext(varRefExpr);
//                lastLinkedNode = varRefExpr;
//                varRefExpr.setParent(fieldAccessExpr);
//                if (childFieldExpr != null) {
//                    lastLinkedNode.setNextSibling(childFieldExpr);
//                    lastLinkedNode = childFieldExpr;
//                } else {
//                    if (!fieldAccessExpr.isLHSExpr()) {
//                        lastLinkedNode.setNextSibling(endNode);
//                        lastLinkedNode = endNode;
//                    }
//                }
//                varRefExpr.accept(this);
//            }
//
//            fieldExpr = fieldExpr.getFieldExpr();
//            if (fieldExpr != null) {
//                fieldExpr.setParent(fieldAccessExpr);
//            }
//        }
//
//        if (lastLinkedNode != null) {
//            lastLinkedNode.setNext(findNext(fieldAccessExpr));
//        }
//    }
//
//    @Override
//    public void visit(JSONFieldAccessExpr jsonFieldAccessExpr) {
//    }
//
//    @Override
//    public void visit(BacktickExpr backtickExpr) {
//        calculateTempOffSet(backtickExpr);
//        BacktickExprEndNode endNode = new BacktickExprEndNode(backtickExpr);
//        Expression rExp = backtickExpr.getRExpr();
//        Expression[] argExprs = backtickExpr.getArgExprs();
//        LinkedNode previous = null;
//        if (rExp != null) {
//            backtickExpr.setNext(rExp);
//            rExp.setParent(backtickExpr);
//            rExp.setNextSibling(endNode);
//            rExp.accept(this);
//            previous = rExp;
//        }
//        endNode.setParent(backtickExpr);
//        if (argExprs != null && argExprs.length > 0) {
//            if (backtickExpr.next == null) {
//                backtickExpr.setNext(argExprs[0]);
//            }
//            for (Expression arg : argExprs) {
//                if (previous != null) {
//                    previous.setNextSibling(arg);
//                }
//
//                previous = arg;
//            }
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            }
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        } else {
//            if (backtickExpr.next == null) {
//                backtickExpr.setNext(endNode);
//            }
//        }
//        endNode.setNext(findNext(endNode));
//    }
//
//    @Override
//    public void visit(ArrayInitExpr arrayInitExpr) {
//        calculateTempOffSet(arrayInitExpr);
//        ArrayInitExprEndNode endNode = new ArrayInitExprEndNode(arrayInitExpr);
//        Expression rExp = arrayInitExpr.getRExpr();
//        Expression[] argExprs = arrayInitExpr.getArgExprs();
//        LinkedNode previous = null;
//        if (rExp != null) {
//            arrayInitExpr.setNext(rExp);
//            rExp.setParent(arrayInitExpr);
//            rExp.setNextSibling(endNode);
//            rExp.accept(this);
//            previous = rExp;
//        }
//        endNode.setParent(arrayInitExpr);
//        if (argExprs != null && argExprs.length > 0) {
//            if (arrayInitExpr.next == null) {
//                arrayInitExpr.setNext(argExprs[0]);
//            }
//            for (Expression arg : argExprs) {
//                if (previous != null) {
//                    previous.setNextSibling(arg);
//                }
//
//                previous = arg;
//            }
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            }
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        } else {
//            if (arrayInitExpr.next == null) {
//                arrayInitExpr.setNext(endNode);
//            }
//        }
//        endNode.setNext(findNext(endNode));
//    }
//
//    @Override
//    public void visit(RefTypeInitExpr refTypeInitExpr) {
//        visitInitExpression(refTypeInitExpr, new RefTypeInitExprEndNode(refTypeInitExpr));
//    }
//
//    @Override
//    public void visit(MapInitExpr mapInitExpr) {
//        visitInitExpression(mapInitExpr, new MapInitExprEndNode(mapInitExpr));
//    }
//
//    @Override
//    public void visit(JSONInitExpr jsonInitExpr) {
//        visitInitExpression(jsonInitExpr, new JSONInitExprEndNode(jsonInitExpr));
//    }
//
//    @Override
//    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {
//        visitInitExpression(jsonArrayInitExpr, new JSONArrayInitExprEndNode(jsonArrayInitExpr));
//    }
//
//    @Override
//    public void visit(ConnectorInitExpr connectorInitExpr) {
//        calculateTempOffSet(connectorInitExpr);
//        ConnectorInitExprEndNode endNode = new ConnectorInitExprEndNode(connectorInitExpr);
//        Expression rExp = connectorInitExpr.getRExpr();
//        Expression[] argExprs = connectorInitExpr.getArgExprs();
//        LinkedNode previous = null;
//        if (rExp != null) {
//            connectorInitExpr.setNext(rExp);
//            rExp.setParent(connectorInitExpr);
//            rExp.setNextSibling(endNode);
//            rExp.accept(this);
//            previous = rExp;
//        }
//        endNode.setParent(connectorInitExpr);
//        if (argExprs != null && argExprs.length > 0) {
//            if (connectorInitExpr.next == null) {
//                connectorInitExpr.setNext(argExprs[0]);
//            }
//            for (Expression arg : argExprs) {
//                if (previous != null) {
//                    previous.setNextSibling(arg);
//                }
//
//                previous = arg;
//            }
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            }
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        } else {
//            if (connectorInitExpr.next == null) {
//                connectorInitExpr.setNext(endNode);
//            }
//        }
//        Connector connector = (Connector) connectorInitExpr.getType();
//        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connector;
//        BlockStmt blockStmt = connectorDef.getInitFunction().getCallableUnitBody();
//        endNode.setNext(blockStmt);
//        CallableUnitEndNode callableUnitEndNode = new CallableUnitEndNode(connectorInitExpr);
//        blockStmt.setNextSibling(callableUnitEndNode);
//        GotoNode gotoNode;
//        // Setup MultiLink Statement for this block Statement.
//        if (blockStmt.getGotoNode() != null) {
//            gotoNode = blockStmt.getGotoNode();
//        } else {
//            gotoNode = new GotoNode();
//            blockStmt.setGotoNode(gotoNode);
//            blockStmt.setNextSibling(gotoNode);
//        }
//        // Get Branching ID for above multi link.
//        int branchID = gotoNode.addNext(callableUnitEndNode);
//        endNode.setHasGotoBranchID(true);
//        endNode.setGotoBranchID(branchID);
//        if (!connectorDef.getInitFunction().isFlowBuilderVisited()) {
//            returningBlockStmtStack.push(blockStmt);
//            offSetCounterStack.push(new OffSetCounter());
//            connectorDef.getInitFunction().setFlowBuilderVisited(true);
//            blockStmt.accept(this);
//            connectorDef.getInitFunction().setTempStackFrameSize(offSetCounterStack.pop().getCount());
//            returningBlockStmtStack.pop();
//        }
//        if (connectorDef.getInitAction() != null) {
//            ConnectorInitActionStartNode actionStartNode = new ConnectorInitActionStartNode(connectorInitExpr);
//            callableUnitEndNode.setNext(actionStartNode);
//            InvokeNativeActionNode nativeActionNode =
//                    new InvokeNativeActionNode((AbstractNativeAction) connectorDef.getInitAction());
//            actionStartNode.setNext(nativeActionNode);
//            CallableUnitEndNode callableUnitActionEndNode = new CallableUnitEndNode(connectorInitExpr);
//            callableUnitActionEndNode.setNativeInvocation(true);
//            nativeActionNode.setNext(callableUnitActionEndNode);
//            callableUnitActionEndNode.setNext(findNext(connectorInitExpr));
//        } else {
//            callableUnitEndNode.setNext(findNext(connectorInitExpr));
//        }
//    }
//
//    @Override
//    public void visit(StructInitExpr structInitExpr) {
//        calculateTempOffSet(structInitExpr);
//        StructInitExprStartNode startNode = new StructInitExprStartNode(structInitExpr);
//        StructInitExprEndNode endNode = new StructInitExprEndNode(structInitExpr);
//        Expression[] argExprs = structInitExpr.getArgExprs();
//
//        // Create flow for populating default values
//        StructDef structDef = (StructDef) structInitExpr.getType();
//        BlockStmt blockStmt = structDef.getInitFunction().getCallableUnitBody();
//        structInitExpr.setNext(startNode);
//        startNode.setParent(structInitExpr);
//
//        startNode.setNext(blockStmt);
//        CallableUnitEndNode callableUnitEndNode = new CallableUnitEndNode(structInitExpr);
//        blockStmt.setNextSibling(callableUnitEndNode);
//        GotoNode gotoNode;
//        // Setup MultiLink Statement for this block Statement.
//        if (blockStmt.getGotoNode() != null) {
//            gotoNode = blockStmt.getGotoNode();
//        } else {
//            gotoNode = new GotoNode();
//            blockStmt.setGotoNode(gotoNode);
//            blockStmt.setNextSibling(gotoNode);
//        }
//        // Get Branching ID for above multi link.
//        int branchID = gotoNode.addNext(callableUnitEndNode);
//        startNode.setHasGotoBranchID(true);
//        startNode.setGotoBranchID(branchID);
//        if (!structDef.getInitFunction().isFlowBuilderVisited()) {
//            returningBlockStmtStack.push(blockStmt);
//            offSetCounterStack.push(new OffSetCounter());
//            structDef.getInitFunction().setFlowBuilderVisited(true);
//            blockStmt.accept(this);
//            structDef.getInitFunction().setTempStackFrameSize(offSetCounterStack.pop().getCount());
//            returningBlockStmtStack.pop();
//        }
//
//        //  Create flow for arguments in struct initializer expression
//        if (argExprs != null && argExprs.length > 0) {
//            LinkedNode previous = null;
//            callableUnitEndNode.setNext(argExprs[0]);
//
//            for (Expression arg : argExprs) {
//                if (previous != null) {
//                    previous.setNextSibling(arg);
//                }
//                previous = arg;
//            }
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            }
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        } else {
//            callableUnitEndNode.setNext(endNode);
//        }
//        endNode.setNext(findNext(endNode));
//    }
//
////    @Override
////    public void visit(MapInitExpr mapInitExpr) {
////        calculateTempOffSet(mapInitExpr);
////        MapInitExprEndNode endNode = new MapInitExprEndNode(mapInitExpr);
////        Expression rExp = mapInitExpr.getRExpr();
////        Expression[] argExprs = mapInitExpr.getArgExprs();
////        LinkedNode previous = null;
////        if (rExp != null) {
////            mapInitExpr.setNext(rExp);
////            rExp.setParent(mapInitExpr);
////            rExp.setNextSibling(endNode);
////            rExp.accept(this);
////            previous = rExp;
////        }
////        endNode.setParent(mapInitExpr);
////        if (argExprs != null && argExprs.length > 0) {
////            if (mapInitExpr.next == null) {
////                mapInitExpr.setNext(argExprs[0]);
////            }
////            for (Expression arg : argExprs) {
////                if (previous != null) {
////                    previous.setNextSibling(arg);
////                }
////
////                previous = arg;
////            }
////            if (previous != null) {
////                previous.setNextSibling(endNode);
////            }
////            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
////        } else {
////            if (mapInitExpr.next == null) {
////                mapInitExpr.setNext(endNode);
////            }
////        }
////        endNode.setNext(findNext(endNode));
////    }
//
//    @Override
//    public void visit(KeyValueExpr keyValueExpr) {
//        calculateTempOffSet(keyValueExpr);
//        // Handle this as non-blocking manner.
//        Expression keyExpr = keyValueExpr.getKeyExpr();
//        Expression valueExpr = keyValueExpr.getValueExpr();
//        valueExpr.setParent(keyValueExpr);
//        if (keyExpr instanceof VariableRefExpr
//                && ((VariableRefExpr) keyExpr).getMemoryLocation() instanceof StructVarLocation) {
//            keyValueExpr.setNext(valueExpr);
//        } else {
//            keyValueExpr.setNext(keyExpr);
//            keyExpr.setParent(keyValueExpr);
//            keyExpr.setNextSibling(valueExpr);
//            keyExpr.accept(this);
//        }
//        valueExpr.accept(this);
//    }
//
//    @Override
//    public void visit(VariableRefExpr variableRefExpr) {
//        variableRefExpr.setNext(findNext(variableRefExpr));
//    }
//
//    @Override
//    public void visit(NullLiteral nullLiteral) {
//        calculateTempOffSet(nullLiteral);
//        nullLiteral.setNext(findNext(nullLiteral));
//    }
//
//    @Override
//    public void visit(StackVarLocation stackVarLocation) {
//
//    }
//
//    @Override
//    public void visit(ServiceVarLocation serviceVarLocation) {
//
//    }
//
//    @Override
//    public void visit(GlobalVarLocation globalVarLocation) {
//
//    }
//
//    @Override
//    public void visit(ConnectorVarLocation connectorVarLocation) {
//
//    }
//
//    @Override
//    public void visit(ConstantLocation constantLocation) {
//
//    }
//
//    @Override
//    public void visit(ResourceInvocationExpr resourceIExpr) {
//
//    }
//
//    @Override
//    public void visit(MainInvoker mainInvoker) {
//
//    }
//
//    @Override
//    public void visit(StructVarLocation structVarLocation) {
//
//    }
//
//    @Override
//    public void visit(WorkerVarLocation workerVarLocation) {
//    }
//
//    public int getCurrentTempStackSize() {
//        return offSetCounterStack.peek().getCount();
//    }
//
//    private void handelBinaryExpression(BinaryExpression binaryExpression) {
//        // Handle this as non-blocking manner.
//        // Flow: BinaryExpr -> RHSExpr -> LHSExpr -> BinaryExpressionEndNode -> BinaryExpression.nextSibling -> ...
//        calculateTempOffSet(binaryExpression);
//        BinaryExpressionEndNode endNode = new BinaryExpressionEndNode(binaryExpression);
//        linkBinaryExpr(binaryExpression, endNode);
//    }
//
//    private void handelBinaryEqualityExpression(BinaryEqualityExpression binaryEqualityExpression) {
//        // Flow: BinaryExpr -> RHSExpr -> LHSExpr ->
//        //              BinaryEqualityExpressionEndNode -> BinaryEqualityExpression.nextSibling -> ...
//        calculateTempOffSet(binaryEqualityExpression);
//        BinaryEqualityExpressionEndNode endNode = new BinaryEqualityExpressionEndNode(binaryEqualityExpression);
//        linkBinaryExpr(binaryEqualityExpression, endNode);
//    }
//
//    private void linkBinaryExpr(BinaryExpression binaryExpression, AbstractLinkedNode endNode) {
//        Expression rExp = binaryExpression.getRExpr();
//        Expression lExp = binaryExpression.getLExpr();
//        binaryExpression.setNext(rExp);
//        rExp.setParent(binaryExpression);
//        lExp.setParent(binaryExpression);
//        endNode.setParent(binaryExpression);
//        rExp.setNextSibling(lExp);
//        lExp.setNextSibling(endNode);
//        rExp.accept(this);
//        lExp.accept(this);
//        endNode.setNext(findNext(endNode));
//    }
//
//
//    /**
//     * Clear Branching Stacks before going though root block statement.
//     */
//    private void clearBranchingStacks() {
//        this.loopingStack.clear();
//        this.returningBlockStmtStack.clear();
//        this.currentResource = null;
//        this.offSetCounterStack.clear();
//        this.abortTransactionStack.clear();
//    }
//
//    /**
//     * Find next execution Unit for given Node recursively.
//     *
//     * @param linkedNode that currently processing.
//     * @return next Statement.
//     */
//    private LinkedNode findNext(LinkedNode linkedNode) {
//        LinkedNode parent = linkedNode, lastParent = null, next = null;
//        while (parent != null) {
//            if (parent.getNextSibling() != null) {
//                next = parent.getNextSibling();
//                break;
//            }
//            lastParent = parent;
//            parent = parent.getParent();
//        }
//        if (next != null) {
//            return next;
//        } else {
//            // Validate whether Last Parent is a start statement.
//            if (lastParent instanceof StartNode) {
//                // There is no execution state left. Hence this should be an end/terminate link.
//                if (((StartNode) lastParent).isMainFunctionInvocation()) {
//                    return new ExitNode();
//                }
//                return new EndNode();
//            } else {
//                // We shouldn't come here. Unless someone forgot to handle linking.
//                String location = "Unknown";
//                if (lastParent instanceof Node) {
//                    Node node = (Node) lastParent;
//                    if (node.getNodeLocation() != null) {
//                        location = node.getNodeLocation().getFileName() + ":" + node.getNodeLocation()
// .getLineNumber();
//                    }
//                }
//                throw new FlowBuilderException("Internal Error. Broken Link in Class " + lastParent.getClass() +
//                        " line " + location);
//            }
//        }
//    }
//
//    private void calculateTempOffSet(Expression expr) {
//        int valRef;
//        if (expr instanceof FunctionInvocationExpr) {
//            valRef = offSetCounterStack.peek().incrementAndGet(
//                    ((FunctionInvocationExpr) expr).getCallableUnit().getReturnParamTypes().length);
//        } else if (expr instanceof ActionInvocationExpr) {
//            valRef = offSetCounterStack.peek().incrementAndGet(
//                    ((ActionInvocationExpr) expr).getCallableUnit().getReturnParamTypes().length);
//        } else {
//            valRef = offSetCounterStack.peek().incrementAndGet();
//        }
//        expr.setTempOffset(valRef);
//    }
//
//    /**
//     * Utility Counter Implementation for calculating offset values for temp values in a statement.
//     */
//    private static class OffSetCounter {
//
//        private int count;
//
//        OffSetCounter() {
//            count = -1;
//        }
//
//        int incrementAndGet() {
//            count++;
//            return count;
//        }
//
//        int incrementAndGet(int value) {
//            count = count + value;
//            // We should return offset starting point.
//            return count - value + 1;
//        }
//
//        int getCount() {
//            // This represents TempOffSet Array size. Hence we need to set max + 1.
//            return count + 1;
//        }
//    }
//
//    private void visitInitExpression(RefTypeInitExpr refTypeInitExpr, AbstractLinkedNode endNode) {
//        calculateTempOffSet(refTypeInitExpr);
//
//        Expression rExp = refTypeInitExpr.getRExpr();
//        Expression[] argExprs = refTypeInitExpr.getArgExprs();
//        LinkedNode previous = null;
//        if (rExp != null) {
//            refTypeInitExpr.setNext(rExp);
//            rExp.setParent(refTypeInitExpr);
//            rExp.setNextSibling(endNode);
//            rExp.accept(this);
//            previous = rExp;
//        }
//        endNode.setParent(refTypeInitExpr);
//        if (argExprs != null && argExprs.length > 0) {
//            if (refTypeInitExpr.next == null) {
//                refTypeInitExpr.setNext(argExprs[0]);
//            }
//            for (Expression arg : argExprs) {
//                if (previous != null) {
//                    previous.setNextSibling(arg);
//                }
//                previous = arg;
//            }
//            if (previous != null) {
//                previous.setNextSibling(endNode);
//            }
//            Arrays.stream(argExprs).forEach(arg -> arg.accept(this));
//        } else {
//            if (refTypeInitExpr.next == null) {
//                refTypeInitExpr.setNext(endNode);
//            }
//        }
//        endNode.setNext(findNext(endNode));
//    }
}
