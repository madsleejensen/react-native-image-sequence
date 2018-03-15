//
// Created by Mads Lee Jensen on 07/07/16.
// Copyright (c) 2016 Facebook. All rights reserved.
//

#import "RCTImageSequenceManager.h"
#import "RCTImageSequenceView.h"

@implementation RCTImageSequenceManager {
}

RCT_EXPORT_MODULE();
RCT_EXPORT_VIEW_PROPERTY(images, NSArray);
RCT_EXPORT_VIEW_PROPERTY(framesPerSecond, NSUInteger);
RCT_EXPORT_VIEW_PROPERTY(loop, BOOL);

- (UIView *)view {
    return [RCTImageSequenceView new];
}

@end
