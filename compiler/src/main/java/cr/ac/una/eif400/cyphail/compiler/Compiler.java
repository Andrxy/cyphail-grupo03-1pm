package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import cr.ac.una.eif400.cyphail.compiler.codegen.CodeGenerator;
import cr.ac.una.eif400.cyphail.compiler.exceptions.CompilationException;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyser;
import cr.ac.una.eif400.cyphail.compiler.tokenizer.Tokenizer;

/**
 * @author Andrey Solano
 */
public class Compiler {
    private Tokenizer tokenizer;
    private Parser parser;
    private SemanticAnalyser semanticAnalyser;
    private CodeGenerator codeGenerator;

    public Compiler() {
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
        this.semanticAnalyser = new SemanticAnalyser();
        this.codeGenerator = new CodeGenerator();
    }

    public String compile(String sourceCode) throws CompilationException {
        try {
            String tokens = tokenizer.tokenize(sourceCode);
            AST ast = parser.parse(tokens);

            semanticAnalyser.analyse(ast);

            return codeGenerator.generate(ast);
        } catch (Exception e) {
            throw new CompilationException("Error al compilar la query: " + e.getMessage());
        }
    }
}
