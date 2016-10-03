/*
 * Copyright 2016 Uncharted Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var express = require('express');
var router = express.Router();
var path = require('path');
var fs = require('fs');
var http = require('http');
var https = require('https');

var config = require('./config');
var crypto = require('crypto');
var readChunk = require('read-chunk'); // npm install read-chunk
var imageType = require('image-type');

var sendFile = function(filePath,res) {
  var buffer = readChunk.sync(filePath, 0, 12);
  var imgType = imageType(buffer);
  var stat = fs.statSync(filePath);
  res.setHeader('Content-Type',imgType.mime);
  res.setHeader('Content-Length',stat.size);
  res.sendFile(path.resolve(filePath));
};

/* GET home page. */
router.get('/:file', function(req, res, next) {

  // Check if the file is an absolute file
  var filePath = config.FILE_PATH + '/' + req.params.file;
  var url = req.params.file;
  var hash = crypto.createHash('md5').update(url).digest('hex');
  var urlPath = config.FILE_PATH + '/' + hash;

  // If it's a file
  if (fs.existsSync(filePath)) {
    sendFile(filePath,res);
  } else if (fs.existsSync(urlPath)) {
    sendFile(urlPath,res);
  } else {
    res.sendStatus(404);
  }
});

/** PUT a url **/
router.post('/add/:url', function(req, res, next) {
  var url = req.params.url;
  var hash = crypto.createHash('md5').update(url).digest('hex');
  var urlPath = config.FILE_PATH + '/' + hash;

  var file = fs.createWriteStream(path.resolve(urlPath));
  var module = url.indexOf('https') == 0 ? https : http;
  module.get(url, function(response) {
    response.pipe(file);
    res.send(hash);
  });
});

module.exports = router;
