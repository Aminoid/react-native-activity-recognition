import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNActivityRecognition } = NativeModules;

const emitter = new NativeEventEmitter(RNActivityRecognition);
var subscription = null;

var ActivityRecognition = {
    test: function() {
        RNActivityRecognition.echo()
    },
    start: function(time: number) {
        RNActivityRecognition.startActivity(time);
    },
    subscribe: function(success: Function) {
        subscription = emitter.addListener(
            "ActivityDetection",
            success
        );
    },
    stop: function() {
        RNActivityRecognition.stopActivity();
        subscription = null;
    }
}

module.exports = ActivityRecognition;
