package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.objects.AntPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathTest {
    @Test
    void canCorrectlyEvaluateAntPathObject() {
        AntPath antPath = new AntPath("path/**");
        Assertions.assertTrue(antPath.matches("path/file.md"));
        Assertions.assertTrue(antPath.matches("path/file"));
        Assertions.assertTrue(antPath.matches("path/"));
        Assertions.assertFalse(antPath.matches("path"));
        Assertions.assertFalse(antPath.matches("path2/file.md"));
    }


    @Test
    void antPathObjectToString() {
        AntPath antPath = new AntPath("path/**");
        Assertions.assertEquals("path\\**", antPath.toString());
        AntPath antPath2 = new AntPath("path\\**");
        Assertions.assertEquals("path\\**", antPath2.toString());
        AntPath antPath3 = new AntPath("path/**/text.txt");
        Assertions.assertEquals("path\\**\\text.txt", antPath3.toString());
    }

    @Test
    void antPathFailInvalidFormat() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AntPath("path/text.txt"));
    }

    @Test
    void resourcePath() {

    }
}
