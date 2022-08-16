package com.example.watchApp.pizzawatchface.util;

public class FlagUtils {

	public static boolean bitwise(int a, int ... bs){
        for(int b : bs){
		    if((a & b) != 0)
		        return true;
        }
        return false;
	}

	public static boolean bitwise(int a, Enum<?> ... es){
        for(Enum<?> e : es){
            int v = getFlag(e);
            if((a & v) != 0){
                return true;
            }
        }
        return false;
	}

	public static int getFlag(Enum<?> e){
        return 1 << e.ordinal();
    }

	public static int getFlags(Enum<?> ... flags){
        int v = 0;
        for(Enum<?> flag :  flags){
            v |= getFlag(flag);
        }
        return v;
    }
}
