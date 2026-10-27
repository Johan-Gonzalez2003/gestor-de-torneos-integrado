package com.tournamentmanager.infrastructure.analytics;

import com.tournamentmanager.application.exception.AnalysisException;
import com.tournamentmanager.application.model.CategoryCount;
import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.model.DatasetSummary;
import com.tournamentmanager.application.port.out.AnalyticsPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Analiza registros JSON con DuckDB en memoria:
 * 1) estadísticas descriptivas (conteo, categorías distintas, promedio y suma del campo numérico);
 * 2) top 5 de categorías por frecuencia.
 * Los nombres de campo se validan con una expresión regular antes de usarse en SQL.
 */
@Component
public class DuckDbAnalyticsAdapter implements AnalyticsPort {

    private static final Pattern SAFE_FIELD = Pattern.compile("^[\\p{L}_][\\p{L}\\p{N}_ ]{0,99}$");
    private static final String JDBC_URL = "jdbc:duckdb:";

    @Override
    public DatasetSummary summarize(DatasetDefinition definition, String recordsJson) {
        String numeric = quote(definition.numericField());
        String category = quote(definition.categoryField());
        Path file = null;
        try {
            file = Files.createTempFile("opendata-", ".json");
            Files.writeString(file, recordsJson, StandardCharsets.UTF_8);
            String escapedPath = file.toAbsolutePath().toString().replace("'", "''");

            try (Connection connection = DriverManager.getConnection(JDBC_URL);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE records AS SELECT * FROM read_json_auto('" + escapedPath + "')");

                long rows;
                long distinct;
                Double average;
                Double sum;
                try (ResultSet rs = statement.executeQuery(
                        "SELECT COUNT(*), COUNT(DISTINCT CAST(" + category + " AS VARCHAR)), "
                                + "AVG(TRY_CAST(" + numeric + " AS DOUBLE)), "
                                + "SUM(TRY_CAST(" + numeric + " AS DOUBLE)) FROM records")) {
                    rs.next();
                    rows = rs.getLong(1);
                    distinct = rs.getLong(2);
                    average = nullableDouble(rs, 3);
                    sum = nullableDouble(rs, 4);
                }

                List<CategoryCount> top = new ArrayList<>();
                try (ResultSet rs = statement.executeQuery(
                        "SELECT CAST(" + category + " AS VARCHAR) AS c, COUNT(*) AS n "
                                + "FROM records GROUP BY c ORDER BY n DESC LIMIT 5")) {
                    while (rs.next()) {
                        top.add(new CategoryCount(rs.getString(1), rs.getLong(2)));
                    }
                }

                return new DatasetSummary(definition.key(), definition.displayName(), DatasetSummary.OK,
                        rows, distinct, average, sum, List.copyOf(top), "Análisis con DuckDB completado.");
            }
        } catch (IOException | SQLException ex) {
            throw new AnalysisException("Falló el análisis con DuckDB: " + ex.getMessage(), ex);
        } finally {
            deleteQuietly(file);
        }
    }

    static String quote(String field) {
        if (field == null || !SAFE_FIELD.matcher(field).matches()) {
            throw new AnalysisException("Nombre de campo no válido: " + field, null);
        }
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    private static Double nullableDouble(ResultSet rs, int column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // El archivo temporal no afecta el resultado; el sistema operativo lo limpiará.
        }
    }
}
