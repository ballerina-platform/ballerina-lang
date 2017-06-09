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
 * Has children of type {@link AnnotationEntry} only.
 * @extends ASTNode
 */
class Annotation extends ASTNode {
    /**
     * Creates new instance of an annotation.
     * @param {object} args The annotation creation args object.
     * @param {string} args.fullPackageName The full package name of the annotation. Example: ballerina.net.http.
     * @param {string} args.packageName The full package name of the annotation. Example: http.
     * @param {string} args.identifier The right hand side test of the annotation. Example: GET
     * @param {boolean} args.supported These annotation will be ignored when generating the source code.
     * @param {string} args.uniqueIdentifier A unique identifier for an annotation. Can be used for filtering later on.
     */
    constructor(args) {
        super('Annotation');

        /**
         * The full package name. Example: ballerina.net.http.
         * @type {string}
         */
        this._fullPackageName = _.get(args, 'fullPackageName');

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

        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '',
            3: ''
        };
    }

    setFullPackageName(fullPackageName, options) {
        this.setAttribute('_fullPackageName', fullPackageName, options);
    }

    getFullPackageName() {
        return this._fullPackageName;
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

    /**
     * The ballerina source code for the current annotation including its nested children.
     * @return {string}
     */
    toString() {
        let annotationString;
        if (_.isUndefined(this._packageName) || _.isEmpty(this._packageName)) {
            annotationString = '@' + this.getWSRegion(0) + this._identifier;
        } else {
            annotationString = '@' + this._packageName + ':' + this._identifier + this.getWSRegion(1);
        }

        let childrenAsString = [];
        _.forEach(this.getChildren(), function (child) {
            childrenAsString.push(child.toString());
        });

        annotationString = annotationString + '{' + _.join(childrenAsString, ',') + '}' + this.getWSRegion(3);
        return annotationString;
    }

    /**
     * Setting parameters from json
     * @param {object} jsonNode to initialize from
     * @param {string} jsonNode.annotation_package_path The full path of the annotation.
     * @param {string} jsonNode.annotation_package_name The package name.
     * @param {string} jsonNode.annotation_name The identifier of the annotation.
     */
    initFromJson(jsonNode) {
        this.setFullPackageName(jsonNode.annotation_package_path, {doSilently: true});
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
