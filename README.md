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
  style={{width: 50, height: 50}} />
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
