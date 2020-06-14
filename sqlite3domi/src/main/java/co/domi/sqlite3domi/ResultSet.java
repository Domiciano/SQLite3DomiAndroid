package co.domi.sqlite3domi;

import java.util.ArrayList;

public class ResultSet {
    private String[][] data;
    private int pointer = -1;

    public ResultSet(String[][] data){
        this.data = data;
    }

    public boolean next() {
        pointer += 1;
        if (pointer >= data.length){
            return false;
        }else{
            return true;
        }
    }

    public String getStringAt(int column) {
        return data[pointer][column];
    }

    public int getIntAt(int column) {
        try {
            int value = Integer.parseInt(data[pointer][column]);
            return value;
        }catch (NumberFormatException ex){
            ex.printStackTrace();
            return -1;
        }
    }

}
