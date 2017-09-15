const { DeviceEventEmitter, NativeModules } = require('react-native')
const { ActivityRecognition } = NativeModules

ActivityRecognition.subscribe = subscribe
ActivityRecognition.start = start
ActivityRecognition.stop = stop

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

function start(detectionIntervalMs) {
  return new Promise((resolve, reject) => {
    ActivityRecognition.startWithCallback(detectionIntervalMs, resolve, logAndReject.bind(null, reject))
  });
}

function stop() {
  return new Promise((resolve, reject) => {
    ActivityRecognition.stopWithCallback(resolve, logAndReject.bind(null, reject))
  });
}

function logAndReject(reject, errorMsg) {
  console.error(`[ActivityRecognition] Error: ${errorMsg}`)
  reject(errorMsg)
}

module.exports = ActivityRecognition
