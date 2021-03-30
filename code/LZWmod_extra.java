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

public class LZWmod_extra {
    private static final int R = 256;        // number of input chars
    private static int L = 512;              // number of codewords = 2^W == codebook capacity
    private static int W = 9;                // minimum codeword width
    private static final int max_W = 16;     // maximum codeword width
    private static final int max_L = 65536;  // maximum codebook capacity
    private static boolean isFullCodebook = false;             // when codebook reaches the maximum capacity
    
    //following variables are for monitoring the compression ratior and decides if to reset codebook
    private static final double CompressRatioThreshhold = 1.1; //in monitor, the ratio of compression cannot exceeds 1.1
    public static double oldCompressionRatio = -1;             
    public static double newCompressionRatio = -1;
    public static int uncompressedDataSize = 0;
    public static int compressedDataSize = 0;    
    
    //method for incrementing the codeword width by one bit
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
        int codewordCounter = R+1;                  //codewordCounter keeps counting the number of codewords in codebook                                                    
        
        StringBuilder currStr = new StringBuilder();//initialize the current string
        
        char ch = BinaryStdIn.readChar();           //read and append the first char
        currStr.append(ch);

        Integer codeword = st.get(currStr);         //get the codeword of currStr from codebook
        while (!BinaryStdIn.isEmpty()) 
        {
            codeword = st.get(currStr);             //get the corresponding codeword of the current string
            ch = BinaryStdIn.readChar();            //read and append the next char to current
            currStr.append(ch);
            if(!st.contains(currStr))               //if the current string not in codebook 
            {
                BinaryStdOut.write(codeword, W);    //then write it out into the zip file first
                uncompressedDataSize += 8*(currStr.length()-1);    //keep updating uncompressedDataSize
                compressedDataSize += W;                           //and compressedDataSize 
                        
                if(!isFullCodebook && codewordCounter == L)//if the codebook not reach max capacity(W < max_W)
                {                               
                    incrementW();                   //increment codeword width
                }
                if(isFullCodebook)                  //if the codebook reaches max capacity(W >= max_W)
                {   
                    //calculate and see if compression ratior reach the threshhold                                 
                    if (oldCompressionRatio == -1) 
                    {
                        oldCompressionRatio = ((double)(uncompressedDataSize)) / (compressedDataSize);
                    }
                    newCompressionRatio = ((double)uncompressedDataSize)/ (compressedDataSize);
                    //If so, reset codebook
                    if((oldCompressionRatio / newCompressionRatio) > CompressRatioThreshhold) 
                    {
                        oldCompressionRatio = -1;
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

        int codeword = BinaryStdIn.readInt(W);
        String val = st[codeword];

        while (true) 
        {
            uncompressedDataSize += 8*val.length();
            compressedDataSize += W;

            BinaryStdOut.write(val);

            //following three if cases are similar to the compression
            if(i == L && !isFullCodebook)        //if the codebook not reach max capacity(W < max_W)
            {                               //increment codeword width
                incrementW();
            }
            if(isFullCodebook)              //if the codebook reaches max capacity(W >= max_W)
            {
                if (oldCompressionRatio == -1) 
                {
                    oldCompressionRatio = ((double)(uncompressedDataSize)) / (compressedDataSize);
                }
                newCompressionRatio = ((double)uncompressedDataSize)/ (compressedDataSize);
                if((oldCompressionRatio / newCompressionRatio) > CompressRatioThreshhold) //that means we need reset codebook
                {
                    oldCompressionRatio = -1;
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
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new RuntimeException("Illegal command line argument");
    }

}
