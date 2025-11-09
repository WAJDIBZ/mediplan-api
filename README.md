# MediPlan API

API Spring Boot pour la gestion des utilisateurs (patients, médecins, administrateurs) avec authentification JWT et MongoDB.

## Démarrage rapide

```bash
./mvnw spring-boot:run
```

Variables d’environnement clés :

| Variable | Description |
|----------|-------------|
| `MONGO_URI` | Chaîne de connexion MongoDB. |
| `JWT_ACCESS_SECRET` / `JWT_REFRESH_SECRET` | Secrets HMAC (>= 32 chars). |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (séparées par des virgules). |
| `FRONT_REDIRECT_URL` | URL de redirection OAuth2 (mode query/cookies). |

## Ressources

- [Documentation API backend](docs/API_BACKEND.md)
- [Plan produit sur 5 sprints](docs/PLAN_SPRINTS.md)
- [Prompt exécutoire frontend](docs/PROMPT_FRONTEND.md)

## Tests

```bash
./mvnw test
```

Les tests utilisent MongoDB embarqué (Flapdoodle) et peuvent être exécutés sans configuration supplémentaire.
