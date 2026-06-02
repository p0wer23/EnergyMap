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
- editable and deletable

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
