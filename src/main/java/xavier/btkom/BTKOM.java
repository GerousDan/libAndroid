package xavier.btkom;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static android.R.attr.id;

public class BTKOM{

    private final int SIZE_MESSAGE = 255;
    private final int SIZE_HEADER = 48;
    private BluetoothSocket socket;
    private String senderName = "Android";
    private ArrayList<Message> messages;
    private int caltoID;
    private int androidID;
    private boolean isSynchronizing = false;
    private OutputStream output;
    private InputStream input;
    private int available = 0;

    BTKOM(BluetoothSocket socket){

        messages = new ArrayList<Message>();
        this.androidID = new Random().nextInt();

        try {
            socket.connect();
            output = socket.getOutputStream();
            input = socket.getInputStream();
            //TODO generating id + SYN ACK
            listen();
            connect();
        }catch(IOException e){
            throw new IllegalArgumentException("Error binding socket");
        }

    }

    void listen(){
        Thread listening = new Thread(new Runnable(){
            int id;
            Message msg;
            String str;
            public void run(){
                ArrayList<String> packets = new ArrayList<String>();
                byte[] buffer = new byte[5120];
                int bytes;
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        if(input.available() > 0 && input.available() == available){
                            bytes = input.read(buffer);
                            int part = (buffer[44] << 8) + buffer[45];
                            int number = (buffer[46] << 8) + buffer[47];
                            if(part < number && part==1){
                                str = new String(buffer, 0, SIZE_MESSAGE);
                                for(int i=1; i<number; i++){
                                    Log.i("log", "size : " + ByteBuffer.wrap(buffer, i*SIZE_MESSAGE+40, 4).getInt());
                                    str += new String(Arrays.copyOfRange(buffer, i*SIZE_MESSAGE+SIZE_HEADER, i*SIZE_MESSAGE+SIZE_HEADER+ByteBuffer.wrap(buffer, i*SIZE_MESSAGE+40, 4).getInt()-1));
                                }
                            }else{
                                str = new String(buffer, 0, bytes);
                            }
                            msg = new Message();
                            msg.setTotalMessage(str);
                            //messages.add(msg);
                            id = msg.getID();
                            if(id != caltoID + 1){
                                Log.i("log", "id : wrong id number "+id);
                            }else{
                                treatMessage(msg);
                            }
                            available = 0;
                        }else if(input.available() != available){
                            available = input.available();
                            Thread.currentThread().sleep(100);
                        }
                    }catch (IOException | InterruptedException e) {

                    }
                }
            }
        });
        listening.start();
    }

    private void treatMessage(Message msg){
        switch(msg.getType()){
            case "SYN":

        }
    }

    private int connect() throws IOException{
        String message = "YYYY+MM+DD+HH+MM+SS"+"SYN"+Integer.toHexString(this.androidID)+"telephone\0"+"000000000101";
        output.write(message.getBytes());
        return 0;
    }

    int setSender(String senderName){
        if(senderName.length() <= 10){
            this.senderName = senderName;
            return 1;
        }
        return 0;
    }

    int sendMessage(Message message){

        return 1;
    }

    void receptMessage(){

    }

    Message getLastMessage(){
        if(messages.size() == 0) return null;
        return messages.get(messages.size()-1);
    }

    Message getMessage(int id){
        for(Message message : messages){
            if(message.getID() == id) return message;
        }
        return null;
    }

}