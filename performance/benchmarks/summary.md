# Ballerina Performance Test Results

During each release, we execute various automated performance test scenarios and publish the results.

| Test Scenarios | Description |
| --- | --- |
| Passthrough HTTP service | An HTTP Service, which forwards all requests to a back-end service. |
| Passthrough HTTPS service | An HTTPS Service, which forwards all requests to an HTTPS back-end service. |
| JSON to XML transformation HTTP service | An HTTP Service, which transforms JSON requests to XML and then forwards all requests to a back-end service. |
| JSON to XML transformation HTTPS service | An HTTPS Service, which transforms JSON requests to XML and then forwards all requests to an HTTPS back-end service. |
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
|  Passthrough HTTP2 (HTTPS) service | 100 | 50 | 0 | 0 | 15665.63 | 6.2 | 8.26 | 47 | 99.53 | 24.109 |
|  Passthrough HTTP2 (HTTPS) service | 100 | 1024 | 0 | 0 | 14593.18 | 6.63 | 8.21 | 45 | 99.58 | 24.712 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 50 | 0 | 0 | 15944.22 | 18.34 | 16.45 | 85 | 98.99 | 24.979 |
|  Passthrough HTTP2 (HTTPS) service | 300 | 1024 | 0 | 0 | 15075.02 | 19.29 | 15.56 | 80 | 99.1 | 25.166 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 50 | 0 | 5.36 | 6711.43 | 148.46 | 215.5 | 1023 | 98.23 | 25.711 |
|  Passthrough HTTP2 (HTTPS) service | 1000 | 1024 | 0 | 1.9 | 9504.14 | 103.83 | 162.46 | 931 | 98.02 | 25.968 |
|  Passthrough HTTPS service | 100 | 50 | 0 | 0 | 18081.3 | 5.49 | 7.77 | 43 | 99.46 | 24.449 |
|  Passthrough HTTPS service | 100 | 1024 | 0 | 0 | 14225.89 | 6.98 | 5.78 | 27 | 99.53 | 24.471 |
|  Passthrough HTTPS service | 300 | 50 | 0 | 0 | 18507.84 | 16.15 | 13.64 | 77 | 98.83 | 24.938 |
|  Passthrough HTTPS service | 300 | 1024 | 0 | 0 | 14733.57 | 20.29 | 11.6 | 62 | 98.96 | 25.046 |
|  Passthrough HTTPS service | 1000 | 50 | 0 | 0.23 | 12385.44 | 76.84 | 1453.07 | 31 | 89.4 | 1421.063 |
|  Passthrough HTTPS service | 1000 | 1024 | 0 | 0.3 | 9630.47 | 98.68 | 1645.7 | 32 | 88.76 | 1433.613 |
|  Passthrough HTTP service | 100 | 50 | 0 | 0 | 19841.41 | 5 | 6.73 | 40 | 99.46 |  |
|  Passthrough HTTP service | 100 | 1024 | 0 | 0 | 18470.96 | 5.37 | 6.7 | 39 | 99.51 |  |
|  Passthrough HTTP service | 300 | 50 | 0 | 0 | 21858.84 | 13.67 | 11.55 | 68 | 98.72 |  |
|  Passthrough HTTP service | 300 | 1024 | 0 | 0 | 20256.05 | 14.75 | 12.46 | 74 | 98.83 |  |
|  Passthrough HTTP service | 1000 | 50 | 0 | 0 | 21109.8 | 47.29 | 23.87 | 144 | 96.14 |  |
|  Passthrough HTTP service | 1000 | 1024 | 0 | 0 | 19936.21 | 50.09 | 25.49 | 154 | 96.51 |  |
|  JSON to XML transformation HTTPS service | 100 | 50 | 0 | 0 | 11562.34 | 8.61 | 8.14 | 45 | 99.12 | 23.769 |
|  JSON to XML transformation HTTPS service | 100 | 1024 | 0 | 0 | 7860.13 | 12.65 | 10.49 | 59 | 99.12 | 23.784 |
|  JSON to XML transformation HTTPS service | 300 | 50 | 0 | 0 | 12588.22 | 23.78 | 16.47 | 95 | 97.82 | 24.23 |
|  JSON to XML transformation HTTPS service | 300 | 1024 | 0 | 0 | 8219.21 | 36.44 | 19.95 | 113 | 97.81 | 24.284 |
|  JSON to XML transformation HTTPS service | 1000 | 50 | 0 | 0.55 | 5408.41 | 176.11 | 2242.64 | 58 | 70.6 | 1650.731 |
|  JSON to XML transformation HTTPS service | 1000 | 1024 | 0 | 1.18 | 2789.66 | 338.99 | 3065.47 | 30079 | 61.36 | 1795.115 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0 | 13519.63 | 7.36 | 8.8 | 44 | 99.07 | 24.005 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 9505.48 | 10.47 | 10.17 | 55 | 99.11 | 24.037 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 0 | 14524.64 | 20.6 | 11.51 | 61 | 97.42 | 24.357 |
|  JSON to XML transformation HTTP service | 300 | 1024 | 0 | 0 | 10253.01 | 29.2 | 11.9 | 68 | 97.58 | 24.235 |
|  JSON to XML transformation HTTP service | 1000 | 50 | 0 | 0 | 13248.71 | 75.41 | 18.76 | 131 | 92.87 | 25.265 |
|  JSON to XML transformation HTTP service | 1000 | 1024 | 0 | 0 | 9628.53 | 103.77 | 21.38 | 161 | 91.69 | 25.303 |
