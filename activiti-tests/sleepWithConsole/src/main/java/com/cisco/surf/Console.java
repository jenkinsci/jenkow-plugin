package com.cisco.surf;

import java.io.Serializable;

public class Console implements Serializable{
    
    public void println(String s){
    	System.out.println("*** console: "+s);
    }
}
