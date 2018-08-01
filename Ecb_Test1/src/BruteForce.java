import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BruteForce 
{
	public static void main(String[] args) 
	{
		//Take file name argument and use the file in byte format
		System.out.println("Reading Plaintext File..");
		File pFile = new File(args[0]);
		byte[] pInputBytes = readFile(pFile);
		        		
		//Take file name argument and use the file in byte format
		System.out.println("Reading Encrypted File..");
		File cFile = new File(args[1]);
		byte[] cInputBytes = readFile(cFile);
        
		String pBinaryText=Bytes2Binary(pInputBytes);
		String cBinaryText=Bytes2Binary(cInputBytes);
      
		//code to create 32-bit blocks
        System.out.println("Creating Blocks..");
        String []pBitBlocks=createBlocks(pBinaryText);
        String []cBitBlocks=createBlocks(cBinaryText);
        
        //code to add padding to last block
        System.out.println("Padding plaintext..");
        pBitBlocks[pBitBlocks.length-1]=addPadding(pBitBlocks[pBitBlocks.length-1]);
        
        pBitBlocks = swapBlocks(pBitBlocks);
        
        //code to bruteforce
        System.out.println("Bruteforcing..");
        long startTime = System.currentTimeMillis();
        BreakKey(pBitBlocks,cBitBlocks);
        long stopTime = System.currentTimeMillis();
		System.out.println("Time elapsed: "+(stopTime - startTime)+" milli seconds");
     }
	
	public static String BreakKey(String[] pBitBlocks,String[] cBitBlocks)
	{	
		//write a method to generate next 32 bit key
		String hexKey="80000000";
		long y = Long.parseLong(hexKey,16);
		int intVal = (int)(y & 0xffffffff);
		String Key = Integer.toBinaryString(intVal);
		int j = 0;
				
		while((Key.length()>31)&&(Key.length()<33))
		{
			System.out.println("Keys tried so far: "+j++);
			//Decrypt only block intially to save time
			String PotentialPlainBlock =DecryptFirstBlock(cBitBlocks[0],Key);
			//Dig further if 1st block matches
			if(PotentialPlainBlock.equals(pBitBlocks[0]))
			{
				//decrypt all blocks when first block matches
				String[] PotentialSolution = DecryptBlocks2(cBitBlocks,Key);
				int i = 0;
				for(String s:PotentialSolution)
				{
					if(s.equals(pBitBlocks[i]))
					{
						i++;
						if(i==3)
						{
							System.out.println("The key used is: "+Binary2Hex(Key));
							return Key;
						}
					}
					else
						break;
				}
			}
			
			long k = Long.parseLong(hexKey,16);
			hexKey = Long.toHexString(++k);
			Key = GenerateNext32BitbKey(hexKey);
					
		}
		return Key;
	}
	
	public static String[] swapBlocks(String[] AllBlocks)
	{
		String tmp="";
		int loopLimit = 0;
		
		if(AllBlocks.length%2==0)
			loopLimit = AllBlocks.length;
		else
			loopLimit = AllBlocks.length-1;
		
		for(int i=0;i<loopLimit;i++)
		{
			tmp=AllBlocks[i];
			AllBlocks[i]=AllBlocks[i+1];
			AllBlocks[++i]=tmp;
		}
		
		return AllBlocks;
	}
	
	public static String Binary2Hex(String Key)
	{
		return Long.toHexString(Long.parseLong(Key,2)).toUpperCase();
	}
		
	public static String GenerateNext32BitbKey(String Key)
	{
		long x = Long.parseLong(Key,16);
		int intVal = (int)(x & 0xffffffff);
		intVal++;
		return Integer.toBinaryString(intVal);
	}
	
	public static String DecryptFirstBlock(String cBlock,String key)
	{
		String pBlock = "";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < cBlock.length(); i++)
		{
			sb.append(((int)cBlock.charAt(i) ^(int) key.charAt(i % key.length())));
			pBlock=sb.toString();
		}
		
		return pBlock;
	}
	
	public static String[] DecryptBlocks(String[] AllBlocks,String key)
	{
		String[]  DecryptedBlocks = new String[AllBlocks.length];
		String str="";
		int j=0;
		
		for(String s:AllBlocks)
		{
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < s.length(); i++)
			{
				sb.append(((int)s.charAt(i) ^(int) key.charAt(i % key.length())));
				str=sb.toString();
			}
			DecryptedBlocks[j]=str;
			j++;
		}
		
		return DecryptedBlocks;
	}
	

	public static String[] DecryptBlocks2(String[] AllBlocks,String key)
	{
		String[]  DecryptedBlocks = new String[AllBlocks.length];
		int j=0;
		
		long x = Long.parseLong(key,2);
		int intVal = (int)(x & 0xffffffff);
		String dKey = getOnesComp(intVal);
				
		for(String s:AllBlocks)
		{			
			StringBuilder sb = new StringBuilder();
			
			if(j%2==0)
			for(int i = 0; i < s.length(); i++)
			    sb.append(((int)s.charAt(i) ^(int) key.charAt(i % key.length())));
			else
				for(int i = 0; i < s.length(); i++)
				    sb.append(((int)s.charAt(i) ^(int) dKey.charAt(i % dKey.length())));
					
			DecryptedBlocks[j]=sb.toString();
			j++;
		}
		
		return DecryptedBlocks;
	}
	
	public static String[] createBlocks(String InputBinary)
	{
		return InputBinary.split("(?<=\\G.{32})");
	}
	
	public static String getOnesComp(int intVal)
	{
		String dKey = Integer.toBinaryString(~intVal);
		
		if(dKey.length()<32)
		{
			String tmp="";
			for(int j=0;j<32-dKey.length();j++)
    			tmp+="0";
    		
			dKey=tmp+dKey;
		}
		
		return(dKey);
	}
	
	public static String addPadding(String UnpadedBlock)
	{
		String pad= "1";
		for(int i = 0; i<31-UnpadedBlock.length() ;i++)
		{
			pad = pad+"0";
		}
		
		return UnpadedBlock+pad;
	}
	
	public static String Bytes2Binary(byte[] InputBytes)
	{
		String binaryText="";
		
		for (byte b : InputBytes) 
		{
			String str ="";
        	
			if(Integer.toBinaryString(b).length()>8)
        		str=Integer.toBinaryString(b).substring(Integer.SIZE - Byte.SIZE);   
        	else
        		str = Integer.toBinaryString(b);
        	
        	//add padding to individualize bytes
        	if(str.length()<8)
        	{
        		String tmp="";
        		for(int j=0;j<8-str.length();j++)
        			tmp+="0";
        		
        		str=tmp+str;
        	}
        	           	
        	binaryText+=str;
		}
				
		return binaryText;
	}
	
	public static byte[] readFile(File inputFile)
	{		
		byte[] inputBytes = null;
		try 
        {
            //Read file
			FileInputStream inputStream = new FileInputStream(inputFile);
            inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
			//System.out.write(inputBytes);
            
            //Close File.
            inputStream.close();
            System.out.println("Read " + inputBytes.length + " bytes");
            
            //writeFile("outimg2.jpg",inputBytes);
        }//Exception Handling
        catch(FileNotFoundException ex)
        {
            System.out.println("Following file does not exist: '" +inputFile.getName());                
        }
        catch(IOException ex) 
        {
            System.out.println("Error in reading file '" + inputFile.getName() + "Ex: "+ex);
        }
		
		return inputBytes;
	}
	
	public static void writeBytesToFile(String outputFile, byte[] outputBytes)
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(outputFile);
		    outputStream.write(outputBytes);
		    outputStream.close();
		}
		catch(IOException ex) 
        {
            System.out.println("Error in writing file '" + outputFile + "Ex: "+ex);
        }
	}

}
