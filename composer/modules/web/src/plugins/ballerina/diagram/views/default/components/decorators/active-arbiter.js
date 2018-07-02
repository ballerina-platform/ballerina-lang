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

 // TODOX: Clean up active arbiter code
class ActiveArbiter {
    readyToDeactivate(statement) {
        clearTimeout(this.timeout);
        if (statement.state.active === 'visible') {
            setTimeout(() => {
                if (statement.state.active === 'fade') {
                    //statement.setState({ active: 'hidden' });
                }
            }, 500);
            //statement.setState({ active: 'fade' });
        }
    }

    readyToDelayedActivate(statement) {
        this.timeout = setTimeout(() => {
            this.readyToActivate(statement);
        }, 500);
    }

    readyToActivate(statement) {
        clearTimeout(this.timeout);
        if (this.active && this.active !== statement) {
            if (this.active.active !== 'hidden') {
               // this.active.setState({ active: 'hidden' });
            }
        }
        this.active = statement;
        if (statement.state.active !== 'visible') {
            //statement.setState({ active: 'visible' });
        }
    }
}
export default ActiveArbiter;
