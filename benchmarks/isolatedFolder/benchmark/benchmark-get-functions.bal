import benchmarksprintf;
import benchmarktypes;
import benchmarkio;
import ballerina/io;

map<function()> functions;

function getFunction(string name) returns (function()) {
    addFunctions();
    return functions[name];
}

function addFunctions() {

    functions["benchmarkTypeAnyJSONCasting"] = benchmarktypes:benchmarkTypeAnyJSONCasting;
    functions["benchmarkJsonStructConstraint"] = benchmarktypes:benchmarkJsonStructConstraint;
    functions["benchmarkJsonInitializationWithStructConstraint"] = benchmarktypes:benchmarkJsonInitializationWithStructConstraint;
    functions["benchmarkGetPlainJson"] = benchmarktypes:benchmarkGetPlainJson;
    functions["benchmarkGetConstraintJson"] = benchmarktypes:benchmarkGetConstraintJson;
    functions["benchmarkGetPersonJson"] = benchmarktypes:benchmarkGetPersonJson;
    functions["benchmarkConstrainingWithNestedRecords"] = benchmarktypes:benchmarkConstrainingWithNestedRecords;
    functions["benchmarkConstraintJSONToJSONCast"] = benchmarktypes:benchmarkConstraintJSONToJSONCast;
    functions["benchmarkJSONToConstraintJsonUnsafeCast"] = benchmarktypes:benchmarkJSONToConstraintJsonUnsafeCast;
    functions["benchmarkConstraintJSONToConstraintJsonCast"] = benchmarktypes:benchmarkConstraintJSONToConstraintJsonCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast"] = benchmarktypes:benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast"] = benchmarktypes:benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastPositive"] = benchmarktypes:benchmarkJSONArrayToConstraintJsonArrayCastPositive;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastNegative"] = benchmarktypes:benchmarkJSONArrayToConstraintJsonArrayCastNegative;
    functions["benchmarkJSONArrayToCJsonArrayCast"] = benchmarktypes:benchmarkJSONArrayToCJsonArrayCast;
    functions["benchmarkJSONArrayToCJsonArrayCastNegative"] = benchmarktypes:benchmarkJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkCJSONArrayToJsonAssignment"] = benchmarktypes:benchmarkCJSONArrayToJsonAssignment;
    functions["benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative"] = benchmarktypes:benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkConstrainedJsonWithFunctions"] = benchmarktypes:benchmarkConstrainedJsonWithFunctions;
    functions["benchmarkConstrainedJsonWithFunctionGetKeys"] = benchmarktypes:benchmarkConstrainedJsonWithFunctionGetKeys;

    functions["benchmarkPrintAnyVal"] = benchmarktypes:benchmarkPrintAnyVal;
    functions["benchmarkPrintlnAnyVal"] = benchmarktypes:benchmarkPrintlnAnyVal;

    functions["benchmarkIntAddition"] = benchmarktypes:benchmarkIntAddition;
    functions["benchmarkIntSubtraction"] = benchmarktypes:benchmarkIntSubtraction;
    functions["benchmarkIntMultiplication"] = benchmarktypes:benchmarkIntMultiplication;
    functions["benchmarkIntDivision"] = benchmarktypes:benchmarkIntDivision;
    functions["benchmarkIntTypesAddition"] = benchmarktypes:benchmarkIntTypesAddition;
    functions["benchmarkIntegerTypesSubtraction"] = benchmarktypes:benchmarkIntegerTypesSubtraction;
    functions["benchmarkIntegerTypesMultiplication"] = benchmarktypes:benchmarkIntegerTypesMultiplication;
    functions["benchmarkIntegerTypesDivision"] = benchmarktypes:benchmarkIntegerTypesDivision;

    functions["benchmarkFloatAddition"] = benchmarktypes:benchmarkFloatAddition;
    functions["benchmarkFloatAdditionWithReturn"] = benchmarktypes:benchmarkFloatAdditionWithReturn;
    functions["benchmarkFloatMultiplication"] = benchmarktypes:benchmarkFloatMultiplication;
    functions["benchmarkFloatMultiplicationWithReturn"] = benchmarktypes:benchmarkFloatMultiplicationWithReturn;
    functions["benchmarkFloatSubtraction"] = benchmarktypes:benchmarkFloatSubtraction;
    functions["benchmarkFloatSubtractionWithReturn"] = benchmarktypes:benchmarkFloatSubtractionWithReturn;
    functions["benchmarkFloatDivision"] = benchmarktypes:benchmarkFloatDivision;
    functions["benchmarkFloatDivisionWithReturn"] = benchmarktypes:benchmarkFloatDivisionWithReturn;

    functions["benchmarkCurrentTimeFunction"] = benchmarktypes:benchmarkCurrentTimeFunction;
    functions["benchmarkCreateTimeWithZoneIDFunction"] = benchmarktypes:benchmarkCreateTimeWithZoneIDFunction;
    functions["benchmarkCreateTimeWithOffsetFunction"] = benchmarktypes:benchmarkCreateTimeWithOffsetFunction;
    functions["benchmarkCreateTimeWithNoZoneFunction"] = benchmarktypes:benchmarkCreateTimeWithNoZoneFunction;
    functions["benchmarkCreateDateTimeFunction"] = benchmarktypes:benchmarkCreateDateTimeFunction;
    functions["benchmarkParseTimeFunction"] = benchmarktypes:benchmarkParseTimeFunction;
    functions["benchmarkToStringWithCreateTimeFunction"] = benchmarktypes:benchmarkToStringWithCreateTimeFunction;
    functions["benchmarkFormatTimeFunction"] = benchmarktypes:benchmarkFormatTimeFunction;
    functions["benchmarkFormatTimeToRFC1123function"] = benchmarktypes:benchmarkFormatTimeToRFC1123function;
    functions["benchmarkTimeGetFunctions"] = benchmarktypes:benchmarkTimeGetFunctions;
    functions["benchmarkGetDateFunction"] = benchmarktypes:benchmarkGetDateFunction;
    functions["benchmarkGetTimeFunction"] = benchmarktypes:benchmarkGetTimeFunction;
    functions["benchmarkAddDurationFunction"] = benchmarktypes:benchmarkAddDurationFunction;
    functions["benchmarkSubtractDurationFunction"] = benchmarktypes:benchmarkSubtractDurationFunction;
    functions["benchmarkToTimezoneFunction"] = benchmarktypes:benchmarkToTimezoneFunction;
    functions["benchmarkToTimezoneFunctionWithDateTime"] = benchmarktypes:benchmarkToTimezoneFunctionWithDateTime;
    functions["benchmarkManualTimeCreateFunction"] = benchmarktypes:benchmarkManualTimeCreateFunction;
    functions["benchmarkManualTimeCreateFunctionWithNoZone"] = benchmarktypes:benchmarkManualTimeCreateFunctionWithNoZone;
    functions["benchmarkManualTimeCreateFunctionWithEmptyZone"] = benchmarktypes:benchmarkManualTimeCreateFunctionWithEmptyZone;
    functions["benchmarkParseTimeFunctionWithDifferentFormats"] = benchmarktypes:benchmarkParseTimeFunctionWithDifferentFormats;

    functions["benchmarkSprintfWithFloat"] = benchmarksprintf:benchmarkSprintfWithFloat;
    functions["benchmarkSprintfWithString"] = benchmarksprintf:benchmarkSprintfWithString;

    functions["benchmarkInitFileChannelReadMode"] = benchmarkio:benchmarkInitFileChannelReadMode;
    functions["benchmarkInitFileChannelWriteMode"] = benchmarkio:benchmarkInitFileChannelWriteMode;
    functions["benchmarkInitFileChannelAppendMode"] = benchmarkio:benchmarkInitFileChannelAppendMode;
    functions["benchmarkReadBytes"] = benchmarkio:benchmarkReadBytes;
    functions["benchmarkWriteBytes"] = benchmarkio:benchmarkWriteBytes;
    functions["benchmarkInitCharacterChannelReadMode"] = benchmarkio:benchmarkInitCharacterChannelReadMode;
    functions["benchmarkInitCharacterChannelWriteMode"] = benchmarkio:benchmarkInitCharacterChannelWriteMode;
    functions["benchmarkInitCharacterChannelAppendMode"] = benchmarkio:benchmarkInitCharacterChannelAppendMode;
    functions["benchmarkWriteCharacters"] = benchmarkio:benchmarkWriteCharacters;
    functions["benchmarkReadCharacters"] = benchmarkio:benchmarkReadCharacters;
    functions["benchmarkReadJson"] = benchmarkio:benchmarkReadJson;
    functions["benchmarkWriteJson"] = benchmarkio:benchmarkWriteJson;
    functions["benchmarkReadXML"] = benchmarkio:benchmarkReadXML;
    functions["benchmarkWriteXML"] = benchmarkio:benchmarkWriteXML;

}

