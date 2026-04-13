# CloudTicketReservation-WhoKnows
Cloud-based Ticket Reservation Application

## Running the app
Run the Java class `MainActivity`

## Running the tests

### Unit Tests
In terminal: `./gradlew testDebugUnitTest`

### Integration Tests
To run integration tests, a Firestore emulator needs to be running alongside the tests. This is how to run said emulator:
1. `curl -sL https://firebase.tools | bash` (Download Firebase tools)
2. `firebase login` (Enter credentials to Firebase account)
3. `firebase emulators:start` (Run emulator on terminal)

On another terminal:
1. ` ./gradlew connectedAndroidTest` OR
2. Run each test class separately.

### Instrumentation Tests

### E2E / System Tests
