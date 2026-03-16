# Errors Example

1. Example Error Response If a user tries to fetch a non-existent customer:

```json
{
  "status": 404,
  "message": "Customer not found with id: 999",
  "timestamp": "2026-03-16T10:00:00Z",
  "path": "/api/customers/999"
}
```

1. If a user sends an invalid request (e.g., missing name):

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-03-16T10:00:00Z",
  "path": "/api/customers",
  "errors": {
    "name": "Name is required"
  }
}
```
