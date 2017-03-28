package org.wso2.siddhi.core.table.record;


import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.condition.*;

public interface ConditionVisitor {

    enum MathOperator {ADD, DIVIDE, MULTIPLY, SUBTRACT, MOD}

    /*And*/
    void beginVisitAnd();
    void endVisitAnd();
    void beginVisitAndLeftOperand();
    void endVisitAndLeftOperand();
    void beginVisitAndRightOperand();
    void endVisitAndRightOperand();

    /*Or*/
    void beginVisitOr();
    void endVisitOr();
    void beginVisitOrLeftOperand();
    void endVisitOrLeftOperand();
    void beginVisitOrRightOperand();
    void endVisitOrRightOperand();

    /*Not*/
    void beginVisitNot();
    void endVisitNot();

    /*Compare*/
    void beginVisitCompare(Compare.Operator operator);
    void endVisitCompare(Compare.Operator operator);
    void beginVisitCompareLeftOperand(Compare.Operator operator);
    void endVisitCompareLeftOperand(Compare.Operator operator);
    void beginVisitCompareRightOperand(Compare.Operator operator);
    void endVisitCompareRightOperand(Compare.Operator operator);

    /*IsNull*/
    void beginVisitIsNull(String streamId);
    void endVisitIsNull(String streamId);

    /*In*/
    void beginVisitIn(String storeId);
    void endVisitIn(String storeId);

    /*Constant*/
    void beginVisitConstant(Object value, Attribute.Type type);
    void endVisitConstant(Object value, Attribute.Type type);

    /*Math*/
    void beginVisitMath(MathOperator mathOperator);
    void endVisitMath(MathOperator mathOperator);
    void beginVisitMathLeftOperand(MathOperator mathOperator);
    void endVisitMathLeftOperand(MathOperator mathOperator);
    void beginVisitMathRightOperand(MathOperator mathOperator);
    void endVisitMathRightOperand(MathOperator mathOperator);

    /*AttributeFunction*/
    void beginVisitAttributeFunction(String namespace, String functionName);
    void endVisitAttributeFunction(String namespace, String functionName);
    void beginVisitParameterAttributeFunction(int index);
    void endVisitParameterAttributeFunction(int index);

    /*Variable*/
    void beginVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type);
    void endVisitStreamVariable(String id, String streamId, String attributeName, Attribute.Type type);

    /*Variable*/
    void beginVisitStoreVariable(String StoreId, String attributeName, Attribute.Type type);
    void endVisitStoreVariable(String StoreId, String attributeName, Attribute.Type type);

}
