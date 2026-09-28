package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Pruebas de los combinadores")
public class CombinatorsTest {

    private static IParser<InputString, String, String> character(char expected) {
        return source -> {
            int index = source.index();
            boolean matches = index < source.input().length() && source.input().charAt(index) == expected;
            return matches
                    ? new Ok<>(new InputString(source.input(), index + 1), String.valueOf(expected))
                    : new Fail<>("Expecting " + expected + " at " + index);
        };
    }

    private static InputString input(String text) {
        return new InputString(text);
    }

    @Test
    @DisplayName("Or devuelve el primer parser que tiene exito")
    void orDevuelveElPrimerExito() {
        var ok = assertOk(Or(character('a'), character('b')).parse(input("b")));

        assertEquals("b", ok.value());
        assertEquals(1, ok.rest().index());
    }

    @Test
    @DisplayName("Or no prueba los siguientes parsers despues de un exito")
    void orSeDetieneEnElPrimerExito() {
        var intentos = new AtomicInteger();
        IParser<InputString, String, String> contador = source -> {
            intentos.incrementAndGet();
            return new Fail<>("no");
        };

        assertOk(Or(character('a'), contador).parse(input("a")));

        assertEquals(0, intentos.get());
    }

    @Test
    @DisplayName("Or junta en orden las razones de todos los fallos")
    void orJuntaLasRazones() {
        var failure = assertFail(Or(character('a'), character('b')).parse(input("c")));

        assertEquals(List.of("Expecting a at 0", "Expecting b at 0"), failure.reason());
    }

    @Test
    @DisplayName("Sequence acumula los valores y avanza la entrada")
    void sequenceAcumulaLosValores() {
        var ok = assertOk(Sequence(character('a'), character('b'), character('c')).parse(input("abcd")));

        assertEquals(List.of("a", "b", "c"), ok.value());
        assertEquals(3, ok.rest().index());
    }

    @Test
    @DisplayName("Sequence falla con la razon del primer parser que falla")
    void sequenceFallaEnElPrimerError() {
        var failure = assertFail(Sequence(character('a'), character('b'), character('c')).parse(input("axc")));

        assertEquals("Expecting b at 1", failure.reason());
    }

    @Test
    @DisplayName("Sequence no ejecuta los parsers que siguen al que falla")
    void sequenceNoSigueDespuesDelFallo() {
        var intentos = new AtomicInteger();
        IParser<InputString, String, String> contador = source -> {
            intentos.incrementAndGet();
            return new Ok<>(source, "");
        };

        assertFail(Sequence(character('a'), character('b'), contador).parse(input("ax")));

        assertEquals(0, intentos.get());
    }

    @Test
    @DisplayName("El mismo Sequence se puede usar varias veces con resultados independientes")
    void sequenceReutilizable() {
        var parser = Sequence(character('a'), character('b'));

        assertEquals(List.of("a", "b"), assertOk(parser.parse(input("ab"))).value());
        assertFail(parser.parse(input("ax")));
        assertEquals(List.of("a", "b"), assertOk(parser.parse(input("ab"))).value());
    }

    @Test
    @DisplayName("Sequence sin parsers tiene exito sin consumir")
    void sequenceVacio() {
        var ok = assertOk(Combinators.<InputString, String, String>Sequence().parse(input("abc")));

        assertEquals(List.of(), ok.value());
        assertEquals(0, ok.rest().index());
    }

    @Test
    @DisplayName("El mismo Star se puede usar varias veces con resultados independientes")
    void starReutilizable() {
        var parser = Star(character('a'));

        assertEquals(List.of("a", "a"), assertOk(parser.parse(input("aab"))).value());
        assertEquals(List.of("a"), assertOk(parser.parse(input("ab"))).value());
    }

    @Test
    @DisplayName("Star deja de llamar al parser despues del primer fallo")
    void starSeDetieneEnElFallo() {
        var intentos = new AtomicInteger();
        IParser<InputString, String, String> contador = source -> {
            intentos.incrementAndGet();
            return character('a').parse(source);
        };

        assertOk(Star(contador).parse(input("aab")));

        assertEquals(3, intentos.get());
    }

    @Test
    @DisplayName("Star acepta cero repeticiones sin consumir la entrada")
    void starAceptaCeroRepeticiones() {
        var ok = assertOk(Star(character('a')).parse(input("b")));

        assertEquals(List.of(), ok.value());
        assertEquals(0, ok.rest().index());
    }

    @Test
    @DisplayName("Star consume todas las repeticiones posibles")
    void starConsumeTodasLasRepeticiones() {
        var ok = assertOk(Star(character('a')).parse(input("aaab")));

        assertEquals(List.of("a", "a", "a"), ok.value());
        assertEquals(3, ok.rest().index());
    }

    @Test
    @DisplayName("Plus falla si no hay al menos una coincidencia")
    void plusExigeUnaCoincidencia() {
        var failure = assertFail(Plus(character('a')).parse(input("b")));

        assertEquals("Expecting a at 0", failure.reason());
    }

    @Test
    @DisplayName("Plus acepta una o mas coincidencias")
    void plusAceptaVariasCoincidencias() {
        assertEquals(List.of("a"), assertOk(Plus(character('a')).parse(input("a"))).value());
        assertEquals(List.of("a", "a"), assertOk(Plus(character('a')).parse(input("aab"))).value());
    }

    @Test
    @DisplayName("Optional envuelve el valor en una lista si el parser tiene exito")
    void optionalConExito() {
        var ok = assertOk(Optional(character('a')).parse(input("a")));

        assertEquals(List.of("a"), ok.value());
        assertEquals(1, ok.rest().index());
    }

    @Test
    @DisplayName("Optional devuelve una lista vacia sin consumir si el parser falla")
    void optionalSinExito() {
        var ok = assertOk(Optional(character('a')).parse(input("b")));

        assertEquals(List.of(), ok.value());
        assertEquals(0, ok.rest().index());
    }

    @Test
    @DisplayName("TFlat transforma la lista reconocida en un solo valor")
    void tflatTransformaElValor() {
        var ok = assertOk(TFlat(Sequence(character('a'), character('b')), list -> String.join("", list)).parse(input("ab")));

        assertEquals("ab", ok.value());
    }

    @Test
    @DisplayName("TFlat propaga el fallo sin aplicar la transformacion")
    void tflatPropagaElFallo() {
        var failure = assertFail(TFlat(Sequence(character('a'), character('b')), list -> "nunca").parse(input("aa")));

        assertEquals("Expecting b at 1", failure.reason());
    }

    @Test
    @DisplayName("RFlat une la lista de razones en una sola")
    void rflatUneLasRazones() {
        var failure = assertFail(RFlat(Or(character('a'), character('b')), concatStringList).parse(input("c")));

        assertEquals("Expecting a at 0 & Expecting b at 0", failure.reason());
    }

    @Test
    @DisplayName("RFlat deja pasar el exito intacto")
    void rflatDejaPasarElExito() {
        var ok = assertOk(RFlat(Or(character('a'), character('b')), concatStringList).parse(input("b")));

        assertEquals("b", ok.value());
    }

    @Test
    @DisplayName("concatStringList separa los mensajes con &")
    void concatStringListUneConAmpersand() {
        assertEquals("uno & dos & tres", concatStringList.apply(List.of("uno", "dos", "tres")));
        assertEquals("", concatStringList.apply(List.of()));
    }

    @Test
    @DisplayName("OrFlat se comporta como Or con las razones ya unidas")
    void orFlatUneLasRazones() {
        assertEquals("a", assertOk(OrFlat(concatStringList, character('a'), character('b')).parse(input("a"))).value());
        assertEquals("Expecting a at 0 & Expecting b at 0",
                assertFail(OrFlat(concatStringList, character('a'), character('b')).parse(input("c"))).reason());
    }

    @Test
    @DisplayName("JustGetRight conserva solo el valor de la derecha")
    void justGetRightConservaLaDerecha() {
        var ok = assertOk(JustGetRight(character('a'), character('b')).parse(input("ab")));

        assertEquals("b", ok.value());
        assertEquals(2, ok.rest().index());
    }

    @Test
    @DisplayName("JustGetLeft conserva solo el valor de la izquierda")
    void justGetLeftConservaLaIzquierda() {
        var ok = assertOk(JustGetLeft(character('a'), character('b')).parse(input("ab")));

        assertEquals("a", ok.value());
        assertEquals(2, ok.rest().index());
    }

    @Test
    @DisplayName("JustGetMiddle conserva solo el valor del medio")
    void justGetMiddleConservaElMedio() {
        var ok = assertOk(JustGetMiddle(character('('), character('x'), character(')')).parse(input("(x)")));

        assertEquals("x", ok.value());
        assertEquals(3, ok.rest().index());
    }

    @Test
    @DisplayName("Los combinadores de seleccion fallan si falta alguna parte")
    void seleccionFallaSiFaltaUnaParte() {
        assertFail(JustGetRight(character('a'), character('b')).parse(input("a")));
        assertFail(JustGetLeft(character('a'), character('b')).parse(input("b")));
        assertFail(JustGetMiddle(character('('), character('x'), character(')')).parse(input("(x")));
    }

    @Test
    @DisplayName("Lazy no construye la regla hasta que se parsea")
    void lazyDifiereLaConstruccion() {
        var construcciones = new AtomicInteger();
        IParser<InputString, String, String> lazy = Lazy(() -> {
            construcciones.incrementAndGet();
            return character('a');
        });

        assertEquals(0, construcciones.get());
        assertOk(lazy.parse(input("a")));
        assertEquals(1, construcciones.get());
    }

    @Test
    @DisplayName("Lazy permite definir reglas recursivas")
    void lazyPermiteRecursion() {
        var ok = assertOk(nested().parse(input("(((x)))")));

        assertEquals("x", ok.value());
        assertEquals(7, ok.rest().index());
    }

    private static IParser<InputString, String, String> nested() {
        return OrFlat(concatStringList,
                character('x'),
                JustGetMiddle(character('('), Lazy(() -> nested()), character(')')));
    }
}
