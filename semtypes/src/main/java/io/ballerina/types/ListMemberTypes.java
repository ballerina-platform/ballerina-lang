/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import io.ballerina.types.subtypedata.Range;

import java.util.Collections;
import java.util.List;

/**
 * Holds a pair of List< SemType> and List< Range>.
 * <i>Note: Member types at the indices that are not contained in `Range` array represent `never.
 * The SemTypes in this list are not `never`.</i>
 *
 * @param ranges   Range array
 * @param semTypes SemType array
 * @since 2201.11.0
 */
public record ListMemberTypes(List<Range> ranges, List<SemType> semTypes) {

    public ListMemberTypes {
        ranges = Collections.unmodifiableList(ranges);
        semTypes = Collections.unmodifiableList(semTypes);
    }

    @Override
    public List<Range> ranges() {
        return Collections.unmodifiableList(ranges);
    }

    @Override
    public List<SemType> semTypes() {
        return Collections.unmodifiableList(semTypes);
    }

    public static ListMemberTypes from(List<Range> ranges, List<SemType> semTypes) {
        assert ranges != null && semTypes != null;
        return new ListMemberTypes(ranges, semTypes);
    }
}
