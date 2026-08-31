package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;

/**
 * Procesa la secuencia de tokens generada por la clase Tokenizer
 * para construir y devolver el AST correspondiente.
 *
 * <p>Para efectos de esta versión P1.1, la lógica interna se encuentra prácticamente vacía ya
 * que la evaluación se centra únicamente en validarla arquitectura y el flujo general del proyecto</p>
 *
 * @author Andrey Solano
 * @version P1.1
 */
public class Parser {

    /**
     * Analiza sintácticamente los tokens y construye el AST
     *
     * @param tokens representación de tokens a procesar.
     * @return nodo raíz del AST generado (simulado para la versión P1.1).
     */
    public AST parse(String tokens) {
        return new AST();
    }
}
