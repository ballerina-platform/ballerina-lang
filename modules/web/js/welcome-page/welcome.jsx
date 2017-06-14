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

class WelcomeView extends React.Component {

    render() {
        return (<div className="initial-background-container">
          <div className="container-fluid welcome-wrapper">
            <div className="media media-welcome-container">
              <div className="media-left">
                <div className="logo">
                  <a href="#">
                    <img className="img-responsive" src="images/ballerina_logo.png" />
                  </a>
                </div>

                <button id="btn-welcome-new" className="btn btn-primary">Create New </button>
                <button id="btn-welcome-open" className="btn btn-secondary"> Open File</button>

                <ul className="nav nav-pills">
                  {/* <li ><a href="#"><i className="fw fw-settings"></i> Settings</a></li>*/}
                  {/* <li ><a href="#"><i className="fw fw-settings"></i> Select a Theme</a></li>*/}
                  {/* <li ><a href="#"><i className="fw fw-settings"></i> Shortcuts</a></li>*/}
                  <li ><a href="http://ballerinalang.org/docs/user-guide/0.8/" target="_blank">
                    <i className="fw fw-document" /> User Guide </a></li>
                </ul>
              </div>
              <div className="media-body">
                {/* <div className="welcome-details-wrapper ">*/}
                {/* <div className="header-title">*/}
                {/* Recently opened*/}
                {/* </div>*/}
                {/* <div className="details-container">*/}
                {/* <div className="row">*/}
                {/* <div className="col-md-3 thumbnail-wrapper">*/}
                {/* <div className="thumbnail">*/}
                {/* <img src="images/tweetMediumFeed.bal.jpg"/>*/}
                {/* <div className="caption">*/}
                {/* <h4>contentBasedRoutingService.bal</h4>*/}
                {/* <p>Apr 19th, 02:30pm</p>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* <div className="col-md-3 thumbnail-wrapper">*/}
                {/* <div className="thumbnail">*/}
                {/* <img src="images/ordersService.bal.jpg"/>*/}
                {/* <div className="caption">*/}
                {/* <h4>ordersService.bal</h4>*/}
                {/* <p>Apr 19th, 02:30pm</p>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* <div className="col-md-3 thumbnail-wrapper">*/}
                {/* <div className="thumbnail">*/}
                {/* <img src="images/jmsReceiver.bal.jpg"/>*/}
                {/* <div className="caption">*/}
                {/* <h4>jmsReceiver.bal</h4>*/}
                {/* <p>Apr 19th, 02:30pm</p>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* <div className="col-md-3 thumbnail-wrapper">*/}
                {/* <div className="thumbnail">*/}
                {/* <img src="images/tweetOpenPR.bal.jpg"/>*/}
                {/* <div className="caption">*/}
                {/* <h4>tweetOpenPR.bal</h4>*/}
                {/* <p>Apr 19th, 02:30pm</p>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                {/* </div>*/}
                <div className="welcome-details-wrapper ">
                  <div className="header-title">
                                 Try out our samples / templates
                             </div>
                  <div className="details-container">
                    <div id="inner-samples" className="row" />
                  </div>
                </div>

              </div>
            </div>
          </div>


        </div>);
    }
}

export default WelcomeView;
