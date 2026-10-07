import java.io.IOException;

public class Main {

    static class Node { // Used to store the value for car and linked list for cdr
        String val;      
        Node car; // First value
        Node cdr; // The rest
        boolean isNil;   
    }

    // Notes on Node class: There are 3 different cases for a node, which represent different things:
    // 1. atom: if a Node n has n.val != null, it is storing an atom
    // 2. empty list: when Node n has n.isNil == true, the list is empty
    // 3. Pair: when a Node n has n.val == null but is not nil. 

    // A Node n will initially hold a list, where n.car will contain a value,
    // and n.cdr will contain another pair, which will recursively hold more values and pairs.
    // If a list ends, n.isNil becomes true and the list stops recursing

    static int peekChar = ' '; // A blank space because it is absorbed by main initially
    static String nextToken = null;
    static Node rho = nil(); // Globals
    static Node locals = nil(); // Locals

    static Node nil() { // This should be called by the switch statement
        Node n = new Node();
        n.isNil = true;
        return n;
    }

    static Node t() {
        Node t = new Node();
        t.val = "'T";
        return t;
    }

    static Node evalList(Node n) {
        // This function is called when n has no value, but instead has a cdr
        // and the current car 
        if (n == null || n.isNil) return n;
        Node result = new Node();
        result.car = evalAtom(n.car);  
        result.cdr = evalList(n.cdr); 
        return result;
    }

    static Node lookup(Node symbol) { // helper function for looking symbols up
        // how to lookup: go through rho, and if a .car.car conta

        // Start with checking for a local variable
        Node stack = locals;
        while (stack != null && !stack.isNil) {
            Node names = stack.car.car; // this is the PARAMETERS list   
            Node vals  = stack.car.cdr.car; // and the ARGUMENTS lsit
            while (names != null && !names.isNil) {
                if (names.car.val.equals(symbol.val)) return vals.car;
                names = names.cdr;
                vals = vals.cdr;
            }
            stack = stack.cdr;              
        }

        // Now search for a global variable
        Node values = rho;
        while (values != null && !values.isNil) {
            // look for the specific symbol
            if (values.car.car.val.equals(symbol.val)) {
                return values.car.cdr.car; // can't return ...car.val because must return a Node
            }
            values = values.cdr;
        }
        return symbol; // If not found, stay same
    }

    static Node evalAtom(Node n) {
        if (n == null || n.isNil) return n;
        if (n.val != null) return lookup(n);

        Node arg;
        Node num1;
        Node num2;
        if (n.car != null && n.car.val != null) {
            switch (n.car.val.toLowerCase()) {
                case "eval":
                    arg = evalAtom(n.cdr.car); 
                    return evalAtom(arg); 
                case "quote":
                    return n.cdr.car; 
                case "'": // same as above
                    return n.cdr.car; // Looks weird, but this is a quote ( ' ) surrounded by " ". Necessary case to handle. 
                case "car": {
                    arg = evalAtom(n.cdr.car); // Go to next value's car
                    return (arg == null || arg.isNil) ? arg : arg.car; 
                }
                case "cdr": {
                    arg = evalAtom(n.cdr.car);
                    return (arg == null || arg.isNil) ? arg : arg.cdr;
                }
                case "cons": {
                    Node a = evalAtom(n.cdr.car);
                    Node b = evalAtom(n.cdr.cdr.car);
                    Node pair = new Node();
                    pair.car = a;
                    pair.cdr = b;
                    return pair;
                }
                case "set": {
                    Node symbol = n.cdr.car; // If correctly formatted, I shouldn't have to format a symbol. It should jsut be (x).
                    Node val = evalAtom(n.cdr.cdr.car); // Do need to format value

                    Node prevNodes = new Node();
                    prevNodes.car = val;
                    prevNodes.cdr = nil();

                    Node temp = new Node();
                    temp.car = symbol;
                    temp.cdr = prevNodes;

                    Node newEntry = new Node(); // Create the new node to set to Rho, which has new pair as its car and old vals as its cdr
                    newEntry.car = temp;
                    newEntry.cdr = rho; // Add all of rho
                    rho = newEntry; // rho now includes newEntry

                    return val; // Do I need a return value? If so what? Revisit later
                }
                case "nil?": {
                    arg = evalAtom(n.cdr.car);
                    if (arg != null && arg.isNil) {
                        return t();
                    }
                    return nil();
                }
                case "atom?": {
                    arg = evalAtom(n.cdr.car);
                    if (arg != null && arg.val != null) {
                        return t();
                    }
                    return nil();
                }
                case "list?": {
                    arg = evalAtom(n.cdr.car);
                    if (arg != null && !arg.isNil && arg.val == null) {
                        return t();
                    }
                    return nil();
                }
                case "and?": {
                    Node arg1 = evalAtom(n.cdr.car);
                    if (arg1 == null || arg1.isNil) return nil(); // Short circuiting means determine if node 1 is Nil or null
                    Node arg2 = evalAtom(n.cdr.cdr.car);          // before determining node 2's status
                    if (arg2 == null || arg2.isNil) return nil();                    
                    return t();
                }
                case "or?": {
                    Node arg1 = evalAtom(n.cdr.car);
                    if (arg1 != null && !arg1.isNil) return t(); // Short circuiting means determine if node 1 is Nil or null
                    Node arg2 = evalAtom(n.cdr.cdr.car);          // before determining node 2's status
                    if (arg2 != null && !arg2.isNil) return t();                    
                    return nil();
                }
                case "eq?": { // Have to set both atoms before evaluating them, because 2 nil nodes are equal
                    Node arg1 = evalAtom(n.cdr.car); 
                    Node arg2 = evalAtom(n.cdr.cdr.car); 
                    if (arg1 == null || arg2 == null) return nil();
                    if (arg1.isNil || arg2.isNil) return nil(); // Even if both are nil, you still return nil

                    if ((arg1 != null && arg2 != null) && (arg1.val != null && arg2.val != null) && arg1.val.equals(arg2.val))
                        return t();
                    return nil();
                }
                case "if": { // Once again, unnecessary code but formatted 'well' for debugging
                    Node a0 = evalAtom(n.cdr.car);
                    if(a0 == null || a0.isNil) {
                        Node a2 = evalAtom(n.cdr.cdr.cdr.car);
                        return a2;
                    }
                    Node a1 = evalAtom(n.cdr.cdr.car);
                    return a1;

                }
                case "cond": {
                    Node clause = n.cdr.car; // Used to have n.cdr.car, but that did not work as intended. Started 1 layer too deep
                                         // No, actually n.cdr.car was correct. 
                    while (clause != null && !clause.isNil) {
                        Node testExpr = clause.car;
                        clause = clause.cdr;
                        if (clause == null || clause.isNil) return nil();

                        Node valueExpr = clause.car;
                        clause = clause.cdr;
                        Node test = evalAtom(testExpr);

                        if (test != null && !test.isNil) return evalAtom(valueExpr);
                    }
                    return nil();
                }
                case "add": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil) return nil();

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    Node ret = new Node();
                    ret.val = String.valueOf(doubleOne + doubleTwo);

                    return ret;
                }
                case "sub": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil) return nil();

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    Node ret = new Node();
                    ret.val = String.valueOf(doubleOne - doubleTwo);

                    return ret;
                }
                case "mul": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil) return nil();

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    Node ret = new Node();
                    ret.val = String.valueOf(doubleOne * doubleTwo);

                    return ret;
                }
                case "div": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil || Double.parseDouble(num2.val.trim()) == 0) return nil(); // Will this have rounding error?

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    Node ret = new Node();
                    ret.val = String.valueOf(doubleOne / doubleTwo);

                    return ret;
                }
                case "rem": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil || Double.parseDouble(num2.val.trim()) == 0) return nil(); // Remainder of 0 cannot happen

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    Node ret = new Node();
                    ret.val = String.valueOf(doubleOne % doubleTwo);

                    return ret;
                }
                case "lt": {
                    num1 = evalAtom(n.cdr.car);
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil) return nil(); 

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    if (doubleOne < doubleTwo) return t();
                    return nil();
                }
                case "gt": { // This is not a required funciton, but it's a copy paste of above with one difference.
                    num1 = evalAtom(n.cdr.car); // Might as well have it
                    if (num1 == null || num1.isNil) return nil();
                    num2 = evalAtom(n.cdr.cdr.car);
                    if (num2 == null || num2.isNil) return nil(); 

                    Double doubleOne = Double.parseDouble(num1.val.trim());
                    Double doubleTwo = Double.parseDouble(num2.val.trim());
                    if (doubleOne > doubleTwo) return t();
                    return nil();
                }
                case "def": { // Going to build a function list, then treat it the same
                    // Use example: (def foo (arg1 arg2) (plus arg1 arg2))

                    Node name = n.cdr.car;
                    Node args = n.cdr.cdr.car;
                    Node body = n.cdr.cdr.cdr.car;

                    Node functionExpression = new Node(); // this will be where the funciton expr is built.
                    functionExpression.car = new Node();
                    functionExpression.car.val = "function";

                    functionExpression.cdr = new Node();
                    functionExpression.cdr.car = args;

                    functionExpression.cdr.cdr = new Node();
                    functionExpression.cdr.cdr.car = body;
                    functionExpression.cdr.cdr.cdr = nil(); // By here, we have function -> args -> body.

                    Node setExpression = new Node();
                    setExpression.car = new Node();
                    setExpression.car.val = "set";

                    setExpression.cdr = new Node();
                    setExpression.cdr.car = name; // Because name is a node, we don't set ...car.val to name, just ...car
                    setExpression.cdr.cdr = new Node();
                    setExpression.cdr.cdr.car = functionExpression; // by here, we have set -> name -> function -> args -> body
                    setExpression.cdr.cdr.cdr = nil();

                    return evalAtom(setExpression);

                 }
                case "function": { // Functions are lists built of 3 parts, so return the list
                    return n; 
                }
            } // end switch

            // NTS: (set square (function (n) (mul n n))) is the format of setting a function, and
            //      (square (add 2 3)) calls it.
            // IMPORTANT NOTE: I have left a lot of notes on this section for myself. 
            // It took me hours to grasp function lookup for calling functions.
            // I may make references to (set square (function (n) (mul n n))) a lot, as this is the function
            // I used to understand the process.
            
            Node fn = lookup(n.car); // search locals and rho

            if ((fn != null && !fn.isNil && fn.val == null) 
            && (fn.car != null && fn.car.val != null)  // function, function car, and its val are not null, and it is a function
            && (fn.car.val.toLowerCase().equals("function"))) {

                Node parameters = fn.cdr.car; // the expected value, such as (n)
                Node body = fn.cdr.cdr.car; // what the function runs, such as (mul n n)
                Node arguments = evalList(n.cdr); // what the parameter actually is, such as 5 or (add 2 3)

                Node argumentPair = new Node(); // Sole purpose is to store the list of arguments
                argumentPair.car = arguments; // only need a car, cdr can be nil()
                argumentPair.cdr = nil();

                Node frame = new Node(); // Stores the parameters and arguments. Pretty straight forward code      
                frame.car = parameters; 
                frame.cdr = argumentPair;

                Node saved = locals; // Store CURRENT locals before adding any more
                Node newLocals = new Node(); // as in what will be put on the localStack until function is done 
                newLocals.car = frame; // new val
                newLocals.cdr = locals; // old val(s)
                locals = newLocals; // assignment is done here

                Node result = evalAtom(body); // body was what function runs, so now it can be evaluated.
                locals = saved; // RESTORE old locals, as we don't need the temporary local vals for this function anymore.                  
                return result;

            }
        }
        return evalList(n);
    }

    // token system: ------------------------------------------------
    static void advanceChar() {
        try {
            peekChar = System.in.read();
        } catch (IOException e) {
            peekChar = -1; // Didnt work
        }
    }

    static String peekToken() { // Purpose is to only peek the next important character
        if (nextToken == null) {
            while (peekChar != -1 && (Character.isWhitespace(peekChar) || peekChar == ',')) { // Ignore whitespace/comma and continue
                advanceChar();
            }

            if (peekChar == -1) return null; 
            
            if (peekChar == '(' || peekChar == ')' || peekChar == '\'') {
                nextToken = String.valueOf((char) peekChar);
                advanceChar();
            } else {
                StringBuilder sb = new StringBuilder(); // While not another '(' or ')', continue to build current String
                while (peekChar != -1 && !Character.isWhitespace(peekChar) && peekChar != ',' && peekChar != '(' && peekChar != ')') {
                    sb.append((char) peekChar);
                    advanceChar();
                }
                nextToken = sb.toString();
            }
        }
        return nextToken;
    }

    static String getToken() {
        String token = peekToken();
        nextToken = null; 
        return token;
    }

    // Notes on tokenization: I chose to not use Scanner class because it does not seperate two values
    // unless there is whitespace, which caused issues for cases such as ...(a... or (val).
    // I instead used the token system to build strings based on where ( and ) occured and use those for
    // seperating car and cdr values within S expressions.
    // I used section 4.4 of Crafting Interpreters as guidence for creating the tokenizing framework.
    // ---------------------------------------------------

    static Node readExpr() { 
        String token = peekToken();
        if (token == null) return null;

        if (token.equals("(")) {
            getToken();
            return readList(); 
        }

        if (token.equals(")")) {
            throw new RuntimeException("Syntax Error: Unexpected closing parenthesis ')'"); // Prevent issues from extra ) in expressions
        }

        if (token.equals("'")) {
            getToken(); // clear out the '
            Node quoted = readExpr(); // get the expression that is quoted

            if (quoted == null) {
                throw new RuntimeException("Syntax Error: Unexpected EOF");
            }

            Node quote = new Node(); // Transform into a quote() statement (for simplicity)
            quote.val = "quote";

            Node rest = new Node();
            rest.car = quoted;

            rest.cdr = nil();
            Node pair = new Node();

            pair.car = quote; // FIXME: might be worth, if I have time, finding a more efficient way to complete this instead of using a built quote() statement.
            pair.cdr = rest;  // FIXME: this current solution will produce bugs for '''a and similar cases. 
            return pair;      // I don't know how to practically fix this.
        }

        Node atom = new Node();
        atom.val = getToken();
        return atom;
        
    }

    static Node readList() throws RuntimeException{
        String token = peekToken();

        // Build up car --------------
        if (token == null) {
            throw new RuntimeException("Syntax Error: Unclosed expression");
        }

        if (token.equals(")")) { 
            getToken(); // Consume ')'
            Node nil = new Node();
            nil.isNil = true;
            return nil;
        }
    
        // Because ')' has been found, that atom is built. Start building cdr
        Node pair = new Node();
        pair.car = readExpr(); // Call self again to 
        if (pair.car == null) {
            throw new RuntimeException("Syntax Error: Unexpected EOF");
        }
        pair.cdr = readList(); 
        return pair;
    }

    static void printExpr(Node n) {
        if (n == null) return;

        if (n.isNil) { // Different from n == null
            System.out.print("()");
        } else if (n.val != null) {
            // System.out.print("temp ");
            System.out.print(n.val);
        } else {
            System.out.print("(");
            printList(n);
            System.out.print(")");
        }
    }

    static void printList(Node n) {
        if (n == null || n.isNil) return;
    
        printExpr(n.car);
    
        if (n.cdr != null && !n.cdr.isNil) { // If node has a cdr, that becomes new list recursively printed
            System.out.print(" ");
            printList(n.cdr);
        }
    }

    public static void main(String[] args) {
        
        advanceChar();

        Node expr;
        while ((expr = readExpr()) != null) {
            Node result = evalAtom(expr);
            printExpr(result);
            System.out.println();
        }
    }
}