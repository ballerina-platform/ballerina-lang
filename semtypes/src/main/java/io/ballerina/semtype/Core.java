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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.semtype;

/**
 * Contains the core functions in semtypes.
 *
 * @since 2.0.0
 */
public class Core {

    public static boolean isEmpty(TypeCheckContext tc, SemType t) {
        throw new AssertionError();
    }

    public static boolean isSubtype(TypeCheckContext tc, SemType t1, SemType t2) {
        throw new AssertionError();
    }

    public static SemType union(SemType t1, SemType t2) {
        throw new AssertionError();
    }

    public static SemType diff(SemType t1, SemType t2) {
        throw new AssertionError();
    }

    public static SemType complement(SemType t) {
        return diff(PredefinedType.TOP, t);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        throw new AssertionError();
    }
}
