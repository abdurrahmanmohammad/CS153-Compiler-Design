.class public test
.super java/lang/Object

.method public <init>()V
.var 0 is this Ltest;
aload 0
invokespecial java/lang/Object/<init>()V
return
.limit locals 1
.limit stack 1
.end method

.method public static main([Ljava/lang/String;)V
.var 0 is print Llibrary/print;
.var 1 is read Llibrary/read;
.var 2 is t Ltest_bin/test$TestType;

new library/print
dup
invokespecial library/print/<init>()V
astore 0
aload 0
putstatic test_bin/test$TestType/print Llibrary/print;
invokestatic test_bin/test$TestType/$staticInitialization()V
new test_bin/test$TestType
dup
invokespecial test_bin/test$TestType/<init>()V
astore 2
aload 2
getfield test_bin/test$TestType/name Llibrary/string;

new library/string
dup
ldc "hi"
invokespecial library/string/<init>(Ljava/lang/String;)V
invokevirtual library/string/operator_assignment(Llibrary/string;)V
aload 2
getfield test_bin/test$TestType/printName Ltest_bin/routine;
invokevirtual test_bin/routine/operator_parenthesis()V

return
.limit locals 3
.limit stack 16
.end method
