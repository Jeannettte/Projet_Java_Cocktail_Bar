package com.example.cocktailbar.data;

import com.example.cocktailbar.exception.DataLoadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvDrinkRepositoryTest {

    private static final String HEADER = "type,name,price,stock,alcohol_percent,details\n";
    private static final String MOJITO = "COCKTAIL,Mojito,8.50,20,10.0,rum|mint|lime\n";
    private static final String LAGER = "BEER,Lager,4.50,40,5.0,33\n";

    @TempDir
    Path tempDir;

    private Path writeFile(String content) throws IOException {
        Path file = tempDir.resolve("drinks.csv");
        Files.writeString(file, content);
        return file;
    }

    private DataLoadException loadExpectingFailure(String content) throws IOException {
        Path file = writeFile(content);
        return assertThrows(DataLoadException.class, () -> new CsvDrinkRepository(file).loadAll());
    }

    // ---- normal cases ----

    @Test
    void everyValidLineIsLoadedInFileOrder() throws Exception {
        Path file = writeFile(HEADER + MOJITO + LAGER);

        List<MenuEntry> entries = new CsvDrinkRepository(file).loadAll();

        assertEquals(2, entries.size());
        assertEquals("Mojito", entries.get(0).drink().getName());
        assertEquals(20, entries.get(0).initialStock());
        assertEquals("Lager", entries.get(1).drink().getName());
    }

    @Test
    void commentsAndBlankLinesAreIgnored() throws Exception {
        Path file = writeFile("# menu of the bar\n\n" + HEADER + "\n" + MOJITO + "   \n# end\n");

        assertEquals(1, new CsvDrinkRepository(file).loadAll().size());
    }

    @Test
    void windowsLineEndingsAreSupported() throws Exception {
        Path file = writeFile((HEADER + MOJITO).replace("\n", "\r\n"));

        assertEquals(1, new CsvDrinkRepository(file).loadAll().size());
    }

    @Test
    void theDataFileShippedWithTheProjectIsValid() throws Exception {
        // Maven runs tests from the project root, so this relative path points to the real data file.
        List<MenuEntry> entries = new CsvDrinkRepository(Path.of("src/main/resources/drinks.csv")).loadAll();

        assertFalse(entries.isEmpty());
        assertTrue(entries.stream().allMatch(entry -> entry.initialStock() >= 0));
    }

    // ---- invalid situations ----

    @Test
    void missingFileIsReportedAsDataLoadException() {
        Path missing = tempDir.resolve("does-not-exist.csv");

        DataLoadException error = assertThrows(DataLoadException.class,
                () -> new CsvDrinkRepository(missing).loadAll());

        assertTrue(error.getMessage().contains("does-not-exist.csv"));
        assertNotNull(error.getCause()); // the low-level IOException is kept for debugging
    }

    @Test
    void invalidLineIsReportedWithItsLineNumber() throws Exception {
        DataLoadException error = loadExpectingFailure(HEADER + MOJITO + "COCKTAIL,Broken,abc,1,1.0,rum\n");

        assertTrue(error.getMessage().contains("line 3"), error.getMessage());
        assertNotNull(error.getCause());
    }

    @Test
    void commentsDoNotShiftTheReportedLineNumber() throws Exception {
        // line 1 = comment, line 2 = header, line 3 = the broken line
        DataLoadException error = loadExpectingFailure("# comment\n" + HEADER + "WINE,Chardonnay,7.00,10,12.0,75\n");

        assertTrue(error.getMessage().contains("line 3"), error.getMessage());
    }

    @Test
    void duplicateNamesAreRejectedEvenWithAnotherCase() throws Exception {
        DataLoadException error = loadExpectingFailure(HEADER + MOJITO + "BEER,mojito,4.50,40,5.0,33\n");

        assertTrue(error.getMessage().contains("Duplicate"), error.getMessage());
    }

    @Test
    void fileWithOnlyAHeaderIsRejected() throws Exception {
        DataLoadException error = loadExpectingFailure(HEADER);

        assertTrue(error.getMessage().contains("No drinks"), error.getMessage());
    }

    @Test
    void emptyFileIsRejected() throws Exception {
        assertTrue(loadExpectingFailure("").getMessage().contains("No drinks"));
    }

    // ---- failures while reading (the reader is opened, then something goes wrong) ----

    @Test
    void fileThatIsNotValidUtf8IsReportedAsDataLoadException() throws Exception {
        Path file = tempDir.resolve("broken-encoding.csv");
        Files.write(file, new byte[]{(byte) 0xC3, (byte) 0x28}); // 0xC3 must be followed by a byte >= 0x80

        DataLoadException error = assertThrows(DataLoadException.class,
                () -> new CsvDrinkRepository(file).loadAll());

        assertTrue(error.getCause() instanceof IOException);
    }

    @Test
    void aDirectoryInsteadOfAFileIsReportedAsDataLoadException() {
        DataLoadException error = assertThrows(DataLoadException.class,
                () -> new CsvDrinkRepository(tempDir).loadAll());

        assertNotNull(error.getCause());
    }
}
