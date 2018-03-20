/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.protobuf.cmd;

import java.util.Locale;

import static org.ballerinalang.protobuf.BalGenerationConstants.OS_ARCH_SYSTEM_PROPERTY;
import static org.ballerinalang.protobuf.BalGenerationConstants.OS_NAME_SYSTEM_PROPERTY;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.AIX;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.FREEBSD;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.HPUX;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.LINUX;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.MACOSX;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.NETBSD;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.OPENBSD;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.OS400;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.OSX;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.SOLARIS;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.SUNOS;
import static org.ballerinalang.protobuf.cmd.SupportOSTypes.WINDOWS;

/**
 * Class for detecting the system operating system version and type.
 * Ref : https://github.com/trustin/os-maven-plugin/blob/master/src/main/java/kr/motd/maven/os/Detector.java
 */
public abstract class OSDetector {
    
    private static final String UNKNOWN = "unknown";
    
    public static String getDetectedClassifier() {
        
        final String osName = System.getProperty(OS_NAME_SYSTEM_PROPERTY);
        final String osArch = System.getProperty(OS_ARCH_SYSTEM_PROPERTY);
        
        final String detectedName = normalizeOs(osName);
        final String detectedArch = normalizeArch(osArch);
        
        final String failOnUnknownOS = System.getProperty("failOnUnknownOS");
        if (!"false".equalsIgnoreCase(failOnUnknownOS)) {
            if (UNKNOWN.equals(detectedName)) {
                throw new RuntimeException("unknown os.name: " + osName);
            }
            if (UNKNOWN.equals(detectedArch)) {
                throw new RuntimeException("unknown os.arch: " + osArch);
            }
        }
        // Assume the default classifier, without any os "like" extension.
        return detectedName + '-' + detectedArch;
    }
    
    private static String normalizeOs(String value) {
        
        value = normalize(value);
        if (value.startsWith(AIX)) {
            return AIX;
        }
        if (value.startsWith(HPUX)) {
            return HPUX;
        }
        if (value.startsWith(OS400)) {
            // Avoid the names such as os4000
            if (value.length() <= 5 || !Character.isDigit(value.charAt(5))) {
                return OS400;
            }
        }
        if (value.startsWith(LINUX)) {
            return LINUX;
        }
        if (value.startsWith(MACOSX) || value.startsWith(OSX)) {
            return "osx";
        }
        if (value.startsWith(FREEBSD)) {
            return FREEBSD;
        }
        if (value.startsWith(OPENBSD)) {
            return OPENBSD;
        }
        if (value.startsWith(NETBSD)) {
            return NETBSD;
        }
        if (value.startsWith(SOLARIS) || value.startsWith(SUNOS)) {
            return SUNOS;
        }
        if (value.startsWith(WINDOWS)) {
            return WINDOWS;
        }
        return UNKNOWN;
    }
    
    private static String normalizeArch(String value) {
        
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64|itanium64)$")) {
            return "itanium_64";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }
        return UNKNOWN;
    }
    
    private static String normalize(String value) {
        
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }
}
