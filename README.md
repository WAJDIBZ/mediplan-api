# MediPlan API

API Spring Boot pour la gestion de l'authentification des patients et des médecins avec MongoDB.

## Endpoints principaux

| Méthode | Chemin                       | Description                     |
|---------|------------------------------|---------------------------------|
| POST    | `/api/auth/register/patient` | Inscription d'un patient         |
| POST    | `/api/auth/register/doctor`  | Inscription d'un médecin         |
| POST    | `/api/auth/login`            | Connexion et génération de JWT   |
| POST    | `/api/auth/refresh`          | Renouvellement du token refresh  |

## Endpoints Administration (ROLE_ADMIN)

| Méthode | Chemin                              | Description                                         |
|---------|-------------------------------------|-----------------------------------------------------|
| GET     | `/api/admin/users`                  | Lister les profils avec filtres et pagination       |
| GET     | `/api/admin/users/{id}`             | Consulter le détail d'un profil                     |
| POST    | `/api/admin/users`                  | Créer un nouveau profil (patient, médecin, admin)   |
| PATCH   | `/api/admin/users/{id}`             | Mettre à jour partiellement un profil               |
| DELETE  | `/api/admin/users/{id}`             | Supprimer (logiquement ou définitivement) un profil |
| POST    | `/api/admin/users/{id}/deactivate`  | Désactiver un compte                                |
| POST    | `/api/admin/users/{id}/reactivate`  | Réactiver un compte                                 |
| POST    | `/api/admin/users/{id}/role`        | Changer le rôle (ADMIN / MEDECIN / PATIENT)         |
| POST    | `/api/admin/users/{id}/reset-password` | Réinitialiser le mot de passe                    |
| POST    | `/api/admin/users/export`           | Exporter la sélection au format CSV                 |
| POST    | `/api/admin/users/bulk/*`           | Actions de masse (désactiver, réactiver, supprimer) |

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

### Listing des utilisateurs (ROLE_ADMIN)

```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10&sort=createdAt,desc&q=martin" \
  -H "Authorization: Bearer <jwt_admin>"
```

Réponse (extrait) :

```json
{
  "content": [
    {
      "id": "665abf91f1e54d74af3e6c21",
      "fullName": "Claire Martin",
      "email": "claire.martin@example.com",
      "role": "PATIENT",
      "active": true,
      "provider": "LOCAL",
      "createdAt": "2024-06-01T08:30:12.123Z"
    }
  ],
  "totalElements": 1
}
```

### Création d'un médecin (ROLE_ADMIN)

```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <jwt_admin>" \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Dr Laura Petit",
        "email": "laura.petit@example.com",
        "password": "MedecinFort123",
        "role": "MEDECIN",
        "specialty": "Cardiologie",
        "licenseNumber": "LIC-55555",
        "yearsOfExperience": 7
      }'
```

### Changement de rôle incompatible (422)

```bash
curl -X POST http://localhost:8080/api/admin/users/665abf91f1e54d74af3e6c21/role \
  -H "Authorization: Bearer <jwt_admin>" \
  -H "Content-Type: application/json" \
  -d '{"role": "MEDECIN"}'
```

Réponse :

```json
{
  "message": "Impossible de promouvoir cet utilisateur : licence manquante."
}
```

### Export CSV (ROLE_ADMIN)

```bash
curl -X POST "http://localhost:8080/api/admin/users/export?role=PATIENT" \
  -H "Authorization: Bearer <jwt_admin>" \
  -H "Accept: text/csv"
```

Le corps de la réponse contient un fichier `text/csv` avec les colonnes : `id,fullName,email,role,active,provider,createdAt`.

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
