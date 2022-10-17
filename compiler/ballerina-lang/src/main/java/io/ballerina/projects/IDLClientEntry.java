/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * IDL client entry loaded from the file system cache.
 *
 * @since 2201.3.0
 */
class IDLClientEntry {
    private final String url;
    private final List<String> annotations;
    private final String generatedModuleName;
    private final String filePath;
    private final long lastModifiedTime;

    public IDLClientEntry(String url, Path filePath, List<String> annotations, String generatedModuleName,
                          long lastModifiedTime) {
        this.url = url;
        this.annotations = annotations;
        this.generatedModuleName = generatedModuleName;
        this.filePath = filePath.toString();
        this.lastModifiedTime = lastModifiedTime;
    }

    public String url() {
        return url;
    }

    public List<String> annotations() {
        return annotations;
    }

    public String filePath() {
        return filePath;
    }

    public String generatedModuleName() {
        return generatedModuleName;
    }

    public long lastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IDLClientEntry that = (IDLClientEntry) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(annotations, that.annotations) &&
                Objects.equals(generatedModuleName, that.generatedModuleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, annotations, generatedModuleName);
    }
}
