package com.yzioual.database;

import com.yzioual.model.artifact.Artifact;
import com.yzioual.model.artifact.ArtifactType;
import com.yzioual.model.hero.Hero;
import com.yzioual.model.hero.HeroType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {
    private static final String DEFAULT_URL = "jdbc:sqlite:swingy.db";
    private final String dbUrl;

    public DatabaseManager() {
        this(DEFAULT_URL);
    }

    public DatabaseManager(String dbUrl) {
        this.dbUrl = dbUrl;
        initDatabase();
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

    public void saveHero(Hero hero) {
        if (hero == null) return;
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
            System.err.println("Error saving hero: " + e.getMessage());
        }
    }

    public List<Hero> loadAllHeroes() {
        List<Hero> list = new ArrayList<>();
        String sql = "SELECT * FROM heroes ORDER BY id DESC;";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapRowToHero(rs).ifPresent(list::add);
            }
        } catch (SQLException e) {
            System.err.println("Error loading heroes: " + e.getMessage());
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
            System.err.println("Error loading hero: " + e.getMessage());
        }
        return Optional.empty();
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
