package at.asit.pdfover.gui.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class VersionComparatorTest {
    @Test
    public void TestVersionComparator() {
        assertTrue(VersionComparator.equals("4.0", "4.0.0"));
        assertTrue(VersionComparator.lessThan("4.4.4-SNAPSHOT", "4.4.4"));
        assertTrue(VersionComparator.greaterThan("4.4.4-SNAPSHOT", "4.4.3"));
        assertTrue(VersionComparator.lessThan("4.4.3-SNAPSHOT", "4.4.3.1"));
        assertTrue(VersionComparator.lessThan("4.4.3.1", "4.4.4-SNAPSHOT"));
        assertTrue(VersionComparator.lessThan("4.4.3-SNAPSHOT", "4.4.3.1"));
    }
}
