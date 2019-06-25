import { Component } from 'react';
import { ViewProps } from 'react-native';

interface ImageSequenceProps extends ViewProps {
    /** An array of source images. Each element of the array should be the result of a call to require(imagePath). */
    images: any[];
    /** Which index of the images array should the sequence start at. Default: 0 */
    startFrameIndex?: number;
    /** Playback speed of the image sequence. Default: 24 */
    framesPerSecond?: number;
    /** Should the sequence loop. Default: true */
    loop?: boolean;
    /** The width to use for optional downsampling. Both `downsampleWidth` and `downsampleHeight` must be set to a positive number to enable downsampling. Default: -1 */
    downsampleWidth?: number;
    /** The height to use for optional downsampling. Both `downsampleWidth` and `downsampleHeight` must be set to a positive number to enable downsampling. Default: -1 */
    downsampleHeight?: number;
}

declare class ImageSequence extends Component<ImageSequenceProps> {
}

export default ImageSequence;
