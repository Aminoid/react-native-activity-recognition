const { DeviceEventEmitter, NativeModules } = require('react-native')
const { ActivityRecognition } = NativeModules

ActivityRecognition.subscribe = subscribe

function subscribe(callback) {
  const subscription = DeviceEventEmitter.addListener('DetectedActivity', detectedActivities => {
    callback({
      ...detectedActivities,
      get sorted() {
        return Object.keys(detectedActivities)
          .map(type => ({ type: type, confidence: detectedActivities[type] }))
          .sort((a, b) => b.confidence - a.confidence)
      },
    })
  })
  return () => DeviceEventEmitter.removeSubscription(subscription)
}

module.exports = ActivityRecognition
