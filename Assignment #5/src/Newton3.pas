PROGRAM Newton3;

VAR
    number : integer;

FUNCTION root(x : real) : real;
    VAR
        r, prev, diff : real;

    BEGIN
        r := 1;
        prev := 0;
        
        REPEAT
            r := (x/r + r)/2;
            diff := r - prev;
            IF diff < 0 THEN diff := -diff;
            prev := r
        UNTIL diff < 1.0e-10;
        
        root := r
    END;

PROCEDURE print(n : integer; root : real);
    BEGIN
        writeln('The square root of ', n:4, ' is ', root:8:4)
    END;

BEGIN
    FOR number := 1 TO 25 DO BEGIN
        print(number, root(number))
    END
END.

{ -execute
The square root of    1 is   1.0000
The square root of    2 is   1.4142
The square root of    3 is   1.7321
The square root of    4 is   2.0000
The square root of    5 is   2.2361
The square root of    6 is   2.4495
The square root of    7 is   2.6458
The square root of    8 is   2.8284
The square root of    9 is   3.0000
The square root of   10 is   3.1623
The square root of   11 is   3.3166
The square root of   12 is   3.4641
The square root of   13 is   3.6056
The square root of   14 is   3.7417
The square root of   15 is   3.8730
The square root of   16 is   4.0000
The square root of   17 is   4.1231
The square root of   18 is   4.2426
The square root of   19 is   4.3589
The square root of   20 is   4.4721
The square root of   21 is   4.5826
The square root of   22 is   4.6904
The square root of   23 is   4.7958
The square root of   24 is   4.8990
The square root of   25 is   5.0000

                 960 statements executed.
                   0 runtime errors.
                  17 milliseconds execution time.
}

{ -convert
The square root of    1 is   1.0000
The square root of    2 is   1.4142
The square root of    3 is   1.7321
The square root of    4 is   2.0000
The square root of    5 is   2.2361
The square root of    6 is   2.4495
The square root of    7 is   2.6458
The square root of    8 is   2.8284
The square root of    9 is   3.0000
The square root of   10 is   3.1623
The square root of   11 is   3.3166
The square root of   12 is   3.4641
The square root of   13 is   3.6056
The square root of   14 is   3.7417
The square root of   15 is   3.8730
The square root of   16 is   4.0000
The square root of   17 is   4.1231
The square root of   18 is   4.2426
The square root of   19 is   4.3589
The square root of   20 is   4.4721
The square root of   21 is   4.5826
The square root of   22 is   4.6904
The square root of   23 is   4.7958
The square root of   24 is   4.8990
The square root of   25 is   5.0000

[7 milliseconds execution time.]
}