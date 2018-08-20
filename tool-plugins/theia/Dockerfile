FROM node:8
RUN useradd --create-home theia
WORKDIR /home/theia
RUN rm -rf /opt/yarn && rm -f /usr/local/bin/yarn && rm -f /usr/local/bin/yarnpkg
RUN apt-get update && apt-get install -y npm && npm install -g yarn@1.3.2
USER theia
COPY --chown=theia:theia . composer
RUN cd composer && \
    yarn && \
    rm -rf /home/theia/composer/resources/lib && \
    rm /home/theia/composer/resources/ballerina.tmLanguage
EXPOSE 3000
WORKDIR /home/theia/composer/browser-app
ENV SHELL /bin/bash
CMD yarn run start --hostname 0.0.0.0