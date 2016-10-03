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

var router = require('express').Router();
var fs = require('fs');
var config = require('./config');

/* GET home page. */
router.get('/', function(req, res, next) {
  var from = parseInt(req.query.from ? req.query.from : 0);
  var to = parseInt(req.query.to ? req.query.to : 10);


  var results = [];
  fs.readdir(config.FILE_PATH, function (err, list) {

    if (!list || list.length == 0) {
      res.json({
        files: [],
        total: 0
      });
    } else {
      for (var i = from; i < to; i++) {
        if (i < list.length) {
          results.push(list[i]);
        }
      }

      res.json({
        files: results,
        total: list.length
      });
    }
  });
});

router.get('/all', function(req, res, next) {
  fs.readdir(config.FILE_PATH, function (err, list) {

    if (!list || list.length == 0) {
      res.json({
        files: [],
        total: 0
      });
    } else {
      res.json({
        files: list,
        total: list.length
      });
    }
  });
});

module.exports = router;
