/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/

public class LZWmod {
    private static final int R = 256;        // number of input chars
    private static int L = 512;              // number of codewords = 2^W == codebook capacity
    private static int W = 9;                // minimum codeword width
    private static final int max_W = 16;     // maximum codeword width
    private static final int max_L = 65536;  // maximum codebook capacity
    private static boolean isFullCodebook = false; // when codebook reaches the maximum capacity
    public static char extraArg = 'n';
    
    private static void incrementW()
    {
        ++W;                                        //increment codeword width(length) by 1
        if(W < max_W)                               //if the codebook not reach max capacity
        {
            L *= 2;                                 //broader the codebook capacity for 1 bit
        }
        else                                        //if the codebook reaches max capacity
        {
            W = max_W;
            isFullCodebook = true;                  //codebook reaches the maximum capacity and need to reset
        }
    }

    public static void compress() 
    { 
        TSTmod<Integer> st = new TSTmod<Integer>();
        //add the 256 input chars to codebook
        for (int i = 0; i < R; i++)                     
            st.put(new StringBuilder("" + (char) i), i);
        int codewordCounter = R+1;                             //codewordCounter keeps counting the number of
                                                               //codewords in codebook

        BinaryStdOut.write((byte) extraArg);                                                       
        //initialize the current string
        StringBuilder currStr = new StringBuilder();
        //read and append the first char
        char ch = BinaryStdIn.readChar();
        currStr.append(ch);
        Integer codeword = st.get(currStr);
        while (!BinaryStdIn.isEmpty()) 
        {
            codeword = st.get(currStr);             //get the corresponding codeword of the current string
            ch = BinaryStdIn.readChar();            //read and append the next char to current
            currStr.append(ch);
            if(!st.contains(currStr))               //if the current string not in codebook 
            {
                BinaryStdOut.write(codeword, W);    //then write it out into the zip file first
                        
                if(!isFullCodebook && codewordCounter == L) //if the codebook not reach max capacity(W < max_W)
                {                               //increment codeword width
                    incrementW();
                }
                if(isFullCodebook)              //if the codebook reaches max capacity(W >= max_W)
                {
                    if(extraArg == 'r')        //if the arg is 'r', reset codebook
                    {
                        L = 512;
                        W = 9;
                        codewordCounter = R + 1;
                        isFullCodebook = false;
                        st = new TSTmod<Integer>();
                        for (int i = 0; i < R; i++)                     
                            st.put(new StringBuilder("" + (char) i), i);
                    }
                } 
                if(codewordCounter<L)   
                {                                   //if the number of codewords does not reach codebook capacity
                    st.put(currStr, codewordCounter++);//then add the current string into the codebook
                }  
                currStr = new StringBuilder();
                currStr.append(ch);
            }
        }
        BinaryStdOut.write(st.get(currStr), W);
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    public static void expand() 
    {
        String[] st = new String[max_L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF
        i = R+1;

        extraArg = BinaryStdIn.readChar();   //Read in the mode, the first byte of the file
        if(extraArg != 'n' && extraArg != 'r') throw new IllegalArgumentException("This file contains an invalid reset argument: " + extraArg);

        int codeword = BinaryStdIn.readInt(W);
        String val = st[codeword];

        while (true) 
        {
            BinaryStdOut.write(val);

            //following 3 if cases pretty similar(do same thing) to compression
            if(i == L && !isFullCodebook)   //if the codebook not reach max capacity(W < max_W)
            {                               //increment codeword width
                incrementW();
            }
            if(isFullCodebook)              //if the codebook reaches max capacity(W >= max_W)
            {
                if(extraArg == 'r')         //if the arg is 'r', reset codebook
                {
                    L = 512;
                    W = 9;
                    i = R + 1;
                    isFullCodebook = false;
                    st = new String[max_L];
                    // initialize symbol table with all 1-character strings
                    for (i = 0; i < R; i++)
                        st[i] = "" + (char) i;
                    st[i++] = "";  
                }

            } 
                    
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];

            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L)
            {
                st[i++] = val + s.charAt(0);
            } 
            val = s;
        }
        BinaryStdOut.close();
    }
    public static void main(String[] args) {
        //read the argument 'r' for reset codebook or 'n' for do nothing
        if (args.length > 1) extraArg = args[1].charAt(0);

        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new RuntimeException("Illegal command line argument");
    }

}
