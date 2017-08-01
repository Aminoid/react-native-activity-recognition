#import <React/RCTEventEmitter.h>
#import <CoreMotion/CoreMotion.h>


@interface RNActivityRecognition : RCTEventEmitter <RCTBridgeModule>
@property(nonatomic, strong) CMMotionActivityManager *motionActivityManager;
@end
  
