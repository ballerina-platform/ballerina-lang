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

package io.ballerina.projects.internal.model;

import com.google.gson.annotations.SerializedName;
import io.ballerina.projects.BuildOptions;

import java.util.List;
import java.util.Map;

/**
 * {@code PackageJson} Model for build file.
 *
 * @since 2.0.0
 */
public class BuildJson {

    public static final String SERIALIZED_NAME_LAST_BUILD_TIME = "last_build_time";
    @SerializedName(SERIALIZED_NAME_LAST_BUILD_TIME)
    private long lastBuildTime;

    public static final String SERIALIZED_NAME_LAST_UPDATE_TIME = "last_update_time";
    @SerializedName(SERIALIZED_NAME_LAST_UPDATE_TIME)
    private long lastUpdateTime;

    public static final String SERIALIZED_NAME_LAST_BAL_TOML_UPDATE_TIME = "last_bal_toml_update_time";
    @SerializedName(SERIALIZED_NAME_LAST_BAL_TOML_UPDATE_TIME)
    private long lastBalTomlUpdateTime;

    public static final String SERIALIZED_NAME_DISTRIBUTION_VERSION = "distribution_version";
    @SerializedName(SERIALIZED_NAME_DISTRIBUTION_VERSION)
    private String distributionVersion;

    public static final String SERIALIZED_NAME_LAST_MODIFIED_TIME = "last_modified_time";
    @SerializedName(SERIALIZED_NAME_LAST_MODIFIED_TIME)
    private Map<String, Long> lastModifiedTime;

    public static final String LAST_BUILD_OPTIONS = "last_build_options";
    @SerializedName(LAST_BUILD_OPTIONS)
    private BuildOptions buildOptions;

    public static final String SRC_META_INFO = "src_meta_info";
    @SerializedName(SRC_META_INFO)
    private FileMetaInfo[] srcMetaInfo;

    public static final String TEST_SRC_META_INFO = "test_src_meta_info";
    @SerializedName(TEST_SRC_META_INFO)
    private FileMetaInfo[] testSrcMetaInfo;

    public static final String TARGET_EXEC_META_INFO = "target_exec_meta_info";
    @SerializedName(TARGET_EXEC_META_INFO)
    private FileMetaInfo targetExecMetaInfo;

    public static final String SETTINGS_META_INFO = "settings_meta_info";
    @SerializedName(SETTINGS_META_INFO)
    private FileMetaInfo settingsMetaInfo;

    public static final String BALLERINA_TOML_META_INFO = "ballerina_toml_meta_info";
    @SerializedName(BALLERINA_TOML_META_INFO)
    private FileMetaInfo ballerinaTomlMetaInfo;

    public static final String TEST_ARTIFACT_META_INFO = "test_artifact_meta_info";
    @SerializedName(TEST_ARTIFACT_META_INFO)
    private FileMetaInfo[] testArtifactMetaInfo;

    public static final String TEST_CLASS_PATH = "test_class_path";
    @SerializedName(TEST_CLASS_PATH)
    private String testClassPath;

    public static final String SERIALIZED_NAME_IMPORTS = "imports";
    @SerializedName(SERIALIZED_NAME_IMPORTS)
    private List<String> imports;

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    public BuildJson(long lastBuildTime,
                     long lastUpdateTime,
                     String distributionVersion,
                     Map<String, Long> lastModifiedTime,
                     List<String> imports,
                     long lastBalTomlUpdateTime) {
        this.lastBuildTime = lastBuildTime;
        this.lastUpdateTime = lastUpdateTime;
        this.distributionVersion = distributionVersion;
        this.lastModifiedTime = lastModifiedTime;
        this.imports = imports;
        this.lastBalTomlUpdateTime = lastBalTomlUpdateTime;
    }

    public BuildOptions getBuildOptions() {
        return buildOptions;
    }

    public void setBuildOptions(BuildOptions buildOptions) {
        this.buildOptions = buildOptions;
    }

    public long lastBuildTime() {
        return lastBuildTime;
    }

    public void setLastBuildTime(long lastBuildTime) {
        this.lastBuildTime = lastBuildTime;
    }

    public long lastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastBalTomlUpdateTime(long lastBalTomlUpdateTime) {
        this.lastBalTomlUpdateTime = lastBalTomlUpdateTime;
    }

    public long lastBalTomlUpdateTime() {
        return lastBalTomlUpdateTime;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }
    public List<String> imports() {
        return imports;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String distributionVersion() {
        return distributionVersion;
    }

    public void setDistributionVersion(String distributionVersion) {
        this.distributionVersion = distributionVersion;
    }

    public Map<String, Long> getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Map<String, Long> lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public boolean isExpiredLastUpdateTime() {
        long oneDayAgo = System.currentTimeMillis() - ONE_DAY;
        return lastUpdateTime() < oneDayAgo;
    }

    public FileMetaInfo[] getSrcMetaInfo() {
        return srcMetaInfo;
    }

    public void setSrcMetaInfo(FileMetaInfo[] srcMetaInfo) {
        this.srcMetaInfo = srcMetaInfo;
    }

    public FileMetaInfo getTargetExecMetaInfo() {
        return targetExecMetaInfo;
    }

    public void setTargetExecMetaInfo(FileMetaInfo targetExecMetaInfo) {
        this.targetExecMetaInfo = targetExecMetaInfo;
    }

    public FileMetaInfo getSettingsMetaInfo() {
        return settingsMetaInfo;
    }

    public void setSettingsMetaInfo(FileMetaInfo settingsMetaInfo) {
        this.settingsMetaInfo = settingsMetaInfo;
    }

    public FileMetaInfo[] getTestArtifactMetaInfo() {
        return testArtifactMetaInfo;
    }

    public void setTestArtifactMetaInfo(FileMetaInfo[] testArtifactMetaInfo) {
        this.testArtifactMetaInfo = testArtifactMetaInfo;
    }

    public FileMetaInfo[] getTestSrcMetaInfo() {
        return testSrcMetaInfo;
    }

    public void setTestSrcMetaInfo(FileMetaInfo[] testSrcMetaInfo) {
        this.testSrcMetaInfo = testSrcMetaInfo;
    }

    public FileMetaInfo getBallerinaTomlMetaInfo() {
        return ballerinaTomlMetaInfo;
    }

    public void setBallerinaTomlMetaInfo(FileMetaInfo ballerinaTomlMetaInfo) {
        this.ballerinaTomlMetaInfo = ballerinaTomlMetaInfo;
    }

    public String getTestClassPath() {
        return testClassPath;
    }

    public void setTestClassPath(String testClassPath) {
        this.testClassPath = testClassPath;
    }

    public static class FileMetaInfo {
        private String file;
        private String hash;
        private long size;
        private long lastModifiedTime;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getLastModifiedTime() {
            return lastModifiedTime;
        }

        public void setLastModifiedTime(long lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
        }
    }
}
