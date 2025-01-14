package com.bigdata.hbase.phoenix;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.LiteralExpression;
import org.apache.phoenix.schema.types.PVarchar;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EncryptTest {

    private static String evaluate(final Expression expr) {
        final ImmutableBytesWritable ptr = new ImmutableBytesWritable();
        assertTrue(expr.evaluate(null, ptr));
        return (String) expr.getDataType().toObject(ptr);
    }

    @Test
    public void testEncrypt() throws Exception {
        final LiteralExpression value = LiteralExpression.newConstant("bar", PVarchar.INSTANCE);
        final LiteralExpression key = LiteralExpression.newConstant("test.hbase.col", PVarchar.INSTANCE);
        final LiteralExpression algo = LiteralExpression.newConstant("AES/CBC/PKCS5Padding", PVarchar.INSTANCE);
        System.out.println("Start: " +System.currentTimeMillis());
        String a= String.valueOf(new Encrypt(Arrays.asList(value, key)));
        System.out.println("Start: " +System.currentTimeMillis());
        assertEquals("tqQpcx7EeQ5B1RkHe9d4dA==", evaluate(new Encrypt(Arrays.asList(value, key))));

    }
}
