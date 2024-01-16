package demo.session;

import java.io.Closeable;

public interface Session extends Closeable {
     <R> R getById(Class<R> clazz, Object id);

     void flush();

}
