# react-native-image-sequence

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
