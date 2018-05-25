import benchmarksprintf;
import benchmarktypes;
import benchmarkio;
import ballerina/io;

map<function()> functions;

function getFunction(string name) returns (function()) {
    return functions[name];
}

function addFunctions() {
    addJSONFunctions();
    addIntFunctions();
    addFloatFunctions();
    addTimeFunctions();
    addSprintfFunctions();
    addIoFunctions();
    addStringFunctions();
}

function addJSONFunctions() {
    functions["benchmarkTypeAnyJSONCasting"] = benchmarktypes:benchmarkTypeAnyJSONCasting;
    functions["benchmarkJsonStructConstraint"] = benchmarktypes:benchmarkJsonStructConstraint;
    functions["benchmarkJsonInitializationWithStructConstraint"] = benchmarktypes:
    benchmarkJsonInitializationWithStructConstraint;
    functions["benchmarkGetPlainJson"] = benchmarktypes:benchmarkGetPlainJson;
    functions["benchmarkGetConstraintJson"] = benchmarktypes:benchmarkGetConstraintJson;
    functions["benchmarkGetPersonJson"] = benchmarktypes:benchmarkGetPersonJson;
    functions["benchmarkConstrainingWithNestedRecords"] = benchmarktypes:benchmarkConstrainingWithNestedRecords;
    functions["benchmarkConstraintJSONToJSONCast"] = benchmarktypes:benchmarkConstraintJSONToJSONCast;
    functions["benchmarkJSONToConstraintJsonUnsafeCast"] = benchmarktypes:benchmarkJSONToConstraintJsonUnsafeCast;
    functions["benchmarkConstraintJSONToConstraintJsonCast"] = benchmarktypes:
    benchmarkConstraintJSONToConstraintJsonCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast"] = benchmarktypes:
    benchmarkConstraintJSONToConstraintJsonUnsafePositiveCast;
    functions["benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast"] = benchmarktypes:
    benchmarkConstraintJSONToConstraintJsonUnsafeNegativeCast;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastPositive"] = benchmarktypes:
    benchmarkJSONArrayToConstraintJsonArrayCastPositive;
    functions["benchmarkJSONArrayToConstraintJsonArrayCastNegative"] = benchmarktypes:
    benchmarkJSONArrayToConstraintJsonArrayCastNegative;
    functions["benchmarkJSONArrayToCJsonArrayCast"] = benchmarktypes:benchmarkJSONArrayToCJsonArrayCast;
    functions["benchmarkJSONArrayToCJsonArrayCastNegative"] = benchmarktypes:benchmarkJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkCJSONArrayToJsonAssignment"] = benchmarktypes:benchmarkCJSONArrayToJsonAssignment;
    functions["benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative"] = benchmarktypes:
    benchmarkMixedTypeJSONArrayToCJsonArrayCastNegative;
    functions["benchmarkConstrainedJsonWithFunctions"] = benchmarktypes:benchmarkConstrainedJsonWithFunctions;
    functions["benchmarkConstrainedJsonWithFunctionGetKeys"] = benchmarktypes:
    benchmarkConstrainedJsonWithFunctionGetKeys;
}

function addIntFunctions() {
    functions["benchmarkIntAddition"] = benchmarktypes:benchmarkIntAddition;
    functions["benchmarkIntSubtraction"] = benchmarktypes:benchmarkIntSubtraction;
    functions["benchmarkIntMultiplication"] = benchmarktypes:benchmarkIntMultiplication;
    functions["benchmarkIntDivision"] = benchmarktypes:benchmarkIntDivision;
    functions["benchmarkIntTypesAddition"] = benchmarktypes:benchmarkIntTypesAddition;
    functions["benchmarkIntegerTypesSubtraction"] = benchmarktypes:benchmarkIntegerTypesSubtraction;
    functions["benchmarkIntegerTypesMultiplication"] = benchmarktypes:benchmarkIntegerTypesMultiplication;
    functions["benchmarkIntegerTypesDivision"] = benchmarktypes:benchmarkIntegerTypesDivision;
}

function addPrintFunctions() {
    functions["benchmarkPrintAnyVal"] = benchmarktypes:benchmarkPrintAnyVal;
    functions["benchmarkPrintlnAnyVal"] = benchmarktypes:benchmarkPrintlnAnyVal;
}

function addFloatFunctions() {
    functions["benchmarkFloatAddition"] = benchmarktypes:benchmarkFloatAddition;
    functions["benchmarkFloatAdditionWithReturn"] = benchmarktypes:benchmarkFloatAdditionWithReturn;
    functions["benchmarkFloatMultiplication"] = benchmarktypes:benchmarkFloatMultiplication;
    functions["benchmarkFloatMultiplicationWithReturn"] = benchmarktypes:benchmarkFloatMultiplicationWithReturn;
    functions["benchmarkFloatSubtraction"] = benchmarktypes:benchmarkFloatSubtraction;
    functions["benchmarkFloatSubtractionWithReturn"] = benchmarktypes:benchmarkFloatSubtractionWithReturn;
    functions["benchmarkFloatDivision"] = benchmarktypes:benchmarkFloatDivision;
    functions["benchmarkFloatDivisionWithReturn"] = benchmarktypes:benchmarkFloatDivisionWithReturn;
}

function addTimeFunctions() {
    functions["benchmarkCurrentTimeFunction"] = benchmarktypes:benchmarkCurrentTimeFunction;
    functions["benchmarkCreateTimeWithZoneIDFunction"] = benchmarktypes:benchmarkCreateTimeWithZoneIDFunction;
    functions["benchmarkCreateTimeWithOffsetFunction"] = benchmarktypes:benchmarkCreateTimeWithOffsetFunction;
    functions["benchmarkCreateTimeWithNoZoneFunction"] = benchmarktypes:benchmarkCreateTimeWithNoZoneFunction;
    functions["benchmarkCreateDateTimeFunction"] = benchmarktypes:benchmarkCreateDateTimeFunction;
    functions["benchmarkParseTimeFunction"] = benchmarktypes:benchmarkParseTimeFunction;
    functions["benchmarkToStringWithCreateTimeFunction"] = benchmarktypes:benchmarkToStringWithCreateTimeFunction;
    functions["benchmarkFormatTimeFunction"] = benchmarktypes:benchmarkFormatTimeFunction;
    functions["benchmarkTimeGetFunctions"] = benchmarktypes:benchmarkTimeGetFunctions;
    functions["benchmarkGetDateFunction"] = benchmarktypes:benchmarkGetDateFunction;
    functions["benchmarkGetTimeFunction"] = benchmarktypes:benchmarkGetTimeFunction;
    functions["benchmarkAddDurationFunction"] = benchmarktypes:benchmarkAddDurationFunction;
    functions["benchmarkSubtractDurationFunction"] = benchmarktypes:benchmarkSubtractDurationFunction;
    functions["benchmarkToTimezoneFunction"] = benchmarktypes:benchmarkToTimezoneFunction;
    functions["benchmarkToTimezoneFunctionWithDateTime"] = benchmarktypes:benchmarkToTimezoneFunctionWithDateTime;
    functions["benchmarkManualTimeCreateFunction"] = benchmarktypes:benchmarkManualTimeCreateFunction;
    functions["benchmarkManualTimeCreateFunctionWithNoZone"] = benchmarktypes:
    benchmarkManualTimeCreateFunctionWithNoZone;
    functions["benchmarkManualTimeCreateFunctionWithEmptyZone"] = benchmarktypes:
    benchmarkManualTimeCreateFunctionWithEmptyZone;
    functions["benchmarkParseTimeFunctionWithDifferentFormats"] = benchmarktypes:
    benchmarkParseTimeFunctionWithDifferentFormats;
}

function addSprintfFunctions() {
    functions["benchmarkSprintfWithFloat"] = benchmarksprintf:benchmarkSprintfWithFloat;
    functions["benchmarkSprintfWithString"] = benchmarksprintf:benchmarkSprintfWithString;
}

function addIoFunctions() {
    functions["benchmarkInitFileChannelReadMode"] = benchmarkio:benchmarkInitFileChannelReadMode;
    functions["benchmarkInitFileChannelWriteMode"] = benchmarkio:benchmarkInitFileChannelWriteMode;
    functions["benchmarkInitFileChannelAppendMode"] = benchmarkio:benchmarkInitFileChannelAppendMode;
    functions["benchmarkReadBytes"] = benchmarkio:benchmarkReadBytes;
    functions["benchmarkWriteBytes"] = benchmarkio:benchmarkWriteBytes;
}

function addStringFunctions() {
    functions["benchmarkStringContains"] = benchmarktypes:benchmarkStringContains;
    functions["benchmarkStringEqualsIgnoreCase"] = benchmarktypes:benchmarkStringEqualsIgnoreCase;
    functions["benchmarkStringConcat"] = benchmarktypes:benchmarkStringConcat;
    functions["benchmarkStringHasPrefix"] = benchmarktypes:benchmarkStringHasPrefix;
    functions["benchmarkStringHasSuffix"] = benchmarktypes:benchmarkStringHasSuffix;
    functions["benchmarkStringIndexOf"] = benchmarktypes:benchmarkStringIndexOf;
    functions["benchmarkStringLastIndexOf"] = benchmarktypes:benchmarkStringLastIndexOf;
    functions["benchmarkStringReplace"] = benchmarktypes:benchmarkStringReplace;
    functions["benchmarkStringReplaceAll"] = benchmarktypes:benchmarkStringReplaceAll;
    functions["benchmarkStringReplaceFirst"] = benchmarktypes:benchmarkStringReplaceFirst;
    functions["benchmarkStringSubstring"] = benchmarktypes:benchmarkStringSubstring;
    functions["benchmarkStringToLower"] = benchmarktypes:benchmarkStringToLower;
    functions["benchmarkStringToUpper"] = benchmarktypes:benchmarkStringToUpper;
    functions["benchmarkStringTrim"] = benchmarktypes:benchmarkStringTrim;
    functions["benchmarkStringIntValueOf"] = benchmarktypes:benchmarkStringIntValueOf;
    functions["benchmarkStringFloatValueOf"] = benchmarktypes:benchmarkStringFloatValueOf;
    functions["benchmarkStringBooleanValueOf"] = benchmarktypes:benchmarkStringBooleanValueOf;
    functions["benchmarkStringValueOf"] = benchmarktypes:benchmarkStringValueOf;
    functions["benchmarkXmlValueOf"] = benchmarktypes:benchmarkXmlValueOf;
    functions["benchmarkStringJsonValueOf"] = benchmarktypes:benchmarkStringJsonValueOf;
    functions["benchmarkStringLength"] = benchmarktypes:benchmarkStringLength;
    functions["benchmarkStringSplit"] = benchmarktypes:benchmarkStringSplit;
    functions["benchmarkStringUnescape"] = benchmarktypes:benchmarkStringUnescape;
}
