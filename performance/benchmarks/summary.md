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
|  Passthrough HTTP service | 100 | 50 | 0 | 0 | 20401.82 | 4.85 | 6.87 | 42 | 99.49 |  |
|  Passthrough HTTP service | 100 | 1024 | 0 | 0 | 18928.92 | 5.23 | 7.04 | 43 | 99.53 |  |
|  Passthrough HTTP service | 300 | 50 | 0 | 0 | 22102.41 | 13.52 | 11.23 | 67 | 98.75 |  |
|  Passthrough HTTP service | 300 | 1024 | 0 | 0 | 20729.52 | 14.41 | 12.22 | 72 | 98.87 |  |
|  Passthrough HTTP service | 1000 | 50 | 0 | 0 | 21094.88 | 47.33 | 24.59 | 141 | 96.41 |  |
|  Passthrough HTTP service | 1000 | 1024 | 0 | 0 | 19628.95 | 50.85 | 25 | 146 | 96.59 |  |
|  Passthrough HTTPS service | 100 | 50 | 0 | 0 | 19804.74 | 5 | 7.11 | 41 | 99.46 | 24.643 |
|  Passthrough HTTPS service | 100 | 1024 | 0 | 0 | 16073.14 | 6.17 | 6.14 | 27 | 99.55 | 24.165 |
|  Passthrough HTTPS service | 300 | 50 | 0 | 0 | 20821.53 | 14.34 | 12.19 | 70 | 98.81 | 24.63 |
|  Passthrough HTTPS service | 300 | 1024 | 0 | 0 | 16563.87 | 18.04 | 11.53 | 61 | 99.03 | 25.014 |
|  Passthrough HTTPS service | 1000 | 50 | 0 | 0 | 18307.58 | 54.54 | 27.88 | 156 | 96.81 | 25.841 |
|  Passthrough HTTPS service | 1000 | 1024 | 0 | 0 | 15397.82 | 64.87 | 26.81 | 152 | 97.2 | 25.908 |
|  JSON to XML transformation HTTP service | 100 | 50 | 0 | 0.06 | 135.05 | 740.6 | 1748.9 | 6175 | 13.89 | 1858.652 |
|  JSON to XML transformation HTTP service | 100 | 1024 | 0 | 0 | 160.44 | 623.41 | 1274.33 | 5791 | 13.85 | 1849.554 |
|  JSON to XML transformation HTTP service | 300 | 50 | 0 | 1.88 | 122.58 | 2392.12 | 4818.18 | 30079 | N/A | N/A |
