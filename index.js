import React, { Component } from 'react';
import {
  requireNativeComponent,
  ViewPropTypes
} from 'react-native';
import { bool, string, number, array, shape, arrayOf } from 'prop-types';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

class ImageSequence extends Component {
  render() {
    const {
      startFrameIndex,
      images,
      ...otherProps
    } = this.props;

    let normalized = images.map(resolveAssetSource);

    // reorder elements if start-index is different from 0 (beginning)
    if (startFrameIndex !== 0) {
      normalized = [...normalized.slice(startFrameIndex), ...normalized.slice(0, startFrameIndex)];
    }

    return (
      <RCTImageSequence
        {...otherProps}
        images={normalized} />
    );
  }
}

ImageSequence.defaultProps = {
  startFrameIndex: 0,
  framesPerSecond: 24,
  loop: true,
  downsampleWidth: -1,
  downsampleHeight: -1
};

ImageSequence.propTypes = {
  startFrameIndex: number,
  images: array.isRequired,
  framesPerSecond: number,
  loop: bool,
  downsampleWidth: number,
  downsampleHeight: number
};

const RCTImageSequence = requireNativeComponent('RCTImageSequence', {
  propTypes: {
    ...ViewPropTypes,
    images: arrayOf(shape({
      uri: string.isRequired
    })).isRequired,
    framesPerSecond: number,
    loop: bool,
    downsampleWidth: number,
    downsampleHeight: number
  },
});

export default ImageSequence;
