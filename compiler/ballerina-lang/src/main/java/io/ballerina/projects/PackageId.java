/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects;

import java.util.Objects;
import java.util.UUID;

/**
 * A unique identifier of a {@code Package} instance.
 *
 * @since 2.0.0
 */
public final class PackageId {
    private final UUID id;
    private final String packageName;

    private PackageId(UUID id, String packageName) {
        this.id = id;
        this.packageName = packageName;
    }

    public static PackageId create(String packageName) {
        return new PackageId(UUID.randomUUID(), packageName);
    }

    public UUID id() {
        return id;
    }

    @Override
    public String toString() {
        return "PackageId{" +
                "packageName='" + packageName + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageId packageId = (PackageId) o;
        return id.equals(packageId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
