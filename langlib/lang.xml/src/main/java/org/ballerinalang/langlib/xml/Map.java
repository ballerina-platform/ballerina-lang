/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.xml;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BXml;

import java.util.ArrayList;
import java.util.List;

/**
 * Native implementation of lang.xml:map(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class Map {

    public static BXml map(Environment env, BXml x, BFunctionPointer func) {
        if (x.isSingleton()) {
            return (BXml) func.call(env.getRuntime(), x);
        }
        List<BXml> elements = new ArrayList<>();
        int size = x.size();
        for (int i = 0; i < size; i++) {
            elements.add((BXml) func.call(env.getRuntime(), x.getItem(i)));
        }
        return ValueCreator.createXmlSequence(elements);
    }
}
