// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Types of protocols of a port.
public type PortProtocol "HTTP"|"HTTPS"|"GRPC"|"HTTP2"|"MONGO"|"TCP"|"TLS";

# Port of a service.
#
# + number - The port number.
# + protocol - The protocol exposed by the port.
# + name - Label for the port.
public type PortConfig record {|
    int number;
    PortProtocol protocol;
    string name;
|};

# TLS mode enforced by the proxy.
public type TLSOptionMode "PASSTHROUGH"|"SIMPLE"|"MUTUAL";

# Istio gateway server tls option configurations.
#
# + httpsRedirect - If set to true, the load balancer will send a 301 redirect for all http connections, asking the clients to use HTTPS. Default is `false`.
# + mode - Indicates whether connections to this port should be secured using TLS. The value of this field determines how TLS is enforced. Default is `"PASSTHROUGH"`.
# + serverCertificate - REQUIRED if mode is SIMPLE or MUTUAL. The path to the file holding the server-side TLS certificate to use.
# + privateKey - REQUIRED if mode is SIMPLE or MUTUAL. The path to the file holding the server’s private key.
# + caCertificates - REQUIRED if mode is MUTUAL. The path to a file containing certificate authority certificates to use in verifying a presented client side certificate.
# + subjectAltNames - A list of alternate names to verify the subject identity in the certificate presented by the client.
public type TLSOptionConfig record {|
    boolean httpsRedirect = false;
    TLSOptionMode mode = "PASSTHROUGH";
    string serverCertificate?;
    string privateKey?;
    string caCertificates?;
    string[] subjectAltNames?;
|};

# Istio gateway server configuration to describe the properties of the proxy on a given load balancer.
#
# + port - The port of the proxy.
# + hosts - List of hosts exposed by the gateway.
# + tls - TLS options.
public type ServerConfig record {|
    PortConfig port;
    string[] hosts;
    TLSOptionConfig tls?;
|};

# Istio gateway annotation configuration.
#
# + name - Name of the resource
# + labels - Map of labels for the resource
# + annotations - Map of annotations for resource
# + selector - Specific set of pods/VMs on which this gateway configuration should be applied.
# + servers - List of servers to pass.
public type GatewayConfig record {|
    string name?;
    map<string> labels?;
    map<string> annotations?;
    map<string> selector?;
    ServerConfig?[] servers?;
|};

# @istio:Gateway annotation to generate istio gateways.
public const annotation GatewayConfig Gateway on source service, source listener;

# Configuration to a network addressable service.
#
# + host - Host of a service.
# + subset - Subset within the service.
# + port - The port on the host that is being addressed.
public type DestinationConfig record {|
    string host;
    string subset?;
    int port?;
|};

# Configuration for weight for destination to traffic route.
#
# + destination - Destination to forward to.
# + weight - Weight for the destination.
public type DestinationWeightConfig record {|
    DestinationConfig destination;
    int weight?;
|};

# Configurations for conditions and actions for routing HTTP.
#
# + route - Route destination.
# + timeout - Timeout for requests in seconds.
# + appendHeaders - Additional header to add before forwarding/directing.
public type HTTPRouteConfig record {|
    DestinationWeightConfig[] route?;
    int 'timeout?;
    map<string> appendHeaders?;
|};

# Virtual service configuration for @istio:VirtualService annotation.
#
# + name - Name of the resource
# + labels - Map of labels for the resource
# + annotations - Map of annotations for resource
# + hosts - Destination which traffic should be sent.
# + gateways - Names of the gateways which the service should listen to.
# + http - Route rules for HTTP traffic.
public type VirtualServiceConfig record {|
    string name?;
    map<string> labels?;
    map<string> annotations?;
    string[] hosts?;
    string[] gateways?;
    HTTPRouteConfig[] http?;
|};

# @istio:VirtualService annotation to generate istio virtual service.
public const annotation VirtualServiceConfig VirtualService on source service, source listener;
