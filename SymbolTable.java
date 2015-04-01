//Dinesh Jayasankar
import java.util.*;

public class SymbolTable {

  public class VariableReference { 

      //use and assignment attributes of the variable
    private class VarInfo {
        //both initialized to zero by default
      public int line;
      public int count;

      public VarInfo() {
        this.line  = -1;
        this.count = 0;
      }

      public VarInfo(int line, int count) {
        this.line  = line;
        this.count = count;
      }
    } // class VarInfo

    private String name;
    private int declaredLine;
    private int nestingDepth; 
    private int totalDepth;   // keeps track of the unique block number for each variable
                    //keeps a list of the lines the variable is assigned to on
    private List<VarInfo> assignment;
    private List<VarInfo> use; // keeps a list of the lines the variable is used on and use count for that line

    public VariableReference(String name, int declaredLine, int nestingDepth, int totalDepth) {
      this.name = name;
      this.declaredLine = declaredLine;
      this.nestingDepth = nestingDepth;
      this.totalDepth   = totalDepth;
      this.assignment   = new ArrayList<VarInfo>();
      this.use          = new ArrayList<VarInfo>();
    }
    
    public boolean markUse(int lineNumber) {
      for(VarInfo var : this.use) {
          //used more than once on same line
        if(var.line == lineNumber) {
          var.count += 1;
          return true;
        } // if()
      } // for()
      VarInfo id = new VarInfo(lineNumber, 1);
      this.use.add(id);
      return false;
    } // markUse()

    public boolean markAssignment(int lineNumber) {
      for(VarInfo var : this.assignment) {
          //assigned more than once on same line
        if(var.line == lineNumber) {
          var.count += 1;
          return true;
        } // if()
      } // for()
      VarInfo id = new VarInfo(lineNumber, 1);
      this.assignment.add(id);
      return false;
    } // markAssignment()
  
    String getName()      { return this.name;         }
    int getLineNumber()   { return this.declaredLine; }
    int getNestingDepth() { return this.nestingDepth; }    
    int getTotalDepth()   { return this.totalDepth;   }
  }; // class VariableReference

  private Stack< List<VariableReference>> naturalRepresentation;
  private List<VariableReference> total;
  public int blockNumber;
  public int blockCount;  //assigned to totalDepth later in VariableReference

  public SymbolTable() {
    this.naturalRepresentation = new Stack< List<VariableReference>>();
    this.total                 = new ArrayList<VariableReference>();
    blockNumber = -1;
  } // constructor()

  public void newBlock() { 
    List<VariableReference> block = new LinkedList<VariableReference>();
    naturalRepresentation.push(block);
    blockNumber += 1;
    blockCount  += 1;
  }

  public void disappearBlock() {
    if( !naturalRepresentation.empty() ) {
      naturalRepresentation.pop(); 
      blockNumber -= 1; 
    }  
  }
  
  public boolean declareVariable(String tokenName, int lineNumber, TK tokenId) { 
    // Each time a variable is declared, the table is checked.
    boolean reDeclared = this.searchCurrentBlock(tokenName);
    // If the variable is in already in the table, then it is being redeclared.
    if( reDeclared ) {
      System.err.println("variable " + tokenName + " is redeclared on line " + lineNumber); 
      return false;
    } 
    else {
      //Otherwise, the variable is added to the table.
      VariableReference entry = new VariableReference(tokenName, lineNumber, blockNumber, blockCount);
      naturalRepresentation.peek().add(0, entry);
      total.add(entry);
      return true;
    }
  }

  public boolean searchCurrentBlock(String tokenName) {
    //stack should be searched beginning with the newest entry
    List<VariableReference> currentBlock = naturalRepresentation.peek(); 
    for(VariableReference var : currentBlock) {
      if(var.getName().equals(tokenName) && var.getNestingDepth() == blockNumber) {
        return true;
      } // if()
    } // for()
    return false;
  }

  public boolean checkProperUse(String tokenName, int lineNumber, String purpose) {
    //An undeclared id, then, is one that does nto appear anywhere in the entire stack of lists.
    // use 'name', 'declaredLine', and 'nestingDepth'
    int lastElementLocation = naturalRepresentation.size();
      //Access to each 'List' of VariableReference
    ListIterator< List<VariableReference>> topToBottom = naturalRepresentation.listIterator(lastElementLocation);
    //stack should be searched beginning with the newest entry 
    
    while( topToBottom.hasPrevious() ) {
      List<VariableReference> element = topToBottom.previous();
      for(VariableReference var : element) {
        if(var.getName().equals(tokenName)) {
              //after verifying the variable is valid, update its use or assignment
            for(VariableReference copy : total) {
              if(copy.getName().equals(var.getName()) && 
		 copy.getLineNumber() == var.getLineNumber() &&
                 copy.getTotalDepth() == var.getTotalDepth()) { 
                if(purpose.equals("assignment")) 
                  copy.markAssignment(lineNumber); 
                else if(purpose.equals("use")) 
                  copy.markUse(lineNumber);     
              }
            }
               
          return true; 
        } // if()
      } // for()
    } // while() 
    
    System.err.println("undeclared variable " + tokenName + " on line " + lineNumber);
    System.exit(1);
 
    return false;
  }

  public void print() {

    for(VariableReference var : this.total) {
      String assignedOn = new String("  assigned to on:");

      System.err.println(var.getName());
      System.err.println("  declared on line " + var.getLineNumber() + " at nesting depth " + var.getNestingDepth());

      if( !var.assignment.isEmpty() ) {
        for(VariableReference.VarInfo info : var.assignment) {      
          assignedOn += (" " + info.line);
          if(info.count > 1) {
            assignedOn += ("("+ info.count + ")");
          } // if(in.. 
        } 
      System.err.println(assignedOn);
      } // if( !...
      else {
        System.err.println("  never assigned");
      } 

      String usedOn = new String("  used on:");
      if( !var.use.isEmpty() ) {
        for(VariableReference.VarInfo info : var.use) {
          usedOn += (" " + info.line);
          if(info.count > 1) { 
            usedOn += ("(" + info.count + ")");
          }
        }
      System.err.println(usedOn);
      } 
      else {
        System.err.println("  never used");
      }
    } // for() 
  } // print() 
} // class SymbolTable
