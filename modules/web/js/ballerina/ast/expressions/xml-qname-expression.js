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
import Expression from './expression';

/**
 * class for the XML QName expression.
 * @class XMLQNameExpression
 * */
class XMLQNameExpression extends Expression {
    /**
     * constructor for XMLQNameExpression.
     * @param {object} args - arguments to create XMLQNameExpression
     * */
    constructor(args) {
        super(args);
        this.type = 'XMLQNameExpression';
        this._isLHS = _.get(args, 'is_lhs_expression', false);
        this._isUsedInXML = _.get(args, 'is_used_in_xml', false);
        this._localName = _.get(args, 'local_name', '');
        this._namespaceURI = _.get(args, 'namespace_uri', '');
        this._prefix = _.get(args, 'prefix', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: '',
            2: '',
            3: '',
        };
    }

    /**
     * is left hand side expression.
     * @return {boolean} if yes true, else false.
     * */
    isLHSExpr() {
        return this._isLHS;
    }

    /**
     * is Used in xml
     * @return {boolean} if yes true, else false.
     * */
    isUsedInXML() {
        return this._isUsedInXML;
    }

    /**
     * get local name.
     * @return {string} local name.
     * */
    getLocalName() {
        return this._localName;
    }

    /**
     * get namespace URI.
     * @return {string} namespace URI.
     * */
    getNamespaceURI() {
        return this._namespaceURI;
    }

    /**
     * get the prefix.
     * @return {string} prefix.
     * */
    getPrefix() {
        return this._prefix;
    }

    /**
     * set the isLHSExpr value.
     * @param {boolean} isLHSExpr - is lhs expression
     * @param {object} options - options to passed in to setAttribute
     * */
    setIsLHSExpr(isLHSExpr, options) {
        this.setAttribute('_isLHS', isLHSExpr, options);
    }

    /**
     * set the isUsedInXML value.
     * @param {boolean} isUsedInXML - is Used In XML
     * @param {object} options - options to passed in to setAttribute
     * */
    setIsUsedInXML(isUsedInXML, options) {
        this.setAttribute('_isUsedInXML', isUsedInXML, options);
    }

    /**
     * set the local name value.
     * @param {string} localName - local name
     * @param {object} options - options to passed in to setAttribute
     * */
    setLocalName(localName, options) {
        this.setAttribute('_localName', localName, options);
    }

    /**
     * set the isLHSExpr value.
     * @param {string} namespaceURI - namespace URI
     * @param {object} options - options to passed in to setAttribute
     * */
    setNamespaceURI(namespaceURI, options) {
        this.setAttribute('_namespaceURI', namespaceURI, options);
    }

    /**
     * set the prefix value.
     * @param {string} prefix - prefix
     * @param {object} options - options to passed in to setAttribute
     * */
    setPrefix(prefix, options) {
        this.setAttribute('_prefix', prefix, options);
    }

    /**
     * get the expression as a string.
     * @return {string} expression string.
     * */
    getExpressionString() {
        return `${this.getPrefix()}:${this.getLocalName()}`;
    }

    /**
     * initialize the XMLQNameExpression from json.
     * @param {object} jsonNode - json node.
     * */
    initFromJson(jsonNode) {
        this.setIsLHSExpr(jsonNode.is_lhs_expression, { doSilently: true });
        this.setIsUsedInXML(jsonNode.is_used_in_xml, { doSilently: true });
        this.setLocalName(jsonNode.local_name, { doSilently: true });
        this.setNamespaceURI(jsonNode.namespace_uri, { doSilently: true });
        this.setPrefix(jsonNode.prefix, { doSilently: true });
    }
}

export default XMLQNameExpression;
