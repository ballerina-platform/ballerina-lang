package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

/*
    * This class represents the Exception in query execution.
 */
public class QueryException extends RuntimeException {
    private BError error;

    public QueryException(BError error) {
        this.error = createDistinctError("Error", BALLERINA_QUERY_PKG_ID, error);
    }

    public QueryException(BError error, boolean isCompleteEarlyError) {
        this.error = createDistinctError("CompleteEarlyError", BALLERINA_QUERY_PKG_ID, error);
    }

    public QueryException(String message) {
        this.error = ErrorCreator.createError(StringUtils.fromString(message));
    }

    public BError getError() {
        return error;
    }

    public void setError(BError error) {
        this.error = error;
    }

    public BError createDistinctError(String typeIdName, Module typeIdPkg, BError error) {
        MapValueImpl<BString, Object> details = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(), TypeChecker
                .getType(details)), error.getErrorMessage(), null, details, typeIdName, typeIdPkg);
    }
}
