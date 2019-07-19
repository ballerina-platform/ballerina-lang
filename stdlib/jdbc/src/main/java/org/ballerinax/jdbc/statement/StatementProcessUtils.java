package org.ballerinax.jdbc.statement;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinax.jdbc.Constants;

import static org.ballerinax.jdbc.Constants.PARAMETER_DIRECTION_FIELD;
import static org.ballerinax.jdbc.Constants.PARAMETER_SQL_TYPE_FIELD;

/**
 * Utility methods for processing statements.
 *
 * @since 1.0.0
 */
public class StatementProcessUtils {

    private StatementProcessUtils() {

    }

    static int getParameterDirection(MapValue<String, Object> parameter) {
        int direction = 0;
        String directionInput = parameter.getStringValue(PARAMETER_DIRECTION_FIELD);
        if (directionInput != null) {
            switch (directionInput) {
            case Constants.QueryParamDirection.DIR_OUT:
                direction = Constants.QueryParamDirection.OUT;
                break;
            case Constants.QueryParamDirection.DIR_INOUT:
                direction = Constants.QueryParamDirection.INOUT;
                break;
            default:
                direction = Constants.QueryParamDirection.IN;
                break;
            }
        }
        return direction;
    }

    static String getSQLType(MapValue<String, Object> parameter) {
        String sqlType = "";
        Object sqlTypeValue = parameter.get(PARAMETER_SQL_TYPE_FIELD);
        if (sqlTypeValue != null) {
            sqlType = (String) sqlTypeValue;
        }
        return sqlType;
    }
}
