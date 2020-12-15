program TestWhile(input, output, error);

var
    i, j : integer;
    
begin
    i := 1;
    while i <= 5 do begin
        writeln('i = ', i);
        i := i + 1
    end;
    
    writeln;
    
    i := 1;
    while i <= 5 do begin
        j := 10;
        
        while j <= 30 do begin
            writeln('i = ', i, ', j = ', j);
            j := j + 10
        end;
        
        i := i + 1
    end
end.

{ -execute
i = 1
i = 2
i = 3
i = 4
i = 5

i = 1, j = 10
i = 1, j = 20
i = 1, j = 30
i = 2, j = 10
i = 2, j = 20
i = 2, j = 30
i = 3, j = 10
i = 3, j = 20
i = 3, j = 30
i = 4, j = 10
i = 4, j = 20
i = 4, j = 30
i = 5, j = 10
i = 5, j = 20
i = 5, j = 30

                  85 statements executed.
                   0 runtime errors.
                   5 milliseconds execution time.
}

{ -convert
i = 1
i = 2
i = 3
i = 4
i = 5

i = 1, j = 10
i = 1, j = 20
i = 1, j = 30
i = 2, j = 10
i = 2, j = 20
i = 2, j = 30
i = 3, j = 10
i = 3, j = 20
i = 3, j = 30
i = 4, j = 10
i = 4, j = 20
i = 4, j = 30
i = 5, j = 10
i = 5, j = 20
i = 5, j = 30

[5 milliseconds execution time.]

}