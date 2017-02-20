package xavier.btkom;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Xavier on 26/11/2016.
 */

public class Message {

    private boolean isHeader = false;
    private boolean isType = false;
    private String type;
    private String date;
    private String message;
    private String sender;
    private int ID;

    Message(){

    }

    Message(String message){
        this.message = message;
    }

    Message(String message, String type){
        this(message);
        if(this.type.length() == 3)
            this.type = type;
        else throw new IllegalArgumentException("Type too long");

    }

    public boolean setTotalMessage(String message){
        if(message.length() < 48) return false;
        this.date = message.substring(0, 19);
        Log.i("log", "date : "+ this.date);
        this.type = message.substring(19, 22);
        Log.i("log", "type : "+ this.type);
        this.ID = ByteBuffer.wrap(message.substring(22,26).getBytes()).getInt();
        Log.i("log", "ID : "+ this.ID);
        this.sender = message.substring(26, message.indexOf('\0', 26));
        Log.i("log", "sender : "+ this.sender);
        this.message =message.substring(48, message.length());
        Log.i("log", "message : "+ this.message);
        return true;
    }

    public void setID(int ID){this.ID = ID;}

    public int setSender(String sender){
        if(sender.length() > 10) return 1;
        else this.sender = sender;
        return 0;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getType() { return this.type; }

    public int getLength(){
        return this.message.length();
    }

    public String getSender(){
        return this.sender;
    }

    public int getID(){
        return this.ID;
    }

    public String getMessage(){ return this.message; }


}
