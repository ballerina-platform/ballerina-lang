/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import _ from 'lodash';
import $ from 'jquery';
import BallerinaEnvironment from './../ballerina/env/environment';

class CompleterFactory {
    constructor() {
        this.variable_dec = /([a-z])+ .*/i;
        this.package_dec = /([a-z0-9])+:.*/i;
        this.param_dec = /([a-z])+@{}: .*/i;
    }

    getCompleters(key, packageScope) {
        switch (key) {
            case 'VariableDefinition':
                return this.getVariableDefinitionCompleters();
            case 'Function':
                return this.getFunctionCompleters(packageScope);
            case 'ParameterDefinition':
                return this.getParameterCompleters();
            default:
                return false;
        }
    }

    getVariableDefinitionCompleters() {
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                if (this.variable_dec.exec(editor.getSession().getValue())) {
                    return [];
                }
                const types = BallerinaEnvironment.getTypes();
                const completions = types.map(item => ({name: item, value: `${item} `, meta: 'type'}));
                callback(null, completions);
            },
        }];
    }

    getFunctionCompleters(packageScope) {
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                let completions = [];
                if (!this.package_dec.exec(editor.getSession().getValue())) {
                    const packages = BallerinaEnvironment
                        .searchPackage(editor.getSession().getValue().trim(), null);
                    completions = packages.map(item => ({
                        name: item.getName(),
                        caption: item.getName(),
                        value: `${this.getPackagePrefix(item.getName())}:`,
                        meta: 'package',
                        score: 1,
                    }));

                    const scopeFunctions = packageScope.getCurrentPackage()
                        .getFunctionDefinitions();
                    const functions = scopeFunctions.map(item => ({
                        name: item.getName(),
                        caption: item.getName(),
                        value: `${item.getName()}(`,
                        meta: 'local function',
                        score: 100,
                    }));

                    callback(null, _.concat(completions, functions));
                } else {
                    const packages = BallerinaEnvironment
                        .searchPackage(editor.getSession().getValue().split(':')[0].trim());
                    if (_.isArray(packages) && packages.length > 0) {
                        const packageItem = packages[0];
                        const functions = packageItem.getFunctionDefinitions();
                        completions = functions.map(item => ({
                            name: item.getName(),
                            caption: item.getName(),
                            value: `${item.getName()}(`,
                            meta: 'function',
                        }));
                    }
                    callback(null, completions);
                }
            },
        }];
    }

    getParameterCompleters() {
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                if (this.param_dec.exec(editor.getSession().getValue())) {
                    return [];
                }
                //TODO: Remove these conditions as the language server support scoped filtering.
                let isPackageSearch = editor.getSession().getValue().trim().includes('@')
                    && !editor.getSession().getValue().trim().includes(':')
                    && !editor.getSession().getValue().trim().includes('{')
                    && !editor.getSession().getValue().trim().includes('}')
                    && !editor.getSession().getValue().trim().includes('"');
                let isAnnotationSupport = editor.getSession().getValue().trim().includes('@')
                    && editor.getSession().getValue().trim().includes(':')
                    && !editor.getSession().getValue().trim().includes('{')
                    && !editor.getSession().getValue().trim().includes('}')
                    && !editor.getSession().getValue().trim().includes('"');
                let isAnnotationDefinitionSupport = editor.getSession().getValue().trim().includes('@')
                    && editor.getSession().getValue().trim().includes(':')
                    && editor.getSession().getValue().trim().includes('{')
                    && !editor.getSession().getValue().trim().includes('}')
                    && !editor.getSession().getValue().trim().includes('"');
                let isVariableSupport = (editor.getSession().getValue().trim().includes('@')
                    && editor.getSession().getValue().trim().includes('}')
                    && editor.getSession().getValue().trim().includes('}')
                    && editor.getSession().getValue().trim().includes('"'))
                    || !editor.getSession().getValue().trim().includes('@');

                let completions = [];

                if (isAnnotationSupport) {
                    let atSignIndex = editor.getSession().getValue().trim().indexOf('@');
                    let colonSignIndex = editor.getSession().getValue().trim().indexOf(':');
                    let packageName = editor.getSession().getValue().trim().slice(atSignIndex + 1, colonSignIndex);
                    const packages = BallerinaEnvironment
                        .searchPackage(packageName, null);
                    const filteredPackages = this.filterPackageByAnnotationAttachmentPoint(packages, "parameter");
                    const annotations = this.filterAnnotationsByAttachementPoint(filteredPackages, "parameter");

                    for (let i = 0; i < annotations.length; i++) {
                        let annotation = annotations[i];
                        completions.push({name: annotation._name, value: `${annotation._name}{`, meta: 'annotation'});
                    }
                } else if (isPackageSearch) {
                    let packageName = editor.getSession().getValue().trim().split('@')[1];
                    const packages = BallerinaEnvironment
                        .searchPackage(packageName, null);
                    const filteredPackages = this.filterPackageByAnnotationAttachmentPoint(packages, "parameter");
                    completions = filteredPackages.map(item => ({
                        name: this.getShortNameForPackage(item._name),
                        value: `${this.getShortNameForPackage(item._name)}:`,
                        meta: 'package'
                    }));
                } else if (isAnnotationDefinitionSupport) {
                    let atSignIndex = editor.getSession().getValue().trim().indexOf('@');
                    let openBracketSignIndex = editor.getSession().getValue().trim().indexOf('{');
                    let colonSignIndex = editor.getSession().getValue().trim().indexOf(':');
                    let packageName = editor.getSession().getValue().trim().slice(atSignIndex + 1, colonSignIndex);
                    let annotationName = editor.getSession().getValue().trim().slice(colonSignIndex + 1, openBracketSignIndex);
                    const packages = BallerinaEnvironment
                        .searchPackage(packageName, null);
                    const filteredPackages = this.filterPackageByAnnotationAttachmentPoint(packages, "parameter");
                    const annotations = this.filterAnnotationsByAttachementPoint(filteredPackages, "parameter");
                    const annotation = this.getAnnotationByName(annotations, annotationName);
                    if(annotation.length > -1) {
                        const definitions = this.getAnnotationDefinitions(annotation[0]);
                        completions = definitions.map(item => ({name: item, value: `${item}:""`, meta: 'attributes'}));
                    }
                } else if (isVariableSupport) {
                    const types = BallerinaEnvironment.getTypes();
                    completions = types.map(item => ({name: item, value: `${item} `, meta: 'type'}));
                }

                callback(null, completions);
            },
        }];
    }

    getPackagePrefix(name) {
        const array = name.split('.');
        return array[array.length - 1];
    }

    filterPackageByAnnotationAttachmentPoint(packages, attachmentPoint) {
        let filteredPackages = [];
        for (let i = 0; i < packages.length; i++) {
            let isAttachmentPointAvailable = false;
            for (let j = 0; j < packages[i]._annotationDefinitions.length; j++) {
                let annotation = packages[i]._annotationDefinitions[j];
                if (annotation._attachmentPoints.indexOf(attachmentPoint) > -1) {
                    isAttachmentPointAvailable = true;
                    break;
                }
            }
            if (isAttachmentPointAvailable) {
                filteredPackages.push(packages[i]);
            }
        }
        return filteredPackages;
    }

    filterAnnotationsByAttachementPoint(packages, attachmentPoint) {
        let annotations = [];
        for (let i = 0; i < packages.length; i++) {
            for (let j = 0; j < packages[i]._annotationDefinitions.length; j++) {
                let annotation = packages[i]._annotationDefinitions[j];
                if (annotation._attachmentPoints.indexOf(attachmentPoint) > -1) {
                    annotations.push(annotation);
                }
            }
        }
        return annotations;
    }

    getAnnotationDefinitions(annotation) {
        let definitions = []
        for (let i = 0; i < annotation._annotationAttributeDefinitions.length; i++) {
            definitions.push(annotation._annotationAttributeDefinitions[i]._identifier);
        }
        return definitions;
    }

    getAnnotationByName(annotations, annotationName) {
        return annotations.filter((annotation) => {
            return annotation._name === annotationName;
        })
    }

    getShortNameForPackage(packageName) {
        let tokens = packageName.split('.');
        return tokens[tokens.length - 1];
    }
}

export const completerFactory = new CompleterFactory();
