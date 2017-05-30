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

class CompleterFactory{
    constructor() {
        this.variable_dec = /([a-z])+ .*/i;
        this.package_dec = /([a-z0-9])+:.*/i;
    }

    getCompleters(key , packageScope){
        switch (key) {
        case "VariableDefinition":
            return this.getVariableDefinitionCompleters();
        case "Function":
            return this.getFunctionCompleters(packageScope);
        default:
            return false;
        }
    }

    getVariableDefinitionCompleters(){
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                if(this.variable_dec.exec(editor.getSession().getValue())){
                    return [];
                }
                let types = BallerinaEnvironment.getTypes();
                var completions = types.map(function(item){
                    return { name:item, value:item + " ", meta: "type" };
                });
                callback(null, completions);
            }
        }];
    }

    getFunctionCompleters(packageScope){
        return [{
            getCompletions: (editor, session, pos, prefix, callback) => {
                let completions = [];
                if(!this.package_dec.exec(editor.getSession().getValue())){
                    let packages = BallerinaEnvironment.
                        searchPackage(editor.getSession().getValue().trim(),null);
                    completions = packages.map((item) => {
                        return {
                            name: item.getName(),
                            caption: item.getName(),
                            value: this.getPackagePrefix(item.getName()) + ":",
                            meta: "package",
                            score:1
                        };
                    });

                    let scopeFunctions = packageScope.getCurrentPackage()
                        .getFunctionDefinitions();
                    var functions = scopeFunctions.map((item) => {
                        return {
                            name:item.getName(),
                            caption: item.getName(),
                            value: item.getName() + "(",
                            meta: "local function",
                            score:100
                        };
                    });

                    callback(null, _.concat(completions, functions));
                }else{
                    let packages = BallerinaEnvironment.
                        searchPackage(editor.getSession().getValue().split(":")[0].trim());
                    if(_.isArray(packages) && packages.length > 0){
                        let packageItem = packages[0];
                        let functions = packageItem.getFunctionDefinitions();
                        completions = functions.map((item) => {
                            return {
                                name:item.getName(),
                                caption: item.getName(),
                                value: item.getName() + "(",
                                meta: "function"
                            };
                        });
                    }
                    callback(null, completions);
                }
            }
        }];
    }

    getPackagePrefix(name){
        let array = name.split(".");
        return array[array.length -1 ];
    }
}

export let completerFactory = new CompleterFactory();
