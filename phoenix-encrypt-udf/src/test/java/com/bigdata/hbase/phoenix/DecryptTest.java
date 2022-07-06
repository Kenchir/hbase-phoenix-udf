package com.bigdata.hbase.phoenix;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.LiteralExpression;
import org.apache.phoenix.schema.types.PVarchar;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DecryptTest {
    private static String evaluate(final Expression expr) {
        final ImmutableBytesWritable ptr = new ImmutableBytesWritable();
        assertTrue(expr.evaluate(null, ptr));
        return (String) expr.getDataType().toObject(ptr);
    }

    @Test
    public void testDecrypt() throws Exception {
        final LiteralExpression value = LiteralExpression.newConstant("b2vgwX61osfSwv/pMEBQzg==", PVarchar.INSTANCE);
            final LiteralExpression key = LiteralExpression.newConstant("test.hbase.col", PVarchar.INSTANCE);

        assertEquals("123456789", evaluate(new Decrypt(Arrays.asList(value, key))));
    }
}
