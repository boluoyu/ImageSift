#!/usr/bin/env bash

ES_ADDRESS="http://localhost:9200"
ES_TEMPLATE="image_similarity"

curl -XDELETE $ES_ADDRESS/_template/$ES_TEMPLATE 
curl -XPUT $ES_ADDRESS/_template/$ES_TEMPLATE -d @$ES_TEMPLATE.json
