package cr.ac.una.eif400.cyphail.compiler.codegen;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;

/**
 * Encargado de la generación de código a partir del AST.
 * Traduce las estructuras abstractas del lenguaje Cyphail en las sentencias
 * prolog correspondientes.
 *
 * <p>Para efectos de esta versión P1.1, la lógica se encuentra simulada y casi vacía,
 * ya que la evaluación actual se centra únicamente en la arquitectura y estructura del proyecto</p>
 *
 * @author Andrey Solano
 * @version P1.1
 */
public class CodeGenerator  {

    /**
     * Traduce el AST proporcionado en su representación de código prolog correspondiente.
     *
     * @param ast raíz del árbol sintáctico
     * @return código generado (simulado para P1.1)
     */
    public String generate(AST ast) {
        return "generated";
    }
}
