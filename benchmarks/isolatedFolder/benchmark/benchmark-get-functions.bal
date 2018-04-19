import sprintf;
import types;
import benchmarkio;
import ballerina/io;

map<function()> functions;

function getFunction(string name) returns (function()) {
    addFunctions();
    return functions[name];
}

function addFunctions() {

    functions["benchmarkTypeAnyJSONCasting"] = types:benchmarkTypeAnyJSONCasting;
    functions["benchmarkJsonStructConstraint"] = types:benchmarkJsonStructConstraint;
    functions["benchmarkJsonInitializationWithStructConstraint"] = types:benchmarkJsonInitializationWithStructConstraint;
    functions["benchmarkGetPlainJson"] = types:benchmarkGetPlainJson;
    functions["benchmarkGetConstraintJson"] = types:benchmarkGetConstraintJson;
    functions["benchmarkGetPersonJson"] = types:benchmarkGetPersonJson;
    functions["benchmarkConstrainingWithNestedRecords"] = types:benchmarkConstrainingWithNestedRecords;
    functions["benchmarkConstraintJSONToJSONCast"] = types:benchmarkConstraintJSONToJSONCast;
    functions["benchmarkJSONToConstraintJsonUnsafeCast"] = types:benchmarkJSONToConstraintJsonUnsafeCast;
    functions["benchmarkConstraintJSONToConstraintJsonCast"] = types:benchmarkConstraintJSONToConstraintJsonCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast"] = types:benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast"] = types:benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastPositive"] = types:benchmarkJSONArrayToConstraintJsonArrayCastPositive;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastNegative"] = types:benchmarkJSONArrayToConstraintJsonArrayCastNegative;
    functions["benchmarkJSONArrayToCJsonArrayCast"] = types:benchmarkJSONArrayToCJsonArrayCast;
    functions["benchmarkJSONArrayToCJsonArrayCastNegative"] = types:benchmarkJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkCJSONArrayToJsonAssignment"] = types:benchmarkCJSONArrayToJsonAssignment;
    functions["benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative"] = types:benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkConstrainedJsonWithFunctions"] = types:benchmarkConstrainedJsonWithFunctions;
    functions["benchmarkConstrainedJsonWithFunctionGetKeys"] = types:benchmarkConstrainedJsonWithFunctionGetKeys;

    functions["benchmarkPrintAnyVal"] = types:benchmarkPrintAnyVal;
    functions["benchmarkPrintlnAnyVal"] = types:benchmarkPrintlnAnyVal;

    functions["benchmarkIntAddition"] = types:benchmarkIntAddition;
    functions["benchmarkIntSubtraction"] = types:benchmarkIntSubtraction;
    functions["benchmarkIntMultiplication"] = types:benchmarkIntMultiplication;
    functions["benchmarkIntDivision"] = types:benchmarkIntDivision;
    functions["benchmarkIntTypesAddition"] = types:benchmarkIntTypesAddition;
    functions["benchmarkIntegerTypesSubtraction"] = types:benchmarkIntegerTypesSubtraction;
    functions["benchmarkIntegerTypesMultiplication"] = types:benchmarkIntegerTypesMultiplication;
    functions["benchmarkIntegerTypesDivision"] = types:benchmarkIntegerTypesDivision;

    functions["benchmarkFloatAddition"] = types:benchmarkFloatAddition;
    functions["benchmarkFloatAdditionWithReturn"] = types:benchmarkFloatAdditionWithReturn;
    functions["benchmarkFloatMultiplication"] = types:benchmarkFloatMultiplication;
    functions["benchmarkFloatMultiplicationWithReturn"] = types:benchmarkFloatMultiplicationWithReturn;
    functions["benchmarkFloatSubtraction"] = types:benchmarkFloatSubtraction;
    functions["benchmarkFloatSubtractionWithReturn"] = types:benchmarkFloatSubtractionWithReturn;
    functions["benchmarkFloatDivision"] = types:benchmarkFloatDivision;
    functions["benchmarkFloatDivisionWithReturn"] = types:benchmarkFloatDivisionWithReturn;

    functions["benchmarkCurrentTimeFunction"] = types:benchmarkCurrentTimeFunction;
    functions["benchmarkCreateTimeWithZoneIDFunction"] = types:benchmarkCreateTimeWithZoneIDFunction;
    functions["benchmarkCreateTimeWithOffsetFunction"] = types:benchmarkCreateTimeWithOffsetFunction;
    functions["benchmarkCreateTimeWithNoZoneFunction"] = types:benchmarkCreateTimeWithNoZoneFunction;
    functions["benchmarkCreateDateTimeFunction"] = types:benchmarkCreateDateTimeFunction;
    functions["benchmarkParseTimeFunction"] = types:benchmarkParseTimeFunction;
    functions["benchmarkToStringWithCreateTimeFunction"] = types:benchmarkToStringWithCreateTimeFunction;
    functions["benchmarkFormatTimeFunction"] = types:benchmarkFormatTimeFunction;
    functions["benchmarkFormatTimeToRFC1123function"] = types:benchmarkFormatTimeToRFC1123function;
    functions["benchmarkTimeGetFunctions"] = types:benchmarkTimeGetFunctions;
    functions["benchmarkGetDateFunction"] = types:benchmarkGetDateFunction;
    functions["benchmarkGetTimeFunction"] = types:benchmarkGetTimeFunction;
    functions["benchmarkAddDurationFunction"] = types:benchmarkAddDurationFunction;
    functions["benchmarkSubtractDurationFunction"] = types:benchmarkSubtractDurationFunction;
    functions["benchmarkToTimezoneFunction"] = types:benchmarkToTimezoneFunction;
    functions["benchmarkToTimezoneFunctionWithDateTime"] = types:benchmarkToTimezoneFunctionWithDateTime;
    functions["benchmarkManualTimeCreateFunction"] = types:benchmarkManualTimeCreateFunction;
    functions["benchmarkManualTimeCreateFunctionWithNoZone"] = types:benchmarkManualTimeCreateFunctionWithNoZone;
    functions["benchmarkManualTimeCreateFunctionWithEmptyZone"] = types:benchmarkManualTimeCreateFunctionWithEmptyZone;
    functions["benchmarkParseTimeFunctionWithDifferentFormats"] = types:benchmarkParseTimeFunctionWithDifferentFormats;

    functions["benchmarkSprintfWithFloat"] = sprintf:benchmarkSprintfWithFloat;
    functions["benchmarkSprintfWithString"] = sprintf:benchmarkSprintfWithString;

    functions["benchmarkinitFileChannelReadMode"] = benchmarkio:benchmarkinitFileChannelReadMode;
    functions["benchmarkinitFileChannelWriteMode"] = benchmarkio:benchmarkinitFileChannelWriteMode;
    functions["benchmarkinitFileChannelAppendMode"] = benchmarkio:benchmarkinitFileChannelAppendMode;
    functions["benchmarkreadBytes"] = benchmarkio:benchmarkreadBytes;
    functions["benchmarkwriteBytes"] = benchmarkio:benchmarkwriteBytes;
    functions["benchmarkinitCharacterChannelReadMode"] = benchmarkio:benchmarkinitCharacterChannelReadMode;
    functions["benchmarkinitCharacterChannelWriteMode"] = benchmarkio:benchmarkinitCharacterChannelWriteMode;
    functions["benchmarkinitCharacterChannelAppendMode"] = benchmarkio:benchmarkinitCharacterChannelAppendMode;
    functions["benchmarkWriteCharecters"] = benchmarkio:benchmarkWriteCharecters;
    functions["benchmarkReadCharecters"] = benchmarkio:benchmarkReadCharecters;
    functions["benchmarkReadJson"] = benchmarkio:benchmarkReadJson;
    functions["benchmarkWriteJson"] = benchmarkio:benchmarkWriteJson;
    functions["benchmarkReadXML"] = benchmarkio:benchmarkReadXML;
    functions["benchmarkWriteXML"] = benchmarkio:benchmarkWriteXML;

}

