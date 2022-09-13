package com.smilewatermelon.kafka.second;

import lombok.Builder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class CompanySerializer implements Serializer<Company> {

    @Override
    public byte[] serialize(String topic, Company data) {
        if (data == null) {
            return null;
        }
        byte[] name, address;
        if (data.getName() != null) {
            name = data.getName().getBytes(StandardCharsets.UTF_8);
        } else {
            name = new byte[0];
        }
        if (data.getAddress() != null) {
            address = data.getAddress().getBytes(StandardCharsets.UTF_8);
        } else {
            address = new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + name.length + address.length);
        buffer.putInt(name.length);
        buffer.put(name);
        buffer.putInt(address.length);
        buffer.put(address);
        return buffer.array();
    }
}
