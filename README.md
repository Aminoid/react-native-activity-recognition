# react-native-activity-recognition

[![npm version][npm shield]][npm url]

React Native wrapper for the [Android Activity Recognition API][1] and [CMMotionActivity][3]. It attempts to determine the user activity such as
driving, walking, running and cycling. Possible detected activities for android are [listed here][2] and for iOS are [listed here][3].

[1]: https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
[2]: https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity
[3]: https://developer.apple.com/reference/coremotion/cmmotionactivity
[4]: https://facebook.github.io/react-native/docs/linking-libraries-ios.html#manual-linking

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

## Linking

### Automatic

`react-native link react-native-activity-recognition`

> **IMPORTANT NOTE:** You'll need to follow Step 4 for both iOS and Android of manual-linking

### Manual

Make alterations to the following files in your project:

#### Android

1. Add following lines to `android/settings.gradle`
```gradle
...
include ':react-native-activity-recognition'
project(':react-native-activity-recognition').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-activity-recognition/android')
...
```

2. Add the compile line to dependencies in `android/app/build.gradle`
```gradle
...
dependencies {
    ...
    compile project(':react-native-activity-recognition')
    ...
}
```

3. Add import and link the package in `android/app/src/.../MainApplication.java`
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

4. Add activityrecognition service in `android/app/src/main/AndroidManifest.xml`
```xml
...
<application ...>
    ...
    <service android:name="com.xebia.activityrecognition.DetectionService"/>
    ...
</application>
...
```

#### iOS

1. In the XCode's "Project navigator", right click on your project's Libraries folder ➜ `Add Files to <...>`
2. Go to `node_modules` ➜ `react-native-activity-recognition` ➜ `ios` ➜ select `RNActivityRecognition.xcodeproj`
3. Add `RNActivityRecognition.a` to `Build Phases -> Link Binary With Libraries`
4. Add `NSMotionUsageDescription` key to your `Info.plist` with strings describing why your app needs this permission


## Usage

```js
import ActivityRecognition from 'react-native-activity-recognition'

...

// Subscribe to updates
this.unsubscribe = ActivityRecognition.subscribe(detectedActivities => {
  const mostProbableActivity = detectedActivities.sorted[0]
})

...

// Start activity detection
const detectionIntervalMillis = 1000
ActivityRecognition.start(detectionIntervalMillis)

...

// Stop activity detection and remove the listener
ActivityRecognition.stop()
this.unsubscribe()
```

### Android

`detectedActivities` is an object with keys for each detected activity, each of which have an integer percentage (0-100) indicating the likelihood that the user is performing this activity. For example:

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

Because the activities are sorted by confidence level, the first value will be the one with the highest probability
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

### iOS

`detectedActivities` is an object with key to the detected activity with a confidence value for that activity given by CMMotionActivityManager. For example:
```js
{
    WALKING: 2
}
```

`detectedActivities.sorted` getter will return it in the form of an array.
```js
[
    {type: "WALKING", confidence: 2}
]
```

The following activity types are supported:

- RUNNING
- WALKING
- STATIONARY
- AUTOMOTIVE
- CYCLING
- UNKNOWN

## Credits / prior art

The following projects were very helpful in developing this library:

- https://github.com/googlesamples/android-play-location
- https://bitbucket.org/timhagn/react-native-google-locations
- https://github.com/facebook/react-native/blob/master/Libraries/Geolocation
