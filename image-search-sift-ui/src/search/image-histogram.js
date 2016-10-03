import {inject, customElement, bindable} from "aurelia-framework";
import {Router} from "aurelia-router";

@customElement('image-histogram')
@inject(Element,Router)
export class ImageHistogramCustomElement {
    @bindable histogram;
    
    histogramChanged() {
        let length = this.histogram.length;
        this.values = [];
        for (let i = 0; i < length; i++) {
            let c = this.histogram.charAt(i);
            this.values.push(Math.floor(HEX_CHAR_MAP[c] * 100));
        }
    }
}

const HEX_CHAR_MAP = {
    '0' : 1/16.0,
    '1' : 2/16.0,
    '2' : 3/16.0,
    '3' : 4/16.0,
    '4' : 5/16.0,
    '5' : 6/16.0,
    '6' : 7/16.0,
    '7' : 8/16.0,
    '8' : 9/16.0,
    '9' : 10/16.0,
    'A' : 11/16.0,
    'B' : 12/16.0,
    'C' : 13/16.0,
    'D' : 14/16.0,
    'E' : 15/16.0,
    'F' : 16/16.0
};