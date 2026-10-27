package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class StatementReaderTest {
    private final StatementReader reader = new StatementReader();

    @Test
    void accumulatesLinesUntilTerminator() {
        assertEquals("MATCH (p:Persona)\n\nRETURN p;",
                read("MATCH (p:Persona)", "\nRETURN p;"));
    }

    @Test
    void waitsForTheLastStatementToEnd() {
        assertEquals("RETURN 1; RETURN 2; MATCH (n)\nRETURN n;",
                read("RETURN 1; RETURN 2; MATCH (n)", "RETURN n;"));
    }

    @Test
    void semicolonInsideStringDoesNotTerminateReading() {
        String query = "CREATE (n {name: 'Ana; María'})";
        assertEquals(query + "\n;", read(query, ";"));
    }

    @Test
    void lexicalErrorIsDispatchedImmediately() {
        try (Scanner input = new Scanner(".exit\n")) {
            assertEquals("RETURN @", reader.read(input, "RETURN @", source -> Compiler.getInstance().isTerminated(source)));
            assertEquals(".exit", input.nextLine());
        }
    }

    @Test
    void completeStatementDoesNotConsumeTheNextCommand() {
        try (Scanner input = new Scanner(".exit\n")) {
            assertEquals("RETURN 1;", reader.read(input, "RETURN 1;", source -> Compiler.getInstance().isTerminated(source)));
            assertEquals(".exit", input.nextLine());
        }
    }

    @Test
    void semicolonInsideCommentDoesNotTerminateReading() {
        assertEquals("MATCH (n) // ;\nRETURN n;",
                read("MATCH (n) // ;", "RETURN n;"));
    }

    @Test
    void trailingCommentAfterTerminatorDoesNotRequireMoreLines() {
        assertEquals("RETURN 1; /* ; */", read("RETURN 1; /* ; */", ""));
    }

    @Test
    void endOfInputReturnsUnterminatedText() {
        assertEquals("MATCH (n)\nRETURN n", read("MATCH (n)", "RETURN n"));
    }

    @Test
    void preservesWhitespaceFromContinuationLines() {
        assertEquals("MATCH (n)\n   RETURN n   ;",
                read("MATCH (n)", "   RETURN n   ;"));
    }

    private String read(String firstLine, String remainingLines) {
        try (Scanner input = new Scanner(remainingLines)) {
            return reader.read(input, firstLine, source -> Compiler.getInstance().isTerminated(source));
        }
    }
}
