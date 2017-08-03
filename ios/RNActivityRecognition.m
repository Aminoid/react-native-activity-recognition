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

RCT_EXPORT_METHOD(echo)
{
    RCTLogInfo(@"The code is strong on this side.");
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
  
