import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;


public class Encrypt 
{

	public static void main(String[] args) 
	{
		
							
		if(args.length == 3) 
		{
			//Take arguments from command line
			String Key = args[0];
			//Key to hexadecimal
			long x = Long.parseLong(Key,16);
			int intVal = (int)(x & 0xffffffff);
			Key = Integer.toBinaryString(intVal);
						
			if(Key.length()<32||Key.length()>32)
			{
				System.out.println("Please enter a 32 bit hex key (Any Key bw 80000000 & FFFFFFFF).");
			}
			else
			{
				String OutFileName = args[2];
	            
				//Take file name argument and use the file in byte format
				System.out.println("Reading File..");
				File File = new File(args[1]);
				byte[] InputBytes = readFile(File);
				
				if(InputBytes.length<=0)
				{
					System.out.println("Please use a valid message.");
				}
				else
				{
				
					System.out.println("Getting binary..");
					String BinaryText=Bytes2Binary(InputBytes);
									
		            //code to create 32-bit blocks
		            System.out.println("Creating Blocks..");
		            String []bitBlocks=createBlocksForEncryption(BinaryText);
		            
		            //code to add padding to last block
		            System.out.println("Padding..");
		            bitBlocks[bitBlocks.length-1]=addPaddingForEncryption(bitBlocks[bitBlocks.length-1]);
		            
		            //swap alternate blocks to create confusion
		            bitBlocks = swapBlocks(bitBlocks);
		            
		            //code for encryption
		            System.out.println("Encrypting..");
		            String CypherText = EncryptBlocks2(bitBlocks,Key);
		            byte[] OutputBytes = Binary2Bytes(CypherText);
		                                  
		            //code to output encrypted file
		            System.out.println("Writing Output file..");
		            writeBytesToFile(OutFileName,OutputBytes);
		            
		            System.out.println("Done!");
				}
			}
		}
		else
		{
			System.out.println("Incorrect Input format.");
		}
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
	
	public static String EncryptBlocks2(String[] AllBlocks,String key)
	{
		long x = Long.parseLong(key,2);
		int intVal = (int)(x & 0xffffffff);
		String dKey = getOnesComp(intVal);
		String CyText = "";
		int j=0;
		
		for(String s:AllBlocks)
		{			
			if(s.equals("10000000000000000000000000000000"))
			{
				CyText="00000000000000000000000000000000";
				break;
			}
			
			StringBuilder sb = new StringBuilder();
			
			if(j%2==0)
			for(int i = 0; i < s.length(); i++)
			    sb.append(((int)s.charAt(i) ^(int) key.charAt(i % key.length())));
			else
				for(int i = 0; i < s.length(); i++)
				    sb.append(((int)s.charAt(i) ^(int) dKey.charAt(i % dKey.length())));
			
			CyText = CyText+sb.toString();
			j++;
		}
		
		return CyText;
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
	
	public static byte[] Binary2Bytes(String BinaryText)
	{
		 byte[] OutputBytes = new byte[(BinaryText.length())/8];
		 int i =0;
		 for(String str:splitByNumber(BinaryText, 8))
         {
         	OutputBytes[i]=(byte)(Integer.parseInt(str,2));
         	i++;
         }
		 return OutputBytes;
	}
	
	public static String[] createBlocksForEncryption(String InputBinary)
	{
		return InputBinary.split("(?<=\\G.{32})");
	}
	
	public static String addPaddingForEncryption(String UnpadedBlock)
	{
		String pad= "1";
		for(int i = 0; i<31-UnpadedBlock.length() ;i++)
		{
			pad = pad+"0";
		}
		
		return UnpadedBlock+pad;
	}
	
	public static String[] splitByNumber(String str, int size) {
	    return (size<1 || str==null) ? null : str.split("(?<=\\G.{"+size+"})");
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
