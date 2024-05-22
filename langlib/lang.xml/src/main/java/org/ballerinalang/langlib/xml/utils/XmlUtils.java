/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.langlib.xml.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List util class for the langlib xml module.
 *
 * @since 2201.9.0
 */
public class XmlUtils {

    @SafeVarargs
    public static <E> List<E> getList(E... values) {
        List<E> list = new ArrayList<>();
        Collections.addAll(list, values);
        return list;
    }
}
