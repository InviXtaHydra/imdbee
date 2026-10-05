package com.imdbee.movie;

import com.imdbee.movie.MovieDtos.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

/**
 * Small built-in catalog used when no TMDB key is configured, so the app can be
 * tried without any setup. Ids match the real TMDB ids, so reviews keep working
 * after you add a key. There are no poster images; the frontend draws placeholders.
 */
public class DemoMovieProvider implements MovieProvider {

    private static final List<Genre> GENRES = List.of(
            new Genre(28, "Actie"), new Genre(12, "Avontuur"), new Genre(16, "Animatie"),
            new Genre(35, "Komedie"), new Genre(80, "Misdaad"), new Genre(18, "Drama"),
            new Genre(14, "Fantasy"), new Genre(27, "Horror"), new Genre(9648, "Mysterie"),
            new Genre(10749, "Romantiek"), new Genre(878, "Sciencefiction"), new Genre(53, "Thriller"),
            new Genre(36, "Historisch"), new Genre(10402, "Muziek"), new Genre(10751, "Familie"));

    private record Demo(long id, String title, String tagline, String overview, String date, int runtime,
                        double score, List<Integer> genres, List<String[]> cast) {
    }

    private static String[] c(String name, String role) {
        return new String[]{name, role};
    }

    private static final List<Demo> MOVIES = List.of(
            new Demo(27205, "Inception", "Je geest is de plaats van de misdaad.",
                    "Dom Cobb steelt geheimen uit de dromen van anderen. Voor één laatste klus moet hij het omgekeerde doen: een idee planten.",
                    "2010-07-16", 148, 8.4, List.of(28, 878, 12),
                    List.of(c("Leonardo DiCaprio", "Cobb"), c("Joseph Gordon-Levitt", "Arthur"), c("Elliot Page", "Ariadne"), c("Tom Hardy", "Eames"))),
            new Demo(155, "The Dark Knight", "Welkom in een wereld zonder regels.",
                    "Batman, inspecteur Gordon en officier van justitie Harvey Dent nemen het op tegen de Joker, die Gotham in chaos wil storten.",
                    "2008-07-18", 152, 8.5, List.of(18, 28, 80, 53),
                    List.of(c("Christian Bale", "Bruce Wayne"), c("Heath Ledger", "Joker"), c("Aaron Eckhart", "Harvey Dent"), c("Gary Oldman", "James Gordon"))),
            new Demo(157336, "Interstellar", "De mensheid werd op aarde geboren. Het was nooit de bedoeling dat ze hier zou sterven.",
                    "Een team ontdekkingsreizigers reist door een wormgat in de ruimte, op zoek naar een nieuwe thuis voor de mensheid.",
                    "2014-11-07", 169, 8.4, List.of(12, 18, 878),
                    List.of(c("Matthew McConaughey", "Cooper"), c("Anne Hathaway", "Brand"), c("Jessica Chastain", "Murph"), c("Michael Caine", "Professor Brand"))),
            new Demo(603, "The Matrix", "Welkom in de echte wereld.",
                    "Hacker Neo ontdekt dat de werkelijkheid een simulatie is, gebouwd door machines, en sluit zich aan bij het verzet.",
                    "1999-03-31", 136, 8.2, List.of(28, 878),
                    List.of(c("Keanu Reeves", "Neo"), c("Laurence Fishburne", "Morpheus"), c("Carrie-Anne Moss", "Trinity"), c("Hugo Weaving", "Agent Smith"))),
            new Demo(680, "Pulp Fiction", null,
                    "De levens van twee huurmoordenaars, een bokser, een gangster en zijn vrouw raken op onverwachte manieren met elkaar verweven.",
                    "1994-10-14", 154, 8.5, List.of(53, 80),
                    List.of(c("John Travolta", "Vincent Vega"), c("Samuel L. Jackson", "Jules Winnfield"), c("Uma Thurman", "Mia Wallace"), c("Bruce Willis", "Butch Coolidge"))),
            new Demo(278, "The Shawshank Redemption", "Angst houdt je gevangen. Hoop maakt je vrij.",
                    "Bankier Andy Dufresne wordt onterecht veroordeeld en bouwt in de gevangenis een onwaarschijnlijke vriendschap op.",
                    "1994-09-23", 142, 8.7, List.of(18, 80),
                    List.of(c("Tim Robbins", "Andy Dufresne"), c("Morgan Freeman", "Red"), c("Bob Gunton", "Warden Norton"))),
            new Demo(238, "The Godfather", null,
                    "De ouder wordende patriarch van een maffiafamilie draagt de leiding over aan zijn jongste zoon, die dat nooit wilde.",
                    "1972-03-24", 175, 8.7, List.of(18, 80),
                    List.of(c("Marlon Brando", "Vito Corleone"), c("Al Pacino", "Michael Corleone"), c("James Caan", "Sonny Corleone"))),
            new Demo(496243, "Parasite", null,
                    "De arme familie Kim nestelt zich één voor één in het leven van de rijke familie Park, met onvoorziene gevolgen.",
                    "2019-05-30", 133, 8.5, List.of(35, 53, 18),
                    List.of(c("Song Kang-ho", "Kim Ki-taek"), c("Choi Woo-shik", "Kim Ki-woo"), c("Park So-dam", "Kim Ki-jung"))),
            new Demo(129, "Spirited Away", null,
                    "De tienjarige Chihiro belandt in een wereld van geesten en moet werken in een badhuis om haar ouders te redden.",
                    "2001-07-20", 125, 8.5, List.of(16, 10751, 14),
                    List.of(c("Rumi Hiiragi", "Chihiro (stem)"), c("Miyu Irino", "Haku (stem)"))),
            new Demo(13, "Forrest Gump", "De wereld zal nooit meer dezelfde zijn.",
                    "Een man met een goed hart wordt zonder het te beseffen getuige van, en deelnemer aan, decennia Amerikaanse geschiedenis.",
                    "1994-07-06", 142, 8.5, List.of(35, 18, 10749),
                    List.of(c("Tom Hanks", "Forrest Gump"), c("Robin Wright", "Jenny Curran"), c("Gary Sinise", "Lt. Dan Taylor"))),
            new Demo(438631, "Dune", null,
                    "Paul Atreides reist met zijn familie naar Arrakis, de gevaarlijkste planeet van het universum en de enige bron van de kostbaarste stof.",
                    "2021-10-22", 155, 7.8, List.of(878, 12),
                    List.of(c("Timothée Chalamet", "Paul Atreides"), c("Rebecca Ferguson", "Lady Jessica"), c("Oscar Isaac", "Leto Atreides"), c("Zendaya", "Chani"))),
            new Demo(329, "Jurassic Park", null,
                    "Een rondleiding door een themapark met gekloonde dinosaurussen loopt uit de hand wanneer de beveiliging uitvalt.",
                    "1993-06-11", 127, 7.9, List.of(12, 878),
                    List.of(c("Sam Neill", "Alan Grant"), c("Laura Dern", "Ellie Sattler"), c("Jeff Goldblum", "Ian Malcolm"))),
            new Demo(98, "Gladiator", null,
                    "Een verraden Romeinse generaal wordt slaaf en vecht zich als gladiator een weg terug om wraak te nemen op de keizer.",
                    "2000-05-05", 155, 8.2, List.of(28, 18, 12),
                    List.of(c("Russell Crowe", "Maximus"), c("Joaquin Phoenix", "Commodus"), c("Connie Nielsen", "Lucilla"))),
            new Demo(244786, "Whiplash", null,
                    "Een jonge drummer op een prestigieus conservatorium wordt tot het uiterste gedreven door een meedogenloze docent.",
                    "2014-10-10", 107, 8.4, List.of(18, 10402),
                    List.of(c("Miles Teller", "Andrew Neiman"), c("J.K. Simmons", "Terence Fletcher"))),
            new Demo(313369, "La La Land", null,
                    "Een jazzpianist en een aspirant-actrice worden verliefd in Los Angeles, terwijl hun dromen hen uit elkaar dreigen te drijven.",
                    "2016-12-09", 128, 7.9, List.of(35, 18, 10749, 10402),
                    List.of(c("Ryan Gosling", "Sebastian"), c("Emma Stone", "Mia"))),
            new Demo(862, "Toy Story", null,
                    "Cowboypop Woody voelt zich bedreigd wanneer de glimmende ruimtevaarder Buzz Lightyear zijn plek als favoriet speelgoed inneemt.",
                    "1995-11-22", 81, 8.0, List.of(16, 12, 10751, 35),
                    List.of(c("Tom Hanks", "Woody (stem)"), c("Tim Allen", "Buzz Lightyear (stem)"))),
            new Demo(105, "Back to the Future", null,
                    "Tiener Marty McFly reist per ongeluk terug naar 1955 en moet ervoor zorgen dat zijn ouders toch verliefd worden.",
                    "1985-07-03", 116, 8.3, List.of(12, 35, 878),
                    List.of(c("Michael J. Fox", "Marty McFly"), c("Christopher Lloyd", "Doc Brown"))),
            new Demo(419430, "Get Out", null,
                    "Chris ontmoet de familie van zijn vriendin en ontdekt dat er achter hun gastvrijheid iets verontrustends schuilgaat.",
                    "2017-02-24", 104, 7.6, List.of(9648, 53, 27),
                    List.of(c("Daniel Kaluuya", "Chris Washington"), c("Allison Williams", "Rose Armitage"))),
            new Demo(76341, "Mad Max: Fury Road", null,
                    "In een woestijnwereld vlucht Furiosa met een groep vrouwen voor een tiran, met de zwijgzame Max aan haar zijde.",
                    "2015-05-15", 120, 7.6, List.of(28, 12, 878),
                    List.of(c("Tom Hardy", "Max Rockatansky"), c("Charlize Theron", "Furiosa"))),
            new Demo(324857, "Spider-Man: Into the Spider-Verse", null,
                    "Tiener Miles Morales wordt Spider-Man en ontmoet Spider-mensen uit andere dimensies.",
                    "2018-12-14", 117, 8.4, List.of(28, 12, 16, 878),
                    List.of(c("Shameik Moore", "Miles Morales (stem)"), c("Jake Johnson", "Peter B. Parker (stem)"), c("Hailee Steinfeld", "Gwen Stacy (stem)"))),
            new Demo(872585, "Oppenheimer", null,
                    "Het verhaal van natuurkundige J. Robert Oppenheimer en zijn rol in de ontwikkeling van de atoombom.",
                    "2023-07-21", 180, 8.1, List.of(18, 36),
                    List.of(c("Cillian Murphy", "J. Robert Oppenheimer"), c("Emily Blunt", "Kitty Oppenheimer"), c("Robert Downey Jr.", "Lewis Strauss")))
    );

    @Override
    public List<MovieRow> homeRows() {
        return List.of(
                row("trending", "Trending deze week", ids(872585, 438631, 496243, 157336, 324857, 76341, 27205, 313369)),
                row("top_rated", "Best beoordeeld", sortedBy(Comparator.comparingDouble(Demo::score).reversed()).limit(10).toList()),
                row("scifi", "Sciencefiction", byGenre(878)),
                row("drama", "Drama", byGenre(18)),
                row("animation", "Animatie & familie", Stream.concat(byGenre(16).stream(), byGenre(10751).stream()).distinct().toList()));
    }

    @Override
    public MoviePage search(String query, Integer genreId, Double minRating, int page) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<MovieSummary> results = sortedBy(Comparator.comparingDouble(Demo::score).reversed())
                .filter(m -> q.isEmpty() || m.title().toLowerCase(Locale.ROOT).contains(q))
                .filter(m -> genreId == null || m.genres().contains(genreId))
                .filter(m -> minRating == null || m.score() >= minRating)
                .map(DemoMovieProvider::toSummary)
                .toList();
        return new MoviePage(1, 1, results.size(), results);
    }

    @Override
    public List<Genre> genres() {
        return GENRES;
    }

    @Override
    public Optional<MovieDetail> details(long id) {
        return MOVIES.stream().filter(m -> m.id() == id).findFirst().map(m -> new MovieDetail(
                m.id(), m.title(), m.tagline(), m.overview(), null, null, LocalDate.parse(m.date()), m.runtime(),
                m.score(), GENRES.stream().filter(g -> m.genres().contains(g.id())).toList(),
                m.cast().stream().map(x -> new CastMember(x[0], x[1], null)).toList(), null, null));
    }

    @Override
    public boolean isDemo() {
        return true;
    }

    private static MovieRow row(String key, String title, List<Demo> movies) {
        return new MovieRow(key, title, movies.stream().map(DemoMovieProvider::toSummary).toList());
    }

    private static List<Demo> ids(long... ids) {
        return Arrays.stream(ids).mapToObj(id -> MOVIES.stream().filter(m -> m.id() == id).findFirst().orElseThrow()).toList();
    }

    private static List<Demo> byGenre(int genre) {
        return MOVIES.stream().filter(m -> m.genres().contains(genre)).toList();
    }

    private static Stream<Demo> sortedBy(Comparator<Demo> order) {
        return MOVIES.stream().sorted(order);
    }

    private static MovieSummary toSummary(Demo m) {
        return new MovieSummary(m.id(), m.title(), m.overview(), null, null, LocalDate.parse(m.date()), m.score(), m.genres());
    }
}
