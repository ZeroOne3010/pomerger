package io.github.zeroone3010.pomerger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElse;

public class PoMerger {
  private static final String MSGID_REGEX = "msgid \"(.*)\"$";
  private static final String MSGSTR_REGEX = "msgstr \"(.*)\"$";

  public record Entry(String comment, String msgid, String msgstr) {
    public boolean isTranslated() {
      return !msgstr.isBlank();
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Enter two file names and an output path as parameters");
      System.exit(1);
    }

    final long startTime = System.currentTimeMillis();
    final List<String> untranslatedFileLines = Files.readAllLines(Path.of(args[0]));
    final List<Entry> untranslatedFile = parse(untranslatedFileLines);
    final List<String> translatedFileLines = Files.readAllLines(Path.of(args[1]));
    final List<Entry> translatedFile = parse(translatedFileLines);
    final String outputPath = args[2];
    final Path outputFilePath = Path.of(outputPath +
        (outputPath.endsWith(File.separator)
            ? ""
            : File.separator)
        + "output.po");

    System.out.printf("Untranslated file has %d entries (%d lines)%n", untranslatedFile.size(), untranslatedFileLines.size());
    final Map<String, Entry> translatedEntries = translatedFile.stream()
        .collect(Collectors.toMap(Entry::comment, e -> e));

    final int translationFileEntryCount = translatedFile.size();
    final int translatedEntryCount = (int) translatedFile.stream().filter(Entry::isTranslated).count();
    System.out.printf("Translated file has %d entries (%d lines), %d of them translated (%d%%)%n",
        translationFileEntryCount,
        translatedFileLines.size(),
        translatedEntryCount, percentage(translatedEntryCount, translationFileEntryCount));

    final Set<String> untranslatedFileKeys = untranslatedFile.stream().map(Entry::comment).collect(Collectors.toSet());
    final Set<String> translatedFileKeys = translatedFile.stream().map(Entry::comment).collect(Collectors.toSet());

    System.out.printf("Untranslated file has %d unique comments", untranslatedFileKeys.size());
    if (untranslatedFileKeys.size() != untranslatedFile.size()) {
      System.out.println(" which is an error!");
    } else {
      System.out.println(" which is as it should be.");
    }
    final boolean allKeysStillThere = untranslatedFileKeys.containsAll(translatedFileKeys);
    if (allKeysStillThere) {
      System.out.println("Untranslated file confirmed to have all the keys that are already in the translated file.");
    } else {
      System.err.println("Translated file has entries that the untranslated one does not! ");
      untranslatedFileKeys.removeAll(translatedFileKeys);
      System.err.println("The keys are: " + untranslatedFileKeys);
      System.exit(3);
    }

    System.out.println("Joining the missing entries into the translation file...");

    final List<Entry> combinedResult = new ArrayList<>(untranslatedFile.size());
    for (final Entry currentUntranslatedEntry : untranslatedFile) {
      final Entry currentTranslatedEntry = translatedEntries.get(currentUntranslatedEntry.comment());
      combinedResult.add(requireNonNullElse(currentTranslatedEntry, currentUntranslatedEntry));
    }
    final List<String> rawOutput = combinedResult.stream().<String>mapMulti((entry, entryConsumer) -> {
      entryConsumer.accept(entry.comment());
      entryConsumer.accept("msgid \"%s\"".formatted(entry.msgid()));
      entryConsumer.accept("msgstr \"%s\"".formatted(entry.msgstr()));
    }).toList();
    System.out.printf("Writing %d entries (%d lines) (%d%% translated) to %s...%n",
        combinedResult.size(),
        rawOutput.size(),
        percentage(translatedEntryCount, combinedResult.size()),
        outputFilePath);
    Files.write(outputFilePath, rawOutput);
    System.out.printf("All done in %d ms.%n", System.currentTimeMillis() - startTime);
  }

  private static int percentage(float smaller, float larger) {
    return Math.round((smaller / larger) * 100f);
  }

  private static List<Entry> parse(List<String> lines) {
    final List<Entry> result = new LinkedList<>();
    for (int i = 0; i < lines.size(); i += 3) {
      final String comment = lines.get(i);
      if (!comment.startsWith("#")) {
        System.err.printf("Out of sync when i = %d and lines.size() = %d!%n%n", i, lines.size());
        System.exit(2);
      }
      final String msgid = lines.get(i + 1).replaceFirst(MSGID_REGEX, "$1");
      final String msgstr = lines.get(i + 2).replaceFirst(MSGSTR_REGEX, "$1");
      result.add(new Entry(comment, msgid, msgstr));
    }
    return result;
  }
}
