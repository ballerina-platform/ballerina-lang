package org.wso2.siddhi.core.table.record;


import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.*;
import org.wso2.siddhi.query.api.expression.constant.Constant;

public class BaseConditionVisitor implements ConditionVisitor {

    @Override
    public void beginVisitAnd(And and) {

    }

    @Override
    public void endVisitAnd(And and) {

    }

    @Override
    public void beginVisitAndLeftOperand(Expression expression) {

    }

    @Override
    public void endVisitAndLeftOperand(Expression expression) {

    }

    @Override
    public void beginVisitAndRightOperand(Expression expression) {

    }

    @Override
    public void endVisitAndRightOperand(Expression expression) {

    }

    @Override
    public void beginVisitOr(Or or) {

    }

    @Override
    public void endVisitOr(Or or) {

    }

    @Override
    public void beginVisitOrLeftOperand(Expression expression) {

    }

    @Override
    public void endVisitOrLeftOperand(Expression expression) {

    }

    @Override
    public void beginVisitOrRightOperand(Expression expression) {

    }

    @Override
    public void endVisitOrRightOperand(Expression expression) {

    }

    @Override
    public void beginVisitNot(Not not) {

    }

    @Override
    public void endVisitNot(Not not) {

    }

    @Override
    public void beginVisitCompare(Compare compare, Compare.Operator operator) {

    }

    @Override
    public void endVisitCompare(Compare Compare, Compare.Operator operator) {

    }

    @Override
    public void beginVisitCompareLeftOperand(Expression expression, Compare.Operator operator) {

    }

    @Override
    public void endVisitCompareLeftOperand(Expression expression, Compare.Operator operator) {

    }

    @Override
    public void beginVisitCompareRightOperand(Expression expression, Compare.Operator operator) {

    }

    @Override
    public void endVisitCompareRightOperand(Expression expression, Compare.Operator operator) {

    }

    @Override
    public void beginVisitIsNull(IsNull isNull, String streamId) {

    }

    @Override
    public void endVisitIsNull(IsNull isNull, String streamId) {

    }

    @Override
    public void beginVisitIn(In in, String storeId) {

    }

    @Override
    public void endVisitIn(In in, String storeId) {

    }

    @Override
    public void beginVisitConstant(Constant constant) {

    }

    @Override
    public void endVisitConstant(Constant constant) {

    }

    @Override
    public void beginVisitMath(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void endVisitMath(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void beginVisitMathLeftOperand(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void endVisitMathLeftOperand(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void beginVisitMathRightOperand(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void endVisitMathRightOperand(Expression expression, MathOperator mathOperator) {

    }

    @Override
    public void beginVisitAttributeFunction(AttributeFunction attributeFunction) {

    }

    @Override
    public void endVisitAttributeFunction(AttributeFunction attributeFunction) {

    }

    @Override
    public void beginVisitParameterAttributeFunction(Expression expression, int index) {

    }

    @Override
    public void endVisitParameterAttributeFunction(Expression expression, int index) {

    }

    @Override
    public void beginVisitStreamVariable(Variable variable, String id, Attribute.Type type) {

    }

    @Override
    public void endVisitStreamVariable(Variable variable, String id, Attribute.Type type) {

    }

    @Override
    public void beginVisitStoreVariable(Variable variable, Attribute.Type type) {

    }

    @Override
    public void endVisitStoreVariable(Variable variable, Attribute.Type type) {

    }


}
