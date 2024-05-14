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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.semtype.SemType;

// All the logic for supporting various Type operations on SemTypes is defined here
final class BTypeAdapter implements Type {

    private final SemType semType;

    BTypeAdapter(SemType semType) {
        this.semType = semType;
    }

    @Override
    public <V> V getZeroValue() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public <V> V getEmptyValue() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public int getTag() {
        // TODO: cache this
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public boolean isNilable() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public String getName() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public String getQualifiedName() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public Module getPackage() {
        throw new IllegalStateException("semtype without identity");
    }

    @Override
    public boolean isPublic() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public boolean isNative() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public boolean isAnydata() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public boolean isPureType() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public boolean isReadOnly() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public long getFlags() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public Type getImmutableType() {
        throw new IllegalStateException("unimplemented");
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        throw new IllegalArgumentException("SemTypes are unmodifiable");
    }

    @Override
    public Module getPkg() {
        throw new IllegalStateException("semtype without identity");
    }
}
