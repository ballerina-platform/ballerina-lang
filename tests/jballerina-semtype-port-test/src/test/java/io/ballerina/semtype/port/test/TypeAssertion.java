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

import java.nio.file.Paths;

/**
 * Subtype test.
 *
 * @param context  Type context under which {@code SemTypes} were defined.
 * @param fileName Name of the file in which types were defined in.
 * @param lhs      Resolved {@code SemType} for the Left-hand side of the subtype test.
 * @param rhs      Resolved {@code SemType} for the Right-hand side of the subtype test.
 * @param kind     Relationship between the two types.
 * @param text     Text that will be shown in case of assertion failure.
 * @since 3.0.0
 */
public record TypeAssertion<SemType>(TypeTestContext<SemType> context, String fileName, SemType lhs, SemType rhs,
                                     SemTypeAssertionTransformer.RelKind kind, String text) {

    public TypeAssertion {
        if (kind != null) {
            assert lhs != null;
            assert rhs != null;
        }
    }

    @Override
    public String toString() {
        return Paths.get(fileName).getFileName().toString() + ": " + text;
    }
}
