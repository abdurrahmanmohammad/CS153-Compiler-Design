.class public test_bin/test$TestType$printName
.super test_bin/routine

.field public type Ltest_bin/test$TestType;

.field public printName Ltest_bin/routine;

.field public static print Llibrary/print;

.method public <init>(Ltest_bin/test$TestType;)V
.var 0 is this Ltest_bin/test$TestType$printName;
aload 0
invokespecial test_bin/routine/<init>()V

aload 0
aload 1
putfield test_bin/test$TestType$printName/type Ltest_bin/test$TestType;

return
.limit locals 3
.limit stack 16
.end method

.method public operator_parenthesis()V
.var 0 is this Ltest_bin/test$TestType$printName;
getstatic test_bin/test$TestType$printName/print Llibrary/print;
aload 0
getfield test_bin/test$TestType$printName/type Ltest_bin/test$TestType;
getfield test_bin/test$TestType/name Llibrary/string;

invokevirtual library/print/operator_parenthesis(Llibrary/string;)V

return
.limit locals 2
.limit stack 16
.end method
