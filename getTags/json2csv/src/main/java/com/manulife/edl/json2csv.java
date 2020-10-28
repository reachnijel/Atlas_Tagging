package com.manulife.edl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.github.opendevl.JFlat;

public class json2csv {

	@SuppressWarnings("unused")
	public static void main(String [] args)
	{
	
	if ( args.length < 2 )
	{
		System.out.println("Usage : json2csv {json filename} {csv filename}");
		System.exit(1);
	}
	
	System.out.println(args[0]);
	System.out.println(args[1]);
	
	try
	{
	String str = new String(Files.readAllBytes(Paths.get(args[0])));

	JFlat flatMe = new JFlat(str);

	//get the 2D representation of JSON document
	List<Object[]> jsonStr = flatMe.json2Sheet().getJsonAsSheet();

	//write the 2D representation in csv format
	flatMe.write2csv(args[1]);
	} catch ( Exception e )
	{
		e.printStackTrace();
	}
	
	}
	
}
