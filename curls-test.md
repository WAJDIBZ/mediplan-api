# curl Tests for HTTP Endpoints

This document lists all known HTTP endpoints exposed by this Spring Boot application, along with ready-to-run `curl` commands and example responses. The API base URL is assumed to be `https://mediplan-api-1b2c88de81dd.herokuapp.com`. All routes except those under `/api/auth/**` require a valid JWT access token in the `Authorization: Bearer <token>` header; admin-only routes additionally require an `ADMIN` role.

## Auth Endpoints

### [POST] /api/auth/register/patient

**Description:** Register a new patient account and obtain JWT tokens.

**Consumes:** application/json  
**Produces:** application/json

**Headers:**
- `Content-Type`: application/json
- `Accept`: application/json

**Request body:**
```json
{
  "fullName": "Claire Martin",
  "avatarUrl": "https://example.com/avatar.jpg",
  "email": "claire.martin@example.com",
  "password": "motdepasseFort123",
  "phone": "+33612345678",
  "address": {
    "line1": "123 rue de la Santé",
    "line2": "Appartement 4B",
    "city": "Paris",
    "country": "France",
    "zip": "75013"
  },
  "dateOfBirth": "1990-05-14",
  "gender": "FEMALE",
  "insuranceNumber": "INS-123456789",
  "emergencyContact": {
    "name": "Jean Dupont",
    "phone": "+33123456789",
    "relation": "Conjoint"
  }
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/register/patient" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "fullName": "Claire Martin",
    "email": "claire.martin@example.com",
    "password": "motdepasseFort123",
    "phone": "+33612345678",
    "dateOfBirth": "1990-05-14",
    "gender": "FEMALE"
  }'
```

Expected response (example):
Status code: 200 OK

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "role": "PATIENT"
}
```

### [POST] /api/auth/register/doctor

**Description:** Register a new doctor account and obtain JWT tokens.

**Consumes:** application/json  
**Produces:** application/json

**Headers:**
- `Content-Type`: application/json
- `Accept`: application/json

**Request body:**
```json
{
  "fullName": "Dr Antoine Leroy",
  "email": "antoine.leroy@example.com",
  "password": "MotDePasseSolide1",
  "phone": "+33111222333",
  "clinicAddress": {
    "line1": "10 rue des Lilas",
    "city": "Lyon",
    "country": "France",
    "zip": "69001"
  },
  "specialty": "Cardiologie",
  "licenseNumber": "LIC-987654",
  "yearsOfExperience": 12,
  "clinicName": "Clinique du Parc",
  "address": {
    "line1": "45 avenue Centrale",
    "city": "Lyon",
    "country": "France",
    "zip": "69002"
  },
  "avatarUrl": "https://cdn.example.com/avatars/doctor.png"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/register/doctor" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "fullName": "Dr Antoine Leroy",
    "email": "antoine.leroy@example.com",
    "password": "MotDePasseSolide1",
    "specialty": "Cardiologie",
    "licenseNumber": "LIC-987654",
    "yearsOfExperience": 12
  }'
```

Expected response (example):
Status code: 200 OK

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "role": "MEDECIN"
}
```

### [POST] /api/auth/login

**Description:** Authenticate an existing user with email and password.

**Consumes:** application/json  
**Produces:** application/json

**Headers:**
- `Content-Type`: application/json
- `Accept`: application/json

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "MotDePasse123"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/login" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"email": "user@example.com", "password": "MotDePasse123"}'
```

Expected response (example):
Status code: 200 OK

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "role": "PATIENT"
}
```

### [POST] /api/auth/refresh

**Description:** Refresh expired/expiring tokens using a refresh token query parameter.

**Consumes:** application/json  
**Produces:** application/json

**Query parameters:**
- `token` (string, required) – refresh token value

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/refresh?token=eyJhbGciOi..." \
  -H "Accept: application/json"
```

Expected response (example):
Status code: 200 OK

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "role": "PATIENT"
}
```

### [GET] /api/auth/me

**Description:** Lightweight authentication check. Returns `200 OK` when the JWT is valid.

**Consumes:** application/json  
**Produces:** application/json (empty body)

**Headers:**
- `Authorization`: Bearer <token>

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/me" \
  -H "Authorization: Bearer <YOUR_TOKEN_HERE>"
```

Expected response: Status code 200 OK with no body.

## Admin User Management Endpoints

_All endpoints require `Authorization: Bearer <token>` with an `ADMIN` role._

### [GET] /api/admin/users

**Description:** List users with optional filters and pagination.

**Consumes:** application/json  
**Produces:** application/json

**Query parameters:**
- `q` (string, optional) – free-text search
- `role` (enum Role, optional) – filter by role
- `active` (boolean, optional) – filter by active status
- `provider` (string, optional) – authentication provider
- `page` (int, optional, default 0)
- `size` (int, optional, default 20)
- `sort` (string, optional) – property,direction (allowed: createdAt, fullName, email, role, active, provider)

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users?page=0&size=10&sort=createdAt,desc" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "content": [
    {
      "id": "usr_1",
      "fullName": "Admin Example",
      "email": "admin@example.com",
      "role": "ADMIN",
      "active": true,
      "provider": "local",
      "createdAt": "2024-01-05T10:15:00Z"
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":10},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {"empty": false,"sorted": true,"unsorted": false},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### [GET] /api/admin/users/{id}

**Description:** Retrieve detailed information about a user.

**Produces:** application/json

**Path parameters:**
- `id` (string, required) – user identifier

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_1" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "id": "usr_1",
  "fullName": "Dr Alice Becker",
  "email": "alice.becker@example.com",
  "role": "MEDECIN",
  "active": true,
  "emailVerified": true,
  "phone": "+33123456789",
  "avatarUrl": "https://cdn.example.com/avatar.png",
  "provider": "local",
  "providerId": null,
  "createdAt": "2024-01-10T12:00:00Z",
  "updatedAt": "2024-02-01T09:00:00Z",
  "dateOfBirth": "1985-03-21",
  "gender": "FEMALE",
  "insuranceNumber": "INS-555222",
  "emergencyContact": {
    "name": "Jean Becker",
    "phone": "+33600001111",
    "relation": "Conjoint"
  },
  "address": {
    "line1": "12 rue Victor Hugo",
    "line2": null,
    "city": "Paris",
    "country": "France",
    "zip": "75008"
  },
  "specialty": "Cardiologie",
  "licenseNumber": "LIC-987654",
  "yearsOfExperience": 12,
  "clinicName": "Clinique du Parc",
  "clinicAddress": {
    "line1": "45 avenue Centrale",
    "line2": null,
    "city": "Lyon",
    "country": "France",
    "zip": "69002"
  }
}
```

### [POST] /api/admin/users

**Description:** Create a new user (patient, doctor, or admin).

**Consumes:** application/json  
**Produces:** application/json

**Headers:**
- `Content-Type`: application/json
- `Authorization`: Bearer <token>

**Request body:**
```json
{
  "fullName": "New Patient",
  "email": "new.patient@example.com",
  "password": "Password123",
  "role": "PATIENT",
  "phone": "+33611223344",
  "address": {
    "line1": "8 rue des Fleurs",
    "city": "Nice",
    "country": "France",
    "zip": "06000"
  },
  "insuranceNumber": "INS-101010",
  "emergencyContact": {
    "name": "Marie Patient",
    "phone": "+33699887766",
    "relation": "Soeur"
  }
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{
    "fullName": "New Patient",
    "email": "new.patient@example.com",
    "password": "Password123",
    "role": "PATIENT"
  }'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "usr_99",
  "fullName": "New Patient",
  "email": "new.patient@example.com",
  "role": "PATIENT",
  "active": true,
  "emailVerified": false,
  "phone": "+33611223344",
  "avatarUrl": null,
  "provider": "local",
  "providerId": null,
  "createdAt": "2024-06-01T08:00:00Z",
  "updatedAt": "2024-06-01T08:00:00Z",
  "dateOfBirth": null,
  "gender": null,
  "insuranceNumber": "INS-101010",
  "emergencyContact": {
    "name": "Marie Patient",
    "phone": "+33699887766",
    "relation": "Soeur"
  },
  "address": {
    "line1": "8 rue des Fleurs",
    "line2": null,
    "city": "Nice",
    "country": "France",
    "zip": "06000"
  },
  "specialty": null,
  "licenseNumber": null,
  "yearsOfExperience": null,
  "clinicName": null,
  "clinicAddress": null
}
```

### [PATCH] /api/admin/users/{id}

**Description:** Partially update user details.

**Consumes:** application/json  
**Produces:** application/json

**Path parameters:**
- `id` (string, required)

**Request body:**
```json
{
  "fullName": "Updated Name",
  "phone": "+33644556677",
  "clinicName": "Nouvelle Clinique"
}
```

curl example:

```bash
curl -X PATCH "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_99" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"fullName": "Updated Name", "phone": "+33644556677"}'
```

Expected response (example):
Status code: 200 OK

```json
{
  "id": "usr_99",
  "fullName": "Updated Name",
  "email": "new.patient@example.com",
  "role": "PATIENT",
  "active": true,
  "emailVerified": false,
  "phone": "+33644556677",
  "avatarUrl": null,
  "provider": "local",
  "providerId": null,
  "createdAt": "2024-06-01T08:00:00Z",
  "updatedAt": "2024-06-02T09:30:00Z",
  "dateOfBirth": null,
  "gender": null,
  "insuranceNumber": "INS-101010",
  "emergencyContact": {
    "name": "Marie Patient",
    "phone": "+33699887766",
    "relation": "Soeur"
  },
  "address": {
    "line1": "8 rue des Fleurs",
    "line2": null,
    "city": "Nice",
    "country": "France",
    "zip": "06000"
  },
  "specialty": null,
  "licenseNumber": null,
  "yearsOfExperience": null,
  "clinicName": "Nouvelle Clinique",
  "clinicAddress": null
}
```

### [DELETE] /api/admin/users/{id}

**Description:** Delete a user (soft delete by default).

**Produces:** application/json (empty)

**Path parameters:**
- `id` (string, required)

**Query parameters:**
- `hard` (boolean, optional, default false) – if true, perform hard delete

curl example:

```bash
curl -X DELETE "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_99?hard=false" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/{id}/deactivate

**Description:** Deactivate a user account.

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_42/deactivate" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/{id}/reactivate

**Description:** Reactivate a previously deactivated user.

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_42/reactivate" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/{id}/role

**Description:** Change a user's role (additional doctor fields optional unless promoting to MEDECIN).

**Consumes:** application/json  
**Produces:** application/json

**Request body:**
```json
{
  "role": "MEDECIN",
  "specialty": "Dermatologie",
  "licenseNumber": "LIC-202020",
  "yearsOfExperience": 5,
  "clinicName": "Centre Soin",
  "clinicAddress": {
    "line1": "15 rue Lumière",
    "city": "Marseille",
    "country": "France",
    "zip": "13001"
  }
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_42/role" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"role": "MEDECIN", "specialty": "Dermatologie", "licenseNumber": "LIC-202020"}'
```

Expected response (example):
Status code: 200 OK

```json
{
  "id": "usr_42",
  "fullName": "Utilisateur Exemple",
  "email": "user42@example.com",
  "role": "MEDECIN",
  "active": true,
  "emailVerified": true,
  "phone": "+33711112222",
  "avatarUrl": null,
  "provider": "local",
  "providerId": null,
  "createdAt": "2024-02-10T10:00:00Z",
  "updatedAt": "2024-03-01T10:00:00Z",
  "dateOfBirth": null,
  "gender": null,
  "insuranceNumber": null,
  "emergencyContact": null,
  "address": null,
  "specialty": "Dermatologie",
  "licenseNumber": "LIC-202020",
  "yearsOfExperience": 5,
  "clinicName": "Centre Soin",
  "clinicAddress": {
    "line1": "15 rue Lumière",
    "line2": null,
    "city": "Marseille",
    "country": "France",
    "zip": "13001"
  }
}
```

### [POST] /api/admin/users/{id}/reset-password

**Description:** Force reset a user's password.

**Request body:**
```json
{
  "newPassword": "NewStrongPwd123"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/usr_42/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"newPassword": "NewStrongPwd123"}'
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/bulk/deactivate

**Description:** Deactivate multiple users in bulk.

**Request body:**
```json
{
  "ids": ["usr_1", "usr_2", "usr_3"]
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/bulk/deactivate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"ids": ["usr_1", "usr_2"]}'
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/bulk/reactivate

**Description:** Reactivate multiple users in bulk.

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/bulk/reactivate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"ids": ["usr_1", "usr_2"]}'
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/bulk/delete

**Description:** Delete multiple users in bulk.

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/bulk/delete" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d '{"ids": ["usr_9", "usr_10"]}'
```

Expected response: Status code 204 No Content.

### [POST] /api/admin/users/export

**Description:** Export users to CSV with optional filters.

**Produces:** text/csv

**Query parameters:** same as list endpoint (`q`, `role`, `active`, `provider`, `sort`).

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/export?role=PATIENT" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -H "Accept: text/csv"
```

Expected response (example):
Status code: 200 OK

```
periodeDebut,periodeFin,totalRendezVous,planifies,confirmes,annules,honores,patientsActifs,medecinsActifs
"2024-01-01T00:00:00Z","2024-12-31T23:59:59Z",120,80,25,10,5,200,50
```

## Doctor Availability (Disponibilités) Endpoints

### [POST] /api/medecins/{medecinId}/disponibilites

**Description:** Create availability slots for a doctor.

**Consumes:** application/json  
**Produces:** application/json

**Headers:** `Authorization: Bearer <token>` (MEDECIN owner or ADMIN)

**Path parameters:**
- `medecinId` (string, required)

**Request body:**
```json
{
  "medecinId": "med_1",
  "date": "2024-06-15",
  "heureDebut": "09:00:00",
  "heureFin": "12:00:00",
  "recurrence": "AUCUNE",
  "commentaire": "Matinée consultations"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins/med_1/disponibilites" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"date": "2024-06-15", "heureDebut": "09:00:00", "heureFin": "12:00:00"}'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "disp_1",
  "medecinId": "med_1",
  "date": "2024-06-15",
  "heureDebut": "09:00:00",
  "heureFin": "12:00:00",
  "actif": true,
  "recurrence": "AUCUNE",
  "commentaire": "Matinée consultations",
  "createdAt": "2024-06-01T07:00:00Z",
  "updatedAt": "2024-06-01T07:00:00Z"
}
```

### [GET] /api/medecins/{medecinId}/disponibilites

**Description:** List availabilities for a doctor within an optional date range.

**Query parameters:**
- `from` (date, optional, ISO date)
- `to` (date, optional, ISO date)

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins/med_1/disponibilites?from=2024-06-01&to=2024-06-30" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
[
  {
    "id": "disp_1",
    "medecinId": "med_1",
    "date": "2024-06-15",
    "heureDebut": "09:00:00",
    "heureFin": "12:00:00",
    "actif": true,
    "recurrence": "AUCUNE",
    "commentaire": "Matinée consultations",
    "createdAt": "2024-06-01T07:00:00Z",
    "updatedAt": "2024-06-01T07:00:00Z"
  }
]
```

### [PUT] /api/medecins/{medecinId}/disponibilites/{id}

**Description:** Update an availability slot.

**Path parameters:**
- `medecinId` (string, required)
- `id` (string, required) – availability id

**Request body:**
```json
{
  "medecinId": "med_1",
  "date": "2024-06-16",
  "heureDebut": "10:00:00",
  "heureFin": "13:00:00",
  "recurrence": "HEBDOMADAIRE",
  "commentaire": "Créneau modifié"
}
```

curl example:

```bash
curl -X PUT "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins/med_1/disponibilites/disp_1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"date": "2024-06-16", "heureDebut": "10:00:00", "heureFin": "13:00:00", "recurrence": "HEBDOMADAIRE"}'
```

Expected response (example):
Status code: 200 OK

```json
{
  "id": "disp_1",
  "medecinId": "med_1",
  "date": "2024-06-16",
  "heureDebut": "10:00:00",
  "heureFin": "13:00:00",
  "actif": true,
  "recurrence": "HEBDOMADAIRE",
  "commentaire": "Créneau modifié",
  "createdAt": "2024-06-01T07:00:00Z",
  "updatedAt": "2024-06-05T09:00:00Z"
}
```

### [DELETE] /api/medecins/{medecinId}/disponibilites/{id}

**Description:** Delete an availability slot.

curl example:

```bash
curl -X DELETE "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins/med_1/disponibilites/disp_1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response: Status code 204 No Content.

## Appointment (Rendez-vous) Endpoints

### [POST] /api/rdv

**Description:** Create a new appointment.

**Consumes:** application/json  
**Produces:** application/json

**Request body:**
```json
{
  "medecinId": "med_1",
  "patientId": "pat_1",
  "debut": "2024-06-20T09:00:00Z",
  "fin": "2024-06-20T09:30:00Z",
  "motif": "Consultation annuelle",
  "notesPrivees": "Prévoir résultats sanguins"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"medecinId": "med_1", "patientId": "pat_1", "debut": "2024-06-20T09:00:00Z", "fin": "2024-06-20T09:30:00Z"}'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "rdv_1",
  "medecinId": "med_1",
  "patientId": "pat_1",
  "medecin": {
    "id": "med_1",
    "fullName": "Dr Antoine Leroy",
    "email": "antoine.leroy@example.com",
    "phone": "+33111222333",
    "avatarUrl": "https://cdn.example.com/avatars/doctor.png",
    "specialty": "Cardiologie"
  },
  "patient": {
    "id": "pat_1",
    "fullName": "Claire Martin",
    "email": "claire.martin@example.com",
    "phone": "+33612345678",
    "avatarUrl": null,
    "specialty": null
  },
  "debut": "2024-06-20T09:00:00Z",
  "fin": "2024-06-20T09:30:00Z",
  "statut": "PLANIFIE",
  "motif": "Consultation annuelle",
  "notesPrivees": "Prévoir résultats sanguins",
  "createurId": "med_1",
  "createdAt": "2024-06-01T08:00:00Z",
  "updatedAt": "2024-06-01T08:00:00Z"
}
```

### [PUT] /api/rdv/{id}

**Description:** Update an appointment's details.

**Path parameters:**
- `id` (string, required)

curl example:

```bash
curl -X PUT "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv/rdv_1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"medecinId": "med_1", "patientId": "pat_1", "debut": "2024-06-20T10:00:00Z", "fin": "2024-06-20T10:30:00Z"}'
```

Expected response: 200 OK with an updated `RendezVousResponse` similar to the creation example.

### [PATCH] /api/rdv/{id}/statut

**Description:** Change appointment status.

**Request body:**
```json
{
  "statut": "CONFIRME",
  "commentaire": "Patient confirmé"
}
```

curl example:

```bash
curl -X PATCH "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv/rdv_1/statut" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"statut": "CONFIRME", "commentaire": "Patient confirmé"}'
```

Expected response: 200 OK with updated `RendezVousResponse` (statut changed to `CONFIRME`).

### [GET] /api/rdv

**Description:** List appointments with optional filters; results depend on caller role (admin sees all, doctors/patients only their own).

**Query parameters:**
- `medecinId` (string, optional)
- `patientId` (string, optional)
- `from` (datetime, optional, ISO 8601)
- `to` (datetime, optional, ISO 8601)
- Paging params supported via `Pageable`.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv?from=2024-06-01T00:00:00Z&to=2024-06-30T23:59:59Z" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "content": [
    {
      "id": "rdv_1",
      "medecinId": "med_1",
      "patientId": "pat_1",
      "medecin": {"id": "med_1", "fullName": "Dr Antoine Leroy", "email": "antoine.leroy@example.com", "phone": "+33111222333", "avatarUrl": "https://cdn.example.com/avatars/doctor.png", "specialty": "Cardiologie"},
      "patient": {"id": "pat_1", "fullName": "Claire Martin", "email": "claire.martin@example.com", "phone": "+33612345678", "avatarUrl": null, "specialty": null},
      "debut": "2024-06-20T09:00:00Z",
      "fin": "2024-06-20T09:30:00Z",
      "statut": "PLANIFIE",
      "motif": "Consultation annuelle",
      "notesPrivees": "Prévoir résultats sanguins",
      "createurId": "med_1",
      "createdAt": "2024-06-01T08:00:00Z",
      "updatedAt": "2024-06-01T08:00:00Z"
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":20},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {"empty": true,"sorted": false,"unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### [GET] /api/rdv/{id}

**Description:** Retrieve appointment details.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv/rdv_1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response: 200 OK with `RendezVousResponse` body (see creation example).

### [DELETE] /api/rdv/{id}

**Description:** Cancel/delete an appointment.

curl example:

```bash
curl -X DELETE "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/rdv/rdv_1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response: Status code 204 No Content.

## Doctor Search Endpoints

### [GET] /api/medecins

**Description:** Search doctors by name, specialty, or city.

**Query parameters:**
- `q` (string, optional) – text search
- `specialite` (string, optional)
- `ville` (string, optional)
- Pageable parameters apply

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins?q=cardio&ville=Paris" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "content": [
    {
      "id": "med_1",
      "fullName": "Dr Antoine Leroy",
      "email": "antoine.leroy@example.com",
      "specialty": "Cardiologie",
      "phone": "+33111222333",
      "clinicAddress": {
        "line1": "10 rue des Lilas",
        "line2": null,
        "city": "Paris",
        "country": "France",
        "zip": "75010"
      },
      "yearsOfExperience": 12,
      "active": true
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":20},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {"empty": true,"sorted": false,"unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

## Patient Profile Endpoints

### [GET] /api/patients/me

**Description:** Get the authenticated patient's profile.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/patients/me" \
  -H "Authorization: Bearer <YOUR_PATIENT_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "id": "pat_1",
  "fullName": "Claire Martin",
  "email": "claire.martin@example.com",
  "phone": "+33612345678",
  "gender": "FEMALE",
  "dateOfBirth": "1990-05-14",
  "address": {
    "line1": "123 rue de la Santé",
    "line2": "Appartement 4B",
    "city": "Paris",
    "country": "France",
    "zip": "75013"
  },
  "insuranceNumber": "INS-123456789",
  "emergencyContact": {
    "name": "Jean Dupont",
    "phone": "+33123456789",
    "relation": "Conjoint"
  },
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

### [PUT] /api/patients/me

**Description:** Update patient profile fields.

**Request body:**
```json
{
  "fullName": "Claire Martin",
  "email": "claire.martin@example.com",
  "phone": "+33612345678",
  "address": {
    "line1": "123 rue de la Santé",
    "city": "Paris",
    "country": "France",
    "zip": "75013"
  },
  "insuranceNumber": "INS-123456789",
  "emergencyContact": {
    "name": "Jean Dupont",
    "phone": "+33123456789",
    "relation": "Conjoint"
  },
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

curl example:

```bash
curl -X PUT "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/patients/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_PATIENT_TOKEN>" \
  -d '{"fullName": "Claire Martin", "email": "claire.martin@example.com"}'
```

Expected response: 200 OK with updated `PatientProfileResponse` (similar to GET example).

## Pre-diagnostic Endpoints

### [POST] /api/prediagnostic

**Description:** Analyze symptoms and return a pre-diagnostic recommendation (accessible to PATIENT, MEDECIN, ADMIN).

**Request body:**
```json
{
  "symptomes": ["fièvre", "toux"],
  "contexte": "Depuis 3 jours, fatigue"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/prediagnostic" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"symptomes": ["fièvre", "toux"], "contexte": "Depuis 3 jours"}'
```

Expected response (example):
Status code: 200 OK

```json
{
  "conclusion": "Symptômes compatibles avec une infection respiratoire",
  "recommandations": [
    "Consulter un médecin si la fièvre persiste",
    "Hydratation et repos"
  ]
}
```

## Notification Endpoints

### [GET] /api/notifications/preferences/me

**Description:** Retrieve notification preferences for the authenticated user.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications/preferences/me" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):
Status code: 200 OK

```json
{
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "rappelAutomatique": true
}
```

### [PUT] /api/notifications/preferences/me

**Description:** Update notification preferences.

**Request body:**
```json
{
  "emailEnabled": true,
  "smsEnabled": true,
  "pushEnabled": false,
  "rappelAutomatique": false
}
```

curl example:

```bash
curl -X PUT "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications/preferences/me" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"emailEnabled": true, "smsEnabled": true}'
```

Expected response: 200 OK with updated preferences.

### [POST] /api/notifications/rappels

**Description:** Schedule a reminder notification (PATIENT/MEDECIN/ADMIN).

**Request body:**
```json
{
  "rendezVousId": "rdv_1",
  "destinataireId": "pat_1",
  "canal": "EMAIL",
  "dateEnvoi": "2024-06-19T09:00:00Z",
  "message": "Rappel de votre rendez-vous"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications/rappels" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{"rendezVousId": "rdv_1", "destinataireId": "pat_1", "canal": "EMAIL", "dateEnvoi": "2024-06-19T09:00:00Z"}'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "notif_1",
  "rendezVousId": "rdv_1",
  "destinataireId": "pat_1",
  "canal": "EMAIL",
  "dateEnvoi": "2024-06-19T09:00:00Z",
  "message": "Rappel de votre rendez-vous",
  "statut": "PLANIFIEE",
  "createdAt": "2024-06-01T09:00:00Z",
  "updatedAt": "2024-06-01T09:00:00Z"
}
```

### [GET] /api/notifications

**Description:** List scheduled notifications (admin only, pageable).

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications?page=0&size=20" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response (example):

```json
{
  "content": [
    {
      "id": "notif_1",
      "rendezVousId": "rdv_1",
      "destinataireId": "pat_1",
      "canal": "EMAIL",
      "dateEnvoi": "2024-06-19T09:00:00Z",
      "message": "Rappel de votre rendez-vous",
      "statut": "PLANIFIEE",
      "createdAt": "2024-06-01T09:00:00Z",
      "updatedAt": "2024-06-01T09:00:00Z"
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":20},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {"empty": true,"sorted": false,"unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### [PATCH] /api/notifications/{id}/etat

**Description:** Mark a notification as sent (or failed).

**Request body:** Optional boolean `true/false` to indicate success (defaults to true when omitted).

curl example:

```bash
curl -X PATCH "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications/notif_1/etat" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -d 'false'
```

Expected response: 200 OK with updated `NotificationPlanifieeResponse` (statut becomes `ENVOYEE` or `ECHEC`).

### [POST] /api/notifications/rappels/execute

**Description:** Trigger due reminders immediately (admin only).

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/notifications/rappels/execute" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response (example):

```json
[
  {
    "id": "notif_2",
    "rendezVousId": "rdv_2",
    "destinataireId": "pat_2",
    "canal": "SMS",
    "dateEnvoi": "2024-06-02T08:00:00Z",
    "message": "Rappel SMS",
    "statut": "ENVOYEE",
    "createdAt": "2024-06-01T09:00:00Z",
    "updatedAt": "2024-06-02T08:00:00Z"
  }
]
```

## Consultation Endpoints

### [POST] /api/consultations

**Description:** Create a consultation record (doctor or admin).

**Request body:**
```json
{
  "rendezVousId": "rdv_1",
  "patientId": "pat_1",
  "date": "2024-06-20T09:00:00Z",
  "resume": "Patient en bonne santé",
  "diagnostic": "Aucun problème majeur",
  "planSuivi": "Contrôle annuel",
  "recommandations": ["Pratique sportive régulière"]
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/consultations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_DOCTOR_OR_ADMIN_TOKEN>" \
  -d '{"rendezVousId": "rdv_1", "patientId": "pat_1", "date": "2024-06-20T09:00:00Z"}'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "consult_1",
  "rendezVousId": "rdv_1",
  "medecinId": "med_1",
  "patientId": "pat_1",
  "date": "2024-06-20T09:00:00Z",
  "resume": "Patient en bonne santé",
  "diagnostic": "Aucun problème majeur",
  "planSuivi": "Contrôle annuel",
  "recommandations": ["Pratique sportive régulière"],
  "createdAt": "2024-06-20T10:00:00Z"
}
```

### [GET] /api/consultations

**Description:** List consultations (admin sees all; doctor/patient see their own).

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/consultations?page=0&size=20" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):

```json
{
  "content": [
    {
      "id": "consult_1",
      "rendezVousId": "rdv_1",
      "medecinId": "med_1",
      "patientId": "pat_1",
      "date": "2024-06-20T09:00:00Z",
      "resume": "Patient en bonne santé",
      "diagnostic": "Aucun problème majeur",
      "planSuivi": "Contrôle annuel",
      "recommandations": ["Pratique sportive régulière"],
      "createdAt": "2024-06-20T10:00:00Z"
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":20},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {"empty": true,"sorted": false,"unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### [GET] /api/consultations/{id}

**Description:** Retrieve a consultation by id.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/consultations/consult_1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response: 200 OK with `ConsultationResponse` (see creation example).

## Prescription Endpoints

### [POST] /api/prescriptions

**Description:** Create a prescription (doctor or admin).

**Request body:**
```json
{
  "consultationId": "consult_1",
  "patientId": "pat_1",
  "medicaments": [
    {
      "nom": "Paracetamol",
      "dosage": "500mg",
      "frequence": "2 fois par jour",
      "duree": "5 jours"
    }
  ],
  "instructionsGenerales": "Prendre après les repas"
}
```

curl example:

```bash
curl -X POST "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/prescriptions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_DOCTOR_OR_ADMIN_TOKEN>" \
  -d '{"consultationId": "consult_1", "patientId": "pat_1", "medicaments": [{"nom": "Paracetamol", "dosage": "500mg", "frequence": "2 fois par jour", "duree": "5 jours"}]}'
```

Expected response (example):
Status code: 201 Created

```json
{
  "id": "presc_1",
  "consultationId": "consult_1",
  "medecinId": "med_1",
  "patientId": "pat_1",
  "medicaments": [
    {
      "nom": "Paracetamol",
      "dosage": "500mg",
      "frequence": "2 fois par jour",
      "duree": "5 jours"
    }
  ],
  "instructionsGenerales": "Prendre après les repas",
  "createdAt": "2024-06-20T10:05:00Z"
}
```

### [GET] /api/prescriptions

**Description:** List prescriptions (admin all; doctor/patient own).

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/prescriptions?page=0&size=20" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response (example):

```json
{
  "content": [
    {
      "id": "presc_1",
      "consultationId": "consult_1",
      "medecinId": "med_1",
      "patientId": "pat_1",
      "medicaments": [
        {
          "nom": "Paracetamol",
          "dosage": "500mg",
          "frequence": "2 fois par jour",
          "duree": "5 jours"
        }
      ],
      "instructionsGenerales": "Prendre après les repas",
      "createdAt": "2024-06-20T10:05:00Z"
    }
  ],
  "pageable": {"pageNumber":0,"pageSize":20},
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {"empty": true,"sorted": false,"unsorted": true},
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

### [GET] /api/prescriptions/{id}

**Description:** Retrieve a prescription by id.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/prescriptions/presc_1" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

Expected response: 200 OK with `PrescriptionResponse` (see creation example).

## Statistics Endpoints

### [GET] /api/admin/stats

**Description:** Retrieve admin dashboard statistics for an optional period (admin only).

**Query parameters:**
- `from` (datetime, optional, ISO 8601)
- `to` (datetime, optional, ISO 8601)

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/stats?from=2024-01-01T00:00:00Z&to=2024-06-30T23:59:59Z" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>"
```

Expected response (example):

```json
{
  "periodeDebut": "2024-01-01T00:00:00Z",
  "periodeFin": "2024-06-30T23:59:59Z",
  "totalRendezVous": 120,
  "rendezVousPlanifies": 80,
  "rendezVousConfirmes": 25,
  "rendezVousAnnules": 10,
  "rendezVousHonores": 5,
  "patientsActifs": 200,
  "medecinsActifs": 50
}
```

### [GET] /api/admin/stats/export

**Description:** Export admin stats as CSV (admin only).

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/stats/export" \
  -H "Authorization: Bearer <YOUR_ADMIN_TOKEN>" \
  -H "Accept: text/csv"
```

Expected response (example):

```
periodeDebut,periodeFin,totalRendezVous,planifies,confirmes,annules,honores,patientsActifs,medecinsActifs
2024-01-01T00:00:00Z,2024-06-30T23:59:59Z,120,80,25,10,5,200,50
```

### [GET] /api/medecins/me/stats

**Description:** Retrieve statistics for the authenticated doctor.

curl example:

```bash
curl -X GET "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins/me/stats?from=2024-01-01T00:00:00Z" \
  -H "Authorization: Bearer <YOUR_DOCTOR_TOKEN>"
```

Expected response: 200 OK with `DashboardStatsResponse` similar to the admin stats JSON.

