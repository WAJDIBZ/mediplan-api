# Documentation API Backend — MediPlan

Base URL production : `https://mediplan-api-1b2c88de81dd.herokuapp.com`

Toutes les routes (hors `/api/auth/**`, `/actuator/**`, `/data-deletion.html`, `/privacy-policy`, `/error`) exigent un jeton JWT Bearer valide. Les réponses d'erreur sont au format JSON : `{ "message": "…" }` ou `{ "message": "…", "erreurs": { … } }` pour la validation.

## Permissions par rôle

| Ressource | Actions | Rôles autorisés |
|-----------|---------|-----------------|
| `/api/auth/**` | Inscription, login, refresh, autodétection | Public |
| `/api/admin/users/**` | Gestion des comptes (CRUD, activation, export) | ADMIN |
| `/api/medecins/**` | Recherche médecins, gestion disponibilités | PATIENT / MEDECIN / ADMIN |
| `/api/rdv/**` | Gestion des rendez-vous (création, statut, annulation) | PATIENT / MEDECIN / ADMIN |
| `/api/patients/me/**` | Profil patient et historique | PATIENT |
| `/api/consultations/**` | Dossiers de consultation | PATIENT / MEDECIN / ADMIN |
| `/api/prescriptions/**` | Prescriptions médicales | PATIENT / MEDECIN / ADMIN |
| `/api/admin/stats/**` | Indicateurs globaux & export CSV | ADMIN |
| `/api/medecins/me/stats` | Tableau de bord médecin | MEDECIN |
| `/api/notifications/**` | Préférences & rappels | PATIENT / MEDECIN / ADMIN (lecture) ; ADMIN (pilotage global) |
| `/api/prediagnostic` | Pré-diagnostic IA léger | PATIENT / MEDECIN / ADMIN |

Les rôles métier (`Role`) sont : `ADMIN`, `MEDECIN`, `PATIENT`.

## Paramètres transverses

| Paramètre | Description |
|-----------|-------------|
| `Authorization` | Header `Authorization: Bearer $TOKEN`. |
| `page` | Index de page (défaut `0`). |
| `size` | Taille de page (défaut `20`). |
| `sort` | Tri `champ,(asc|desc)`. Champs autorisés pour la liste utilisateurs : `createdAt`, `fullName`, `email`, `role`, `active`, `provider`. |

---

## Authentification (`/api/auth`)

### POST `/api/auth/register/patient`

- **Rôle** : public.
- **Body** (`application/json`) :
  ```json
  {
    "fullName": "Claire Martin",
    "avatarUrl": "https://cdn...",
    "email": "claire.martin@example.com",
    "password": "MotdepasseSolide123",
    "phone": "+33612345678",
    "address": {
      "line1": "10 rue de la Paix",
      "line2": "",
      "city": "Paris",
      "country": "France",
      "zip": "75002"
    },
    "dateOfBirth": "1990-05-14",
    "gender": "FEMALE",
    "insuranceNumber": "INS-123456",
    "emergencyContact": {
      "name": "Jean Martin",
      "phone": "+33123456789",
      "relation": "Conjoint"
    }
  }
  ```
- **Réponse 200** :
  ```json
  {
    "accessToken": "…",
    "refreshToken": "…",
    "role": "PATIENT"
  }
  ```
- **Erreurs** : `400` (validation), `409` (email déjà utilisé).
- **curl** :
  ```bash
  curl -X POST \
    "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/register/patient" \
    -H "Content-Type: application/json" \
    -d '{"fullName":"Claire Martin","email":"claire.martin@example.com","password":"MotdepasseSolide123","dateOfBirth":"1990-05-14","gender":"FEMALE"}'
  ```

### POST `/api/auth/register/doctor`

- **Rôle** : public.
- **Body** : champs équivalents au patient + `specialty`, `licenseNumber`, `yearsOfExperience`, `clinicName`, `clinicAddress`.
- **Réponse / Erreurs** : identique au patient (`role` = `MEDECIN`).
- **Règles métier** : numéro de licence unique, spécialité obligatoire.

### POST `/api/auth/login`

- **Rôle** : public.
- **Body** : `{ "email": "…", "password": "…" }`.
- **Réponse 200** : jetons d’accès/refresh + rôle.
- **Erreurs** : `401` si identifiants invalides.

```bash
curl -X POST \
  "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Secret123"}'
```

### POST `/api/auth/refresh?token={refreshToken}`

- **Rôle** : public (mais token refresh valide obligatoire).
- **Réponse 200** : nouveaux jetons `{ "accessToken", "refreshToken", "role" }`.
- **Erreurs** : `401` token invalide ou expiré.

```bash
curl -X POST \
  "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/auth/refresh?token=$REFRESH" \
  -H "Content-Type: application/json"
```

### GET `/api/auth/me`

- **Rôle** : authentifié (tout rôle).
- **Réponse 200** : corps vide (utilisé comme ping auth).
- **Erreurs** : `401`/`403` si token absent ou compte désactivé.

---

## Administration utilisateurs (`/api/admin/users`)

> Toutes les routes ci-dessous exigent le rôle `ADMIN`. En cas de compte désactivé, la requête retourne `403`.

### GET `/api/admin/users`

- **Description** : liste paginée des utilisateurs.
- **Query params** :
  - `q` : recherche plein texte sur nom/email.
  - `role` : filtre enum (`ADMIN`, `MEDECIN`, `PATIENT`).
  - `active` : `true`/`false`.
  - `provider` : filtre fournisseur (`LOCAL`, `GOOGLE`, `FACEBOOK`, …).
  - `page`, `size`, `sort` (voir paramètres transverses).
- **Réponse 200** : `Page<AdminUserListItemDTO>`
  ```json
  {
    "content": [
      {
        "id": "665f…",
        "fullName": "Claire Martin",
        "email": "claire.martin@example.com",
        "role": "PATIENT",
        "active": true,
        "provider": "LOCAL",
        "createdAt": "2024-05-21T09:30:00Z"
      }
    ],
    "pageable": { … },
    "totalElements": 42,
    "totalPages": 5
  }
  ```
- **Erreurs** : `422` si champ de tri invalide.
- **curl** :
  ```bash
  curl -X GET \
    "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users?page=0&size=20&sort=createdAt,desc" \
    -H "Authorization: Bearer $TOKEN"
  ```

### GET `/api/admin/users/{id}`

- **Description** : détail complet `AdminUserDetailsDTO` (profil, coordonnées, informations patient/médecin).
- **Réponse 200** : JSON détaillé.
- **Erreurs** : `404` si utilisateur introuvable.

### POST `/api/admin/users`

- **Description** : création manuelle d’un utilisateur (patient, médecin ou admin).
- **Body** : `AdminCreateUserRequest` (mêmes champs que DTO d’inscription + rôle cible + adresse, contact d’urgence, informations cabinet).
- **Réponse 201** : `AdminUserDetailsDTO` nouvellement créé.
- **Erreurs** : `400` validation, `409` email/licence dupliqués, `422` règles métier (ex : spécialité manquante pour un médecin).

```bash
curl -X POST \
  "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Dr Antoine Leroy","email":"antoine@example.com","role":"MEDECIN","licenseNumber":"LIC-123","specialty":"Cardiologie","password":"Secret123"}'
```

### PATCH `/api/admin/users/{id}`

- **Description** : mise à jour partielle (nom, email, mot de passe, téléphone, adresses, infos patient/médecin).
- **Body** : `AdminUpdateUserRequest` (tous champs optionnels).
- **Réponse 200** : `AdminUserDetailsDTO` mis à jour.
- **Erreurs** : `400` validation, `404` utilisateur, `409` email/licence déjà pris, `422` si champs incohérents (ex : champs patient pour un médecin).

### DELETE `/api/admin/users/{id}`

- **Description** : suppression. Par défaut, désactive le compte (soft delete). `?hard=true` réalise une suppression définitive.
- **Réponse 204** : sans contenu.
- **Erreurs** : `404` si `hard=true` et utilisateur absent.
- **Bonnes pratiques** : côté UI préférer la désactivation.

### POST `/api/admin/users/{id}/deactivate`

- **Description** : désactive un compte.
- **Réponse 204**. Idempotent.

### POST `/api/admin/users/{id}/reactivate`

- **Description** : réactive un compte inactif.
- **Réponse 204**.

### POST `/api/admin/users/{id}/role`

- **Description** : changer le rôle d’un utilisateur.
- **Body** : `AdminChangeRoleRequest`.
  ```json
  {
    "role": "MEDECIN",
    "specialty": "Cardiologie",
    "licenseNumber": "LIC-999",
    "yearsOfExperience": 12,
    "clinicName": "Cabinet du Parc",
    "clinicAddress": {
      "line1": "5 avenue des Peupliers",
      "city": "Lyon",
      "country": "France",
      "zip": "69006"
    }
  }
  ```
- **Réponse 200** : `AdminUserDetailsDTO`.
- **Erreurs** : `404` (utilisateur), `409` (licence déjà attribuée), `422` (spécialité/licence manquantes).

```bash
curl -X POST \
  "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/665f123/role" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"MEDECIN","specialty":"Dermatologie","licenseNumber":"LIC-777"}'
```

### POST `/api/admin/users/{id}/reset-password`

- **Description** : réinitialise le mot de passe (hashé côté serveur via BCrypt).
- **Body** : `{ "newPassword": "MotdepasseSolide123" }`.
- **Réponse 204**.
- **Erreurs** : `400` (mot de passe trop court), `404`.

### POST `/api/admin/users/bulk/{action}`

- **Routes** :
  - `/bulk/deactivate`
  - `/bulk/reactivate`
  - `/bulk/delete`
- **Body** : `{ "ids": ["..."] }`.
- **Réponse 204** (les identifiants inexistants sont ignorés silencieusement).

### POST `/api/admin/users/export`

- **Description** : export CSV filtré.
- **Query params** : mêmes filtres que la liste (`q`, `role`, `active`, `provider`, `sort`).
- **Réponse 200** : fichier `text/csv`, en-tête `Content-Disposition: attachment; filename=users.csv`.
- **Format** : en-tête `id,fullName,email,role,active,provider,createdAt` ; dates au format ISO-8601.

```bash
curl -X POST \
  "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/admin/users/export?role=MEDECIN" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: text/csv" -o users.csv
```

---

## Gestion des erreurs

| Code | Contexte |
|------|----------|
| 400 | Validation Bean (`MethodArgumentNotValidException`). |
| 401 | Absence token / token invalide / identifiants erronés. |
| 403 | Accès interdit (rôle insuffisant, compte désactivé). |
| 404 | Ressource introuvable. |
| 409 | Conflit (email/licence déjà existants). |
| 422 | Règle métier non respectée (ex : tri invalide, spécialité manquante). |
| 500 | Erreur interne inattendue. |

Les messages sont toujours en français et ne contiennent pas de détails sensibles.

---

## Exemple de flux complet (Admin)

1. **Connexion** : POST `/api/auth/login` → récupérer `$TOKEN`.
2. **Lister** : GET `/api/admin/users?page=0&size=20` avec `Authorization`.
3. **Changer rôle** : POST `/api/admin/users/{id}/role` (promotion médecin, spécialité obligatoire).
4. **Désactiver** : POST `/api/admin/users/{id}/deactivate` pour masquer un compte au lieu de le supprimer.
5. **Exporter** : POST `/api/admin/users/export` pour récupérer le CSV filtré.

Respecter systématiquement les réponses HTTP et les messages fournis pour afficher les toasts côté UI (401/403/422/500).

---

## Recherche médecins & disponibilités (`/api/medecins`)

### GET `/api/medecins`

- **Rôles** : `PATIENT`, `MEDECIN`, `ADMIN`.
- **Query params** :
  - `q` : recherche plein texte (nom, spécialité, cabinet).
  - `specialite` : filtre exact (case insensitive).
  - `ville` : filtre sur la ville du cabinet.
  - `page`, `size`, `sort` (tri par `fullName` par défaut).
- **Réponse 200** : `Page<MedecinSearchResponse>`.
- **Erreurs** : `401/403` si non authentifié.
- **curl** :
  ```bash
  curl -X GET \
    "https://mediplan-api-1b2c88de81dd.herokuapp.com/api/medecins?q=cardio&ville=Lyon" \
    -H "Authorization: Bearer $TOKEN"
  ```

### Gestion des disponibilités (`/api/medecins/{id}/disponibilites`)

- **Rôles** :
  - `MEDECIN` (sur son propre identifiant).
  - `ADMIN` (sur n'importe quel médecin).
  - `PATIENT` : lecture seule (`GET`).

#### POST `/api/medecins/{id}/disponibilites`
- **Body** :
  ```json
  {
    "date": "2024-10-15",
    "heureDebut": "08:30",
    "heureFin": "12:00",
    "recurrence": "AUCUNE",
    "commentaire": "Consultations au cabinet"
  }
  ```
- **Réponse 201** : `DisponibiliteResponse`.
- **Erreurs** : `400` (validation), `403` (médecin différent), `404` (médecin inconnu).

#### GET `/api/medecins/{id}/disponibilites`
- **Query params** : `from`, `to` (ISO `YYYY-MM-DD`).
- **Réponse 200** : liste JSON de disponibilités actives.

#### PUT / DELETE `/api/medecins/{id}/disponibilites/{disponibiliteId}`
- **Règles** : mêmes validations que la création ; suppression logique par effacement du document.
- **Réponse** : `200` (`PUT`) / `204` (`DELETE`).

---

## Rendez-vous (`/api/rdv`)

### POST `/api/rdv`
- **Rôles** : `ADMIN`, `MEDECIN`, `PATIENT`.
- **Body** :
  ```json
  {
    "medecinId": "666f1...",
    "patientId": "777ab...",
    "debut": "2024-10-15T08:30:00Z",
    "fin": "2024-10-15T09:00:00Z",
    "motif": "Suivi trimestriel"
  }
  ```
- **Réponse 201** : `RendezVousResponse` (statut `PLANIFIE`).
- **Erreurs** : `400` (validation horaire), `409` (chevauchement), `422` (hors disponibilité), `403` si patient/medecin tente d'agir pour un autre compte.

### GET `/api/rdv`
- **Rôles** :
  - `ADMIN` : vision globale avec filtres.
  - `MEDECIN` : uniquement ses rendez-vous.
  - `PATIENT` : uniquement ses rendez-vous.
- **Query params** : `medecinId`, `patientId`, `from`, `to`, `page`, `size`, `sort` (`debut` par défaut).
- **Réponse 200** : `Page<RendezVousResponse>`.

### PUT `/api/rdv/{id}`
- **Objet** : modifier créneau et participants (mêmes règles que la création).

### PATCH `/api/rdv/{id}/statut`
- **Body** : `{ "statut": "CONFIRME", "commentaire": "Patient prévenu" }`.
- **Statuts autorisés** : `PLANIFIE`, `CONFIRME`, `ANNULE`, `HONORE`.
- **Règle** : un rendez-vous annulé ne peut pas repasser à un statut actif.

### DELETE `/api/rdv/{id}`
- **Effet** : statut forcé à `ANNULE` (pas de suppression dure).

---

## Profil patient (`/api/patients/me`)

### GET `/api/patients/me`
- **Rôle** : `PATIENT`.
- **Réponse** : informations profil (nom, coordonnées, assurances, contacts d'urgence).

### PUT `/api/patients/me`
- **Body** :
  ```json
  {
    "fullName": "Claire Martin",
    "email": "claire@example.com",
    "phone": "+33612345678",
    "address": { "line1": "10 rue de la Paix", "city": "Paris", "country": "France" },
    "insuranceNumber": "INS-123",
    "emergencyContact": { "name": "Jean", "phone": "+33123456789", "relation": "Conjoint" }
  }
  ```
- **Réponse** : profil mis à jour.
- **Erreurs** : `400` (validation), `403` (autre rôle).

---

## Consultations (`/api/consultations`)

### POST `/api/consultations`
- **Rôles** : `MEDECIN`, `ADMIN`.
- **Body** :
  ```json
  {
    "rendezVousId": "67a1...",
    "patientId": "777ab...",
    "date": "2024-10-15T09:15:00Z",
    "resume": "Bilan annuel",
    "diagnostic": "RAS",
    "planSuivi": "Contrôle dans 6 mois",
    "recommandations": ["Poursuivre l'activité physique", "Hydratation"]
  }
  ```
- **Réponse 201** : `ConsultationResponse`.
- **Erreurs** : `400` (validation), `403` (rendez-vous ne correspondant pas), `404` (rendez-vous absent).

### GET `/api/consultations`
- **Rôles** :
  - `ADMIN` : toutes les consultations (paginées).
  - `MEDECIN` : ses dossiers.
  - `PATIENT` : ses dossiers.
- **Réponse 200** : `Page<ConsultationResponse>` triée par date décroissante.

### GET `/api/consultations/{id}`
- **Accès** : même règles que ci-dessus, `404` si introuvable / `403` si non autorisé.

---

## Prescriptions (`/api/prescriptions`)

### POST `/api/prescriptions`
- **Rôles** : `MEDECIN`, `ADMIN`.
- **Body** :
  ```json
  {
    "consultationId": "67a1...",
    "patientId": "777ab...",
    "medicaments": [
      { "nom": "Doliprane", "dosage": "500mg", "frequence": "3 fois/jour", "duree": "5 jours" }
    ],
    "instructionsGenerales": "Ne pas dépasser 3g/jour"
  }
  ```
- **Réponse 201** : `PrescriptionResponse`.
- **Erreurs** : `400` (validation), `403` (rôle patient ou patient différent), `404` (consultation absente), `409` (prescription déjà existante).

### GET `/api/prescriptions`
- **Rôles** : `ADMIN` (vision globale), `MEDECIN` (ses prescriptions), `PATIENT` (ses prescriptions).
- **Réponse 200** : page triée par date de création décroissante.

### GET `/api/prescriptions/{id}`
- **Accès** : mêmes règles de visibilité.

---

## Statistiques (`/api/admin/stats`, `/api/medecins/me/stats`)

### GET `/api/admin/stats`
- **Rôle** : `ADMIN`.
- **Query params** : `from`, `to` (ISO-8601). Sans paramètre, agrégation globale.
- **Réponse 200** :
  ```json
  {
    "periodeDebut": "1970-01-01T00:00:00Z",
    "periodeFin": "2024-10-15T12:00:00Z",
    "totalRendezVous": 124,
    "rendezVousPlanifies": 40,
    "rendezVousConfirmes": 52,
    "rendezVousAnnules": 12,
    "rendezVousHonores": 20,
    "patientsActifs": 320,
    "medecinsActifs": 42
  }
  ```

### GET `/api/admin/stats/export`
- **Effet** : export CSV (`text/csv`, header `Content-Disposition`), mêmes paramètres `from`/`to`.

### GET `/api/medecins/me/stats`
- **Rôle** : `MEDECIN`.
- **Réponse** : mêmes indicateurs que ci-dessus mais limités au médecin connecté.

---

## Notifications (`/api/notifications`)

### GET `/api/notifications/preferences/me`
- **Rôles** : `PATIENT`, `MEDECIN`, `ADMIN`.
- **Réponse** : préférences courantes (email/SMS/push, rappel automatique).

### PUT `/api/notifications/preferences/me`
- **Body** : `{ "emailEnabled": true, "smsEnabled": false, "pushEnabled": false, "rappelAutomatique": true }`.

### POST `/api/notifications/rappels`
- **Rôles** : `ADMIN`, `MEDECIN`, `PATIENT` (uniquement pour soi).
- **Body** :
  ```json
  {
    "rendezVousId": "67a1...",
    "destinataireId": "777ab...",
    "canal": "EMAIL",
    "dateEnvoi": "2024-10-14T07:30:00Z",
    "message": "Rappel : rendez-vous demain à 9h"
  }
  ```
- **Réponse 201** : `NotificationPlanifieeResponse` (statut `PLANIFIEE`).

### GET `/api/notifications`
- **Rôle** : `ADMIN`.
- **Objet** : lister les notifications planifiées (paginées).

### PATCH `/api/notifications/{id}/etat`
- **Rôle** : `ADMIN`.
- **Body** : `{ "succes": true }` (optionnel, défaut `true`). Met à jour le statut (`ENVOYEE`/`ECHEC`).

### POST `/api/notifications/rappels/execute`
- **Rôle** : `ADMIN`.
- **Effet** : exécute le « scheduler » manuel : toutes les notifications planifiées avant `now` passent à `ENVOYEE`.

---

## Pré-diagnostic IA (`/api/prediagnostic`)

### POST `/api/prediagnostic`
- **Rôles** : `PATIENT`, `MEDECIN`, `ADMIN`.
- **Body** :
  ```json
  {
    "symptomes": ["fièvre", "maux de tête"],
    "contexte": "Depuis 48h"
  }
  ```
- **Réponse 200** :
  ```json
  {
    "conclusion": "Symptômes légers détectés. Consultez votre médecin si cela persiste.",
    "recommandations": [
      "Surveillez votre température toutes les 4 heures",
      "Ce pré-diagnostic ne remplace pas un avis médical. En cas de doute, consultez."
    ]
  }
  ```
- **Erreurs** : `400` si liste vide.

---

## Matrice des permissions détaillée

| Ressource | ADMIN | MEDECIN | PATIENT |
|-----------|-------|---------|---------|
| `/api/medecins` | Lecture | Lecture | Lecture |
| `/api/medecins/{id}/disponibilites` | CRUD | CRUD (sur soi) | Lecture |
| `/api/rdv` | CRUD complet | CRUD sur ses RDV | CRUD sur ses RDV |
| `/api/consultations` | Lecture globale / création | Lecture + création | Lecture (consultations liées) |
| `/api/prescriptions` | Lecture globale / création | Lecture + création | Lecture (prescriptions liées) |
| `/api/admin/stats` | Lecture + export | — | — |
| `/api/medecins/me/stats` | — | Lecture | — |
| `/api/notifications` | Pilotage complet | Préférences perso + planification patient | Préférences perso + planification perso |
| `/api/prediagnostic` | Utilisation | Utilisation | Utilisation |

Tous les endpoints renvoient des erreurs JSON `{ "message": "…" }`. Les validations remontent des erreurs détaillées `{ "message": "Requête invalide", "erreurs": { "champ": "raison" } }` avec des messages en français.

