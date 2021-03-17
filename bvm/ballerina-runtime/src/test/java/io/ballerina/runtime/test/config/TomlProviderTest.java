/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.test.config;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.ballerina.runtime.test.TestUtils.getConfigPath;

/**
 * Test cases for toml configuration related implementations.
 */
public class TomlProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1.0.0");

    @Test(dataProvider = "arrays-data-provider")
    public void testConfigurableArrays(VariableKey arrayKey,
                                       Function<BArray, Object[]> arrayGetFunction, Object[] expectedArray) {
        Module module = new Module("myorg", "arrays", "1.0.0");
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = new VariableKey[]{arrayKey};
        configVarMap.put(module, keys);
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                                                           new ConfigTomlProvider(getConfigPath("Array_Config.toml")));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertTrue(configValueMap.get(arrayKey) instanceof BArray);
        Object[] configuredArrayValues = arrayGetFunction.apply((BArray) configValueMap.get(arrayKey));
        for (int i = 0; i < configuredArrayValues.length; i++) {
            Assert.assertEquals(configuredArrayValues[i], expectedArray[i]);
        }
    }

    @DataProvider(name = "arrays-data-provider")
    public Object[][] arrayDataProvider() {
        Module module = new Module("myorg", "arrays", "1.0.0");
        Function<BArray, Object[]> intArrayGetFunction = bArray -> new Object[]{bArray.getIntArray()};
        Function<BArray, Object[]> byteArrayGetFunction = bArray -> new Object[]{bArray.getByteArray()};
        Function<BArray, Object[]> floatArrayGetFunction = bArray -> new Object[]{bArray.getFloatArray()};
        Function<BArray, Object[]> stringArrayGetFunction = BArray::getStringArray;
        Function<BArray, Object[]> booleanArrayGetFunction = bArray -> new Object[]{bArray.getBooleanArray()};
        Function<BArray, Object[]> decimalArrayGetFunction = bArray -> bArray.getValues();
        BDecimal[] expectedDecimalArray = new BDecimal[100];
        expectedDecimalArray[0] = ValueCreator.createDecimalValue("8.9");
        expectedDecimalArray[1] = ValueCreator.createDecimalValue("4.5");
        expectedDecimalArray[2] = ValueCreator.createDecimalValue("6.2");
        return new Object[][]{
                {new VariableKey(module, "intArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_INT), 0, false), true), intArrayGetFunction,
                        new long[]{123456, 1234567, 987654321}
                },
                {new VariableKey(module, "byteArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BYTE), 0, false), true), byteArrayGetFunction,
                        new byte[]{1, 2, 3}},
                {new VariableKey(module, "floatArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_FLOAT), 0, false), true), floatArrayGetFunction,
                        new double[]{9.0, 5.6}},
                {new VariableKey(module, "stringArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_STRING), 0, false), true), stringArrayGetFunction,
                        new String[]{"red", "yellow", "green"}},
                {new VariableKey(module, "booleanArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true), booleanArrayGetFunction,
                        new boolean[]{true, false, false, true}},
                {new VariableKey(module, "decimalArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_DECIMAL), 0, false), true), decimalArrayGetFunction,
                        expectedDecimalArray}
        };
    }
}
