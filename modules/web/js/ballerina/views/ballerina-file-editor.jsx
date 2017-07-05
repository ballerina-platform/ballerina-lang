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
import React from 'react';
import PropTypes from 'prop-types';
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';

const DESIGN_VIEW = 'DESIGN_VIEW';
const SOURCE_VIEW = 'SOURCE_VIEW';
const SWAGGER_VIEW = 'SWAGGER_VIEW';

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
            activeView: DESIGN_VIEW,
        };
    }

    /**
     * Getter for getting the model.
     *
     * @returns {ASTNode} The model.
     * @memberof BallerinaFileEditor
     */
    getModel() {
        return this.model;
    }

    /**
     * Setter for model.
     *
     * @param {ASTNode} model The model.
     * @memberof BallerinaFileEditor
     */
    setModel(model) {
        this.model = model;

        this.model.on('tree-modified', () => {
            this.forceUpdate();
        });
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        return (
            <div id="tab-template">
                {this.state.activeView === DESIGN_VIEW
                    && <DesignView />
                }
                {this.state.activeView === SOURCE_VIEW
                    && <SourceView />
                }
                {this.state.activeView === SWAGGER_VIEW
                    && <SwaggerView />
                }
            </div>
        );
    }
}

export default BallerinaFileEditor;
