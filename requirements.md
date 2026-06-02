# EnergyMap Requirements

## Product Idea

EnergyMap is a personal daily timeline app for tracking activities and energy. It lets the user record what they do throughout the day and separately log quick energy check-ins. Over time, the app should help the user understand how energy changes across the day and around different activities.

## Core Concepts

### Energy Check-In

A point-in-time snapshot of the user's current energy.

- Energy level on a simple scale, preferably 1-5 for the first version
- Automatic timestamp using the current time
- Optional note
- User should be able to edit or delete a check-in

Suggested scale:

- 1: Exhausted
- 2: Low
- 3: Neutral
- 4: Good
- 5: Energized

### Activity Log

A duration-based record of what the user did during the day.

- Activity title/name
- Start time
- End time, or marked as currently ongoing
- Optional note
- User should be able to edit or delete an activity
- User should be able to manually add past activities they forgot to log

Use the broader term "Activity" instead of only "Task", because entries may include work, meals, breaks, commute, rest, exercise, chores, or social time.

## First-Pass Features

- Quick action to log current energy
- Quick action to start an activity
- Ability to end the current activity
- Ability to manually add an activity with start and end time
- Today view showing a combined timeline of energy check-ins and activities
- Local-only data storage
- Basic edit and delete support for both energy check-ins and activities

## Timeline Behavior

The main view should show the user's day in chronological order, combining both streams.

Example:

```text
8:30 AM    Energy 3/5
9:00 AM    Started: Reading
9:45 AM    Ended: Reading
10:00 AM   Started: Coding
10:30 AM   Energy 4/5
11:15 AM   Ended: Coding
11:30 AM   Started: Meeting
12:00 PM   Energy 2/5
12:15 PM   Ended: Meeting
```

## Data Model

### EnergyEntry

- id
- timestamp
- energyLevel
- note

### ActivityEntry

- id
- title
- startTime
- endTime
- note
- isOngoing

## Out Of Scope For First Pass

- Accounts or cloud sync
- Advanced analytics
- Calendar integration
- Notifications or reminders
- AI summaries
- Complex categories or tagging
- Social/sharing features

## First Milestone

Build a local-only Android app where the user can track today's activities and log energy check-ins, then view both together in a simple daily timeline.
