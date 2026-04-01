### Upgrade CalendarScreen UI

Implement a three-screen flow for schedule management with the following screens:
1. **Day List**: Display a list of events for the selected day.
2. **Event Detail**: Show details of a selected event.
3. **Event Edit/New**: A form for editing existing events or creating new ones.

### Features to Implement

- **CRUD Operations**: 
  - Create, Read, Update, Delete operations on events using EventRepository. 
  - Implement soft delete for events.
- **ObserveInRange**: Update the list query to observe events within the selected day range.
- **Persistent Storage**: Replace the existing InMemoryEventRepository with a persistent repository that supports iOS and JVM using JSON files and kotlinx.serialization. 
- **Platform-Specific Implementations**: Use expect/actual declarations for file path providers across platforms. 
- **Keep Android Implementation**: Maintain the existing RoomEventRepository for Android.
- **Validation**: Add basic validation to ensure title is non-empty and end date is greater than or equal to start date.

### Modular Updates
- Update `shared/composeApp` modules and any relevant Gradle libraries.
- Ensure `rememberEventRepository()` is wired correctly for `iosMain` and `jvmMain` to utilize the new persistent repository.