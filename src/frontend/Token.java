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
		token.lineNumber = source.lineNumber();
		int pointCount = 0;

		// Loop to get the rest of the characters of the number token.
		// Append digits to the token.
		for (char ch = source.nextChar(); Character.isDigit(ch) || (ch == '.'); ch = source.nextChar()) {
			if (ch == '.')
				pointCount++;
			token.text += ch;
		}

		if (pointCount == 0) { // Integer constant.
			token.type = TokenType.INTEGER;
			token.value = Long.parseLong(token.text);
		} else if (pointCount == 1) { // Real constant.
			token.type = TokenType.REAL;
			token.value = Double.parseDouble(token.text);
		} else {
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
		token.lineNumber = source.lineNumber();
		char ch;
		// True when we find a ' character that could be used as an escape;
		boolean escaped = false; // Mainly represents whether ' was the previous character

		// Loop to append the rest of the characters of the string
		// unless we reach a closing ' or the EOF
		for (ch = source.nextChar(); ch != Source.EOF; ch = source.nextChar()) {
			// If the previous char was ', but the current char isn't ',
			// so we know we're at the end of a string
			if (escaped && ch != '\'')
				break;
			else if (escaped && ch == '\'') {
				// The previous char was ', and the current char is ',
				// so we know the previous was an escape char
				escaped = false;
				token.text += ch;
			} else if (ch == '\'') {
				// The current char is ', so it's either an escape char,
				// or it's the end of the string
				escaped = true;
			} else
				token.text += ch; // Standard case
		}

		// Is the token a single character surrounded by two quotes?
		// If so, it's a Character (size = 3)
		if (ch == Source.EOF) {
			token.type = TokenType.STRING;
			tokenError(token, "String not closed");
		} else {
			token.text += '\''; // Append the closing ' (it has already been consumed)
			// Don't include the leading and trailing ' in the value.
			token.value = token.text.substring(1, token.text.length() - 1);
			if (token.text.length() == 3)
			{
			    token.type = TokenType.CHARACTER;
			    token.value = token.text.charAt(1);
			}
				
			else
				token.type = TokenType.STRING; // If not, it's a String
		}

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
		token.lineNumber = source.lineNumber();

		switch (firstChar) {
		case '.': {
			char nextChar = source.nextChar();

			if (nextChar == '.') { // Is it the .. symbol?
				token.text += nextChar;
				token.type = TokenType.DOT_DOT;
			} else { // No, it's just the . symbol.
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

			if (nextChar == '>') { // Symbol: <>
				token.text += nextChar;
				token.type = TokenType.NOT_EQUALS;
			} else if (nextChar == '=') { // Symbol: <=
				token.text += nextChar;
				token.type = TokenType.LESS_EQUALS;
			} else { // No, it's just the < symbol.
				token.type = TokenType.LESS_THAN;
				return token; // already consumed <
			}
			break;
		}
		case '>': {
			char nextChar = source.nextChar();

			if (nextChar == '=') { // Symbol: <=
				token.text += nextChar;
				token.type = TokenType.GREATER_EQUALS;
			} else { // No, it's just the < symbol.
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

			// Is it the := symbol?
			if (nextChar == '=') {
				token.text += nextChar;
				token.type = TokenType.COLON_EQUALS;
			} else { // No, it's just the : symbol.
				token.type = TokenType.COLON;
				return token; // already consumed :
			}
			break;
		}
		case Source.EOF:
			token.type = TokenType.END_OF_FILE;
			break;
		default:
			tokenError(token, "Invalid token");
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
