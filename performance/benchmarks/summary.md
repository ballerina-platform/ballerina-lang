# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service (h1c -> h1c) | An HTTP Service, which forwards all requests to an HTTP back-end service. |
| Passthrough HTTPS service (h1 -> h1) | An HTTPS Service, which forwards all requests to an HTTPS back-end service. |
| JSON to XML transformation HTTP service | An HTTP Service, which transforms JSON requests to XML and then forwards all requests to an HTTP back-end service. |
| JSON to XML transformation HTTPS service | An HTTPS Service, which transforms JSON requests to XML and then forwards all requests to an HTTPS back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h2) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTP/2(over TLS) back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h1) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTPS back-end service. |
| Passthrough HTTP/2(over TLS) service (h2 -> h1c) | An HTTPS Service exposed over HTTP/2 protocol, which forwards all requests to an HTTP back-end service. |
| HTTP/2 client and server downgrade service (h2 -> h2) | An HTTP/2(with TLS) server accepts requests from an HTTP/1.1(with TLS) client and the HTTP/2(with TLS) client sends requests to an HTTP/1.1(with TLS) back-end service. Both the upstream and the downgrade connection is downgraded to HTTP/1.1(with TLS). |

Our test client is [Apache JMeter](https://jmeter.apache.org/index.html). We test each scenario for a fixed duration of
time. We split the test results into warmup and measurement parts and use the measurement part to compute the
performance metrics.

A majority of test scenarios use a [Netty](https://netty.io/) based back-end service which echoes back any request
posted to it after a specified period of time.

We run the performance tests under different numbers of concurrent users, message sizes (payloads) and back-end service
delays.

The main performance metrics:

1. **Throughput**: The number of requests that the Ballerina service processes during a specific time interval (e.g. per second).
2. **Response Time**: The end-to-end latency for an operation of invoking a Ballerina service. The complete distribution of response times was recorded.

In addition to the above metrics, we measure the load average and several memory-related metrics.

The following are the test parameters.

| Test Parameter | Description | Values |
| --- | --- | --- |
| Scenario Name | The name of the test scenario. | Refer to the above table. |
| Heap Size | The amount of memory allocated to the application | 2G |
| Concurrent Users | The number of users accessing the application at the same time. | 100, 300, 1000 |
| Message Size (Bytes) | The request payload size in Bytes. | 50, 1024 |
| Back-end Delay (ms) | The delay added by the back-end service. | 0 |

The duration of each test is **900 seconds**. The warm-up period is **300 seconds**.
The measurement results are collected after the warm-up period.

A [**c5.xlarge** Amazon EC2 instance](https://aws.amazon.com/ec2/instance-types/) was used to install Ballerina.

The following are the measurements collected from each performance test conducted for a given combination of
test parameters.

| Measurement | Description |
| --- | --- |
| Error % | Percentage of requests with errors |
| Average Response Time (ms) | The average response time of a set of results |
| Standard Deviation of Response Time (ms) | The “Standard Deviation” of the response time. |
| 99th Percentile of Response Time (ms) | 99% of the requests took no more than this time. The remaining samples took at least as long as this |
| Throughput (Requests/sec) | The throughput measured in requests per second. |
| Average Memory Footprint After Full GC (M) | The average memory consumed by the application after a full garbage collection event. |

The following is the summary of performance test results collected for the measurement period.

|  Scenario Name | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | Ballerina GC Throughput (%) | Average Ballerina Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Passthrough HTTP service (h1c -> h1c) | 100 | 50 | 0 | 0 | 14671.84 | 6.78 | 3.95 | 18 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 100 | 1024 | 0 | 0 | 13830.56 | 7.19 | 4.18 | 19 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 50 | 0 | 0 | 14960.15 | 20 | 8.5 | 44 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 300 | 1024 | 0 | 0 | 14892.82 | 20.09 | 8.4 | 44 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 50 | 0 | 0 | 13922.32 | 71.75 | 21.17 | 131 | N/A | N/A |
|  Passthrough HTTP service (h1c -> h1c) | 1000 | 1024 | 0 | 0 | 14055 | 71.07 | 21.09 | 130 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 9656.21 | 10.32 | 6.8 | 32 | N/A | N/A |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 7360.45 | 13.54 | 7.04 | 37 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 9854.45 | 30.39 | 12.91 | 68 | N/A | N/A |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 7193.73 | 41.65 | 10.9 | 78 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 9612.22 | 103.96 | 27.04 | 180 | N/A | N/A |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 6860.09 | 145.65 | 28.4 | 220 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 50 | 0 | 0 | 12483.66 | 7.97 | 4.16 | 20 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 100 | 1024 | 0 | 0 | 10929.29 | 9.1 | 4.41 | 22 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 50 | 0 | 0 | 12049.19 | 24.84 | 9.73 | 52 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 300 | 1024 | 0 | 0 | 11105.17 | 26.95 | 10.17 | 56 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 50 | 0 | 0 | 11781.69 | 84.79 | 23.94 | 152 | N/A | N/A |
|  Passthrough HTTPS service (h1 -> h1) | 1000 | 1024 | 0 | 0 | 11149.1 | 89.6 | 25.07 | 160 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 8381.08 | 11.89 | 7.13 | 34 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 6375.05 | 15.64 | 9.17 | 45 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 8300.19 | 36.08 | 16.45 | 83 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 6469.1 | 46.31 | 17.75 | 99 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 8407.65 | 118.85 | 36 | 218 | N/A | N/A |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 5999.05 | 166.58 | 37.44 | 269 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 50 | 0 | 0 | 11316.25 | 8.64 | 4.28 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 100 | 1024 | 0 | 0 | 10873.75 | 8.93 | 4.33 | 22 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 50 | 0 | 0 | 11544.45 | 25.6 | 9.93 | 54 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 300 | 1024 | 0 | 0 | 11239.65 | 26.07 | 9.97 | 55 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 50 | 0 | 0 | 10987.79 | 90.31 | 26.68 | 166 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1c) | 1000 | 1024 | 0 | 0 | 10691.01 | 92.57 | 27.56 | 172 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 50 | 0 | 0 | 10618.3 | 9.23 | 4.56 | 22 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 100 | 1024 | 0 | 0 | 10323.67 | 9.42 | 4.39 | 22 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 50 | 0 | 0 | 10592.08 | 27.97 | 10.67 | 58 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 300 | 1024 | 0 | 0 | 10004.39 | 29.44 | 11.03 | 61 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 50 | 0 | 0 | 10140.49 | 98.22 | 27.76 | 178 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h1) | 1000 | 1024 | 0 | 0 | 10006.98 | 99.3 | 28.2 | 181 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 50 | 0 | 0 | 12642.22 | 7.86 | 4.1 | 19 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 100 | 1024 | 0 | 0 | 11342.44 | 8.76 | 4.31 | 21 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 50 | 0 | 0 | 12452.82 | 24.03 | 9.45 | 50 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11821.45 | 25.31 | 9.79 | 53 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12096.18 | 82.54 | 23.76 | 149 | N/A | N/A |
|  HTTP/2 client and server downgrade service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11421.73 | 87.45 | 24.14 | 155 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 50 | 0 | 0 | 11288.94 | 8.68 | 4.28 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 100 | 1024 | 0 | 0 | 10702.02 | 9.09 | 4.31 | 21 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 50 | 0 | 0 | 11969.65 | 24.69 | 9.63 | 52 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 300 | 1024 | 0 | 0 | 11347.38 | 25.87 | 9.94 | 54 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 50 | 0 | 0 | 12092.39 | 82.2 | 23.69 | 149 | N/A | N/A |
|  Passthrough HTTP/2(over TLS) service (h2 -> h2) | 1000 | 1024 | 0 | 0 | 11285.09 | 87.99 | 26.26 | 163 | N/A | N/A |
