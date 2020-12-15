PROGRAM TestCase;

VAR
    i, j, even, odd, prime : integer;
    ch, str : char;

BEGIN
    i := 3;  ch := 'b';
    even := -990; odd := -999; prime := 0;

    CASE i+1 OF
        1:       j := i;
        -8:      j := 8*i;
        5, 7, 4: j := 574*i;
    END;
    
    writeln('j = ', j);

    CASE ch OF
        'c', 'b' : str := 'p';
        'a'      : str := 'q'
    END;

    writeln('str = ''', str, '''');

    FOR i := -5 TO 15 DO BEGIN
        CASE i OF
            2: prime := i;
            -4, -2, 0, 4, 6, 8, 10, 12: even := i;
            -3, -1, 1, 3, 5, 7, 9, 11:  CASE i OF
                                            -3, -1, 1, 9:   odd := i;
                                            2, 3, 5, 7, 11: prime := i;
                                        END
        END;

        writeln('i = ', i, ', even = ', even, ', odd = ', odd,
                ', prime = ', prime);
    END
END.

{ -execute
j = 1722
str = 'p'
i = -5, even = -990, odd = -999, prime = 0
i = -4, even = -4, odd = -999, prime = 0
i = -3, even = -4, odd = -3, prime = 0
i = -2, even = -2, odd = -3, prime = 0
i = -1, even = -2, odd = -1, prime = 0
i = 0, even = 0, odd = -1, prime = 0
i = 1, even = 0, odd = 1, prime = 0
i = 2, even = 0, odd = 1, prime = 2
i = 3, even = 0, odd = 1, prime = 3
i = 4, even = 4, odd = 1, prime = 3
i = 5, even = 4, odd = 1, prime = 5
i = 6, even = 6, odd = 1, prime = 5
i = 7, even = 6, odd = 1, prime = 7
i = 8, even = 8, odd = 1, prime = 7
i = 9, even = 8, odd = 9, prime = 7
i = 10, even = 10, odd = 9, prime = 7
i = 11, even = 10, odd = 9, prime = 11
i = 12, even = 12, odd = 9, prime = 11
i = 13, even = 12, odd = 9, prime = 11
i = 14, even = 12, odd = 9, prime = 11
i = 15, even = 12, odd = 9, prime = 11

                 121 statements executed.
                   0 runtime errors.
                   7 milliseconds execution time.
}

{ -convert
j = 1722
str = 'p'
i = -5, even = -990, odd = -999, prime = 0
i = -4, even = -4, odd = -999, prime = 0
i = -3, even = -4, odd = -3, prime = 0
i = -2, even = -2, odd = -3, prime = 0
i = -1, even = -2, odd = -1, prime = 0
i = 0, even = 0, odd = -1, prime = 0
i = 1, even = 0, odd = 1, prime = 0
i = 2, even = 0, odd = 1, prime = 2
i = 3, even = 0, odd = 1, prime = 3
i = 4, even = 4, odd = 1, prime = 3
i = 5, even = 4, odd = 1, prime = 5
i = 6, even = 6, odd = 1, prime = 5
i = 7, even = 6, odd = 1, prime = 7
i = 8, even = 8, odd = 1, prime = 7
i = 9, even = 8, odd = 9, prime = 7
i = 10, even = 10, odd = 9, prime = 7
i = 11, even = 10, odd = 9, prime = 11
i = 12, even = 12, odd = 9, prime = 11
i = 13, even = 12, odd = 9, prime = 11
i = 14, even = 12, odd = 9, prime = 11
i = 15, even = 12, odd = 9, prime = 11

[5 milliseconds execution time.]
}