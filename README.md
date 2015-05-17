<!---
/*
 *
 *
 *
 *
 * Reactive Android with type-safety.
 * http://reactoid.org
 *
 *
 *
 *
 *
 * Copyright 2015 Sung-Ho Lee and Reactoid team
 *
 * Sung-Ho Lee licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
-->

# Reactive Android with type-safety

Reactoid provides two-way data binding on Android, similar to what AngularJS or React does. For example, given this very short code below, text you typed in the `EditText` is synchronized with the title of the `Button` in real-time.

```scala
val txt = EditText("Hello").textVar
Button(txt() + ", world!")
```

Becuase Reactoid is based on [Scaloid](https://github.com/pocorall/scaloid) and [Scala.Rx](https://github.com/lihaoyi/scala.rx), you can take advantage of sophisticated asynchronous task management, better runtime performance, and type-safety. These are not easily achievable from javascript-based frameworks.

Reactoid is currently in the proof-of-concept stage. Everything can be changed.

## Demos

Go to the `sample` directory and just do `sbt run`

### Prerequisites

 * Sbt 0.13.7 or above
 * Android build tools 22.0.0 or above
 * Android API Level 22
   - Level 22 is required for building, while this app retains runtime compatibility from API Level 15. Please refer to `minSdkVersion` property in `AndroidManifest.xml`.
