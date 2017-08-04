import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNActivityRecognition } = NativeModules;

const emitter = new NativeEventEmitter(RNActivityRecognition);
var subscription = null;

var ActivityRecognition = {
    start: function(time: number) {
        RNActivityRecognition.startActivity(time);
    },
    subscribe: function(success: Function) {
        subscription = emitter.addListener(
            "ActivityDetection",
            activity => {
                success({
                    ...activity,
                    get sorted() {
                        return Object.keys(activity)
                            .map(type => ({ type: type, confidence: activity[type] }))
                    }
                })
            }
        );
        return () => subscription.remove();
    },
    stop: function() {
        RNActivityRecognition.stopActivity();
    }
}

module.exports = ActivityRecognition;
