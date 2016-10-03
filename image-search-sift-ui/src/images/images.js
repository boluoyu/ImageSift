import {inject} from 'aurelia-framework';
import {activationStrategy, Router} from 'aurelia-router';
import ImageService from '../services/image-service';

@inject(Router,ImageService)
export class ImagesView {
  constructor(router, imageService) {
    this.router = router;
    this.imageService = imageService;
    this.images = [];

    this.RESULTS_PER_PAGE = 10;
    this.from = 0;
    this.to = this.RESULTS_PER_PAGE;
    this.response = null;
  }

  determineActivationStrategy() {
    return activationStrategy.invokeLifecycle;
  }

  activate(params) {
    if (params.from) {
      this.from = parseInt(params.from);
      this.to = this.from + this.RESULTS_PER_PAGE;
    }
    this.getImages();
  }

  getImages() {
    this.imageService.getListing(this.from,this.to)
        .then((response) => {
          if (response && response.files) {
            this.response = response;
            this.images = response.files.map((filename) => {
              return {
                filename : filename,
                url : '/images/image/' + filename
              };
            })
          }
        });
  }

  get hasMore() {
    //return this.response && this.response.files > this.to + this.RESULTS_PER_PAGE;
    return true;
  }

  get hasLess() {
    return !(this.from == 0);
  }

  next() {
    this.router.navigateToRoute('images', {
      from : this.from+this.RESULTS_PER_PAGE
    });
  }

  prev() {
    this.router.navigateToRoute('images', {
      from : this.from-this.RESULTS_PER_PAGE
    });
  }
}