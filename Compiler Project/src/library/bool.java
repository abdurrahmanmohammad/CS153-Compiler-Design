package library;

public class bool {

	public int value;
	
	public bool()
	{
	    value = 0;
	}
	
	// Constructor for int
	public bool(int value) {
		this.value = value;
	}

	// Constructor for boolean
	public bool(boolean value) {
		if (value)
			this.value = 1;
		else
			this.value = 0;
	}

	// self = other
	public bool operator_assignment(bool other) {
		this.value = other.value;
		return other;
	}

	// self += other
	public bool operator_plus(bool other) {
		this.value += other.value;
		return other;
	}

	// self -= other
	public bool operator_minus(bool other) {
		this.value -= other.value;
		return other;
	}

	// self *= other
	public bool operator_star(bool other) {
		this.value *= other.value;
		return other;
	}

	// self /= other
	public bool operator_slash(bool other) {
		this.value /= other.value;
		return other;
	}

	// #################### Static methods below ####################

	// bool1 = bool2
	public static integer operator_assignment(integer bool1, integer bool2) {
		bool1.value = bool2.value;
		return bool2;
	}

	// integer(string(x))
	public static bool operator_parenthesis(string str) {
		int result = Integer.parseInt(str.value);
		if (result == 1)
			return new bool(1);
		return new bool(0);
	}

	// bool(integer(x))
	public static bool operator_parenthesis(integer integer) {
		if (integer.value == 1)
			return new bool(1);
		return new bool(0);
	}

	// integer(real(x))
	public static bool operator_parenthesis(real real) {
		if (real.value == 1.0)
			return new bool(1);
		return new bool(0);
	}

	// bool1 - bool2
	public static bool operator_minus(integer bool1, integer bool2) {
		return new bool(bool1.value - bool2.value);
	}

	// bool1 * bool2
	public static bool operator_star(integer bool1, integer bool2) {
		return new bool(bool1.value * bool2.value);
	}

	// bool1 / bool2
	public static bool operator_slash(integer bool1, integer bool2) {
		return new bool(bool1.value / bool2.value);
	}

	// bool1 + bool2
	public static bool operator_plus(integer bool1, integer bool2) {
		return new bool(bool1.value + bool2.value);
	}

	// bool1 == bool2
	public static bool operator_equals(real bool1, real bool2) {
		return new bool(bool1.value == bool2.value);
	}

	// bool1 != bool2
	public static bool operator_not_equals(real bool1, real bool2) {
		return new bool(bool1.value != bool2.value);
	}

	// bool1 <= bool2
	public static bool operator_less_equals(real bool1, real bool2) {
		return new bool(bool1.value <= bool2.value);
	}

	// bool1 >= bool2
	public static bool operator_greater_equals(real bool1, real bool2) {
		return new bool(bool1.value >= bool2.value);
	}

	// bool1 < bool2
	public static bool operator_less_than(real bool1, real bool2) {
		return new bool(bool1.value < bool2.value);
	}

	// bool1 > bool2
	public static bool operator_greater_than(real bool1, real bool2) {
		return new bool(bool1.value > bool2.value);
	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		bool other = (bool) obj;
		if (value != other.value)
			return false;
		return true;
	}

}
