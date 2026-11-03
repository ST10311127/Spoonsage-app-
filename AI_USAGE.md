# AI Usage Disclosure

Throughout development of SpoonSage, I used Claude (Anthropic) as a coding assistant for specific implementation tasks, debugging, and architectural decisions. Below is an honest account of where and how it was used.

## 1. Migrating the AI Coach from the Anthropic API to Gemini

The original AI Coach feature called the Anthropic API directly through a custom `ClaudeApi.kt` client, which required a paid API key — not practical for a student project with no budget. I asked Claude to suggest a free alternative and to implement the switch.

Claude recommended Google Gemini via Firebase AI Logic, since I was already planning to add Firebase for authentication and data storage, and its free tier needs no billing setup. It then:
- Deleted `ClaudeApi.kt` and the associated Retrofit client
- Created `GeminiCoachClient.kt`, calling `gemini-2.5-flash` through the Firebase AI Logic Gemini Developer API backend
- Updated `RecipeRepository.sendCoachMessage()` to use the new client

I reviewed each change, tested the coach chat manually to confirm responses came through correctly, and adjusted prompt wording myself where the tone didn't match the rest of the app.

## 2. Wiring up Firebase Authentication and Firestore

I asked Claude to help replace my local, on-device authentication (a DataStore-based system with a manually SHA-256-hashed password) with real Firebase Authentication and Firestore for cloud data sync. Claude:
- Rewrote `AuthViewModel` to use Firebase email/password sign-up and login, plus anonymous guest sign-in
- Created `FirestoreUserRepository.kt` to push and pull user profile data (goals, diet, dislikes) to/from `users/{uid}`
- Removed the manual password-hashing code, since Firebase now handles that server-side
- Produced a `SETUP_FIREBASE.md` guide for the console steps it couldn't do itself (creating the project, enabling Auth providers, downloading `google-services.json`)

I did the actual Firebase console setup myself, since it required my own Google account, and verified in the Firestore console that user data was landing correctly after sign-up.

## 3. Fixing the GitHub Actions build (missing Gradle wrapper)

My first CI run failed with `chmod: cannot access 'gradlew': No such file or directory`. I pasted the error to Claude, which correctly diagnosed that the Gradle wrapper files hadn't been included when I uploaded the project to GitHub via the browser (rather than git). Since I couldn't locate the wrapper files locally, Claude regenerated `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` (with a matching `gradle-wrapper.properties`) as a downloadable package, which I then committed to the repo root. This unblocked the build.

## My role vs. Claude's role

Claude wrote first-draft code and diagnosed CI errors from logs I provided; I made the underlying product decisions (which features to keep, tone of the coach, when to use guest vs. authenticated flow), tested every change on-device, did all Firebase/GitHub console configuration myself, and I'm responsible for understanding and being able to explain the resulting code.

*(Word count: ~430)*
