.class public test_bin/test$TestType
.super java/lang/Object

.field public name Llibrary/string;

.field public printName Ltest_bin/routine;

.field public static print Llibrary/print;

.method public <init>()V
.var 0 is this Ltest_bin/test$TestType;
aload 0
invokespecial java/lang/Object/<init>()V

aload 0
new test_bin/test$TestType$printName
dup
aload 0
invokespecial test_bin/test$TestType$printName/<init>(Ltest_bin/test$TestType;)V
putfield test_bin/test$TestType/printName Ltest_bin/routine;

aload 0
new library/string
dup
invokespecial library/string/<init>()V
putfield test_bin/test$TestType/name Llibrary/string;

return
.limit locals 3
.limit stack 16
.end method

.method public static $staticInitialization()V
getstatic test_bin/test$TestType/print Llibrary/print;
putstatic test_bin/test$TestType$printName/print Llibrary/print;
return
.limit locals 0
.limit stack 16
.end method
