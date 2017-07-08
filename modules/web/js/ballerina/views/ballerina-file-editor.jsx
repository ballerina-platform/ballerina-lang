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
import PackageScopedEnvironment from './../env/package-scoped-environment';

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
            parseFailed: true,
            model: new BallerinaASTRoot(),
            activeView: DESIGN_VIEW,
        };
        this.onViewChange = this.onViewChange.bind(this);
        this.environment = new PackageScopedEnvironment();
        this.parseFile();
    }

    /**
     * Update the diagram
     */
    update() {
        this.forceUpdate();
    }

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            editor: this,
            environment: this.environment,
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
     * Parse current content of the file
     * and build AST
     */
    parseFile() {
        parseFile(this.props.file)
            .then((jsonTree) => {
                // get ast from json
                const ast = BallerinaASTDeserializer.getASTModel(jsonTree);
                const pkgName = ast.getPackageDefinition().getPackageName();
                
                this.setState({
                    parseFailed: false,
                    model: ast,
                });
                this.forceUpdate();
            })
            .catch((error) => {
                log.error(error);
            });
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        return (
            <div id={`bal-file-editor-${this.props.file.id}`}>
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
    file: PropTypes.instanceOf(File).isRequired
};

BallerinaFileEditor.childContextTypes = {
    editor: PropTypes.instanceOf(BallerinaFileEditor).isRequired,
    environment: PropTypes.instanceOf(PackageScopedEnvironment).isRequired,
};

export default BallerinaFileEditor;
