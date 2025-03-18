package com.wonkglorg.docapi;

import com.wonkglorg.doc.core.path.AntPath;
import com.wonkglorg.doc.core.path.TargetPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests related to the {@link AntPath} and {@link com.wonkglorg.doc.core.path.TargetPath}
 */
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
    void targetPathDefinition() {
        Assertions.assertFalse(TargetPath.of("path/file.md").isAntPath());
        Assertions.assertTrue(TargetPath.of("path/**").isAntPath());
    }

    @Test
    void targetPathToString() {
        Assertions.assertEquals("path\\file.md", TargetPath.of("path/file.md").toString());
        Assertions.assertEquals("path\\**", TargetPath.of("path/**").toString());
    }
}
