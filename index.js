const { DeviceEventEmitter, NativeModules } = require('react-native');
const { ActivityRecognition } = NativeModules;

ActivityRecognition.subscribe = function subscribe(callback) {
  const subscription = DeviceEventEmitter.addListener('DetectedActivity', activities => callback(activities))
  return () => DeviceEventEmitter.removeSubscription(subscription)
}

ActivityRecognition.getMostProbableActivity = function getMostProbableActivity(detectedActivities) {
  const mostProbableType = Object.keys(detectedActivities).reduce((acc, type) => {
    return detectedActivities[acc] > detectedActivities[type] ? acc : type
  })
  return {
    type: mostProbableType,
    confidence: detectedActivities[mostProbableType]),
  }
}

module.exports = ActivityRecognition
