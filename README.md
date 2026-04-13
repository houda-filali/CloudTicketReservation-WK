# CloudTicketReservation-WhoKnows
Cloud-based Ticket Reservation Application
The objective of this project was to build a cloud-based ticket reservation Android application using Firebase. The app allows customers to browse available events, reserve tickets to events and receive notifications regarding their reserved tickets, while allowing admin users to create, edit and cancel events.  
On top of building the app, the team developed several types of tests in order to maintain the integrity of the code and ensure its reliability to all users.

## Team
Team Name: Who Knows  
Team Members:  
Hiba Maifi – 40289223  
Houda Filali – 40276607  
Hiba Talbi – 40278717  
Lilia Messaoudi – 40252419  
Elif Sag Sesen – 40283343  

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
