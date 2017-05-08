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

class ServicePreviewView extends React.Component {
    
    render() {
        var previewThumbnails = this.props.sampleConfigs.map(function(config) {
            return (<div className="item active" onClick={config.clickEventCallback}>
                <div className="col-xs-3 preview-parent">
                    <div className="preview-div">
                        <img id="previewImg" className="preview-img" src={"images/preview_" + config.sampleName + ".png"} />
                    </div>
                    <div className="preview-name-div">
                        <div className="file-name">
                            <span>{config.sampleName}</span>
                        </div>
                    </div>
                </div>
            </div>);
        });

        return (<div>{previewThumbnails}</div>);
    }

}

export default ServicePreviewView;
