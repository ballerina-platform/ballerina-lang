/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.semtype.port.test;

public interface TypeTestAPI<Context, SemType> {

    boolean isSubtype(Context cx, SemType t1, SemType t2);

    // TODO: may be introduce is mapping and is list
    boolean isSubtypeSimple(SemType t1, SemType t2);

    boolean isListType(SemType t);

    boolean isMapType(SemType t);

    SemType intConst(long l);

    SemType mappingMemberTypeInnerVal(Context context, SemType type, SemType m);

    SemType listProj(Context context, SemType t, SemType key);

    SemType listMemberType(Context context, SemType t, SemType key);
}
