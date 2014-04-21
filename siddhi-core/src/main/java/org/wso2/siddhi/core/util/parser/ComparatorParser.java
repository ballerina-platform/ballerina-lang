package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.compare.contains.ContainsCompareConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorBoolBool;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.equal.EqualCompareConditionExecutorStringString;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than.GreaterThanCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.greater_than_equal.GreaterThanEqualCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.instance_of.InstanceOfCompareConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than.LessThanCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.less_than_equal.LessThanEqualCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorBoolBool;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorDoubleDouble;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorDoubleFloat;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorDoubleInt;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorDoubleLong;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorFloatDouble;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorFloatFloat;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorFloatInt;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorFloatLong;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorIntDouble;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorIntFloat;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorIntInt;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorIntLong;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorLongDouble;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorLongFloat;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorLongInt;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorLongLong;
import org.wso2.siddhi.core.executor.conditon.compare.not_equal.NotEqualCompareConditionExecutorStringString;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;

public class ComparatorParser {
    public static ConditionExecutor parseGreaterThanCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in greater than comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in greater than comparisons");
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in greater than comparisons");
    }


    public static ConditionExecutor parseGreaterThanEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in greater than equal comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in greater than equal comparisons");
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in greater than equal comparisons");
    }

    public static ConditionExecutor parseLessThanCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in less than comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in less than comparisons");
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in less than comparisons");
    }

    public static ConditionExecutor parseLessThanEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in less than equal comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in less than equal comparisons");
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in less than equal comparisons");
    }

    public static ConditionExecutor parseEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new EqualCompareConditionExecutorStringString(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("sting cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                switch (rightExpressionExecutor.getReturnType()) {
                    case BOOL:
                        return new EqualCompareConditionExecutorBoolBool(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("bool cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used  equal comparisons");
    }

    public static ConditionExecutor parseNotEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new NotEqualCompareConditionExecutorStringString(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("sting cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                }
            case BOOL:
                switch (rightExpressionExecutor.getReturnType()) {
                    case BOOL:
                        return new NotEqualCompareConditionExecutorBoolBool(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("bool cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in not equal comparisons");
    }

    public static ConditionExecutor parseContainsCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new ContainsCompareConditionExecutor(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException(rightExpressionExecutor.getReturnType() + " cannot be used in contains comparisons");
                }
        }
        throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in contains comparisons");
    }

    public static ConditionExecutor parseInstanceOfCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {

        switch (rightExpressionExecutor.getReturnType()) {
            case TYPE:
                return new InstanceOfCompareConditionExecutor(leftExpressionExecutor, rightExpressionExecutor);
        }
        throw new OperationNotSupportedException(rightExpressionExecutor.getReturnType() + " cannot be used in right hand side of the instanceof comparisons");
    }


}