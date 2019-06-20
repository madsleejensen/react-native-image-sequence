//
// Created by Mads Lee Jensen on 07/07/16.
// Copyright (c) 2016 Facebook. All rights reserved.
//

#import "RCTImageSequenceView.h"

#import <UIKit/UIKit.h>
#import <CoreGraphics/CoreGraphics.h>

@implementation RCTImageSequenceView {
    NSUInteger _framesPerSecond;
    NSMutableDictionary *_activeTasks;
    NSMutableDictionary *_imagesLoaded;
    BOOL _loop;
    NSInteger _downsampleWidth;
    NSInteger _downsampleHeight;
}

- (void)setImages:(NSArray *)images {
    __weak RCTImageSequenceView *weakSelf = self;

    self.animationImages = nil;

    _activeTasks = [NSMutableDictionary new];
    _imagesLoaded = [NSMutableDictionary new];

    for (NSUInteger index = 0; index < images.count; index++) {
        NSDictionary *item = images[index];

#ifdef DEBUG
        NSString *urlString = item[@"uri"];
#else
        // when not in debug, the paths are "local paths" (because resources are bundled in app)
        NSString *urlString = [NSString stringWithFormat:@"file://%@", item[@"uri"]];
#endif

        dispatch_async(dispatch_queue_create("dk.mads-lee.ImageSequence.Downloader", NULL), ^{
            // Sleep for 1ms to make sure that all the props have been set properly before starting processing
            [NSThread sleepForTimeInterval:0.001];

            NSURL *url = [NSURL URLWithString:urlString];

            if (_downsampleWidth <= 0 || _downsampleHeight <= 0) {
                // Downsampling is not set so just return normally
                UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:url]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [weakSelf onImageLoadTaskAtIndex:index image:image];
                });
            } else {
                // Downsampling is set so we need to downsample
                UIImage *image = [weakSelf resizedImage:url width:_downsampleWidth height:_downsampleHeight];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [weakSelf onImageLoadTaskAtIndex:index image:image];
                });
            }
        });

        _activeTasks[@(index)] = urlString;
    }
}

- (UIImage *)resizedImage:(NSURL *)url width:(NSInteger)width height:(NSInteger)height {
    CFURLRef cfurl = (__bridge CFURLRef)url;
    CGImageSourceRef imageSourceRef = CGImageSourceCreateWithURL(cfurl, nil);
    if (!imageSourceRef) {
       return nil;
    }

    NSDictionary* d = @{
                        (id)kCGImageSourceShouldAllowFloat: (id)kCFBooleanTrue,
                        (id)kCGImageSourceCreateThumbnailWithTransform: (id)kCFBooleanTrue,
                        (id)kCGImageSourceCreateThumbnailFromImageAlways: (id)kCFBooleanTrue,
                        (id)kCGImageSourceThumbnailMaxPixelSize: @((int)(width > height ? width : height))
                        };
    CGImageRef imageRef = CGImageSourceCreateThumbnailAtIndex(imageSourceRef, 0, (__bridge CFDictionaryRef)d);
    CFRelease(imageSourceRef);
    if (!imageRef) {
       return nil;
    }

    UIImage* scaledImage = [UIImage imageWithCGImage:imageRef scale:1 orientation:UIImageOrientationUp];
    CFRelease(imageRef);

    return scaledImage;
}

- (void)onImageLoadTaskAtIndex:(NSUInteger)index image:(UIImage *)image {
    if (index == 0) {
        self.image = image;
    }

    [_activeTasks removeObjectForKey:@(index)];

    _imagesLoaded[@(index)] = image;

    if (_activeTasks.allValues.count == 0) {
        [self onImagesLoaded];
    }
}

- (void)onImagesLoaded {
    NSMutableArray *images = [NSMutableArray new];
    for (NSUInteger index = 0; index < _imagesLoaded.allValues.count; index++) {
        UIImage *image = _imagesLoaded[@(index)];
        [images addObject:image];
    }

    [_imagesLoaded removeAllObjects];

    self.image = nil;
    self.animationDuration = images.count * (1.0f / _framesPerSecond);
    self.animationImages = images;
    self.animationRepeatCount = _loop ? 0 : 1;
    [self startAnimating];
}

- (void)setFramesPerSecond:(NSUInteger)framesPerSecond {
    _framesPerSecond = framesPerSecond;

    if (self.animationImages.count > 0) {
        self.animationDuration = self.animationImages.count * (1.0f / _framesPerSecond);
    }
}

- (void)setLoop:(NSUInteger)loop {
    _loop = loop;

    self.animationRepeatCount = _loop ? 0 : 1;
}

- (void)setDownsampleWidth:(NSInteger)downsampleWidth {
    _downsampleWidth = downsampleWidth;
}

- (void)setDownsampleHeight:(NSInteger)downsampleHeight {
    _downsampleHeight = downsampleHeight;
}

@end
