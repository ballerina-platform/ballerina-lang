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
