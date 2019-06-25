# react-native-image-sequence
native modules for handling image sequence animations. (created because i had performance issues with a javascript only solution like: https://github.com/remobile/react-native-image-animation)

its a simple wrapper around **iOS** `UIImageView.animationImages` and **Android** `AnimationDrawable`

## Installation

1. `npm i --save react-native-image-sequence`
2. `react-native link react-native-image-sequence`


## Examples

```javascript
import ImageSequence from 'react-native-image-sequence';

const images = [
  require('1.jpg'),
  require('2.jpg'),
  require('3.jpg'),
  require('4.jpg'),
  require('5.jpg'),
];

const centerIndex = Math.round(images.length / 2);

<ImageSequence
  images={images}
  startFrameIndex={centerIndex}
  style={{width: 50, height: 50}}
/>
```

### Change animation speed
You can change the speed of the animation by setting the `framesPerSecond` property.

```javascript
<ImageSequence
  images={images}
  framesPerSecond={24}
/>
```

### Looping
You can change if animation loops indefinitely by setting the `loop` property.

```javascript
<ImageSequence
  images={images}
  framesPerSecond={24}
  loop={false}
/>
```

### Downsampling
Loading and using an image with a higher resolution than the size of the image display area does not provide any visible benefit, but still takes up precious memory and incurs additional performance overhead due to additional on the fly scaling. So choosing to downsample an image before rendering saves memory and CPU time during the rendering process, but costs more CPU time during the image loading process.

You can set the images to be downsampled by setting both the `downsampleWidth` and `downsampleHeight` properties. Both properties must be set to positive numbers to enable downsampling.

```javascript
<ImageSequence
  images={images}
  downsampleWidth={32}
  downsampleHeight={32}
/>
```

IMPORTANT: The final image width and height will not necessarily match `downsampleWidth` and `downsampleHeight` but will just be a target for the per-platform logic on how to downsample.

On Android, the logic for how to downsample is taken from [here](https://developer.android.com/topic/performance/graphics/load-bitmap). The image's aspect ratio will stay consistent after downsampling.

On iOS, the max value of `downsampleWidth` and `downsampleHeight` will be used as the max pixel count for both dimensions in the final image. The image's aspect ratio will stay consistent after downsampling.
