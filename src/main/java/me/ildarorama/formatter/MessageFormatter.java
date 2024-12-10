package me.ildarorama.formatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageFormatter {
    private final BindingStore store;

    public MessageFormatter(BindingStore store) {
        this.store = store;
    }

    public String format(String template) throws ParseException {
        Objects.requireNonNull(template);

        List<BindingItem> items = getBindingItems(template);

        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (BindingItem item : items) {
            sb.append(template, idx, item.start);
            sb.append(store.get(item.name));
            idx = item.end;
        }
        sb.append(template.substring(idx));
        return sb.toString();
    }

    public void export(String template, Path outputFile) throws ParseException, IOException {
        Files.writeString(outputFile, format(template), StandardOpenOption.CREATE_NEW);
    }

    private List<BindingItem> getBindingItems(String template) throws ParseException {
        List<BindingItem> items = new ArrayList<>();
        int beginIndex = 0;
        while ((beginIndex = template.indexOf("#{", beginIndex)) > -1) {
            int endIndex = template.indexOf("}", beginIndex);
            if (endIndex == -1) {
                throw new ParseException("Can not parse template", beginIndex);
            }
            String name = template.substring(beginIndex + 2, endIndex);

            if (name.isBlank() || !name.chars().allMatch(Character::isLetterOrDigit)) {
                throw new ParseException("Can not parse template", beginIndex);
            }
            BindingItem item = new BindingItem(name, beginIndex, endIndex + 1);
            items.add(item);
            beginIndex = endIndex;
        }
        return items;
    }

    private static class BindingItem {
        final int start;
        final int end;
        final String name;

        private BindingItem(String name, int start, int end) {
            this.start = start;
            this.end = end;
            this.name = name;
        }
    }
}
