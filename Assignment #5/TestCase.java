public class TestCase
{
    private static java.util.Scanner _sysin = new java.util.Scanner(System.in);

    private static int i;
    private static int j;
    private static int even;
    private static int odd;
    private static int prime;
    private static char ch;
    private static char str;

    public static void main(String[] args)
    {
        java.time.Instant _start = java.time.Instant.now();


        i = 3;
        ch = 'b';
        even = -990;
        odd = -999;
        prime = 0;
        
        switch(i + 1)
        {
            case 1: j = i;
                break;
            case -8: j = 8*i;
                break;
            case 5: 
            case 7: 
            case 4: j = 574*i;
                break;
        }
        System.out.printf("j = %d\n", j);
        
        switch(ch)
        {
            case 'c': 
            case 'b': str = 'p';
                break;
            case 'a': str = 'q';
                break;
        }
        System.out.printf("str = '%c'\n", str);
        
        for (i = -5; i <= 15; i++)
        {
            
            switch(i)
            {
                case 2: prime = i;
                    break;
                case -4: 
                case -2: 
                case 0: 
                case 4: 
                case 6: 
                case 8: 
                case 10: 
                case 12: even = i;
                    break;
                case -3: 
                case -1: 
                case 1: 
                case 3: 
                case 5: 
                case 7: 
                case 9: 
                case 11: 
                switch(i)
                {
                    case -3: 
                    case -1: 
                    case 1: 
                    case 9: odd = i;
                        break;
                    case 2: 
                    case 3: 
                    case 5: 
                    case 7: 
                    case 11: prime = i;
                        break;
                }
                    break;
            }
            System.out.printf("i = %d, even = %d, odd = %d, prime = %d\n", 
                              i, even, odd, prime);
        }

        java.time.Instant _end = java.time.Instant.now();
        long _elapsed = java.time.Duration.between(_start, _end).toMillis();
        System.out.printf("\n[%,d milliseconds execution time.]\n", _elapsed);
    }
}
