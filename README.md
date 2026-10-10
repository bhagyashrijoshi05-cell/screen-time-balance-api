# screen-time-balance-api

A Spring Boot REST API to track daily screen time and hobbies (reading, outdoor play, games) for each family member, with age-based screen limits.

Built to explore code review with Codzee.

## Tech

Java 17, Spring Boot 3, Spring Data JPA, H2 (in-memory), Maven

## Run

```
mvn spring-boot:run
```

The app starts on http://localhost:8080

## Run tests

```
mvn test
```

## API

| Method | URL | What it does |
|--------|-----|--------------|
| POST | `/api/persons` | Add a family member |
| GET | `/api/persons` | List family members |
| GET | `/api/persons/{id}` | Get one member |
| POST | `/api/persons/{id}/activities` | Log an activity |
| GET | `/api/persons/{id}/activities?date=2026-10-12` | List a member's activities for a day |

### Example

Add a member:

```
curl -X POST localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name":"Aarav","ageGroup":"CHILD"}'
```

Log screen time:

```
curl -X POST localhost:8080/api/persons/1/activities \
  -H "Content-Type: application/json" \
  -d '{"type":"SCREEN","minutes":150,"date":"2026-10-12"}'
```

If the daily screen limit is crossed, the response includes a `warning`.

Daily screen limits: CHILD 120 min, ADULT 240 min, SENIOR 180 min.

## Planned improvements

- Weekly summary per member
- Stronger input validation
- Move limit rules into a service layer
- Authentication, PostgreSQL, notifications
