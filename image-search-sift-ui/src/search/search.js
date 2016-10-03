import {inject} from 'aurelia-framework';
import {activationStrategy, Router} from 'aurelia-router';
import ImageService from '../services/image-service';
import Dropzone from 'dropzone';
import $ from 'jquery';

@inject(Element,Router,ImageService)
export class ImagesView {
  constructor(element,router,imageService) {
    this.element = element;
    this.router = router;
    this.imageService = imageService;

    this.images = [];
    this.response = null;
  }

  activate(params) {
    this.uri = params.uri;
    this.histogram = params.histogram;
    if (this.histogram) {
      this.imageService.getSimilarImages(this.histogram)
          .then((r) => {
            this.response = r;
            if (r && r.images) {
              this.images = r.images;
            }
          });
    }
  }

  _onDrop() {
    $(this.element).find('.dz-preview').empty();

    this.images = [];
    this.histogram = null;
    this.uri = null;
  }

  _onSuccess(file,response,e) {
    if (response && response.histogram) {
      this.router.navigateToRoute('search',{
        histogram : response.histogram,
        uri : response.dataURI
      });
    }
  }

  attached() {
    // Create dropzone
    let dropzoneElement = $(this.element).find('.dropzoneDiv')[0];
    let self = this;
    new Dropzone(dropzoneElement,
        {
          url: '/api/image/histogram',
          paramName: 'image',
          init : function() {
            this.on('success', self._onSuccess.bind(self));
            this.on('drop', self._onDrop.bind(self));
          },
          previewTemplate:'<div class="dz-preview dz-file-preview"><img data-dz-thumbnail /> </div>'
        }
    )

    if (this.uri) {
      $(this.element).find('.dz-preview').remove();
      $(this.element).find('.dropzoneDiv').append('<div class="dz-preview">')
      $('<img>')
          .addClass('data-dz-thumbnail')
          .width(120)
          .height(120)
          .attr('src',this.uri)
          .appendTo($(this.element).find('.dz-preview'));
    }
  }

  determineActivationStrategy() {
    return activationStrategy.invokeLifecycle;
  }
}