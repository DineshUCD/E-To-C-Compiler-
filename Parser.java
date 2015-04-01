/* *** This file is given as part of the programming assignment. *** */
//Dinesh Jayasankar	
import java.util.HashSet;
import java.util.Arrays;


public class Parser {

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private SymbolTable scopeAnalysis = new SymbolTable(); 
    private static final int initialValue = -12345; // initial value of each integer in E

      // maps the first of each nonterminal when we have multiple choices
    static HashSet<TK> f_declaration = new HashSet<TK>(Arrays.asList(TK.VAR));
    static HashSet<TK> f_statement   = new HashSet<TK>(Arrays.asList(TK.ID, TK.PRINT, TK.IF,TK.DO, TK.FA));
    static HashSet<TK> f_assignment  = new HashSet<TK>(Arrays.asList(TK.ID));
    static HashSet<TK> f_print       = new HashSet<TK>(Arrays.asList(TK.PRINT));
    static HashSet<TK> f_if          = new HashSet<TK>(Arrays.asList(TK.IF));
    static HashSet<TK> f_do          = new HashSet<TK>(Arrays.asList(TK.DO));
    static HashSet<TK> f_fa          = new HashSet<TK>(Arrays.asList(TK.FA));
    static HashSet<TK> f_relop       = new HashSet<TK>(Arrays.asList(TK.EQ, TK.LT, TK.GT, TK.NE, TK.LE, TK.GE));
    static HashSet<TK> f_addop       = new HashSet<TK>(Arrays.asList(TK.PLUS, TK.MINUS));
    static HashSet<TK> f_multop      = new HashSet<TK>(Arrays.asList(TK.TIMES, TK.DIVIDE));
   
    private void scan() {
        tok = scanner.scan(); // tok has kind, string, and lineNumber
    }

    private Scan scanner;
    Parser(Scan scanner) {
        this.scanner = scanner;
        Generate.println("#include<stdio.h>");
        Generate.println("");
        Generate.println("int square(int expression) {");
        Generate.println("  return expression * expression;");
        Generate.println("}");
        Generate.println("");
        Generate.println("int square_root(int expression) { ");
        Generate.println("  if( expression <= 0 ) { return 0; }");
        Generate.println("  int operand = expression; ");
        Generate.println("  int remainder = 0; ");
        Generate.println("  int one = 1uL << 30;");
        Generate.println("  while( one > operand ) { ");
        Generate.println("    one >>= 2; ");
        Generate.println("  }");
        Generate.println("  while( one != 0 ) {");
        Generate.println("    if( operand >= remainder + one ) {");
        Generate.println("      operand -= (remainder + one);");
        Generate.println("      remainder = (remainder >> 1) + one;");
        Generate.println("    }");
        Generate.println("    else {");
        Generate.println("      remainder >>= 1;");
        Generate.println("    }");
        Generate.println("    one >>= 2;");
        Generate.println("  }");
        Generate.println("  return remainder;");
        Generate.println("}");
        Generate.println(""); 
        Generate.println("main() {");
        scan();
        program();
        if( tok.kind != TK.EOF )
            parse_error("junk after logical end of program");
        Generate.println("}");
        scopeAnalysis.print();
    }

    private void program() {
        block();
    }

    private void block() {
      scopeAnalysis.newBlock();
      if( first(f_declaration) ) {    
          declarations();
      }
      statement_list();
      scopeAnalysis.disappearBlock();
    }

    private void declarations() {
        mustbe(TK.VAR);
        while( is(TK.ID) ) {
            boolean isValid = scopeAnalysis.declareVariable(tok.string, tok.lineNumber, tok.kind);
            if( isValid ) {
              Generate.println("int " + tok.string + "_ = " + initialValue + ";");    
            }  
            scan();
        }
        mustbe(TK.RAV); 
    }

    // you'll need to add a bunch of methods here
    private void statement_list() {
      if( is(TK.EOF) ) {
        return;
      }
      while( first(f_statement) ) {
          statement();
      }
    }

    private void statement() {
      if( first(f_assignment) ) {
          assignment();
      } 
      else if( first(f_print) ) {
          print();
      }
      else if( first(f_if) ) {
          if_procedure();
      }    
      else if( first(f_do) ) {
          do_procedure();
      }
      else if( first(f_fa) ) {
          fa();
      }
      else {
          parse_error("invalid statement");
      }
    }

    private void assignment() {
      scopeAnalysis.checkProperUse(tok.string, tok.lineNumber, "assignment");
      Generate.print(tok.string + "_");
      mustbe(TK.ID);
      Generate.print(" = ");
      mustbe(TK.ASSIGN);
      expression();
      Generate.println(";");
    }

    private void print() {
      mustbe(TK.PRINT);
      Generate.print("printf(\"%d\\n\", ");
      expression();
      Generate.println(");");
    }
      
    private void if_procedure() {
      mustbe(TK.IF); 
      Generate.print("if");
      guarded_commands();
      mustbe(TK.FI);
    }

    private void do_procedure() {
      mustbe(TK.DO);
      Generate.println("while(1) {");
      Generate.print("if");
      guarded_command();
      while( is(TK.BOX) ) {
          scan();
          Generate.print("else if");
          guarded_command();
      }
      if( is(TK.ELSE) ) {
          Generate.print("else");
          scan();
          mustbe(TK.ARROW);
          Generate.println("{");
          block();
          Generate.println("break;");
          Generate.println("}");
      } 
      else {
        Generate.println("else {");
        Generate.println("break;");
        Generate.println("}");
      }   
      mustbe(TK.OD);
      Generate.println("}");
    }
  
    private void fa() {
      String iterator;

      Generate.print("for("); // for(
      mustbe(TK.FA);
      scopeAnalysis.checkProperUse(tok.string, tok.lineNumber, "assignment");
      iterator = tok.string;
      Generate.print(tok.string + "_"); // for(int i_
      mustbe(TK.ID);
      Generate.print(" = "); // for(int i_ =
      mustbe(TK.ASSIGN);
      expression();
      Generate.print(";"); // for(int i_ = expression;
      mustbe(TK.TO); 
      Generate.print(iterator + "_" + " <= "); 
      expression();
      Generate.print("; " + iterator + "_++) ");
      Generate.println("{");
      if( is(TK.ST) ) { 
          scan();
          Generate.print("if(");
          expression();
          Generate.print(") { } else { continue; }");
      }
      commands();
      Generate.print("}");
      mustbe(TK.AF);
    }

    private void guarded_commands() {
      guarded_command();
      while( is(TK.BOX) ) {
          scan();
          Generate.print("else if");
          guarded_command();
      }
      if( is(TK.ELSE) ) {
          Generate.print("else");
          scan();
          commands();
      }
    }     

    private void guarded_command() {
      Generate.print("(");
      expression();
      Generate.print(")");
      commands();
    }

    private void commands() {
      mustbe(TK.ARROW);
        //Opening and closing braces added in 'block()'
      Generate.println("{");
      block();
      Generate.println("}");
    }

    private void expression() {
      simple();
      if( first(f_relop) ) {
         relop();
         simple();
      }
    }

    private void simple() {
      term();
      while( first(f_addop) ) {
         addop();
         term();
      }
    }
 
    private void term() {
      factor();
      while( first(f_multop) ) {
          multop();
          factor();
      }
    }

      // factor ::= '(' expression ')' | id | number 
      //            | '^' expression | '@' expression
    private void factor() {
      if( is(TK.LPAREN) ) {
        Generate.print("(");
        scan();
        expression();
        Generate.print(")");
        mustbe(TK.RPAREN);
      }
      else if( is(TK.ID) ) {
        scopeAnalysis.checkProperUse(tok.string, tok.lineNumber, "use"); 
        Generate.print(tok.string + "_");
        scan();
      }
      else if( is(TK.NUM) ) {
        Generate.print(tok.string);
        scan();
      } // Extend your implementation to support unary operators
      else if( is(TK.SQUARE) ) {
        scan();
        Generate.print("square("); 
        expression();
        Generate.print(")"); 
      }
      else if( is(TK.SQRT) ) {
        scan();
        Generate.print("square_root("); 
        expression();
        Generate.print(")"); 
      }
      else {
         parse_error("factor");
     }
    }

    private void relop() {
      if( is(TK.EQ) || is(TK.LT) || is(TK.GT) 
          || is(TK.NE) || is(TK.LE) || is(TK.GE)) {
            if( is(TK.NE) ) {
              Generate.print(" != "); 
            }
            else if( is(TK.EQ) ) {
              Generate.print(" == "); 
            }
            else {
              Generate.print(" " + tok.string + " ");
            }
          scan();
      }    
    }
     
    private void addop() {
      if( is(TK.PLUS) || is(TK.MINUS) ) { 
        Generate.print(" " + tok.string + " ");
        scan();
      }
    }
   
    private void multop() { 
      if( is(TK.TIMES) || is(TK.DIVIDE) ) {
        Generate.print(" " + tok.string + " ");
        scan();
      }
    }

    // is current token what we want?
    private boolean is(TK tk) { // Tk (enum) is passed in 
        return tk == tok.kind;  // formal parameter is equal to current token
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
        if( ! is(tk) ) {
            System.err.println( "mustbe: want " + tk + ", got " +
                                    tok);
            parse_error( "missing token (mustbe)" );
        }
        scan();
    }

    private void parse_error(String msg) {
        System.err.println( "can't parse: line "
                            + tok.lineNumber + " " + msg );
        System.exit(1);
    }

    private boolean first(HashSet<TK> nonTerminal) {
        return nonTerminal.contains(tok.kind);
    }
}
