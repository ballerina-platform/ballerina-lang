package io.ballerina.runtime.internal.query.utils;

import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.values.ErrorValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_QUERY_PKG_ID;

public class DistinctQueryErrorCreator {
    public static BError createDistinctError(BError e) {
//        Type queryError = TypeCreator.createErrorType("Error", BALLERINA_QUERY_PKG_ID);
//        return ErrorCreator.createError(queryError, e.getErrorMessage(), null);
        MapValueImpl<BString, Object> details = new MapValueImpl<>(PredefinedTypes.TYPE_ERROR_DETAIL);
        return new ErrorValue(new BErrorType(TypeConstants.ERROR, PredefinedTypes.TYPE_ERROR.getPackage(),
                TypeChecker.getType(details)), e.getErrorMessage(), e, details,
                "CompleteEarlyError", BALLERINA_QUERY_PKG_ID);
    }
}
