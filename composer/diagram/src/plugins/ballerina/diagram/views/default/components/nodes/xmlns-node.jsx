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

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import StatementDecorator from '../decorators/statement-decorator';
import FragmentUtil from '../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';

/**
 * Class for XML namespace.
 * @extends React.Component
 * @class XMLNSNode
 * */
class XmlnsNode extends React.Component {
    /**
     * Constructor for XMLNSNode
     * @param {Object} props - properties passed in to this component.
     * */
    constructor(props) {
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'XML Namespace',
            model: this.props.model,
            setterMethod: this.handleSetter,
        };

        this.handleSetter = this.handleSetter.bind(this);
    }

    /**
     * Handle setter for XML namespace.
     * @param {string} value - edited value.
     * */
    handleSetter(value) {
        if (!_.isNil(value)) {
            const fragment = FragmentUtil.createStatementFragment(value);
            FragmentUtil.parseFragment(fragment)
                .then((parsedJson) => {
                    const newXmlNsNode = TreeBuilder.build(parsedJson, this.parent, this.parent.kind);
                    this.parent.replaceStatements(this, newXmlNsNode);
                });
        }
    }

    /**
     * Get the view to render if xmlns is not a global level variable.
     * @return {XML} if not a global return React component else return null.
     * */
    getRenderingView() {
        const model = this.props.model;
        const expression = model.viewState.expression;
        if (!model.global) {
            return (
                <StatementDecorator
                    model={model}
                    viewState={model.viewState}
                    expression={expression}
                    editorOptions={this.editorOptions}
                />
            );
        }
        return null;
    }

    /**
     * Render the XMLNS view.
     * @return {XML} react component.
     * */
    render() {
        return this.getRenderingView();
    }
}

XmlnsNode.PropTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.shape({
        viewState: PropTypes.shape({
            expression: PropTypes.shape({
                expression: PropTypes.string,
            }),
        }).isRequired,
    }).isRequired,
};

XmlnsNode.contextTypes = {
    designer: PropTypes.instanceOf(Object).isRequired,
};


export default XmlnsNode;
