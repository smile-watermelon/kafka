package com.smilewatermelon.kafka.three.chapter;

import com.smilewatermelon.kafka.two.chapter.Company;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


/**
 * 反序列化
 */
public class CompanyDeserializer implements Deserializer<Company> {


    @Override
    public Company deserialize(String topic, byte[] data) {

        if (data.length < 8) {
            throw new SerializationException("size if data received " + data.length + " by demo serializer is shorter than expected");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int nameLen, addressLen;
        String name, address;

        nameLen = buffer.getInt();
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);

        addressLen = buffer.getInt();
        byte[] addressBytes = new byte[addressLen];
        buffer.get(addressBytes);

        name = new String(nameBytes, StandardCharsets.UTF_8);
        address = new String(addressBytes, StandardCharsets.UTF_8);

        return new Company(name, address);
    }

    /**
     * 使用protostuff 反序列化对象
     *
     * @param topic
     * @param data
     * @return
     */
    public Company protostuffDeserialize(String topic, byte[] data) {
        Schema<Company> schema = RuntimeSchema.getSchema(Company.class);
        Company company = new Company();
        ProtostuffIOUtil.mergeFrom(data, company, schema);
        return company;
    }
}
