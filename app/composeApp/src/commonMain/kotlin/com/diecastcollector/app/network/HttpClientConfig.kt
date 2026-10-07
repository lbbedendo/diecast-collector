package com.diecastcollector.app.network

// 10.0.2.2 is the Android emulator's alias for the host machine, so this reaches a local
// `./gradlew bootRun` on the host out of the box. Point this at a real deployed API URL
// (and drop the debug-only cleartext allowance in the Android manifest) before shipping.
const val API_BASE_URL = "http://10.0.2.2:8080"
