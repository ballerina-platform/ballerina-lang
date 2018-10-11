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

export const MAC_OS = 'MacOS';

export const WINDOWS = 'Windows';

export const LINUX = 'Linux';

export const UNIX = 'UNIX';

export const UNKNOWN = 'Unknown OS';

/**
 * Gets Client OS
 *
 * @returns {String} Client OS
 */
export function getClientOS() {
    let operatingSystem = UNKNOWN;
    if (navigator.appVersion.indexOf('Win') !== -1) {
        operatingSystem = WINDOWS;
    } else if (navigator.appVersion.indexOf('Mac') !== -1) {
        operatingSystem = MAC_OS;
    } else if (navigator.appVersion.indexOf('X11') !== -1) {
        operatingSystem = UNIX;
    } else if (navigator.appVersion.indexOf('Linux') !== -1) {
        operatingSystem = LINUX;
    }
    return operatingSystem;
}

/**
 * Indicates whether the client is on Mac OS
 *
 * @returns {boolean} Indicates whether the client on Mac OS
 */
export function isClientOnMacOS() {
    return getClientOS() === MAC_OS;
}

/**
 * Indicates whether the client is on Windows
 *
 * @returns {boolean} Indicates whether the client is on windows
 */
export function isClientOnWindows() {
    return getClientOS() === WINDOWS;
}

/**
 * Indicates whether the composer is running inside electron
 */
export function isOnElectron() {
    const userAgent = navigator.userAgent.toLowerCase();
    return userAgent.indexOf(' electron/') > -1;
}
