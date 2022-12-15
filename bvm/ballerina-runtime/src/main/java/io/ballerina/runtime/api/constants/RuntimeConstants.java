/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.api.constants;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This file contains a list of constant values used by Ballerina Runtime.
 *
 * @since 0.90
 */
public class RuntimeConstants {

    public static final String MAIN_FUNCTION_NAME = "main";
    public static final String MODULE_INIT_CLASS_NAME = "$_init";
    public static final String TEST_PACKAGE_NAME = "$test";
    public static final String FILE_NAME_PERIOD_SEPARATOR = "$$$";

    // Configs
    public static final String BALLERINA_ARGS_INIT_PREFIX = "--";
    public static final int BALLERINA_ARGS_INIT_PREFIX_LENGTH = BALLERINA_ARGS_INIT_PREFIX.length();
    public static final String CONFIG_SEPARATOR = "=";
    public static final String CONFIG_FILE_PROPERTY = "b7a.config.file";

    public static final String EMPTY = "";
    public static final String ANON_ORG = "$anon";
    public static final String DOT = ".";
    public static final String ORG_NAME_SEPARATOR = "/";
    public static final String VERSION_SEPARATOR = ":";
    public static final String UNDERSCORE = "_";
    public static final String COLON = ":";
    public static final char DOLLAR = '$';

    public static final String BLANG_SRC_FILE_EXT = "bal";
    public static final String BLANG_SRC_FILE_SUFFIX = "." + BLANG_SRC_FILE_EXT;

    public static final String BALLERINA_BUILTIN_PKG_PREFIX = "ballerina";

    public static final String USER_HOME = "user.home";
    public static final String BALLERINA_HOME = "ballerina.home";
    public static final String BALLERINA_PACKAGE_PREFIX = "ballerina" + ORG_NAME_SEPARATOR;
    public static final String BALLERINA_MAX_POOL_SIZE_ENV_VAR = "BALLERINA_MAX_POOL_SIZE";
    public static final Module BALLERINA_LANG_ERROR_PKG_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX,
                                                                        "lang.error", "0");

    // Lang libs
    public static final String STRING_LANG_LIB = "lang.string";
    public static final String MAP_LANG_LIB = "lang.map";
    public static final String ARRAY_LANG_LIB = "lang.array";
    public static final String TYPEDESC_LANG_LIB = "lang.typedesc";
    public static final String VALUE_LANG_LIB = "lang.value";
    public static final String XML_LANG_LIB = "lang.xml";
    public static final String FUNCTION_LANG_LIB = "lang.function";
    public static final String FUTURE_LANG_LIB = "lang.future";
    public static final String OBJECT_LANG_LIB = "lang.object";
    public static final String TABLE_LANG_LIB = "lang.table";
    public static final String INT_LANG_LIB = "lang.int";
    public static final String FLOAT_LANG_LIB = "lang.float";
    public static final String DECIMAL_LANG_LIB = "lang.decimal";
    public static final String BOOLEAN_LANG_LIB = "lang.boolean";
    public static final String TRANSACTION_LANG_LIB = "lang.transaction";
    public static final String REGEXP_LANG_LIB = "lang.regexp";

    // Workers
    public static final String DEFAULT_WORKER_NAME = "default";

    // Zero value for string
    public static final String STRING_NULL_VALUE = null;
    public static final BString BSTRING_NULL_VALUE = null;

    // Empty value for string
    public static final BString STRING_EMPTY_VALUE = StringUtils.fromString("");

    public static final Integer BBYTE_MIN_VALUE = 0;
    public static final Integer BBYTE_MAX_VALUE = 255;
    public static final Integer SIGNED32_MAX_VALUE = 2147483647;
    public static final Integer SIGNED32_MIN_VALUE = -2147483648;
    public static final Integer SIGNED16_MAX_VALUE = 32767;
    public static final Integer SIGNED16_MIN_VALUE = -32768;
    public static final Integer SIGNED8_MAX_VALUE = 127;
    public static final Integer SIGNED8_MIN_VALUE = -128;
    public static final Long UNSIGNED32_MAX_VALUE = 4294967295L;
    public static final Integer UNSIGNED16_MAX_VALUE = 65535;
    public static final Integer UNSIGNED8_MAX_VALUE = 255;
    public static final double BINT_MAX_VALUE_DOUBLE_RANGE_MAX = 9223372036854775807.5;
    public static final double BINT_MIN_VALUE_DOUBLE_RANGE_MIN = -9223372036854775807.6;
    public static final BigDecimal BINT_MAX_VALUE_BIG_DECIMAL_RANGE_MAX = new BigDecimal("9223372036854775807.5",
                                                                                         MathContext.DECIMAL128);
    public static final BigDecimal BINT_MIN_VALUE_BIG_DECIMAL_RANGE_MIN = new BigDecimal("-9223372036854775808.5",
                                                                                         MathContext.DECIMAL128);
    // runtime related error message constant values
    public static final String INTERNAL_ERROR_MESSAGE =
            "ballerina: Oh no, something really went wrong. Bad. Sad.\n" +
            "\n" +
            "We appreciate it if you can report the code that broke Ballerina in\n" +
            "https://github.com/ballerina-platform/ballerina-lang/issues with the\n" +
            "log you get below and your sample code.\n" +
            "\n" +
            "We thank you for helping make us better.\n";

    public static final String DEFAULT_LOG_FILE_HANDLER_PATTERN =
            "org.ballerinalang.logging.handlers.DefaultLogFileHandler.pattern";


    // Ballerina version system property name
    public static final String BALLERINA_VERSION = "ballerina.version";

    // Name of the system property to hold the debug port
    public static final String SYSTEM_PROP_BAL_DEBUG = "debug";

    // Transaction constants
    public static final String GLOBAL_TRANSACTION_ID = "globalTransactionId";
    public static final String TRANSACTION_URL = "transactionUrl";
    public static final String TRANSACTION_INFO = "transactionInfo";

    // Instance id key
    public static final String STATE_ID = "b7a.state.id";
    public static final String IS_INTERRUPTIBLE = "b7a.state.interruptible";

    // Serialization related Constants
    public static final String TYPE = "type";
    public static final String DATA = "data";
    public static final String NULL = "null";

    // Default worker name
    public static final String DEFAULT = "default";

    public static final String DISTRIBUTED_TRANSACTIONS = "b7a.distributed.transactions.enabled";

    public static final String FALSE = "false";

    public static final String CURRENT_TRANSACTION_CONTEXT_PROPERTY = "currentTrxContext";

}
