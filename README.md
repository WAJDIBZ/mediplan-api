# MediPlan API

API Spring Boot pour la gestion de l'authentification des patients et des médecins avec MongoDB.

## Endpoints principaux

| Méthode | Chemin                       | Description                     |
|---------|------------------------------|---------------------------------|
| POST    | `/api/auth/register/patient` | Inscription d'un patient         |
| POST    | `/api/auth/register/doctor`  | Inscription d'un médecin         |
| POST    | `/api/auth/login`            | Connexion et génération de JWT   |
| POST    | `/api/auth/refresh`          | Renouvellement du token refresh  |

## Exemples cURL

### Inscription d'un patient

```bash
curl -X POST http://localhost:8080/api/auth/register/patient \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Claire Martin",
        "email": "claire.martin@example.com",
        "password": "motdepasseFort123",
        "phone": "+33612345678",
        "dateOfBirth": "1990-05-14",
        "gender": "FEMALE"
      }'
```

Réponse attendue :

```json
{
  "accessToken": "<jwt_access>",
  "refreshToken": "<jwt_refresh>"
}
```

### Inscription d'un médecin

```bash
curl -X POST http://localhost:8080/api/auth/register/doctor \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Dr Antoine Leroy",
        "email": "antoine.leroy@example.com",
        "password": "MotDePasseSolide1",
        "specialty": "Cardiologie",
        "licenseNumber": "LIC-987654",
        "yearsOfExperience": 12
      }'
```

### Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
        "email": "claire.martin@example.com",
        "password": "motdepasseFort123"
      }'
```

### Erreur sur email dupliqué (409)

```bash
curl -X POST http://localhost:8080/api/auth/register/doctor \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Dr Antoine Leroy",
        "email": "claire.martin@example.com",
        "password": "MotDePasseSolide1",
        "specialty": "Cardiologie",
        "licenseNumber": "LIC-987654",
        "yearsOfExperience": 12
      }'
```

Réponse :

```json
{
  "message": "Cet email est déjà utilisé."
}
```

### Erreur de validation (400)

```bash
curl -X POST http://localhost:8080/api/auth/register/patient \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Cl",
        "email": "mauvais-format",
        "password": "123"
      }'
```

Réponse :

```json
{
  "message": "Requête invalide",
  "erreurs": {
    "fullName": "Le nom complet doit contenir au moins 3 caractères",
    "email": "L'email doit être valide",
    "password": "Le mot de passe doit contenir au moins 8 caractères",
    "dateOfBirth": "La date de naissance est obligatoire",
    "gender": "Le genre est obligatoire"
  }
}
```

## Variables d'environnement attendues

| Nom                       | Description                                                                 | Obligatoire |
|---------------------------|------------------------------------------------------------------------------|-------------|
| `MONGO_URI`               | Chaîne de connexion MongoDB                                                 | ✅          |
| `JWT_ACCESS_SECRET`       | Secret pour signer les tokens d'accès JWT                                   | ✅          |
| `JWT_REFRESH_SECRET`      | Secret pour signer les tokens de rafraîchissement                           | ✅          |
| `GOOGLE_CLIENT_ID`        | Identifiant OAuth2 Google                                                    | ✅          |
| `GOOGLE_CLIENT_SECRET`    | Secret OAuth2 Google                                                         | ✅          |
| `FACEBOOK_CLIENT_ID`      | Identifiant OAuth2 Facebook                                                  | ✅          |
| `FACEBOOK_CLIENT_SECRET`  | Secret OAuth2 Facebook                                                       | ✅          |
| `FRONT_REDIRECT_URL`      | URL de redirection front (défaut `http://localhost:3000/oauth/success`)    | ❌ (défaut) |
| `APP_OAUTH_REDIRECT_MODE` | `QUERY` (défaut) pour renvoyer les tokens en query, `COOKIES` pour cookies | ❌          |
