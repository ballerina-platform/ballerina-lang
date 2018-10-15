/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import java.util.List;

/**
 * Interface to implement by all type creaters.
 * 
 * @param <T> Type of the types created by the type creater.
 * 
 * @since 0.975.0
 */
public interface TypeCreater<T> {

    T getBasicType(char typeChar);

    T getBuiltinRefType(String typeName);

    T getRefType(char typeChar, String pkgPath, String typeName);

    T getConstrainedType(char typeChar, T constraint);

    T getArrayType(T elemType, int size);

    T getCollectionType(char typeChar, List<T> memberTypes);

    T getFunctionType(List<T> funcParamsStack, T retType);

    T getErrorType(T reasonType, T detailsType);
}
