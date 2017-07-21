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
import _ from 'lodash';
import log from 'log';
import Alerts from 'alerts';
import VariableDefinition from './variable-definition';
import FragmentUtils from './../utils/fragment-utils';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';
import ArgumentParameterDefinitionHolder from './argument-parameter-definition-holder';

class ParameterDefinition extends VariableDefinition {
    /**
     * Constructor for ParameterDefinition
     * @param {Object} [args] - The arguments to create the ParameterDefinition.
     * @param {string} [args.type=undefined] - Type of the parameter definition.
     * @param {string} [args.name=undefined] - Identifier of the parameter definition.
     * contains {Annotation} as children
     * @class ParameterDefinition
     * @constructor
     * @extends VariableDefinition
     */
    constructor(args) {
        super(args);
        // since there are ParameterDefinitions without names (return types) we set this to undefined
        this._name = _.get(args, 'name', undefined);
        this.type = 'ParameterDefinition';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
        };
    }

    /**
     * set the name of the parameter definition
     * @param name
     * @param options
     */
    setName(name, options) {
        // name can be null for parameter definitions (e.g. : named and unnamed return types)
        this.setAttribute('_name', name, options);
    }

    getParameterDefinitionAsString() {
        let argAsString = '';
        // add annotations
        this.getChildrenOfType(this.getFactory().isAnnotationAttachment).forEach(
            (annotationAttachment) => {
                // TODO: This func should move into the source-gen visitor. Unable to use annotation source gen visitor
                // due to cyclic imports.
                argAsString += '@';
                if (annotationAttachment.getPackageName() !== undefined) {
                    argAsString += annotationAttachment.getPackageName() + ':';
                }
                argAsString += annotationAttachment.getName() + '{';

                if (annotationAttachment.getChildren().length === 1) {
                    const annotationAttribute = annotationAttachment.getChildren()[0];
                    argAsString += annotationAttribute.getKey() + ':';

                    if (annotationAttribute.getValue().isBValue()) {
                        const bValue = annotationAttribute.getValue().getChildren()[0];
                        argAsString += `"${bValue.getStringValue()}"`;
                    }
                }

                argAsString += '} ';
            });

        argAsString += (this.getPkgName()) ? this.getPkgName() + ':' : '';
        argAsString += this.getTypeName();
        if (this.getTypeConstraint()) {
            const constraint = this.getTypeConstraint();
            const constraintStr = ('<' + ((constraint.pkgName) ? constraint.pkgName + ':' : '')
                                  + constraint.type + '>');
            argAsString += constraintStr;
        }
        argAsString += !_.isNil(this.getName()) ? /* FIXME*/ (this.getWSRegion(1) || ' ') + this.getName() : '';
        argAsString += this.getWSRegion(2);
        return argAsString;
    }

    addAnnotation(annotation) {
        this.addChild(annotation);
    }

    setParameterFromString(stmtString, callback) {
        let fragment = "";
        if (this.getParent() instanceof ArgumentParameterDefinitionHolder) {
            fragment = FragmentUtils.createArgumentParameterFragment(stmtString);
        } else {
            fragment = FragmentUtils.createReturnParameterFragment(stmtString);
        }
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            let nodeToFireEvent = this;
            if (_.isEqual(parsedJson.type, 'parameter_definition')) {
                const newNode = this.getFactory().createFromJson(parsedJson);
                const parent = this.getParent();
                const index = parent.getIndexOfChild(this);
                if (!this.checkWhetherIdentifierAlreadyExist(parsedJson.parameter_name)) {
                    parent.removeChild(this, true);
                    newNode.initFromJson(parsedJson);
                    parent.addChild(newNode, index, true, true);
                    nodeToFireEvent = newNode;
                } else {
                    const errorString = `Variable Already exists: ${parsedJson.parameter_name}`;
                    Alerts.error(errorString);
                    return false;
                }
            } else {
                log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
            }

            if (_.isFunction(callback)) {
                callback({isValid: true});
            }
            nodeToFireEvent.accept(new EnableDefaultWSVisitor());
            // Manually firing the tree-modified event here.
            // TODO: need a proper fix to avoid breaking the undo-redo
            nodeToFireEvent.trigger('tree-modified', {
                origin: nodeToFireEvent,
                type: 'custom',
                title: 'Modify Parameter Definition',
                context: nodeToFireEvent,
            });
        } else {
            log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
            if (_.isFunction(callback)) {
                callback({isValid: false, response: parsedJson});
            }
        }
    }

    initFromJson(jsonNode) {
        this.setTypeName(jsonNode.parameter_type, { doSilently: true });
        this.setName(jsonNode.parameter_name, { doSilently: true });
        this.setPkgName(jsonNode.package_name, { doSilently: true });

        if (jsonNode.type_constraint) {
            const typeConstraint = {};
            typeConstraint.pkgName = jsonNode.type_constraint.package_name;
            typeConstraint.type = jsonNode.type_constraint.type_constraint;
            this.setTypeConstraint(typeConstraint, { doSilently: true });
        }

        // As of now we only support one annotation.
        if (_.isEqual(_.size(jsonNode.children), 1) && _.isEqual(jsonNode.children[0].type, 'annotation_attachment')) {
            const annotationJson = jsonNode.children[0];
            const child = this.getFactory().createFromJson(annotationJson);
            this.addChild(child);
            child.initFromJson(annotationJson);
        }
    }

    checkWhetherIdentifierAlreadyExist(identifier) {
        let isExist = false;
        if (this.getParent().getChildren().length > 0) {
            for (let i = 0; i < this.getParent().getChildren().length; i++) {
                if (this.getParent().getChildren()[i].getName() === identifier
                    && !_.isMatch(this.getParent().getChildren()[i], this)) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }
}

export default ParameterDefinition;
