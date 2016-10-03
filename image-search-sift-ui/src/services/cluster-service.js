import {HttpClient} from 'aurelia-fetch-client';
import {inject} from 'aurelia-framework';

@inject(HttpClient)
export default class ClusterService {
    constructor(http) {
        this.http = http;
    }

    getClusters(from) {
        console.log('Getting image clusters from ' + from ? from : 0);
        var url = 'api/clusters' + (from ? ('?from=' + from) : '');
        return this.http.fetch(url)
            .then((r) => r.json());
    }

    getCluster(lsh) {
        console.log('Getting image cluster ' + lsh);
        var url = 'api/clusters/cluster/' + lsh;
        return this.http.fetch(url)
            .then((r) => r.json());
    }
}