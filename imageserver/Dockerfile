FROM nodesource/node:4.0

ADD package.json package.json
RUN npm install

ADD app.js app.js
ADD image.js image.js
ADD images.js images.js
ADD config.js config.js

RUN mkdir /usr/src/app/images/

CMD ["node","app.js"]
