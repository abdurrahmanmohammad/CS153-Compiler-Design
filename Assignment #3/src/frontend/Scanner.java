/**
 * Scanner class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

public class Scanner {
	private Source source;

	/**
	 * Constructor.
	 * 
	 * @param source the input source.
	 */
	public Scanner(Source source) {
		this.source = source;
	}

	/**
	 * Extract the next token from the source.
	 * 
	 * @return the token.
	 */
	public Token nextToken() {
		char ch = source.currentChar();
		// Skip blanks and other whitespace characters.
		// Skips: blanks -> comments -> blanks -> comments -> ...
		while (ch == '{' || Character.isWhitespace(ch)) {
			if (ch == '{')
				while (ch != '}')
					ch = source.nextChar();
			ch = source.nextChar();
		}
		if (Character.isLetter(ch)) // A-Z & a-z
			return Token.word(ch, source);
		else if (Character.isDigit(ch)) // 0-9
			return Token.number(ch, source);
		else if (ch == '\'') // '
			return Token.string(ch, source);
		else { // Special Symbols
			return Token.specialSymbol(ch, source);

		}
	}
}