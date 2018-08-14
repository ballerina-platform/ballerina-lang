// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


public type LoadBalancerRounRobinRule object {

   public TargetService[] targets;
   public CallerActions[] loadBalanceClientsArray;
   public int nextIndex;
   //public (function (LoadBalancerActions, CallerActions[]) returns CallerActions) algorithm;

   documentation {
        Load Balancer adds an additional layer to the HTTP client to make network interactions more resilient.

        P{{loadBalanceClientsArray}} Array of HTTP clients for load balancing
   }
   public new (targets, nextIndex) {

   }

   public function resolve() returns CallerActions;

};

function LoadBalancerRounRobinRule::resolve() returns CallerActions {
   CallerActions httpClient;

   lock {
      if (self.nextIndex == ((lengthof (self.loadBalanceClientsArray)) - 1)) {
         httpClient = self.loadBalanceClientsArray[self.nextIndex];
         self.nextIndex = 0;
      } else {
         httpClient = self.loadBalanceClientsArray[self.nextIndex];
         self.nextIndex = self.nextIndex + 1;
      }
   }

   return httpClient;
}
