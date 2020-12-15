type TestType:
	string name
	
	def printName:
		print(name)
	end printName
	
end TestType

TestType t
t.name = "hi"
t.printName()

