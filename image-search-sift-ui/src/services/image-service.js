import {HttpClient} from 'aurelia-fetch-client';
import {inject} from 'aurelia-framework';

@inject(HttpClient)
export default class ImageService {
    constructor(http) {
        this.http = http;
    }

    getListing(from,to) {
        console.log('Getting images from ' + from + ' to ' + to);
        var url = 'images/images?from=' + from + '&to=' + to;
        return this.http.fetch(url)
            .then((r) => r.json());
    }

    getSimilarImages(histogram) {
        console.log('Getting similar images for histogram ' + histogram);
        var url = 'api/search/histogram/' + histogram;
        return this.http.fetch(url)
            .then((r) => r.json());
    }
}