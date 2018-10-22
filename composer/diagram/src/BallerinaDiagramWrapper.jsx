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
import React from 'react';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from 'prop-types';
import { Dimmer, Loader } from 'semantic-ui-react';
import DesignView from 'plugins/ballerina/views/design-view';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';
import FragmentUtils from 'plugins/ballerina/utils/fragment-utils';
import '@ballerina/theme/default-theme/index.js/';
import './scss/themes/default.scss';
import '@ballerina/theme/font-ballerina';

const BallerinaDesignView = DragDropContext(HTML5Backend)(DesignView);

const TREE_MODIFIED = 'tree-modified';

/**
 * A wrapper component to wrap Editable Ballerina Diagram.
 */
class BallerinaDiagramWrapper extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            currentAST: undefined,
            editMode: true,
            diagramMode: 'action',
        };
        if (props.parseFragment) {
            FragmentUtils.setParseFragmentFn(props.parseFragment);
        }
    }

    getChildContext() {
        return {
            getEndpoints: this.props.getEndpoints,
            astRoot: this.state.currentAST,
            goToSource: this.props.goToSource,
        };
    }

    componentDidMount() {
        this.updateDiagram(this.props.docUri);
    }

    componentWillReceiveProps(nextProps) {
        this.updateDiagram(nextProps.docUri);
    }

    onModelUpdate(evt) {
        this.forceUpdate();
        const { currentAST } = this.state;
        if (currentAST) {
            this.props.onChange({ newAST: currentAST });
        }
    }

    updateDiagram(docUri) {
        this.props.getAST(docUri)
                .then((parserReply) => {
                    const { currentAST } = this.state;
                    if (parserReply.ast) {
                        if (currentAST) {
                            currentAST.off(TREE_MODIFIED, this.onModelUpdate);
                        }
                        const newAST = TreeBuilder.build(parserReply.ast);
                        if (newAST) {
                            newAST.on(TREE_MODIFIED, this.onModelUpdate.bind(this));
                        }
                        this.setState({
                            currentAST: newAST,
                        });
                    }
                });
    }

    render() {
        const { currentAST, diagramMode, editMode } = this.state;
        const { width, height } = this.props;
        if (!currentAST) {
            return (
                <Dimmer active inverted>
                    <Loader size='large'></Loader>
                </Dimmer>
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

BallerinaDiagramWrapper.childContextTypes = {
    getEndpoints: PropTypes.func,
    astRoot: PropTypes.instanceOf(Object).isRequired,
    goToSource: PropTypes.func.isRequired,
};

BallerinaDiagramWrapper.propTypes = {
    getAST: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    docUri: PropTypes.string.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    getEndpoints: PropTypes.func,
    parseFragment: PropTypes.func,
    goToSource: PropTypes.func.isRequired,
};

export default BallerinaDiagramWrapper;
