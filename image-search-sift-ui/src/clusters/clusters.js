import {inject} from 'aurelia-framework';
import {activationStrategy, Router} from 'aurelia-router';
import ClusterService from '../services/cluster-service';

@inject(Router,ClusterService)
export class ClustersView {
  constructor(router, clusterService) {
    this.router = router;
    this.clusterService = clusterService;
  }

  determineActivationStrategy() {
    return activationStrategy.invokeLifecycle;
  }

  activate(params) {
    this.from = params.from ? parseInt(params.from) : 0;
    this.cluster = params.cluster;
    this._fetch();
  }

  _fetch() {
    this.response = null;
    if (this.cluster) {
      this.clusterService.getCluster(this.cluster)
          .then((response) => this.response = response);
    } else {
      this.clusterService.getClusters(this.from)
          .then((response) => this.response = response);
    }
  }

  get hasMore() {
    return this.response && this.response.hasMore;
  }

  get hasLess() {
    return !(this.from == 0);
  }

  next() {
    this.router.navigateToRoute('clusters', {
      from : this.from+10
    });
  }

  prev() {
    this.router.navigateToRoute('clusters', {
      from : Math.max(0,this.from-10)
    });
  }
}