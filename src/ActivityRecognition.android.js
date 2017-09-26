const { DeviceEventEmitter, NativeModules } = require('react-native')
const { ActivityRecognition: ActivityRecognitionNative } = NativeModules

const ActivityRecognition = {
  'ANDROID_STILL': ActivityRecognitionNative.ANDROID_STILL,
  'ANDROID_WALKING': ActivityRecognitionNative.ANDROID_WALKING,
  'ANDROID_IN_VEHICLE': ActivityRecognitionNative.ANDROID_IN_VEHICLE,

  subscribe(callback) {
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
  },

  start(detectionIntervalMs) {
    return new Promise((resolve, reject) => {
      ActivityRecognitionNative.startWithCallback(detectionIntervalMs, resolve, logAndReject.bind(null, reject))
    });
  },

  startMocked(detectionIntervalMs, mockActivityType) {
    return new Promise((resolve, reject) => {
      ActivityRecognitionNative.startMockedWithCallback(detectionIntervalMs, mockActivityType, resolve, logAndReject.bind(null, reject))
    });
  },

  stopMocked() {
    return new Promise((resolve, reject) => {
      ActivityRecognitionNative.stopMockedWithCallback(resolve, logAndReject.bind(null, reject))
    });
  },

  stop() {
    return new Promise((resolve, reject) => {
      ActivityRecognitionNative.stopWithCallback(resolve, logAndReject.bind(null, reject))
    });
  },
}

function logAndReject(reject, errorMsg) {
  // Don't log this as an error, because the client should handle it using `catch`.
  console.log(`[ActivityRecognition] Error: ${errorMsg}`)
  reject(errorMsg)
}

module.exports = ActivityRecognition
