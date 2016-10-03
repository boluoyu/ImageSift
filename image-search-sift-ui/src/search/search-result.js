import {inject, customElement,bindable} from 'aurelia-framework';
import {Router} from 'aurelia-router';

@customElement('search-result')
@inject(Element,Router)
export class SearchResultCustomElement {
    @bindable image;
}