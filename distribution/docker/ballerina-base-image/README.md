# Ballerina Base Docker Image

This Dockerfile can be used to create a Ballerina base Docker distribution that can be used to run Ballerina archives and package Ballerina archives as distributable Docker images.

## Usage
The Docker distribution for Ballerina is available on Docker Hub as `ballerinalang/ballerina`. To run a Ballerina package using the Ballerina Docker image, simply mount the folder containing the package(s) to `/ballerina/files` folder inside the container. 

```bash
# Create a directory and copy the packages needed to be run.
mkdir -p ~/ballerina/packages/
cp mypackage.balx ~/ballerina/packages/
chmod +r ~/ballerina/packages/mypackage.balx

# Run container with the volume mount
docker run -v ~/ballerina/packages:/ballerina/files -it ballerinalang/ballerina
```

If you are running a Ballerina Service,

```bash
cp mysvcpackage.balx ~/ballerina/packages/
chmod +r ~/ballerina/packages/mysvcpackage.balx

docker run -v /tmp/testb:/ballerina/files -p 9090:9090 -it ballerinalang/ballerina
```

## Building the image
```bash
# bash build.sh -d <ballerina-dist> to build ballerina:latest
cp ~/Downloads/ballerina-0.970.0-alpha1-SNAPSHOT.zip .
bash build.sh -d ballerina-0.970.0-alpha1-SNAPSHOT.zip

# Or build without copying the distribution.
bash build.sh -d ~/Downloads/ballerina-0.970.0-alpha1-SNAPSHOT.zip

# Show usage
bash build.sh -h
```
The above command packs the Ballerina Runtime in to the Docker image.
## As a Parent Image
This base image can be used by child images to pack one or more Ballerina packages in to their own Docker images. When doing so there are few practices to adhere to.

This base image contains build triggers to make sure that crucial settings are applied in the child images. Because of this, the following values must be set as build arguments when building child images.

```
BUILD_DATE=[RFC3339 formatted date]
```

`BUILD_DATE` is mandatory, as this affects the meta data of the child images. The value should be `RFC 3339` formatted value of the current date and time.

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

Notice that the only task the child Dockerfile does is to copy Ballerina packages to the standard location inside the image which is `/ballerina/files`.

## Updating DockerHub

> The following instructions are for the `ballerinalang/container-support` developers.

After each Ballerina runtime release, the base image in the Public Docker Hub should be updated. There should be a new tag for the release done and the `latest` tag should be updated to point to the latest release. This should be done for both [`ballerinalang`](https://hub.docker.com/r/ballerinalang/ballerina/) and [`ballerina`](https://hub.docker.com/r/ballerina/ballerina/) organizations.

1. **OPTIONAL:** Copy the official Ballerina Runtime release to the Dockerfile context.
```bash
$ cd ballerinalang/container-support/ballerina-base-image
$ cp ~/Downloads/ballerina-0.970.0-alpha1-SNAPSHOT.zip .
```

2. Build the Docker image with an organization prefix for `ballerinalang`, and a version tag of the Ballerina runtime release version.

```bash
$ bash build.sh -d ~/dev/ballerina/ballerina-0.970.0-alpha1-SNAPSHOT.zip -v 0.970.0-alpha1-SNAPSHOT -o ballerinalang
```

3. This will build a Docker image with name `ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT` in the local Docker repository. The `latest` tag for `ballerinalang/ballerina` should now also point to this image now.
```bash
$ docker tag ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT ballerinalang/ballerina:latest
```

4. The same image should also be pushed to `ballerina` organization. For this, re-creating the image is not necessary. Instead, another set of tags can be pointed to the same image.
```bash
$ docker tag ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT ballerina/ballerina:0.970.0-alpha1-SNAPSHOT
$ docker tag ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT ballerina/ballerina:latest
```

5. If the Docker images are listed, it should show an output similar to the following. Notice that the `Image ID` column contains the same value for all four tags. This is because they all point to the same AUFS layer.
```
REPOSITORY                                        TAG                                 IMAGE ID            CREATED             SIZE
ballerinalang/ballerina                           0.970.0-alpha1-SNAPSHOT             4daa1c1f2089        1 minute ago        142MB
ballerinalang/ballerina                           latest                              4daa1c1f2089        1 minute ago        142MB
ballerina/ballerina                               0.970.0-alpha1-SNAPSHOT             4daa1c1f2089        1 minute ago        142MB
ballerina/ballerina                               latest                              4daa1c1f2089        1 minute ago        142MB
```

6. Push the above Docker images to the Public Docker Hub. Observe how only the first push command actually pushes any data. The rest of the push commands only push the tag details, since the image is already pushed once.
```bash
$ docker push ballerinalang/ballerina:0.970.0-alpha1-SNAPSHOT
$ docker push ballerinalang/ballerina:latest
$ docker push ballerina/ballerina:0.970.0-alpha1-SNAPSHOT
$ docker push ballerina/ballerina:latest
```