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
 *
 */

/**
 * Sets the active stylesheet.
 *
 *  @param {string} title The stylesheet title. Can be 'default', 'light' or 'dark'.
 *  @return {void}
 */
function setActiveStyleSheet(title) {
    let i;
    for (i = 0; document.getElementsByTagName('link')[i] !== undefined; i += 1) {
        const a = document.getElementsByTagName('link')[i];
        if (a.getAttribute('rel').indexOf('style') !== -1 && a.getAttribute('title')) {
            a.disabled = true;
            if (a.getAttribute('title') === title) {
                a.disabled = false;
            }
        }
    }
}

/**
 * Gets the active stylesheet. Can be 'default', 'light' or 'dark'.
 *
 * @returns {string} stylesheet title
 */
function getActiveStyleSheet() {
    let i;
    for (i = 0; document.getElementsByTagName('link')[i] !== undefined; i += 1) {
        const a = document.getElementsByTagName('link')[i];
        if (a.getAttribute('rel').indexOf('style') !== -1 && a.getAttribute('title') && !a.disabled) {
            return a.getAttribute('title');
        }
    }
    return null;
}

/**
 * Gets the preffered stylesheet.
 *
 * @returns {string} The title of the preferred stylesheet.
 */
function getPreferredStyleSheet() {
    let i;
    for (i = 0; document.getElementsByTagName('link')[i] !== undefined; i += 1) {
        const a = document.getElementsByTagName('link')[i];
        if (a.getAttribute('rel').indexOf('style') !== -1 && a.getAttribute('rel').indexOf('alt') === -1
                && a.getAttribute('title')
       ) return a.getAttribute('title');
    }
    return null;
}

/**
 * Creates a cookie.
 *
 * @param {string} name The name of the cookie.
 * @param {string} value The value of the cookie.
 * @param {number|undefined} days The number of days for cookie to expire.
 * @returns {void}
 */
function createCookie(name, value, days) {
    let expires;
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = `;expires=${date.toGMTString()}`;
    } else {
        expires = '';
    }
    document.cookie = `${name}=${value}${expires}; path=/`;
}

/**
 * Reads the value of a cookie.
 *
 * @param {string} name The name of the cookie.
 * @returns {string|undefined} The value of the cookie.
 */
function readCookie(name) {
    const nameEQ = `${name}=`;
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i += 1) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return undefined;
}

window.onload = () => {
    const cookie = readCookie('style');
    const title = cookie || getPreferredStyleSheet();
    setActiveStyleSheet(title);
};
window.onunload = () => {
    const title = getActiveStyleSheet();
    createCookie('style', title, 365);
};
const cookie = readCookie('style');
const title = cookie || getPreferredStyleSheet();
setActiveStyleSheet(title);
