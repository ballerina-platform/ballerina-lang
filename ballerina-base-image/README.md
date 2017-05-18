# Ballerina Base Docker Image

This Dockerfile can be used to create a Ballerina base Docker distribution that can be used to run Ballerina archives and package Ballerina archives as distributable Docker images.

## Usage
The Docker distribution for Ballerina is available on Docker Hub as `ballerinalang/ballerina`. To run a Ballerina package using the Ballerina Docker image, simply mount the folder containing the package(s) to `/ballerina/files` folder inside the container. 

```bash
# Create a directory and copy the packages needed to be run.
mkdir -p ~/ballerina/packages/
cp mypackage.bmz ~/ballerina/packages/
chmod +r ~/ballerina/packages/mypackage.bmz

# Run container with the volume mount
docker run -v ~/ballerina/packages:/ballerina/files -it ballerinalang/ballerina
```

If you are running a Ballerina Service, set the `SVC_MODE` environment variable to `true`. 

```bash
cp mysvcpackage.bsz ~/ballerina/packages/
chmod +r ~/ballerina/packages/mysvcpackage.bsz

docker run -v /tmp/testb:/ballerina/files -e "SVC_MODE=true" -p 9090:9090 -it ballerinalang/ballerina
```

## Building the image
```bash
# bash build.sh -d <ballerina-dist> to build ballerina:latest
cp ~/Downloads/ballerina-0.87.zip .
bash build.sh -d ballerina-0.87.zip

# Or build without copying the distribution.
bash build.sh -d ~/Downloads/ballerina-0.87.zip

# Show usage
bash build.sh -h
```
The above command packs the Ballerina Runtime in to the Docker image.
## As a Parent Image
This base image can be used by child images to pack one or more Ballerina packages in to their own Docker images. When doing so there are few practices to adhere to.

This base image contains build triggers to make sure that crucial settings are applied in the child images. Because of this, the following values must be set as build arguments when building child images.

```
SVC_MODE=[true|false]
FILE_MODE=[true|false]
BUILD_DATE=[RFC3339 formatted date]
```

Out of these, `BUILD_DATE` is mandatory, as this affects the meta data of the child images. The value should be `RFC 3339` formatted value of the current date and time.

```
BUILD_DATE=2017-02-27T09:22:57Z
```

For example, in Bash the following would be a good way to parse the current time as a proper value for `BUILD_DATE`.

```bash
--build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
```

An example Dockerfile that makes use of this as the parent image would look like the following.

```Dockerfile
FROM ballerinalang/ballerina

COPY files/* /ballerina/files/
```

Notice that the only task the child Dockerfile does is to copy Ballerina packages to the standard location inside the image which is `/ballerina/files`. In this case, the Ballerina package will have to be a Ballerina `main`. For an image that packs a Ballerina Service archive, the Dockerfile would change to something like the following.

```Dockerfile
FROM ballerinalang/ballerina

ENV SVC_MODE=true

COPY files/* /ballerina/files/
```

