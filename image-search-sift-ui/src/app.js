export class App {
  configureRouter(config, router) {
    config.title = 'Sift ImageSearch';
    config.map([
      { route: ['', 'images'], name: 'images', moduleId: './images/images',      nav: true, title: 'Images' },
      { route: ['search'], name: 'search', moduleId: './search/search',      nav: true, title: 'Search' },
      { route: ['clusters'], name: 'clusters', moduleId: './clusters/clusters',      nav: true, title: 'Cluster' }
    ]);

    this.router = router;
  }
}
