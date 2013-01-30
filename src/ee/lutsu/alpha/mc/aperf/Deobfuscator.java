package ee.lutsu.alpha.mc.aperf;

import net.minecraft.world.gen.ChunkProviderServer;

public class Deobfuscator 
{
	private static int isObfuscated = 0;
	
	public static boolean isObfuscated()
	{
		if (isObfuscated == 0)
		{
			try
			{
				ChunkProviderServer.class.getDeclaredField("loadedChunks");
				isObfuscated = 2;
			}
			catch (NoSuchFieldException e)
			{
				isObfuscated = 1;
			}
		}
		return isObfuscated == 1;
	}
	
	public static String getFieldName(Class clazz, String original)
	{
		if (!isObfuscated())
			return original;
		
		/*
		FD: im/g net/minecraft/src/ChunkProviderServer/field_73245_g - field_73245_g,loadedChunks,2,
		*/
		
		if (clazz == net.minecraft.world.gen.ChunkProviderServer.class)
		{
			if (original.equals("loadedChunks")) return "g";
		}
		
		return null;
	}
}
