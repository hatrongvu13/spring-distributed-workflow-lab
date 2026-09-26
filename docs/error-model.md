# Error and retry model

Classify errors before retry:
- `TRANSIENT`: timeout, connection reset, 429, selected 5xx. Retry with exponential backoff and jitter.
- `PERMANENT`: validation, unsupported request, authentication/authorization. Fail immediately.
- `BUSINESS`: insufficient balance, rejected policy. Usually compensate, do not blindly retry.
- `DEADLINE_EXCEEDED`: stop new work and compensate completed steps.
- `POISON_MESSAGE`: unreadable schema or invariant violation. Send to DLQ and alert.

Do not let consumers requeue forever. Set a finite delivery policy, record attempts, publish stable error codes, retain the original event ID, and alert on terminal failure or DLQ growth.
