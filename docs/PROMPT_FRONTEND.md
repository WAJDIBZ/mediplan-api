# Prompt exécutoire Frontend — MediPlan Web

Tu es chargé(e) de développer l’interface Next.js (mediplan-web) en respectant strictement le backend stabilisé. Voici le cahier des charges front à appliquer sans renégociation.

## Hypothèses backend (à utiliser telles quelles)

- Base API : `https://mediplan-api-1b2c88de81dd.herokuapp.com`.
- Authentification JWT via `/api/auth/login` (POST), refresh `/api/auth/refresh?token=...` (POST), inscription patients/médecins (`/api/auth/register/*`).
- Header obligatoire après login : `Authorization: Bearer <token>`.
- `/api/auth/me` sert de ping pour vérifier la session (200 si valide, 401/403 sinon).
- Routes d’administration : `/api/admin/users` (GET liste paginée + filtres `q`, `role`, `active`, `provider`, `page`, `size`, `sort`), `/api/admin/users/{id}` (GET/PATCH/DELETE), `/api/admin/users/{id}/deactivate|reactivate|role|reset-password`, `/api/admin/users/bulk/{action}`, `/api/admin/users/export` (POST → CSV `text/csv`).
- Recherche médecins : GET `/api/medecins?q=&specialite=&ville=` (paginé) ; les disponibilités se gèrent via `/api/medecins/{medecinId}/disponibilites` (GET/POST/PUT/DELETE`).
- Rendez-vous : `/api/rdv` (POST création, GET liste filtrable `medecinId`, `patientId`, `from`, `to`, tri), `/api/rdv/{id}` (PUT mise à jour), `/api/rdv/{id}/statut` (PATCH), suppression logique via DELETE.
- Profil patient : `/api/patients/me` (GET/PUT) pour coordonnées, assurance, contact d’urgence.
- Consultations & prescriptions : `/api/consultations` (POST + GET paginé + GET détail) et `/api/prescriptions` (POST + GET + GET détail).
- Statistiques : `/api/admin/stats?from=&to=` (dashboard global) + `/api/admin/stats/export` (CSV) et `/api/medecins/me/stats` (vue médecin).
- Notifications : `/api/notifications/preferences/me` (GET/PUT), `/api/notifications/rappels` (POST planification), `/api/notifications` (GET admin), `/api/notifications/{id}/etat` (PATCH), `/api/notifications/rappels/execute` (POST).
- Pré-diagnostic : POST `/api/prediagnostic` avec `symptomes[]` + `contexte` (optionnel) → réponse JSON (conclusion + recommandations).
- Validation métier : spécialité & numéro de licence obligatoires pour rôle `MEDECIN`, désactivation privilégiée vs suppression, messages d’erreur FR retournés par l’API (`message`, `erreurs`).
- Statuts HTTP exploités côté UI : `400` validation, `401` non authentifié, `403` non autorisé ou compte désactivé, `404` ressource manquante, `409` conflit, `422` règle métier, `500` erreur inattendue.

## Nettoyage / Préparation du repo frontend

Supprimer ou archiver :
- Pages/demo obsolètes, composants non utilisés, mocks backend remplacés par appels API réels.
- Code de suppression dure : toujours préférer l’activation/désactivation.
- Toute logique OAuth manuelle non alignée avec le flow `/oauth2/authorization/{provider}` (prévoir placeholder pour intégration future, sans l’implémenter).

Mettre en place :
- Gestion centralisée des appels API (ex : hook `useApiClient` avec interceptors refresh token).
- Store d’authentification (React context/zustand) pour conserver `accessToken`, `refreshToken`, rôle utilisateur et identifiants (`userId`, `role`) nécessaires pour les filtres `/api/rdv`.
- i18n inutile : tout en français, mais prévoir fichiers de messages pour mutualiser.

## Règles UI

- Langue : français, labels explicites, titres de pages cohérents avec navigation.
- Accessibilité : balises `<label>` associées, contraste, navigation clavier.
- États de chargement/squelette sur listes et formulaires.
- Gestion erreurs : toasts/banners selon statut
  - `401` : redirection page login + toast « Session expirée ».
  - `403` : toast « Accès refusé ».
  - `422`/`409`/`400` : afficher `message` + détails champ dans formulaire.
  - `500` : toast générique « Une erreur inattendue est survenue ».
- Pagination : composants table/liste avec contrôles `page`, `size`, tri sur colonnes autorisées.
- Filtres utilisateurs : champ recherche (debounce 300ms), filtres rôle (enum), statut (`actif`), fournisseur (`provider`).
- Agenda médecin : vue calendrier semaine/jour (composant client) couplée à `/api/rdv` et `/api/medecins/{id}/disponibilites`, édition via modales.
- Prise de rendez-vous patient : wizard en 3 étapes (choix médecin → choix créneau → confirmation) avec récapitulatif.
- Espace patient : onglets « Rendez-vous », « Consultations », « Prescriptions » alimentés par les endpoints dédiés.
- Tableau de bord admin : cartes KPI + graphique simple (barres ou donut) basés sur `/api/admin/stats`, export CSV accessible via bouton.
- Centre de notifications : liste administrateur (`/api/notifications`) avec actions « Marquer envoyé » / « Marquer échec », préférences utilisateur accessibles depuis header.
- Module pré-diagnostic : formulaire accessible aux patients, affiche conclusion + message légal « Ce pré-diagnostic ne remplace pas un avis médical ».
- Désactivation/activation : modal de confirmation, badges d’état (Actif/Inactif), actions idempotentes.
- Export CSV : bouton qui déclenche POST `/api/admin/users/export`, feedback téléchargement.
- Formulaires création/mise à jour utilisateur : séparateurs sections (identité, coordonnées, patient, médecin). Champs conditionnels s’affichent selon rôle sélectionné.
- Responsive : tableau admin en grille sur mobile, actions regroupées dans menu.


## Structure recommandée

```
src/
  app/
    layout.tsx
    auth/login/page.tsx
    admin/
      users/
        page.tsx (liste)
        [id]/page.tsx (détails + édition)
      stats/page.tsx (indicateurs + export)
      notifications/page.tsx (pilotage rappels)
    medecin/
      agenda/page.tsx (agenda + disponibilités)
      stats/page.tsx (KPIs personnels)
    patient/
      profil/page.tsx
      rendez-vous/page.tsx
      consultations/page.tsx
      prescriptions/page.tsx
      prediagnostic/page.tsx
    rendez-vous/nouveau/page.tsx (assistant prise RDV)
  components/
    tables/UserTable.tsx
    tables/RendezVousTable.tsx
    tables/NotificationTable.tsx
    forms/AdminUserForm.tsx
    forms/DisponibiliteForm.tsx
    forms/RendezVousForm.tsx
    forms/ConsultationForm.tsx
    forms/PrescriptionForm.tsx
    analytics/StatCard.tsx
    analytics/TimeFilter.tsx
    notifications/PreferenceSwitches.tsx
    ui/
      Button.tsx, Modal.tsx, Toast.tsx, Badge.tsx, Tabs.tsx, Pagination.tsx
  hooks/
    useAuth.ts
    useApiClient.ts
    useDebouncedValue.ts
  libs/
    authStorage.ts
    validators.ts
    date.ts (helpers fuseaux horaires)
```

## Scénarios de tests manuels

### Rôle Admin
1. **Connexion** : login admin → redirection tableau utilisateurs.
2. **Filtrage** : appliquer recherche texte + filtre rôle + tri `createdAt desc` → vérifier requêtes envoyées.
3. **Activation** : désactiver un utilisateur → badge passe à « Inactif », action inverse réactive sans erreur.
4. **Changement de rôle** : passer un patient en médecin sans spécialité → toast erreur `422` ; re-soumettre avec spécialité/licence → succès.
5. **Export utilisateurs** : POST `/api/admin/users/export` → téléchargement `users.csv`.
6. **Dashboard statistiques** : choisir une période → cartes KPI et graphique se mettent à jour ; exporter CSV et contrôler contenu.
7. **Notifications** : planifier un rappel, vérifier qu’il apparaît dans la liste, le marquer envoyé (`PATCH /etat`) → statut `ENVOYEE`.

### Rôle Médecin
1. **Agenda** : affichage initial des rendez-vous + disponibilités ; création d’un créneau → liste mise à jour ; suppression → créneau retiré.
2. **Planification RDV** : tenter un créneau occupé → toast `409`, puis créneau libre → RDV créé et visible.
3. **Consultations** : créer une consultation associée à un RDV → section « Consultations » actualisée.
4. **Statistiques** : consulter `/medecin/stats` → indicateurs cohérents avec jeux de données.

### Rôle Patient
1. **Onboarding** : après inscription, compléter profil (PUT `/api/patients/me`) avec validations.
2. **Recherche médecin** : utiliser filtres spécialité/ville, lancer prise de RDV → confirmation visuelle et entrée dans historique.
3. **Historique** : distinguer RDV futurs/passés, annuler un RDV → statut `ANNULE` et disparition des actions.
4. **Consultations & prescriptions** : vérifier affichage séparé et détails cohérents.
5. **Pré-diagnostic** : soumettre symptômes → afficher conclusion + disclaimer « ne remplace pas un avis médical ».

## Contraintes techniques

- Next.js 14 avec app router, React Server Components pour pages, composants client pour formulaires.
- Styling : Tailwind CSS (ou système en place) avec design système cohérent (primary, warning, danger).
- Tests : au minimum tests unitaires sur hooks (`useApiClient`, `useAuth`), tests e2e Playwright prioritaires sur login + liste utilisateurs.
- CI : scripts lint (`npm run lint`) et tests (`npm run test`) doivent passer.
- Pas de dépendances inutiles (tableur, chart) tant que sprint concerné non abordé.

## Livraison

- Branch naming : `feature/<topic>-frontend`.
- PR message : résumé FR (fonctionnalités, tests). Inclure capture écran pour modifications visibles (utiliser `browser_container` si dispo).
- Respecter ce prompt : toute divergence doit être validée explicitement.
