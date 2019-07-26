/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.value;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeConstants;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

/**
 * Merge two JSON values.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.value",
        functionName = "mergeJson",
        args = {@Argument(name = "j1", type = TypeKind.JSON), @Argument(name = "j2", type = TypeKind.JSON)},
        returnType = {@ReturnType(type = TypeKind.JSON), @ReturnType(type = TypeKind.ERROR)},
        isPublic = true
)
public class MergeJson {

    public static Object mergeJson(Strand strand, Object j1, Object j2) {

        if (j1 == null) {
            return j2;
        }

        if (j2 == null) {
            return j1;
        }

        BType j1Type = TypeChecker.getType(j1);
        BType j2Type = TypeChecker.getType(j2);

        if (j1Type.getTag() != TypeTags.MAP_TAG || j2Type.getTag() != TypeTags.MAP_TAG) {
            return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR,
                                               "Cannot merge JSON values of types '" + j1Type + "' and '" +
                                                       j2Type + "'");
        }

        MapValue<String, Object> m1 = (MapValue<String, Object>) j1;
        MapValue<String, Object> m2 = (MapValue<String, Object>) j2;

        for (Map.Entry<String, Object> entry : m2.entrySet()) {
            String key = entry.getKey();

            if (!m1.containsKey(key)) {
                m1.put(key, entry.getValue());
                continue;
            }

            Object elementMergeResult = mergeJson(strand, m1.get(key), entry.getValue());
            BType elementMergeResultType = TypeChecker.getType(elementMergeResult);

            if (elementMergeResultType.getTag() == TypeTags.ERROR_TAG) {
                MapValueImpl<String, Object> detailMap = new MapValueImpl<>(BTypes.typeErrorDetail);
                detailMap.put(TypeConstants.DETAIL_MESSAGE, "JSON Merge failed for key '" + key + "'");
                detailMap.put(TypeConstants.DETAIL_CAUSE, elementMergeResult);
                return BallerinaErrors.createError(BallerinaErrorReasons.MERGE_JSON_ERROR, detailMap);
            }

            m1.put(key, elementMergeResult);
        }
        return m1;
    }

}
