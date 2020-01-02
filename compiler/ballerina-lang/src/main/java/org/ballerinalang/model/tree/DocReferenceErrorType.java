/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.tree;

/**
 * This class contains a list of diagnostic codes.
 *
 * @since 1.1.0
 */
public enum DocReferenceErrorType {
    NO_ERROR,
    BACKTICK_IDENTIFIER_ERROR, // Invalid use of `reference`. Eg : `9getName()`.
    IDENTIFIER_ERROR, // Invalid use of keyword `identifier`. Eg : function `9getName`.
    REFERENCE_ERROR, // Reference not found in the scope.
    PARAMETER_REFERENCE_ERROR // Incorrectly used parameter `reference` in a place other than a function.
}
