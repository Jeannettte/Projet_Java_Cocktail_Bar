package com.example.cocktailbar.data;

import com.example.cocktailbar.exception.DataLoadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads the menu from a CSV file.
 *
 * <p>Blank lines and lines starting with '#' are ignored so the file can carry comments.
 * The first remaining line is the header. Loading is all-or-nothing: the first invalid line stops
 * the load, because starting a bar with half a menu would hide the problem.
 */
public class CsvDrinkRepository implements DrinkRepository {

    private static final String COMMENT_PREFIX = "#";

    private final Path file;
    private final DrinkCsvParser parser;

    public CsvDrinkRepository(Path file) {
        this(file, new DrinkCsvParser());
    }

    // The parser can be injected so tests can replace it if needed.
    public CsvDrinkRepository(Path file, DrinkCsvParser parser) {
        this.file = file;
        this.parser = parser;
    }

    @Override
    public List<MenuEntry> loadAll() throws DataLoadException {
        List<String> lines = readLines();
        List<MenuEntry> entries = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        boolean headerSkipped = false;

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }

            int lineNumber = index + 1; // humans count lines from 1
            MenuEntry entry = parseLine(line, lineNumber);
            String key = entry.drink().getName().toLowerCase(Locale.ROOT);
            if (!seenNames.add(key)) {
                throw new DataLoadException("Duplicate drink '" + entry.drink().getName() + "' at line " + lineNumber);
            }
            entries.add(entry);
        }

        if (entries.isEmpty()) {
            throw new DataLoadException("No drinks found in " + file);
        }
        return entries;
    }

    private List<String> readLines() throws DataLoadException {
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            // Low-level IOException is translated into our own exception so callers do not depend on file details.
            throw new DataLoadException("Cannot read data file " + file + ": " + e.getMessage(), e);
        } finally {
            // Runs on success, on failure and on the early "return" above: the file is always released.
            closeQuietly(reader);
        }
    }

    // A failure to close must not replace the real error (or hide a successful read), so it is only reported.
    private void closeQuietly(BufferedReader reader) {
        if (reader == null) { // the file could not even be opened: nothing to close
            return;
        }
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Warning: could not close " + file + ": " + e.getMessage());
        }
    }

    private MenuEntry parseLine(String line, int lineNumber) throws DataLoadException {
        try {
            return parser.parse(line);
        } catch (IllegalArgumentException e) {
            throw new DataLoadException("Invalid data at line " + lineNumber + ": " + e.getMessage(), e);
        }
    }
}
