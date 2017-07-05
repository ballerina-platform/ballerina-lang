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
import log from 'log';
import React from 'react';
import PropTypes from 'prop-types';
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';
import File from './../../workspace/file';
import { parseFile } from '../../api-client/api-client';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import BallerinaASTRoot from './../ast/ballerina-ast-root';

export const DESIGN_VIEW = 'DESIGN_VIEW';
export const SOURCE_VIEW = 'SOURCE_VIEW';
export const SWAGGER_VIEW = 'SWAGGER_VIEW';

/**
 * React component for BallerinaFileEditor.
 *
 * @class BallerinaFileEditor
 * @extends {React.Component}
 */
class BallerinaFileEditor extends React.Component {

    /**
     * Creates an instance of BallerinaFileEditor.
     * @param {Object} props React properties.
     * @memberof BallerinaFileEditor
     */
    constructor(props) {
        super(props);
        this.state = {
            parseFailed: false,
            model: new BallerinaASTRoot(),
            activeView: DESIGN_VIEW,
        };
        this.onViewChange = this.onViewChange.bind(this);
        this.parseFile()
                .then((resp) => {
                    this.setState({
                        model: BallerinaASTDeserializer.getASTModel(resp),
                    });
                    this.forceUpdate();
                })
                .catch((error) => {
                    log.error(error);
                });
    }

    componentDidMount() {
    }

    /**
     * Parse current content of the file
     * and build AST
     */
    parseFile() {
        return new Promise((resolve, reject) => {
            parseFile(this.props.file)
            .then(response => resolve(response))
            .catch(error => reject(error));
        });
    }

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            editor: this,
        };
    }

    /**
     * Invoked when view is changed
     * @param {STRING} newView ID of the new View
     */
    onViewChange(newView) {
        this.setState({ activeView: newView });
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        return (
            <div id="tab-template">
                {!this.state.parseFailed && this.state.activeView === DESIGN_VIEW
                    && <DesignView model={this.state.model} onViewChange={this.onViewChange} />
                }
                {(this.state.parseFailed || this.state.activeView === SOURCE_VIEW)
                    && <SourceView content={this.props.file.getContent()} />
                }
                {!this.state.parseFailed && this.state.activeView === SWAGGER_VIEW
                    && <SwaggerView />
                }
            </div>
        );
    }
}

BallerinaFileEditor.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
};

BallerinaFileEditor.childContextTypes = {
    editor: PropTypes.instanceOf(BallerinaFileEditor).isRequired,
};

export default BallerinaFileEditor;
