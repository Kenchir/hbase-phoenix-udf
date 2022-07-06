package com.bigdata.hbase.phoenix;

import java.io.DataInput;
import java.io.IOException;
import java.net.URLClassLoader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import com.bigdata.hbase.phoenix.service.AesEncryption;
import com.bigdata.hbase.phoenix.util.Helper;
import net.thisptr.jackson.jq.Scope;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.security.User;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.parse.FunctionParseNode.Argument;
import org.apache.phoenix.parse.FunctionParseNode.BuiltInFunction;
import org.apache.phoenix.schema.ColumnModifier;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PVarchar;
import org.apache.phoenix.expression.function.*;
import org.apache.phoenix.util.StringUtil;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@BuiltInFunction(name = Encrypt.NAME, args = {
        @Argument(allowedTypes = {PVarchar.class}), // raw data
        @Argument(allowedTypes = {PVarchar.class}),// identifier

})

// CREATE FUNCTION decrypt(VARCHAR, VARCHAR, VARCHAR CONSTANT DEFAULTVALUE='AES/CBC/PKCS5Padding') RETURNS VARCHAR AS 'com.bigdata.hbase.phoenix.Decrypt';
public class Decrypt extends ScalarFunction {
    private final static Logger logger = LoggerFactory.getLogger(Decrypt.class);

    public static final String NAME = "DECRYPT";

    private String identifier;
    volatile String algorithm = "AES/CBC/PKCS5Padding";
    private final AesEncryption aesEncryption = new AesEncryption();


    private final  Helper helper = new Helper();

    static {
        // Force initializing Scope object when JsonQueryFunction is loaded using the Scope classloader. Otherwise,
        // built-in jq functions are not loaded.
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Scope.class.getClassLoader());
        try {
            Scope.rootScope();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }



    public Decrypt() {
    }

    public Decrypt(final List<Expression> children) throws SQLException {
        super(children);
        init();
    }


    private void init() {
        final ImmutableBytesWritable id = new ImmutableBytesWritable();
        if (!getChildren().get(1).evaluate(null, id))
            throw new RuntimeException("key: the 2nd argument must be a varchar.");
        this.identifier = (String) PVarchar.INSTANCE.toObject(id);

    }

    @Override
    public boolean evaluate(final Tuple tuple, final ImmutableBytesWritable ptr) {
        final Expression inArg = getChildren().get(0);
        if (!inArg.evaluate(tuple, ptr)) {
            return false;
        }

        String encKey = helper.getKeyFromCache("hbase",identifier);


        if (Objects.equals(encKey, "Invalid") || Objects.equals(encKey, "unauthorized")){
            ptr.set(encKey.getBytes());
            return  true;
        }

        String plainText = aesEncryption.decrypt(this.algorithm,(String) PVarchar.INSTANCE.toObject(ptr),encKey);

        ptr.set(plainText.getBytes());
        return true;

    }

    @Override
    public void readFields(final DataInput input) throws IOException {
        super.readFields(input);
        init();
    }

    @Override
    public PDataType<?> getDataType() {
        return PVarchar.INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
