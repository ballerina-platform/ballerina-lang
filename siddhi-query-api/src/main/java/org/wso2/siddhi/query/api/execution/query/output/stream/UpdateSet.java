package org.wso2.siddhi.query.api.execution.query.output.stream;

import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Updating UpdateSet Attribute for UpdateStream
 */
public class UpdateSet {

  private List<SetAttribute> setAttributeList = new ArrayList<>();

    public UpdateSet set(Variable tableVariable, Expression assignmentExpression) {
        setAttributeList.add(new SetAttribute(tableVariable, assignmentExpression));
        return this;
    }

    public List<SetAttribute> getSetAttributeList() {
        return setAttributeList;
    }

    /**
     * Attribute assignment for updates
     */
    public static class SetAttribute {
        private Variable tableVariable;
        private Expression assignmentExpression;

        public SetAttribute(Variable tableVariable, Expression assignmentExpression) {
            this.tableVariable = tableVariable;
            this.assignmentExpression = assignmentExpression;
        }

        public Variable getTableVariable() {
            return tableVariable;
        }

        public Expression getAssignmentExpression() {
            return assignmentExpression;
        }
    }
}
