/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

/**
 * Represents commons in worker async-send, sync-send and single-receive.
 *
 * @since 2201.9.0
 */
public abstract class BLangWorkerSendReceiveExpr extends BLangExpression {

    public SymbolEnv env;
    public BSymbol workerSymbol;
    public BType workerType;
    public BLangIdentifier workerIdentifier;
    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public record Channel(String sender, String receiver, int eventIndex) {

        public String workerPairId() {
            return workerPairId(sender, receiver);
        }

        public static String workerPairId(String sender, String receiver) {
            return sender + "->" + receiver;
        }

        public String channelId() {
            return sender + "->" + receiver + ":" + eventIndex;
        }
    }
}
