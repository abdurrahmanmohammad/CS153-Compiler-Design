print("Welcome to Calculator!)
print("Supported functions:")
print("Addition: 1")
print("Subtraction: 2")
print("Multiplication: 3")
print("Division: 4")
print("To quit, enter 0\n")


print("Enter function number:")
string functionStr = read()
integer function(functionStr)

print("Enter op1:")
string op1Str = read()
real op1 = real(op1Str)


print("Enter op2:")
string op2Str = read()
real op2 = real(op2Str)


while input != 0:
	real answer = 0
	if function == 1:
		answer = op1 + op2
	else if function == 2:
		answer = op1 - op2
	else if function == 3:
		answer = op1 * op2
	else if function == 4:
		answer = op1 / op2
	string answerStr = string(answer)
	print("Answer: " + answer)

	print("Enter function number:")
	string functionStr = read()
	integer function(functionStr)

	print("Enter op1:")
	string op1Str = read()
	real op1 = real(op1Str)


	print("Enter op2:")
	string op2Str = read()
	real op2 = real(op2Str)
	END WHILE

print("Good Bye!")