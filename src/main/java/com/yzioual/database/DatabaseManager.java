package com.yzioual.database;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.artifact.ArtifactType;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:sqlite:swingy.db";
    private static final String DEFAULT_FILE = "heroes.txt";

    private final String dbUrl;
    private final Path filePath;

    public DatabaseManager() {
        this(DEFAULT_URL, DEFAULT_FILE);
    }

    public DatabaseManager(String dbUrl) {
        this(dbUrl, DEFAULT_FILE);
    }

    public DatabaseManager(String dbUrl, String filePath) {
        this.dbUrl = dbUrl;
        this.filePath = Paths.get(filePath);
        initDatabase();
        syncStorage();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void initDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS heroes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                hero_type TEXT NOT NULL,
                level INTEGER NOT NULL,
                experience INTEGER NOT NULL,
                weapon_name TEXT,
                weapon_bonus INTEGER,
                armor_name TEXT,
                armor_bonus INTEGER,
                helm_name TEXT,
                helm_bonus INTEGER
            );
        """;
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Database init error: " + e.getMessage());
        }
    }

    private void syncStorage() {
        try {
            List<Hero> dbHeroes = loadHeroesFromDatabase();
            List<Hero> fileHeroes = loadHeroesFromFile();

            boolean fileIsNewer = false;
            if (Files.exists(filePath)) {
                Path dbPath = Paths.get("swingy.db");
                if (!Files.exists(dbPath) || Files.getLastModifiedTime(filePath).toMillis() >= Files.getLastModifiedTime(dbPath).toMillis()) {
                    fileIsNewer = true;
                }
            }

            if (fileIsNewer && !fileHeroes.isEmpty()) {
                // External edits were made to text file, update database
                for (Hero fh : fileHeroes) {
                    saveHeroToDatabase(fh);
                }
            } else {
                // Import any heroes from file into DB
                for (Hero fh : fileHeroes) {
                    boolean inDb = dbHeroes.stream().anyMatch(h -> h.getName().equalsIgnoreCase(fh.getName()));
                    if (!inDb) {
                        saveHeroToDatabase(fh);
                    }
                }
            }

            // Refresh DB list after import
            dbHeroes = loadHeroesFromDatabase();

            // Ensure text file contains all DB heroes
            for (Hero dh : dbHeroes) {
                saveHeroToFile(dh);
            }
        } catch (Exception ignored) {
        }
    }

    public void saveHero(Hero hero) {
        if (hero == null) return;
        saveHeroToDatabase(hero);
        saveHeroToFile(hero);
    }

    private void saveHeroToDatabase(Hero hero) {
        String sql = """
            INSERT INTO heroes (name, hero_type, level, experience,
                                weapon_name, weapon_bonus,
                                armor_name, armor_bonus,
                                helm_name, helm_bonus)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(name) DO UPDATE SET
                hero_type = excluded.hero_type,
                level = excluded.level,
                experience = excluded.experience,
                weapon_name = excluded.weapon_name,
                weapon_bonus = excluded.weapon_bonus,
                armor_name = excluded.armor_name,
                armor_bonus = excluded.armor_bonus,
                helm_name = excluded.helm_name,
                helm_bonus = excluded.helm_bonus;
        """;
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hero.getName());
            ps.setString(2, hero.getHeroType().name());
            ps.setInt(3, hero.getLevel());
            ps.setInt(4, hero.getExperience());

            if (hero.getWeapon() != null) {
                ps.setString(5, hero.getWeapon().name());
                ps.setInt(6, hero.getWeapon().bonus());
            } else {
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.INTEGER);
            }

            if (hero.getArmor() != null) {
                ps.setString(7, hero.getArmor().name());
                ps.setInt(8, hero.getArmor().bonus());
            } else {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.INTEGER);
            }

            if (hero.getHelm() != null) {
                ps.setString(9, hero.getHelm().name());
                ps.setInt(10, hero.getHelm().bonus());
            } else {
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.INTEGER);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving hero to DB: " + e.getMessage());
        }
    }

    private synchronized void saveHeroToFile(Hero hero) {
        try {
            List<String> lines = new ArrayList<>();
            boolean replaced = false;
            String newEntry = serializeHeroToLine(hero);

            if (Files.exists(filePath)) {
                List<String> existing = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                for (String line : existing) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        lines.add(line);
                        continue;
                    }
                    String[] parts = line.split("\\|");
                    if (parts.length > 0 && parts[0].equalsIgnoreCase(hero.getName())) {
                        lines.add(newEntry);
                        replaced = true;
                    } else {
                        lines.add(line);
                    }
                }
            }
            if (!replaced) {
                lines.add(newEntry);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving hero to text file: " + e.getMessage());
        }
    }

    private String serializeHeroToLine(Hero hero) {
        String weaponStr = hero.getWeapon() != null
                ? hero.getWeapon().name() + ":" + hero.getWeapon().bonus() : "None:0";
        String armorStr = hero.getArmor() != null
                ? hero.getArmor().name() + ":" + hero.getArmor().bonus() : "None:0";
        String helmStr = hero.getHelm() != null
                ? hero.getHelm().name() + ":" + hero.getHelm().bonus() : "None:0";

        return String.join("|",
                hero.getName(),
                hero.getHeroType().name(),
                String.valueOf(hero.getLevel()),
                String.valueOf(hero.getExperience()),
                weaponStr,
                armorStr,
                helmStr
        );
    }

    public List<Hero> loadAllHeroes() {
        syncStorage();
        List<Hero> list = loadHeroesFromDatabase();
        if (!list.isEmpty()) {
            return list;
        }
        return loadHeroesFromFile();
    }

    private List<Hero> loadHeroesFromDatabase() {
        List<Hero> list = new ArrayList<>();
        String sql = "SELECT * FROM heroes ORDER BY id DESC;";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapRowToHero(rs).ifPresent(list::add);
            }
        } catch (SQLException e) {
            System.err.println("Error loading heroes from DB: " + e.getMessage());
        }
        return list;
    }

    public List<Hero> loadHeroesFromFile() {
        List<Hero> list = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return list;
        }
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                parseLineToHero(line).ifPresent(list::add);
            }
        } catch (IOException e) {
            System.err.println("Error loading heroes from text file: " + e.getMessage());
        }
        return list;
    }

    public Optional<Hero> loadHeroByName(String name) {
        String sql = "SELECT * FROM heroes WHERE name = ?;";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToHero(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading hero from DB: " + e.getMessage());
        }
        // Fallback check from text file
        return loadHeroesFromFile().stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    private Optional<Hero> parseLineToHero(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 4) return Optional.empty();

            String name = parts[0].trim();
            String typeStr = parts[1].trim();
            HeroType heroType;
            try {
                heroType = HeroType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                heroType = HeroType.getListOfHeroTypes().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(typeStr))
                        .findFirst()
                        .orElse(HeroType.SPEARFISHER);
            }

            int level = Integer.parseInt(parts[2].trim());
            int experience = Integer.parseInt(parts[3].trim());

            Hero.HeroBuilder builder = new Hero.HeroBuilder()
                    .setName(name)
                    .setHeroType(heroType)
                    .setLevel(level)
                    .setExperience(experience);

            if (parts.length > 4) {
                parseArtifactItem(parts[4], ArtifactType.WEAPON).ifPresent(builder::setWeapon);
            }
            if (parts.length > 5) {
                parseArtifactItem(parts[5], ArtifactType.ARMOR).ifPresent(builder::setArmor);
            }
            if (parts.length > 6) {
                parseArtifactItem(parts[6], ArtifactType.HELM).ifPresent(builder::setHelm);
            }

            return Optional.of(builder.build());
        } catch (Exception e) {
            System.err.println("Failed to parse hero line: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<Artifact> parseArtifactItem(String raw, ArtifactType type) {
        raw = raw.trim();
        if (raw.isEmpty() || raw.equalsIgnoreCase("None") || raw.startsWith("None:")) {
            return Optional.empty();
        }
        int colon = raw.lastIndexOf(':');
        if (colon == -1) {
            return Optional.empty();
        }
        String name = raw.substring(0, colon).trim();
        int bonus = Integer.parseInt(raw.substring(colon + 1).trim());
        return Optional.of(new Artifact(name, type, bonus));
    }

    private Optional<Hero> mapRowToHero(ResultSet rs) {
        try {
            String name = rs.getString("name");
            String typeStr = rs.getString("hero_type");
            HeroType heroType;
            try {
                heroType = HeroType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                heroType = HeroType.getListOfHeroTypes().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(typeStr))
                        .findFirst()
                        .orElse(HeroType.SPEARFISHER);
            }

            int level = rs.getInt("level");
            int experience = rs.getInt("experience");

            Hero.HeroBuilder builder = new Hero.HeroBuilder()
                    .setName(name)
                    .setHeroType(heroType)
                    .setLevel(level)
                    .setExperience(experience);

            String weaponName = rs.getString("weapon_name");
            if (weaponName != null) {
                int bonus = rs.getInt("weapon_bonus");
                builder.setWeapon(new Artifact(weaponName, ArtifactType.WEAPON, bonus));
            }

            String armorName = rs.getString("armor_name");
            if (armorName != null) {
                int bonus = rs.getInt("armor_bonus");
                builder.setArmor(new Artifact(armorName, ArtifactType.ARMOR, bonus));
            }

            String helmName = rs.getString("helm_name");
            if (helmName != null) {
                int bonus = rs.getInt("helm_bonus");
                builder.setHelm(new Artifact(helmName, ArtifactType.HELM, bonus));
            }

            return Optional.of(builder.build());
        } catch (Exception e) {
            System.err.println("Failed to parse hero row: " + e.getMessage());
            return Optional.empty();
        }
    }
}
