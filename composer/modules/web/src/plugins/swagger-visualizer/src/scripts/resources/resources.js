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
import proptypes from 'prop-types';
import { Accordion, Button, Icon } from 'semantic-ui-react';

import OasResource from './resource';
import OasAddResource from './add-resource';
import SwaggerAppContext from '../../context/app-context';


/**
 * Wrapper component to map resources in
 * swagger JSON.
 */
class OasResources extends React.Component {
    /**
     * Constructor
     * @param {Object} props props object
     */
    constructor(props) {
        super(props);

        this.state = {
            activeIndex: -1,
            showAddResource: false,
        };

        this.handleResourceExpand = this.handleResourceExpand.bind(this);
        this.handleShowAddResource = this.handleShowAddResource.bind(this);
    }

    /**
     * Event handler for resource component expand
     * @param {Object} e click event
     * @param {Object} titleProps Accordion title props
     */
    handleResourceExpand(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;

        this.setState({ activeIndex: newIndex });
    }

    /**
     * Event handler for show add resource form
     */
    handleShowAddResource() {
        const { showAddResource } = this.state;
        this.setState({
            showAddResource: !showAddResource,
        });
    }

    /**
     * Render Method
     * @returns {ReactElement} Resource element
     */
    /* eslint-disable */
    render() {
        const { resObj } = this.props;
        const { activeIndex, showAddResource } = this.state;
        return (
            <React.Fragment>
                <Button size='mini' icon labelPosition='left' onClick={this.handleShowAddResource}>
                    <Icon name='plus' />
                    Add Resource
                </Button>
                {showAddResource && 
                    <SwaggerAppContext.Consumer>
                        {(appContext) => {
                            return (
                                <OasAddResource onAddResource={appContext.onAddResource} />
                            );
                        }}
                    </SwaggerAppContext.Consumer>
                }
                {resObj && Object.keys(resObj).length > 0 &&
                    <Accordion className='resource-container' fluid>
                        {resObj && Object.keys(resObj).map((resource, index) => {
                            return (
                                <OasResource
                                    resPath={resource}
                                    oasOps={resObj[resource]}
                                    activeIndex={activeIndex}
                                    currIndex={index}
                                    handleExpand={this.handleResourceExpand}
                                />
                            );
                        })}
                    </Accordion>
                }
            </React.Fragment>
            
        );
    }
}

OasResources.prototypes = {
    resObj: proptypes.object.isRequired,
};

export default OasResources;
