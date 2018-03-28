// Generated from Toml.g4 by ANTLR 4.5.3
package org.ballerinalang.toml.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TomlParser}.
 */
public interface TomlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TomlParser#toml}.
	 * @param ctx the parse tree
	 */
	void enterToml(TomlParser.TomlContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#toml}.
	 * @param ctx the parse tree
	 */
	void exitToml(TomlParser.TomlContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#alpha}.
	 * @param ctx the parse tree
	 */
	void enterAlpha(TomlParser.AlphaContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#alpha}.
	 * @param ctx the parse tree
	 */
	void exitAlpha(TomlParser.AlphaContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(TomlParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(TomlParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#ws}.
	 * @param ctx the parse tree
	 */
	void enterWs(TomlParser.WsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#ws}.
	 * @param ctx the parse tree
	 */
	void exitWs(TomlParser.WsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#wschar}.
	 * @param ctx the parse tree
	 */
	void enterWschar(TomlParser.WscharContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#wschar}.
	 * @param ctx the parse tree
	 */
	void exitWschar(TomlParser.WscharContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#newline}.
	 * @param ctx the parse tree
	 */
	void enterNewline(TomlParser.NewlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#newline}.
	 * @param ctx the parse tree
	 */
	void exitNewline(TomlParser.NewlineContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#keyVal}.
	 * @param ctx the parse tree
	 */
	void enterKeyVal(TomlParser.KeyValContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#keyVal}.
	 * @param ctx the parse tree
	 */
	void exitKeyVal(TomlParser.KeyValContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(TomlParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(TomlParser.KeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#simpleKey}.
	 * @param ctx the parse tree
	 */
	void enterSimpleKey(TomlParser.SimpleKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#simpleKey}.
	 * @param ctx the parse tree
	 */
	void exitSimpleKey(TomlParser.SimpleKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#unquotedKey}.
	 * @param ctx the parse tree
	 */
	void enterUnquotedKey(TomlParser.UnquotedKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#unquotedKey}.
	 * @param ctx the parse tree
	 */
	void exitUnquotedKey(TomlParser.UnquotedKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#quotedKey}.
	 * @param ctx the parse tree
	 */
	void enterQuotedKey(TomlParser.QuotedKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#quotedKey}.
	 * @param ctx the parse tree
	 */
	void exitQuotedKey(TomlParser.QuotedKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dottedKey}.
	 * @param ctx the parse tree
	 */
	void enterDottedKey(TomlParser.DottedKeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dottedKey}.
	 * @param ctx the parse tree
	 */
	void exitDottedKey(TomlParser.DottedKeyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#keyValSep}.
	 * @param ctx the parse tree
	 */
	void enterKeyValSep(TomlParser.KeyValSepContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#keyValSep}.
	 * @param ctx the parse tree
	 */
	void exitKeyValSep(TomlParser.KeyValSepContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dotSep}.
	 * @param ctx the parse tree
	 */
	void enterDotSep(TomlParser.DotSepContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dotSep}.
	 * @param ctx the parse tree
	 */
	void exitDotSep(TomlParser.DotSepContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#val}.
	 * @param ctx the parse tree
	 */
	void enterVal(TomlParser.ValContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#val}.
	 * @param ctx the parse tree
	 */
	void exitVal(TomlParser.ValContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(TomlParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(TomlParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#basicString}.
	 * @param ctx the parse tree
	 */
	void enterBasicString(TomlParser.BasicStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#basicString}.
	 * @param ctx the parse tree
	 */
	void exitBasicString(TomlParser.BasicStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#basicStringValue}.
	 * @param ctx the parse tree
	 */
	void enterBasicStringValue(TomlParser.BasicStringValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#basicStringValue}.
	 * @param ctx the parse tree
	 */
	void exitBasicStringValue(TomlParser.BasicStringValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#basicChar}.
	 * @param ctx the parse tree
	 */
	void enterBasicChar(TomlParser.BasicCharContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#basicChar}.
	 * @param ctx the parse tree
	 */
	void exitBasicChar(TomlParser.BasicCharContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#digit}.
	 * @param ctx the parse tree
	 */
	void enterDigit(TomlParser.DigitContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#digit}.
	 * @param ctx the parse tree
	 */
	void exitDigit(TomlParser.DigitContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#escaped}.
	 * @param ctx the parse tree
	 */
	void enterEscaped(TomlParser.EscapedContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#escaped}.
	 * @param ctx the parse tree
	 */
	void exitEscaped(TomlParser.EscapedContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#escapeSeqChar}.
	 * @param ctx the parse tree
	 */
	void enterEscapeSeqChar(TomlParser.EscapeSeqCharContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#escapeSeqChar}.
	 * @param ctx the parse tree
	 */
	void exitEscapeSeqChar(TomlParser.EscapeSeqCharContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlBasicString}.
	 * @param ctx the parse tree
	 */
	void enterMlBasicString(TomlParser.MlBasicStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlBasicString}.
	 * @param ctx the parse tree
	 */
	void exitMlBasicString(TomlParser.MlBasicStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlBasicStringDelim}.
	 * @param ctx the parse tree
	 */
	void enterMlBasicStringDelim(TomlParser.MlBasicStringDelimContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlBasicStringDelim}.
	 * @param ctx the parse tree
	 */
	void exitMlBasicStringDelim(TomlParser.MlBasicStringDelimContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlBasicBody}.
	 * @param ctx the parse tree
	 */
	void enterMlBasicBody(TomlParser.MlBasicBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlBasicBody}.
	 * @param ctx the parse tree
	 */
	void exitMlBasicBody(TomlParser.MlBasicBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlBasicChar}.
	 * @param ctx the parse tree
	 */
	void enterMlBasicChar(TomlParser.MlBasicCharContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlBasicChar}.
	 * @param ctx the parse tree
	 */
	void exitMlBasicChar(TomlParser.MlBasicCharContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#literalString}.
	 * @param ctx the parse tree
	 */
	void enterLiteralString(TomlParser.LiteralStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#literalString}.
	 * @param ctx the parse tree
	 */
	void exitLiteralString(TomlParser.LiteralStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlLiteralString}.
	 * @param ctx the parse tree
	 */
	void enterMlLiteralString(TomlParser.MlLiteralStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlLiteralString}.
	 * @param ctx the parse tree
	 */
	void exitMlLiteralString(TomlParser.MlLiteralStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlLiteralStringDelim}.
	 * @param ctx the parse tree
	 */
	void enterMlLiteralStringDelim(TomlParser.MlLiteralStringDelimContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlLiteralStringDelim}.
	 * @param ctx the parse tree
	 */
	void exitMlLiteralStringDelim(TomlParser.MlLiteralStringDelimContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#mlLiteralBody}.
	 * @param ctx the parse tree
	 */
	void enterMlLiteralBody(TomlParser.MlLiteralBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#mlLiteralBody}.
	 * @param ctx the parse tree
	 */
	void exitMlLiteralBody(TomlParser.MlLiteralBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#integer}.
	 * @param ctx the parse tree
	 */
	void enterInteger(TomlParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#integer}.
	 * @param ctx the parse tree
	 */
	void exitInteger(TomlParser.IntegerContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#minus}.
	 * @param ctx the parse tree
	 */
	void enterMinus(TomlParser.MinusContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#minus}.
	 * @param ctx the parse tree
	 */
	void exitMinus(TomlParser.MinusContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#hexPrefix}.
	 * @param ctx the parse tree
	 */
	void enterHexPrefix(TomlParser.HexPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#hexPrefix}.
	 * @param ctx the parse tree
	 */
	void exitHexPrefix(TomlParser.HexPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#octPrefix}.
	 * @param ctx the parse tree
	 */
	void enterOctPrefix(TomlParser.OctPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#octPrefix}.
	 * @param ctx the parse tree
	 */
	void exitOctPrefix(TomlParser.OctPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#binPrefix}.
	 * @param ctx the parse tree
	 */
	void enterBinPrefix(TomlParser.BinPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#binPrefix}.
	 * @param ctx the parse tree
	 */
	void exitBinPrefix(TomlParser.BinPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#decInt}.
	 * @param ctx the parse tree
	 */
	void enterDecInt(TomlParser.DecIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#decInt}.
	 * @param ctx the parse tree
	 */
	void exitDecInt(TomlParser.DecIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#unsignedDecInt}.
	 * @param ctx the parse tree
	 */
	void enterUnsignedDecInt(TomlParser.UnsignedDecIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#unsignedDecInt}.
	 * @param ctx the parse tree
	 */
	void exitUnsignedDecInt(TomlParser.UnsignedDecIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#hexInt}.
	 * @param ctx the parse tree
	 */
	void enterHexInt(TomlParser.HexIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#hexInt}.
	 * @param ctx the parse tree
	 */
	void exitHexInt(TomlParser.HexIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#octInt}.
	 * @param ctx the parse tree
	 */
	void enterOctInt(TomlParser.OctIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#octInt}.
	 * @param ctx the parse tree
	 */
	void exitOctInt(TomlParser.OctIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#binInt}.
	 * @param ctx the parse tree
	 */
	void enterBinInt(TomlParser.BinIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#binInt}.
	 * @param ctx the parse tree
	 */
	void exitBinInt(TomlParser.BinIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#floatingPoint}.
	 * @param ctx the parse tree
	 */
	void enterFloatingPoint(TomlParser.FloatingPointContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#floatingPoint}.
	 * @param ctx the parse tree
	 */
	void exitFloatingPoint(TomlParser.FloatingPointContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#floatIntPart}.
	 * @param ctx the parse tree
	 */
	void enterFloatIntPart(TomlParser.FloatIntPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#floatIntPart}.
	 * @param ctx the parse tree
	 */
	void exitFloatIntPart(TomlParser.FloatIntPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#frac}.
	 * @param ctx the parse tree
	 */
	void enterFrac(TomlParser.FracContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#frac}.
	 * @param ctx the parse tree
	 */
	void exitFrac(TomlParser.FracContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#decimalPoint}.
	 * @param ctx the parse tree
	 */
	void enterDecimalPoint(TomlParser.DecimalPointContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#decimalPoint}.
	 * @param ctx the parse tree
	 */
	void exitDecimalPoint(TomlParser.DecimalPointContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#zeroPrefixableInt}.
	 * @param ctx the parse tree
	 */
	void enterZeroPrefixableInt(TomlParser.ZeroPrefixableIntContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#zeroPrefixableInt}.
	 * @param ctx the parse tree
	 */
	void exitZeroPrefixableInt(TomlParser.ZeroPrefixableIntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#exp}.
	 * @param ctx the parse tree
	 */
	void enterExp(TomlParser.ExpContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#exp}.
	 * @param ctx the parse tree
	 */
	void exitExp(TomlParser.ExpContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#specialFloat}.
	 * @param ctx the parse tree
	 */
	void enterSpecialFloat(TomlParser.SpecialFloatContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#specialFloat}.
	 * @param ctx the parse tree
	 */
	void exitSpecialFloat(TomlParser.SpecialFloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#bool}.
	 * @param ctx the parse tree
	 */
	void enterBool(TomlParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#bool}.
	 * @param ctx the parse tree
	 */
	void exitBool(TomlParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void enterDateTime(TomlParser.DateTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dateTime}.
	 * @param ctx the parse tree
	 */
	void exitDateTime(TomlParser.DateTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dateFullyear}.
	 * @param ctx the parse tree
	 */
	void enterDateFullyear(TomlParser.DateFullyearContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dateFullyear}.
	 * @param ctx the parse tree
	 */
	void exitDateFullyear(TomlParser.DateFullyearContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dateMonth}.
	 * @param ctx the parse tree
	 */
	void enterDateMonth(TomlParser.DateMonthContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dateMonth}.
	 * @param ctx the parse tree
	 */
	void exitDateMonth(TomlParser.DateMonthContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#dateMday}.
	 * @param ctx the parse tree
	 */
	void enterDateMday(TomlParser.DateMdayContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#dateMday}.
	 * @param ctx the parse tree
	 */
	void exitDateMday(TomlParser.DateMdayContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeDelim}.
	 * @param ctx the parse tree
	 */
	void enterTimeDelim(TomlParser.TimeDelimContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeDelim}.
	 * @param ctx the parse tree
	 */
	void exitTimeDelim(TomlParser.TimeDelimContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeHour}.
	 * @param ctx the parse tree
	 */
	void enterTimeHour(TomlParser.TimeHourContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeHour}.
	 * @param ctx the parse tree
	 */
	void exitTimeHour(TomlParser.TimeHourContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeMinute}.
	 * @param ctx the parse tree
	 */
	void enterTimeMinute(TomlParser.TimeMinuteContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeMinute}.
	 * @param ctx the parse tree
	 */
	void exitTimeMinute(TomlParser.TimeMinuteContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeSecond}.
	 * @param ctx the parse tree
	 */
	void enterTimeSecond(TomlParser.TimeSecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeSecond}.
	 * @param ctx the parse tree
	 */
	void exitTimeSecond(TomlParser.TimeSecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeSecfrac}.
	 * @param ctx the parse tree
	 */
	void enterTimeSecfrac(TomlParser.TimeSecfracContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeSecfrac}.
	 * @param ctx the parse tree
	 */
	void exitTimeSecfrac(TomlParser.TimeSecfracContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeNumoffset}.
	 * @param ctx the parse tree
	 */
	void enterTimeNumoffset(TomlParser.TimeNumoffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeNumoffset}.
	 * @param ctx the parse tree
	 */
	void exitTimeNumoffset(TomlParser.TimeNumoffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#timeOffset}.
	 * @param ctx the parse tree
	 */
	void enterTimeOffset(TomlParser.TimeOffsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#timeOffset}.
	 * @param ctx the parse tree
	 */
	void exitTimeOffset(TomlParser.TimeOffsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#partialTime}.
	 * @param ctx the parse tree
	 */
	void enterPartialTime(TomlParser.PartialTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#partialTime}.
	 * @param ctx the parse tree
	 */
	void exitPartialTime(TomlParser.PartialTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#fullDate}.
	 * @param ctx the parse tree
	 */
	void enterFullDate(TomlParser.FullDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#fullDate}.
	 * @param ctx the parse tree
	 */
	void exitFullDate(TomlParser.FullDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#fullTime}.
	 * @param ctx the parse tree
	 */
	void enterFullTime(TomlParser.FullTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#fullTime}.
	 * @param ctx the parse tree
	 */
	void exitFullTime(TomlParser.FullTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#offsetDateTime}.
	 * @param ctx the parse tree
	 */
	void enterOffsetDateTime(TomlParser.OffsetDateTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#offsetDateTime}.
	 * @param ctx the parse tree
	 */
	void exitOffsetDateTime(TomlParser.OffsetDateTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#localDateTime}.
	 * @param ctx the parse tree
	 */
	void enterLocalDateTime(TomlParser.LocalDateTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#localDateTime}.
	 * @param ctx the parse tree
	 */
	void exitLocalDateTime(TomlParser.LocalDateTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#localDate}.
	 * @param ctx the parse tree
	 */
	void enterLocalDate(TomlParser.LocalDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#localDate}.
	 * @param ctx the parse tree
	 */
	void exitLocalDate(TomlParser.LocalDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#localTime}.
	 * @param ctx the parse tree
	 */
	void enterLocalTime(TomlParser.LocalTimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#localTime}.
	 * @param ctx the parse tree
	 */
	void exitLocalTime(TomlParser.LocalTimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(TomlParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(TomlParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#arrayOpen}.
	 * @param ctx the parse tree
	 */
	void enterArrayOpen(TomlParser.ArrayOpenContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#arrayOpen}.
	 * @param ctx the parse tree
	 */
	void exitArrayOpen(TomlParser.ArrayOpenContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#arrayClose}.
	 * @param ctx the parse tree
	 */
	void enterArrayClose(TomlParser.ArrayCloseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#arrayClose}.
	 * @param ctx the parse tree
	 */
	void exitArrayClose(TomlParser.ArrayCloseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#arrayValues}.
	 * @param ctx the parse tree
	 */
	void enterArrayValues(TomlParser.ArrayValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#arrayValues}.
	 * @param ctx the parse tree
	 */
	void exitArrayValues(TomlParser.ArrayValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#arrayvalsNonEmpty}.
	 * @param ctx the parse tree
	 */
	void enterArrayvalsNonEmpty(TomlParser.ArrayvalsNonEmptyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#arrayvalsNonEmpty}.
	 * @param ctx the parse tree
	 */
	void exitArrayvalsNonEmpty(TomlParser.ArrayvalsNonEmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#arraySep}.
	 * @param ctx the parse tree
	 */
	void enterArraySep(TomlParser.ArraySepContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#arraySep}.
	 * @param ctx the parse tree
	 */
	void exitArraySep(TomlParser.ArraySepContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void enterInlineTable(TomlParser.InlineTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTable}.
	 * @param ctx the parse tree
	 */
	void exitInlineTable(TomlParser.InlineTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTableOpen}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableOpen(TomlParser.InlineTableOpenContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTableOpen}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableOpen(TomlParser.InlineTableOpenContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTableClose}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableClose(TomlParser.InlineTableCloseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTableClose}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableClose(TomlParser.InlineTableCloseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTableSep}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableSep(TomlParser.InlineTableSepContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTableSep}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableSep(TomlParser.InlineTableSepContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTableKeyvals}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableKeyvals(TomlParser.InlineTableKeyvalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTableKeyvals}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableKeyvals(TomlParser.InlineTableKeyvalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#inlineTableKeyvalsNonEmpty}.
	 * @param ctx the parse tree
	 */
	void enterInlineTableKeyvalsNonEmpty(TomlParser.InlineTableKeyvalsNonEmptyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#inlineTableKeyvalsNonEmpty}.
	 * @param ctx the parse tree
	 */
	void exitInlineTableKeyvalsNonEmpty(TomlParser.InlineTableKeyvalsNonEmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#table}.
	 * @param ctx the parse tree
	 */
	void enterTable(TomlParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#table}.
	 * @param ctx the parse tree
	 */
	void exitTable(TomlParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#stdTable}.
	 * @param ctx the parse tree
	 */
	void enterStdTable(TomlParser.StdTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#stdTable}.
	 * @param ctx the parse tree
	 */
	void exitStdTable(TomlParser.StdTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#stdTableOpen}.
	 * @param ctx the parse tree
	 */
	void enterStdTableOpen(TomlParser.StdTableOpenContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#stdTableOpen}.
	 * @param ctx the parse tree
	 */
	void exitStdTableOpen(TomlParser.StdTableOpenContext ctx);
	/**
	 * Enter a parse tree produced by {@link TomlParser#stdTableClose}.
	 * @param ctx the parse tree
	 */
	void enterStdTableClose(TomlParser.StdTableCloseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TomlParser#stdTableClose}.
	 * @param ctx the parse tree
	 */
	void exitStdTableClose(TomlParser.StdTableCloseContext ctx);
}