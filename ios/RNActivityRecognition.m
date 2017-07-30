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
    return @[@"ActivityDetection", @"ActivityFailure"];
}

- (void)activityManager
{
    if (_motionActivityManager == nil) {
        _motionActivityManager = [[CMMotionActivityManager alloc] init];
    }
    
    if ([CMMotionActivityManager isActivityAvailable]) {
        [self.motionActivityManager startActivityUpdatesToQueue: [NSOperationQueue mainQueue]
                                                    withHandler:^(CMMotionActivity *activity) {
                                                        _activityEvent = @{
                                                                @"confidence": @(activity.confidence),
                                                                @"stationary": @(activity.stationary),
                                                                @"walking": @(activity.walking),
                                                                @"running": @(activity.running),
                                                                @"automotive": @(activity.automotive),
                                                                @"cycling": @(activity.cycling),
                                                                @"unknown": @(activity.unknown),
                                                                };
                                                        [self sendEventWithName:@"ActivityDetection" body: _activityEvent];
                                                    }
         ];
    } else {
        RCTLogInfo(@"Activity is Not Available");
        [self sendEventWithName:@"ActivityFailure" body: @"This device does not support activity"];
    }
}

RCT_EXPORT_METHOD(echo)
{
    RCTLogInfo(@"Pretending to create Activity manager with Aossie");
}

RCT_EXPORT_METHOD(startActivity:(float) time)
{
    checkActivityConfig();
    _timeout = time/1000;
    RCTLogInfo(@"Starting Activity Detection");
    _timer = [NSTimer scheduledTimerWithTimeInterval: _timeout
                                              target:self selector:@selector(activityManager) userInfo:nil repeats:YES];
}

RCT_EXPORT_METHOD(stopActivity)
{
    RCTLogInfo(@"Stopping Activity Detection");
    [self.motionActivityManager stopActivityUpdates];
    [_timer invalidate];
}

static void checkActivityConfig()
{
#if RCT_DEV
    if (![[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSMotionUsageDescription"]) {
        RCTLogError(@"NSMotionUsageDescription key must be present in Info.plist to use Activity Manager.");
    }
#endif
}


@end
  
