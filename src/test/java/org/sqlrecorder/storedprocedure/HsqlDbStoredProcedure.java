package org.sqlrecorder.storedprocedure;

public class HsqlDbStoredProcedure {

    public static void spWithoutParams(){
        System.out.println("Executing stored procedure spWithoutParams");
    }

    public static void spWithParams(String s, int i, double d){
        System.out.format("Executing stored procedure spWithParams: %s, %d, %f",s,i,d);
    }
}
