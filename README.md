<img src="frontend/public/brand/imdbee-logo.png" alt="IMDBee" width="360">

Een filmcatalogus in Netflix-stijl met accounts, zoeken en filteren, reviews en trailers.

| Laag | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Security (JWT + BCrypt), Spring Data JPA |
| Frontend | Vue 3 (Composition API), Vue Router, Pinia, Tailwind CSS v4, Axios |
| Database | H2 (standaard, bestand) of PostgreSQL (profiel `postgres`) |
| Filmdata | TMDB API, via de backend (de API-sleutel komt nooit in de browser) |

## Snel starten

Je hebt Java 21+ en Node 20+ nodig. Open twee terminals.

**1. Backend** (http://localhost:8080)

```bash
cd backend
./mvnw spring-boot:run
```

**2. Frontend** (http://localhost:5174)

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5174, maak een account en schrijf je eerste review.

### Echte films met TMDB

De backend leest je TMDB-sleutel uit `backend/.env`. Dat bestand staat in `.gitignore` en komt dus nooit op GitHub.

```bash
cd backend
cp .env.example .env      # alleen de eerste keer
# open .env en vul TMDB_API_KEY in (de "API Read Access Token" van themoviedb.org)
```

Zonder sleutel draait de app in **demomodus**: ongeveer twintig bekende films zonder afbeeldingen en trailers. De demofilms gebruiken de echte TMDB-id's, dus reviews blijven bij de juiste film als je overschakelt.

Met een sleutel krijg je posters, achtergronden, foto's van de cast en **trailers** (YouTube, afgespeeld via `youtube-nocookie.com`).

## Configuratie (omgevingsvariabelen)

| Variabele | Standaard | Uitleg |
|---|---|---|
| `TMDB_API_KEY` | leeg (demomodus) | TMDB v4-token of v3-sleutel, bij voorkeur in `backend/.env` |
| `TMDB_LANGUAGE` | `nl-NL` | Taal van titels en beschrijvingen |
| `JWT_SECRET` | dev-waarde | **Verplicht aanpassen in productie**, minstens 32 tekens |
| `JWT_EXPIRATION_MINUTES` | `1440` | Geldigheid van een login (24 uur) |
| `SPRING_PROFILES_ACTIVE` | leeg (H2) | `postgres` om PostgreSQL te gebruiken |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | zie `application-postgres.properties` | PostgreSQL-verbinding |
| `CORS_ORIGINS` | `*` (alles toegestaan) | Toegestane frontend-origins, komma-gescheiden. Beperk dit in productie tot je eigen domein |

### PostgreSQL gebruiken

```bash
docker compose up -d                                   # start PostgreSQL (of gebruik je eigen installatie)
cd backend && SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

De H2-database staat in `backend/data/`. Verwijder die map om opnieuw te beginnen. De H2-console vind je op http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/imdbee`, gebruiker `sa`, leeg wachtwoord).

## Online zetten (Vercel + Render + Supabase)

| Onderdeel | Dienst | Configuratie |
|---|---|---|
| Frontend | Vercel | `frontend/vercel.json` stuurt `/api/*` door naar de backend |
| Backend | Render (Docker, gratis plan) | `render.yaml` + `backend/Dockerfile` |
| Database | Supabase (PostgreSQL) | profiel `postgres`, via de **Session pooler** |

**Supabase**
- Gebruik de *Session pooler*-verbinding (IPv4). De directe verbinding werkt niet vanaf Render.
- Row Level Security staat aan op `app_user`, `review` en `movie_data`, en `anon`/`authenticated` hebben geen rechten. Zo zijn de tabellen niet leesbaar via de publieke Supabase REST-API; de backend verbindt als `postgres` en heeft wel toegang. **Maak je later een nieuwe tabel, zet daar dan ook RLS aan.**

**Render:** *New → Blueprint* → kies deze repository. Vul daarna in:

| Variabele | Waarde |
|---|---|
| `DB_URL` | `jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DB_USERNAME` | `postgres.<project-ref>` |
| `DB_PASSWORD` | je Supabase-databasewachtwoord |
| `TMDB_API_KEY` | je TMDB-token |

`JWT_SECRET` wordt automatisch gegenereerd. Het gratis plan slaapt na 15 minuten zonder verkeer; de eerste aanvraag daarna duurt ongeveer een minuut. Daarom:

- de GitHub Action `.github/workflows/keep-alive.yml` roept elke 10 minuten de health check en een database-endpoint aan, zodat Render wakker blijft en Supabase het project niet pauzeert (één service die altijd aan staat past in de 750 gratis uren per maand);
- slaapt de backend toch, dan probeert de frontend het tot 90 seconden opnieuw (`src/services/api.js`) en toont hij "De server wordt wakker…" in plaats van een foutmelding.

**Vercel:** deploy de map `frontend`. Pas in `frontend/vercel.json` de backend-URL aan als je Render-service anders heet dan `imdbee-api`.

**Lokaal tegen Supabase draaien:** zet `DB_URL`, `DB_USERNAME` en `DB_PASSWORD` in `backend/.env` en start met `SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run`. Zonder dat profiel gebruikt de backend de lokale H2-database.

## API

| Methode | Pad | Login | Beschrijving |
|---|---|---|---|
| POST | `/api/auth/register` | nee | Account maken, geeft een JWT terug |
| POST | `/api/auth/login` | nee | Inloggen met gebruikersnaam of e-mail |
| GET | `/api/auth/me` | ja | Huidige gebruiker |
| GET | `/api/movies/home` | nee | Hero-film en rijen per categorie |
| GET | `/api/movies/search?query=&genre=&minRating=&page=` | nee | Zoeken en filteren |
| GET | `/api/movies/genres` | nee | Genrelijst |
| GET | `/api/movies/{id}` | nee | Details, cast, trailer en score van gebruikers |
| GET | `/api/reviews/movie/{movieId}` | nee | Alle reviews van een film |
| GET | `/api/reviews/me` | ja | Je eigen reviews |
| POST | `/api/reviews` | ja | Review plaatsen (`movieId`, `rating` 1–5, `content`) |
| PUT | `/api/reviews/{id}` | ja | Eigen review bewerken |
| DELETE | `/api/reviews/{id}` | ja | Eigen review verwijderen |

Stuur de token mee als `Authorization: Bearer <token>`. Elke gebruiker kan één review per film schrijven. Gebruikers met de rol `ADMIN` kunnen alle reviews bewerken en verwijderen (moderatie).

## Structuur

```
backend/src/main/java/com/imdbee/
  auth/        AuthController, AuthService, DTO's (register/login/me)
  security/    JwtService, JwtAuthenticationFilter, AuthUser, AppUserDetailsService
  config/      SecurityConfig (stateless, CORS, BCrypt)
  user/        User-entity, Role, UserRepository
  movie/       MovieData-entity (lokale cache), MovieService,
               TmdbMovieProvider (echte API), DemoMovieProvider (zonder sleutel)
  review/      Review-entity, ReviewService, ReviewController
  common/      ApiException, GlobalExceptionHandler (nette JSON-fouten)

frontend/src/
  services/    api.js (Axios + JWT-interceptor), auth-, movie-, reviewService
  stores/      auth.js, movies.js (Pinia)
  router/      routes en guards (ingelogd / alleen gasten)
  components/  AppLogo, TrailerModal, Navbar, HeroBanner, MovieRow, MovieCard, MovieGrid, PosterImage,
               StarRating, ReviewForm, ReviewList, ErrorState
  views/       Home, Browse, MovieDetail, Login, Register, Profile, NotFound
```

## Logo en huisstijl

Het logo staat in `frontend/public/brand/`:

- `imdbee-logo-original.png`: het originele logo
- `imdbee-logo.png`: uitgeknipt label met transparante hoeken (gebruikt in de app)
- `imdbee-icon.png`: de bij als vierkant icoon (favicon)

Kleuren (in `frontend/src/style.css`): zwart `#000000`, balk `#121212`, logogeel `#FBC50B`, links `#5799EF`. Lettertypes: Roboto voor tekst, Archivo voor titels.

## Volgende stappen (ideeën)

- Watchlist ("Mijn lijst") per gebruiker
- Refresh tokens in een httpOnly-cookie in plaats van localStorage
- Paginering voor reviews bij populaire films
- Integratietests voor de auth- en review-endpoints
