# react-native-activity-recognition

React Native wrapper for the [Android Activity Recognition API][1]. It attempts to determine the user activity such as
driving, walking, running and cycling. Possible detected activities are [listed here][2].

[1]: https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
[2]: https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity

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
project(':react-native-activity-recognition').projectDir = new File(settingsDir, '../node_modules/react-native-activity-recognition/android')
```

#### `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':react-native-activity-recognition')
}
```

#### `android/.../MainActivity.java`

```java
import com.xebia.activityrecognition.RNActivityRecognitionPackage;  // <--- add import

public class MainActivity extends ReactActivity {
    // ...
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),                                     // <--- add comma
        new RNActivityRecognitionPackage()                          // <--- add package
      );
    }
```

## Usage

### Imports

```js
import { DeviceEventEmitter } from 'react-native'
import { ActivityRecognition } from 'NativeModules'
```


### Accelerometer
```js
ActivityRecognition.start(100)
DeviceEventEmitter.addListener('DetectedActivity', ({ type, confidence }) => { ... })
ActivityRecognition.stop()
```
