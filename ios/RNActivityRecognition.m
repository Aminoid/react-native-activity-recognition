#import "RNActivityRecognition.h"
#import <React/RCTLog.h>

@implementation RNActivityRecognition
{
    NSTimer * _timer;
    float _timeout;
    NSDictionary<NSString *, id> * _activityEvent;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_MODULE()

float _timeout = 1.0;

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"ActivityDetection"];
}

- (NSString *)generateAct: (CMMotionActivity *) activity {
    if (activity.stationary) {
        return @"STATIONARY";
    }
    
    if (activity.walking) {
        return @"WALKING";
    }
    
    if (activity.running) {
        return @"RUNNING";
    }
    
    if (activity.automotive) {
        return @"AUTOMOTIVE";
    }
    
    if (activity.cycling) {
        return @"CYCLING";
    }
    
    return @"UNKNOWN";
}

- (NSDictionary *)constantsToExport
{
    // Export a few common activity types to allow easier mocking.
    return @{
             @"IOS_STATIONARY": @"STATIONARY",
             @"IOS_WALKING": @"WALKING",
             @"IOS_AUTOMOTIVE": @"AUTOMOTIVE",
             };
}

- (void)activityManager
{
    if (_motionActivityManager == nil) {
        _motionActivityManager = [[CMMotionActivityManager alloc] init];
    }
    
    if ([CMMotionActivityManager isActivityAvailable]) {
        [self.motionActivityManager startActivityUpdatesToQueue: [NSOperationQueue mainQueue]
                                                    withHandler:^(CMMotionActivity *activity) {
                                                        NSString *act;
                                                        act = [self generateAct:activity];
                                                        _activityEvent = @{
                                                            act: @(activity.confidence)
                                                        };
                                                        [self sendEventWithName:@"ActivityDetection" body: _activityEvent];
                                                    }
         ];
    } else {
        RCTLogInfo(@"Motion data is not available on this device.");
    }
}

- (void)mockActivityManager:(NSTimer *)timer
{
    // Receive the data.
    NSString* mockActivity = timer.userInfo;
    
    if (mockActivity == nil) mockActivity = @"UNKNOWN";
    
    dispatch_async(dispatch_get_main_queue(), ^{
        _activityEvent = @{ mockActivity: @100 };
        [self sendEventWithName:@"ActivityDetection" body: _activityEvent];
    });
}

RCT_EXPORT_METHOD(startActivity:(float)time callback:(RCTResponseSenderBlock)callback)
{
    NSString* errorMsg = checkActivityConfig(callback);
    
    if (errorMsg != nil) {
        NSLog(@"Error: %@", errorMsg);
        callback(@[errorMsg]);
        return;
    }
    
    _timeout = time/1000;
    RCTLogInfo(@"Starting Activity Detection");
    _timer = [NSTimer scheduledTimerWithTimeInterval: _timeout
                                              target:self selector:@selector(activityManager) userInfo:nil repeats:YES];
    
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(startMockedActivity:(float)time mockActivity:(NSString*)mockActivity callback:(RCTResponseSenderBlock)callback)
{
    _timeout = time/1000;
    RCTLogInfo(@"Starting Mock Activity Detection");
    _timer = [NSTimer scheduledTimerWithTimeInterval:_timeout
                                              target:self selector:@selector(mockActivityManager:) userInfo:mockActivity repeats:YES];
    
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(stopMockedActivity:(RCTResponseSenderBlock)callback)
{
    RCTLogInfo(@"Stopping Mock Activity Detection");
    [_timer invalidate];
    
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(stopActivity:(RCTResponseSenderBlock)callback)
{
    RCTLogInfo(@"Stopping Activity Detection");
    [self.motionActivityManager stopActivityUpdates];
    [_timer invalidate];
    
    callback(@[[NSNull null]]);
}

static NSString* checkActivityConfig()
{
#if RCT_DEV
    if (![[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSMotionUsageDescription"]) {
        return @"NSMotionUsageDescription key must be present in Info.plist to use Activity Manager.";
    }
    if (![CMMotionActivityManager isActivityAvailable]) {
        return @"Motion data is not available on this device.";
    }
#endif
    return nil;
}

@end
