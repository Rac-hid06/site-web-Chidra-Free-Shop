# Backend Spring Boot

## Déploiement sur Render

Crée un **Web Service** depuis le dépôt GitHub, avec ces paramètres :

- Root Directory : `backend-springboot`
- Runtime : Docker
- Dockerfile Path : `./Dockerfile`

Ajoute une variable d’environnement `ADMIN_API_TOKEN` avec une valeur secrète
générée pour l’administration. Ne la publie pas dans GitHub et ne la partage pas
dans le chat. Les routes d’administration restent désactivées tant que cette
variable n’est pas définie.

Le backend utilise actuellement une base H2 locale. Les données peuvent être
perdues si l’instance Render redémarre ou est redéployée.

## Développement local

Dans PowerShell, définis un jeton local avant de démarrer Spring Boot :

```powershell
$env:ADMIN_API_TOKEN = "un-jeton-local"
mvn spring-boot:run
```
