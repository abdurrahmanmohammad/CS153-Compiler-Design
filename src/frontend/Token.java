/**
 * Token class for a simple interpreter.
 * 
 * (c) 2020 by Ronald Mak
 * Department of Computer Science
 * San Jose State University
 */
package frontend;

import java.util.HashMap;

public class Token {
	// Assignment 2: Added additional reserved word starting from DIV to OF
	public enum TokenType {
		PROGRAM, BEGIN, END, REPEAT, UNTIL, WRITE, WRITELN, PERIOD, COLON, COLON_EQUALS, SEMICOLON, PLUS, MINUS, STAR,
		SLASH, LPAREN, RPAREN, EQUALS, LESS_THAN, IDENTIFIER, INTEGER, REAL, STRING, END_OF_FILE, ERROR, DIV, MOD, AND,
		OR, NOT, CONST, TYPE, VAR, PROCEDURE, FUNCTION, WHILE, DO, FOR, TO, DOWNTO, IF, THEN, ELSE, CASE, OF, CHARACTER,
		COMMA, NOT_EQUALS, LESS_EQUALS, GREATER_EQUALS, GREATER_THAN, LBRACKET, RBRACKET, CARAT, DOT_DOT
	}

	/**
	 * The table (as a hashmap) of reserved words. Initialize the table.
	 */
	private static HashMap<String, TokenType> reservedWords;
	static {
		reservedWords = new HashMap<String, TokenType>();

		reservedWords.put("PROGRAM", TokenType.PROGRAM);
		reservedWords.put("BEGIN", TokenType.BEGIN);
		reservedWords.put("END", TokenType.END);
		reservedWords.put("REPEAT", TokenType.REPEAT);
		reservedWords.put("UNTIL", TokenType.UNTIL);
		reservedWords.put("WRITE", TokenType.WRITE);
		reservedWords.put("WRITELN", TokenType.WRITELN);
		// Assignment 2: Added additional reserved word
		reservedWords.put("DIV", TokenType.DIV);
		reservedWords.put("MOD", TokenType.MOD);
		reservedWords.put("AND", TokenType.AND);
		reservedWords.put("OR", TokenType.OR);
		reservedWords.put("NOT", TokenType.NOT);
		reservedWords.put("CONST", TokenType.CONST);
		reservedWords.put("TYPE", TokenType.TYPE);
		reservedWords.put("VAR", TokenType.VAR);
		reservedWords.put("PROCEDURE", TokenType.PROCEDURE);
		reservedWords.put("FUNCTION", TokenType.FUNCTION);
		reservedWords.put("WHILE", TokenType.WHILE);
		reservedWords.put("DO", TokenType.DO);
		reservedWords.put("FOR", TokenType.FOR);
		reservedWords.put("TO", TokenType.TO);
		reservedWords.put("DOWNTO", TokenType.DOWNTO);
		reservedWords.put("IF", TokenType.IF);
		reservedWords.put("THEN", TokenType.THEN);
		reservedWords.put("ELSE", TokenType.ELSE);
		reservedWords.put("CASE", TokenType.CASE);
		reservedWords.put("OF", TokenType.OF);
	}

	public TokenType type; // what type of token
	public int lineNumber = 0; // source line number of the token (made this static!!!)
	public String text = ""; // text of the token
	public Object value = null; // the value (if any) of the token

	/**
	 * Constructor.
	 * 
	 * @param firstChar the first character of the token.
	 */
	private Token(char firstChar) {
		this.text += firstChar;
	}

	/**
	 * Construct a word token.
	 * 
	 * @param firstChar the first character of the token.
	 * @param source    the input source.
	 * @return the word token.
	 */
	public static Token word(char firstChar, Source source) {
		Token token = new Token(firstChar);
		token.lineNumber = source.lineNumber();

		// Loop to get the rest of the characters of the word token.
		// Append letters and digits to the token.
		for (char ch = source.nextChar(); Character.isLetterOrDigit(ch); ch = source.nextChar()) {
			token.text += ch;
		}

		// Is it a reserved word or an identifier?
		token.type = reservedWords.get(token.text.toUpperCase());
		if (token.type == null)
			token.type = TokenType.IDENTIFIER;

		return token;
	}

	/**
	 * Construct a number token and set its value.
	 * 
	 * @param firstChar the first character of the token.
	 * @param source    the input source.
	 * @return the number token.
	 */
	public static Token number(char firstChar, Source source) {
		Token token = new Token(firstChar);
		int pointCount = 0;

		// Loop to get the rest of the characters of the number token.
		// Append digits to the token.
		for (char ch = source.nextChar(); Character.isDigit(ch) || (ch == '.'); ch = source.nextChar()) {
			if (ch == '.')
				pointCount++;
			token.text += ch;
		}

		// Integer constant.
		if (pointCount == 0) {
			token.type = TokenType.INTEGER;
			token.value = Long.parseLong(token.text);
		}

		// Real constant.
		else if (pointCount == 1) {
			token.type = TokenType.REAL;
			token.value = Double.parseDouble(token.text);
		}

		else {
			tokenError(token, "Invalid number");
			token.type = TokenType.ERROR;
		}

		return token;
	}

	/**
	 * Construct a string token and set its value.
	 * 
	 * @param firstChar the first character of the token.
	 * @param source    the input source.
	 * @return the string token.
	 */
	public static Token string(char firstChar, Source source) {
		Token token = new Token(firstChar); // the leading '

		// Loop to append the rest of the characters of the string,
		// up to but not including the closing quote.
		/*
		 * for (char ch = source.nextChar(); ch != '\''; ch = source.nextChar()) {
		 * token.text += ch; }
		 */
		char ch1 = source.nextChar();
		char ch2 = source.nextChar();
		while (!(ch1 == '\'' && Character.isWhitespace(ch2))) {
			if (ch1 == Source.EOF)
				break; // No closing char
			if (ch1 == '\'' && ch2 == '\'')
				ch2 = source.nextChar();
			token.text += ch1;
			ch1 = ch2;
			ch2 = source.nextChar();
		}

		token.text += ch1; // append the closing ' (unless error)

		if (token.text.length() == 3)
			token.type = TokenType.CHARACTER;
		else if (ch1 == Source.EOF) { // EOF
			tokenError(token, "String not closed at");
			token.type = TokenType.ERROR;
		} else
			token.type = TokenType.STRING;

		// Don't include the leading and trailing ' in the value.
		token.value = token.text.substring(1, token.text.length() - 1);

		return token;
	}

	/**
	 * Construct a special symbol token and set its value.
	 * 
	 * @param firstChar the first character of the token.
	 * @param source    the input source.
	 * @return the special symbol token.
	 */
	public static Token specialSymbol(char firstChar, Source source) {
		Token token = new Token(firstChar);

		switch (firstChar) {
		case '.': {
			char nextChar = source.nextChar();

			// Is it the .. symbol?
			if (nextChar == '.') {
				token.text += nextChar;
				token.type = TokenType.DOT_DOT;
			}

			// No, it's just the . symbol.
			else {
				token.type = TokenType.PERIOD;
				return token; // already consumed :
			}
		}
			break;
		case ';':
			token.type = TokenType.SEMICOLON;
			break;
		case '+':
			token.type = TokenType.PLUS;
			break;
		case '-':
			token.type = TokenType.MINUS;
			break;
		case '*':
			token.type = TokenType.STAR;
			break;
		case '/':
			token.type = TokenType.SLASH;
			break;
		case '=':
			token.type = TokenType.EQUALS;
			break;
		case '<': {
			char nextChar = source.nextChar();
			token.text += nextChar;

			if (nextChar == '>') // Symbol: <>
				token.type = TokenType.NOT_EQUALS;
			else if (nextChar == '=') // Symbol: <=
				token.type = TokenType.LESS_EQUALS;
			else { // No, it's just the < symbol.
				token.type = TokenType.LESS_THAN;
				return token; // already consumed <
			}
			break;
		}
		case '>': {
			char nextChar = source.nextChar();
			token.text += nextChar;

			if (nextChar == '=') // Symbol: <=
				token.type = TokenType.GREATER_EQUALS;
			else { // No, it's just the < symbol.
				token.type = TokenType.GREATER_THAN;
				return token; // already consumed <
			}
			break;
		}
		case '(':
			token.type = TokenType.LPAREN;
			break;
		case ')':
			token.type = TokenType.RPAREN;
			break;
		case '[':
			token.type = TokenType.LBRACKET;
			break;
		case ']':
			token.type = TokenType.RBRACKET;
			break;
		case '^':
			token.type = TokenType.CARAT;
			break;
		case ',':
			token.type = TokenType.COMMA;
			break;
		case ':': {
			char nextChar = source.nextChar();
			token.text += nextChar;

			// Is it the := symbol?
			if (nextChar == '=') {
				token.type = TokenType.COLON_EQUALS;
			}

			// No, it's just the : symbol.
			else {
				token.type = TokenType.COLON;
				return token; // already consumed :
			}

			break;
		}
		case Source.EOF:
			token.type = TokenType.END_OF_FILE;
			break;
		default:
			tokenError(token, "Invalid token at");
			token.type = TokenType.ERROR;
		}

		source.nextChar(); // consume the special symbol
		return token;
	}

	/**
	 * Handle a token error.
	 * 
	 * @param token   the bad token.
	 * @param message the error message.
	 */
	private static void tokenError(Token token, String message) {
		System.out.println("TOKEN ERROR at line " + token.lineNumber + ": " + message + " at '" + token.text + "'");
	}
}
