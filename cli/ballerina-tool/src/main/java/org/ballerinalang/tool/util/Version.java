/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.tool.util;

/**
 * Ballerina version tools.
 */
public class Version {

    private String versionString;
    private int major = 0;
    private int minor = 0;
    private int patch = 0;
    private int preRelease = 0;
    private int preReleaseCount = 0;
    private boolean isSnapshot = false;

    public Version(String version) {
        versionString = version;
        version = version.toLowerCase();
        //Checks for pre releases
        if (version.contains("-")) {
            //Checks for snapshots
            if (version.contains("snapshot")) {
                isSnapshot = true;
                version = version.replace("-snapshot", "");
                //alpha-snapshot
                if (version.contains("alpha")) {
                    preRelease = 1;
                    preReleaseCount = getPreReleaseCount(version, "alpha");
                    version = replacePreRelease(version, "alpha", preReleaseCount);
                } else if (version.contains("beta")) {
                    preRelease = 2;
                    preReleaseCount = getPreReleaseCount(version, "beta");
                    version = replacePreRelease(version, "beta", preReleaseCount);
                } else if (version.contains("rc")) {
                    preRelease = 3;
                    preReleaseCount = getPreReleaseCount(version, "rc");
                    version = replacePreRelease(version, "rc", preReleaseCount);
                }
            } else {
                String prePart = version.split("-")[1];
                if (prePart.contains("alpha")) {
                    preRelease = 1;
                    preReleaseCount = getPreReleaseCount(prePart, "alpha");
                    version = replacePreRelease(version, "alpha", preReleaseCount);
                } else if (prePart.contains("beta")) {
                    preRelease = 2;
                    preReleaseCount = getPreReleaseCount(prePart, "beta");
                    version = replacePreRelease(version, "beta", preReleaseCount);
                } else if (prePart.contains("rc")) {
                    preRelease = 3;
                    preReleaseCount = getPreReleaseCount(prePart, "rc");
                    version = replacePreRelease(version, "rc", preReleaseCount);
                }
            }
        }
        String[] versions = version.split("\\.");
        major = Integer.parseInt(versions[0]);
        minor = Integer.parseInt(versions[1]);
        patch = Integer.parseInt(versions[2]);
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }

    public int getPreRelease() {
        return preRelease;
    }

    public void setPreRelease(int preRelease) {
        this.preRelease = preRelease;
    }

    public int getPreReleaseCount() {
        return preReleaseCount;
    }

    public void setPreReleaseCount(int preReleaseCount) {
        this.preReleaseCount = preReleaseCount;
    }

    public boolean isSnapshot() {
        return isSnapshot;
    }

    public void setSnapshot(boolean snapshot) {
        isSnapshot = snapshot;
    }

    private String replacePreRelease(String version, String type, int preReleaseCount) {

        String newVersion;

        if (preReleaseCount == 0) {
            newVersion = version.replace("-" + type, "");
        } else {
            newVersion = version.replace("-" + type + preReleaseCount, "");
        }
        return newVersion;
    }

    private int getPreReleaseCount(String version, String preReleaseType) {
        version = version.replace("-", "").replace(".", "");
        String[] parts = version.split(preReleaseType);
        if (parts.length > 1) {
            return Integer.parseInt(parts[1]);
        } else {
            return 0;
        }
    }

    /**
     * Specifies compatible latest version from provided versions.
     * @param versions list versions need to be checked
     * @return provide compatible version
     */
    public String getLatest(String[] versions) {
        Version latestVersion = this;
        for (String versionString : versions) {
            Version currentVersion = new Version(versionString);
            if (isLater(latestVersion, currentVersion)) {
                latestVersion = currentVersion;
            }
        }
        return latestVersion.toString();
    }

    /**
     * Check two versions and specify whether second version is later.
     * @param version1 first version
     * @param version2 second version which should be checked newer than first
     * @return is second version is later
     */
    private boolean isLater(Version version1, Version version2) {
        boolean isLater = false;
        if (!version2.isSnapshot
                && version2.getMajor() == version1.getMajor()
                && version2.getMinor() >= version1.getMinor()
                && version2.getPatch() >= version1.getPatch()) {
            //When both versions are same pre releases we check latest
            if (version1.preRelease == version2.preRelease
                    && version2.preReleaseCount > version1.preReleaseCount) {
                isLater = true;
            } else if (version2.preRelease == 0) {
                isLater = true;
            } else if (version1.preRelease != 0 && version2.preRelease > version1.preRelease) {
                isLater = true;
            }
        }
        return isLater;
    }

    public String toString() {
        return versionString;
    }
}
