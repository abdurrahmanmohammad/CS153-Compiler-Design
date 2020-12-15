program TestFor;

var
    i, j : integer;
    ch : char;
    
begin
    for i := 1 to 5 do writeln('i = ', i);
    writeln;
    
    for i := 1 to 3 do begin
        for j := 4 downto 1 do begin
            writeln('i = ', i, ', j = ', j);
        END
    end;
    
    writeln;
    
    for ch := 'a' to 'z' do write(ch);
    writeln;
    
    for ch := 'Z' DOWNto 'A' do write(ch);
    writeln;
end.

{ -execute
i = 1
i = 2
i = 3
i = 4
i = 5

i = 1, j = 4
i = 1, j = 3
i = 1, j = 2
i = 1, j = 1
i = 2, j = 4
i = 2, j = 3
i = 2, j = 2
i = 2, j = 1
i = 3, j = 4
i = 3, j = 3
i = 3, j = 2
i = 3, j = 1

abcdefghijklmnopqrstuvwxyz
ZYXWVUTSRQPONMLKJIHGFEDCBA

                 108 statements executed.
                   0 runtime errors.
                   6 milliseconds execution time.
}

{ -convert
i = 1, j = 4
i = 1, j = 3
i = 1, j = 2
i = 1, j = 1
i = 2, j = 4
i = 2, j = 3
i = 2, j = 2
i = 2, j = 1
i = 3, j = 4
i = 3, j = 3
i = 3, j = 2
i = 3, j = 1

abcdefghijklmnopqrstuvwxyz
ZYXWVUTSRQPONMLKJIHGFEDCBA
}