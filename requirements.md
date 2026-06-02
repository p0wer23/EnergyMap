# EnergyMap Requirements

## Product

EnergyMap is a local-only Android app for tracking:

- current energy as a point-in-time check-in
- activities done throughout the day as time-based entries

## Core Records

### Energy Check-In

- energy score from `1-10`
- timestamp
- optional note
- multiple timestamped check-ins allowed per day
- editable and deletable
- v1 edit flow changes level and note, not timestamp
- add flow starts from the most recent recorded score, or `5` if none exists
- check-in list defaults to `Today` with a `Today` / `Previous` selector
- list rows show score-only circles, timestamp, and expandable note previews

Suggested labels:

- `1-2`: exhausted
- `3-4`: low
- `5-6`: neutral
- `7-8`: good
- `9-10`: high

### Activity Log

- activity title
- start time
- end time or ongoing state
- optional note
- editable and deletable

Use `Activity` as the product term, not `Task`.

## V1 Scope

- quick energy check-in
- start activity
- end current activity
- manual activity entry with start/end time
- combined daily timeline for energy and activities
- local storage only
- basic edit/delete for both record types

## Current Setup State

- setup build currently shows a minimal verification screen with `app running...`
- app features above are still the intended v1 scope

## Data Model

### `EnergyEntry`

- `id`
- `timestamp`
- `energyLevel`
- `note`

### `ActivityEntry`

- `id`
- `title`
- `startTime`
- `endTime`
- `note`
- `isOngoing`

## Out Of Scope

- accounts
- sync
- reminders
- analytics
- AI summaries
- social features
