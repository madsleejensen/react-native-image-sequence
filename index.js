import React, { Component } from 'react';
import {
  View,
  requireNativeComponent
} from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource';

class ImageSequence extends Component {
  render() {
    let normalized = this.props.images.map(resolveAssetSource);

    // reorder elements if start-index is different from 0 (beginning)
    if (this.props.startFrameIndex !== 0) {
      normalized = [...normalized.slice(this.props.startFrameIndex), ...normalized.slice(0, this.props.startFrameIndex)];
    }

    return (
      <RCTImageSequence
        {...this.props}
        images={normalized} />
    );
  }
}

ImageSequence.defaultProps = {
  startFrameIndex: 0,
  framesPerSecond: 24,
  start: true,
  oneShot: false
};

ImageSequence.propTypes = {
  startFrameIndex: React.PropTypes.number,
  images: React.PropTypes.array.isRequired,
  framesPerSecond: React.PropTypes.number,
  start: React.PropTypes.bool,
  oneShot: React.PropTypes.bool
};

const RCTImageSequence = requireNativeComponent('RCTImageSequence', {
  propTypes: {
    ...View.propTypes,
    images: React.PropTypes.arrayOf(React.PropTypes.shape({
      uri: React.PropTypes.string.isRequired
    })).isRequired,
    framesPerSecond: React.PropTypes.number,
    start: React.PropTypes.bool,
    oneShot: React.PropTypes.bool
  },
});

export default ImageSequence;
