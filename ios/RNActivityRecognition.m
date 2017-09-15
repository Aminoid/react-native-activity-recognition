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
        RCTLogInfo(@"Activity is Not Available on this device.");
    }
}

RCT_EXPORT_METHOD(startActivity:(float)time callback:(RCTResponseSenderBlock)callback)
{
    NSString* errorMsg = checkActivityConfig(callback);
    
    if (errorMsg != nil) {
        RCTLogError(@"%@", errorMsg);
        callback(@[errorMsg]);
        return;
    }
    
    _timeout = time/1000;
    RCTLogInfo(@"Starting Activity Detection");
    _timer = [NSTimer scheduledTimerWithTimeInterval: _timeout
                                              target:self selector:@selector(activityManager) userInfo:nil repeats:YES];
    
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
#endif
    return nil;
}

@end
