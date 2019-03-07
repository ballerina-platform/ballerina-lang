FROM openjdk:8-jre

ARG ARG_GITHUB_TOKEN

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash -
RUN curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
RUN apt-get update -qq && apt-get install -qq --no-install-recommends \
  nodejs \
  yarn \
  build-essential \
  python-dev \
  && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home theia
WORKDIR /home/theia

COPY ./resources resources
ENV THEIA_DEFAULT_PLUGINS="local-dir:///home/theia/resources/vsix"
RUN dpkg -i "/home/theia/resources/ballerina-linux-installer.deb"

USER theia

COPY --chown=theia:theia ./browser-app composer
RUN cd composer \
    && export GITHUB_TOKEN=$ARG_GITHUB_TOKEN \
    && echo using token $GITHUB_TOKEN \
    && yarn

EXPOSE 3000

WORKDIR /home/theia/composer
ENV SHELL /bin/bash
CMD yarn start --hostname 0.0.0.0