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
import ASTNode from '../node';
const supportedHttpMethodAnnotations = ['POST', 'GET', 'PUT', 'HEAD', 'DELETE', 'PATCH', 'OPTION'];

/**
 * Has children of type annotationEntry.
 */
class Annotation extends ASTNode {
    /**
    * Creates new instance of an annotation.
    * @param {string} identifier The text of the identifier. Example: http:GET
    */
    constructor(args) {
        super('Annotation');

        /**
         * The package of the annotation. Example: http.
         * @type {string}
         */
        this._packageName = _.get(args, 'packageName');

        /**
         * The identifier/annotation name for the annotation without @ sign. Example: GET.
         * @type {string}
         */
        this._identifier = _.get(args, 'identifier');

        /**
         * The annotation is not supported. Hence will be ignored on source-gen.
         * @type {boolean}
         */
        this._supported = _.get(args, 'supported', true);

        /**
         * A special string for identification.
         */
        this._uniqueIdentifier = _.get(args, 'uniqueIdentifier');
    }

    setPackageName(packageName, options) {
        this.setAttribute('_packageName', packageName, options);
    }

    getPackageName() {
        return this._packageName;
    }

    setIdentifier(identifier, options) {
        this.setAttribute('_identifier', identifier, options);
    }

    getIdentifier() {
        return this._identifier;
    }

    setSupported(supported, options) {
        this.setAttribute('_supported', supported, options);
    }

    isSupported() {
        return this._supported;
    }

    setUniqueIdentifier(uniqueIdentifier) {
        this._uniqueIdentifier = uniqueIdentifier;
    }

    getUniqueIdentifier() {
        return this._uniqueIdentifier;
    }

    toString() {
        let annotationString;
        if (_.isUndefined(this._packageName) || _.isEmpty(this._packageName)) {
            annotationString = '@' + this._identifier;
        } else {
            annotationString = '@' + this._packageName + ':' + this._identifier;
        }

        let childrenAsString = [];
        _.forEach(this.getChildren(), function (child) {
            childrenAsString.push(child.toString());
        });

        annotationString = annotationString + '{' + _.join(childrenAsString, ', ') + '}';

        return annotationString;
    }

    /**
     * setting parameters from json
     * @param {Object} jsonNode to initialize from
     */
    initFromJson(jsonNode) {
        this.setPackageName(jsonNode.annotation_package_name, {doSilently: true});
        this.setIdentifier(jsonNode.annotation_name, {doSilently: true});

        if (_.includes(
                _.map(supportedHttpMethodAnnotations, (e) => {
                    return e.toLowerCase();
                }), this.getIdentifier().toLowerCase())) {
            this.setUniqueIdentifier('httpMethod');
        }

        _.each(jsonNode.children, childNode => {
            let child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default Annotation;
