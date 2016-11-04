# react-native-activity-recognition

[![npm version][npm shield]][npm url]

React Native wrapper for the [Android Activity Recognition API][1]. It attempts to determine the user activity such as
driving, walking, running and cycling. Possible detected activities are [listed here][2].

Right now only Android devices are supported, but iOS support could be added using [CMMotionActivity][3].
I would love to see a pull request that adds iOS support.

[1]: https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
[2]: https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity
[3]: https://developer.apple.com/reference/coremotion/cmmotionactivity

[npm shield]: https://img.shields.io/npm/v/react-native-activity-recognition.svg
[npm url]: https://www.npmjs.com/package/react-native-activity-recognition

## Installation

```bash
npm i -S react-native-activity-recognition
```

or with Yarn:

```bash
yarn add react-native-activity-recognition
```

### Linking

Make alterations to the following files in your project:

#### `android/settings.gradle`

```gradle
...
include ':react-native-activity-recognition'
project(':react-native-activity-recognition').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-activity-recognition/android')
...
```

#### `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':react-native-activity-recognition')
    ...
}
```

#### `android/app/src/.../MainApplication.java`

```java
import com.xebia.activityrecognition.RNActivityRecognitionPackage;  // <--- add import

public class MainApplication extends Application implements ReactApplication {
    // ...
    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            // ...
            new RNActivityRecognitionPackage()                      // <--- add package
        );
    }
```

#### `android/app/src/main/AndroidManifest.xml`

```xml
...
<application ...>
    ...
    <service android:name="com.xebia.activityrecognition.DetectionService"/>
    ...
</application>
...
```

## Usage

```js
import ActivityRecognition from 'react-native-activity-recognition'

...

// Start activity detection
const detectionIntervalMillis = 1000
ActivityRecognition.start(detectionIntervalMillis)

// Subscribe to updates
this.unsubscribe = ActivityRecognition.subscribe(detectedActivities => {
  const mostProbableActivity = detectedActivities.sorted[0]
})

...

// Stop activity detection and remove the listener
ActivityRecognition.stop()
this.unsubscribe()
```

`detectedActivities` is an object with keys for each detected activity, each of which have an integer percentage (0-100)
indicating the likelihood that the user is performing this activity. For example:

```js
{
  ON_FOOT: 8,
  IN_VEHICLE: 15,
  WALKING: 8,
  STILL: 77
}
```

Additionally, the `detectedActivities.sorted` getter is provided which returns an array of activities, ordered by their
confidence value:

```js
[
  { type: 'STILL', confidence: 77 },
  { type: 'IN_VEHICLE', confidence: 15 },
  { type: 'ON_FOOT', confidence: 8 },
  { type: 'WALKING', confidence: 8 },
]
```

Because the activities are sorted by confidence level, the first value will be the one with the highest probability.
Note that ON_FOOT and WALKING are related but won't always have the same value. I have never seen WALKING with a higher
confidence than ON_FOOT, but it may happen that WALKING comes before ON_FOOT in the array if they have the same value.

The following activity types are supported:

- IN_VEHICLE
- ON_BICYCLE
- ON_FOOT
- RUNNING
- WALKING
- STILL
- TILTING
- UNKNOWN

## Methods

### `start(detectionIntervalMillis: number): void`
Starts listening for activity updates. The detectionIntervalMillis is passed to ActivityRecognitionApi.requestActivityUpdates().

### `subscribe(callback: Function): Function`
Subscribes a callback function to be invoked on each activity update. Returns a function which can be called in order to unsubscribe.
The update callback will be invoked with the detectedActivities object.

### `stop(): void`
Stops listening for activity updates.

## Credits / prior art

The following projects were very helpful in developing this library:

- https://github.com/googlesamples/android-play-location
- https://bitbucket.org/timhagn/react-native-google-locations
