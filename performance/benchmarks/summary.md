# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service | An HTTP Service, which forwards all requests to a back-end service. |
| Passthrough HTTPS service | An HTTPS Service, which forwards all requests to a back-end service. |
| JSON to XML transformation HTTP service | An HTTP Service, which transforms JSON requests to XML and then forwards all requests to a back-end service. |
| JSON to XML transformation HTTPS service | An HTTPS Service, which transforms JSON requests to XML and then forwards all requests to a back-end service. |
| Passthrough HTTP2 (HTTPS) service | An HTTPS Service exposed over HTTP2 protocol, which forwards all requests to a back-end service. |

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
| Concurrent Users | The number of users accessing the application at the same time. | 50, 150, 500 |
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

|  Scenario Name | Concurrent Users | Message Size (Bytes) | Back-end Service Delay (ms) | Error % | Throughput (Requests/sec) | Average Response Time (ms) | Standard Deviation of Response Time (ms) | 99th Percentile of Response Time (ms) | Ballerina GC Throughput (%) | Average of Ballerina Memory Footprint After Full GC (M) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
|  Passthrough HTTP service | 100 | 50 | 0 | 0 | 22774.51 | 4.35 | 5.95 | 34 | 99.51 |  |
|  Passthrough HTTP service | 100 | 1024 | 0 | 0 | 21554.19 | 4.59 | 6.41 | 37 | 99.54 |  |
|  Passthrough HTTP service | 300 | 50 | 0 | 0 | 23672.03 | 12.62 | 10.14 | 58 | 98.91 |  |
|  Passthrough HTTP service | 300 | 1024 | 0 | 0 | 21899.7 | 13.64 | 11.24 | 66 | 99.03 |  |
|  Passthrough HTTP service | 1000 | 50 | 0 | 0 | 22095.87 | 45.19 | 23.39 | 137 | 97.13 |  |
|  Passthrough HTTP service | 1000 | 1024 | 0 | 0 | 21117.11 | 47.26 | 23.81 | 139 | 97.31 |  |
|  Passthrough HTTP2 (HTTPS) service | 100 | 50 | 0 | 0 | 17191.4 | 5.6 | 7.67 | 41 | 99.56 | 25.65 |
|  Passthrough HTTP2 (HTTPS) service | 100 | 1024 | 0 | 0 | 15982.96 | 5.99 | 6.9 | 34 | 99.6 | 25.675 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 50 | 0 | 0 | 17498.42 | 16.58 | 14.02 | 73 | 99.06 | 26.021 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 1024 | 0 | 0 | 16229.81 | 17.75 | 13.42 | 67 | 99.2 | 26.01 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 50 | 0 | 1.98 | 9254.17 | 104.77 | 183.26 | 1023 | 98.45 | 26.736 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 1024 | 0 | 0 | 15493.16 | 63.03 | 44.24 | 179 | 98.06 | 26.922 |
|  Passthrough HTTPS service | 100 | 50 | 0 | 0 | 21126.54 | 4.68 | 6.68 | 38 | 99.5 | 25.466 |
|  Passthrough HTTPS service | 100 | 1024 | 0 | 0 | 17246.29 | 5.74 | 5.63 | 25 | 99.57 | 25.708 |
|  Passthrough HTTPS service | 300 | 50 | 0 | 0 | 21222.59 | 14.07 | 11.74 | 66 | 98.97 | 26.004 |
|  Passthrough HTTPS service | 300 | 1024 | 0 | 0 | 17140.13 | 17.44 | 11.29 | 60 | 99.15 | 26.215 |
|  Passthrough HTTPS service | 1000 | 50 | 0 | 0 | 19464.76 | 51.3 | 23.78 | 136 | 97.42 | 26.847 |
|  Passthrough HTTPS service | 1000 | 1024 | 0 | 0 | 16207.92 | 61.61 | 25.52 | 145 | 97.7 | 26.772 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 14133.86 | 7.03 | 8.19 | 43 | 99.1 | 24.93 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9917.89 | 10.03 | 10.36 | 60 | 99.14 | 25.05 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 15229.51 | 19.65 | 12.35 | 71 | 97.87 | 25.229 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10793.63 | 27.73 | 14.21 | 81 | 97.79 | 25.262 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 14600.75 | 68.4 | 19.13 | 127 | 93.56 | 26.21 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 10194.82 | 98.01 | 22.78 | 163 | 92.78 | 26.196 |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 12918.43 | 7.69 | 7.66 | 41 | 99.12 | 24.459 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 9010.8 | 11.05 | 9.36 | 52 | 99.17 | 24.479 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 13866.8 | 21.57 | 13.66 | 79 | 98 | 24.997 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 9533.03 | 31.4 | 16.96 | 95 | 97.99 | 25.034 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0 | 13444.79 | 74.3 | 25.59 | 157 | 94.22 | 26.04 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 0 | 9010.88 | 110.9 | 40.1 | 238 | 93.89 | 26.116 |