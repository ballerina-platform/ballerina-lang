/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * @param {CompletionItem} completionItem completion item
 * @returns {any} resolved type from the completion item
 * @memberof LanguageServerUtils
 */
export function getResolvedTypeData(completionItem) {
    let typeName = completionItem.detail;
    let packageName;
    let constraint;
    if ((completionItem.data) && (completionItem.data.data.type)) {
        // there are more details on the type. e.g. : array dimensions, constraints
        const data = completionItem.data.data.type;
        if (data.isArrayType) {
            typeName = data.arrayType;
        }
        if (data.pkgName !== '.') {
            packageName = data.pkgName;
        }
        if (data.constraint) {
            let constraintPkgName = data.constraintPkgName;
            if (constraintPkgName === '.') {
                constraintPkgName = null;
            }
            constraint = { type: data.constraintName, packageName: constraintPkgName };
            typeName = 'json';
        }
    }
    return ({ typeName, packageName, constraint });
}
