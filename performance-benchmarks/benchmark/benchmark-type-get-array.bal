package benchmark;
import ballerina/io;

functionRecord[] functionArray = [];

public function getTypeBenchmarkArray() returns (functionRecord[]) {

    functionArray = [
        {functionName:"benchmarkTypeAnyJSONCasting", f:benchmarkTypeAnyJSONCasting},
        {functionName:"benchmarkJsonStructConstraint", f:benchmarkJsonStructConstraint},
        {functionName:"benchmarkJsonInitializationWithStructConstraint", f:benchmarkJsonInitializationWithStructConstraint},
        {functionName:"benchmarkGetPlainJson", f:benchmarkGetPlainJson},
        {functionName:"benchmarkGetConstraintJson", f:benchmarkGetConstraintJson},
        {functionName:"benchmarkGetPersonJson", f:benchmarkGetPersonJson},
        {functionName:"benchmarkConstrainingWithNestedRecords", f:benchmarkConstrainingWithNestedRecords},
        {functionName:"benchmarkConstraintJSONToJSONCast", f:benchmarkConstraintJSONToJSONCast},
        {functionName:"benchmarkJSONToConstraintJsonUnsafeCast", f:benchmarkJSONToConstraintJsonUnsafeCast},
        {functionName:"benchmarkConstraintJSONToConstraintJsonCast", f:benchmarkConstraintJSONToConstraintJsonCast},
        {functionName:"benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast", f:benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast},
        {functionName:"benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast", f:benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast},
        {functionName:"benchmarkJSONArrayToConstraintJsonArrayCastPositive", f:benchmarkJSONArrayToConstraintJsonArrayCastPositive},
        {functionName:"benchmarkJSONArrayToConstraintJsonArrayCastNegative", f:benchmarkJSONArrayToConstraintJsonArrayCastNegative},
        {functionName:"benchmarkJSONArrayToCJsonArrayCast", f:benchmarkJSONArrayToCJsonArrayCast},
        {functionName:"benchmarkJSONArrayToCJsonArrayCastNegative", f:benchmarkJSONArrayToCJsonArrayCastNegative},
        {functionName:"benchmarkCJSONArrayToJsonAssignment", f:benchmarkCJSONArrayToJsonAssignment},
        {functionName:"benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative", f:benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative},
        {functionName:"benchmarkConstrainedJsonWithFunctions", f:benchmarkConstrainedJsonWithFunctions},
        {functionName:"benchmarkConstrainedJsonWithFunctionGetKeys", f:benchmarkConstrainedJsonWithFunctionGetKeys},

        {functionName:"benchmarkPrintAnyVal", f:benchmarkPrintAnyVal},
        {functionName:"benchmarkPrintlnAnyVal", f:benchmarkPrintlnAnyVal},

        {functionName:"benchmarkIntAddition", f:benchmarkIntAddition},
        {functionName:"benchmarkIntSubtraction", f:benchmarkIntSubtraction},
        {functionName:"benchmarkIntMultiplication", f:benchmarkIntMultiplication},
        {functionName:"benchmarkIntDivision", f:benchmarkIntDivision},
        {functionName:"benchmarkIntTypesAddition", f:benchmarkIntTypesAddition},
        {functionName:"benchmarkIntegerTypesMultiplication", f:benchmarkIntegerTypesMultiplication},
        {functionName:"benchmarkIntegerTypesSubtraction", f:benchmarkIntegerTypesSubtraction},
        {functionName:"benchmarkIntegerTypesDivision", f:benchmarkIntegerTypesDivision},

        {functionName:"benchmarkFloatAddition", f:benchmarkFloatAddition},
        {functionName:"benchmarkFloatAdditionWithReturn", f:benchmarkFloatAdditionWithReturn},
        {functionName:"benchmarkFloatMultiplication", f:benchmarkFloatMultiplication},
        {functionName:"benchmarkFloatMultiplicationWithReturn", f:benchmarkFloatMultiplicationWithReturn},
        {functionName:"benchmarkFloatSubtraction", f:benchmarkFloatSubtraction},
        {functionName:"benchmarkFloatSubtractionWithReturn", f:benchmarkFloatSubtractionWithReturn},
        {functionName:"benchmarkFloatDivision", f:benchmarkFloatDivision},
        {functionName:"benchmarkFloatDivisionWithReturn", f:benchmarkFloatDivisionWithReturn},

        {functionName:"benchmarkCurrentTimeFunction", f:benchmarkCurrentTimeFunction},
        {functionName:"benchmarkCreateTimeWithZoneIDFunction", f:benchmarkCreateTimeWithZoneIDFunction},
        {functionName:"benchmarkCreateTimeWithOffsetFunction", f:benchmarkCreateTimeWithOffsetFunction},
        {functionName:"benchmarkCreateTimeWithNoZoneFunction", f:benchmarkCreateTimeWithNoZoneFunction},
        {functionName:"benchmarkCreateDateTimeFunction", f:benchmarkCreateDateTimeFunction},
        {functionName:"benchmarkParseTimeFunction", f:benchmarkParseTimeFunction},
        {functionName:"benchmarkToStringWithCreateTimeFunction", f:benchmarkToStringWithCreateTimeFunction},
        {functionName:"benchmarkFormatTimeFunction", f:benchmarkFormatTimeFunction},
        {functionName:"benchmarkFormatTimeToRFC1123function", f:benchmarkFormatTimeToRFC1123function},
        {functionName:"benchmarkTimeGetFunctions", f:benchmarkTimeGetFunctions},
        {functionName:"benchmarkGetDateFunction", f:benchmarkGetDateFunction},
        {functionName:"benchmarkGetTimeFunction", f:benchmarkGetTimeFunction},
        {functionName:"benchmarkAddDurationFunction", f:benchmarkAddDurationFunction},
        {functionName:"benchmarkSubtractDurationFunction", f:benchmarkSubtractDurationFunction},
        {functionName:"benchmarkToTimezoneFunction", f:benchmarkToTimezoneFunction},
        {functionName:"benchmarkToTimezoneFunctionWithDateTime", f:benchmarkToTimezoneFunctionWithDateTime},
        {functionName:"benchmarkManualTimeCreateFunction", f:benchmarkManualTimeCreateFunction},
        {functionName:"benchmarkManualTimeCreateFunctionWithNoZone", f:benchmarkManualTimeCreateFunctionWithNoZone},
        {functionName:"benchmarkManualTimeCreateFunctionWithEmptyZone", f:benchmarkManualTimeCreateFunctionWithEmptyZone},
        {functionName:"benchmarkParseTimeFunctionWithDifferentFormats", f:benchmarkParseTimeFunctionWithDifferentFormats},

        {functionName:"benchmarkSprintfWithFloat", f:benchmarkSprintfWithFloat},
        {functionName:"benchmarkSprintfWithString", f:benchmarkSprintfWithString}

    ];

    return functionArray;

}

public function addToBenchmarkArray(string functionNames) {

    functionRecord[] functionArray2 = [];
    functionArray2 = [{functionName:"benchmarkTypeAnyJSONCasting", f:benchmarkTypeAnyJSONCasting}];

    // functionArray += {functionName:"benchmarkTypeAnyJSONCasting", f:benchmarkTypeAnyJSONCasting} ;
}
