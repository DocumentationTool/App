package com.wonkglorg.doc.core.convert;


import com.groupdocs.parser.Parser;
import com.groupdocs.parser.data.TextReader;
import com.groupdocs.parser.options.FormattedTextMode;
import com.groupdocs.parser.options.FormattedTextOptions;
import com.wonkglorg.doc.core.exception.client.ClientException;

import java.io.IOException;
import java.nio.file.Path;

public class Converter {
    public static String convertWordToMarkdown(Path pathToWord) throws IOException, ClientException {
        try (Parser parser = new Parser(pathToWord.toString())) {
            try (TextReader reader = parser.getFormattedText(new FormattedTextOptions(FormattedTextMode.Markdown))) {
                return reader.readToEnd();
            }
        }
    }
}
