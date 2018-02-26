package corex.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corex.proto.ModelProto;
import corex.proto.ModelProto.ListValue;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joshua on 2018/2/27.
 */
public class CodecTest {

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Long> map;
    ModelProto.Struct struct;

    List<String> list;
    ListValue listValue;

    byte[] bs1;
    byte[] bs2;

    @Before
    public void before() throws JsonProcessingException {
        int n = 1000;
        map = new HashMap<>();
        list = new ArrayList<>();
        ModelProto.Struct.Builder builder = ModelProto.Struct.newBuilder();
        ListValue.Builder listBuilder = ListValue.newBuilder();
        for (int i = 0; i < n; ++i) {
            map.put("param" + i, (long) i);
            list.add("str" + i);
            builder.putFields("param" + i, ModelProto.Value.newBuilder().setLongValue(i).build());
            listBuilder.addValues(ModelProto.Value.newBuilder().setStringValue("str" + i));
        }

        struct = builder.build();
        listValue = listBuilder.build();

        bs1 = objectMapper.writeValueAsBytes(map);
        bs2 = struct.toByteArray();
    }

    @Test
    public void sizeTest1() throws IOException {
        byte[] bs = objectMapper.writeValueAsBytes(map);
        System.out.println(bs.length);
        byte[] bs2 = struct.toByteArray();
        System.out.println(bs2.length);
    }

    @Test
    public void sizeTest2() throws IOException {
        byte[] bs = objectMapper.writeValueAsBytes(list);
        System.out.println(bs.length);
        byte[] bs2 = listValue.toByteArray();
        System.out.println(bs2.length);
    }

    @Test
    public void speedTest1() throws IOException {
        for (int i = 0; i < 10000; ++i) {
//            struct.toByteArray();
            listValue.toByteArray();
        }
    }

    @Test
    public void speedTest2() throws IOException {
        for (int i = 0; i < 10000; ++i) {
//            objectMapper.writeValueAsBytes(map);
            objectMapper.writeValueAsBytes(list);
        }
    }

    @Test
    public void speedTest3() throws IOException {
        for (int i = 0; i < 10000; ++i) {
            ModelProto.Struct.parseFrom(bs2);

        }
    }

    @Test
    public void speedTest4() throws IOException {
        for (int i = 0; i < 10000; ++i) {
            objectMapper.readValue(bs1, Map.class);
        }
    }

}
