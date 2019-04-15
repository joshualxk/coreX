package io.bigoldbro.corex.model;

import io.bigoldbro.corex.NetData;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Created by Joshua on 2019/04/15.
 */
public class ErrorMsg implements NetData {

    public int code;
    public String msg;

    public ErrorMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ErrorMsg() {

    }

    @Override
    public void read(DataInput dataInput) throws Exception {
        this.code = dataInput.readInt();
        this.msg = dataInput.readUTF();
    }

    @Override
    public void write(DataOutput dataOutput) throws Exception {
        dataOutput.writeInt(code);
        dataOutput.writeUTF(msg);
    }
}
