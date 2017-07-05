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

/**
 * React component of a preview of a ballerina program shown in welcome page.
 *
 * @class ServicePreviewView
 * @extends {React.Component}
 */
class ServicePreviewView extends React.PureComponent {

    constructor(props) {
        super(props);
        this.state = {
            imagesLoadStatus: {},
        };
    }

    render() {
        let previewThumbnails = (null);
        if (this.props.sampleConfigs) {
            previewThumbnails = this.props.sampleConfigs.map((config, index) => (
                <div
                    className="col-sm-12 col-md-6 col-lg-3 thumbnail-wrapper"
                    onClick={config.clickEventCallback}
                    key={config.sampleName}
                >
                    <div
                        className={config.isFile ? 'thumbnail' : 'thumbnail multiple'}
                        style={{ 'text-align': 'center' }}
                    >
                        <img
                            id="previewImg"
                            src={`images/${config.image}.png`}
                            alt={config.sampleName}
                            onLoad={() => {
                                const statusObj = this.state.imagesLoadStatus;
                                statusObj[index] = true;
                                this.forceUpdate();
                            }}
                        />
                        {!this.state.imagesLoadStatus[index] &&
                            <i className="fw fw-loader5 fw-spin fw-2x" />
                        }
                        <div className="caption">
                            <h4>{config.sampleName}</h4>
                        </div>
                    </div>
                </div>));
        }

        return (<div>{previewThumbnails}</div>);
    }
}

ServicePreviewView.propTypes = {
    sampleConfigs: PropTypes.arrayOf(PropTypes.shape({
        clickEventCallback: PropTypes.func.isRequired,
        sampleName: PropTypes.string.isRequired,
        isFile: PropTypes.bool.isRequired,
        image: PropTypes.string.isRequired,
    })).isRequired,
};

export default ServicePreviewView;
