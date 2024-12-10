package me.ildarorama.formatter.test;

import me.ildarorama.formatter.BindingStore;
import me.ildarorama.formatter.MessageFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.MissingFormatArgumentException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFormatter {
    private static @TempDir Path dir;

    @Test
    public void testStore() throws ParseException {
        BindingStore spiedStore = Mockito.spy(new BindingStore());

        spiedStore.put("test1", "1");
        spiedStore.put("test2", "2");

        String result  = new MessageFormatter(spiedStore).format("Head #{test1} gap gap #{test2}");

        assertEquals(result, "Head 1 gap gap 2");
        Mockito.verify(spiedStore).get("test1");
        Mockito.verify(spiedStore).get("test2");
    }

    @ParameterizedTest
    @MethodSource("source")
    public void testExport(BindingStore store, String template, String expected) throws ParseException, IOException {
        String fileName = String.format("%d.txt", System.currentTimeMillis());
        Path outputFile = dir.resolve(fileName);
        new MessageFormatter(store).export(template, outputFile);

        assertTrue(Files.exists(outputFile));
        assertEquals(Files.readString(outputFile), expected);
    }


    @ParameterizedTest
    @MethodSource("source")
    public void testSuccess(BindingStore score, String template, String expected) throws ParseException {
        String text = new MessageFormatter(score).format(template);
        System.out.println(text);
        assertEquals(expected, text);
    }

    @ParameterizedTest
    @MethodSource("failureSource")
    public void testFailure(BindingStore score, String template) {
        assertThrows(ParseException.class, () ->
            new MessageFormatter(score).format(template)
        );
    }

    @Test
    public void checkForMissingBinding() {
        BindingStore source = new BindingStore();
        source.put("test1", "value1");

        MessageFormatter formatter = new MessageFormatter(source);
        assertThrows(MissingFormatArgumentException.class,
                () -> formatter.format("Test #{test2}"));
    }

    @Test
    public void checkForNull() {
        BindingStore source = new BindingStore();
        source.put("test1", "value1");
        source.put("test2", "value2");

        MessageFormatter formatter = new MessageFormatter(source);
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }

    public static Stream<Arguments> failureSource() {
        BindingStore source = new BindingStore();
        source.put("test1", "value1");
        source.put("test2", "value2");
        return Stream.of(
                Arguments.of(source, "Head #{test1 gap gap #{test2}"),
                Arguments.of(source, "#{test1} gap gap #{test2 tail"),
                Arguments.of(source, "#{ test1} gap gap #{test2} tail")
        );
    }

    public static Stream<Arguments> source() {
        BindingStore source = new BindingStore();
        source.put("test1", "value1");
        source.put("test2", "value2");
        return Stream.of(
                Arguments.of(source, "Head #{test1} gap gap #{test2}", "Head value1 gap gap value2"),
                Arguments.of(source, "#{test1} gap gap #{test2} tail", "value1 gap gap value2 tail"),
                Arguments.of(source, "Head #{test1} gap gap #{test2} tail", "Head value1 gap gap value2 tail"),
                Arguments.of(source, "Head #{test1} gap #{test2} gap #{test2} tail", "Head value1 gap value2 gap value2 tail"),
                Arguments.of(source, "#{test1} gap gap #{test2}", "value1 gap gap value2"),
                Arguments.of(source, "#{test1}#{test2}", "value1value2"),
                Arguments.of(source, "", "")
        );
    }
}
