package demo.session;

import java.io.Closeable;

public interface Session extends Closeable {
     <R, T> R getById(Class<R> clazz, T id);

}
