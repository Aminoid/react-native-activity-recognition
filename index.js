const { DeviceEventEmitter, NativeModules } = require('react-native')
const { ActivityRecognition } = NativeModules

ActivityRecognition.subscribe = subscribe

function subscribe(callback) {
  const subscription = DeviceEventEmitter.addListener('DetectedActivity', detectedActivities => {
    Object.defineProperty(detectedActivities, 'sorted', {
      get: () => Object.keys(detectedActivities)
        .map(type => ({ type: type, confidence: detectedActivities[type] }))
        .sort((a, b) => b.confidence - a.confidence),
    })
    callback(detectedActivities)
  })
  return () => DeviceEventEmitter.removeSubscription(subscription)
}

module.exports = ActivityRecognition
