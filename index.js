const { DeviceEventEmitter, NativeModules } = require('react-native');
const { ActivityRecognition } = NativeModules;

ActivityRecognition.subscribe = function subscribe(callback) {
  const subscription = DeviceEventEmitter.addListener('DetectedActivity', detectedActivities => {
    const sortedActivities = Object.keys(detectedActivities)
      .map(type => ({ type: type, confidence: detectedActivities[type] }))
      .sort((a, b) => b.confidence - a.confidence)
    callback(sortedActivities)
  })
  return () => DeviceEventEmitter.removeSubscription(subscription)
}

module.exports = ActivityRecognition
