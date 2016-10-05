import React from 'react';
import { View, requireNativeComponent, DeviceEventEmitter } from 'react-native';
import resolveAssetSource from 'react-native/Libraries/Image/resolveAssetSource'

class ImageSequence extends React.Component {
  componentWillMount() {
    DeviceEventEmitter.addListener('onLoadStart', (event: Event) => {
      if (this.props.onLoadStart) {
        this.props.onLoadStart(event)
      }
    })
    DeviceEventEmitter.addListener('onLoadComplete', (event: Event) => {
      if (this.props.onLoadComplete) {
        this.props.onLoadComplete(event)
      }
    })
    DeviceEventEmitter.addListener('onError', (event: Event) => {
      if (this.props.onError) {
        this.props.onError(event)
      }
    })
  }
  render() {
    let normalized = this.props.images.map(resolveAssetSource)

    // reorder elements if start-index is different from 0 (beginning)
    if (this.props.startFrameIndex !== 0) {
      normalized = [...normalized.slice(this.props.startFrameIndex), ...normalized.slice(0, this.props.startFrameIndex)]
    }

    return (
      <RCTImageSequence
        {...this.props}
        images={normalized} />
    )
  }
}

ImageSequence.propTypes = {
  startFrameIndex: React.PropTypes.number,
  images: React.PropTypes.array.isRequired,
  sampleSize: React.PropTypes.number,
  framesPerSecond: React.PropTypes.number,
  start: React.PropTypes.bool,
  oneShot: React.PropTypes.bool,
  onLoadStart: React.PropTypes.func,
  onLoadComplete: React.PropTypes.func,
  onError: React.PropTypes.func
}

ImageSequence.defaultProps = {
  startFrameIndex: 0,
  sampleSize: 1,
  framesPerSecond: 24,
  start: true,
  oneShot: false
}

const RCTImageSequence = requireNativeComponent('RCTImageSequence', {
  propTypes: {
    ...View.propTypes,
    images: React.PropTypes.arrayOf(React.PropTypes.shape({
      uri: React.PropTypes.string.isRequired
    })).isRequired,
    sampleSize: React.PropTypes.number,
    framesPerSecond: React.PropTypes.number,
    start: React.PropTypes.bool,
    oneShot: React.PropTypes.bool,
    onLoadStart: React.PropTypes.func,
    onLoadComplete: React.PropTypes.func,
    onError: React.PropTypes.func
  },
})

export default ImageSequence
