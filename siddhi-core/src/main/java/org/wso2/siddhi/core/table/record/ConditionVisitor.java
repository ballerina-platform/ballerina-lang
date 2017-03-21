package org.wso2.siddhi.core.table.record;


import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.*;
import org.wso2.siddhi.query.api.expression.constant.Constant;

public interface ConditionVisitor {

    enum MathOperator {ADD, DIVIDE, MULTIPLY, SUBTRACT, MOD}

    /*And*/
    public void beginVisitAnd(And and);
    public void endVisitAnd(And and);
    public void beginVisitAndLeftOperand(Expression expression);
    public void endVisitAndLeftOperand(Expression expression);
    public void beginVisitAndRightOperand(Expression expression);
    public void endVisitAndRightOperand(Expression expression);

    /*Or*/
    public void beginVisitOr(Or or);
    public void endVisitOr(Or or);
    public void beginVisitOrLeftOperand(Expression expression);
    public void endVisitOrLeftOperand(Expression expression);
    public void beginVisitOrRightOperand(Expression expression);
    public void endVisitOrRightOperand(Expression expression);

    /*Not*/
    public void beginVisitNot(Not not);
    public void endVisitNot(Not not);

    /*Compare*/
    public void beginVisitCompare(Compare compare, Compare.Operator operator);
    public void endVisitCompare(Compare Compare, Compare.Operator operator);
    public void beginVisitCompareLeftOperand(Expression expression, Compare.Operator operator);
    public void endVisitCompareLeftOperand(Expression expression, Compare.Operator operator);
    public void beginVisitCompareRightOperand(Expression expression, Compare.Operator operator);
    public void endVisitCompareRightOperand(Expression expression, Compare.Operator operator);

    /*IsNull*/
    public void beginVisitIsNull(IsNull isNull, String streamId);
    public void endVisitIsNull(IsNull isNull, String streamId);

    /*In*/
    public void beginVisitIn(In in, String storeId);
    public void endVisitIn(In in, String storeId);

    /*Constant*/
    public void beginVisitConstant(Constant constant);
    public void endVisitConstant(Constant constant);

    /*Math*/
    public void beginVisitMath(Expression expression, MathOperator mathOperator);
    public void endVisitMath(Expression expression, MathOperator mathOperator);
    public void beginVisitMathLeftOperand(Expression expression, MathOperator mathOperator);
    public void endVisitMathLeftOperand(Expression expression, MathOperator mathOperator);
    public void beginVisitMathRightOperand(Expression expression, MathOperator mathOperator);
    public void endVisitMathRightOperand(Expression expression, MathOperator mathOperator);

    /*AttributeFunction*/
    public void beginVisitAttributeFunction(AttributeFunction attributeFunction);
    public void endVisitAttributeFunction(AttributeFunction attributeFunction);
    public void beginVisitParameterAttributeFunction(Expression expression, int index);
    public void endVisitParameterAttributeFunction(Expression expression, int index);

    /*Variable*/
    public void beginVisitStreamVariable(Variable variable, String id, Attribute.Type type);
    public void endVisitStreamVariable(Variable variable, String id, Attribute.Type type);

    /*Variable*/
    public void beginVisitStoreVariable(Variable variable, Attribute.Type type);
    public void endVisitStoreVariable(Variable variable, Attribute.Type type);

}
