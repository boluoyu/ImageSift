# Image Sift

Image Sift allows a user to index a set of images by similarity.  It can then be quickly queried using another image to find similar images in the index.

Image Sift analyzes the colour fingerprint of an image to detect similarity.  It works well under scaling, rotation, and resizing as well as small
modifications such as watermarks, etc.  It does NOT perform well under cropping.

## Prerequisites

A system with docker/docker-compose for running as well as node/npm and Java8 for development purposes.

## Development

To build the backend containers, navigate to `containers/local` and execute `docker-compose up`.  If this is your first time running the Elasticsearch
 container, you'll need to import the template by running `containers/local/template.sh`.  If you've indexed images, but the search isn't returning anything, 
 it's most likely because you forgot this step!

To run the webserver, navigate to the root directory and run `./gradlew run`.  Alternatively, you can set up a Spring Boot debug profile in IntelliJ if you're
using an IDE.  If you're using IntelliJ, make sure you `Enable Annotation Processing` from the preferences as Image Sift uses Project Lombok for annotations.

To build the UI, navigate to `image-search-sift-ui` and run `npm install`.  To serve the front-end, run `npm start`.  Navigate to `http://localhost:9000`.

## Indexing Images

The simplest way to index images is to copy them into the `containers/local/images` directory and then run `./gradle reindex`.  This will re-index all 
images in that directory.

Alternatively, you can index images by URL by making post requests to the server:

`curl -H "Content-Type: application/json" -X POST -d '{"url":"http://some.web/image.jpg"}' http://localhost:8080/index`

If the index was successful, you will see the document as it is stored in Elasticsearch.

## Searching

To do a reverse image search, use the UI to explore your dataset or make POST requests to the webserver:

1) Search via URL:

`curl -H "Content-Type: application/json" -X POST -d '{"url":"http://some.web/image.jpg"}' http://localhost:8080/search`

2) Search via File:

`curl -i -X POST -H "Content-Type: multipart/form-data" -F "image=@/path/to/image.jpg" http://localhost:8080/search/file`



    
