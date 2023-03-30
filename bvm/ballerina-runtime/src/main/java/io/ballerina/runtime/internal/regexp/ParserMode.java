/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.regexp;

/**
 * Modes of parsing.
 *
 * @since 2201.3.0
 */
public enum ParserMode {
    RE_DISJUNCTION,
    RE_CHAR_SET,
    RE_NEGATED_CHAR_CLASS_START,
    RE_CHAR_SET_RANGE_RHS,
    RE_CHAR_SET_RANGE_RHS_START,
    RE_CHAR_SET_RANGE_NO_DASH_RHS,
    RE_CHAR_SET_RANGE_NO_DASH_RHS_START,
    RE_CHAR_SET_NO_DASH,
    RE_CAPTURING_GROUP_RE_DISJUNCTION,
    RE_QUANTIFIER,
    RE_NON_GREEDY_QUANTIFIER,
    RE_BRACED_QUANTIFIER,
    RE_BRACED_QUANTIFIER_LEAST_DIGITS,
    RE_BRACED_QUANTIFIER_MOST_DIGITS,
    RE_BRACED_QUANTIFIER_MOST_DIGITS_START,
    RE_FLAGS,
    RE_FLAGS_START,
    RE_UNICODE_PROP_ESCAPE,
    RE_UNICODE_PROP_START,
    RE_UNICODE_PROPERTY_VALUE,
    RE_UNICODE_GENERAL_CATEGORY_NAME,
    RE_ESCAPE
}
