/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import 'font-ballerina/css/font-ballerina.css';
import { renderToStaticMarkup } from 'react-dom/server';
import React, { createElement } from 'react';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from 'prop-types';

import Diagram from 'plugins/ballerina/diagram/diagram.jsx';
import DesignView from 'plugins/ballerina/views/design-view.jsx';
import TreeBuilder from 'plugins/ballerina/model/tree-builder.js';
import '../src/ballerina-theme/semantic.less';

const BalDiagram = DragDropContext(HTML5Backend)(Diagram);
const BallerinaDesignView = DragDropContext(HTML5Backend)(DesignView);

const TREE_MODIFIED = 'tree-modified';

function renderStaticDiagram(target, modelJson, props = {}) {
    const defaultProps = {
        model: TreeBuilder.build(modelJson),
        mode: 'action',
        editMode: true,
        height: 300,
        width: 300,
    };
    Object.assign(defaultProps, props);
    const el = createElement(BalDiagram, defaultProps);
    target.innerHTML = renderToStaticMarkup(el);
}
class BallerinaDiagram extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            content: this.props.content,
            currentAST: undefined,
            editMode: true,
            diagramMode: 'action',
        };
    }

    componentDidMount() {
        this.updateDiagram(this.state.content);
    }

    componentWillReceiveProps(nextProps) {
        this.updateDiagram(nextProps.content);
    }

    onModelUpdate(evt) {
        const { currentAST } = this.state;
        if (currentAST) {
            const newContent = currentAST.getSource();
            this.setState({
                content: newContent,
            });
            this.props.onChange({ newContent });
        }
    }

    updateDiagram(content) {
        this.props.parseContent(content)
                .then((parserReply) => {
                    const { currentAST } = this.state;
                    if (parserReply.model) {
                        if (currentAST) {
                            currentAST.off(TREE_MODIFIED, this.onModelUpdate);
                        }
                        const newAST = TreeBuilder.build(parserReply.model);
                        if (newAST) {
                            newAST.on(TREE_MODIFIED, this.onModelUpdate.bind(this));
                        }
                        this.setState({
                            currentAST: newAST,
                            content,
                        });
                    }
                });
        this.forceUpdate();
    }

    render() {
        const { currentAST, diagramMode, editMode } = this.state;
        const { width, height } = this.props;
        if (!currentAST) {
            return (
                <div className='spinnerContainer'>
                    <div className='fa fa-spinner fa-pulse fa-3x fa-fw' style={{ color: 'grey' }} />
                </div>
            );
        }
        return (
            <React.Fragment>
                <div className='ballerina-editor design-view-container'>
                    <BallerinaDesignView
                        model={currentAST}
                        mode={diagramMode}
                        editMode={editMode}
                        height={height}
                        width={width}
                        onModeChange={(evt) => {
                            this.setState({
                                editMode: evt.editMode,
                            });
                        }}
                        onCodeExpandToggle={(evt) => {
                            this.setState({
                                diagramMode: evt.mode,
                            });
                        }}
                    />
                </div>
            </React.Fragment>);
    }
}

BallerinaDiagram.propTypes = {
    parseContent: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    content: PropTypes.string.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export {
    BallerinaDiagram,
    renderStaticDiagram,
};
