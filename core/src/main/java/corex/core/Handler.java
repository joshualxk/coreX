package corex.core;


/**
 * Created by Joshua on 2018/2/26.
 */
@FunctionalInterface
public interface Handler<E> {

    void handle(E event);
}
