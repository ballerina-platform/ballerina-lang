/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { getPathSeperator } from 'api-client/api-client';

const PKG_SEP = '.';

/**
 * Checks whether the package name matches file path
 * @param {string} pkgName Package name in package declaration
 * @param {string} filePath Folder path of the file
 *
 * @returns {boolean} true if file path matches pkg path
 */
export function isInCorrectPath(pkgName, filePath) {
    const pathSep = getPathSeperator();
    const pkgPartsRv = pkgName ? pkgName.split(PKG_SEP).reverse() : [];
    const filePathToSplit = filePath.endsWith(pathSep)
        ? filePath.substring(0, filePath.length - pathSep.length)
        : filePath;
    const pathPartsRv = filePathToSplit
            ? filePathToSplit.split(pathSep).reverse().slice(0, pkgPartsRv.length)
            : [];
    for (let i = 0; i < pkgPartsRv.length; i++) {
        if (pkgPartsRv[i] !== pathPartsRv[i]) {
            return false;
        }
    }
    return true;
}

/**
 * Gets correct path for the given package in given project directory
 * @param {string} programDir Path of the project directory
 * @param {string} pkgName Name of the package
 *
 * @returns {string} Correct folder path for given package
 */
export function getCorrectPathForPackage(programDir, pkgName) {
    if (!programDir || !pkgName) {
        return;
    }
    const pathSep = getPathSeperator();
    const pkgPart = pkgName.split(PKG_SEP).join(pathSep);
    return programDir.endsWith(pathSep)
        ? programDir + pkgPart + pathSep
        : programDir + pathSep + pkgPart + pathSep;
}

/**
 * Gets correct package name for the given file in given project directory
 *
 * @param {string} programDir Path of the project directory
 * @param {string} filePath Folder path of the file which need package name for
 *
 * @returns {string} correct folder path for given package
 */
export function getCorrectPackageForPath(programDir, filePath) {
    if (!programDir || !filePath) {
        return;
    }
    const pathSep = getPathSeperator();
    const pkgPart = filePath.substring(programDir.length);
    let pkgPartToSplit = pkgPart.startsWith(pathSep)
            ? pkgPart.substring(pathSep.length)
            : pkgPart;
    pkgPartToSplit = pkgPartToSplit.endsWith(pathSep)
            ? pkgPartToSplit.substring(0, pkgPartToSplit.length - pathSep.length)
            : pkgPartToSplit;
    return pkgPartToSplit.split(pathSep).join(PKG_SEP);
}

