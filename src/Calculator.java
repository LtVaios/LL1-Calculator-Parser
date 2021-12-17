import java.io.InputStream;
import java.io.IOException;
import java.lang.Math;

/* My LL1 grammar is:
exp -> term exp2
exp2 -> '+' term exp2
      |'-' term exp2
      | e
term -> factor term2
term2 -> '**'  term
       | e
factor -> num
        | (exp)
num -> digit num2
num_2 -> num 
       | e
digit -> 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9
*/

/* My lookahead table is:
(NOTE: Some words in the table are written in short: f=factor , t2=term2 , e2=exp2)
* ------------------------------------------------------------------------------------------------------
* 	       |     '0' .. '9'      |  '+'       |       '-'       |  '**'   |  '('         |   ')'   |     $   
* ------------------------------------------------------------------------------------------------------
* 	       |		             |	          |	                |         |              |         |   
* exp      |'0' .. '9' term exp2 |   err      |        err      |  err    |'0'..'9' t e2 |   err   |  err
*          | 	   	             |	          |    	            |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* exp2     |       err    	     |'+' t e2    |   '-' t e2      |   err   |   err        |  ')' e  |  e
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* term     |  '0' .. '9' f t2    |   err      |       err       |   err   |'0'..'9' f t2 |   err   |  err
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* term2    |        err	         |    e       |         e       |'**' term|   err        |   e     |   e   
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* factor   |    '0' .. '9' num   |  err       |      err        |   err   |'(' exp)      |   err   |  err
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* num      |'0' .. '9' digit num2|   err      |       err       |   err   | err          |  err    |   err
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* num2     |    '0' .. '9' num   |    e       |         e       |    e    |   err        |    e    |  e
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*          |		             |	          |		            |         |              |         |
* digit    |    '0' .. '9' e     |   err      |        err      |  err    |  err         |    err  | err
* 	       |	  	             |	          |    	     	    |         |              |         |
* ------------------------------------------------------------------------------------------------------
*/

class Calculator {
    private final InputStream in;
    private int lookahead;
    private String temp_str=new String("");

    public Calculator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else{
            throw new ParseError();
        }
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int result=0;
        int value = exp(result);
        return value;
    }

    private int exp(int result) throws IOException, ParseError {
        if (isDigit(lookahead) || lookahead=='('){
            result=term(result);
            result=exp_2(result);
            return result;
        }
        else {
            throw new ParseError();
        }          
    }

    private int exp_2(int result) throws IOException, ParseError {
        switch (lookahead) {
            case '+':
                consume(lookahead);
                result+=term(result);
                result=exp_2(result);
                return result;
            case '-':
                consume(lookahead);
                result-=term(result);
                result=exp_2(result);
                return result;
            case ')':
                return result;
            case -1:
                return result;
            case '\n':
                return result;
            case '\r':
                return result;
        }
        throw new ParseError();
    }

    private int term(int result) throws IOException, ParseError {
        if (isDigit(lookahead) || lookahead=='('){
            result=factor(result);
            result=term_2(result);
            return result;
        }
        else
            throw new ParseError();
    }

    private int term_2(int result) throws IOException, ParseError {
        switch (lookahead) {
            case '*':
                consume('*');
                consume('*');
                result=(int)Math.pow(result,term(result));
                //result=term_2(result);
                return result;
            case '+':
                return result;
            case '-':
                return result;
            case ')':
                return result;
            case -1:
                return result;
            case '\n':
                return result;
            case '\r':
                return result;
        }
        throw new ParseError();
    }

    private int factor(int result) throws IOException, ParseError {
        if (isDigit(lookahead)){
            temp_str="";
            result=num(result);
            return result;
        }
        else if(lookahead=='('){
            consume('(');
            result=exp(result);
            consume(')');
            return result;
        }
        else
            throw new ParseError();
    }

    private int num(int result) throws IOException, ParseError {
        if (isDigit(lookahead)){
            temp_str+=String.valueOf(digit());
            result=num_2(result);
            result=Integer.parseInt(temp_str);
            return result;
        }
        else
            throw new ParseError(); 
    }

    private int num_2(int result) throws IOException, ParseError {
        if (isDigit(lookahead)){
            result=num(result);
            return result;
        }
        else
            switch (lookahead) {
                case '*':
                    return result;
                 case '+':
                    return result;
                case '-':
                    return result;
                case ')':
                    return result;
                case -1:
                    return result;
                case '\n':
                    return result;
                case '\r':
                    return result;
            }
        throw new ParseError();
    }

    private int digit() throws IOException, ParseError {
        int number = evalDigit(lookahead);
        consume(lookahead);
        return number;
    }
}
