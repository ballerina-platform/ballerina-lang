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



import ballerina/http;
import ballerina/websub;

service websubSubscriber =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhub.com", "http://websubpubtopic.com"]
}
service {
    resource function onIntentVerification (websub:Caller caller, websub:IntentVerificationRequest verRequest) {
    }

    resource function onNotification (websub:Notification notification) {
    }
};

service websubSubscriberTwo =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onIntentVerification (http:Listener caller, websub:Notification notification) {
    }

    resource function onNotification (websub:IntentVerificationRequest verRequest) {
    }
};

service websubSubscriberThree =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onNotificationTwo (websub:IntentVerificationRequest verRequest) {
    }
};

service websubSubscriberFour =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onIntentVerification (websub:Caller caller, websub:IntentVerificationRequest verRequest) {
    }
};

service websubSubscriberFive =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onIntentVerification (websub:Caller caller) {
    }

    resource function onNotification () {
    }
};

service websubSubscriberSix =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onIntentVerification (websub:IntentVerificationRequest verRequest) {
    }

    resource function onNotification (websub:Notification notification) {
    }
};

service websubSubscriberSeven =
@websub:SubscriberServiceConfig {
    path: "/websub",
    subscribeOnStartUp: true,
    target: ["http://websubpubhubtwo.com", "http://websubpubtopictwo.com"]
}
service {
    resource function onIntentVerification (websub:Caller caller, websub:IntentVerificationRequest verRequest, string s) {
    }

    resource function onNotification (websub:Caller caller, websub:Notification notification) {
    }
};

service websubSubscriberEight =
service {
    resource function noSubscriberServiceConfig (websub:Notification notification) {
    }
};
