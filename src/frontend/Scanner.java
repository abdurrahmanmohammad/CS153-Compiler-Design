/**
 * Scanner class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

public class Scanner
{
    private Source source;
    private boolean inComment; //whether the scanner is currently in a comment
    
    /**
     * Constructor.
     * @param source the input source.
     */
    public Scanner(Source source)
    {
        this.source = source;
    }
    
    public boolean isWhitespace(char ch)
    {
    	if(ch == '{')
    		inComment = true;
    	
    	else if(inComment && ch == '}')
    	{
    		inComment = false;
    		return true; //no longer in a comment, but the character still counts as whitespace
    	}
    	
    	return Character.isWhitespace(ch) || inComment;
    }
    
    /**
     * Extract the next token from the source.
     * @return the token.
     */
    public Token nextToken()
    {
        char ch = source.currentChar();
        
        // Skip blanks and other whitespace characters.
        while (isWhitespace(ch)) ch = source.nextChar();
        
        if (Character.isLetter(ch))     return Token.word(ch, source);
        else if (Character.isDigit(ch)) return Token.number(ch, source);
        else if (ch == '\'')            return Token.string(ch, source);
        else                            return Token.specialSymbol(ch, source);
    }
}
